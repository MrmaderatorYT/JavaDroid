package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts documentation fragments into a self-contained Java 8 compilation unit. */
final class JavaSnippetSourceBuilder {

    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+[^;]+;\\s*$");
    private static final Pattern PACKAGE = Pattern.compile("(?m)^\\s*package\\s+[^;]+;\\s*$");
    private static final Pattern TYPE_START = Pattern.compile(
            "(?m)^\\s*(?:(?:public|protected|private)\\s+)?(?:(?:abstract|final)\\s+)?"
                    + "(?:class|interface|enum|@interface)\\s+[A-Za-z_$][\\w$]*[^\\n{]*\\{");
    private static final Pattern STATIC_METHOD_START = Pattern.compile(
            "(?m)^\\s*(?:(?:public|protected|private)\\s+)?static\\s+"
                    + "(?:<[^>]+>\\s*)?[\\w.$<>?\\[\\], &]+\\s+[A-Za-z_$][\\w$]*\\s*\\([^;]*?\\)"
                    + "(?:\\s+throws\\s+[^\\{]+)?\\s*\\{");
    private static final Pattern TYPE_NAME = Pattern.compile(
            "\\b(?:class|interface|enum|@interface)\\s+([A-Za-z_$][\\w$]*)");
    private static final Pattern MAIN_METHOD = Pattern.compile(
            "\\bpublic\\s+static\\s+void\\s+main\\s*\\(\\s*String(?:\\s*\\[\\s*]|\\s*\\.\\.\\.)"
                    + "\\s*[A-Za-z_$][\\w$]*\\s*\\)");

    private JavaSnippetSourceBuilder() {
    }

    static String build(String displayedCode, boolean compileOnly) {
        Parts parts = split(displayedCode);
        String body = parts.body.trim();
        if (compileOnly && !body.isEmpty()) {
            parts.members.add("    static void example() throws Exception {\n"
                    + indent(body, "        ") + "\n    }\n");
            body = "System.out.println(\"✓ Приклад успішно скомпільовано\");";
        } else if (body.isEmpty()) {
            String entryPoint = compileOnly ? null : findEntryPoint(parts.members);
            body = entryPoint == null
                    ? "System.out.println(\"✓ Приклад успішно скомпільовано\");"
                    : entryPoint + ".main(new String[0]);";
        }
        return assemble(parts, body);
    }

    /** Returns the first example type that already declares a conventional Java entry point. */
    private static String findEntryPoint(List<String> members) {
        for (String member : members) {
            Matcher type = TYPE_NAME.matcher(member);
            if (type.find() && MAIN_METHOD.matcher(member).find()) {
                return type.group(1);
            }
        }
        return null;
    }

    static String buildWithMain(String displayedCode, String mainBody) {
        return assemble(split(displayedCode), mainBody);
    }

    private static String assemble(Parts parts, String body) {
        StringBuilder out = new StringBuilder();
        out.append("import java.io.*;\n")
                .append("import java.lang.annotation.*;\n")
                .append("import java.lang.reflect.*;\n")
                .append("import java.math.*;\n")
                .append("import java.net.*;\n")
                .append("import java.nio.*;\n")
                .append("import java.nio.channels.*;\n")
                .append("import java.nio.charset.*;\n")
                .append("import java.nio.file.*;\n")
                .append("import java.sql.*;\n")
                .append("import java.text.*;\n")
                .append("import java.time.*;\n")
                .append("import java.time.format.*;\n")
                .append("import java.time.temporal.*;\n")
                .append("import java.util.*;\n")
                .append("import java.util.concurrent.*;\n")
                .append("import java.util.concurrent.atomic.*;\n")
                .append("import java.util.concurrent.locks.*;\n")
                .append("import java.util.function.*;\n")
                .append("import java.util.regex.*;\n")
                .append("import java.util.stream.*;\n");
        for (String value : parts.imports) out.append(value.trim()).append('\n');
        out.append("\npublic final class SnippetRunner {\n");
        for (String member : parts.members) {
            out.append(makeNested(member.trim())).append("\n\n");
        }
        out.append("    public static void main(String[] args) throws Exception {\n")
                .append(indent(body.trim(), "        ")).append('\n')
                .append("    }\n")
                .append("}\n");
        return out.toString();
    }

