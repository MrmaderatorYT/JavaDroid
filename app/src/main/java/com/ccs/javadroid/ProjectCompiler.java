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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
            String ecjErr = compileEcj(androidJar, null, cacheDir, javaTarget(context), srcFile);
            if (ecjErr == null) {
                return new ArrayList<>();
            }
            return EcjProblemParser.remapToLogicalFile(
                    EcjProblemParser.parse(ecjErr, null), logicalFile);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String javaTarget(Context ctx) {
        try {
            return new AppPreferences(ctx).getJavaTarget();
        } catch (Throwable t) {
            return AppPreferences.JAVA_8;
        }
    }

    public static void runSingleSource(Context context, String sourceCode, Callback callback) {
        runSingleSource(context, sourceCode, null, callback);
    }

    /**
     * @param logicalSourceFile файл у проєкті (редактор); шляхи помилок ECJ з кешу підміняються на нього
     */
    public static void runSingleSource(Context context, String sourceCode, File logicalSourceFile,
                                       Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
        new Thread(() -> {
            try {
                boolean isKotlin = logicalSourceFile != null && logicalSourceFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".kt");
                String className = extractClassName(sourceCode);
                File cacheDir = new File(context.getCacheDir(), "compile_cache");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File androidJar = ensureAndroidJar(context, cacheDir);
                File dexDir = new File(cacheDir, "dex");
                if (!dexDir.exists()) {
                    dexDir.mkdirs();
                } else {
                    File[] oldFiles = dexDir.listFiles();
                    if (oldFiles != null) {
                        for (File f : oldFiles) f.delete();
                    }
                }

                if (isKotlin) {
                    postProgress(callback, "Compiling Kotlin source...");
                    // Clean stale .class/.kt files from previous runs so findAllClassFiles()
                    // only reports the output of this compilation. Keep android.jar and
                    // the cached kotlin-stdlib.jar.
                    cleanKotlinCache(cacheDir);

                    File srcFile = new File(cacheDir, className + ".kt");
                    writeUtf8(srcFile, sourceCode);

                    List<File> classFiles = compileKotlinViaCompilerApi(srcFile, cacheDir, androidJar, className, callback, context);
                    if (classFiles == null || classFiles.isEmpty()) {
                        return;
                    }

                    runD8Dex(androidJar, dexDir, classFiles);

                    // Determine the class name to run.
                    // Kotlin generates ClassNameKt.class for top-level functions,
                    // or ClassName.class for class/object declarations.
                    String runClassName = null;

                    // First: look for ClassNameKt in generated class files
                    for (File cf : classFiles) {
                        String name = cf.getName().replace(".class", "");
                        if (name.equals(className + "Kt")) {
                            String relative = cacheDir.toURI().relativize(cf.toURI()).getPath();
                            runClassName = relative.replace(".class", "").replace('/', '.');
                            break;
                        }
                    }
                    // Second: look for the exact class name
                    if (runClassName == null) {
                        for (File cf : classFiles) {
                            String name = cf.getName().replace(".class", "");
                            if (name.equals(className)) {
                                String relative = cacheDir.toURI().relativize(cf.toURI()).getPath();
                                runClassName = relative.replace(".class", "").replace('/', '.');
                                break;
                            }
                        }
                    }
                    // Third: check cacheDir directly for .class files
                    if (runClassName == null) {
                        File directKt = new File(cacheDir, className + "Kt.class");
                        if (directKt.exists()) {
                            runClassName = className + "Kt";
                        } else {
                            File directClass = new File(cacheDir, className + ".class");
                            if (directClass.exists()) {
                                runClassName = className;
                            }
                        }
                    }

                    if (runClassName == null) {
                        postResult(callback, "Kotlin Error: no runnable class found. " +
                                "Ensure the file has a `fun main()` or a `class` with a companion main.");
                        return;
                    }
                    runDexMain(context, null, dexDir, runClassName, callback);
                } else {
                    File srcFile = new File(cacheDir, className + ".java");
                    writeUtf8(srcFile, sourceCode);

                    String ecjErr = compileEcj(androidJar, null, cacheDir, javaTarget(context), srcFile);
                    if (ecjErr != null) {
                        postCompileFailure(callback, context, null, ecjErr, logicalSourceFile,
                                "Compilation Error:\n" + ecjErr);
                        return;
                    }
                    postProblems(callback, context, null, "", logicalSourceFile);

                    File classFile = findClassFile(cacheDir, className);
                    if (classFile == null) {
                        postResult(callback, "Error: " + className + ".class not found.");
                        return;
                    }

                    runD8Dex(androidJar, dexDir, classFile);
                    // Derive FQN from class file path (e.g. com/ccs/crr/App.class → com.ccs.crr.App)
                    String fqName = classFile.getAbsolutePath()
                            .substring(cacheDir.getAbsolutePath().length() + 1)
                            .replace(".class", "")
                            .replace('/', '.');
                    runDexMain(context, null, dexDir, fqName, callback);
                }
            } catch (ClassNotFoundException e) {
                postResult(callback, "Error: Kotlin compiler library not integrated. Please verify kotlin-compiler-embeddable in build.gradle.");
            } catch (Exception e) {
                postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
            }
        }).start();
    }

    /**
     * Compile and run a single source in debug mode: instruments bytecode
     * with debug hooks, loads DexClassLoader, and starts a debug session.
     */
    public static void debugSingleSource(Context context, String sourceCode, File logicalSourceFile,
                                          Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
        new Thread(() -> {
            try {
                boolean isKotlin = logicalSourceFile != null
                        && logicalSourceFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".kt");
                if (isKotlin) {
                    postResult(callback, "Debugging is not supported for Kotlin files yet.");
                    return;
                }

                String className = extractClassName(sourceCode);
                File cacheDir = new File(context.getCacheDir(), "debug_compile_cache");
                // Clean the directory to avoid stale class files
                if (cacheDir.exists()) {
                    File[] oldFiles = cacheDir.listFiles();
                    if (oldFiles != null) {
                        for (File f : oldFiles) {
                            if (f.getName().equals("android.jar")) continue; // keep android.jar
                            f.delete();
                        }
                    }
                }
                if (!cacheDir.exists()) cacheDir.mkdirs();


                File androidJar = ensureAndroidJar(context, cacheDir);
                File dexDir = new File(cacheDir, "debug_dex");
                if (!dexDir.exists()) {
                    dexDir.mkdirs();
                } else {
                    File[] oldFiles = dexDir.listFiles();
                    if (oldFiles != null) {
                        for (File f : oldFiles) f.delete();
                    }
                }

                File srcFile = new File(cacheDir, className + ".java");
                writeUtf8(srcFile, sourceCode);


                postProgress(callback, "Compiling...");
                String ecjErr = compileEcj(androidJar, null, cacheDir, javaTarget(context), srcFile);
                if (ecjErr != null) {
                    postCompileFailure(callback, context, null, ecjErr, logicalSourceFile,
                            "Compilation Error:\n" + ecjErr);
                    return;
                }
                postProblems(callback, context, null, "", logicalSourceFile);

                // Direct check for the class file
                File directClass = new File(cacheDir, className + ".class");

                // Log directory contents after compilation
                File[] afterFiles = cacheDir.listFiles();
                if (afterFiles != null) {
                    for (File f : afterFiles) {
                    }
                } else {
                    android.util.Log.e("JavaDroidDebug", "cacheDir.listFiles() returned null!");
                }

                File classFile = findClassFile(cacheDir, className);
                if (classFile == null) {
                    android.util.Log.e("JavaDroidDebug", "Class file not found: " + className + " in " + cacheDir);
                    // Build detailed error message with directory listing
                    StringBuilder sb = new StringBuilder("Error: " + className + ".class not found.\nDirectory contents of " + cacheDir + ":\n");
                    File[] allFiles = cacheDir.listFiles();
                    if (allFiles != null) {
                        for (File f : allFiles) {
                            sb.append("  ").append(f.isDirectory() ? "[DIR] " : "").append(f.getName()).append(" (").append(f.length()).append(" bytes)\n");
                        }
                    }
                    postResult(callback, sb.toString());
                    return;
                }


                // Compile native sources for debug mode
                File jniLibsDir = null;
                if (logicalSourceFile != null) {
                    File projectRoot = logicalSourceFile.getParentFile();
                    while (projectRoot != null && !new File(projectRoot, "pom.xml").exists()
                            && !new File(projectRoot, ".project").exists()) {
                        projectRoot = projectRoot.getParentFile();
                    }
                    if (projectRoot != null) {
                        jniLibsDir = compileNativeSources(context, projectRoot, callback);
                    }
                }

                // Instrument bytecode with debug hooks
                postProgress(callback, "Instrumenting for debugging...");
                java.util.Map<Integer, String> bpMap = com.ccs.javadroid.debug.DebuggerController.getInstance().getBreakpoints();
                java.util.Set<Integer> bpLines = new java.util.HashSet<>(bpMap.keySet());
                com.ccs.javadroid.debug.DebugInstrumenter.instrumentFile(classFile, bpLines);

                postProgress(callback, "Converting to DEX...");
                runD8Dex(androidJar, dexDir, classFile);

                postProgress(callback, "Starting debug session...");

                // Derive fully qualified class name from the class file path
                // e.g. cacheDir/com/ccs/crr/App.class → com.ccs.crr.App
                String fqClassName = classFile.getAbsolutePath()
                        .substring(cacheDir.getAbsolutePath().length() + 1)
                        .replace(".class", "")
                        .replace('/', '.');

                postResult(callback, "DEBUG_SESSION:" + fqClassName + ":" + cacheDir.getAbsolutePath()
                        + ":" + dexDir.getAbsolutePath()
                        + ":" + (jniLibsDir != null ? jniLibsDir.getAbsolutePath() : ""));
            } catch (Exception e) {
                postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
            }
        }).start();
    }

    /**
     * Виконує .class байти напряму: конвертує у DEX та запускає main().
     */
    public static void runClassBytes(Context context, String className, byte[] classBytes,
                                      Callback callback) {
        try {
            File cacheDir = new File(context.getCacheDir(), "bytecode_run_" + System.currentTimeMillis());
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File classFile = new File(cacheDir, className.replace('.', '/') + ".class");
            classFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(classFile);
            fos.write(classBytes);
            fos.close();

            File androidJar = ensureAndroidJar(context, cacheDir);
            File dexDir = new File(cacheDir, "dex");
            if (!dexDir.exists()) dexDir.mkdirs();

            postProgress(callback, "Converting .class → DEX…");
            runD8Dex(androidJar, dexDir, classFile);

            postProgress(callback, "Running " + className + "…");
            runDexMain(context, null, dexDir, className, callback);
        } catch (Exception e) {
            postResult(callback, "Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
    }

    /**
     * Load and run dex in debug mode with debug classpath.
     */
    public static void debugRunDex(Context context, String className, File dexDir,
                                   File debugCacheDir, File jniLibsDir, Callback callback) {
        try {
            File dexFile = new File(dexDir, "classes.dex");
            if (!dexFile.exists()) {
                postResult(callback, "Error: classes.dex not found in " + dexDir.getAbsolutePath());
                return;
            }

            // Android 14+ — копіюємо dex у read-only директорію
            File secureDexDir = new File(context.getDir("dex", Context.MODE_PRIVATE), "debug_" + System.currentTimeMillis());
            secureDexDir.mkdirs();
            File secureDex = new File(secureDexDir, "classes.dex");
            java.io.FileInputStream fis = new java.io.FileInputStream(dexFile);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(secureDex);
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) fos.write(buf, 0, n);
            fis.close();
            fos.close();

            File optDir = new File(secureDexDir, "opt");
            if (optDir.exists()) deleteRecursive(optDir);
            optDir.mkdirs();

            File nativeLibDir;
            if (jniLibsDir != null && jniLibsDir.exists()) {
                nativeLibDir = jniLibsDir;
            } else {
                nativeLibDir = findLatestJniLibsDir(context);
                if (nativeLibDir == null) {
                    nativeLibDir = new File(context.getCacheDir(), "jni_libs");
                }
            }
            if (!nativeLibDir.exists()) nativeLibDir.mkdirs();

            // Use a classpath that includes the debug bridge
            String debugCp = debugCacheDir.getAbsolutePath();
            DexClassLoader cl = new DexClassLoader(
                    secureDex.getAbsolutePath(),
                    optDir.getAbsolutePath(),
                    nativeLibDir.getAbsolutePath(),
                    context.getClassLoader()
            );

            Class<?> cls = cl.loadClass(className);
            Method main = cls.getMethod("main", String[].class);

            final boolean verbose = new AppPreferences(context).isVerboseLoggingEnabled();
            ByteArrayOutputStream execOut = new ByteArrayOutputStream();
            java.io.OutputStream interceptor = new java.io.OutputStream() {
                private StringBuilder line = new StringBuilder();
                @Override
                public void write(int b) throws java.io.IOException {
                    execOut.write(b);
                    if (b == '\n') {
                        line.setLength(0);
                    } else if (b != '\r') {
                        line.append((char) b);
                    }
                }
                @Override
                public void write(byte[] b, int off, int len) throws java.io.IOException {
                    execOut.write(b, off, len);
                    for (int i = off; i < off + len; i++) {
                        if (b[i] == '\n') {
                            line.setLength(0);
                        } else if (b[i] != '\r') {
                            line.append((char) b[i]);
                        }
                    }
                }
                @Override
                public void flush() throws java.io.IOException {
                    execOut.flush();
                    if (line.length() > 0) {
                        line.setLength(0);
                    }
                }
            };
            PrintStream ps = new PrintStream(interceptor, true);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            System.setOut(ps);
            System.setErr(ps);
            try {
                main.invoke(null, new Object[]{new String[]{}});
                ps.flush();
                postResult(callback, execOut.toString("UTF-8"));
            } catch (Throwable e) {
                Log.e("JavaDroidDebug", "Debug execution exception", e);
                e.printStackTrace(ps);
                ps.flush();
                postResult(callback, "Execution Exception:\n" + execOut.toString("UTF-8"));
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        } catch (Throwable e) {
            postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
    }

    private static File findLatestJniLibsDir(Context context) {
        File[] cacheFiles = context.getCacheDir().listFiles();
        if (cacheFiles == null) return null;
        File newest = null;
        long newestTime = 0;
        for (File f : cacheFiles) {
            if (f.isDirectory() && f.getName().startsWith("jni_libs_")) {
                long mod = f.lastModified();
                if (mod >= newestTime) {
                    newestTime = mod;
                    newest = f;
                }
            }
        }
        return newest;
    }

    private static File compileNativeSources(Context context, File projectRoot, Callback callback) {
        File jniLibsDir = new File(context.getCacheDir(), "jni_libs_" + System.currentTimeMillis());
        if (!jniLibsDir.exists()) jniLibsDir.mkdirs();

        File srcMain = new File(projectRoot, "src/main");
        File cppDir = new File(srcMain, "cpp");
        File jniDir = new File(srcMain, "jni");
        List<File> nativeCSources = new ArrayList<>();
        List<File> nativeCppSources = new ArrayList<>();
        if (cppDir.exists() && cppDir.isDirectory()) {
            File[] files = cppDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".c")) nativeCSources.add(f);
                    else if (f.getName().endsWith(".cpp") || f.getName().endsWith(".cxx")) nativeCppSources.add(f);
                }
            }
        }
        if (jniDir.exists() && jniDir.isDirectory()) {
            File[] files = jniDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".c")) nativeCSources.add(f);
                    else if (f.getName().endsWith(".cpp") || f.getName().endsWith(".cxx")) nativeCppSources.add(f);
                }
            }
        }

        if (nativeCSources.isEmpty() && nativeCppSources.isEmpty()) {
            return null;
        }

        postProgress(callback, "Compiling native sources...");

        // Compile C sources with TCC
        if (!nativeCSources.isEmpty()) {
            NativeCompiler.init(context);
            if (!NativeCompiler.isLoaded() || !NativeCompiler.isAvailable()) {
                postProgress(callback, context.getString(R.string.ndk_warning_c_not_available));
                return null;
            }
            String includePath = NativeCompiler.getIncludePath();
            for (File nativeSrc : nativeCSources) {
                String simpleName = nativeSrc.getName();
                String baseName = simpleName.substring(0, simpleName.lastIndexOf('.'));
                String libName = "lib" + baseName + ".so";
                File destSo = new File(jniLibsDir, libName);
                String sourceCode;
                try {
                    sourceCode = new String(java.nio.file.Files.readAllBytes(nativeSrc.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    postProgress(callback, context.getString(R.string.ndk_warning_cannot_read, simpleName, ex.getMessage()));
                    continue;
                }
                String error = NativeCompiler.compileToSharedLib(sourceCode, destSo.getAbsolutePath(), includePath);
                if (error != null) {
                    postProgress(callback, context.getString(R.string.ndk_warning_c_error, simpleName, error));
                    continue;
                }
                destSo.setExecutable(true, false);
                postProgress(callback, context.getString(R.string.ndk_compiled_tcc, simpleName, libName));
            }
        }

        // Compile C++ sources with NDK
        if (!nativeCppSources.isEmpty()) {
            if (!NdkManager.isNdkInstalled(context)) {
                postProgress(callback, context.getString(R.string.ndk_warning_cpp_requires_ndk));
                return null;
            }
            File clang = NdkManager.getClangPath(context);
            for (File nativeSrc : nativeCppSources) {
                String simpleName = nativeSrc.getName();
                String baseName = simpleName.substring(0, simpleName.lastIndexOf('.'));
                String libName = "lib" + baseName + ".so";
                File destSo = new File(jniLibsDir, libName);
                List<String> cmd = new ArrayList<>();
                cmd.add(clang.getAbsolutePath());
                cmd.add("-shared");
                cmd.add("-o");
                cmd.add(destSo.getAbsolutePath());
                cmd.add(nativeSrc.getAbsolutePath());
                try {
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    String output = new String(p.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    p.waitFor();
                    if (p.exitValue() != 0) {
                        postProgress(callback, context.getString(R.string.ndk_warning_cpp_error, simpleName, output));
                        continue;
                    }
                    destSo.setExecutable(true, false);
                    postProgress(callback, context.getString(R.string.ndk_compiled_clang, simpleName, libName));
                } catch (Exception e) {
                    postProgress(callback, context.getString(R.string.ndk_warning_cpp_failed, simpleName, e.getMessage()));
                }
            }
        }

        return jniLibsDir;
    }

    public static void mavenCompileAndRun(Context context, File projectRoot, PomModel pom,
                                          Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
        new Thread(() -> {
            try {
                // Clean up old maven_dex_* directories first to prevent disk bloat
                cleanupOldDexDirs(context);

                File androidJar = ensureAndroidJar(context,
                        new File(context.getCacheDir(), "compile_cache"));
                File outDir = MavenPaths.targetClassesDir(projectRoot);
                outDir.mkdirs();

                // Clean up old JNI lib directories
                File[] cacheFiles = context.getCacheDir().listFiles();
                if (cacheFiles != null) {
                    for (File f : cacheFiles) {
                        if (f.getName().startsWith("jni_libs_")) {
                            deleteRecursive(f);
                        }
                    }
                }
                
                // Dynamic JNI C/C++ Compilation
                File jniLibsDir = new File(context.getCacheDir(), "jni_libs_" + System.currentTimeMillis());
                if (!jniLibsDir.exists()) jniLibsDir.mkdirs();
                
                File srcMain = new File(projectRoot, "src/main");
                File cppDir = new File(srcMain, "cpp");
                File jniDir = new File(srcMain, "jni");
                List<File> nativeCSources = new ArrayList<>();
                List<File> nativeCppSources = new ArrayList<>();
                if (cppDir.exists() && cppDir.isDirectory()) {
                    File[] files = cppDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".c")) nativeCSources.add(f);
                            else if (f.getName().endsWith(".cpp") || f.getName().endsWith(".cxx")) nativeCppSources.add(f);
                        }
                    }
                }
                if (jniDir.exists() && jniDir.isDirectory()) {
                    File[] files = jniDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".c")) nativeCSources.add(f);
                            else if (f.getName().endsWith(".cpp") || f.getName().endsWith(".cxx")) nativeCppSources.add(f);
                        }
                    }
                }

                if (!nativeCSources.isEmpty() || !nativeCppSources.isEmpty()) {
                    postProgress(callback, "Compiling native sources...");
                    List<File> javaSources = ProjectScanner.listJavaSources(projectRoot);
                    
                    // 1. Compile C sources with built-in TCC
                    if (!nativeCSources.isEmpty()) {
                        NativeCompiler.init(context);
                        if (!NativeCompiler.isLoaded() || !NativeCompiler.isAvailable()) {
                            postResult(callback, context.getString(R.string.ndk_error_c_not_available));
                            return;
                        }
                        String includePath = NativeCompiler.getIncludePath();
                        for (File nativeSrc : nativeCSources) {
                            String simpleName = nativeSrc.getName();
                            String baseName = simpleName.substring(0, simpleName.lastIndexOf('.'));
                            String libName = "lib" + baseName + ".so";
                            File destSo = new File(jniLibsDir, libName);
                            
                            String sourceCode;
                            try {
                                sourceCode = new String(java.nio.file.Files.readAllBytes(nativeSrc.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                            } catch (IOException ex) {
                                postResult(callback, context.getString(R.string.ndk_error_cannot_read, simpleName, ex.getMessage()));
                                return;
                            }
                            
                            String error = NativeCompiler.compileToSharedLib(sourceCode, destSo.getAbsolutePath(), includePath);
                            if (error != null) {
                                postResult(callback, context.getString(R.string.ndk_error_c_compile, simpleName, error));
                                return;
                            }
                            destSo.setExecutable(true, false);
                            
                            File userLibsDir = new File(projectRoot, "build/jni_libs");
                            userLibsDir.mkdirs();
                            File userSo = new File(userLibsDir, libName);
                            try {
                                java.nio.file.Files.copy(destSo.toPath(), userSo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } catch (Exception e) {}
                            
                            postProgress(callback, "✅ Compiled (TCC) " + simpleName + " → " + libName + " (at build/jni_libs)");
                        }
                    }
                    
                    // 2. Compile C++ sources with NDK
                    if (!nativeCppSources.isEmpty()) {
                        if (!NdkManager.isNdkInstalled(context)) {
                            postResult(callback, context.getString(R.string.ndk_error_cpp_ndk_required));
                            return;
                        }
                        
                        File clang = NdkManager.getClangPath(context);
                        File sysroot = new File(NdkManager.getNdkDir(context), "toolchains/llvm/prebuilt/linux-aarch64/sysroot");
                        
                        for (File nativeSrc : nativeCppSources) {
                            String simpleName = nativeSrc.getName();
                            String baseName = simpleName.substring(0, simpleName.lastIndexOf('.'));
                            String libName = "lib" + baseName + ".so";
                            File destSo = new File(jniLibsDir, libName);
                            
                            List<String> cmd = new ArrayList<>();
                            cmd.add(clang.getAbsolutePath());
                            cmd.add("-shared");
                            cmd.add("-fPIC");
                            cmd.add("--sysroot=" + sysroot.getAbsolutePath());
                            cmd.add("-target");
                            cmd.add("aarch64-linux-android26"); // Match minSdkVersion
                            cmd.add("-o");
                            cmd.add(destSo.getAbsolutePath());
                            cmd.add(nativeSrc.getAbsolutePath());
                            
                            try {
                                ProcessBuilder pb = new ProcessBuilder(cmd);
                                pb.redirectErrorStream(true);
                                // Set library path so clang can find libc++_shared.so and others
                                File libDir = new File(NdkManager.getNdkDir(context), "toolchains/llvm/prebuilt/linux-aarch64/lib");
                                pb.environment().put("LD_LIBRARY_PATH", libDir.getAbsolutePath());
                                
                                Process p = pb.start();
                                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                                InputStream is = p.getInputStream();
                                byte[] buffer = new byte[1024];
                                int readBytes;
                                while ((readBytes = is.read(buffer)) != -1) {
                                    bos.write(buffer, 0, readBytes);
                                }
                                int code = p.waitFor();
                                if (code != 0) {
                                    postResult(callback, context.getString(R.string.ndk_error_cpp_compile, simpleName, bos.toString("UTF-8")));
                                    return;
                                } else {
                                    destSo.setExecutable(true, false);
                                    
                                    File userLibsDir = new File(projectRoot, "build/jni_libs");
                                    userLibsDir.mkdirs();
                                    File userSo = new File(userLibsDir, libName);
                                    try {
                                        java.nio.file.Files.copy(destSo.toPath(), userSo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                    } catch (Exception e) {}
                                    
                                    postProgress(callback, "✅ Compiled (Clang++) " + simpleName + " → " + libName + " (at build/jni_libs)");
                                }
                            } catch (Exception ex) {
                                postResult(callback, context.getString(R.string.ndk_error_cpp_execute, simpleName, ex.getMessage()));
                                return;
                            }
                        }
                    }
                }

                List<File> depJars = MavenDependencyResolver.resolve(projectRoot, pom,
                        msg -> postProgress(callback, msg));

                String cp = classpath(depJars);
                List<File> sources = ProjectScanner.listJavaSources(projectRoot);
                if (sources.isEmpty()) {
                    postResult(callback, "\u041D\u0435\u043C\u0430\u0454 .java \u0444\u0430\u0439\u043B\u0456\u0432 \u0443 src/main/java");
                    return;
                }

                List<File> srcArgs = new ArrayList<>(sources);
                File[] ecjFiles = srcArgs.toArray(new File[0]);

                String ecjErr = compileEcjMulti(androidJar, cp, outDir, javaTarget(context), ecjFiles);
                if (ecjErr != null) {
                    postCompileFailure(callback, context, projectRoot, ecjErr, null,
                            "Compilation Error:\n" + ecjErr);
                    return;
                }
                postProblems(callback, context, projectRoot, "", null);

                List<java.nio.file.Path> classes = new ArrayList<>();
                collectClasses(outDir, classes);
                if (classes.isEmpty()) {
                    postResult(callback, "\u041D\u0435\u043C\u0430\u0454 .class \u043F\u0456\u0441\u043B\u044F \u043A\u043E\u043C\u043F\u0456\u043B\u044F\u0446\u0456\u0457");
                    return;
                }

                File dexDir = new File(context.getCacheDir(), "maven_dex_" + System.currentTimeMillis());
                dexDir.mkdirs();
                
                D8Command.Builder b = D8Command.builder()
                        .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                        .setMinApiLevel(android.os.Build.VERSION.SDK_INT);
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
                runDexMain(context, jniLibsDir, dexDir, mainClass, callback);
            } catch (Exception e) {
                postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
            } finally {
                // Keep cache clean but avoid deleting during execution
            }
        }).start();
    }

    public static void mavenPackage(Context context, File projectRoot, PomModel pom, Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
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
                                        Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
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

                String ecjErr = compileEcjMulti(androidJar, cp, outDir, javaTarget(context),
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

        // process-resources: copy src/main/resources → target/classes (before compile)
        MavenLifecycle.processResources(projectRoot, msg -> postProgress(callback, msg));

        List<File> depJars = MavenDependencyResolver.resolve(projectRoot, pom,
                msg -> postProgress(callback, msg));
        String cp = classpath(depJars);
        List<File> sources = ProjectScanner.listJavaSources(projectRoot);
        if (sources.isEmpty()) throw new IllegalStateException("no sources");

        String ecjErr = compileEcjMulti(androidJar, cp, outDir, javaTarget(context),
                sources.toArray(new File[0]));
        if (ecjErr != null) throw new IllegalStateException(ecjErr);
    }

    // ─── Maven Clean ────────────────────────────────────────────────────────

    public static void mavenClean(Context context, File projectRoot, Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
        new Thread(() -> {
            try {
                MavenLifecycle.clean(projectRoot, msg -> postProgress(callback, msg));
                postResult(callback, "clean: BUILD SUCCESS\ntarget/ deleted.");
            } catch (Exception e) {
                postResult(callback, "clean failed: " + e.getMessage());
            }
        }).start();
    }

    // ─── Maven Install ──────────────────────────────────────────────────────

    public static void mavenInstall(Context context, File projectRoot, PomModel pom, Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
        new Thread(() -> {
            try {
                // Compile → Package → Install
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

                // Install JAR into local repo
                File installed = MavenLifecycle.install(projectRoot, pom,
                        msg -> postProgress(callback, msg));
                if (installed != null) {
                    postResult(callback, "install: BUILD SUCCESS\nJAR installed to:\n"
                            + installed.getAbsolutePath());
                } else {
                    postResult(callback, "install: BUILD FAILURE\nCould not install JAR.");
                }
            } catch (Exception e) {
                postResult(callback, "install failed: " + e.getMessage());
            }
        }).start();
    }

    // ─── Maven Test Run (on-device Surefire) ───────────────────────────────

    public static void mavenTestRun(Context context, File projectRoot, PomModel pom, Callback rawCallback) {
        final Callback callback = wrapCallback(context, rawCallback);
        new Thread(() -> {
            try {
                File androidJar = ensureAndroidJar(context,
                        new File(context.getCacheDir(), "compile_cache"));

                // 1. Compile main sources if not already compiled
                File classesMain = MavenPaths.targetClassesDir(projectRoot);
                if (!classesMain.exists() || isEmpty(classesMain)) {
                    mavenCompileOnly(context, projectRoot, pom, callback);
                }

                // 2. Compile test sources
                File outDir = MavenPaths.targetTestClassesDir(projectRoot);
                outDir.mkdirs();

                // Copy test resources
                MavenLifecycle.processTestResources(projectRoot,
                        msg -> postProgress(callback, msg));

                List<File> deps = new ArrayList<>();
                deps.addAll(MavenDependencyResolver.resolve(projectRoot, pom,
                        msg -> postProgress(callback, msg)));
                deps.addAll(MavenDependencyResolver.resolveTestScoped(projectRoot, pom,
                        msg -> postProgress(callback, msg)));

                String cp = classpath(deps);
                if (!classesMain.exists()) classesMain.mkdirs();
                if (!cp.isEmpty()) cp += File.pathSeparator;
                cp += classesMain.getAbsolutePath();

                List<File> testSrc = ProjectScanner.listTestSources(projectRoot);
                if (testSrc.isEmpty()) {
                    postResult(callback, "No tests found in src/test/java");
                    return;
                }

                postProgress(callback, "Compiling test sources...");
                String ecjErr = compileEcjMulti(androidJar, cp, outDir, javaTarget(context),
                        testSrc.toArray(new File[0]));
                if (ecjErr != null) {
                    postCompileFailure(callback, context, projectRoot, ecjErr, null,
                            "Test compilation failed:\n" + ecjErr);
                    return;
                }
                postProgress(callback, "Test sources compiled successfully.");

                // 3. Collect all .class files (main + test) and dex them with JUnit
                List<java.nio.file.Path> allClasses = new ArrayList<>();
                collectClasses(classesMain, allClasses);
                collectClasses(outDir, allClasses);

                if (allClasses.isEmpty()) {
                    postResult(callback, "No .class files found for dexing.");
                    return;
                }

                postProgress(callback, "Dexing test classes...");
                File dexDir = new File(context.getCacheDir(), "maven_test_dex_" + System.currentTimeMillis());
                dexDir.mkdirs();

                D8Command.Builder b = D8Command.builder()
                        .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                        .setMinApiLevel(android.os.Build.VERSION.SDK_INT);
                b.addLibraryFiles(androidJar.toPath());
                for (File j : deps) b.addLibraryFiles(j.toPath());
                for (java.nio.file.Path c : allClasses) b.addProgramFiles(c);
                D8.run(b.build());

                postProgress(callback, "Running tests...");
                MavenTestRunner.runTests(context, dexDir, outDir, classesMain, deps, androidJar,
                        new MavenTestRunner.Callback() {
                            @Override public void onProgress(String line) {
                                postProgress(callback, line);
                            }
                            @Override public void onResult(String output) {
                                postResult(callback, output);
                            }
                        });
            } catch (Exception e) {
                postResult(callback, "Test execution failed: " + e.getMessage() + "\n"
                        + Log.getStackTraceString(e));
            }
        }).start();
    }

    private static boolean isEmpty(File dir) {
        File[] files = dir.listFiles();
        return files == null || files.length == 0;
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

    private static String compileEcj(File androidJar, String classpath, File outDir,
                                     String javaTarget, File... srcFiles) {
        ByteArrayOutputStream ecjOut = new ByteArrayOutputStream();
        ByteArrayOutputStream ecjErr = new ByteArrayOutputStream();
        PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(ecjOut, StandardCharsets.UTF_8), true);
        PrintWriter errWriter = new PrintWriter(new OutputStreamWriter(ecjErr, StandardCharsets.UTF_8), true);
        Main ecj = new Main(outWriter, errWriter, false, null, null);
        List<String> args = new ArrayList<>();
        for (String flag : ecjVersionFlags(javaTarget)) args.add(flag);
        args.add("-proc:none");
        boolean isJava9OrAbove = javaTarget != null && !javaTarget.equals("1.8") && !javaTarget.startsWith("1.");
        if (isJava9OrAbove) {
            args.add("-classpath");
            if (classpath != null && !classpath.isEmpty()) {
                args.add(androidJar.getAbsolutePath() + File.pathSeparator + classpath);
            } else {
                args.add(androidJar.getAbsolutePath());
            }
        } else {
            args.add("-bootclasspath");
            args.add(androidJar.getAbsolutePath());
            if (classpath != null && !classpath.isEmpty()) {
                args.add("-classpath");
                args.add(classpath);
            }
        }
        args.add("-d");
        args.add(outDir.getAbsolutePath());
        for (File s : srcFiles) args.add(s.getAbsolutePath());
        
        // Filter out non-jar files from classpath to suppress ZipException warnings
        String filteredCp = filterClasspath(classpath);
        if (filteredCp != null && !filteredCp.equals(classpath)) {
            // Rebuild args with filtered classpath
            args.clear();
            for (String flag : ecjVersionFlags(javaTarget)) args.add(flag);
            args.add("-proc:none");
            if (isJava9OrAbove) {
                args.add("-classpath");
                args.add(androidJar.getAbsolutePath() + File.pathSeparator + filteredCp);
            } else {
                args.add("-bootclasspath");
                args.add(androidJar.getAbsolutePath());
                args.add("-classpath");
                args.add(filteredCp);
            }
            args.add("-d");
            args.add(outDir.getAbsolutePath());
            for (File s : srcFiles) args.add(s.getAbsolutePath());
        }
        
        PrintStream oldErr = System.err;
        // Silence ECJ's internal System.err prints (like ZipException for non-jar classpath entries)
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        boolean ok;
        try {
            ok = ecj.compile(args.toArray(new String[0]));
        } finally {
            System.setErr(oldErr);
        }
        
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

    private static String filterClasspath(String classpath) {
        if (classpath == null || classpath.isEmpty()) return classpath;
        StringBuilder filtered = new StringBuilder();
        for (String entry : classpath.split(File.pathSeparator)) {
            File f = new File(entry);
            if (f.isDirectory() || entry.endsWith(".jar") || entry.endsWith(".zip")) {
                if (filtered.length() > 0) filtered.append(File.pathSeparator);
                filtered.append(entry);
            }
        }
        return filtered.toString();
    }

    private static String compileEcjMulti(File androidJar, String classpath, File outDir,
                                          String javaTarget, File[] srcFiles) {
        return compileEcj(androidJar, classpath, outDir, javaTarget, srcFiles);
    }

    /** Повертає ECJ опції для версії Java: "-source X -target X". */
    private static String[] ecjVersionFlags(String javaTarget) {
        if (javaTarget == null || javaTarget.isEmpty()) javaTarget = "1.8";
        return new String[]{"-source", javaTarget, "-target", javaTarget};
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
        runD8Dex(androidJar, dexDir, java.util.Collections.singletonList(classFile));
    }

    private static void runD8Dex(File androidJar, File dexDir, List<File> classFiles) throws Exception {
        File[] old = dexDir.listFiles();
        if (old != null) {
            for (File f : old) f.delete();
        }
        D8Command.Builder builder = D8Command.builder()
                .addLibraryFiles(androidJar.toPath())
                .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                .setMinApiLevel(android.os.Build.VERSION.SDK_INT);
        for (File f : classFiles) {
            builder.addProgramFiles(f.toPath());
        }
        D8.run(builder.build());
    }

    private static void runDexMain(Context context, File jniLibsDir, File dexDir, String className,
                                  Callback callback) {
        try {
            File dexFile = new File(dexDir, "classes.dex");
            if (!dexFile.exists()) {
                postResult(callback, "Error: classes.dex not found in " + dexDir.getAbsolutePath());
                return;
            }

            // Android 14+ (API 34+) не дозволяє завантажувати writable dex-файли.
            // Копіюємо у read-only директорію code_cache.
            File secureDexDir = new File(context.getDir("dex", Context.MODE_PRIVATE), "run_" + System.currentTimeMillis());
            secureDexDir.mkdirs();
            File secureDex = new File(secureDexDir, "classes.dex");
            java.io.FileInputStream fis = new java.io.FileInputStream(dexFile);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(secureDex);
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) fos.write(buf, 0, n);
            fis.close();
            fos.close();

            // Expose the cache directory containing dynamically compiled JNI .so libraries to DexClassLoader
            File nativeLibDir = jniLibsDir != null ? jniLibsDir : new File(context.getCacheDir(), "jni_libs");
            if (!nativeLibDir.exists()) nativeLibDir.mkdirs();
            
            // Use a unique optimized-dex output directory per run to prevent
            // "Dex checksum does not match" errors. Android's DexClassLoader caches
            // optimized dex files keyed by path; reusing a shared directory causes
            // checksum conflicts when the source dex changes between runs.
            File optDir = new File(secureDexDir, "opt");
            if (optDir.exists()) deleteRecursive(optDir);
            optDir.mkdirs();

            DexClassLoader cl = new DexClassLoader(
                    secureDex.getAbsolutePath(),
                    optDir.getAbsolutePath(),
                    nativeLibDir.getAbsolutePath(),
                    context.getClassLoader()
            );
            Class<?> cls = cl.loadClass(className);
            Method main = cls.getMethod("main", String[].class);
            final boolean verbose = new AppPreferences(context).isVerboseLoggingEnabled();
            ByteArrayOutputStream execOut = new ByteArrayOutputStream();
            java.io.OutputStream interceptor = new java.io.OutputStream() {
                private StringBuilder line = new StringBuilder();
                @Override
                public void write(int b) throws java.io.IOException {
                    execOut.write(b);
                    if (verbose) {
                        if (b == '\n') {
                            Log.d("JavaDroidProgram", line.toString());
                            line.setLength(0);
                        } else if (b != '\r') {
                            line.append((char) b);
                        }
                    }
                }
                @Override
                public void write(byte[] b, int off, int len) throws java.io.IOException {
                    execOut.write(b, off, len);
                    if (verbose) {
                        for (int i = off; i < off + len; i++) {
                            if (b[i] == '\n') {
                                Log.d("JavaDroidProgram", line.toString());
                                line.setLength(0);
                            } else if (b[i] != '\r') {
                                line.append((char) b[i]);
                            }
                        }
                    }
                }
                @Override
                public void flush() throws java.io.IOException {
                    execOut.flush();
                    if (verbose && line.length() > 0) {
                        Log.d("JavaDroidProgram", line.toString());
                        line.setLength(0);
                    }
                }
            };
            PrintStream ps = new PrintStream(interceptor, true);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            System.setOut(ps);
            System.setErr(ps);
            try {
                main.invoke(null, new Object[]{new String[]{}});
                ps.flush();
                postResult(callback, execOut.toString("UTF-8"));
            } catch (Throwable e) {
                e.printStackTrace(ps);
                ps.flush();
                postResult(callback, "Execution Exception:\n" + execOut.toString("UTF-8"));
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        } catch (Throwable e) {
            postResult(callback, "System Error: " + e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
    }

    /** Remove stale maven_dex_* directories, keeping only the 2 most recent. */
    private static void cleanupOldDexDirs(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            File[] dirs = cacheDir.listFiles((f) -> f.isDirectory() && f.getName().startsWith("maven_dex_"));
            if (dirs == null || dirs.length <= 2) return;
            java.util.Arrays.sort(dirs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            // Keep the 2 newest, delete the rest
            for (int i = 2; i < dirs.length; i++) {
                deleteRecursive(dirs[i]);
            }
        } catch (Exception ignored) {}
    }

    private static String extractClassName(String source) {
        // Try "public class Foo" first (Java)
        Matcher m = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(source);
        if (m.find()) return m.group(1);
        // Try "class Foo" (Java/Kotlin)
        m = Pattern.compile("(?m)\\bclass\\s+(\\w+)").matcher(source);
        if (m.find()) return m.group(1);
        // Try "object Foo" (Kotlin singleton)
        m = Pattern.compile("(?m)\\bobject\\s+(\\w+)").matcher(source);
        if (m.find()) return m.group(1);
        // Fallback: derive from file-like first meaningful word, or "Main"
        return "Main";
    }

    private static File findClassFile(File dir, String className) {
        File direct = new File(dir, className + ".class");
        if (direct.exists()) return direct;
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    File found = findClassFile(child, className);
                    if (found != null) return found;
                }
            }
        }
        return null;
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

    private static Callback wrapCallback(Context context, Callback original) {
        if (original == null) return null;
        return new Callback() {
            @Override
            public void onProgress(String message) {
                if (context != null) {
                    try {
                        if (new AppPreferences(context).isVerboseLoggingEnabled()) {
                            Log.d("JavaDroidConsole", message);
                        }
                    } catch (Throwable ignored) {}
                }
                original.onProgress(message);
            }

            @Override
            public void onResult(String output) {
                if (context != null) {
                    try {
                        if (new AppPreferences(context).isVerboseLoggingEnabled()) {
                            Log.d("JavaDroidConsole", output);
                        }
                    } catch (Throwable ignored) {}
                }
                original.onResult(output);
            }

            @Override
            public void onProblems(List<ProblemItem> problems) {
                original.onProblems(problems);
            }
        };
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
            String ecjErr = compileEcj(androidJar, cp, outDir, javaTarget(context), srcFile);
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

    /**
     * Compile a single Kotlin source file using the Kotlin compiler embeddable library.
     * Uses KotlinCoreEnvironment.createForProduction + KotlinToJVMBytecodeCompiler.compileBunchOfSources,
     * bypassing K2JVMCompiler.exec() which needs PathUtil resource lookup (fails on Android DEX).
     *
     * @return list of generated .class files, or null on failure
     */
    private static List<File> compileKotlinViaCompilerApi(File srcFile, File cacheDir,
                                                           File androidJar, String className,
                                                           Callback callback, Context context) {
        try {
            postProgress(callback, "Preparing Kotlin compiler...");

            // 1. Ensure kotlin-stdlib.jar is on disk (compiler needs it for type resolution)
            File stdlibJar = ensureKotlinStdlib(cacheDir);
            if (stdlibJar == null) {
                postResult(callback, "Kotlin Error: kotlin-stdlib-1.9.22.jar not available.\n" +
                        "Please connect to internet on first use to download it.");
                return null;
            }

            // 2. Create plugin root with META-INF/extensions/compiler.xml
            //    This bypasses PathUtil.getResourcePathForClass() which fails on Android DEX.
            //    registerApplicationExtensionPointsAndExtensionsFrom() checks INTELLIJ_PLUGIN_ROOT first.
            File pluginRoot = ensureKotlinPluginRoot(cacheDir);

            // 3. Create child-first classloader with stdlib JAR for resource access.
            //    BuiltInsLoaderImpl uses this::class.java.classLoader.getResourceAsStream()
            //    which needs "kotlin/kotlin.kotlin_builtins" from kotlin-stdlib.jar.
            //    PathClassLoader with APK+stdlib in dexPath can serve resources from the JAR.
            File apkFile = new File(context.getApplicationInfo().sourceDir);
            String combinedDexPath = apkFile.getAbsolutePath() + ":" + stdlibJar.getAbsolutePath();
            ClassLoader appCl = context.getClassLoader();
            dalvik.system.PathClassLoader kotlinCl = new dalvik.system.PathClassLoader(
                    combinedDexPath, null, appCl) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    Class<?> c = findLoadedClass(name);
                    if (c != null) return c;
                    boolean isKotlin = name.startsWith("org.jetbrains.kotlin.") || name.startsWith("kotlin.");
                    if (isKotlin) {
                        try {
                            c = findClass(name);
                            if (resolve) resolveClass(c);
                            return c;
                        } catch (ClassNotFoundException ignored) {}
                    }
                    return super.loadClass(name, resolve);
                }
            };

            // 4. Build CompilerConfiguration
            Class<?> cfgClass = Class.forName("org.jetbrains.kotlin.config.CompilerConfiguration", true, kotlinCl);
            Object config = cfgClass.getDeclaredConstructor().newInstance();

            // Configuration key classes
            Class<?> cckClass = Class.forName("org.jetbrains.kotlin.config.CompilerConfigurationKey", true, kotlinCl);
            Class<?> cliKeysClass = Class.forName("org.jetbrains.kotlin.cli.common.CLIConfigurationKeys", true, kotlinCl);
            Class<?> jvmKeysClass = Class.forName("org.jetbrains.kotlin.config.JVMConfigurationKeys", true, kotlinCl);
            Class<?> commonKeysClass = Class.forName("org.jetbrains.kotlin.config.CommonConfigurationKeys", true, kotlinCl);

            Method putMethod = cfgClass.getMethod("put", cckClass, Object.class);
            Method addMethod = cfgClass.getMethod("add", cckClass, Object.class);

            // INTELLIJ_PLUGIN_ROOT — plugin root with compiler.xml (bypasses getResourcePathForClass)
            Field pluginRootField = cliKeysClass.getField("INTELLIJ_PLUGIN_ROOT");
            Object pluginRootKey = pluginRootField.get(null);
            // INTELLIJ_PLUGIN_ROOT is typed as String (the code does ?.let(::File) on it),
            // so pass the absolute path, not a File instance.
            putMethod.invoke(config, pluginRootKey, pluginRoot.getAbsolutePath());

            // NO_JDK — don't try to discover JDK, we provide classpath manually
            Field noJdkField = jvmKeysClass.getField("NO_JDK");
            putMethod.invoke(config, noJdkField.get(null), true);

            // OUTPUT_DIRECTORY — where .class files are written
            Field outputDirField = jvmKeysClass.getField("OUTPUT_DIRECTORY");
            putMethod.invoke(config, outputDirField.get(null), cacheDir);

            // MODULE_NAME
            Field moduleNameField = commonKeysClass.getField("MODULE_NAME");
            putMethod.invoke(config, moduleNameField.get(null), "main");

            // JVM_TARGET — JVM_1_8
            Class<?> jvmTargetEnumClass = Class.forName("org.jetbrains.kotlin.config.JvmTarget", true, kotlinCl);
            Field jvmTargetField = jvmKeysClass.getField("JVM_TARGET");
            Object jvm18 = Enum.valueOf((Class<Enum>) jvmTargetEnumClass, "JVM_1_8");
            putMethod.invoke(config, jvmTargetField.get(null), jvm18);

            // MESSAGE_COLLECTOR_KEY — capture compiler messages/errors
            final boolean[] hadError = {false};
            final StringBuilder compilerMessages = new StringBuilder();
            Class<?> mcInterface = Class.forName("org.jetbrains.kotlin.cli.common.messages.MessageCollector", true, kotlinCl);
            Class<?> severityClass = Class.forName("org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity", true, kotlinCl);
            Method isErrorMethod = severityClass.getMethod("isError");
            Object messageCollector = Proxy.newProxyInstance(
                    mcInterface.getClassLoader(),
                    new Class<?>[]{mcInterface},
                    (InvocationHandler) (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("report".equals(methodName) && args != null && args.length >= 2) {
                            Object severity = args[0];
                            String message = (String) args[1];
                            boolean isError = false;
                            try {
                                isError = (boolean) isErrorMethod.invoke(severity);
                            } catch (Exception ignored) {}
                            if (isError) hadError[0] = true;
                            String prefix = isError ? "ERROR: " : "INFO: ";
                            compilerMessages.append(prefix).append(message).append("\n");
                            Log.d("KotlinCompiler", prefix + message);
                            return null;
                        }
                        if ("clear".equals(methodName)) {
                            compilerMessages.setLength(0);
                            hadError[0] = false;
                            return null;
                        }
                        if ("hasErrors".equals(methodName)) {
                            return hadError[0];
                        }
                        // Object methods
                        if ("toString".equals(methodName)) return "JavaDroidMessageCollector";
                        if ("hashCode".equals(methodName)) return System.identityHashCode(proxy);
                        if ("equals".equals(methodName)) return proxy == args[0];
                        // Default for any other interface method
                        Class<?> rt = method.getReturnType();
                        if (rt == boolean.class) return hadError[0];
                        if (rt == int.class) return 0;
                        return null;
                    }
            );
            Field mcKeyField = cliKeysClass.getField("MESSAGE_COLLECTOR_KEY");
            putMethod.invoke(config, mcKeyField.get(null), messageCollector);

            // CONTENT_ROOTS — source roots + classpath roots
            Field contentRootsField = cliKeysClass.getField("CONTENT_ROOTS");

            // KotlinSourceRoot — source file directory
            Class<?> kotlinSourceRootClass = Class.forName("org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot", true, kotlinCl);
            Object sourceRoot = kotlinSourceRootClass.getDeclaredConstructor(
                    String.class, boolean.class, String.class)
                    .newInstance(srcFile.getAbsolutePath(), false, null);
            addMethod.invoke(config, contentRootsField.get(null), sourceRoot);

            // JvmClasspathRoot — classpath entries (android.jar + kotlin-stdlib)
            Class<?> jvmCpRootClass = Class.forName("org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot", true, kotlinCl);
            Object cpRootAndroid = jvmCpRootClass.getDeclaredConstructor(File.class)
                    .newInstance(androidJar);
            addMethod.invoke(config, contentRootsField.get(null), cpRootAndroid);
            Object cpRootStdlib = jvmCpRootClass.getDeclaredConstructor(File.class)
                    .newInstance(stdlibJar);
            addMethod.invoke(config, contentRootsField.get(null), cpRootStdlib);

            // 5. Create KotlinCoreEnvironment
            postProgress(callback, "Initializing Kotlin compiler environment...");
            Class<?> disposableClass = Class.forName("org.jetbrains.kotlin.com.intellij.openapi.Disposable", true, kotlinCl);
            Class<?> disposerClass = Class.forName("org.jetbrains.kotlin.com.intellij.openapi.util.Disposer", true, kotlinCl);
            Object disposable = disposerClass.getMethod("newDisposable").invoke(null);

            Class<?> envConfigClass = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles", true, kotlinCl);
            Object jvmConfigFiles = Enum.valueOf((Class<Enum>) envConfigClass, "JVM_CONFIG_FILES");

            Class<?> kotlinCoreEnvClass = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment", true, kotlinCl);
            Object environment = null;
            try {
                environment = kotlinCoreEnvClass.getMethod(
                        "createForProduction", disposableClass,
                        cfgClass, envConfigClass)
                        .invoke(null, disposable, config, jvmConfigFiles);
            } catch (Exception e) {
                Log.e("KotlinCompiler", "Failed to create KotlinCoreEnvironment", e);
                postResult(callback, "Kotlin Error: Failed to initialize compiler environment.\n" +
                        e.getClass().getSimpleName() + ": " + e.getMessage());
                return null;
            }

            // 5. Compile using KotlinToJVMBytecodeCompiler.compileBunchOfSources
            postProgress(callback, "Compiling Kotlin...");
            Class<?> compilerClass = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler", true, kotlinCl);
            Object compilerInstance = compilerClass.getField("INSTANCE").get(null);

            Method compileMethod = compilerClass.getMethod("compileBunchOfSources", kotlinCoreEnvClass);
            boolean success = (Boolean) compileMethod.invoke(compilerInstance, environment);

            // 6. Collect generated .class files
            List<File> classFiles = findAllClassFiles(cacheDir);

            if (!success && classFiles.isEmpty()) {
                String errDetail = compilerMessages.toString();
                if (errDetail.isEmpty()) {
                    errDetail = "compileBunchOfSources returned false with no output files.";
                }
                postResult(callback, "Kotlin Compilation Error:\n" + errDetail);
                return null;
            }

            // Log what was generated
            Log.d("KotlinCompiler", "Generated " + classFiles.size() + " class files:");
            for (File cf : classFiles) {
                Log.d("KotlinCompiler", "  " + cf.getAbsolutePath() + " (" + cf.length() + " bytes)");
            }

            if (hadError[0] && classFiles.isEmpty()) {
                postResult(callback, "Kotlin Compilation Error:\n" + compilerMessages);
                return null;
            }

            return classFiles.isEmpty() ? null : classFiles;

        } catch (ClassNotFoundException e) {
            Log.e("KotlinCompiler", "Kotlin compiler class not found", e);
            postResult(callback, "Kotlin Error: compiler library not integrated.\n" +
                    e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e("KotlinCompiler", "Method not found in compiler library", e);
            postResult(callback, "Kotlin Error: incompatible compiler API.\n" +
                    e.getMessage());
        } catch (Exception e) {
            Log.e("KotlinCompiler", "Kotlin compilation failed", e);
            postResult(callback, "Kotlin System Error:\n" + e.getClass().getSimpleName() +
                    ": " + e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
        return null;
    }

    /**
     * Ensure kotlin-stdlib-1.9.22.jar is available on disk.
     * Downloaded from Maven Central on first use and cached.
     */
    private static File ensureKotlinStdlib(File cacheDir) {
        File stdlibJar = new File(cacheDir, "kotlin-stdlib-1.9.22.jar");
        if (stdlibJar.exists() && stdlibJar.length() > 0) {
            return stdlibJar;
        }
        String url = "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.22/kotlin-stdlib-1.9.22.jar";
        try {
            Log.d("KotlinCompiler", "Downloading kotlin-stdlib from " + url);
            if (downloadFile(url, stdlibJar, 30000, 60000)) {
                Log.d("KotlinCompiler", "kotlin-stdlib downloaded: " + stdlibJar.length() + " bytes");
                return stdlibJar;
            }
        } catch (Exception e) {
            Log.e("KotlinCompiler", "Failed to download kotlin-stdlib", e);
        }
        // Fallback: try without stdlib (some simple programs may work)
        Log.w("KotlinCompiler", "kotlin-stdlib not available, compilation may fail for complex code");
        return null;
    }

    /**
     * Create a plugin root directory with the real META-INF/extensions/compiler.xml extracted
     * from the kotlin-compiler-embeddable classes (via the DEX classloader's getResourceAsStream).
     *
     * This is needed because KotlinCoreEnvironment calls PathUtil.getResourcePathForClass()
     * to locate compiler.xml, which fails on Android DEX (getResource() returns null for classes).
     * Setting CLIConfigurationKeys.INTELLIJ_PLUGIN_ROOT to this directory bypasses that lookup.
     *
     * The real compiler.xml (with all <extensionPoints>) is required: the compiler later calls
     * registerExtensionPoint() for each extension point defined in it, so an empty stub would
     * cause the compiler to fail when registering/looking up its own extension points.
     */
    private static File ensureKotlinPluginRoot(File cacheDir) {
        File pluginRoot = new File(cacheDir, "kotlin_plugin_root");
        File extensions = new File(pluginRoot, "META-INF/extensions");
        File compilerXml = new File(extensions, "compiler.xml");

        if (compilerXml.exists() && compilerXml.length() > 100) {
            return pluginRoot;
        }

        extensions.mkdirs();
        boolean ok = false;

        // 1. Try to extract the real compiler.xml from the embeddable compiler via the DEX classloader.
        //    getResourceAsStream() works on DEX/APK resources even though getResource() returns null.
        try (InputStream is = ProjectCompiler.class.getClassLoader()
                .getResourceAsStream("META-INF/extensions/compiler.xml")) {
            if (is != null) {
                try (FileOutputStream fos = new FileOutputStream(compilerXml)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                }
                if (compilerXml.length() > 100) {
                    ok = true;
                    Log.d("KotlinCompiler", "Extracted compiler.xml from classloader ("
                            + compilerXml.length() + " bytes)");
                }
            }
        } catch (Exception e) {
            Log.w("KotlinCompiler", "Could not extract compiler.xml via classloader", e);
        }

        // 2. Fallback: download the real compiler.xml from the Kotlin 1.9.22 source on GitHub.
        if (!ok) {
            String rawUrl = "https://raw.githubusercontent.com/JetBrains/kotlin/v1.9.22/compiler/cli/cli-common/resources/META-INF/extensions/compiler.xml";
            try {
                if (downloadToFile(rawUrl, compilerXml, 15000, 20000) && compilerXml.length() > 100) {
                    ok = true;
                    Log.d("KotlinCompiler", "Downloaded compiler.xml (" + compilerXml.length() + " bytes)");
                }
            } catch (Exception e) {
                Log.w("KotlinCompiler", "Could not download compiler.xml", e);
            }
        }

        // 3. Last-resort fallback: write a minimal valid stub (compiler may fail to register
        //    some extension points, but at least initialization proceeds).
        if (!ok) {
            try {
                writeUtf8(compilerXml, "<idea-plugin><id>org.jetbrains.kotlin</id></idea-plugin>");
                Log.w("KotlinCompiler", "Using minimal compiler.xml stub — some features may fail");
            } catch (Exception e) {
                Log.e("KotlinCompiler", "Failed to create plugin root", e);
            }
        }
        return pluginRoot;
    }

    /**
     * Download a URL directly to a file, returning success (does not throw on HTTP errors).
     */
    private static boolean downloadToFile(String urlStr, File dest, int connectTimeout, int readTimeout) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(urlStr).openConnection();
            c.setConnectTimeout(connectTimeout);
            c.setReadTimeout(readTimeout);
            c.setRequestMethod("GET");
            if (c.getResponseCode() != 200) return false;
            try (InputStream in = c.getInputStream();
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }
            return true;
        } catch (Exception e) {
            Log.w("KotlinCompiler", "Download failed: " + urlStr + " — " + e.getMessage());
            return false;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    /**
     * Download a file from URL to local destination.
     */
    private static boolean downloadFile(String urlStr, File dest, int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
        c.setConnectTimeout(connectTimeout);
        c.setReadTimeout(readTimeout);
        c.setRequestMethod("GET");
        try {
            int code = c.getResponseCode();
            if (code != 200) {
                c.disconnect();
                return false;
            }
            try (InputStream in = c.getInputStream();
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }
            return true;
        } finally {
            c.disconnect();
        }
    }

    /**
     * Recursively find all .class files in a directory.
     */
    private static List<File> findAllClassFiles(File dir) {
        List<File> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return result;
        for (File f : files) {
            if (f.isDirectory()) {
                result.addAll(findAllClassFiles(f));
            } else if (f.getName().endsWith(".class")) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Remove stale .class and .kt files from the Kotlin cache directory before a fresh
     * compilation, but preserve reusable artifacts: android.jar, kotlin-stdlib jar, the
     * plugin root, and the dex subdirectory.
     */
    private static void cleanKotlinCache(File cacheDir) {
        File[] files = cacheDir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String name = f.getName();
            if (name.equals("android.jar") || name.equals("dex")) continue;
            if (name.startsWith("kotlin-stdlib") && name.endsWith(".jar")) continue;
            if (name.equals("kotlin_plugin_root")) continue;
            if (name.endsWith(".class") || name.endsWith(".kt")) {
                if (!f.delete()) Log.w("KotlinCompiler", "Could not delete " + f);
                continue;
            }
            // Recurse into package directories (e.g. com/.../Foo.class)
            if (f.isDirectory()) {
                deleteRecursive(f);
            }
        }
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
