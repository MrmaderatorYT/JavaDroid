package com.ccs.javadroid;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;

import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dalvik.system.DexClassLoader;

/**
 * Компіляція ECJ + D8, Maven-проєкти та пакування JAR.
 */
public final class ProjectCompiler {

    public interface Callback {
        void onProgress(String message);
        void onResult(String output);
        void onProblems(List<ProblemItem> problems);
    }

    private ProjectCompiler() {}

    /**
     * Лише ECJ (без D8), для фонового аналізу. Викликати не з головного потоку.
     */
    public static List<ProblemItem> ecjProblemsForSource(Context context, String sourceCode,
                                                         File logicalFile) {
        try {
            if (sourceCode == null || sourceCode.trim().isEmpty()) {
                return new ArrayList<>();
            }
            String className = extractClassName(sourceCode);
            File cacheDir = new File(context.getCacheDir(), "live_compile_cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File srcFile = new File(cacheDir, className + ".java");
            File androidJar = ensureAndroidJar(context, cacheDir);
            writeUtf8(srcFile, sourceCode);
            String ecjErr = compileEcj(androidJar, null, cacheDir, srcFile);
            if (ecjErr == null) {
                return new ArrayList<>();
            }
            return EcjProblemParser.remapToLogicalFile(
                    EcjProblemParser.parse(ecjErr, null), logicalFile);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void runSingleSource(Context context, String sourceCode, Callback callback) {
        runSingleSource(context, sourceCode, null, callback);
    }

    /**
     * @param logicalSourceFile файл у проєкті (редактор); шляхи помилок ECJ з кешу підміняються на нього
     */
    public static void runSingleSource(Context context, String sourceCode, File logicalSourceFile,
                                       Callback callback) {
        new Thread(() -> {
            try {
                String className = extractClassName(sourceCode);
                File cacheDir = new File(context.getCacheDir(), "compile_cache");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File srcFile = new File(cacheDir, className + ".java");
                File androidJar = ensureAndroidJar(context, cacheDir);
                File dexDir = new File(cacheDir, "dex");
                if (!dexDir.exists()) dexDir.mkdirs();

                writeUtf8(srcFile, sourceCode);

                String ecjErr = compileEcj(androidJar, null, cacheDir, srcFile);
                if (ecjErr != null) {
                    postCompileFailure(callback, context, null, ecjErr, logicalSourceFile,
                            "Compilation Error:\n" + ecjErr);
                    return;
                }
                postProblems(callback, context, null, "", logicalSourceFile);

                File classFile = new File(cacheDir, className + ".class");
                if (!classFile.exists()) {
                    postResult(callback, "Error: " + className + ".class not found.");
                    return;
                }

                runD8Dex(androidJar, dexDir, classFile);
                runDexMain(context, cacheDir, dexDir, className, callback);
            } catch (Exception e) {
                postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
            }
        }).start();
    }

    public static void mavenCompileAndRun(Context context, File projectRoot, PomModel pom,
                                          Callback callback) {
        new Thread(() -> {
            try {
                File androidJar = ensureAndroidJar(context,
                        new File(context.getCacheDir(), "compile_cache"));
                File outDir = MavenPaths.targetClassesDir(projectRoot);
                outDir.mkdirs();

                List<File> depJars = MavenDependencyResolver.resolve(projectRoot, pom,
                        msg -> postProgress(callback, msg));

                String cp = classpath(depJars);
                List<File> sources = ProjectScanner.listJavaSources(projectRoot);
                if (sources.isEmpty()) {
                    postResult(callback, "Немає .java файлів у src/main/java");
                    return;
                }

                List<File> srcArgs = new ArrayList<>(sources);
                File[] ecjFiles = srcArgs.toArray(new File[0]);

                String ecjErr = compileEcjMulti(androidJar, cp, outDir, ecjFiles);
                if (ecjErr != null) {
                    postCompileFailure(callback, context, projectRoot, ecjErr, null,
                            "Compilation Error:\n" + ecjErr);
                    return;
                }
                postProblems(callback, context, projectRoot, "", null);

                List<java.nio.file.Path> classes = new ArrayList<>();
                collectClasses(outDir, classes);
                if (classes.isEmpty()) {
                    postResult(callback, "Немає .class після компіляції");
                    return;
                }

                File dexDir = new File(context.getCacheDir(), "maven_dex");
                if (!dexDir.exists()) dexDir.mkdirs();
                File[] oldDex = dexDir.listFiles();
                if (oldDex != null) {
                    for (File f : oldDex) {
                        if (f.getName().endsWith(".dex")) f.delete();
                    }
                }

                D8Command.Builder b = D8Command.builder()
                        .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                        .setDisableDesugaring(true);
                b.addLibraryFiles(androidJar.toPath());
                for (File j : depJars) b.addLibraryFiles(j.toPath());
                for (java.nio.file.Path c : classes) b.addProgramFiles(c);
                D8.run(b.build());

                String mainClass = pom.mainClass;
                if (mainClass == null || mainClass.isEmpty()) {
                    mainClass = pom.properties.get("mainClass");
                    if (mainClass != null) mainClass = pom.resolveProperty(mainClass);
                }
                if (mainClass == null || mainClass.isEmpty()) mainClass = "com.ccs.App";

                postProgress(callback, "Running " + mainClass + "...");
                runDexMain(context, context.getCacheDir(), dexDir, mainClass, callback);
            } catch (Exception e) {
                postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
            }
        }).start();
    }

    public static void mavenPackage(Context context, File projectRoot, PomModel pom, Callback callback) {
        new Thread(() -> {
            try {
                mavenCompileOnly(context, projectRoot, pom, callback);
                String mainClass = pom.mainClass;
                if (mainClass == null || mainClass.isEmpty()) {
                    mainClass = pom.properties.get("mainClass");
                    if (mainClass != null) mainClass = pom.resolveProperty(mainClass);
                }
                if (mainClass == null || mainClass.isEmpty()) mainClass = "com.ccs.App";

                File target = new File(projectRoot, "target");
                target.mkdirs();
                File jar = new File(target, pom.artifactId + "-" + pom.version + ".jar");

                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);

                try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), manifest)) {
                    File classesDir = MavenPaths.targetClassesDir(projectRoot);
                    addDirectoryToJar(classesDir, classesDir, jos);
                }

                postProgress(callback, "JAR: " + jar.getAbsolutePath());
                postResult(callback, "package: BUILD SUCCESS\n" + jar.getAbsolutePath());
            } catch (Exception e) {
                postResult(callback, "package failed: " + e.getMessage());
            }
        }).start();
    }

    public static void mavenTestCompile(Context context, File projectRoot, PomModel pom,
                                        Callback callback) {
        new Thread(() -> {
            try {
                File androidJar = ensureAndroidJar(context,
                        new File(context.getCacheDir(), "compile_cache"));
                File outDir = MavenPaths.targetTestClassesDir(projectRoot);
                outDir.mkdirs();

                List<File> deps = new ArrayList<>();
                deps.addAll(MavenDependencyResolver.resolve(projectRoot, pom,
                        msg -> postProgress(callback, msg)));
                deps.addAll(MavenDependencyResolver.resolveTestScoped(projectRoot, pom,
                        msg -> postProgress(callback, msg)));

                String cp = classpath(deps);
                File classesMain = MavenPaths.targetClassesDir(projectRoot);
                if (!classesMain.exists() || classesMain.list() == null || classesMain.list().length == 0) {
                    postResult(callback, "Спочатку виконайте compile (Run).");
                    return;
                }
                if (!cp.isEmpty()) cp += File.pathSeparator;
                cp += classesMain.getAbsolutePath();

                List<File> testSrc = ProjectScanner.listTestSources(projectRoot);
                if (testSrc.isEmpty()) {
                    postResult(callback, "Немає тестів у src/test/java");
                    return;
                }

                String ecjErr = compileEcjMulti(androidJar, cp, outDir,
                        testSrc.toArray(new File[0]));
                if (ecjErr != null) {
                    postCompileFailure(callback, context, projectRoot, ecjErr, null,
                            "Test compile failed:\n" + ecjErr);
                    return;
                }
                postResult(callback, "Test sources compiled to target/test-classes.\n"
                        + "Запуск JUnit на пристрої не вбудовано — використовуйте desktop CI.");
            } catch (Exception e) {
                postResult(callback, "testCompile: " + e.getMessage());
            }
        }).start();
    }

    private static void mavenCompileOnly(Context context, File projectRoot, PomModel pom,
                                         Callback callback) throws Exception {
        File androidJar = ensureAndroidJar(context, new File(context.getCacheDir(), "compile_cache"));
        File outDir = MavenPaths.targetClassesDir(projectRoot);
        outDir.mkdirs();

        List<File> depJars = MavenDependencyResolver.resolve(projectRoot, pom,
                msg -> postProgress(callback, msg));
        String cp = classpath(depJars);
        List<File> sources = ProjectScanner.listJavaSources(projectRoot);
        if (sources.isEmpty()) throw new IllegalStateException("no sources");

        String ecjErr = compileEcjMulti(androidJar, cp, outDir, sources.toArray(new File[0]));
        if (ecjErr != null) throw new IllegalStateException(ecjErr);
    }

    private static void addDirectoryToJar(File root, File source, JarOutputStream jos)
            throws java.io.IOException {
        File[] files = source.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                addDirectoryToJar(root, f, jos);
            } else {
                String rel = root.toPath().relativize(f.toPath()).toString().replace('\\', '/');
                jos.putNextEntry(new JarEntry(rel));
                try (FileInputStream in = new FileInputStream(f)) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) jos.write(buf, 0, n);
                }
                jos.closeEntry();
            }
        }
    }

    private static File ensureAndroidJar(Context context, File cacheDir) throws Exception {
        if (cacheDir != null && !cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new java.io.IOException("Cannot create directory: " + cacheDir.getAbsolutePath());
        }
        File androidJar = new File(cacheDir, "android.jar");
        if (!androidJar.exists() || androidJar.length() == 0L) {
            if (androidJar.exists() && !androidJar.delete()) {
                throw new java.io.IOException("Cannot replace invalid android.jar");
            }
            try (InputStream is = context.getAssets().open("android.jar");
                 FileOutputStream fos = new FileOutputStream(androidJar)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) {
                    fos.write(buf, 0, n);
                }
            }
        }
        if (!androidJar.isFile() || androidJar.length() == 0L) {
            throw new java.io.IOException("android.jar missing or empty: " + androidJar.getAbsolutePath());
        }
        return androidJar;
    }

    private static void writeUtf8(File f, String s) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String compileEcj(File androidJar, String classpath, File outDir, File... srcFiles) {
        ByteArrayOutputStream ecjOut = new ByteArrayOutputStream();
        ByteArrayOutputStream ecjErr = new ByteArrayOutputStream();
        PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(ecjOut, StandardCharsets.UTF_8), true);
        PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(ecjErr, StandardCharsets.UTF_8), true);
        Main ecj = new Main(outWriter, errWriter, false, null, null);
        List<String> args = new ArrayList<>();
        args.add("-1.8");
        args.add("-proc:none");
        args.add("-bootclasspath");
        args.add(androidJar.getAbsolutePath());
        if (classpath != null && !classpath.isEmpty()) {
            args.add("-classpath");
            args.add(classpath);
        }
        args.add("-d");
        args.add(outDir.getAbsolutePath());
        for (File s : srcFiles) args.add(s.getAbsolutePath());
        boolean ok = ecj.compile(args.toArray(new String[0]));
        outWriter.flush();
        errWriter.flush();
        if (!ok) {
            String errStr = utf8Stream(ecjErr);
            String outStr = utf8Stream(ecjOut);
            if (!errStr.trim().isEmpty()) {
                return errStr;
            }
            if (!outStr.trim().isEmpty()) {
                return outStr;
            }
            return "ECJ: compilation failed (" + ecj.globalErrorsCount
                    + " error(s)); diagnostic streams were empty.";
        }
        return null;
    }

    private static String utf8Stream(ByteArrayOutputStream baos) {
        try {
            return baos.toString("UTF-8");
        } catch (Exception e) {
            return baos.toString();
        }
    }

    private static String compileEcjMulti(File androidJar, String classpath, File outDir, File[] srcFiles) {
        return compileEcj(androidJar, classpath, outDir, srcFiles);
    }

    private static String classpath(List<File> jars) {
        if (jars == null || jars.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jars.size(); i++) {
            if (i > 0) sb.append(File.pathSeparatorChar);
            sb.append(jars.get(i).getAbsolutePath());
        }
        return sb.toString();
    }

    private static void collectClasses(File dir, List<java.nio.file.Path> out) {
        File[] ch = dir.listFiles();
        if (ch == null) return;
        for (File f : ch) {
            if (f.isDirectory()) collectClasses(f, out);
            else if (f.getName().endsWith(".class")) out.add(f.toPath());
        }
    }

    private static void runD8Dex(File androidJar, File dexDir, File classFile) throws Exception {
        for (File f : dexDir.listFiles()) {
            if (f.getName().endsWith(".dex")) f.delete();
        }
        D8.run(D8Command.builder()
                .addProgramFiles(classFile.toPath())
                .addLibraryFiles(androidJar.toPath())
                .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                .setDisableDesugaring(true)
                .build());
    }

    private static void runDexMain(Context context, File cacheDir, File dexDir, String className,
                                 Callback callback) {
        try {
            File dexFile = new File(dexDir, "classes.dex");
            DexClassLoader cl = new DexClassLoader(
                    dexFile.getAbsolutePath(),
                    cacheDir.getAbsolutePath(),
                    null,
                    context.getClassLoader()
            );
            Class<?> cls = cl.loadClass(className);
            Method main = cls.getMethod("main", String[].class);
            ByteArrayOutputStream execOut = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(execOut);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            System.setOut(ps);
            System.setErr(ps);
            try {
                main.invoke(null, new Object[]{new String[]{}});
                ps.flush();
                postResult(callback, execOut.toString("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace(ps);
                ps.flush();
                postResult(callback, "Execution Exception:\n" + execOut.toString("UTF-8"));
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        } catch (Exception e) {
            postResult(callback, "System Error: " + e.getMessage());
        }
    }

    private static String extractClassName(String source) {
        Matcher m = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(source);
        if (m.find()) return m.group(1);
        m = Pattern.compile("(?m)\\bclass\\s+(\\w+)").matcher(source);
        return m.find() ? m.group(1) : "Main";
    }

    private static void postProgress(Callback cb, String msg) {
        if (cb == null) return;
        new Handler(Looper.getMainLooper()).post(() -> cb.onProgress(msg));
    }

    private static void postResult(Callback cb, String r) {
        if (cb == null) return;
        new Handler(Looper.getMainLooper()).post(() -> cb.onResult(r));
    }

    private static List<ProblemItem> buildProblemsList(File projectRoot, String ecjErr,
                                                       File remapSourceFile) {
        List<ProblemItem> problems = new ArrayList<>(EcjProblemParser.remapToLogicalFile(
                EcjProblemParser.parse(ecjErr, projectRoot), remapSourceFile));
        if (projectRoot != null) {
            List<File> sources = ProjectScanner.listJavaSources(projectRoot);
            problems.addAll(StaticAnalyzer.analyze(projectRoot, sources));
        }
        return problems;
    }

    /** Помилка компіляції: спочатку Problems, потім консоль — в одному UI-тику (без гонок). */
    private static void postCompileFailure(Callback cb, Context ctx, File projectRoot, String ecjErr,
                                           File remapSourceFile, String fullResultText) {
        if (cb == null) return;
        final List<ProblemItem> toPost = buildProblemsList(projectRoot, ecjErr, remapSourceFile);
        new Handler(Looper.getMainLooper()).post(() -> {
            cb.onProblems(toPost);
            cb.onResult(fullResultText);
        });
    }

    private static void postProblems(Callback cb, Context ctx, File projectRoot, String ecjErr,
                                     File remapSourceFile) {
        if (cb == null) return;
        final List<ProblemItem> toPost = buildProblemsList(projectRoot, ecjErr, remapSourceFile);
        new Handler(Looper.getMainLooper()).post(() -> cb.onProblems(toPost));
    }

    // ── Bytecode viewer (ECJ → .class → ASM Textifier) ────────────────────

    public static final class BytecodeCompileResult {
        public final File classFile;
        public final String errorMessage;

        public BytecodeCompileResult(File classFile, String errorMessage) {
            this.classFile = classFile;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Compiles the active source to a temp dir and locates the resulting {@code .class} for disassembly.
     */
    public static BytecodeCompileResult compileForBytecodeView(Context context, File javaFile,
            String sourceText, File projectRoot) {
        try {
            File outDir = new File(context.getCacheDir(), "bytecode_view");
            deleteRecursive(outDir);
            outDir.mkdirs();
            File srcFile = new File(outDir, javaFile.getName());
            writeUtf8(srcFile, sourceText);
            File androidJar = ensureAndroidJar(context, new File(context.getCacheDir(), "compile_cache"));
            String cp = "";
            if (projectRoot != null && new File(projectRoot, "pom.xml").exists()) {
                PomModel pom = PomParser.parse(MavenPaths.pomFile(projectRoot));
                List<File> deps = MavenDependencyResolver.resolve(projectRoot, pom, null);
                cp = classpath(deps);
                File tc = MavenPaths.targetClassesDir(projectRoot);
                if (tc.exists()) {
                    if (!cp.isEmpty()) cp += File.pathSeparator;
                    cp += tc.getAbsolutePath();
                }
            }
            String ecjErr = compileEcj(androidJar, cp, outDir, srcFile);
            if (ecjErr != null) {
                return new BytecodeCompileResult(null, ecjErr.trim());
            }
            File classFile = findCompiledClass(outDir, sourceText);
            if (classFile == null || !classFile.exists()) {
                return new BytecodeCompileResult(null, "No .class output (check package and class name).");
            }
            return new BytecodeCompileResult(classFile, null);
        } catch (Exception e) {
            String msg = e.getMessage();
            return new BytecodeCompileResult(null, msg != null ? msg : "compile error");
        }
    }

    private static void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] ch = f.listFiles();
            if (ch != null) {
                for (File c : ch) {
                    deleteRecursive(c);
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }

    private static File findCompiledClass(File outDir, String sourceText) {
        String simple = extractClassName(sourceText);
        String pkg = extractPackageName(sourceText);
        if (pkg != null && !pkg.isEmpty()) {
            File f = new File(outDir, pkg.replace('.', File.separatorChar) + "/" + simple + ".class");
            if (f.exists()) {
                return f;
            }
        }
        File f2 = new File(outDir, simple + ".class");
        if (f2.exists()) {
            return f2;
        }
        return findFileNamedRecursive(outDir, simple + ".class");
    }

    private static String extractPackageName(String source) {
        Matcher m = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;").matcher(source);
        return m.find() ? m.group(1) : "";
    }

    private static File findFileNamedRecursive(File dir, String fileName) {
        File[] list = dir.listFiles();
        if (list == null) {
            return null;
        }
        for (File f : list) {
            if (f.isDirectory()) {
                File r = findFileNamedRecursive(f, fileName);
                if (r != null) {
                    return r;
                }
            } else if (fileName.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }
}
