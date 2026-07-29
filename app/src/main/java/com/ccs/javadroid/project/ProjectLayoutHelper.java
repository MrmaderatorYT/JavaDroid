package com.ccs.javadroid.project;

import com.ccs.javadroid.maven.PomModel;
import com.ccs.javadroid.maven.PomParser;
import com.ccs.javadroid.maven.MavenPaths;
import java.io.File;
import java.io.IOException;

/**
 * Шляхи до package у src/main/java за {@code groupId} з pom.xml або build.gradle.
 */
public final class ProjectLayoutHelper {

    private ProjectLayoutHelper() {}

    /** Каталог пакета для нових класів: src/main/java/&lt;groupId з крапок у слеші&gt;. */
    public static File mainJavaPackageDir(File projectRoot) throws IOException {
        String g = mainPackageName(projectRoot);
        String rel = g.replace('.', File.separatorChar);
        File dir = new File(MavenPaths.mainJavaDir(projectRoot), rel);
        dir.mkdirs();
        return dir;
    }

    /**
     * The root package for new sources: {@code groupId} from pom.xml, else
     * {@code group} from the Gradle script, else the package already present
     * on disk, else {@code com.ccs}.
     */
    public static String mainPackageName(File projectRoot) throws IOException {
        File pom = MavenPaths.pomFile(projectRoot);
        if (pom.isFile()) {
            try {
                PomModel model = PomParser.parse(pom);
                if (model.groupId != null && !model.groupId.isEmpty()) return model.groupId;
            } catch (Exception ignored) {
            }
        }
        if (com.ccs.javadroid.gradle.GradlePaths.isGradleProject(projectRoot)) {
            try {
                PomModel model = com.ccs.javadroid.gradle.GradleBuildParser
                        .parseOrDefault(projectRoot).pom;
                if (model.groupId != null && !model.groupId.isEmpty()) return model.groupId;
            } catch (Exception ignored) {
            }
        }
        String onDisk = deepestSinglePackage(MavenPaths.mainJavaDir(projectRoot));
        return onDisk != null ? onDisk : "com.ccs";
    }

    /**
     * Walks the single-child directory chain under {@code src/main/java} to
     * recover the existing package, e.g. {@code com/example/app} → {@code com.example.app}.
     *
     * @return the dotted package name, or {@code null} if the layout is ambiguous
     */
    private static String deepestSinglePackage(File sourceRoot) {
        if (sourceRoot == null || !sourceRoot.isDirectory()) return null;
        StringBuilder pkg = new StringBuilder();
        File current = sourceRoot;
        for (int depth = 0; depth < 16; depth++) {
            File[] children = current.listFiles(File::isDirectory);
            if (children == null || children.length != 1) break;
            if (pkg.length() > 0) pkg.append('.');
            pkg.append(children[0].getName());
            current = children[0];
        }
        return pkg.length() > 0 ? pkg.toString() : null;
    }

    public static String packageNameForDir(File projectRoot, File folder) {
        File mainJava = MavenPaths.mainJavaDir(projectRoot);
        File testJava = MavenPaths.testJavaDir(projectRoot);

        String folderPath = folder.getAbsolutePath();
        String mainJavaPath = mainJava.getAbsolutePath();
        String testJavaPath = testJava.getAbsolutePath();

        if (folderPath.startsWith(mainJavaPath)) {
            String rel = folderPath.substring(mainJavaPath.length());
            if (rel.startsWith(File.separator)) {
                rel = rel.substring(1);
            }
            if (rel.isEmpty()) return "";
            return rel.replace(File.separatorChar, '.');
        } else if (folderPath.startsWith(testJavaPath)) {
            String rel = folderPath.substring(testJavaPath.length());
            if (rel.startsWith(File.separator)) {
                rel = rel.substring(1);
            }
            if (rel.isEmpty()) return "";
            return rel.replace(File.separatorChar, '.');
        }
        return "";
    }
}
