package com.ccs.javadroid.util;

import com.ccs.javadroid.maven.MavenPaths;
import com.ccs.javadroid.project.ProjectScanner;
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;

/**
 * Доповнення автодоповнення: методи через reflection, класи з проєкту,
 * вбудовані Java-бібліотеки та шаблони коду.
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

    // ── Вбудовані Java-бібліотеки: popular classes + їх popular methods ─────────
    private static final String[][] BUILTIN_CLASSES = {
            {"System", "java.lang.System"},
            {"String", "java.lang.String"},
            {"Integer", "java.lang.Integer"},
            {"Double", "java.lang.Double"},
            {"Long", "java.lang.Long"},
            {"Boolean", "java.lang.Boolean"},
            {"Character", "java.lang.Character"},
            {"Math", "java.lang.Math"},
            {"Object", "java.lang.Object"},
            {"Class", "java.lang.Class"},
            {"Thread", "java.lang.Thread"},
            {"Runnable", "java.lang.Runnable"},
            {"Exception", "java.lang.Exception"},
            {"RuntimeException", "java.lang.RuntimeException"},
            {"NullPointerException", "java.lang.NullPointerException"},
            {"IllegalArgumentException", "java.lang.IllegalArgumentException"},
            {"StringBuilder", "java.lang.StringBuilder"},
            {"StringBuffer", "java.lang.StringBuffer"},
            {"Override", "java.lang.Override"},
            {"SuppressWarnings", "java.lang.SuppressWarnings"},
            {"Comparable", "java.lang.Comparable"},
            {"Iterable", "java.lang.Iterable"},
            {"Number", "java.lang.Number"},
            {"Void", "java.lang.Void"},
            {"Package", "java.lang.Package"},
            {"StackTraceElement", "java.lang.StackTraceElement"},
            {"AutoCloseable", "java.lang.AutoCloseable"},
            {"Cloneable", "java.lang.Cloneable"},
            {"Readable", "java.lang.Readable"},
            {"Iterable", "java.lang.Iterable"},

            // java.util
            {"ArrayList", "java.util.ArrayList"},
            {"LinkedList", "java.util.LinkedList"},
            {"HashMap", "java.util.HashMap"},
            {"LinkedHashMap", "java.util.LinkedHashMap"},
            {"TreeMap", "java.util.TreeMap"},
            {"HashSet", "java.util.HashSet"},
            {"TreeSet", "java.util.TreeSet"},
            {"LinkedHashSet", "java.util.LinkedHashSet"},
            {"Collections", "java.util.Collections"},
            {"Arrays", "java.util.Arrays"},
            {"List", "java.util.List"},
            {"Map", "java.util.Map"},
            {"Set", "java.util.Set"},
            {"Queue", "java.util.Queue"},
            {"Deque", "java.util.Deque"},
            {"Stack", "java.util.Stack"},
            {"Vector", "java.util.Vector"},
            {"Hashtable", "java.util.Hashtable"},
            {"Date", "java.util.Date"},
            {"Calendar", "java.util.Calendar"},
            {"GregorianCalendar", "java.util.GregorianCalendar"},
            {"Locale", "java.util.Locale"},
            {"Random", "java.util.Random"},
            {"UUID", "java.util.UUID"},
            {"Optional", "java.util.Optional"},
            {"Objects", "java.util.Objects"},
            {"Collections", "java.util.Collections"},
            {"Iterator", "java.util.Iterator"},
            {"ListIterator", "java.util.ListIterator"},
            {"Comparator", "java.util.Comparator"},
            {"Comparator", "java.util.Comparator"},
            {"Stream", "java.util.stream.Stream"},
            {"Collectors", "java.util.stream.Collectors"},
            {"HashMap", "java.util.HashMap"},

            // java.io
            {"File", "java.io.File"},
            {"FileReader", "java.io.FileReader"},
            {"FileWriter", "java.io.FileWriter"},
            {"BufferedReader", "java.io.BufferedReader"},
            {"BufferedWriter", "java.io.BufferedWriter"},
            {"InputStream", "java.io.InputStream"},
            {"OutputStream", "java.io.OutputStream"},
            {"FileInputStream", "java.io.FileInputStream"},
            {"FileOutputStream", "java.io.FileOutputStream"},
            {"ByteArrayInputStream", "java.io.ByteArrayInputStream"},
            {"ByteArrayOutputStream", "java.io.ByteArrayOutputStream"},
            {"PrintWriter", "java.io.PrintWriter"},
            {"PrintStream", "java.io.PrintStream"},
            {"Scanner", "java.util.Scanner"},
            {"ObjectInputStream", "java.io.ObjectInputStream"},
            {"ObjectOutputStream", "java.io.ObjectOutputStream"},
            {"Serializable", "java.io.Serializable"},
            {"IOException", "java.io.IOException"},
            {"FileNotFoundException", "java.io.FileNotFoundException"},
            {"BufferedInputStream", "java.io.BufferedInputStream"},
            {"BufferedOutputStream", "java.io.BufferedOutputStream"},
            {"DataInputStream", "java.io.DataInputStream"},
            {"DataOutputStream", "java.io.DataOutputStream"},
            {"File", "java.io.File"},

            // java.nio
            {"Files", "java.nio.file.Files"},
            {"Path", "java.nio.file.Path"},
            {"Paths", "java.nio.file.Paths"},
            {"ByteBuffer", "java.nio.ByteBuffer"},
            {"CharBuffer", "java.nio.CharBuffer"},
            {"StandardCharsets", "java.nio.charset.StandardCharsets"},
            {"Charset", "java.nio.charset.Charset"},

            // java.time
            {"LocalDate", "java.time.LocalDate"},
            {"LocalTime", "java.time.LocalTime"},
            {"LocalDateTime", "java.time.LocalDateTime"},
            {"Instant", "java.time.Instant"},
            {"Duration", "java.time.Duration"},
            {"Period", "java.time.Period"},
            {"ZonedDateTime", "java.time.ZonedDateTime"},
            {"DateTimeFormatter", "java.time.format.DateTimeFormatter"},

            // java.lang.reflect
            {"Method", "java.lang.reflect.Method"},
            {"Field", "java.lang.reflect.Field"},
            {"Constructor", "java.lang.reflect.Constructor"},
            {"Modifier", "java.lang.reflect.Modifier"},
            {"Array", "java.lang.reflect.Array"},
            {"Proxy", "java.lang.reflect.Proxy"},

            // java.math
            {"BigDecimal", "java.math.BigDecimal"},
            {"BigInteger", "java.math.BigInteger"},
            {"MathContext", "java.math.MathContext"},
            {"RoundingMode", "java.math.RoundingMode"},

            // java.net
            {"URL", "java.net.URL"},
            {"URI", "java.net.URI"},
            {"HttpURLConnection", "java.net.HttpURLConnection"},
            {"InetAddress", "java.net.InetAddress"},
            {"ServerSocket", "java.net.ServerSocket"},
            {"Socket", "java.net.Socket"},

            // java.util.concurrent
            {"ExecutorService", "java.util.concurrent.ExecutorService"},
            {"Executors", "java.util.concurrent.Executors"},
            {"Future", "java.util.concurrent.Future"},
            {"Callable", "java.util.concurrent.Callable"},
            {"ConcurrentHashMap", "java.util.concurrent.ConcurrentHashMap"},
            {"AtomicInteger", "java.util.concurrent.atomic.AtomicInteger"},
            {"AtomicLong", "java.util.concurrent.atomic.AtomicLong"},
            {"CountDownLatch", "java.util.concurrent.CountDownLatch"},
            {"Semaphore", "java.util.concurrent.Semaphore"},
            {"ReentrantLock", "java.util.concurrent.locks.ReentrantLock"},
    };

    private static final String[][] COMMON_SNIPPETS = {
            // ── Print / Output ──────────────────────────────
            {"sout",  "System.out.println($1);",        "Print to stdout"},
            {"soutv", "System.out.println(\"$1 = \" + $1);", "Print variable with name"},
            {"serr",  "System.err.println($1);",        "Print to stderr"},
            {"printf", "System.out.printf(\"$1%n\", $2);", "Formatted print"},

            // ── Control flow ────────────────────────────────
            {"if",      "if ($1) {\n    $2\n}",                                   "If statement"},
            {"ife",     "if ($1) {\n    $2\n} else {\n    $3\n}",                 "If-else statement"},
            {"ifnn",    "if ($1 != null) {\n    $2\n}",                           "If not null check"},
            {"ifeq",    "if ($1.equals($2)) {\n    $3\n}",                        "If equals check"},
            {"while",   "while ($1) {\n    $2\n}",                                "While loop"},
            {"for",     "for ($1; $2; $3) {\n    $4\n}",                          "For loop"},
            {"fori",    "for (int ${1:i} = 0; ${1:i} < $2; ${1:i}++) {\n    $3\n}", "Indexed for loop"},
            {"foreach", "for (${1:Type} ${2:item} : ${3:iterable}) {\n    $4\n}",  "Enhanced for-each loop"},
            {"switch",  "switch ($1) {\n    case $2:\n        $3\n        break;\n    default:\n        break;\n}", "Switch statement"},

            // ── Try / Catch ─────────────────────────────────
            {"try",     "try {\n    $1\n} catch (Exception ${2:e}) {\n    $3\n}",              "Try-catch block"},
            {"tryc",    "try {\n    $1\n} catch (Exception ${2:e}) {\n    $3\n}",              "Try-catch block"},
            {"tryf",    "try {\n    $1\n} finally {\n    $2\n}",                          "Try-finally block"},
            {"trycf",   "try {\n    $1\n} catch (Exception ${2:e}) {\n    $3\n} finally {\n    $4\n}",  "Try-catch-finally"},
            {"tryr",    "try ($1) {\n    $2\n}",                                  "Try-with-resources"},

            // ── Methods / Classes ────────────────────────────
            {"main",  "public static void main(String[] args) {\n    $1\n}",   "Main method"},
            {"psvm",  "public static void main(String[] args) {\n    $1\n}",   "public static void main"},
            {"cw",    "public class $1 {\n    $2\n}",                             "New public class"},
            {"ab",    "abstract class $1 {\n    $2\n}",                           "Abstract class"},
            {"itf",   "public interface $1 {\n    $2\n}",                         "Public interface"},
            {"enum",  "enum $1 {\n    $2\n}",                                     "Enum declaration"},
            {"ann",   "@interface $1 {\n    $2\n}",                               "Annotation interface"},
            {"ctor",  "public $1() {\n    $2\n}",                                "Constructor"},
            {"m",     "public void $1() {\n    $2\n}",                           "Public void method"},
            {"ms",    "public static void $1() {\n    $2\n}",                    "Static void method"},
            {"mf",    "public int $1() {\n    return $2;\n}",                            "Method returning int"},

            // ── Object creation ──────────────────────────────
            {"new",    "new $1($2)",                     "New object instance"},
            {"newa",   "new $1[$2]",                    "New array"},

            // ── Return / Throw ───────────────────────────────
            {"return", "return $1;",                   "Return statement"},
            {"throw",  "throw new $1($2);",              "Throw exception"},
            {"throws", "throws $1",                    "Throws declaration"},

            // ── Lambda / Streams ─────────────────────────────
            {"lambda",  "($1) -> {\n    $2\n}",                                                     "Lambda expression"},
            {"lsp",     "(${1:s}) -> ${1:s}.$2",                                                    "Lambda single param"},
            {"stream",  ".stream().filter($1).map($2).collect(Collectors.toList())",        "Stream pipeline"},
            {"filter",  ".stream().filter(${1:item} -> $2).collect(Collectors.toList())",          "Filter stream"},
            {"map",     ".stream().map(${1:item} -> $2).collect(Collectors.toList())",             "Map stream"},
            {"collect", ".collect(Collectors.toList())",                                "Collect to list"},

            // ── Collections ──────────────────────────────────
            {"list",   "new ArrayList<>()",          "New ArrayList"},
            {"map",    "new HashMap<>()",            "New HashMap"},
            {"set",    "new HashSet<>()",            "New HashSet"},
            {"coll",   "Collections.$1",               "Collections utility"},

            // ── Optional / Null safety ───────────────────────
            {"opt",   "Optional.ofNullable($1).orElse($2)",             "Optional wrap"},
            {"optif", "Optional.ofNullable($1).ifPresent(${2:val} -> {\n    $3\n})",    "Optional ifPresent"},

            // ── Common patterns ──────────────────────────────
            {"sync",       "synchronized ($1) {\n    $2\n}",                                              "Synchronized block"},
            {"inst",       "instanceof $1",                                                      "Instanceof check"},
            {"sleep",      "Thread.sleep($1);",                                                   "Thread sleep"},
            {"run",        "new Thread(() -> {\n    $1\n}).start();",                                    "New thread"},
            {"runnable",   "Runnable ${1:r} = () -> {\n    $2\n};",                                           "Runnable lambda"},
            {"comparable", "implements Comparable<$1>",                                          "Implement Comparable"},
            {"iterable",   "implements Iterable<$1>",                                            "Implement Iterable"},

            // ── Annotations ──────────────────────────────────
            {"ovr",  "@Override\n",                    "Override annotation"},
            {"dep",  "@Deprecated\n",                  "Deprecated annotation"},
            {"sup",  "@SuppressWarnings(\"$1\")\n",  "Suppress warnings"},
            {"fn",   "@FunctionalInterface\n",         "Functional interface"},

            // ── Logging ──────────────────────────────────────
            {"log",   "private static final Logger log = Logger.getLogger($1.class.getName());", "Logger field"},
            {"logi",  "log.info(\"$1\");",             "Log INFO"},
            {"logw",  "log.warning(\"$1\");",          "Log WARNING"},
            {"loge",  "log.severe(\"$1\");",           "Log SEVERE"}
    };

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

        if (prefix.isEmpty()) return;

        // Snippets
        addSnippetItems(prefix, publisher);
        publisher.checkCancelled();

        // Import-и: конкретні + wildcard-и (import java.util.* → всі класи пакету)
        addImportItems(importsFlat(fullSource), prefix, publisher);
        addWildcardImportItems(fullSource, prefix, publisher, cl);
        publisher.checkCancelled();

        // Проєктні класи
        if (projectRoot != null) {
            addProjectClassItems(projectRoot, prefix, publisher);
        }
        publisher.checkCancelled();

        // Maven dependency classes (з .javadroid/repository/*.jar)
        if (projectRoot != null) {
            addMavenDependencyItems(projectRoot, prefix, publisher);
        }
        publisher.checkCancelled();

        // Вбудовані Java-бібліотеки
        addBuiltinClassItems(prefix, publisher);
    }

    private static void addSnippetItems(String prefix, CompletionPublisher pub) {
        pub.checkCancelled();
        String pl = prefix.toLowerCase(Locale.ROOT);
        for (String[] snippet : COMMON_SNIPPETS) {
            if (snippet[0].startsWith(pl)) {
                try {
                    String desc = snippet.length > 2 ? snippet[2] : snippet[1];
                    SnippetDescription sDesc = new SnippetDescription(
                        prefix.length(),
                        CodeSnippetParser.parse(snippet[1]),
                        true
                    );
                    SimpleSnippetCompletionItem it = new SimpleSnippetCompletionItem(snippet[0], desc, sDesc);
                    it.kind(CompletionItemKind.Snippet);
                    pub.addItem(it);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void addBuiltinClassItems(String prefix, CompletionPublisher pub) {
        pub.checkCancelled();
        String pl = prefix.toLowerCase(Locale.ROOT);
        int count = 0;
        for (String[] cls : BUILTIN_CLASSES) {
            if (cls[0].toLowerCase(Locale.ROOT).startsWith(pl)) {
                SimpleCompletionItem it = new SimpleCompletionItem(
                        cls[0], cls[1], prefix.length(), cls[0]);
                it.kind(CompletionItemKind.Class);
                pub.addItem(it);
                count++;
                if (count >= 60) break;
            }
        }
    }

    /**
     * Обробляє wildcard import-и (import java.util.*): шукає класи з вбудованого
     * списку, що належать до пакету, зазначеного у wildcard.
     */
    private static void addWildcardImportItems(String fullSource, String prefix,
                                                CompletionPublisher pub, ClassLoader cl) {
        pub.checkCancelled();
        String pl = prefix.toLowerCase(Locale.ROOT);
        List<String> wildcards = new ArrayList<>();
        Matcher m = P_IMPORT.matcher(fullSource);
        while (m.find()) {
            String imp = m.group(1);
            if (imp.endsWith(".*")) {
                wildcards.add(imp.substring(0, imp.length() - 2));
            }
        }
        if (wildcards.isEmpty()) return;

        Set<String> seen = new HashSet<>();
        int count = 0;
        for (String wcPkg : wildcards) {
            for (String[] cls : BUILTIN_CLASSES) {
                if (count >= 80) return;
                String fqName = cls[1];
                if (fqName.startsWith(wcPkg + ".")) {
                    String simple = cls[0];
                    if (!simple.toLowerCase(Locale.ROOT).startsWith(pl)) continue;
                    if (!seen.add(simple)) continue;
                    SimpleCompletionItem it = new SimpleCompletionItem(
                            simple, fqName, prefix.length(), simple);
                    it.kind(CompletionItemKind.Class);
                    pub.addItem(it);
                    count++;
                }
            }
            // Також шукаємо через reflection у classpath
            resolveWildcardPackage(wcPkg, prefix, pub, cl, seen);
        }
    }

    /**
     * Сканує jar-файли Maven-залежностей (.javadroid/repository/) та додає
     * класи з відповідних пакетів як підказки.
     */
    private static void addMavenDependencyItems(File projectRoot, String prefix,
                                                  CompletionPublisher pub) {
        pub.checkCancelled();
        String pl = prefix.toLowerCase(Locale.ROOT);
        File repoDir = MavenPaths.localRepoDir(projectRoot);
        if (!repoDir.isDirectory()) return;

        File[] jars = repoDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) return;

        Set<String> seen = new HashSet<>();
        int count = 0;
        for (File jar : jars) {
            if (count >= 100) return;
            pub.checkCancelled();
            try {
                JarFile jf = new JarFile(jar, true);
                Enumeration<JarEntry> entries = jf.entries();
                while (entries.hasMoreElements() && count < 100) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class") && !name.contains("$")
                            && !name.startsWith("META-INF/")) {
                        // Перетворюємо шлях на FQN: com/example/Foo.class → com.example.Foo
                        String fqn = name.substring(0, name.length() - 6).replace('/', '.');
                        // Беремо simple name
                        int dot = fqn.lastIndexOf('.');
                        String simple = dot >= 0 ? fqn.substring(dot + 1) : fqn;
                        if (!simple.toLowerCase(Locale.ROOT).startsWith(pl)) continue;
                        if (!seen.add(fqn)) continue;
                        SimpleCompletionItem it = new SimpleCompletionItem(
                                simple + "  [" + jar.getName() + "]", fqn, prefix.length(), simple);
                        it.kind(CompletionItemKind.Class);
                        pub.addItem(it);
                        count++;
                    }
                }
                jf.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Спроба знайти класи з заданого пакету через classloader (для базових Java пакетів).
     */
    private static void resolveWildcardPackage(String pkgName, String prefix,
                                                CompletionPublisher pub, ClassLoader cl,
                                                Set<String> seen) {
        // Додаємо popular підпакети для wildcard import-ів
        String[] subPkgs = {"", ".nio", ".nio.charset", ".nio.file",
                ".time", ".time.format", ".time.temporal", ".time.zone",
                ".concurrent", ".concurrent.atomic", ".concurrent.locks",
                ".function", ".stream", ".stream.collectors"};
        for (String sub : subPkgs) {
            String fullPkg = pkgName + sub;
            try {
                // Не вдасться enumerate, але перевіримо кілька типових класів
                String[] probeClasses = {"List", "Map", "Set", "Collection", "Iterator",
                        "Optional", "Stream", "Collectors", "HashMap", "ArrayList",
                        "LinkedList", "TreeMap", "TreeSet", "HashSet",
                        "LinkedHashMap", "LinkedHashSet", "Arrays", "Collections",
                        "Objects", "Comparator", "Function", "Predicate", "Consumer",
                        "Supplier", "Runnable", "Callable", "Future",
                        "ExecutorService", "Executors", "ConcurrentHashMap",
                        "AtomicInteger", "AtomicLong", "ReentrantLock",
                        "Semaphore", "CountDownLatch", "LocalDate", "LocalTime",
                        "LocalDateTime", "Instant", "Duration", "Period",
                        "ZonedDateTime", "DateTimeFormatter", "ZoneId",
                        "ByteBuffer", "CharBuffer", "Files", "Path", "Paths",
                        "StandardCharsets", "Charset"};
                for (String probe : probeClasses) {
                    if (seen.contains(probe)) continue;
                    try {
                        Class<?> c = Class.forName(fullPkg + "." + probe, false, cl);
                        String simple = c.getSimpleName();
                        if (!simple.toLowerCase(Locale.ROOT).startsWith(
                                prefix.toLowerCase(Locale.ROOT))) continue;
                        if (!seen.add(simple)) continue;
                        SimpleCompletionItem it = new SimpleCompletionItem(
                                simple, c.getName(), prefix.length(), simple);
                        it.kind(CompletionItemKind.Class);
                        pub.addItem(it);
                    } catch (ClassNotFoundException ignored) {}
                }
            } catch (Throwable ignored) {}
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
        if (prefix.isEmpty()) return;
        pub.checkCancelled();
        Set<String> seen = new HashSet<>();
        String pl = prefix.toLowerCase(Locale.ROOT);
        for (String imp : imports) {
            if (imp.endsWith(".*")) continue;
            int dot = imp.lastIndexOf('.');
            String simple = dot >= 0 ? imp.substring(dot + 1) : imp;
            if (!simple.toLowerCase(Locale.ROOT).startsWith(pl)) continue;
            if (!seen.add(imp)) continue;
            SimpleCompletionItem it = new SimpleCompletionItem(
                    simple, imp, prefix.length(), simple);
            it.kind(CompletionItemKind.Class);
            pub.addItem(it);
            if (seen.size() > 80) break;
        }
    }

    private static void addProjectClassItems(File projectRoot, String prefix, CompletionPublisher pub) {
        pub.checkCancelled();
        if (prefix.isEmpty()) return;
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
                            && !fq.toLowerCase(Locale.ROOT).startsWith(pl)) continue;
                    if (!seen.add(fq)) continue;
                    SimpleCompletionItem it = new SimpleCompletionItem(
                            fq, f.getName(), prefix.length(), cn);
                    it.kind(CompletionItemKind.Class);
                    pub.addItem(it);
                    if (seen.size() > 120) return;
                }
            } catch (IOException ignored) {}
        }
    }

    @Nullable
    static String lastIdentifier(String expr) {
        if (expr == null) return null;
        expr = expr.trim();
        if (expr.isEmpty()) return null;
        int end = expr.length();
        int i = end - 1;
        while (i >= 0 && Character.isWhitespace(expr.charAt(i))) i--;
        end = i + 1;
        while (i >= 0 && (Character.isJavaIdentifierPart(expr.charAt(i)) || expr.charAt(i) == '.')) i--;
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
        while (m.find()) out.add(m.group(1));
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
        if (t == null) return null;
        int idx = t.indexOf('<');
        return idx > 0 ? t.substring(0, idx).trim() : t.trim();
    }

    @Nullable
    static Class<?> resolveTypeToClass(String typeName, List<String> imports, String pkg, ClassLoader cl) {
        if (typeName == null) return null;
        typeName = stripGenerics(typeName);
        if (typeName.isEmpty()) return null;
        if (typeName.contains(".")) {
            try {
                return Class.forName(typeName, false, cl);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                return null;
            }
        }
        for (String imp : imports) {
            if (imp.endsWith("." + typeName)) {
                try { return Class.forName(imp, false, cl); }
                catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
            }
            if (imp.endsWith(".*")) {
                String base = imp.substring(0, imp.length() - 1) + typeName;
                try { return Class.forName(base, false, cl); }
                catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
            }
        }
        if (pkg != null && !pkg.isEmpty()) {
            try { return Class.forName(pkg + "." + typeName, false, cl); }
            catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
        }
        try { return Class.forName("java.lang." + typeName, false, cl); }
        catch (ClassNotFoundException | NoClassDefFoundError ignored) {}
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
            if (!Modifier.isPublic(m.getModifiers())) continue;
            if (!m.getName().toLowerCase(Locale.ROOT).startsWith(pl)) continue;
            String sig = m.getName() + Arrays.toString(m.getParameterTypes());
            if (!seen.add(sig)) continue;
            String label = formatMethodLabel(m);
            String ret = formatReturnType(m);
            SimpleCompletionItem it = new SimpleCompletionItem(label, ret, prefix.length(), m.getName() + "(");
            it.kind(CompletionItemKind.Method);
            pub.addItem(it);
            n++;
            if (n >= 200) break;
        }
    }

    private static String formatMethodLabel(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName()).append('(');
        Class<?>[] p = m.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(simpleParamType(p[i]));
        }
        sb.append(')');
        return sb.toString();
    }

    private static String simpleParamType(Class<?> c) {
        if (c.isArray()) return simpleParamType(c.getComponentType()) + "[]";
        return c.getSimpleName();
    }

    private static String formatReturnType(Method m) {
        Class<?> r = m.getReturnType();
        if (r == void.class) return "void";
        if (r.isArray()) return simpleParamType(r.getComponentType()) + "[]";
        return r.getSimpleName();
    }
}