    private static Parts split(String source) {
        Parts parts = new Parts();
        Matcher imports = IMPORT.matcher(source);
        StringBuffer withoutImports = new StringBuffer();
        while (imports.find()) {
            parts.imports.add(imports.group().trim());
            imports.appendReplacement(withoutImports, "");
        }
        imports.appendTail(withoutImports);
        String remaining = PACKAGE.matcher(withoutImports.toString()).replaceAll("");

        while (true) {
            Match next = firstConstruct(remaining);
            if (next == null) break;
            int start = includeLeadingAnnotations(remaining, next.start);
            int open = remaining.indexOf('{', next.start);
            int close = matchingBrace(remaining, open);
            if (open < 0 || close < 0) break;
            int end = close + 1;
            while (end < remaining.length() && Character.isWhitespace(remaining.charAt(end))) end++;
            if (end < remaining.length() && remaining.charAt(end) == ';') end++;
            parts.members.add(remaining.substring(start, end));
            remaining = remaining.substring(0, start) + newlineMask(remaining.substring(start, end))
                    + remaining.substring(end);
        }
        parts.body = remaining;
        return parts;
    }

    private static Match firstConstruct(String source) {
        Matcher type = TYPE_START.matcher(source);
        Matcher method = STATIC_METHOD_START.matcher(source);
        boolean hasType = type.find();
        boolean hasMethod = method.find();
        if (!hasType && !hasMethod) return null;
        if (hasType && (!hasMethod || type.start() <= method.start())) {
            return new Match(type.start());
        }
        return new Match(method.start());
    }

    private static int includeLeadingAnnotations(String source, int start) {
        int result = start;
        while (result > 0) {
            int previousEnd = result;
            while (previousEnd > 0 && (source.charAt(previousEnd - 1) == '\n'
                    || source.charAt(previousEnd - 1) == '\r')) previousEnd--;
            int lineStart = source.lastIndexOf('\n', Math.max(0, previousEnd - 1)) + 1;
            String line = source.substring(lineStart, previousEnd).trim();
            if (line.startsWith("@") || line.startsWith("//") || line.isEmpty()) {
                result = lineStart;
            } else {
                break;
            }
        }
        return result;
    }

    private static int matchingBrace(String source, int open) {
        if (open < 0) return -1;
        int depth = 0;
        boolean string = false, character = false, escape = false, lineComment = false, blockComment = false;
        for (int i = open; i < source.length(); i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';
            if (lineComment) {
                if (c == '\n') lineComment = false;
                continue;
            }
            if (blockComment) {
                if (c == '*' && next == '/') { blockComment = false; i++; }
                continue;
            }
            if (string || character) {
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (string && c == '"') string = false;
                if (character && c == '\'') character = false;
                continue;
            }
            if (c == '/' && next == '/') { lineComment = true; i++; continue; }
            if (c == '/' && next == '*') { blockComment = true; i++; continue; }
            if (c == '"') { string = true; continue; }
            if (c == '\'') { character = true; continue; }
            if (c == '{') depth++;
            if (c == '}' && --depth == 0) return i;
        }
        return -1;
    }

    private static String makeNested(String member) {
        Matcher type = Pattern.compile(
                "(?m)^(\\s*)((?:(?:public|protected|private)\\s+)?)(?:(abstract|final)\\s+)?class\\s+")
                .matcher(member);
        if (!type.find()) return indent(member, "    ");
        String modifier = type.group(2) == null ? "" : type.group(2);
        String kind = type.group(3) == null ? "" : type.group(3) + " ";
        String replacement = type.group(1) + modifier + "static " + kind + "class ";
        return indent(type.replaceFirst(Matcher.quoteReplacement(replacement)), "    ");
    }

    private static String newlineMask(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            out.append(c == '\n' || c == '\r' ? c : ' ');
        }
        return out.toString();
    }

    private static String indent(String value, String prefix) {
        if (value.isEmpty()) return prefix;
        return prefix + value.replace("\r\n", "\n").replace("\r", "\n")
                .replace("\n", "\n" + prefix);
    }

    private static final class Parts {
        final List<String> imports = new ArrayList<>();
        final List<String> members = new ArrayList<>();
        String body = "";
    }

    private static final class Match {
        final int start;
        Match(int start) { this.start = start; }
    }
}
