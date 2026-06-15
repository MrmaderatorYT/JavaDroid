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
                    File srcFile = new File(cacheDir, className + ".kt");
                    writeUtf8(srcFile, sourceCode);

                    // Compile Kotlin using K2JVMCompiler via reflection
                    ByteArrayOutputStream kotOut = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(kotOut, true, "UTF-8");
                    
                    Class<?> compilerClass = Class.forName("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler");
                    Object compilerInstance = compilerClass.getDeclaredConstructor().newInstance();
                    Method execMethod = compilerClass.getMethod("exec", PrintStream.class, String[].class);

                    // We need standard library path, but since we are compiling single source without stdlib jar sometimes,
                    // we can pass noStdlib=false or specify classpath including our own android.jar.
                    String[] args = new String[] {
                        srcFile.getAbsolutePath(),
                        "-d", cacheDir.getAbsolutePath(),
                        "-classpath", androidJar.getAbsolutePath(),
                        "-no-stdlib"
                    };

                    Object exitCodeObj = execMethod.invoke(compilerInstance, ps, args);
                    ps.flush();
                    String errLog = kotOut.toString("UTF-8");

                    int exitCode = 0;
                    if (exitCodeObj != null) {
                        if (exitCodeObj instanceof Enum) {
                            exitCode = ((Enum<?>) exitCodeObj).ordinal(); // 0 is OK usually
                        } else if (exitCodeObj instanceof Integer) {
                            exitCode = (Integer) exitCodeObj;
                        }
                    }

                    if (exitCode != 0 || !errLog.isEmpty()) {
                        // Some warnings can be in errLog even if exitCode is 0, check if file exists
                        File classFile = new File(cacheDir, className + "Kt.class");
                        if (!classFile.exists()) {
                            classFile = new File(cacheDir, className + ".class");
                        }
                        if (!classFile.exists()) {
                            postCompileFailure(callback, context, null, errLog, logicalSourceFile,
                                    "Kotlin Compilation Error:\n" + errLog);
                            return;
                        }
                    }

                    File classFile = new File(cacheDir, className + "Kt.class");
                    if (!classFile.exists()) {
                        classFile = new File(cacheDir, className + ".class");
                    }
                    if (!classFile.exists()) {
                        postResult(callback, "Error: Compiled Kotlin class file not found.");
                        return;
                    }

                    runD8Dex(androidJar, dexDir, classFile);
                    // Kotlin compiled file usually has "Kt" suffix for main methods (e.g. MainKt)
                    String runClassName = className;
                    if (new File(cacheDir, className + "Kt.class").exists()) {
                        runClassName = className + "Kt";
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

            File optDir = new File(dexDir, "opt");
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
                    dexFile.getAbsolutePath(),
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
                postProgress(callback, "Warning: built-in C compiler not available, skipping native compilation");
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
                    postProgress(callback, "Warning: Cannot read " + simpleName + ": " + ex.getMessage());
                    continue;
                }
                String error = NativeCompiler.compileToSharedLib(sourceCode, destSo.getAbsolutePath(), includePath);
                if (error != null) {
                    postProgress(callback, "Warning: C compilation error (" + simpleName + "): " + error);
                    continue;
                }
                destSo.setExecutable(true, false);
                postProgress(callback, "Compiled (TCC) " + simpleName + " → " + libName);
            }
        }

        // Compile C++ sources with NDK
        if (!nativeCppSources.isEmpty()) {
            if (!NdkManager.isNdkInstalled(context)) {
                postProgress(callback, "Warning: C++ sources require external NDK. Skipping.");
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
                        postProgress(callback, "Warning: C++ compilation error (" + simpleName + "): " + output);
                        continue;
                    }
                    destSo.setExecutable(true, false);
                    postProgress(callback, "Compiled (Clang++) " + simpleName + " → " + libName);
                } catch (Exception e) {
                    postProgress(callback, "Warning: C++ compilation failed (" + simpleName + "): " + e.getMessage());
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
                            postResult(callback, "\u274C Native Build Error:\nBuilt-in C compiler is not available on this device.");
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
                                postResult(callback, "❌ Cannot read " + simpleName + ": " + ex.getMessage());
                                return;
                            }
                            
                            String error = NativeCompiler.compileToSharedLib(sourceCode, destSo.getAbsolutePath(), includePath);
                            if (error != null) {
                                postResult(callback, "❌ C Compilation Error (" + simpleName + "):\n" + error);
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
                            postResult(callback, "\u274C C++ Build Error:\n"
                                    + "C++ sources (.cpp) require the external NDK.\n\n"
                                    + "Please go to Settings -> Install C++ NDK to download the toolchain (~130MB).\n"
                                    + "Or rewrite your JNI code in pure C (.c) to use the built-in fast compiler.");
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
                                    postResult(callback, "\u274C C++ Compilation Error (" + simpleName + "):\n" + bos.toString("UTF-8"));
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
                                postResult(callback, "\u274C C++ Build Error:\n"
                                        + "Failed to execute clang++ for " + simpleName + ": " + ex.getMessage());
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

        List<File> depJars = MavenDependencyResolver.resolve(projectRoot, pom,
                msg -> postProgress(callback, msg));
        String cp = classpath(depJars);
        List<File> sources = ProjectScanner.listJavaSources(projectRoot);
        if (sources.isEmpty()) throw new IllegalStateException("no sources");

        String ecjErr = compileEcjMulti(androidJar, cp, outDir, javaTarget(context),
                sources.toArray(new File[0]));
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
        File[] old = dexDir.listFiles();
        if (old != null) {
            for (File f : old) f.delete();
        }
        D8.run(D8Command.builder()
                .addProgramFiles(classFile.toPath())
                .addLibraryFiles(androidJar.toPath())
                .setOutput(dexDir.toPath(), OutputMode.DexIndexed)
                .setMinApiLevel(android.os.Build.VERSION.SDK_INT)
                .build());
    }

    private static void runDexMain(Context context, File jniLibsDir, File dexDir, String className,
                                 Callback callback) {
        try {
            File dexFile = new File(dexDir, "classes.dex");
            if (!dexFile.exists()) {
                postResult(callback, "Error: classes.dex not found in " + dexDir.getAbsolutePath());
                return;
            }

            // Expose the cache directory containing dynamically compiled JNI .so libraries to DexClassLoader
            File nativeLibDir = jniLibsDir != null ? jniLibsDir : new File(context.getCacheDir(), "jni_libs");
            if (!nativeLibDir.exists()) nativeLibDir.mkdirs();
            
            // Use a unique optimized-dex output directory per run to prevent
            // "Dex checksum does not match" errors. Android's DexClassLoader caches
            // optimized dex files keyed by path; reusing a shared directory causes
            // checksum conflicts when the source dex changes between runs.
            File optDir = new File(dexDir, "opt");
            if (optDir.exists()) deleteRecursive(optDir);
            optDir.mkdirs();

            DexClassLoader cl = new DexClassLoader(
                    dexFile.getAbsolutePath(),
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
        Matcher m = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(source);
        if (m.find()) return m.group(1);
        m = Pattern.compile("(?m)\\bclass\\s+(\\w+)").matcher(source);
        return m.find() ? m.group(1) : "Main";
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
