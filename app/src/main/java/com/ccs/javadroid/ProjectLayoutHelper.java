package com.ccs.javadroid;

import java.io.File;
import java.io.IOException;

/**
 * Шляхи до package у src/main/java за {@code groupId} з pom.xml.
 */
public final class ProjectLayoutHelper {

    private ProjectLayoutHelper() {}

    /** Каталог пакета для нових класів: src/main/java/&lt;groupId з крапок у слеші&gt;. */
    public static File mainJavaPackageDir(File projectRoot) throws IOException {
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectRoot));
            String g = pom.groupId != null ? pom.groupId : "com.ccs";
            String rel = g.replace('.', File.separatorChar);
            File dir = new File(MavenPaths.mainJavaDir(projectRoot), rel);
            dir.mkdirs();
            return dir;
        } catch (Exception e) {
            File fallback = new File(MavenPaths.mainJavaDir(projectRoot), "com/ccs");
            fallback.mkdirs();
            return fallback;
        }
    }

    public static String mainPackageName(File projectRoot) throws IOException {
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectRoot));
            return pom.groupId != null ? pom.groupId : "com.ccs";
        } catch (Exception e) {
            return "com.ccs";
        }
    }
}
