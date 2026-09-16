package com.ccs.javadroid.ai;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.util.AppPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A file-aware coding agent backed by Gemini's native function-calling protocol.
 *
 * <p>The agent never edits "where the cursor happens to be". It reads a project
 * file, identifies one exact and unique fragment, and queues a replacement for
 * that canonical path. A virtual in-memory copy is updated after every accepted
 * edit, so a second tool call sees the first change instead of stale source.</p>
 */
public class GeminiAgent {

    public interface AgentCallback {
        void onToolCall(String toolName, String args);
        void onToolResult(String toolName, String result);
        void onTextResponse(String text);
        void onError(String error);
        void onDone();
    }

    private static final int MAX_ITERATIONS = 14;
    private static final int MAX_FILE_CHARS = 600_000;
    private static final int MAX_TREE_ENTRIES = 600;
    private static final int MAX_SEARCH_FILES = 3_000;
    private static final long CONFIRMATION_TIMEOUT_SECONDS = 120;

    private static final Set<String> SKIPPED_DIRECTORIES = new HashSet<>(Arrays.asList(
            ".git", ".gradle", ".idea", "build", "target", "out", "node_modules"));
    private static final Set<String> SEARCHABLE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "java", "kt", "kts", "xml", "gradle", "properties", "json", "md", "txt",
            "yaml", "yml", "toml", "html", "htm", "css", "js", "ts", "tsx", "jsx",
            "c", "cc", "cpp", "h", "hpp", "py", "sh", "sql", "gitignore"));

    /** Activity context is intentional: confirmation dialogs need its window. */
    private final Context context;
    private final AgentCallback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService agentExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, String> workingFiles = new HashMap<>();

    private volatile boolean cancelled;
    private long turnId;
    private int iterationCount;
    private String currentCode = "";
    private String currentFileName = "";
    private String currentFilePath = "";
    private String configuredProjectRoot = "";
    private String workingProjectRoot = "";
    private JSONArray conversationContents = new JSONArray();

    public GeminiAgent(Context context, AgentCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /** Backwards-compatible entry point for callers that do not know the file path. */
    public void send(String userMessage, String codeContext, String fileName,
                     List<GeminiService.ChatMessage> history) {
        send(userMessage, codeContext, fileName, "", "", history);
    }

    public void send(String userMessage, String codeContext, String fileName, String filePath,
                     List<GeminiService.ChatMessage> history) {
        send(userMessage, codeContext, fileName, filePath, "", history);
    }

    /** Starts one complete agent turn, including all function calls it needs. */
    public void send(String userMessage, String codeContext, String fileName, String filePath,
                     String projectRoot, List<GeminiService.ChatMessage> history) {
        final long token;
        synchronized (this) {
            cancelled = false;
            token = ++turnId;
            iterationCount = 0;
            currentFileName = fileName == null ? "" : fileName;
            configuredProjectRoot = projectRoot == null ? "" : projectRoot;
            String rootKey = canonicalProjectRoot();
            if (!rootKey.equals(workingProjectRoot)) workingFiles.clear();
            workingProjectRoot = rootKey;
            currentFilePath = canonicalCurrentPath(filePath);
            String stagedCurrent = currentFilePath.isEmpty()
                    ? null : workingFiles.get(currentFilePath);
            currentCode = stagedCurrent != null ? stagedCurrent
                    : (codeContext == null ? "" : codeContext);
            if (!currentFilePath.isEmpty()) workingFiles.put(currentFilePath, currentCode);
            conversationContents = buildInitialContents(userMessage, history);
        }
        requestNext(token);
    }

    private JSONArray buildInitialContents(String userMessage,
                                            List<GeminiService.ChatMessage> history) {
        JSONArray contents = new JSONArray();
        try {
            if (history != null) {
                for (GeminiService.ChatMessage item : history) {
                    contents.put(message(item.isFromUser() ? "user" : "model",
                            new JSONObject().put("text", item.getText())));
                }
            }
            contents.put(message("user", new JSONObject().put("text",
                    userMessage == null ? "" : userMessage)));
        } catch (JSONException impossible) {
            throw new IllegalStateException(impossible);
        }
        return contents;
    }

    private void requestNext(long token) {
        if (!isCurrent(token)) return;
        final JSONObject body;
        try {
            body = buildAgentRequest();
        } catch (JSONException e) {
            finishError(token, "Could not build the agent request: " + e.getMessage());
            return;
        }
        GeminiService.generateContent(context, body, new GeminiService.JsonResponseCallback() {
            @Override public void onSuccess(JSONObject response) {
                if (!isCurrent(token)) return;
                try {
                    agentExecutor.execute(() -> processResponse(token, response));
                } catch (RejectedExecutionException ignored) {
                    // The activity was destroyed while the network call completed.
                }
            }

            @Override public void onError(String error) {
                finishError(token, error);
            }
        });
    }

    private JSONObject buildAgentRequest() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("systemInstruction", new JSONObject().put("parts",
                new JSONArray().put(new JSONObject().put("text", systemInstruction()))));
        body.put("contents", conversationContents);

        body.put("tools", new JSONArray().put(new JSONObject()
                .put("functionDeclarations", toolDeclarations())));
        body.put("toolConfig", new JSONObject().put("functionCallingConfig",
                new JSONObject().put("mode", "AUTO")));

        JSONObject generation = new JSONObject();
        generation.put("temperature", 0.2);
        generation.put("maxOutputTokens", 8192);
        generation.put("thinkingConfig", new JSONObject().put("thinkingBudget", 0));
        body.put("generationConfig", generation);
        return body;
    }

    private String systemInstruction() {
        String visible = currentFilePath.isEmpty()
                ? (currentFileName.isEmpty() ? "none" : currentFileName)
                : relativePath(new File(currentFilePath));
        return "You are a coding agent inside JavaDroid. The current file is " + visible + ".\n"
                + "Work like a careful IDE agent: inspect relevant files before changing them, "
                + "follow references when needed, and use project-relative paths.\n"
                + "For every change to an existing file call editFile(path, find, replace). `find` "
                + "must be copied exactly from the latest file content and must occur exactly once. "
                + "Include enough surrounding lines to make it unique.\n"
                + "To add an import, field, method, or block, replace a stable nearby anchor with "
                + "that same anchor plus the new code in `replace`. Never use a cursor position. "
                + "Never rewrite a whole file to make a local change.\n"
                + "After editFile succeeds, its changed content becomes the latest version for later "
                + "tool calls. If it reports not found or ambiguous, read the file and retry with a "
                + "correct longer fragment. Use createFile only for a genuinely new file.\n"
                + "Do not paste code in the final response when tools can apply it. At the end, give "
                + "a concise summary of files changed and what was done.";
    }

    private JSONArray toolDeclarations() throws JSONException {
        JSONArray declarations = new JSONArray();
        declarations.put(declaration("getCurrentFile",
                "Read the complete latest contents and project-relative path of the file open in the editor.",
                new JSONObject(), new JSONArray()));
        declarations.put(declaration("getProjectStructure",
                "List the project tree so you can locate relevant files.",
                new JSONObject(), new JSONArray()));

        JSONObject readProps = new JSONObject();
        readProps.put("path", stringProperty("Project-relative path of the file to read."));
        declarations.put(declaration("readFile", "Read the latest contents of one project file.",
                readProps, new JSONArray().put("path")));

        JSONObject listProps = new JSONObject();
        listProps.put("path", stringProperty(
                "Project-relative directory to list. Omit for the project root."));
        declarations.put(declaration("listFiles", "List the immediate children of a directory.",
                listProps, new JSONArray()));

        JSONObject searchProps = new JSONObject();
        searchProps.put("query", stringProperty("Literal, case-sensitive text to search for."));
        searchProps.put("path", stringProperty(
                "Optional project-relative file or directory to limit the search."));
        searchProps.put("maxResults", new JSONObject().put("type", "integer")
                .put("description", "Maximum matches to return, from 1 to 100."));
        declarations.put(declaration("searchInProject",
                "Search project text files and return path, line number, and matching line.",
                searchProps, new JSONArray().put("query")));

        JSONObject editProps = new JSONObject();
        editProps.put("path", stringProperty("Project-relative path of the existing file to edit."));
        editProps.put("find", stringProperty(
                "Exact unique text copied from the latest file contents, preserving all whitespace."));
        editProps.put("replace", stringProperty(
                "Exact replacement text. Use an empty string only to delete the matched fragment."));
        declarations.put(declaration("editFile",
                "Replace one exact unique fragment in a specific existing file.",
                editProps, new JSONArray().put("path").put("find").put("replace")));

        JSONObject createProps = new JSONObject();
        createProps.put("path", stringProperty("Project-relative path for the new file."));
        createProps.put("content", stringProperty("Complete contents of the new file."));
        declarations.put(declaration("createFile",
                "Create a new project file. Refuses to overwrite an existing file.",
                createProps, new JSONArray().put("path").put("content")));
        return declarations;
    }

    private static JSONObject declaration(String name, String description, JSONObject properties,
                                          JSONArray required) throws JSONException {
        JSONObject parameters = new JSONObject();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        if (required.length() > 0) parameters.put("required", required);
        return new JSONObject().put("name", name).put("description", description)
                .put("parameters", parameters);
    }

    private static JSONObject stringProperty(String description) throws JSONException {
        return new JSONObject().put("type", "string").put("description", description);
    }

    private void processResponse(long token, JSONObject response) {
        if (!isCurrent(token)) return;
        if (++iterationCount > MAX_ITERATIONS) {
            finishError(token, "The agent reached its tool-call limit before finishing.");
            return;
        }
        try {
            JSONArray candidates = response.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                finishError(token, "Gemini returned no candidate response.");
                return;
            }
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.optJSONObject("content");
            JSONArray parts = content == null ? null : content.optJSONArray("parts");
            if (content == null || parts == null || parts.length() == 0) {
                finishError(token, "The agent stopped without a response ("
                        + candidate.optString("finishReason", "empty response") + ").");
                return;
            }

            // Preserve the exact model turn, including opaque signatures used by
            // newer Gemini models to validate subsequent function responses.
            conversationContents.put(content);
            List<JSONObject> calls = new ArrayList<>();
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.optJSONObject(i);
                if (part == null) continue;
                JSONObject call = part.optJSONObject("functionCall");
                if (call != null) calls.add(call);
                String piece = part.optString("text", "");
                if (!piece.isEmpty()) {
                    if (text.length() > 0) text.append('\n');
                    text.append(piece);
                }
            }

            if (calls.isEmpty()) {
                if (text.length() == 0) finishError(token,
                        "The agent returned neither text nor a tool call.");
                else finishText(token, text.toString());
                return;
            }

            JSONArray responseParts = new JSONArray();
            for (JSONObject call : calls) {
                if (!isCurrent(token)) return;
                String name = call.optString("name", "");
                JSONObject args = call.optJSONObject("args");
                if (args == null) args = new JSONObject();
                postToolCall(name, args.toString());
                String result = executeTool(name, args);
                if (!isCurrent(token)) return;
                postToolResult(name, result);

                JSONObject functionResponse = new JSONObject();
                functionResponse.put("name", name);
                if (call.has("id")) functionResponse.put("id", call.optString("id"));
                functionResponse.put("response", new JSONObject().put("result", result));
                responseParts.put(new JSONObject().put("functionResponse", functionResponse));
            }
            conversationContents.put(new JSONObject().put("role", "user")
                    .put("parts", responseParts));
            requestNext(token);
        } catch (Exception e) {
            finishError(token, "Could not process the agent response: " + e.getMessage());
        }
    }

    private String executeTool(String name, JSONObject args) {
        try {
            if (("editFile".equals(name) || "createFile".equals(name))
                    && !new AppPreferences(context).isInclusiveMode()
                    && !confirmChange(name, args)) {
                return "Denied by user.";
            }
            switch (name) {
                case "getCurrentFile": return executeGetCurrentFile();
                case "getProjectStructure": return executeGetProjectStructure();
                case "readFile": return executeReadFile(args);
                case "listFiles": return executeListFiles(args);
                case "searchInProject": return executeSearch(args);
                case "editFile": return executeEditFile(args);
                case "createFile": return executeCreateFile(args);
                default: return "Unknown or unavailable tool: " + name;
            }
        } catch (JSONException e) {
            return "Invalid arguments for " + name + ": " + e.getMessage();
        } catch (Exception e) {
            return "Error executing " + name + ": "
                    + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    private boolean confirmChange(String name, JSONObject args) {
        if (!(context instanceof Activity)) return false;
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return false;

        CountDownLatch answer = new CountDownLatch(1);
        AtomicBoolean approved = new AtomicBoolean(false);
        StringBuilder summary = new StringBuilder(args.optString("path", ""));
        if ("editFile".equals(name)) {
            summary.append("\n\nFind:\n").append(shorten(args.optString("find", ""), 450));
            summary.append("\n\nReplace with:\n")
                    .append(shorten(args.optString("replace", ""), 450));
        } else {
            summary.append("\n\n").append(shorten(args.optString("content", ""), 700));
        }
        mainHandler.post(() -> com.ccs.javadroid.ui.Dialogs.rounded(activity)
                .setTitle("AI wants to " + name)
                .setMessage(summary.toString())
                .setPositiveButton("Allow", (dialog, which) -> {
                    approved.set(true);
                    answer.countDown();
                })
                .setNegativeButton("Deny", (dialog, which) -> answer.countDown())
                .setOnCancelListener(dialog -> answer.countDown())
                .show());
        try {
            return answer.await(CONFIRMATION_TIMEOUT_SECONDS, TimeUnit.SECONDS) && approved.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String executeGetCurrentFile() {
        if (currentCode.isEmpty() && currentFilePath.isEmpty()) {
            return "No file is open in the editor.";
        }
        String path = currentFilePath.isEmpty() ? currentFileName
                : relativePath(new File(currentFilePath));
        return "Path: " + path + "\n" + currentCode;
    }

    private String executeReadFile(JSONObject args) throws Exception {
        File file = resolveProjectFile(args.getString("path"), true);
        if (!file.isFile()) return "Not a file: " + relativePath(file);
        String content = latestContents(file);
        if (content.length() > MAX_FILE_CHARS) {
            return "File is too large to return safely (" + content.length()
                    + " characters). Use searchInProject to narrow the location.";
        }
        return "Path: " + relativePath(file) + "\n" + content;
    }

    private String executeListFiles(JSONObject args) throws Exception {
        File directory = resolveProjectFile(args.optString("path", "."), true);
        if (!directory.isDirectory()) return "Not a directory: " + relativePath(directory);
        File root = projectRoot();
        File[] children = directory.listFiles();
        if (children == null || children.length == 0) return "Empty directory.";
        Arrays.sort(children, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        StringBuilder out = new StringBuilder();
        int count = 0;
        for (File child : children) {
            if (isSkipped(child) || !isInsideProject(root, child)) continue;
            out.append(child.isDirectory() ? "[DIR] " : "[FILE] ")
                    .append(relativePath(child)).append('\n');
            if (++count >= 400) {
                out.append("... list truncated");
                break;
            }
        }
        return out.length() == 0 ? "Empty directory." : out.toString();
    }

    private String executeGetProjectStructure() {
        try {
            File root = projectRoot();
            StringBuilder out = new StringBuilder();
            int[] count = {0};
            appendTree(root, root, out, 0, count);
            if (count[0] >= MAX_TREE_ENTRIES) out.append("... tree truncated\n");
            return out.toString();
        } catch (Exception e) {
            return "Cannot read project structure: " + e.getMessage();
        }
    }

    private void appendTree(File root, File directory, StringBuilder out,
                            int depth, int[] count) {
        if (depth > 6 || count[0] >= MAX_TREE_ENTRIES) return;
        File[] children = directory.listFiles();
        if (children == null) return;
        Arrays.sort(children, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (File child : children) {
            if (count[0] >= MAX_TREE_ENTRIES) return;
            if (isSkipped(child) || !isInsideProject(root, child)) continue;
            out.append("  ".repeat(depth)).append(child.isDirectory() ? "[D] " : "[F] ")
                    .append(child.getName()).append('\n');
            count[0]++;
            if (child.isDirectory()) appendTree(root, child, out, depth + 1, count);
        }
    }

    private String executeSearch(JSONObject args) throws Exception {
        String query = args.getString("query");
        if (query.isEmpty()) return "searchInProject requires a non-empty query.";
        int limit = Math.max(1, Math.min(100, args.optInt("maxResults", 30)));
        File start = resolveProjectFile(args.optString("path", "."), true);
        File root = projectRoot();
        ArrayDeque<File> pending = new ArrayDeque<>();
        pending.add(start);
        StringBuilder matches = new StringBuilder();
        int results = 0;
        int scanned = 0;

        while (!pending.isEmpty() && results < limit && scanned < MAX_SEARCH_FILES) {
            File item = pending.removeFirst();
            if (!isInsideProject(root, item)) continue;
            if (item.isDirectory()) {
                File[] children = item.listFiles();
                if (children == null) continue;
                List<File> ordered = new ArrayList<>(Arrays.asList(children));
                ordered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                for (File child : ordered) {
                    if (!isSkipped(child) && isInsideProject(root, child)) pending.addLast(child);
                }
                continue;
            }
            scanned++;
            if (!isSearchable(item) || item.length() > MAX_FILE_CHARS * 4L) continue;
            String content;
            try {
                content = latestContents(item);
            } catch (Exception ignored) {
                continue;
            }
            int from = 0;
            int line = 1;
            int countedUntil = 0;
            while (results < limit) {
                int at = content.indexOf(query, from);
                if (at < 0) break;
                for (int i = countedUntil; i < at; i++) if (content.charAt(i) == '\n') line++;
                countedUntil = at;
                int lineStart = content.lastIndexOf('\n', Math.max(0, at - 1)) + 1;
                int lineEnd = content.indexOf('\n', at);
                if (lineEnd < 0) lineEnd = content.length();
                String preview = content.substring(lineStart, lineEnd).trim();
                matches.append(relativePath(item)).append(':').append(line).append(": ")
                        .append(shorten(preview, 220)).append('\n');
                results++;
                from = at + Math.max(1, query.length());
            }
        }
        if (results == 0) return "No matches for: " + query;
        if (results >= limit) matches.append("... result limit reached\n");
        if (scanned >= MAX_SEARCH_FILES) matches.append("... file scan limit reached\n");
        return matches.toString();
    }

    private String executeEditFile(JSONObject args) throws Exception {
        File file = resolveProjectFile(args.getString("path"), true);
        if (!file.isFile()) return "editFile target is not a file: " + relativePath(file);
        String find = ExactTextEdit.unwrapFence(args.getString("find"));
        String replacement = ExactTextEdit.unwrapFence(args.getString("replace"));
        ExactTextEdit.Result result = ExactTextEdit.apply(latestContents(file), find, replacement);
        switch (result.status) {
            case EMPTY_FIND:
                return "editFile refused: find must not be empty.";
            case NOT_FOUND:
                return "editFile refused: the exact find text is not in the latest version of "
                        + relativePath(file) + ". Read the file again and copy it exactly.";
            case AMBIGUOUS:
                return "editFile refused: find occurs more than once in " + relativePath(file)
                        + ". Include more surrounding lines so it is unique.";
            case APPLIED:
            default:
                String canonical = file.getCanonicalPath();
                workingFiles.put(canonical, result.text);
                if (canonical.equals(currentFilePath)) currentCode = result.text;
                PendingEdits.addPatch(canonical, find, replacement);
                return "Queued exact edit in " + relativePath(file) + " at line "
                        + lineAt(result.text, result.offset) + ". The rest of the file is unchanged.";
        }
    }

    private String executeCreateFile(JSONObject args) throws Exception {
        File file = resolveProjectFile(args.getString("path"), false);
        if (file.exists()) return "createFile refused: file already exists: " + relativePath(file);
        File parent = file.getParentFile();
        if (parent == null || (!parent.isDirectory() && !parent.mkdirs())) {
            return "createFile failed: parent directory is unavailable.";
        }
        if (!file.createNewFile()) return "createFile failed: could not create the file.";
        String content = ExactTextEdit.unwrapFence(args.optString("content", ""));
        projectManager().writeFile(file, content);
        workingFiles.put(file.getCanonicalPath(), content);
        return "Created " + relativePath(file) + ".";
    }

    private String latestContents(File file) throws IOException {
        String key = file.getCanonicalPath();
        String staged = workingFiles.get(key);
        if (staged != null) return staged;
        String content = projectManager().readFile(file);
        workingFiles.put(key, content);
        return content;
    }

    private ProjectManager projectManager() throws IOException {
        ProjectManager manager = new ProjectManager(context);
        manager.setProjectRoot(projectRoot());
        return manager;
    }

    private File projectRoot() throws IOException {
        String configured = configuredProjectRoot.isEmpty()
                ? new AppPreferences(context).getProjectRoot() : configuredProjectRoot;
        if (configured == null || configured.trim().isEmpty()) {
            throw new IOException("no project is open");
        }
        File root = new File(configured).getCanonicalFile();
        if (!root.isDirectory()) throw new IOException("project root is unavailable");
        return root;
    }

    /** Resolves an AI-provided path strictly inside the active project. */
    private File resolveProjectFile(String rawPath, boolean requireExisting) throws IOException {
        if (rawPath == null || rawPath.trim().isEmpty()) throw new IOException("empty path");
        File root = projectRoot();
        File candidate = new File(rawPath.trim());
        if (!candidate.isAbsolute()) candidate = new File(root, rawPath.trim());
        candidate = candidate.getCanonicalFile();
        String prefix = root.getPath() + File.separator;
        if (!candidate.equals(root) && !candidate.getPath().startsWith(prefix)) {
            throw new IOException("path is outside the active project");
        }
        if (requireExisting && !candidate.exists()) throw new IOException("file does not exist");
        return candidate;
    }

    private String canonicalCurrentPath(String path) {
        if (path == null || path.trim().isEmpty()) return "";
        try {
            return resolveProjectFile(path, true).getCanonicalPath();
        } catch (IOException ignored) {
            return "";
        }
    }

    private String canonicalProjectRoot() {
        try {
            return projectRoot().getCanonicalPath();
        } catch (IOException ignored) {
            return "";
        }
    }

    private String relativePath(File file) {
        try {
            File root = projectRoot();
            String rootPath = root.getCanonicalPath();
            String path = file.getCanonicalPath();
            if (path.equals(rootPath)) return ".";
            String prefix = rootPath + File.separator;
            return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
        } catch (IOException e) {
            return file.getPath();
        }
    }

    private static boolean isSkipped(File file) {
        return file.isDirectory() && (file.getName().startsWith(".")
                || SKIPPED_DIRECTORIES.contains(file.getName()));
    }

    /** Symlinks in a project must not turn read/search/tree into an escape hatch. */
    private static boolean isInsideProject(File root, File candidate) {
        try {
            String rootPath = root.getCanonicalPath();
            String candidatePath = candidate.getCanonicalPath();
            return candidatePath.equals(rootPath)
                    || candidatePath.startsWith(rootPath + File.separator);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isSearchable(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? name : name.substring(dot + 1);
        return SEARCHABLE_EXTENSIONS.contains(extension)
                || "gradlew".equals(name) || "pom.xml".equals(name);
    }

    private static int lineAt(String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') line++;
        }
        return line;
    }

    private static String shorten(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max) + "\n…";
    }

    private static JSONObject message(String role, JSONObject part) throws JSONException {
        return new JSONObject().put("role", role).put("parts", new JSONArray().put(part));
    }

    private boolean isCurrent(long token) {
        return !cancelled && token == turnId;
    }

    private void postToolCall(String name, String args) {
        mainHandler.post(() -> callback.onToolCall(name, args));
    }

    private void postToolResult(String name, String result) {
        mainHandler.post(() -> callback.onToolResult(name, result));
    }

    private void finishText(long token, String text) {
        if (!isCurrent(token)) return;
        mainHandler.post(() -> {
            if (!isCurrent(token)) return;
            callback.onTextResponse(text);
            callback.onDone();
        });
    }

    private void finishError(long token, String error) {
        if (!isCurrent(token)) return;
        mainHandler.post(() -> {
            if (!isCurrent(token)) return;
            callback.onError(error);
            callback.onDone();
        });
    }

    public synchronized void cancel() {
        cancelled = true;
        turnId++;
        agentExecutor.shutdownNow();
    }
}
