package com.ccs.javadroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Завантаження JAR із Maven Central (прямі залежності з pom.xml).
 */
public final class MavenDependencyResolver {

    private static final String CENTRAL =
            "https://repo1.maven.org/maven2/";

    public interface Log {
        void onLine(String line);
    }

    private MavenDependencyResolver() {}

    public static List<File> resolve(File projectRoot, PomModel pom, Log log) throws IOException {
        File repo = MavenPaths.localRepoDir(projectRoot);
        repo.mkdirs();

        List<File> jars = new ArrayList<>();
        for (PomModel.MavenDependency d : pom.compileDependencies()) {
            if (d.version == null || d.version.isEmpty()) {
                if (log != null) log.onLine("Skip (no version): " + d.groupId + ":" + d.artifactId);
                continue;
            }
            File jar = downloadJar(repo, d, log);
            if (jar != null && jar.exists()) jars.add(jar);
        }
        return jars;
    }

    /** Лише test-scoped JAR (наприклад junit), додатково до compile classpath. */
    public static List<File> resolveTestScoped(File projectRoot, PomModel pom, Log log) throws IOException {
        File repo = MavenPaths.localRepoDir(projectRoot);
        repo.mkdirs();

        List<File> jars = new ArrayList<>();
        for (PomModel.MavenDependency d : pom.dependencies) {
            String sc = d.scope == null ? "" : d.scope.trim();
            if (!"test".equalsIgnoreCase(sc)) continue;
            if (d.version == null || d.version.isEmpty()) continue;
            File jar = downloadJar(repo, d, log);
            if (jar != null && jar.exists()) jars.add(jar);
        }
        return jars;
    }

    private static File downloadJar(File localRepoRoot, PomModel.MavenDependency d, Log log)
            throws IOException {
        String path = d.groupId.replace('.', '/') + "/" + d.artifactId + "/" + d.version + "/"
                + d.artifactId + "-" + d.version + ".jar";

        File destDir = new File(localRepoRoot, d.groupId.replace('.', '/')
                + "/" + d.artifactId + "/" + d.version);
        destDir.mkdirs();
        File dest = new File(destDir, d.artifactId + "-" + d.version + ".jar");
        if (dest.exists() && dest.length() > 0) {
            if (log != null) log.onLine("Cached: " + dest.getName());
            return dest;
        }

        String urlStr = CENTRAL + path;
        if (log != null) log.onLine("GET " + urlStr);

        HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
        c.setConnectTimeout(30000);
        c.setReadTimeout(60000);
        c.setRequestMethod("GET");
        int code = c.getResponseCode();
        if (code != 200) {
            if (log != null) log.onLine("HTTP " + code + " for " + d.artifactId);
            c.disconnect();
            return null;
        }

        try (InputStream in = c.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        } finally {
            c.disconnect();
        }

        if (log != null) log.onLine("Saved " + dest.getName());
        return dest;
    }
}
