package com.ccs.javadroid;

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

        // build.gradle
        String mainClass = gid + ".App";
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
                + "    sourceCompatibility = JavaVersion.VERSION_11\n"
                + "    targetCompatibility = JavaVersion.VERSION_11\n"
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
                + "dependencies {\n"
                + "    testImplementation 'junit:junit:4.13.2'\n"
                + "}\n");

        // settings.gradle
        writeUtf8(new File(root, "settings.gradle"),
                "rootProject.name = '" + safe + "'\n");

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

        // .gitignore
        writeUtf8(new File(root, ".gitignore"),
                "build/\n"
                + ".gradle/\n"
                + "*.class\n"
                + "*.jar\n"
                + "!gradle/wrapper/gradle-wrapper.jar\n");

        return root;
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
