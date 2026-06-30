package com.ccs.javadroid.tools.compilers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Kotlin компіляція через embeddable compiler API.
 * Використовує KotlinCoreEnvironment + KotlinToJVMBytecodeCompiler.
 */
final class KotlinCompiler {

    private static final String TAG = "KotlinCompiler";

    private KotlinCompiler() {}

    static List<File> compile(File srcFile, File projectRoot, File cacheDir,
                              File androidJar, String className,
                              ProjectCompiler.Callback callback, Context context) {
        try {
            ProjectCompiler.postProgress(callback, "Preparing Kotlin compiler...");

            File stdlibJar = ensureKotlinStdlib(cacheDir);
            if (stdlibJar == null) {
                ProjectCompiler.postResult(callback, "Kotlin Error: kotlin-stdlib-1.9.22.jar not available.\n" +
                        "Please connect to internet on first use to download it.");
                return null;
            }

            File pluginRoot = ensureKotlinPluginRoot(cacheDir);

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

            Class<?> cfgClass = Class.forName("org.jetbrains.kotlin.config.CompilerConfiguration", true, kotlinCl);
            Object config = cfgClass.getDeclaredConstructor().newInstance();

            Class<?> cckClass = Class.forName("org.jetbrains.kotlin.config.CompilerConfigurationKey", true, kotlinCl);
            Class<?> cliKeysClass = Class.forName("org.jetbrains.kotlin.cli.common.CLIConfigurationKeys", true, kotlinCl);
            Class<?> jvmKeysClass = Class.forName("org.jetbrains.kotlin.config.JVMConfigurationKeys", true, kotlinCl);
            Class<?> commonKeysClass = Class.forName("org.jetbrains.kotlin.config.CommonConfigurationKeys", true, kotlinCl);

            Method putMethod = cfgClass.getMethod("put", cckClass, Object.class);
            Method addMethod = cfgClass.getMethod("add", cckClass, Object.class);

            Field pluginRootField = cliKeysClass.getField("INTELLIJ_PLUGIN_ROOT");
            Object pluginRootKey = pluginRootField.get(null);
            putMethod.invoke(config, pluginRootKey, pluginRoot.getAbsolutePath());

            Field noJdkField = jvmKeysClass.getField("NO_JDK");
            putMethod.invoke(config, noJdkField.get(null), true);

            Field outputDirField = jvmKeysClass.getField("OUTPUT_DIRECTORY");
            putMethod.invoke(config, outputDirField.get(null), cacheDir);

            Field moduleNameField = commonKeysClass.getField("MODULE_NAME");
            putMethod.invoke(config, moduleNameField.get(null), "main");

            Class<?> jvmTargetEnumClass = Class.forName("org.jetbrains.kotlin.config.JvmTarget", true, kotlinCl);
            Field jvmTargetField = jvmKeysClass.getField("JVM_TARGET");
            Object jvm18 = Enum.valueOf((Class<Enum>) jvmTargetEnumClass, "JVM_1_8");
            putMethod.invoke(config, jvmTargetField.get(null), jvm18);

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
                            try { isError = (boolean) isErrorMethod.invoke(severity); } catch (Exception ignored) {}
                            if (isError) hadError[0] = true;
                            String prefix = isError ? "ERROR: " : "INFO: ";
                            compilerMessages.append(prefix).append(message).append("\n");
                            Log.d(TAG, prefix + message);
                            return null;
                        }
                        if ("clear".equals(methodName)) { compilerMessages.setLength(0); hadError[0] = false; return null; }
                        if ("hasErrors".equals(methodName)) return hadError[0];
                        if ("toString".equals(methodName)) return "JavaDroidMessageCollector";
                        if ("hashCode".equals(methodName)) return System.identityHashCode(proxy);
                        if ("equals".equals(methodName)) return proxy == args[0];
                        Class<?> rt = method.getReturnType();
                        if (rt == boolean.class) return hadError[0];
                        if (rt == int.class) return 0;
                        return null;
                    }
            );
            Field mcKeyField = cliKeysClass.getField("MESSAGE_COLLECTOR_KEY");
            putMethod.invoke(config, mcKeyField.get(null), messageCollector);

            Field contentRootsField = cliKeysClass.getField("CONTENT_ROOTS");

            Class<?> kotlinSourceRootClass = Class.forName("org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot", true, kotlinCl);
            Object sourceRoot = kotlinSourceRootClass.getDeclaredConstructor(
                    String.class, boolean.class, String.class)
                    .newInstance(srcFile.getAbsolutePath(), false, null);
            addMethod.invoke(config, contentRootsField.get(null), sourceRoot);

            if (projectRoot != null && projectRoot.exists()) {
                List<File> allSources = new ArrayList<>();
                collectSources(projectRoot, allSources);
                for (File f : allSources) {
                    if (f.getAbsolutePath().equals(srcFile.getAbsolutePath())) continue;
                    Object extSourceRoot = kotlinSourceRootClass.getDeclaredConstructor(
                            String.class, boolean.class, String.class)
                            .newInstance(f.getAbsolutePath(), false, null);
                    addMethod.invoke(config, contentRootsField.get(null), extSourceRoot);
                }
            }

            Class<?> jvmCpRootClass = Class.forName("org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot", true, kotlinCl);
            Object cpRootAndroid = jvmCpRootClass.getDeclaredConstructor(File.class).newInstance(androidJar);
            addMethod.invoke(config, contentRootsField.get(null), cpRootAndroid);
            Object cpRootStdlib = jvmCpRootClass.getDeclaredConstructor(File.class).newInstance(stdlibJar);
            addMethod.invoke(config, contentRootsField.get(null), cpRootStdlib);

            ProjectCompiler.postProgress(callback, "Initializing Kotlin compiler environment...");
            Class<?> disposableClass = Class.forName("org.jetbrains.kotlin.com.intellij.openapi.Disposable", true, kotlinCl);
            Class<?> disposerClass = Class.forName("org.jetbrains.kotlin.com.intellij.openapi.util.Disposer", true, kotlinCl);
            Object disposable = disposerClass.getMethod("newDisposable").invoke(null);

            Class<?> envConfigClass = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles", true, kotlinCl);
            Object jvmConfigFiles = Enum.valueOf((Class<Enum>) envConfigClass, "JVM_CONFIG_FILES");

            Class<?> kotlinCoreEnvClass = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment", true, kotlinCl);
            Object environment = null;
            try {
                environment = kotlinCoreEnvClass.getMethod(
                        "createForProduction", disposableClass, cfgClass, envConfigClass)
                        .invoke(null, disposable, config, jvmConfigFiles);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create KotlinCoreEnvironment", e);
                ProjectCompiler.postResult(callback, "Kotlin Error: Failed to initialize compiler environment.\n" +
                        e.getClass().getSimpleName() + ": " + e.getMessage());
                return null;
            }

            ProjectCompiler.postProgress(callback, "Compiling Kotlin...");
            Class<?> compilerClass = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler", true, kotlinCl);
            Object compilerInstance = compilerClass.getField("INSTANCE").get(null);
            Method compileMethod = compilerClass.getMethod("compileBunchOfSources", kotlinCoreEnvClass);
            boolean success = (Boolean) compileMethod.invoke(compilerInstance, environment);

            List<File> classFiles = findAllClassFiles(cacheDir);

            if (!success && classFiles.isEmpty()) {
                String errDetail = compilerMessages.toString();
                if (errDetail.isEmpty()) errDetail = "compileBunchOfSources returned false with no output files.";
                ProjectCompiler.postResult(callback, "Kotlin Compilation Error:\n" + errDetail);
                return null;
            }

            Log.d(TAG, "Generated " + classFiles.size() + " class files");
            if (hadError[0] && classFiles.isEmpty()) {
                ProjectCompiler.postResult(callback, "Kotlin Compilation Error:\n" + compilerMessages);
                return null;
            }
            return classFiles.isEmpty() ? null : classFiles;

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Kotlin compiler class not found", e);
            ProjectCompiler.postResult(callback, "Kotlin Error: compiler library not integrated.\n" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Method not found in compiler library", e);
            ProjectCompiler.postResult(callback, "Kotlin Error: incompatible compiler API.\n" + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Kotlin compilation failed", e);
            ProjectCompiler.postResult(callback, "Kotlin System Error:\n" + e.getClass().getSimpleName() +
                    ": " + e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
        return null;
    }

    private static File ensureKotlinStdlib(File cacheDir) {
        File stdlibJar = new File(cacheDir, "kotlin-stdlib-1.9.22.jar");
        if (stdlibJar.exists() && stdlibJar.length() > 0) return stdlibJar;
        String url = "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.22/kotlin-stdlib-1.9.22.jar";
        try {
            Log.d(TAG, "Downloading kotlin-stdlib from " + url);
            if (ProjectCompiler.downloadFile(url, stdlibJar, 30000, 60000)) {
                Log.d(TAG, "kotlin-stdlib downloaded: " + stdlibJar.length() + " bytes");
                return stdlibJar;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to download kotlin-stdlib", e);
        }
        return null;
    }

    private static File ensureKotlinPluginRoot(File cacheDir) {
        File pluginRoot = new File(cacheDir, "kotlin_plugin_root");
        File extensions = new File(pluginRoot, "META-INF/extensions");
        File compilerXml = new File(extensions, "compiler.xml");

        if (compilerXml.exists() && compilerXml.length() > 100) return pluginRoot;

        extensions.mkdirs();

        try {
            ClassLoader cl = KotlinCompiler.class.getClassLoader();
            java.io.InputStream is = cl.getResourceAsStream("META-INF/extensions/compiler.xml");
            if (is != null) {
                java.io.FileOutputStream fos = new java.io.FileOutputStream(compilerXml);
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                fos.close();
                is.close();
                return pluginRoot;
            }
        } catch (Exception ignored) {}

        String stub = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<idea-plugin>\n" +
                "  <extensionPoints/>\n" +
                "</idea-plugin>\n";
        try {
            java.io.FileWriter fw = new java.io.FileWriter(compilerXml);
            fw.write(stub);
            fw.close();
        } catch (IOException ignored) {}
        return pluginRoot;
    }

    private static void collectSources(File dir, List<File> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName();
                if (!name.equals("build") && !name.equals("target") && !name.equals(".idea")
                        && !name.equals(".git") && !name.equals(".javadroid")) {
                    collectSources(f, out);
                }
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".kt")) {
                out.add(f);
            }
        }
    }

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
}
