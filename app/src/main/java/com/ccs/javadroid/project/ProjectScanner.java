package com.ccs.javadroid.project;

import com.ccs.javadroid.ui.FileTreeNode;
import com.ccs.javadroid.maven.MavenPaths;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class ProjectScanner {

    private ProjectScanner() {}

    public static List<File> listJavaSources(File projectRoot) {
        List<File> out = new ArrayList<>();
        File main = MavenPaths.mainJavaDir(projectRoot);
        if (main.exists()) {
            collectJavaRecursive(main, out);
        } else {
            File[] files = projectRoot.listFiles();
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".java")) out.add(f);
                }
            }
        }
        out.sort(Comparator.comparing(File::getAbsolutePath));
        return out;
    }

    public static List<File> listTestSources(File projectRoot) {
        List<File> out = new ArrayList<>();
        File test = MavenPaths.testJavaDir(projectRoot);
        if (test.exists()) collectJavaRecursive(test, out);
        out.sort(Comparator.comparing(File::getAbsolutePath));
        return out;
    }

    /**
     * Дерево проєкту у стилі IDEA: pom.xml, далі DFS по {@code src/}
     * (папки та файли; {@code target}, {@code .javadroid} пропускаються).
     */
    public static List<FileTreeNode> listIdeaStyleTree(File projectRoot) {
        List<FileTreeNode> out = new ArrayList<>();
        File pom = MavenPaths.pomFile(projectRoot);
        if (pom.exists()) {
            out.add(new FileTreeNode(pom, 0, false));
        }

        // Root-level files (non-Java, like .html, .css, .js, .http, .sql, .svg, .md, .kt)
        File[] rootFiles = projectRoot.listFiles();
        if (rootFiles != null) {
            Arrays.sort(rootFiles, Comparator.comparing(File::getName));
            for (File f : rootFiles) {
                if (f.isFile() && !shouldSkipFile(f)) {
                    out.add(new FileTreeNode(f, 0, false));
                }
            }
        }

        File src = new File(projectRoot, "src");
        if (src.isDirectory()) {
            walkTree(out, src, 0);
        }
        return out;
    }

    private static boolean shouldSkipFile(File f) {
        String name = f.getName();
        return name.startsWith(".") || name.equals("pom.xml");
    }

    private static void walkTree(List<FileTreeNode> out, File dir, int depth) {
        out.add(new FileTreeNode(dir, depth, true));
        File[] ch = dir.listFiles();
        if (ch == null) return;
        Arrays.sort(ch, Comparator.comparing(File::getName));
        for (File f : ch) {
            if (shouldSkip(f)) continue;
            if (f.isDirectory()) {
                walkTree(out, f, depth + 1);
            } else {
                out.add(new FileTreeNode(f, depth + 1, false));
            }
        }
    }

    private static boolean shouldSkip(File f) {
        String n = f.getName();
        return f.isDirectory() && ("target".equals(n) || ".javadroid".equals(n)
                || ".git".equals(n) || ".idea".equals(n));
    }

    /** Застаріле плоске дерево — для сумісності; краще {@link #listIdeaStyleTree}. */
    public static List<File> listTreeFiles(File projectRoot) {
        List<File> out = new ArrayList<>();
        File pom = MavenPaths.pomFile(projectRoot);
        if (pom.exists()) out.add(pom);

        List<File> all = new ArrayList<>();
        collectExtRecursive(projectRoot, all, ".java", ".xml");
        all.sort(Comparator.comparing(f -> relativePath(projectRoot, f)));
        out.addAll(all);
        return out;
    }

    private static void collectJavaRecursive(File dir, List<File> out) {
        File[] ch = dir.listFiles();
        if (ch == null) return;
        Arrays.sort(ch, Comparator.comparing(File::getName));
        for (File f : ch) {
            if (f.isDirectory()) collectJavaRecursive(f, out);
            else if (f.getName().endsWith(".java")) out.add(f);
        }
    }

    private static void collectExtRecursive(File dir, List<File> out, String... exts) {
        File[] ch = dir.listFiles();
        if (ch == null) return;
        for (File f : ch) {
            if (f.isDirectory()) {
                String n = f.getName();
                if (".javadroid".equals(n) || "target".equals(n)) continue;
                collectExtRecursive(f, out, exts);
            } else {
                String name = f.getName();
                for (String ext : exts) {
                    if (name.endsWith(ext)) {
                        out.add(f);
                        break;
                    }
                }
            }
        }
    }

    static String relativePath(File root, File file) {
        String rp = file.getAbsolutePath().substring(root.getAbsolutePath().length());
        if (rp.startsWith("/")) rp = rp.substring(1);
        return rp;
    }
}
