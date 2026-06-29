package com.ccs.javadroid.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Agent that can execute tools in the IDE.
 * Extends GeminiService with function calling support.
 */
public class GeminiAgent {

    public interface AgentCallback {
        void onToolCall(String toolName, String args);
        void onToolResult(String toolName, String result);
        void onTextResponse(String text);
        void onError(String error);
        void onDone();
    }

    private final Context context;
    private final AgentCallback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean cancelled = false;
    private int iterationCount = 0;
    private static final int MAX_ITERATIONS = 10;
    private static final long REQUEST_TIMEOUT_MS = 30000; // 30 seconds per request

    // Tool definitions for Gemini function calling
    private static final String TOOL_DEFINITIONS = """
            You are an AI coding assistant with access to tools. You can:
            1. readFile(path) - Read file contents
            2. writeFile(path, content) - Write content to file
            3. createFile(path, content) - Create new file
            4. listFiles(directory) - List files in directory
            5. searchInProject(query) - Search for text in project
            6. runCode(code) - Compile and run Java code
            7. openFile(path) - Open file in editor
            8. getProjectStructure() - Get project file tree
            9. getCurrentFile() - Get currently open file
            10. getCompilationErrors() - Get current compilation errors
            11. insertCode(code, location) - Insert generated code into the EDITOR that the user currently has open. This is the PREFERRED way to deliver code to the user. Do NOT show the code as text when you can insert it directly.
                - code (required): the code text to insert. Provide ONLY the raw code, no markdown fences.
                - location (optional): where to place it. One of:
                    * "cursor"  (default) — insert at the user's cursor position in the open file
                    * "append"  — add at the end of the open file
                    * "replace" — replace the ENTIRE contents of the open file (use for full refactor/rewrite; the old code will be lost)

            When you want to use a tool, respond with a JSON object in this format:
            {"tool": "toolName", "args": {"param1": "value1", "param2": "value2"}}

            When done with all tool calls, provide your final text response.
            """;

    public GeminiAgent(Context context, AgentCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
    }

