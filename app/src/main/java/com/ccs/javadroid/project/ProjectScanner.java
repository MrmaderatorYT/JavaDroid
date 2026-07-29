package com.ccs.javadroid.project;

import com.ccs.javadroid.ui.FileTreeNode;
import com.ccs.javadroid.maven.MavenPaths;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
     * Дерево проєкту у стилі IDEA з усіма розкритими папками.
     *
     * @deprecated краще {@link #listIdeaStyleTree(File, Set)} — воно показує лише
     *             вміст розкритих каталогів.
     */
    @Deprecated
    public static List<FileTreeNode> listIdeaStyleTree(File projectRoot) {
        return listIdeaStyleTree(projectRoot, null);
    }

    /**
     * Дерево проєкту у стилі IDEA: build-скрипт, файли кореня, далі DFS по
     * {@code src/} — але спускається лише в ті каталоги, шляхи яких є в
     * {@code expandedPaths}.
     *
     * @param expandedPaths абсолютні шляхи розкритих каталогів; {@code null}
     *                      означає «розкрити все» (стара поведінка)
     */
    public static List<FileTreeNode> listIdeaStyleTree(File projectRoot, Set<String> expandedPaths) {
        List<FileTreeNode> out = new ArrayList<>();

        // Build scripts first — they are what a project is usually opened for.
        File pom = MavenPaths.pomFile(projectRoot);
        if (pom.exists()) {
            out.add(new FileTreeNode(pom, 0, false));
        }
        for (String buildScript : new String[]{
                "build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts"}) {
            File f = new File(projectRoot, buildScript);
            if (f.isFile()) out.add(new FileTreeNode(f, 0, false));
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

        // Source roots and any other visible top-level directory.
        List<File> topDirs = new ArrayList<>();
        if (rootFiles != null) {
            for (File f : rootFiles) {
                if (f.isDirectory() && !shouldSkip(f)) topDirs.add(f);
            }
            topDirs.sort(Comparator.comparing(File::getName));
        }
        for (File dir : topDirs) {
            walkTree(out, dir, 0, expandedPaths);
        }
        return out;
    }

    private static boolean shouldSkipFile(File f) {
        String name = f.getName();
        return name.startsWith(".")
                || name.equals("pom.xml")
                || name.equals("build.gradle") || name.equals("build.gradle.kts")
                || name.equals("settings.gradle") || name.equals("settings.gradle.kts");
    }

    /**
     * Adds {@code dir} and, when it is expanded, its contents.
     *
     * @param expandedPaths {@code null} expands everything
     */
    private static void walkTree(List<FileTreeNode> out, File dir, int depth,
                                 Set<String> expandedPaths) {
        File[] ch = dir.listFiles();
        boolean hasChildren = false;
        if (ch != null) {
            for (File f : ch) {
                if (!shouldSkip(f)) { hasChildren = true; break; }
            }
        }
        boolean expanded = expandedPaths == null
                || expandedPaths.contains(dir.getAbsolutePath());

        out.add(new FileTreeNode(dir, depth, true, expanded, hasChildren));
        if (!expanded || ch == null) return;

        Arrays.sort(ch, Comparator.comparing(File::getName));
        // Directories first, then files — the usual project-view ordering.
        for (File f : ch) {
            if (shouldSkip(f) || !f.isDirectory()) continue;
            walkTree(out, f, depth + 1, expandedPaths);
        }
        for (File f : ch) {
            if (shouldSkip(f) || f.isDirectory()) continue;
            out.add(new FileTreeNode(f, depth + 1, false));
        }
    }

    /** Build output and tool metadata are noise in the project view. */
    private static boolean shouldSkip(File f) {
        String n = f.getName();
        if (!f.isDirectory()) return false;
        // Hidden directories are skipped wholesale, matching shouldSkipFile.
        if (n.startsWith(".")) return true;
        return "target".equals(n) || "build".equals(n) || "out".equals(n) || "bin".equals(n);
    }

    /**
     * Каталоги, які варто розкрити при першому відкритті проєкту: {@code src} і
     * далі по ланцюжку одиничних підкаталогів до пакета з файлами.
     *
     * @return абсолютні шляхи каталогів
     */
    public static Set<String> defaultExpandedPaths(File projectRoot) {
        Set<String> out = new LinkedHashSet<>();
        if (projectRoot == null || !projectRoot.isDirectory()) return out;

        File[] top = projectRoot.listFiles();
        if (top == null) return out;
        for (File dir : top) {
            if (!dir.isDirectory() || shouldSkip(dir)) continue;
            if ("src".equals(dir.getName())) {
                out.add(dir.getAbsolutePath());
                expandSingleChildChain(dir, out, 0);
                continue;
            }
            // A module folder (Gradle multi-project) holds its own src/.
            File moduleSrc = new File(dir, "src");
            if (moduleSrc.isDirectory()) {
                out.add(dir.getAbsolutePath());
                out.add(moduleSrc.getAbsolutePath());
                expandSingleChildChain(moduleSrc, out, 0);
            }
        }
        return out;
    }

    /** Follows a chain of single-subdirectory folders, e.g. main/java/com/example. */
    private static void expandSingleChildChain(File dir, Set<String> out, int depth) {
        if (depth > 24) return;
        File[] ch = dir.listFiles();
        if (ch == null) return;
        List<File> dirs = new ArrayList<>();
        for (File f : ch) {
            if (f.isDirectory() && !shouldSkip(f)) dirs.add(f);
        }
        // Fan-out means the user should choose; stop expanding there.
        if (dirs.size() != 1) {
            for (File f : dirs) out.add(f.getAbsolutePath());
            return;
        }
        out.add(dirs.get(0).getAbsolutePath());
        expandSingleChildChain(dirs.get(0), out, depth + 1);
    }

    /** Усі каталоги проєкту — для «розкрити все». */
    public static Set<String> allDirectories(File projectRoot) {
        Set<String> out = new LinkedHashSet<>();
        collectDirs(projectRoot, out, 0);
        return out;
    }

    private static void collectDirs(File dir, Set<String> out, int depth) {
        if (depth > 32 || dir == null) return;
        File[] ch = dir.listFiles();
        if (ch == null) return;
        for (File f : ch) {
            if (f.isDirectory() && !shouldSkip(f)) {
                out.add(f.getAbsolutePath());
                collectDirs(f, out, depth + 1);
            }
        }
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
