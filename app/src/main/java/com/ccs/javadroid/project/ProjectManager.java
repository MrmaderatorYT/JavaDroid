package com.ccs.javadroid.project;

import com.ccs.javadroid.util.AppPreferences;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.List;

public class ProjectManager {

    private File projectRoot;
    private final Context context;

    public ProjectManager(Context context) {
        this.context = context;
        projectRoot = new File(context.getFilesDir(), "project");
        projectRoot.mkdirs();
    }

    private String getEncoding() {
        String encoding = new AppPreferences(context).getFileEncoding();
        if ("UTF-8 BOM".equalsIgnoreCase(encoding)) return "UTF-8";
        if ("UTF-16 BE BOM".equalsIgnoreCase(encoding)) return "UTF-16BE";
        if ("UTF-16 LE BOM".equalsIgnoreCase(encoding)) return "UTF-16LE";
        if ("Ansi".equalsIgnoreCase(encoding)) return "windows-1252";
        // Keep old preference values readable after upgrading.
        return encoding;
    }

    public void setProjectRoot(File root) {
        if (root != null) {
            projectRoot = root;
            projectRoot.mkdirs();
        }
    }

    public File getProjectDir() {
        return projectRoot;
    }

    public boolean isMavenProject() {
        return new File(projectRoot, "pom.xml").exists();
    }

    /** True when the project is driven by a Gradle build script. */
    public boolean isGradleProject() {
        return com.ccs.javadroid.gradle.GradlePaths.isGradleProject(projectRoot);
    }

    /**
     * True when the project follows the {@code src/main/java/<package>} layout —
     * that is, either build system. Used to decide where new sources go.
     */
    public boolean hasStandardLayout() {
        return isMavenProject() || isGradleProject();
    }

    public List<File> getJavaFiles() {
        return ProjectScanner.listJavaSources(projectRoot);
    }

    /** Sources in any supported language — see {@link ProjectScanner#listAllSources}. */
    public List<File> getSourceFiles() {
        return ProjectScanner.listAllSources(projectRoot);
    }

    public List<File> getProjectTreeFiles() {
        return ProjectScanner.listTreeFiles(projectRoot);
    }

    public File createFile(String name, String template) throws IOException {
        if (!name.contains(".")) {
            name += ".java";
        }
        File dir = projectRoot;
        if (hasStandardLayout()) {
            // Routed by extension: a .kt file lands in src/main/kotlin, anything
            // else in src/main/java. Putting Kotlin under src/main/java compiles
            // here but is the wrong layout for any desktop IDE opening it later.
            dir = ProjectLayoutHelper.mainSourcePackageDir(projectRoot, name);
        }
        File file = new File(dir, name);
        if (file.createNewFile()) {
            writeFile(file, template);
            return file;
        }
        return null;
    }

    public boolean deleteFile(File file) {
        boolean ok = deleteRecursive(file);
        // A delete may take a whole directory with it, so nothing kept is trusted.
        if (ok) com.ccs.javadroid.analysis.ProblemsWorkspaceAnalyzer.invalidateWorkspace();
        return ok;
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return file.delete();
    }

    /**
     * Checks whether the given file resides on external/shared storage.
     * Internal app storage (getFilesDir, getCacheDir) does NOT require
     * MANAGE_EXTERNAL_STORAGE, so the check should be skipped for those paths.
     */
    private boolean isExternalFile(File file) {
        try {
            String path = file.getCanonicalPath();
            String internal = context.getFilesDir().getCanonicalPath();
            String cache = context.getCacheDir().getCanonicalPath();
            return !path.startsWith(internal) && !path.startsWith(cache);
        } catch (Exception e) {
            return true; // assume external if we can't determine
        }
    }

    public String readFile(File file) throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        String mode = new AppPreferences(context).getFileEncoding();
        int offset = 0;
        if ("UTF-8 BOM".equalsIgnoreCase(mode) && bytes.length >= 3
                && (bytes[0] & 0xff) == 0xef && (bytes[1] & 0xff) == 0xbb && (bytes[2] & 0xff) == 0xbf) offset = 3;
        else if (("UTF-16 BE BOM".equalsIgnoreCase(mode) || "UTF-16 LE BOM".equalsIgnoreCase(mode))
                && bytes.length >= 2 && (((bytes[0] & 0xff) == 0xfe && (bytes[1] & 0xff) == 0xff)
                || ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xfe))) offset = 2;
        return new String(bytes, offset, bytes.length - offset, Charset.forName(getEncoding()));
    }

    public void writeFile(File file, String content) throws IOException {
        String mode = new AppPreferences(context).getFileEncoding();
        byte[] body = content.getBytes(Charset.forName(getEncoding()));
        byte[] bom = "UTF-8 BOM".equalsIgnoreCase(mode) ? new byte[]{(byte)0xef,(byte)0xbb,(byte)0xbf}
                : "UTF-16 BE BOM".equalsIgnoreCase(mode) ? new byte[]{(byte)0xfe,(byte)0xff}
                : "UTF-16 LE BOM".equalsIgnoreCase(mode) ? new byte[]{(byte)0xff,(byte)0xfe} : new byte[0];
        try (FileOutputStream out = new FileOutputStream(file)) { out.write(bom); out.write(body); }
        // Every route that changes a file goes through here, which makes it the
        // one place that reliably knows this file is no longer what the last
        // analysis read. Only this file is dropped — the rest of the project did
        // not change.
        com.ccs.javadroid.analysis.ProblemsWorkspaceAnalyzer.invalidateFile(file);
    }
}
