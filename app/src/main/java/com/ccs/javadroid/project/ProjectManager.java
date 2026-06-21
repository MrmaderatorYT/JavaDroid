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
        return new AppPreferences(context).getFileEncoding();
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

    public List<File> getJavaFiles() {
        return ProjectScanner.listJavaSources(projectRoot);
    }

    public List<File> getProjectTreeFiles() {
        return ProjectScanner.listTreeFiles(projectRoot);
    }

    public File createFile(String name, String template) throws IOException {
        if (!name.contains(".")) {
            name += ".java";
        }
        File dir = projectRoot;
        if (isMavenProject()) {
            dir = ProjectLayoutHelper.mainJavaPackageDir(projectRoot);
        }
        File file = new File(dir, name);
        if (file.createNewFile()) {
            writeFile(file, template);
            return file;
        }
        return null;
    }

    public boolean deleteFile(File file) {
        return deleteRecursive(file);
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
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), getEncoding()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public void writeFile(File file, String content) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), getEncoding())) {
            writer.write(content);
        }
    }
}
