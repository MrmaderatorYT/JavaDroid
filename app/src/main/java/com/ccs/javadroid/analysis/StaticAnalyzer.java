package com.ccs.javadroid.analysis;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ccs.javadroid.R;

/**
 * Повноцінний статичний аналізатор Java-коду (Checkstyle/Lint-рівень).
 * Понад 70 правил: стиль, безпека, продуктивність, архітектура, ресурси, потоки, тести, документація.
 */
public final class StaticAnalyzer {

    // ── Thresholds ────────────────────────────────────────────
    private static final int MAX_LINE_LENGTH        = 160;
    private static final int MAX_METHOD_LENGTH       = 50;
    private static final int MAX_CLASS_LENGTH        = 500;
    private static final int MAX_PARAMETERS          = 5;
    private static final int MAX_CYCLOMATIC          = 10;
    private static final int MAX_NESTING_DEPTH       = 4;
    private static final int MAX_FIELDS             = 15;
    private static final int MAX_STRING_LITERAL_LEN  = 120;
    private static final int MAX_ENUM_CONSTANTS      = 20;
    private static final int MAX_LINE_WITHOUT_COMMENT = 80;

    // ── Patterns ──────────────────────────────────────────────
    private static final Pattern P_METHOD_DECL = Pattern.compile(
            "^\\s*(public|private|protected)?\\s*(static)?\\s*(final)?\\s*(synchronized)?\\s*"
            + "(?:abstract)?\\s*(?:native)?\\s*[\\w<>,\\s?]+\\s+(\\w+)\\s*\\(");
    private static final Pattern P_IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.*]+)\\s*;");
    private static final Pattern P_CATCH_EMPTY = Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}");
    private static final Pattern P_STRING_EQ = Pattern.compile(
            "(?:==|!=)\\s*\"[^\"]*\"|\"[^\"]*\"\\s*(?:==|!=)");
    private static final Pattern P_MAGIC_NUMBER = Pattern.compile(
            "(?<!=|!|<|>|\\+|-|\\*|/|%|\\||&|\\^|~|<<|>>)\\s*(\\d{2,})\\b");
    private static final Pattern P_SYSOUT = Pattern.compile("System\\.out\\.print(ln)?\\s*\\(");
    private static final Pattern P_SYSE = Pattern.compile("System\\.err\\.print(ln)?\\s*\\(");
    private static final Pattern P_NEW_HASHMAP = Pattern.compile("new\\s+HashMap\\s*\\(\\s*\\)");
    private static final Pattern P_NEW_ARRAYLIST = Pattern.compile("new\\s+ArrayList\\s*\\(\\s*\\)");
    private static final Pattern P_THROWABLE_CATCH = Pattern.compile(
            "catch\\s*\\(\\s*(?:Throwable|Exception)\\s+\\w+\\s*\\)");
    private static final Pattern P_NULL_COMPARE = Pattern.compile("==\\s*null|!=\\s*null");
    private static final Pattern P_FINAL_FIELD = Pattern.compile("^\\s*(?:public|private|protected)?\\s*final\\s+");

    // Additional patterns
    private static final Pattern P_CLASS_DECL = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:abstract|final)?\\s*(class|interface|enum)\\s+(\\w+)");
    private static final Pattern P_FIELD_DECL = Pattern.compile(
            "^\\s*(?:private|protected|public)\\s+(?:static\\s+)?(?:final\\s+)?[\\w<>,\\[\\]]+\\s+(\\w+)\\s*[=;]");
    private static final Pattern P_LOCAL_VAR = Pattern.compile(
            "^\\s*(?:final\\s+)?(?:[A-Z][\\w<>\\[\\],]*|int|boolean|byte|char|short|long|float|double|var)\\s+(\\w+)\\s*[=;]");
    private static final Pattern P_RETURN_STMT = Pattern.compile("\\breturn\\b");
    private static final Pattern P_THIS_OR_SUPER = Pattern.compile("\\bthis\\.|super\\.");
    private static final Pattern P_GENERIC_RETURN = Pattern.compile(
            "<[\\w<>,\\s?]+>\\s+\\w+\\s*\\(");

    private StaticAnalyzer() {}

    /** Аналіз без контексту (fallback на hardcoded рядки). */
    public static List<ProblemItem> analyze(File projectRoot, List<File> javaFiles) {
        return analyze(null, projectRoot, javaFiles);
    }

    /** Аналіз з контекстом (використовує перекладені рядки). */
    public static List<ProblemItem> analyze(Context ctx, File projectRoot, List<File> javaFiles) {
        List<ProblemItem> out = new ArrayList<>();
        if (javaFiles == null) return out;

        for (File f : javaFiles) {
            if (!f.getName().endsWith(".java")) continue;
            try {
                String content = new String(java.nio.file.Files.readAllBytes(f.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                if (content.trim().isEmpty()) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_empty_file), f, 1));
                    continue;
                }
                String[] lines = content.split("\n", -1);

                analyzeLineRules(ctx, lines, f, out);
                analyzeImports(ctx, content, lines, f, out);
                analyzeMethods(ctx, content, lines, f, out);
                analyzeClass(ctx, lines, f, out);
                analyzeEmptyCatch(ctx, lines, f, out);
                analyzeStringComparison(ctx, lines, f, out);
                analyzeMagicNumbers(ctx, lines, f, out);
                analyzeSysout(ctx, lines, f, out);
                analyzeProductivity(ctx, lines, f, out);
                analyzeCodeStyle(ctx, lines, f, out);
                analyzeSafety(ctx, lines, f, out);
                analyzeArchitecture(ctx, lines, f, out);
                analyzeNaming(ctx, lines, f, out);
                analyzeResourceManagement(ctx, lines, f, out);
                analyzeNullSafety(ctx, lines, f, out);
                analyzeThreadSafety(ctx, lines, f, out);
                analyzeModernization(ctx, lines, f, out);
                analyzeTesting(ctx, lines, f, out);
                analyzeDocumentation(ctx, lines, f, out);
                analyzeDeprecated(ctx, lines, f, out);
                analyzeCollections(ctx, lines, f, out);
                analyzeExceptionHandling(ctx, lines, f, out);
                analyzeImmutability(ctx, lines, f, out);
                analyzeGenerics(ctx, lines, f, out);
                analyzeStreams(ctx, lines, f, out);
                analyzeAnnotations(ctx, lines, f, out);
                analyzeEnums(ctx, lines, f, out);
                analyzeApiDesign(ctx, lines, f, out);
                analyzeCopyPaste(ctx, lines, f, out);
                analyzeFieldRules(ctx, lines, f, out);
                analyzeSwitch(ctx, lines, f, out);
                analyzeLambda(ctx, lines, f, out);
                analyzeOptional(ctx, lines, f, out);
                analyzeDate(ctx, lines, f, out);
                analyzeRegex(ctx, lines, f, out);
                analyzeInline(ctx, lines, f, out);
                analyzeFinalLocal(ctx, lines, f, out);

            } catch (Exception ignored) {}
        }
        return out;
    }

    /** Допоміжний метод: повертає перекладений рядок або fallback. */
    private static String str(Context ctx, int resId) {
        if (ctx != null) return ctx.getString(resId);
        return "";
    }

    private static String str(Context ctx, int resId, Object... args) {
        if (ctx != null) return ctx.getString(resId, args);
        return "";
    }

    // ══════════════════════════════════════════════════════════
    //  1. Line-level rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeLineRules(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            // Long line
            if (line.length() > MAX_LINE_LENGTH) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Рядок довший за " + MAX_LINE_LENGTH + " символів (" + line.length() + ")",
                        f, i + 1));
            }

            // TODO / FIXME / HACK / XXX
            if (line.contains("TODO")) out.add(new ProblemItem(ProblemItem.Severity.INFO,
                    str(ctx, R.string.sa_todo), f, i + 1));
            if (line.contains("FIXME")) out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                    str(ctx, R.string.sa_fixme), f, i + 1));
            if (line.contains("HACK")) out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                    str(ctx, R.string.sa_hack), f, i + 1));
            if (line.contains("XXX")) out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                    str(ctx, R.string.sa_xxx), f, i + 1));
            if (line.contains("NOSONAR")) out.add(new ProblemItem(ProblemItem.Severity.INFO,
                    "NOSONAR — приховує попередження SonarQube", f, i + 1));
            if (line.contains("noinspection")) out.add(new ProblemItem(ProblemItem.Severity.INFO,
                    "noinspection — приховує попередження аналізатора", f, i + 1));

            // Trailing whitespace
            if (!line.isEmpty() && line.charAt(line.length() - 1) == ' ') {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_trailing_space), f, i + 1));
            }

            // Tab + space mix
            if (line.contains("\t") && line.contains("    ")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_mixed_tabs), f, i + 1));
            }

            // Commented-out code (heuristic)
            if (trimmed.startsWith("//") && !trimmed.startsWith("///")
                    && (trimmed.contains("(") || trimmed.contains("=") || trimmed.contains(";"))) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_commented_code), f, i + 1));
            }

            // Deprecated API usage
            if (line.contains("@Deprecated")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_deprecated_api), f, i + 1));
            }

            // System.exit in non-main
            if (line.contains("System.exit(")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_system_exit), f, i + 1));
            }

            // Thread.sleep without try-catch
            if (line.contains("Thread.sleep(") && !line.contains("try")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_thread_sleep), f, i + 1));
            }

            // Raw type
            if (line.matches(".*\\b(List|Map|Set|ArrayList|HashMap|HashSet|LinkedList)\\s+\\w+.*")
                    && !line.contains("<")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Raw type — використовуйте параметризований тип", f, i + 1));
            }

            // Hardcoded paths
            if (line.contains("C:\\\\") || line.contains("/home/")
                    || line.contains("/Users/") || line.contains("D:\\\\")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_hardcoded_path), f, i + 1));
            }

            // Hardcoded IP addresses
            if (line.matches(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b.*")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_hardcoded_ip), f, i + 1));
            }

            // Password/key in code
            String lower = line.toLowerCase();
            if ((lower.contains("password") || lower.contains("secret") || lower.contains("api_key"))
                    && (lower.contains("=") || lower.contains(":"))) {
                out.add(new ProblemItem(ProblemItem.Severity.ERROR,
                        str(ctx, R.string.sa_password_in_code), f, i + 1));
            }

            // Empty if/else/for/while body
            if ((trimmed.startsWith("if") || trimmed.startsWith("for") || trimmed.startsWith("while") || trimmed.startsWith("else"))
                    && trimmed.endsWith(" {}")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Порожній блок " + trimmed.substring(0, trimmed.indexOf(' ')), f, i + 1));
            }

            // Multiple statements on one line
            int semicolons = 0;
            for (char c : line.toCharArray()) {
                if (c == ';') semicolons++;
            }
            if (semicolons > 2 && !trimmed.startsWith("//") && !trimmed.startsWith("for") && !trimmed.startsWith("import")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_multiple_statements), f, i + 1));
            }

            // Wild import
            if (trimmed.startsWith("import") && trimmed.contains(".*;")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_wildcard_import), f, i + 1));
            }

            // Deprecated @SuppressWarnings
            if (trimmed.contains("@SuppressWarnings(\"all\")")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_suppress_all), f, i + 1));
            }

            // Empty string concatenation
            if (line.contains("\"\" +") || line.contains("+ \"\"")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_empty_string_concat), f, i + 1));
            }

            // Potential resource leak (new InputStream/Reader without try-with-resources)
            if ((line.contains("new FileInputStream(") || line.contains("new FileOutputStream(")
                    || line.contains("new BufferedReader(") || line.contains("new BufferedWriter("))
                    && !line.contains("try (")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_resource_leak), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  2. Import analysis
    // ══════════════════════════════════════════════════════════

    private static void analyzeImports(Context ctx, String content, String[] lines, File f, List<ProblemItem> out) {
        Set<String> imports = new HashSet<>();
        List<Integer> importLineNums = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            Matcher m = P_IMPORT.matcher(lines[i]);
            if (m.find()) {
                imports.add(m.group(2));
                importLineNums.add(i);
            }
        }

        // Unused imports
        for (int idx = 0; idx < importLineNums.size(); idx++) {
            int lineNum = importLineNums.get(idx);
            String imp = new ArrayList<>(imports).get(idx);
            if (imp.endsWith("*")) continue;
            String simpleName = imp.substring(imp.lastIndexOf('.') + 1);

            boolean used = false;
            for (int j = lineNum + 1; j < lines.length; j++) {
                if (lines[j].contains(simpleName)) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_unused_import, imp), f, lineNum + 1));
            }
        }

        // Duplicate imports
        Set<String> seen = new HashSet<>();
        for (String imp : imports) {
            if (!seen.add(imp)) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_duplicate_import, imp), f, 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  3. Method analysis
    // ══════════════════════════════════════════════════════════

    private static void analyzeMethods(Context ctx, String content, String[] lines, File f, List<ProblemItem> out) {
        List<int[]> methods = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            Matcher m = P_METHOD_DECL.matcher(lines[i]);
            if (m.find() && !lines[i].trim().startsWith("//") && !lines[i].trim().startsWith("*")) {
                methods.add(new int[]{i, 0, 0, 0});
            }
        }

        for (int[] method : methods) {
            int startLine = method[0];
            int braceCount = 0;
            boolean foundOpen = false;
            int endLine = startLine;

            for (int i = startLine; i < lines.length; i++) {
                for (char c : lines[i].toCharArray()) {
                    if (c == '{') { braceCount++; foundOpen = true; }
                    else if (c == '}') braceCount--;
                }
                if (foundOpen && braceCount == 0) {
                    endLine = i;
                    break;
                }
            }

            int methodLength = 0;
            for (int i = startLine; i <= endLine && i < lines.length; i++) {
                String tl = lines[i].trim();
                if (!tl.isEmpty() && !tl.startsWith("//") && !tl.startsWith("*") && !tl.startsWith("/*")) {
                    methodLength++;
                }
            }
            int complexity = computeCyclomaticComplexity(lines, startLine, endLine);
            int paramCount = countParameters(lines[startLine]);

            if (methodLength > MAX_METHOD_LENGTH) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Метод занадто довгий (" + methodLength + " рядків)", f, startLine + 1));
            }
            if (complexity > MAX_CYCLOMATIC) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Висока цикломатична складність (" + complexity + ")", f, startLine + 1));
            }
            if (paramCount > MAX_PARAMETERS) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Забагато параметрів (" + paramCount + ")", f, startLine + 1));
            }

            int maxNesting = computeMaxNesting(lines, startLine, endLine);
            if (maxNesting > MAX_NESTING_DEPTH) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Глибока вкладеність (" + maxNesting + " рівнів)", f, startLine + 1));
            }

            // Empty method
            if (methodLength <= 2 && lines[startLine].contains("{")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Порожній метод", f, startLine + 1));
            }

            // Method name starts with uppercase
            String methodName = extractMethodName(lines[startLine]);
            if (methodName != null && !methodName.isEmpty()
                    && Character.isUpperCase(methodName.charAt(0))
                    && !methodName.equals(methodName.toUpperCase())
                    && !lines[startLine].contains("class ")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Назва методу з великої літери: " + methodName, f, startLine + 1));
            }

            // Multiple return statements
            int returnCount = 0;
            for (int i = startLine; i <= endLine && i < lines.length; i++) {
                if (P_RETURN_STMT.matcher(lines[i]).find()) returnCount++;
            }
            if (returnCount > 3) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Багато return-ів (" + returnCount + ") — розгляньте ранні return", f, startLine + 1));
            }

            // Public method without JavaDoc
            if (lines[startLine].contains("public ") && !lines[startLine].contains("class ")
                    && !lines[startLine].contains("interface ")) {
                boolean hasDoc = false;
                if (startLine > 0) {
                    for (int j = startLine - 1; j >= Math.max(0, startLine - 5); j--) {
                        if (lines[j].contains("/**") || lines[j].contains("*/") || lines[j].trim().startsWith("*")) {
                            hasDoc = true;
                            break;
                        }
                    }
                }
                if (!hasDoc) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_missing_javadoc_method), f, startLine + 1));
                }
            }

            // Method has too many local variables
            int localVarCount = 0;
            for (int i = startLine; i <= endLine && i < lines.length; i++) {
                if (P_LOCAL_VAR.matcher(lines[i]).find()) localVarCount++;
            }
            if (localVarCount > 10) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Забагато локальних змінних (" + localVarCount + ") — розгляньте рефакторинг",
                        f, startLine + 1));
            }

            // Method with 0 return statements but non-void return type
            String returnType = extractReturnType(lines[startLine]);
            if (returnType != null && !returnType.equals("void") && returnCount == 0) {
                out.add(new ProblemItem(ProblemItem.Severity.ERROR,
                        "Метод не повертає значення (тип: " + returnType + ")", f, startLine + 1));
            }
        }
    }

    private static int computeCyclomaticComplexity(String[] lines, int start, int end) {
        int cc = 1;
        for (int i = start; i <= end && i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;
            cc += countPattern(l, "\\bif\\b");
            cc += countPattern(l, "\\bcase\\b");
            cc += countPattern(l, "\\bfor\\b");
            cc += countPattern(l, "\\bwhile\\b");
            cc += countPattern(l, "\\bcatch\\b");
            cc += countPattern(l, "&&");
            cc += countPattern(l, "\\|\\|");
            cc += countPattern(l, "\\?");
        }
        return cc;
    }

    private static int computeMaxNesting(String[] lines, int start, int end) {
        int max = 0;
        int current = 0;
        for (int i = start; i <= end && i < lines.length; i++) {
            for (char c : lines[i].toCharArray()) {
                if (c == '{') { current++; max = Math.max(max, current); }
                else if (c == '}') current--;
            }
        }
        return max;
    }

    private static int countParameters(String line) {
        int idx = line.indexOf('(');
        if (idx < 0) return 0;
        int end = line.indexOf(')', idx);
        if (end < 0) return 0;
        String params = line.substring(idx + 1, end).trim();
        if (params.isEmpty()) return 0;
        return params.split(",").length;
    }

    private static String extractMethodName(String line) {
        Matcher m = P_METHOD_DECL.matcher(line);
        return m.find() ? m.group(5) : null;
    }

    private static String extractReturnType(String line) {
        String l = line.trim();
        int parenIdx = l.indexOf('(');
        if (parenIdx < 0) return null;
        String before = l.substring(0, parenIdx).trim();
        before = before.replaceAll("\\b(public|private|protected|static|final|synchronized|abstract|native)\\s+", "").trim();
        int lastSpace = before.lastIndexOf(' ');
        if (lastSpace < 0) return null;
        return before.substring(0, lastSpace).trim();
    }

    // ══════════════════════════════════════════════════════════
    //  4. Class-level rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeClass(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        int classStart = -1;
        int fieldCount = 0;
        int classBraceCount = 0;
        boolean inClass = false;
        String className = "";

        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();

            if (!inClass && P_CLASS_DECL.matcher(l).find()) {
                classStart = i;
                inClass = true;
                Matcher m = P_CLASS_DECL.matcher(l);
                if (m.find()) className = m.group(2);
            }

            if (inClass) {
                for (char c : lines[i].toCharArray()) {
                    if (c == '{') classBraceCount++;
                    else if (c == '}') classBraceCount--;
                }

                if ((l.startsWith("private ") || l.startsWith("protected ") || l.startsWith("public "))
                        && (l.contains(";") && !l.contains("("))) {
                    fieldCount++;
                }

                if (classBraceCount == 0 && classStart >= 0) {
                    int classLength = i - classStart + 1;

                    if (classLength > MAX_CLASS_LENGTH) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                "Клас занадто великий (" + classLength + " рядків)", f, classStart + 1));
                    }
                    if (fieldCount > MAX_FIELDS) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                "Забагато полів класу (" + fieldCount + ")", f, classStart + 1));
                    }
                    if (classLength > 300 && fieldCount > 10) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                "God class — занадто багато відповідальності", f, classStart + 1));
                    }

                    // Missing serialVersionUID for Serializable
                    if (lines[classStart].contains("implements") && lines[classStart].contains("Serializable")) {
                        boolean hasSerial = false;
                        for (int j = classStart; j <= i; j++) {
                            if (lines[j].contains("serialVersionUID")) {
                                hasSerial = true;
                                break;
                            }
                        }
                        if (!hasSerial) {
                            out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                    str(ctx, R.string.sa_serial_version_uid), f, classStart + 1));
                        }
                    }

                    // Missing @Override annotations
                    for (int j = classStart; j <= i; j++) {
                        String mLine = lines[j].trim();
                        if ((mLine.contains("public boolean equals(") || mLine.contains("public int hashCode(")
                                || mLine.contains("public String toString("))
                                && (j == 0 || !lines[j - 1].contains("@Override"))) {
                            out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                    "Має бути @Override для " + mLine.substring(0, Math.min(40, mLine.length())),
                                    f, j + 1));
                        }
                    }

                    // Unused fields
                    Set<String> fieldNames = new HashSet<>();
                    for (int j = classStart; j <= i; j++) {
                        Matcher fm = P_FIELD_DECL.matcher(lines[j]);
                        if (fm.find()) fieldNames.add(fm.group(1));
                    }
                    for (String fname : fieldNames) {
                        boolean used = false;
                        for (int j = classStart; j <= i; j++) {
                            if (lines[j].contains(fname) && !P_FIELD_DECL.matcher(lines[j]).find()) {
                                used = true;
                                break;
                            }
                        }
                        if (!used) {
                            out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                    str(ctx, R.string.sa_unused_field, fname), f, classStart + 1));
                        }
                    }

                    classStart = -1;
                    fieldCount = 0;
                    inClass = false;
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  5. Empty catch
    // ══════════════════════════════════════════════════════════

    private static void analyzeEmptyCatch(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            if (P_CATCH_EMPTY.matcher(lines[i]).find()) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_empty_catch), f, i + 1));
            }
            // Catch without rethrow or log
            if (lines[i].trim().startsWith("catch") && i + 1 < lines.length) {
                String next = lines[i + 1].trim();
                if (!next.equals("}") && !next.contains("throw") && !next.contains("log")
                        && !next.contains("System.err") && next.length() < 5) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_catch_no_log), f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  6. String == comparison
    // ══════════════════════════════════════════════════════════

    private static void analyzeStringComparison(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;
            if (P_STRING_EQ.matcher(l).find() && !l.contains("length()")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_string_equals), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  7. Magic numbers
    // ══════════════════════════════════════════════════════════

    private static void analyzeMagicNumbers(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*") || l.startsWith("import")) continue;

            Matcher m = P_MAGIC_NUMBER.matcher(l);
            while (m.find()) {
                String num = m.group(1);
                int val = Integer.parseInt(num);
                if (val == 0 || val == 1 || val == -1) continue;
                if (l.contains("static final") || l.contains("@") || l.contains("new int[")
                        || l.contains("case ") || l.contains("0x")) continue;
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Magic number " + num + " — винесіть у константу", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  8. System.out / System.err
    // ══════════════════════════════════════════════════════════

    private static void analyzeSysout(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;
            if (P_SYSOUT.matcher(l).find()) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_system_out), f, i + 1));
            }
            if (P_SYSE.matcher(l).find()) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_system_err), f, i + 1));
            }
            // e.printStackTrace()
            if (l.contains(".printStackTrace()")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_print_stacktrace), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  9. Productivity
    // ══════════════════════════════════════════════════════════

    private static void analyzeProductivity(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Redundant .toString()
            if (l.contains("+") && l.contains(".toString()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_redundant_tostring), f, i + 1));
            }

            // String concatenation in loop
            if ((l.contains("for") || l.contains("while"))) {
                for (int j = i + 1; j < Math.min(i + 20, lines.length); j++) {
                    if (lines[j].contains("+= \"") || lines[j].contains("+ \"")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_concat_in_loop), f, j + 1));
                        break;
                    }
                    if (lines[j].contains("}") || lines[j].trim().startsWith("return")) break;
                }
            }

            // Boxing
            if (l.contains("new Integer(") || l.contains("new Long(")
                    || l.contains("new Boolean(") || l.contains("new Double(")
                    || l.contains("new Float(")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_autoboxing), f, i + 1));
            }

            // Collections.sort()
            if (l.contains("Collections.sort(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_collections_sort), f, i + 1));
            }

            // Redundant null before instanceof
            if (i > 0 && l.contains("instanceof")) {
                String prev = lines[i - 1].trim();
                if (prev.contains("!= null") && !prev.contains("&&") && !prev.contains("||")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_redundant_null_instanceof), f, i));
                }
            }

            // String.indexOf() == -1 (use contains())
            if (l.contains(".indexOf(") && (l.contains("== -1") || l.contains("!= -1"))) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_indexof_contains), f, i + 1));
            }

            // length() == 0 (use isEmpty())
            if (l.contains(".length() == 0") || l.contains(".length() > 0")
                    || l.contains(".length() != 0")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_length_zero), f, i + 1));
            }

            // size() == 0 (use isEmpty())
            if (l.contains(".size() == 0") || l.contains(".size() > 0")
                    || l.contains(".size() != 0")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_size_zero), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  10. Code style
    // ══════════════════════════════════════════════════════════

    private static void analyzeCodeStyle(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*") || l.startsWith("import")) continue;

            // Method without braces
            if ((l.startsWith("if") || l.startsWith("for") || l.startsWith("while") || l.startsWith("else"))
                    && l.endsWith(")") && i + 1 < lines.length
                    && !lines[i + 1].trim().startsWith("{")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_one_line_block), f, i + 1));
            }

            // Long ternary
            if (l.contains("?") && l.contains(":") && l.length() > 100) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_long_ternary), f, i + 1));
            }

            // Unnecessary semicolons
            if (l.equals(";") || l.equals("};")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_unnecessary_semicolon), f, i + 1));
            }

            // Constant naming
            if (l.contains("static final") && l.contains("=")) {
                String varName = l.replaceAll(".*static\\s+final\\s+\\w+\\s+(\\w+)\\s*=.*", "$1");
                if (!varName.equals(l) && !varName.matches("[A-Z_][A-Z0-9_]*")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Константа '" + varName + "' — UPPER_CASE", f, i + 1));
                }
            }

            // Redundant this.
            if (l.startsWith("this.") && !l.contains("this(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Redundant this. в контексті", f, i + 1));
            }

            // == true / == false
            if (l.contains("== true") || l.contains("== false") || l.contains("!= true") || l.contains("!= false")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_bool_true_false), f, i + 1));
            }

            // Unnecessary boxing
            if (l.contains("Integer.valueOf(") && l.contains("int")
                    || l.contains("Long.valueOf(") && l.contains("long")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_redundant_boxing), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  11. Safety / best practices
    // ══════════════════════════════════════════════════════════

    private static void analyzeSafety(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Catching Throwable
            if (P_THROWABLE_CATCH.matcher(l).find()) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_catch_throwable), f, i + 1));
            }

            // finalize()
            if (l.contains("protected void finalize()")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_finalize), f, i + 1));
            }

            // Double/Float ==
            if ((l.contains("double") || l.contains("float")) && l.contains("==")
                    && !l.contains("!=") && !l.contains("case")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Порівняння double/float через ==", f, i + 1));
            }

            // java.util.Date
            if (l.contains("new Date()") || l.contains("java.util.Date")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_java_util_date), f, i + 1));
            }

            // SimpleDateFormat
            if (l.contains("new SimpleDateFormat")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_simple_date_format), f, i + 1));
            }

            // SQL injection
            if (l.contains("\"SELECT") || l.contains("\"INSERT") || l.contains("\"UPDATE")
                    || l.contains("\"DELETE")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "SQL конкатенацією — SQL-ін'єкція!", f, i + 1));
            }

            // Math.random()
            if (l.contains("Math.random()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_math_random), f, i + 1));
            }

            // Runtime.exec
            if (l.contains("Runtime.getRuntime().exec(") || l.contains("ProcessBuilder")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_runtime_exec), f, i + 1));
            }

            // Mutable static field
            if (l.contains("static ") && !l.contains("final ") && l.contains("List")
                    || l.contains("static ") && !l.contains("final ") && l.contains("Map")
                    || l.contains("static ") && !l.contains("final ") && l.contains("Set")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_mutable_static), f, i + 1));
            }

            // Field should be final if not reassigned
            if (l.startsWith("private ") && l.contains(";") && !l.contains("final")
                    && !l.contains("static") && !l.contains("(")) {
                // Heuristic: check if field is reassigned
                String fieldName = l.replaceAll(".*\\s+(\\w+)\\s*[=;].*", "$1");
                if (!fieldName.equals(l)) {
                    boolean reassigned = false;
                    for (int j = i + 1; j < lines.length; j++) {
                        if (lines[j].contains(fieldName + " =") || lines[j].contains(fieldName + "=")) {
                            reassigned = true;
                            break;
                        }
                    }
                    if (!reassigned) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "Поле '" + fieldName + "' не перезаписується — зробіть final", f, i + 1));
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  12. Architecture
    // ══════════════════════════════════════════════════════════

    private static void analyzeArchitecture(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Long method chain
            int dots = 0;
            for (char c : l.toCharArray()) {
                if (c == '.') dots++;
            }
            if (dots > 4) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Довгий ланцюжок викликів (" + dots + ")", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  13. Naming
    // ══════════════════════════════════════════════════════════

    private static void analyzeNaming(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        Pattern pVarDecl = Pattern.compile(
                "^\\s*(?:private|protected|public)?\\s*(?:static)?\\s*(?:final)?\\s*"
                + "[A-Z][\\w<>\\[\\],]*\\s+([a-z]\\w*)\\s*[=;]");

        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*") || l.startsWith("import")) continue;

            // Single-char variable
            if (!l.contains("for ") && !l.contains("for(")) {
                Matcher m = pVarDecl.matcher(l);
                if (m.find()) {
                    String varName = m.group(1);
                    if (varName.length() == 1 && !varName.equals("i") && !varName.equals("j")
                            && !varName.equals("e")) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "Однолітерна змінна '" + varName + "'", f, i + 1));
                    }
                }
            }

            // Boolean naming
            if (l.contains("boolean ") && l.contains("=")) {
                String varName = l.replaceAll(".*boolean\\s+(\\w+)\\s*=.*", "$1");
                if (!varName.equals(l) && !varName.startsWith("is") && !varName.startsWith("has")
                        && !varName.startsWith("can") && !varName.startsWith("should")
                        && !varName.startsWith("was") && !varName.startsWith("will")
                        && !varName.startsWith("use") && !varName.startsWith("enable")
                        && !varName.startsWith("disable") && !varName.startsWith("flag")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Boolean '" + varName + "' — префікс is/has/can", f, i + 1));
                }
            }

            // Hungarian notation (mField, sField)
            if (l.contains("private ") && (l.contains("m") && l.matches(".*\\bm[A-Z]\\w+.*"))) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Hungarian notation (mField) — не рекомендується", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  14. Resource management
    // ══════════════════════════════════════════════════════════

    private static void analyzeResourceManagement(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Unclosed Connection/Statement/ResultSet
            if (l.contains("Connection") && l.contains("DriverManager.getConnection")) {
                boolean hasTryWithResources = false;
                for (int j = Math.max(0, i - 2); j <= Math.min(lines.length - 1, i + 1); j++) {
                    if (lines[j].contains("try (")) hasTryWithResources = true;
                }
                if (!hasTryWithResources) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_connection_leak), f, i + 1));
                }
            }

            // Socket without close
            if (l.contains("new Socket(") || l.contains("new ServerSocket(")) {
                boolean hasTryWithResources = false;
                for (int j = Math.max(0, i - 2); j <= Math.min(lines.length - 1, i + 1); j++) {
                    if (lines[j].contains("try (")) hasTryWithResources = true;
                }
                if (!hasTryWithResources) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_socket_leak), f, i + 1));
                }
            }

            // ExecutorService shutdown
            if (l.contains("Executors.new") || l.contains("new ThreadPoolExecutor")) {
                boolean hasShutdown = false;
                for (int j = i; j < Math.min(lines.length, i + 50); j++) {
                    if (lines[j].contains("shutdown")) hasShutdown = true;
                }
                if (!hasShutdown) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_executor_no_shutdown), f, i + 1));
                }
            }

            // Temp file without deleteOnExit
            if (l.contains("File.createTempFile")) {
                boolean hasDelete = false;
                for (int j = i; j < Math.min(lines.length, i + 20); j++) {
                    if (lines[j].contains("deleteOnExit") || lines[j].contains("delete()")) hasDelete = true;
                }
                if (!hasDelete) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_temp_no_delete), f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  15. Null safety
    // ══════════════════════════════════════════════════════════

    private static void analyzeNullSafety(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Dereference after null check (inverted)
            if (l.contains("if (") && l.contains("== null")) {
                String varName = l.replaceAll(".*if\\s*\\(\\s*(\\w+)\\s*==\\s*null.*", "$1");
                if (!varName.equals(l)) {
                    // Check if next line dereferences
                    if (i + 1 < lines.length && lines[i + 1].contains(varName + ".")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_npe_after_null_check), f, i + 2));
                    }
                }
            }

            // Returning null from collection method
            if (l.contains("return null;")) {
                // Check if method returns collection type
                String methodName = "";
                for (int j = i; j >= Math.max(0, i - 30); j--) {
                    Matcher m = P_METHOD_DECL.matcher(lines[j]);
                    if (m.find()) {
                        methodName = m.group(5);
                        break;
                    }
                }
                if (methodName.contains("get") || methodName.contains("find") || methodName.contains("search")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Повернення null — розгляньте Optional або Collections.empty*", f, i + 1));
                }
            }

            // Arrays.asList(null) or new String[]{null}
            if (l.contains("null") && (l.contains("Arrays.asList") || l.contains("new String[]{"))) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_null_in_collection), f, i + 1));
            }

            // Optional.get() without isPresent
            if (l.contains(".get()") && !l.contains("isPresent") && !l.contains("orElse")) {
                for (int j = Math.max(0, i - 5); j < i; j++) {
                    if (lines[j].contains("Optional")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                "Optional.get() без перевірки — можливий NoSuchElementException", f, i + 1));
                        break;
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  16. Thread safety
    // ══════════════════════════════════════════════════════════

    private static void analyzeThreadSafety(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        boolean hasSynchronized = false;
        boolean hasVolatile = false;
        boolean hasConcurrent = false;

        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            if (l.contains("synchronized")) hasSynchronized = true;
            if (l.contains("volatile")) hasVolatile = true;
            if (l.contains("ConcurrentHashMap") || l.contains("AtomicInteger")
                    || l.contains("AtomicLong") || l.contains("AtomicBoolean")) hasConcurrent = true;

            // SimpleDateFormat in static context
            if (l.contains("static") && l.contains("SimpleDateFormat")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_simple_date_format_static), f, i + 1));
            }

            // HashMap in multi-threaded context
            if (l.contains("new HashMap<>()") && (hasSynchronized || hasConcurrent)) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_hashmap_threaded), f, i + 1));
            }

            // AtomicInteger for simple increment
            if (l.contains("AtomicInteger") && l.contains("incrementAndGet")) {
                // This is correct usage, just info
            }

            // Double-checked locking without volatile
            if (l.contains("if (instance == null)") && l.contains("synchronized")) {
                boolean hasVolatileField = false;
                for (int j = 0; j < i; j++) {
                    if (lines[j].contains("volatile") && lines[j].contains("instance")) {
                        hasVolatileField = true;
                        break;
                    }
                }
                if (!hasVolatileField) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_dcl_volatile), f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  17. Modernization (Java 8+)
    // ══════════════════════════════════════════════════════════

    private static void analyzeModernization(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // for-each with index on List
            if (l.contains("for (int i = 0") && l.contains(".size()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Indexed for на List — for-each або stream()", f, i + 1));
            }

            // Manual null check + stream
            if (l.contains("if (") && l.contains("!= null") && i + 1 < lines.length
                    && lines[i + 1].contains(".stream()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Optional.ifPresent() замість null check + method", f, i + 1));
            }

            // StringBuilder append chain
            if (l.contains("new StringBuilder()") || l.contains("new StringBuilder(\"\"")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "StringBuilder — розгляньте String.join() або stream().collect()", f, i + 1));
            }

            // Anonymous class for single method (lambda)
            if (l.contains("new Runnable()") || l.contains("new ActionListener()")
                    || l.contains("new Comparator()") || l.contains("new Callable()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_anonymous_to_lambda), f, i + 1));
            }

            // Manual array copy
            if (l.contains("System.arraycopy(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "System.arraycopy — Arrays.copyOf() коротший", f, i + 1));
            }

            // Arrays.toString() for debug
            if (l.contains("Arrays.toString(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Arrays.toString() — для дебагу використовуйте Arrays.deepToString()", f, i + 1));
            }

            // Old-style date parsing
            if (l.contains("SimpleDateFormat") && l.contains("parse(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "SimpleDateFormat.parse — LocalDate.parse() в Java 8+", f, i + 1));
            }

            // Can use diamond operator
            if (l.contains("new HashMap<String,") || l.contains("new ArrayList<String")
                    || l.contains("new HashSet<String")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Diamond operator <> — приберіть типи справа", f, i + 1));
            }

            // switch without break (fall-through)
            if (l.startsWith("case ") && i + 1 < lines.length) {
                String next = lines[i + 1].trim();
                if (!next.startsWith("break") && !next.startsWith("return")
                        && !next.startsWith("throw") && !next.startsWith("case")
                        && !next.startsWith("default") && !next.equals("}")
                        && next.length() > 0) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            "Fall-through в switch — забули break?", f, i + 2));
                }
            }

            // switch with String (Java 7+)
            if (l.contains("switch (") && l.contains("String")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Switch з String — розгляньте enum або Map", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  18. Testing
    // ══════════════════════════════════════════════════════════

    private static void analyzeTesting(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        boolean isTestClass = f.getName().endsWith("Test.java") || f.getName().endsWith("Tests.java")
                || f.getName().contains("Test");

        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            if (isTestClass) {
                // Test without assertion
                if (l.contains("@Test") && i + 1 < lines.length) {
                    int methodEnd = Math.min(i + 30, lines.length);
                    boolean hasAssert = false;
                    for (int j = i + 1; j < methodEnd; j++) {
                        if (lines[j].contains("assert") || lines[j].contains("verify")
                                || lines[j].contains("expect") || lines[j].contains("should")) {
                            hasAssert = true;
                            break;
                        }
                    }
                    if (!hasAssert) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_test_no_assert), f, i + 1));
                    }
                }

                // System.out in test
                if (l.contains("System.out") || l.contains("System.err")) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_test_sysout), f, i + 1));
                }

                // Thread.sleep in test
                if (l.contains("Thread.sleep")) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            "Thread.sleep у тесті — ненадійно, використовуйте Awaitility", f, i + 1));
                }

                // Empty test
                if (l.contains("@Test")) {
                    int methodEnd = Math.min(i + 10, lines.length);
                    boolean hasCode = false;
                    for (int j = i + 1; j < methodEnd; j++) {
                        if (lines[j].contains(";") && !lines[j].trim().startsWith("//")) {
                            hasCode = true;
                            break;
                        }
                    }
                    if (!hasCode) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_test_empty), f, i + 1));
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  19. Documentation
    // ══════════════════════════════════════════════════════════

    private static void analyzeDocumentation(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        // Missing class JavaDoc
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (P_CLASS_DECL.matcher(l).find() && l.contains("public")) {
                boolean hasDoc = false;
                for (int j = Math.max(0, i - 5); j < i; j++) {
                    if (lines[j].contains("/**")) {
                        hasDoc = true;
                        break;
                    }
                }
                if (!hasDoc) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_missing_class_javadoc), f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  20. Deprecated
    // ══════════════════════════════════════════════════════════

    private static void analyzeDeprecated(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // @Deprecated without @java.lang.Deprecated
            if (l.contains("@Deprecated") && !l.contains("java.lang.Deprecated")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "@Deprecated без javadoc @deprecated тегу", f, i + 1));
            }

            // Using deprecated methods
            if (l.contains(".toString().equals(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        ".toString().equals() — Object.equals() або String.equals()", f, i + 1));
            }

            // Hashtable/Vector usage
            if (l.contains("new Hashtable") || l.contains("new Vector")
                    || l.contains("new StringBuffer")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Застарій клас — використовуйте HashMap/ArrayList/StringBuilder", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  21. Collections
    // ══════════════════════════════════════════════════════════

    private static void analyzeCollections(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // addAll on empty list
            if (l.contains(".addAll(") && i > 0) {
                String prev = lines[i - 1].trim();
                if (prev.contains("new ArrayList") || prev.contains("new HashSet")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "addAll після створення — передайте елементи в конструктор", f, i + 1));
                }
            }

            // keySet() when entrySet needed
            if (l.contains(".keySet()")) {
                for (int j = i; j < Math.min(lines.length, i + 5); j++) {
                    if (lines[j].contains(".get(")) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "keySet() + get() — entrySet() ефективніше", f, j + 1));
                        break;
                    }
                }
            }

            // Creating list then adding elements (use Arrays.asList or List.of)
            if (l.contains("new ArrayList<>()") && i + 1 < lines.length) {
                int adds = 0;
                for (int j = i + 1; j < Math.min(lines.length, i + 20); j++) {
                    if (lines[j].contains(".add(")) adds++;
                    else if (lines[j].contains(";") || lines[j].contains("}")) break;
                }
                if (adds >= 2 && adds <= 10) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Створення та заповнення — List.of() або Arrays.asList()", f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  22. Exception handling
    // ══════════════════════════════════════════════════════════

    private static void analyzeExceptionHandling(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // catch block that just rethrows
            if (l.startsWith("catch") && i + 2 < lines.length) {
                String inside = lines[i + 1].trim();
                if (inside.startsWith("throw") && inside.endsWith(";")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Catch що просто rethrow — видаліть catch блок", f, i + 1));
                }
            }

            // Exception used for flow control
            if (l.contains("try") && i + 3 < lines.length) {
                for (int j = i + 1; j < Math.min(lines.length, i + 10); j++) {
                    if (lines[j].startsWith("catch")) {
                        String catchContent = lines[j + 1] != null ? lines[j + 1].trim() : "";
                        if (catchContent.contains("return")) {
                            out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                    "Exception для flow control — неправильний патерн", f, j + 1));
                        }
                        break;
                    }
                }
            }

            // Thrown exception type matches catch
            if (l.startsWith("throw new ")) {
                String exType = l.replaceAll(".*throw new\\s+(\\w+).*", "$1");
                for (int j = i; j < Math.min(lines.length, i + 20); j++) {
                    if (lines[j].contains("catch") && lines[j].contains(exType)) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                "Ловите власний виняток — можливо, зайве", f, j + 1));
                        break;
                    }
                }
            }

            // Empty finally
            if (l.contains("finally") && i + 1 < lines.length) {
                String next = lines[i + 1].trim();
                if (next.equals("}")) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            "Порожній finally блок", f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  23. Immutability rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeImmutability(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Mutable field should be final if never reassigned
            if (l.startsWith("private ") && !l.contains("final") && !l.contains("static")
                    && !l.contains("(") && l.contains(";")) {
                String varName = l.replaceAll(".*\\s+(\\w+)\\s*[=;].*", "$1");
                if (!varName.equals(l)) {
                    boolean reassigned = false;
                    for (int j = 0; j < lines.length; j++) {
                        if (j != i && (lines[j].contains(varName + " =") || lines[j].contains(varName + "="))) {
                            reassigned = true;
                            break;
                        }
                    }
                    if (!reassigned) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "Поле '" + varName + "' можна зробити final", f, i + 1));
                    }
                }
            }

            // Non-static final field with mutable type
            if (l.contains("private final List") || l.contains("private final Map")
                    || l.contains("private final Set") || l.contains("private final ArrayList")
                    || l.contains("private final HashMap")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Final колекція — використовуйте unmodifiableList/Map/Set", f, i + 1));
            }

            // String field should be final
            if (l.matches(".*private\\s+String\\s+\\w+\\s*[=;].*") && !l.contains("final")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_string_field_final), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  24. Generics rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeGenerics(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Raw type List/Map/Set
            if (l.matches(".*\\b(List|Map|Set|ArrayList|HashMap|HashSet|LinkedList|TreeMap|TreeSet|LinkedHashMap)\\s+\\w+.*")
                    && !l.contains("<") && !l.contains("import") && !l.contains("class ")
                    && !l.contains("extends") && !l.contains("implements")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_raw_type_generic), f, i + 1));
            }

            // Unchecked cast
            if (l.contains("(") && l.contains(")") && l.contains("<")) {
                if (l.contains("Map<") || l.contains("List<") || l.contains("Set<")) {
                    // Heuristic for unchecked cast
                    if (l.contains(")= (") || l.contains(") = (")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                "Можливий unchecked cast — додайте @SuppressWarnings(\"unchecked\")", f, i + 1));
                    }
                }
            }

            // Wildcard in return type
            if (l.contains("return") && l.contains("? extends")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_wildcard_return), f, i + 1));
            }

            // Nested generics > 2 levels
            int angleCount = 0;
            for (char c : l.toCharArray()) {
                if (c == '<') angleCount++;
            }
            if (angleCount > 2) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Глибока вкладеність generic (> 2) — розгляньте TypeAlias", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  25. Stream API rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeStreams(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Stream without terminal operation
            if (l.contains(".stream()")) {
                boolean hasTerminal = false;
                for (int j = i; j < Math.min(lines.length, i + 10); j++) {
                    if (lines[j].contains(".collect(") || lines[j].contains(".forEach(")
                            || lines[j].contains(".count()") || lines[j].contains(".findFirst(")
                            || lines[j].contains(".anyMatch(") || lines[j].contains(".allMatch(")
                            || lines[j].contains(".noneMatch(") || lines[j].contains(".reduce(")
                            || lines[j].contains(".sum()") || lines[j].contains(".toList()")) {
                        hasTerminal = true;
                        break;
                    }
                }
                if (!hasTerminal) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_stream_no_terminal), f, i + 1));
                }
            }

            // collect(toList()) — use toList() in Java 16+
            if (l.contains(".collect(Collectors.toList())")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "collect(toList()) — у Java 16+ .toList() коротше", f, i + 1));
            }

            // Stream.findFirst().get() — use orElseThrow
            if (l.contains(".findFirst()") && l.contains(".get()")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "findFirst().get() — .findFirst().orElseThrow() безпечніше", f, i + 1));
            }

            // Parallel stream on small collections
            if (l.contains(".parallelStream()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_parallel_stream), f, i + 1));
            }

            // forEach with return (doesn't work)
            if (l.contains(".forEach(") && i + 1 < lines.length) {
                for (int j = i + 1; j < Math.min(lines.length, i + 5); j++) {
                    if (lines[j].trim().startsWith("return")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_foreach_return), f, j + 1));
                        break;
                    }
                    if (lines[j].contains("})") || lines[j].contains("});")) break;
                }
            }

            // map + filter chain readability
            if (l.contains(".map(") && l.contains(".filter(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "map + filter — розгляньте flatMap для складних випадків", f, i + 1));
            }

            // Optional.map().orElse(null)
            if (l.contains(".map(") && l.contains(".orElse(null)")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "map().orElse(null) — використовуйте map().orElseGet()", f, i + 1));
            }

            // flatMap to flatten
            if (l.contains(".flatMap(")) {
                // Correct usage
            }

            // forEach on Stream (use for-each loop for simple cases)
            if (l.contains(".stream()") && l.contains(".forEach(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Stream.forEach — for-each цикл може бути читабельнішим", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  26. Annotation rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeAnnotations(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // @Override missing
            if (l.contains("public boolean equals(") || l.contains("public int hashCode(")
                    || l.contains("public String toString(")
                    || l.contains("protected void finalize(")) {
                if (i == 0 || !lines[i - 1].contains("@Override")) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_missing_override, l.substring(0, Math.min(30, l.length()))),
                            f, i + 1));
                }
            }

            // @SuppressWarnings("unchecked") without justification
            if (l.contains("@SuppressWarnings(\"unchecked\")")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "@SuppressWarnings(\"unchecked\") — додайте коментар чому", f, i + 1));
            }

            // @Deprecated without javadoc
            if (l.contains("@Deprecated") && !l.contains("@deprecated")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_deprecated_javadoc), f, i + 1));
            }

            // Multiple @SuppressWarnings
            if (l.contains("@SuppressWarnings") && l.contains(",")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Кілька suppressions — можливо, код потребує рефакторингу", f, i + 1));
            }

            // @FunctionalInterface on non-interface
            if (l.contains("@FunctionalInterface")) {
                // Should be followed by interface
            }

            // @Nullable / @NonNull
            if (l.contains("@Nullable") && i + 1 < lines.length) {
                if (lines[i + 1].contains(".get()") || lines[i + 1].contains(".toString()")) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_nullable_dereference), f, i + 2));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  27. Enum rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeEnums(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Enum with too many constants
            if (l.contains("enum ") && l.contains("{")) {
                int enumEnd = Math.min(lines.length, i + MAX_ENUM_CONSTANTS + 5);
                int count = 0;
                for (int j = i + 1; j < enumEnd; j++) {
                    String el = lines[j].trim();
                    if (el.endsWith(";") || el.equals("}")) break;
                    if (!el.isEmpty() && !el.startsWith("//") && !el.startsWith("/*")
                            && !el.contains("(") && !el.contains("public ") && !el.contains("private ")) {
                        count++;
                    }
                }
                if (count > MAX_ENUM_CONSTANTS) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Enum з " + count + " константами — розгляньте Strategy pattern",
                            f, i + 1));
                }
            }

            // ordinal() usage (fragile)
            if (l.contains(".ordinal()")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "ordinal() — ненадійний, використовуйте name() або custom field",
                        f, i + 1));
            }

            // EnumMap/EnumSet instead of HashMap for enum keys
            if (l.contains("HashMap<") && l.contains("Enum")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "HashMap з enum ключами — EnumMap ефективніше", f, i + 1));
            }

            // switch on enum without default
            if (l.contains("switch") && i + 1 < lines.length) {
                boolean hasDefault = false;
                for (int j = i + 1; j < Math.min(lines.length, i + 30); j++) {
                    if (lines[j].contains("default:")) {
                        hasDefault = true;
                        break;
                    }
                    if (lines[j].contains("}")) break;
                }
                if (!hasDefault) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "switch без default — нові константи enum не обробляться", f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  28. API design rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeApiDesign(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Public method returning mutable collection
            if (l.contains("public") && l.contains("List") && l.contains("return")) {
                if (!l.contains("Collections.unmodifiable") && !l.contains("List.of")
                        && !l.contains("List.copyOf") && !l.contains("ImmutableList")) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            str(ctx, R.string.sa_mutable_collection_return), f, i + 1));
                }
            }

            // Method with boolean parameter (flag argument)
            if (l.contains("public") && l.contains("boolean") && l.contains(",")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Boolean параметр — розгляньте Builder або два методи", f, i + 1));
            }

            // Public field
            if (l.contains("public ") && l.contains(";") && !l.contains("(")
                    && !l.contains("static final") && !l.contains("class ")
                    && !l.contains("interface ") && !l.contains("enum ")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_public_field), f, i + 1));
            }

            // Static mutable field
            if (l.contains("public static") && !l.contains("final") && !l.contains("(")
                    && (l.contains("List") || l.contains("Map") || l.contains("Set")
                    || l.contains("String") || l.contains("int") || l.contains("boolean"))) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_public_static_mutable), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  29. Copy/paste detection
    // ══════════════════════════════════════════════════════════

    private static void analyzeCopyPaste(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        // Simple duplicate line detection
        Map<String, Integer> lineCount = new HashMap<>();
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.length() < 20 || l.startsWith("//") || l.startsWith("*")
                    || l.startsWith("import") || l.startsWith("package")
                    || l.contains("return;") || l.equals("}") || l.equals("{")) continue;
            lineCount.merge(l, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : lineCount.entrySet()) {
            if (e.getValue() > 3) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "Дубльований рядок (" + e.getValue() + " разів): " + e.getKey().substring(0, Math.min(40, e.getKey().length())),
                        f, 1));
            }
        }

        // Duplicate code blocks (3+ identical consecutive lines)
        for (int i = 0; i < lines.length - 2; i++) {
            String a = lines[i].trim();
            String b = lines[i + 1].trim();
            String c = lines[i + 2].trim();
            if (a.length() > 15 && a.equals(b) && b.equals(c)) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_triple_duplicate), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  30. Field rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeFieldRules(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        List<Integer> fieldLines = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (P_FIELD_DECL.matcher(l).find() && !l.contains("(")) {
                fieldLines.add(i);
            }
        }

        // Check field ordering (constants should be first)
        boolean seenNonConstant = false;
        for (int lineIdx : fieldLines) {
            String l = lines[lineIdx].trim();
            if (l.contains("static final")) {
                if (seenNonConstant) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_constant_before_fields), f, lineIdx + 1));
                }
            } else if (l.startsWith("private") || l.startsWith("protected") || l.startsWith("public")) {
                seenNonConstant = true;
            }
        }

        // Check for field coupling (field used in only one method)
        for (int lineIdx : fieldLines) {
            String l = lines[lineIdx].trim();
            Matcher m = P_FIELD_DECL.matcher(l);
            if (m.find()) {
                String fname = m.group(1);
                int usageCount = 0;
                for (String line : lines) {
                    if (line.contains(fname) && !P_FIELD_DECL.matcher(line).find()) {
                        usageCount++;
                    }
                }
                if (usageCount == 1) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Поле '" + fname + "' використовується в одному місці — локальна змінна?",
                            f, lineIdx + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  31. Switch rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeSwitch(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Switch without default
            if (l.contains("switch (")) {
                boolean hasDefault = false;
                int depth = 0;
                for (int j = i; j < Math.min(lines.length, i + 50); j++) {
                    for (char c : lines[j].toCharArray()) {
                        if (c == '{') depth++;
                        if (c == '}') depth--;
                    }
                    if (lines[j].contains("default:")) hasDefault = true;
                    if (depth == 0 && j > i) break;
                }
                if (!hasDefault) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            "switch без default — непередбачені значення не обробляються", f, i + 1));
                }
            }

            // case without break/return/throw (fall-through)
            if (l.startsWith("case ") && !l.contains("->")) {
                boolean hasBreak = false;
                for (int j = i + 1; j < Math.min(lines.length, i + 10); j++) {
                    String next = lines[j].trim();
                    if (next.startsWith("break") || next.startsWith("return") || next.startsWith("throw")) {
                        hasBreak = true;
                        break;
                    }
                    if (next.startsWith("case ") || next.startsWith("default:") || next.equals("}")) break;
                }
                if (!hasBreak) {
                    out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                            "Fall-through в switch — забули break?", f, i + 1));
                }
            }

            // Switch expression with multiple cases
            if (l.contains("switch (") && l.contains(") {")) {
                int caseCount = 0;
                for (int j = i + 1; j < Math.min(lines.length, i + 30); j++) {
                    if (lines[j].trim().startsWith("case ")) caseCount++;
                    if (lines[j].trim().equals("}")) break;
                }
                if (caseCount > 10) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Switch з " + caseCount + " case — розгляньте polymorphism або Map",
                            f, i + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  32. Lambda rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeLambda(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Lambda too long (> 5 lines)
            if (l.contains("->") && l.contains("{")) {
                int depth = 0;
                int lambdaLines = 0;
                for (int j = i; j < Math.min(lines.length, i + 20); j++) {
                    for (char c : lines[j].toCharArray()) {
                        if (c == '{') depth++;
                        if (c == '}') depth--;
                    }
                    lambdaLines++;
                    if (depth == 0 && j > i) break;
                }
                if (lambdaLines > 5) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_lambda_long), f, i + 1));
                }
            }

            // Single-parameter lambda without explicit type
            if (l.contains("->") && l.contains("->")) {
                // Already good practice in Java
            }

            // Method reference possible
            if (l.contains("->") && l.contains("()")) {
                // Heuristic: x -> x.method() could be Class::method
                if (l.matches(".*\\w+\\s*->\\s*\\w+\\.\\w+\\(\\).*")) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            str(ctx, R.string.sa_lambda_method_ref), f, i + 1));
                }
            }

            // Effectively final variable in lambda
            if (l.contains("->") && i > 0) {
                // Check if lambda uses a non-final variable
                // This is complex, just info
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  33. Optional rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeOptional(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Optional.get() without isPresent
            if (l.contains(".get()") && !l.contains("orElse") && !l.contains("orElseThrow")
                    && !l.contains("ifPresent")) {
                for (int j = Math.max(0, i - 10); j < i; j++) {
                    if (lines[j].contains("Optional")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_optional_get), f, i + 1));
                        break;
                    }
                }
            }

            // Optional.get().equals()
            if (l.contains(".get().equals(")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Optional.get().equals() — .map().orElse() безпечніше", f, i + 1));
            }

            // Optional field (anti-pattern)
            if (l.contains("Optional<") && l.contains(";") && !l.contains("return")
                    && !l.contains("(") && !l.contains("new ")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        "Optional як поле — anti-pattern (не Serializable)", f, i + 1));
            }

            // Optional.orElse(null)
            if (l.contains(".orElse(null)")) {
                out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                        str(ctx, R.string.sa_optional_or_null), f, i + 1));
            }

            // Optional empty() with filter
            if (l.contains("Optional.empty()") && l.contains(".filter(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_optional_empty_filter), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  34. Date/time rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeDate(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // new Date() — use Instant
            if (l.contains("new Date()") && !l.contains("//")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_date_new), f, i + 1));
            }

            // Calendar.getInstance() — use LocalDateTime
            if (l.contains("Calendar.getInstance()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_calendar), f, i + 1));
            }

            // SimpleDateFormat with pattern
            if (l.contains("new SimpleDateFormat(")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "SimpleDateFormat — DateTimeFormatter (thread-safe)", f, i + 1));
            }

            // Date.getTime() for comparison
            if (l.contains(".getTime()") && (l.contains(">") || l.contains("<") || l.contains("=="))) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_date_gettime), f, i + 1));
            }

            // java.sql.Date
            if (l.contains("java.sql.Date") || l.contains("java.sql.Timestamp")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_sql_date), f, i + 1));
            }

            // ZoneId usage
            if (l.contains("TimeZone.getDefault()")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_timezone), f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  35. Regex rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeRegex(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*")) continue;

            // Pattern.compile inside loop
            if ((l.contains("for ") || l.contains("while ")) && i + 1 < lines.length) {
                for (int j = i + 1; j < Math.min(lines.length, i + 20); j++) {
                    if (lines[j].contains("Pattern.compile(")) {
                        out.add(new ProblemItem(ProblemItem.Severity.WARNING,
                                str(ctx, R.string.sa_pattern_compile_loop), f, j + 1));
                        break;
                    }
                    if (lines[j].contains("}")) break;
                }
            }

            // String.matches() — Pattern.compile кожного разу
            if (l.contains(".matches(\"")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        "String.matches() — Pattern.compile() краще для повторного використання",
                        f, i + 1));
            }

            // Greedy quantifier in regex
            if (l.contains("Pattern.compile(") && l.contains(".*")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        str(ctx, R.string.sa_greedy_regex), f, i + 1));
            }

            // String.split with regex that could be simple
            if (l.contains(".split(\"\\\\.\"")) {
                out.add(new ProblemItem(ProblemItem.Severity.INFO,
                        ".split(\"\\\\.\") — .split(Pattern.quote(\".\")) безпечніше", f, i + 1));
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  36. Inline rules
    // ══════════════════════════════════════════════════════════

    private static void analyzeInline(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        // Check for methods that could be inlined (used only once, short)
        List<int[]> methods = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            Matcher m = P_METHOD_DECL.matcher(lines[i]);
            if (m.find() && !lines[i].trim().startsWith("//") && !lines[i].trim().startsWith("*")) {
                methods.add(new int[]{i});
            }
        }

        for (int[] method : methods) {
            String methodName = extractMethodName(lines[method[0]]);
            if (methodName == null) continue;

            // Count usages
            int usageCount = 0;
            for (String line : lines) {
                if (line.contains(methodName + "(") && !P_METHOD_DECL.matcher(line).find()) {
                    usageCount++;
                }
            }

            if (usageCount == 1) {
                // Find method length
                int braceCount = 0;
                boolean foundOpen = false;
                int endLine = method[0];
                for (int i = method[0]; i < lines.length; i++) {
                    for (char c : lines[i].toCharArray()) {
                        if (c == '{') { braceCount++; foundOpen = true; }
                        else if (c == '}') braceCount--;
                    }
                    if (foundOpen && braceCount == 0) { endLine = i; break; }
                }

                int methodLen = endLine - method[0] + 1;
                if (methodLen <= 3) {
                    out.add(new ProblemItem(ProblemItem.Severity.INFO,
                            "Метод '" + methodName + "' використовується 1 раз і короткий — inline",
                            f, method[0] + 1));
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  37. Final local variables
    // ══════════════════════════════════════════════════════════

    private static void analyzeFinalLocal(Context ctx, String[] lines, File f, List<ProblemItem> out) {
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.startsWith("//") || l.startsWith("*") || l.startsWith("import")) continue;

            // Local variable that could be final
            if (P_LOCAL_VAR.matcher(l).find() && !l.contains("final")) {
                String varName = l.replaceAll(".*\\s+(\\w+)\\s*[=;].*", "$1");
                if (!varName.equals(l) && !varName.isEmpty()) {
                    boolean reassigned = false;
                    for (int j = i + 1; j < lines.length; j++) {
                        if (lines[j].contains(varName + " =") || lines[j].contains(varName + "=")) {
                            reassigned = true;
                            break;
                        }
                        if (lines[j].contains("}") && j > i + 10) break;
                    }
                    if (!reassigned) {
                        out.add(new ProblemItem(ProblemItem.Severity.INFO,
                                "Локальна змінна '" + varName + "' — final для читабельності",
                                f, i + 1));
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════

    private static int countPattern(String text, String regex) {
        int count = 0;
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find()) count++;
        return count;
    }
}
