package com.ccs.javadroid;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;

/**
 * Доповнення автодоповнення: методи через reflection та класи з проєкту.
 */
public final class JavaReflectionCompletion {

    private static final Pattern P_PACKAGE = Pattern.compile(
            "^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern P_CLASS = Pattern.compile(
            "\\b(?:public\\s+)?(?:abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)");
    private static final Pattern P_IMPORT = Pattern.compile(
            "^\\s*import\\s+(?:static\\s+)?([\\w.*]+)\\s*;", Pattern.MULTILINE);

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null",
            "var", "record", "yield", "sealed", "permits"));

    private JavaReflectionCompletion() {}

    public static void contribute(@NonNull Context appContext, @Nullable File projectRoot,
                                  @NonNull ContentReference contentRef, @NonNull CharPosition position,
                                  @NonNull String prefix, @NonNull CompletionPublisher publisher) {
        publisher.checkCancelled();
        ContentReference cref = contentRef;
        String fullSource = cref.getReference().toString();
        ClassLoader cl = appContext.getClassLoader();

        String line = cref.getLine(position.line);
        int col = Math.min(position.column, line.length());
        String beforeCursor = line.substring(0, col);
        int lastDot = beforeCursor.lastIndexOf('.');

        if (lastDot >= 0) {
            String beforeDot = beforeCursor.substring(0, lastDot).trim();
            String receiver = lastIdentifier(beforeDot);
            if (receiver != null) {
                List<String> imports = parseImports(fullSource);
                String pkg = parsePackage(fullSource);
                String typeName = findDeclaredType(fullSource, receiver);
                Class<?> cls = resolveTypeToClass(typeName, imports, pkg, cl);
                if (cls != null) {
                    addMethodItems(cls, prefix, publisher);
                }
            }
            return;
        }

        addImportItems(importsFlat(fullSource), prefix, publisher);
        if (projectRoot != null) {
            addProjectClassItems(projectRoot, prefix, publisher);
        }
    }

    private static List<String> importsFlat(String source) {
        List<String> out = new ArrayList<>();
        Matcher m = P_IMPORT.matcher(source);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    private static void addImportItems(List<String> imports, String prefix, CompletionPublisher pub) {
        if (prefix.isEmpty()) {
            return;
        }
        pub.checkCancelled();
        Set<String> seen = new HashSet<>();
        String pl = prefix.toLowerCase(Locale.ROOT);
        for (String imp : imports) {
            if (imp.endsWith(".*")) {
                continue;
            }
            int dot = imp.lastIndexOf('.');
            String simple = dot >= 0 ? imp.substring(dot + 1) : imp;
            if (!simple.toLowerCase(Locale.ROOT).startsWith(pl)) {
                continue;
            }
            if (!seen.add(imp)) {
                continue;
            }
            SimpleCompletionItem it = new SimpleCompletionItem(
                    simple, imp, prefix.length(), simple);
            it.kind(CompletionItemKind.Class);
            pub.addItem(it);
            if (seen.size() > 80) {
                break;
            }
        }
    }

    private static void addProjectClassItems(File projectRoot, String prefix, CompletionPublisher pub) {
        pub.checkCancelled();
        if (prefix.isEmpty()) {
            return;
        }
        List<File> files = ProjectScanner.listJavaSources(projectRoot);
        String pl = prefix.toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        for (File f : files) {
            pub.checkCancelled();
            try {
                String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                String pkg = parsePackage(s);
                Matcher mc = P_CLASS.matcher(s);
                while (mc.find()) {
                    String cn = mc.group(1);
                    String fq = (pkg == null || pkg.isEmpty()) ? cn : pkg + "." + cn;
                    if (!cn.toLowerCase(Locale.ROOT).startsWith(pl)
                            && !fq.toLowerCase(Locale.ROOT).startsWith(pl)) {
                        continue;
                    }
                    if (!seen.add(fq)) {
                        continue;
                    }
                    SimpleCompletionItem it = new SimpleCompletionItem(
                            fq, f.getName(), prefix.length(), cn);
                    it.kind(CompletionItemKind.Class);
                    pub.addItem(it);
                    if (seen.size() > 120) {
                        return;
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Nullable
    static String lastIdentifier(String expr) {
        if (expr == null) {
            return null;
        }
        expr = expr.trim();
        if (expr.isEmpty()) {
            return null;
        }
        int end = expr.length();
        int i = end - 1;
        while (i >= 0 && Character.isWhitespace(expr.charAt(i))) {
            i--;
        }
        end = i + 1;
        while (i >= 0 && (Character.isJavaIdentifierPart(expr.charAt(i)) || expr.charAt(i) == '.')) {
            i--;
        }
        String token = expr.substring(i + 1, end);
        int ld = token.lastIndexOf('.');
        return ld >= 0 ? token.substring(ld + 1) : token;
    }

    @Nullable
    static String parsePackage(String source) {
        Matcher m = P_PACKAGE.matcher(source);
        return m.find() ? m.group(1) : null;
    }

    static List<String> parseImports(String source) {
        List<String> out = new ArrayList<>();
        Matcher m = P_IMPORT.matcher(source);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    @Nullable
    static String findDeclaredType(String source, String varName) {
        if ("this".equals(varName)) {
            Matcher m = Pattern.compile("\\bclass\\s+(\\w+)").matcher(source);
            return m.find() ? m.group(1) : null;
        }
        if ("super".equals(varName)) {
            Matcher m = Pattern.compile("class\\s+\\w+\\s+extends\\s+([A-Za-z_][\\w.]*)").matcher(source);
            return m.find() ? stripGenerics(m.group(1)) : null;
        }
        Pattern decl = Pattern.compile(
                "(?:^|[;{}\\n])\\s*(?:@[\\w.]+\\s+)*(?:public|private|protected|static|final|volatile|transient|\\s)*"
                        + "([A-Za-z_][\\w.]*(?:\\.[A-Za-z_][\\w.]*)*)\\s+" + Pattern.quote(varName) + "\\b");
        Matcher m = decl.matcher(source);
        String last = null;
        while (m.find()) {
            String t = stripGenerics(m.group(1));
            if (!JAVA_KEYWORDS.contains(t) && !isPrimitiveLike(t)) {
                last = t;
            }
        }
        return last;
    }

    private static boolean isPrimitiveLike(String t) {
        return "int".equals(t) || "long".equals(t) || "short".equals(t) || "byte".equals(t)
                || "char".equals(t) || "float".equals(t) || "double".equals(t) || "boolean".equals(t)
                || "void".equals(t);
    }

    static String stripGenerics(String t) {
        if (t == null) {
            return null;
        }
        int idx = t.indexOf('<');
        return idx > 0 ? t.substring(0, idx).trim() : t.trim();
    }

    @Nullable
    static Class<?> resolveTypeToClass(String typeName, List<String> imports, String pkg,
                                       ClassLoader cl) {
        if (typeName == null) {
            return null;
        }
        typeName = stripGenerics(typeName);
        if (typeName.isEmpty()) {
            return null;
        }
        if (typeName.contains(".")) {
            try {
                return Class.forName(typeName, false, cl);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                return null;
            }
        }
        for (String imp : imports) {
            if (imp.endsWith("." + typeName)) {
                try {
                    return Class.forName(imp, false, cl);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
            if (imp.endsWith(".*")) {
                String base = imp.substring(0, imp.length() - 1) + typeName;
                try {
                    return Class.forName(base, false, cl);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
        }
        if (pkg != null && !pkg.isEmpty()) {
            try {
                return Class.forName(pkg + "." + typeName, false, cl);
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            }
        }
        try {
            return Class.forName("java.lang." + typeName, false, cl);
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
        }
        return null;
    }

    private static void addMethodItems(Class<?> cls, String prefix, CompletionPublisher pub) {
        pub.checkCancelled();
        String pl = prefix.toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        Method[] methods = cls.getMethods();
        Arrays.sort(methods, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        int n = 0;
        for (Method m : methods) {
            pub.checkCancelled();
            if (!Modifier.isPublic(m.getModifiers())) {
                continue;
            }
            if (!m.getName().toLowerCase(Locale.ROOT).startsWith(pl)) {
                continue;
            }
            String sig = m.getName() + Arrays.toString(m.getParameterTypes());
            if (!seen.add(sig)) {
                continue;
            }
            String label = formatMethodLabel(m);
            String ret = formatReturnType(m);
            SimpleCompletionItem it = new SimpleCompletionItem(label, ret, prefix.length(), m.getName() + "(");
            it.kind(CompletionItemKind.Method);
            pub.addItem(it);
            n++;
            if (n >= 200) {
                break;
            }
        }
    }

    private static String formatMethodLabel(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName()).append('(');
        Class<?>[] p = m.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(simpleParamType(p[i]));
        }
        sb.append(')');
        return sb.toString();
    }

    private static String simpleParamType(Class<?> c) {
        if (c.isArray()) {
            return simpleParamType(c.getComponentType()) + "[]";
        }
        return c.getSimpleName();
    }

    private static String formatReturnType(Method m) {
        Class<?> r = m.getReturnType();
        if (r == void.class) {
            return "void";
        }
        if (r.isArray()) {
            return simpleParamType(r.getComponentType()) + "[]";
        }
        return r.getSimpleName();
    }
}