    /**
     * Send a message to the agent. The agent will use tools as needed.
     */
    public void send(String userMessage, String codeContext, String fileName,
                     List<GeminiService.ChatMessage> history) {
        cancelled = false;
        iterationCount = 0;

        // Build system prompt with tools
        String systemPrompt = TOOL_DEFINITIONS + "\n\n" +
                "Current file: " + (fileName != null ? fileName : "none") + "\n" +
                "Current code:\n```\n" + (codeContext != null ? codeContext : "") + "\n```\n";

        // Send to Gemini
        GeminiService.chat(context, systemPrompt, userMessage, history,
                new GeminiService.ResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        if (cancelled) return;
                        processResponse(response);
                    }

                    @Override
                    public void onError(String error) {
                        if (cancelled) return;
                        mainHandler.post(() -> callback.onError(error));
                    }
                });
    }

    private void processResponse(String response) {
        if (cancelled) return;

        iterationCount++;
        if (iterationCount > MAX_ITERATIONS) {
            mainHandler.post(() -> callback.onTextResponse("Reached maximum iterations. Stopping agent."));
            mainHandler.post(() -> callback.onDone());
            return;
        }

        // Check if response contains a tool call
        String trimmed = response.trim();
        if (trimmed.startsWith("{") && trimmed.contains("\"tool\"")) {
            try {
                JSONObject toolCall = new JSONObject(trimmed);
                String toolName = toolCall.getString("tool");
                JSONObject args = toolCall.optJSONObject("args");

                mainHandler.post(() -> callback.onToolCall(toolName, args != null ? args.toString() : ""));

                // Execute the tool
                String result = executeTool(toolName, args);

                mainHandler.post(() -> callback.onToolResult(toolName, result));

                // Send result back to AI for next step
                if (!cancelled) {
                    continueConversation("Tool result for " + toolName + ": " + result);
                }

            } catch (JSONException e) {
                // Not a tool call, treat as text
                mainHandler.post(() -> callback.onTextResponse(response));
                mainHandler.post(() -> callback.onDone());
            }
        } else {
            // Text response — conversation finished
            if (response == null || response.isEmpty()) {
                mainHandler.post(() -> callback.onTextResponse("AI returned empty response. Try rephrasing."));
            } else {
                mainHandler.post(() -> callback.onTextResponse(response));
            }
            mainHandler.post(() -> callback.onDone());
        }
    }

    private void continueConversation(String toolResult) {
        if (cancelled) return;

        // Show progress
        mainHandler.post(() -> callback.onTextResponse("🔄 Processing step " + (iterationCount + 1) + "..."));

        GeminiService.chat(context, "", toolResult, null,
                new GeminiService.ResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        if (cancelled) return;
                        processResponse(response);
                    }

                    @Override
                    public void onError(String error) {
                        if (cancelled) return;
                        mainHandler.post(() -> callback.onError("Step " + (iterationCount) + " error: " + error));
                        mainHandler.post(() -> callback.onDone());
                    }
                });
    }

    private String executeTool(String toolName, JSONObject args) {
        // Check if inclusive mode is enabled (no confirmations)
        com.ccs.javadroid.util.AppPreferences prefs = new com.ccs.javadroid.util.AppPreferences(context);
        if (prefs.isInclusiveMode()) {
            try {
                switch (toolName) {
                    case "readFile":
                        return executeReadFile(args);
                    case "writeFile":
                        return executeWriteFile(args);
                    case "createFile":
                        return executeCreateFile(args);
                    case "listFiles":
                        return executeListFiles(args);
                    case "searchInProject":
                        return executeSearch(args);
                    case "runCode":
                        return executeRunCode(args);
                    case "openFile":
                        return executeOpenFile(args);
                    case "getProjectStructure":
                        return executeGetProjectStructure();
                    case "getCurrentFile":
                        return executeGetCurrentFile();
                    case "getCompilationErrors":
                        return executeGetCompilationErrors();
                    case "insertCode":
                        return executeInsertCode(args);
                    default:
                        return "Unknown tool: " + toolName;
                }
            } catch (Exception e) {
                return "Error executing " + toolName + ": " + e.getMessage();
            }
        }

        // Normal mode — dangerous tools require confirmation
        boolean needsConfirmation = toolName.equals("writeFile") || toolName.equals("createFile");

        if (needsConfirmation) {
            // Show confirmation dialog on main thread and wait
            final boolean[] approved = {false};
            final Object lock = new Object();

            mainHandler.post(() -> {
                String summary = toolName + ": " + args.optString("path", "");
                if (toolName.equals("writeFile")) {
                    String content = args.optString("content", "");
                    summary += "\n\n" + content.substring(0, Math.min(500, content.length()));
                    if (content.length() > 500) summary += "\n...";
                }

                new android.app.AlertDialog.Builder(context)
                        .setTitle("AI wants to " + toolName)
                        .setMessage(summary)
                        .setPositiveButton("Allow", (d, w) -> {
                            approved[0] = true;
                            synchronized (lock) { lock.notifyAll(); }
                        })
                        .setNegativeButton("Deny", (d, w) -> {
                            approved[0] = false;
                            synchronized (lock) { lock.notifyAll(); }
                        })
                        .setCancelable(false)
                        .show();
            });

            // Wait for user response
            synchronized (lock) {
                try { lock.wait(60000); } catch (InterruptedException ignored) {}
            }

            if (!approved[0]) {
                return "Denied by user";
            }
        }

        try {
            switch (toolName) {
                case "readFile":
                    return executeReadFile(args);
                case "writeFile":
                    return executeWriteFile(args);
                case "createFile":
                    return executeCreateFile(args);
                case "listFiles":
                    return executeListFiles(args);
                case "searchInProject":
                    return executeSearch(args);
                case "runCode":
                    return executeRunCode(args);
                case "openFile":
                    return executeOpenFile(args);
                case "getProjectStructure":
                    return executeGetProjectStructure();
                case "getCurrentFile":
                    return executeGetCurrentFile();
                case "getCompilationErrors":
                    return executeGetCompilationErrors();
                case "insertCode":
                    return executeInsertCode(args);
                default:
                    return "Unknown tool: " + toolName;
            }
        } catch (Exception e) {
            return "Error executing " + toolName + ": " + e.getMessage();
        }
    }

    private String executeReadFile(JSONObject args) throws JSONException {
        String path = args.getString("path");
        java.io.File file = new java.io.File(path);
        if (!file.exists()) return "File not found: " + path;
        if (!file.canRead()) return "Cannot read file: " + path;

        try {
            StringBuilder sb = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(new java.io.FileInputStream(file)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private String executeWriteFile(JSONObject args) throws JSONException {
        String path = args.getString("path");
        String content = args.getString("content");
        try {
            java.io.File file = new java.io.File(path);
            file.getParentFile().mkdirs();
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(content);
            }
            return "File written: " + path;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }

    private String executeCreateFile(JSONObject args) throws JSONException {
        String path = args.getString("path");
        String content = args.optString("content", "");
        try {
            java.io.File file = new java.io.File(path);
            file.getParentFile().mkdirs();
            file.createNewFile();
            if (!content.isEmpty()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write(content);
                }
            }
            return "File created: " + path;
        } catch (Exception e) {
            return "Error creating file: " + e.getMessage();
        }
    }

    private String executeListFiles(JSONObject args) throws JSONException {
        String path = args.optString("path", ".");
        java.io.File dir = new java.io.File(path);
        if (!dir.isDirectory()) return "Not a directory: " + path;

        StringBuilder sb = new StringBuilder();
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File f : files) {
                sb.append(f.isDirectory() ? "[DIR] " : "[FILE] ");
                sb.append(f.getName()).append("\n");
            }
        }
        return sb.length() > 0 ? sb.toString() : "Empty directory";
    }

    private String executeSearch(JSONObject args) throws JSONException {
        String query = args.getString("query");
        // Simple grep-like search
        StringBuilder sb = new StringBuilder();
        // TODO: implement project-wide search
        return "Search for: " + query + "\n(Search functionality coming soon)";
    }

    private String executeRunCode(JSONObject args) throws JSONException {
        String code = args.getString("code");
        // TODO: integrate with ProjectCompiler
        return "Code execution not yet integrated with agent";
    }

    private String executeOpenFile(JSONObject args) throws JSONException {
        String path = args.getString("path");
        java.io.File file = new java.io.File(path);
        if (!file.exists()) return "File not found: " + path;
        return "File opened: " + path + " (would open in editor)";
    }

    private String executeGetProjectStructure() {
        // Get project root from preferences
        com.ccs.javadroid.util.AppPreferences prefs = new com.ccs.javadroid.util.AppPreferences(context);
        String projectRoot = prefs.getProjectRoot();
        if (projectRoot == null) return "No project open";

        StringBuilder sb = new StringBuilder();
        buildTree(new java.io.File(projectRoot), sb, 0, 3);
        return sb.toString();
    }

    private void buildTree(java.io.File dir, StringBuilder sb, int depth, int maxDepth) {
        if (depth > maxDepth) return;
        java.io.File[] files = dir.listFiles();
        if (files == null) return;

        java.util.Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (java.io.File f : files) {
            if (f.getName().startsWith(".")) continue; // skip hidden files
            sb.append("  ".repeat(depth)).append(f.isDirectory() ? "📁 " : "📄 ");
            sb.append(f.getName()).append("\n");
            if (f.isDirectory()) {
                buildTree(f, sb, depth + 1, maxDepth);
            }
        }
    }

    private String executeGetCurrentFile() {
        // This would need to be passed from the activity
        return "Current file info not available in agent context";
    }

    private String executeGetCompilationErrors() {
        // This would need to be passed from the activity
        return "Compilation errors not available in agent context";
    }

    /**
     * Інструмент insertCode: планує вставку коду у відкритий редактор через
     * PendingEdits. Сама вставка відбудеться в MainActivity.onResume, коли
     * користувач закриє/покине чат. Повертає підтвердження агенту.
     *
     * location: "cursor" (за замовч.) | "append" | "replace"
     */
    private String executeInsertCode(JSONObject args) throws JSONException {
        String code = args.getString("code");
        String location = args.optString("location", PendingEdits.LOCATION_CURSOR);

        // Нормалізуємо location у відомий набір
        String normalized;
        if (location == null || location.isEmpty()
                || location.equalsIgnoreCase("cursor") || location.equalsIgnoreCase("insert")) {
            normalized = PendingEdits.LOCATION_CURSOR;
        } else if (location.equalsIgnoreCase("append") || location.equalsIgnoreCase("end")) {
            normalized = PendingEdits.LOCATION_APPEND;
        } else if (location.equalsIgnoreCase("replace")
                || location.equalsIgnoreCase("overwrite") || location.equalsIgnoreCase("full")) {
            normalized = PendingEdits.LOCATION_REPLACE;
        } else {
            normalized = PendingEdits.LOCATION_CURSOR;
        }

        // Прибрати markdown-огорожки, якщо модель випадково їх додала
        String cleaned = stripCodeFences(code);
        if (cleaned.isEmpty()) {
            return "insertCode: empty code, nothing inserted";
        }

        PendingEdits.add(cleaned, normalized);

        int lines = countLines(cleaned);
        return "Code queued for the editor (" + normalized + ", " + lines + " lines, "
                + cleaned.length() + " chars). It will be inserted when the user returns to the editor.";
    }

    private static String stripCodeFences(String code) {
        if (code == null) return "";
        String s = code.trim();
        // Зняти початковий ```lang
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl >= 0) s = s.substring(nl + 1);
            else s = s.substring(3);
        }
        // Зняти завершальний ```
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }

    private static int countLines(String s) {
        if (s == null || s.isEmpty()) return 0;
        int count = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') count++;
        }
        return count;
    }

    public void cancel() {
        cancelled = true;
    }
}
