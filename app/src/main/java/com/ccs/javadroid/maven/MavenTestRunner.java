package com.ccs.javadroid.maven;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * On-device JUnit 4 test runner (analog of Maven Surefire).
 *
 * Discovers and runs {@code @Test}-annotated methods from compiled test classes
 * loaded via {@link DexClassLoader}. Captures stdout/stderr and reports results
 * in Surefire-compatible text format.
 *
 * Supports: {@code @Test}, {@code @Ignore}, {@code @Before}, {@code @After}.
 */
public final class MavenTestRunner {

    private static final String TAG = "MavenTestRunner";

    /** Callback for test execution progress/results */
    public interface Callback {
        void onProgress(String line);
        void onResult(String output);
    }

    private MavenTestRunner() {}

    /**
     * Run all JUnit 4 tests from the given dex directory.
     *
     * @param context         Android context (for secure dex dir)
     * @param dexDir          Directory containing {@code classes.dex} with test classes + JUnit
     * @param testClassesDir  Directory with test .class files (used for class name discovery)
     * @param mainClassesDir  Main classes (added to classpath for class-under-test access)
     * @param depJars         Dependency JARs (including JUnit)
     * @param androidJar      android.jar (needed for DexClassLoader parent)
     * @param callback        Progress/result callback
     */
    public static void runTests(Context context, File dexDir, File testClassesDir,
                                File mainClassesDir, List<File> depJars, File androidJar,
                                Callback callback) {
        try {
            // 1. Secure dex copy (Android 14+ requirement)
            File dexFile = new File(dexDir, "classes.dex");
            if (!dexFile.exists()) {
                postResult(callback, "Error: classes.dex not found in " + dexDir.getAbsolutePath());
                return;
            }

            File secureDexDir = new File(context.getDir("dex", Context.MODE_PRIVATE),
                    "test_" + System.currentTimeMillis());
            secureDexDir.mkdirs();
            File secureDex = new File(secureDexDir, "classes.dex");
            copyFile(dexFile, secureDex);

            // 2. Build classpath for DexClassLoader library path (dependency JARs)
            StringBuilder libPath = new StringBuilder();
            for (File jar : depJars) {
                if (libPath.length() > 0) libPath.append(File.pathSeparator);
                libPath.append(jar.getAbsolutePath());
            }
            if (androidJar != null) {
                if (libPath.length() > 0) libPath.append(File.pathSeparator);
                libPath.append(androidJar.getAbsolutePath());
            }

            File optDir = new File(secureDexDir, "opt");
            optDir.mkdirs();

            // 3. Create DexClassLoader
            DexClassLoader cl = new DexClassLoader(
                    secureDex.getAbsolutePath(),
                    optDir.getAbsolutePath(),
                    libPath.toString(),
                    context.getClassLoader()
            );

            // 4. Redirect stdout/stderr
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            ByteArrayOutputStream testOut = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(testOut, true);

            // 5. Discover test classes from .class files
            List<String> classNames = discoverTestClassNames(testClassesDir);
            if (classNames.isEmpty()) {
                postResult(callback, "No test classes found in target/test-classes");
                return;
            }

            postProgress(callback, "Found " + classNames.size() + " test class(es)");

            // 6. Run tests
            StringBuilder results = new StringBuilder();
            int totalTests = 0;
            int passed = 0;
            int failed = 0;
            int errors = 0;
            int skipped = 0;

            System.setOut(ps);
            System.setErr(ps);

            try {
                for (String className : classNames) {
                    try {
                        Class<?> testClass = cl.loadClass(className);

                        // Load JUnit annotations from the same classloader as annotation classes
                        @SuppressWarnings("unchecked")
                        Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>)
                                cl.loadClass("org.junit.Test");
                        @SuppressWarnings("unchecked")
                        Class<? extends Annotation> ignoreAnnotation = (Class<? extends Annotation>)
                                cl.loadClass("org.junit.Ignore");
                        @SuppressWarnings("unchecked")
                        Class<? extends Annotation> beforeAnnotation = (Class<? extends Annotation>)
                                cl.loadClass("org.junit.Before");
                        @SuppressWarnings("unchecked")
                        Class<? extends Annotation> afterAnnotation = (Class<? extends Annotation>)
                                cl.loadClass("org.junit.After");

                        // Check if the class itself is @Ignore'd
                        if (testClass.getAnnotation(ignoreAnnotation) != null) {
                            postProgress(callback, "\u23F8 " + className + " — skipped (class @Ignore)");
                            results.append("\u23F8 ").append(className).append(" — skipped (class @Ignore)\n");
                            skipped++;
                            continue;
                        }

                        // Collect test methods
                        Method[] methods = testClass.getDeclaredMethods();
                        Object instance = null;
                        Method beforeMethod = null;
                        Method afterMethod = null;

                        // Find @Before / @After
                        for (Method m : methods) {
                            if (m.getAnnotation(beforeAnnotation) != null && beforeMethod == null) {
                                beforeMethod = m;
                            }
                            if (m.getAnnotation(afterAnnotation) != null && afterMethod == null) {
                                afterMethod = m;
                            }
                        }

                        for (Method m : methods) {
                            if (m.getAnnotation(testAnnotation) == null) continue;
                            if (!Modifier.isPublic(m.getModifiers())) continue;

                            totalTests++;

                            // Check if method is @Ignore'd
                            if (m.getAnnotation(ignoreAnnotation) != null) {
                                postProgress(callback, "\u23F8 " + className + "." + m.getName() + " — skipped");
                                results.append("\u23F8 ").append(className).append(".").append(m.getName())
                                        .append(" — skipped\n");
                                skipped++;
                                continue;
                            }

                            // Instantiate test class
                            try {
                                instance = testClass.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                postProgress(callback, "\u274C " + className + " — cannot instantiate: " + e.getMessage());
                                results.append("\u274C ").append(className)
                                        .append(" — cannot instantiate: ").append(e.getMessage()).append("\n");
                                errors++;
                                continue;
                            }

                            // Run @Before
                            String beforeError = runSetupMethod(beforeMethod, instance);
                            if (beforeError != null) {
                                postProgress(callback, "\u274C " + className + "." + m.getName()
                                        + " — @Before failed: " + beforeError);
                                results.append("\u274C ").append(className).append(".").append(m.getName())
                                        .append(" — @Before failed: ").append(beforeError).append("\n");
                                errors++;
                                runTeardownMethod(afterMethod, instance);
                                continue;
                            }

                            // Run @Test
                            try {
                                postProgress(callback, "\u25B6 " + className + "." + m.getName() + "...");
                                ps.flush();
                                testOut.reset();
                                m.invoke(instance, (Object[]) null);
                                ps.flush();
                                passed++;
                                postProgress(callback, "\u2705 " + className + "." + m.getName() + " — passed");
                                results.append("\u2705 ").append(className).append(".").append(m.getName())
                                        .append(" — passed\n");
                            } catch (java.lang.reflect.InvocationTargetException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof AssertionError) {
                                    failed++;
                                    String msg = cause.getMessage() != null ? cause.getMessage() : "(no message)";
                                    postProgress(callback, "\u274C " + className + "." + m.getName() + " — failed: " + msg);
                                    results.append("\u274C ").append(className).append(".").append(m.getName())
                                            .append(" — failed: ").append(msg).append("\n");
                                } else {
                                    errors++;
                                    String msg = cause != null ? cause.toString() : e.toString();
                                    postProgress(callback, "\u274E " + className + "." + m.getName() + " — error: " + msg);
                                    results.append("\u274E ").append(className).append(".").append(m.getName())
                                            .append(" — error: ").append(msg).append("\n");
                                }
                            } catch (Exception e) {
                                errors++;
                                postProgress(callback, "\u274E " + className + "." + m.getName() + " — error: " + e);
                                results.append("\u274E ").append(className).append(".").append(m.getName())
                                        .append(" — error: ").append(e).append("\n");
                            }

                            // Run @After
                            String afterError = runTeardownMethod(afterMethod, instance);
                            if (afterError != null) {
                                postProgress(callback, "\u26A0 @After failed for " + className + ": " + afterError);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // Class might not be a test class (e.g. helper class)
                        continue;
                    } catch (NoClassDefFoundError e) {
                        // JUnit annotations not available in this classloader
                        postProgress(callback, "Skipping " + className + " (JUnit not in classpath)");
                        continue;
                    }
                }

                ps.flush();

                // Build summary
                StringBuilder summary = new StringBuilder();
                summary.append("\n");
                summary.append("Tests run: ").append(totalTests);
                summary.append(", Failures: ").append(failed);
                summary.append(", Errors: ").append(errors);
                summary.append(", Skipped: ").append(skipped);
                summary.append("\n\n");

                // Append captured stdout from test execution
                String capturedOutput = testOut.toString("UTF-8").trim();
                if (!capturedOutput.isEmpty()) {
                    summary.append("--- Test output ---\n");
                    summary.append(capturedOutput);
                    summary.append("\n--- End of test output ---\n\n");
                }

                if (!results.toString().isEmpty()) {
                    summary.append(results);
                }

                if (failed == 0 && errors == 0) {
                    summary.append("\nBUILD SUCCESS — all tests passed");
                } else {
                    summary.append("\nBUILD FAILURE — there are test failures");
                }

                postResult(callback, summary.toString());

            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        } catch (Throwable e) {
            postResult(callback, "Test runner error: " + e.getMessage() + "\n"
                    + Log.getStackTraceString(e));
        }
    }

    /**
     * Discover fully-qualified class names from .class files in a directory.
     * Converts file paths like {@code com/ccs/AppTest.class} to {@code com.ccs.AppTest}.
     */
    private static List<String> discoverTestClassNames(File classesDir) {
        List<String> names = new ArrayList<>();
        if (!classesDir.exists()) return names;
        collectClassNames(classesDir, classesDir, names);
        return names;
    }

    private static void collectClassNames(File root, File dir, List<String> names) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectClassNames(root, f, names);
            } else if (f.getName().endsWith(".class")) {
                String rel = root.toPath().relativize(f.toPath()).toString()
                        .replace(File.separatorChar, '.');
                // Remove .class extension
                if (rel.endsWith(".class")) {
                    rel = rel.substring(0, rel.length() - 6);
                }
                // Skip inner classes ($...) and module-info
                if (!rel.contains("$") && !rel.equals("module-info") && !rel.equals("package-info")) {
                    names.add(rel);
                }
            }
        }
    }

    private static String runSetupMethod(Method before, Object instance) {
        if (before == null) return null;
        try {
            before.setAccessible(true);
            before.invoke(instance);
            return null;
        } catch (Exception e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException ? e.getCause() : e;
            return cause != null ? cause.getMessage() : e.getMessage();
        }
    }

    private static String runTeardownMethod(Method after, Object instance) {
        if (after == null) return null;
        try {
            after.setAccessible(true);
            after.invoke(instance);
            return null;
        } catch (Exception e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException ? e.getCause() : e;
            return cause != null ? cause.getMessage() : e.getMessage();
        }
    }

    private static void copyFile(File src, File dest) throws Exception {
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) fos.write(buf, 0, n);
        }
    }

    private static void postProgress(Callback cb, String line) {
        if (cb != null) cb.onProgress(line);
    }

    private static void postResult(Callback cb, String output) {
        if (cb != null) cb.onResult(output);
    }
}
