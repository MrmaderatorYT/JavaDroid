package com.ccs.javadroid.project;

import com.ccs.javadroid.maven.MavenPaths;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Creates a Gradle-based Java project with standard layout.
 * <pre>
 *   ProjectName/
 *     build.gradle
 *     settings.gradle
 *     src/main/java/&lt;package&gt;/App.java
 *     src/main/resources/
 *     src/test/java/&lt;package&gt;/AppTest.java
 *     src/test/resources/
 * </pre>
 */
public final class GradleProjectFactory {

    private GradleProjectFactory() {}

    public static File create(Context context, String projectName, String groupId) throws IOException {
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        String safe = projectName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        File root = MavenPaths.projectDir(context, safe);
        if (root.exists()) {
            throw new IOException("Project already exists: " + safe);
        }

        String gid = (groupId != null && !groupId.trim().isEmpty())
                ? groupId.trim().replaceAll("\\s+", "")
                : "com.ccs." + safe.toLowerCase().replace('-', '_');

        String pkgPath = gid.replace('.', File.separatorChar);
        File mainJavaPkg = new File(root, "src/main/java/" + pkgPath);
        File testJavaPkg = new File(root, "src/test/java/" + pkgPath);
        mainJavaPkg.mkdirs();
        testJavaPkg.mkdirs();

        new File(root, "src/main/resources").mkdirs();
        new File(root, "src/test/resources").mkdirs();

        // build.gradle — source/target level follows the configured ECJ target so
        // the on-device compiler and the script agree.
        String mainClass = gid + ".App";
        String javaVersion = javaVersionConstant(context);
        writeUtf8(new File(root, "build.gradle"),
                "plugins {\n"
                + "    id 'java'\n"
                + "    id 'application'\n"
                + "}\n"
                + "\n"
                + "group = '" + gid + "'\n"
                + "version = '1.0-SNAPSHOT'\n"
                + "\n"
                + "java {\n"
                + "    sourceCompatibility = JavaVersion." + javaVersion + "\n"
                + "    targetCompatibility = JavaVersion." + javaVersion + "\n"
                + "}\n"
                + "\n"
                + "application {\n"
                + "    mainClass = '" + mainClass + "'\n"
                + "}\n"
                + "\n"
                + "repositories {\n"
                + "    mavenCentral()\n"
                + "}\n"
                + "\n"
                + "ext {\n"
                + "    junitVersion = '4.13.2'\n"
                + "}\n"
                + "\n"
                + "dependencies {\n"
                + "    testImplementation \"junit:junit:${junitVersion}\"\n"
                + "}\n");

        // settings.gradle
        writeUtf8(new File(root, "settings.gradle"),
                "rootProject.name = '" + safe + "'\n");

        // gradle.properties
        writeUtf8(new File(root, "gradle.properties"),
                "# Project-wide Gradle properties.\n"
                + "# Values here are also visible to the JavaDroid build script parser.\n"
                + "org.gradle.jvmargs=-Xmx1g\n");

        // App.java
        writeUtf8(new File(mainJavaPkg, "App.java"),
                "package " + gid + ";\n"
                + "\n"
                + "public class App {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello from \" + App.class.getPackage().getName());\n"
                + "    }\n"
                + "}\n");

        // AppTest.java
        writeUtf8(new File(testJavaPkg, "AppTest.java"),
                "package " + gid + ";\n"
                + "\n"
                + "import org.junit.Test;\n"
                + "import static org.junit.Assert.*;\n"
                + "\n"
                + "public class AppTest {\n"
                + "    @Test\n"
                + "    public void smoke() {\n"
                + "        assertTrue(true);\n"
                + "    }\n"
                + "}\n");

        // .gitignore — `target/` is listed too because the on-device compiler
        // writes class output there.
        writeUtf8(new File(root, ".gitignore"),
                "build/\n"
                + "target/\n"
                + ".gradle/\n"
                + ".javadroid/\n"
                + "*.class\n"
                + "*.jar\n"
                + "!gradle/wrapper/gradle-wrapper.jar\n");

        return root;
    }

    /** Maps the configured ECJ target onto a {@code JavaVersion} constant. */
    private static String javaVersionConstant(Context context) {
        String target;
        try {
            target = new com.ccs.javadroid.util.AppPreferences(context).getJavaTarget();
        } catch (Exception e) {
            target = com.ccs.javadroid.util.AppPreferences.JAVA_8;
        }
        if (target == null) return "VERSION_1_8";
        switch (target) {
            case com.ccs.javadroid.util.AppPreferences.JAVA_11: return "VERSION_11";
            case com.ccs.javadroid.util.AppPreferences.JAVA_17: return "VERSION_17";
            case com.ccs.javadroid.util.AppPreferences.JAVA_21: return "VERSION_21";
            case com.ccs.javadroid.util.AppPreferences.JAVA_8:
            default: return "VERSION_1_8";
        }
    }

    private static void writeUtf8(File file, String content) throws IOException {
        File p = file.getParentFile();
        if (p != null) p.mkdirs();
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(content);
        }
    }
}
