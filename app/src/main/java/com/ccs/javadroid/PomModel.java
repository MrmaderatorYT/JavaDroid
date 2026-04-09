package com.ccs.javadroid;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PomModel {

    public String groupId = "com.ccs";
    public String artifactId = "app";
    public String version = "1.0-SNAPSHOT";
    public String packaging = "jar";
    /** Повне ім'я головного класу з pom.properties або exec-maven-plugin */
    public String mainClass;

    public final Map<String, String> properties = new LinkedHashMap<>();
    public final List<MavenDependency> dependencies = new ArrayList<>();

    public String resolveProperty(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.startsWith("${") && s.endsWith("}")) {
            String key = s.substring(2, s.length() - 1);
            String v = properties.get(key);
            return v != null ? v : s;
        }
        return s;
    }

    public List<MavenDependency> compileDependencies() {
        List<MavenDependency> out = new ArrayList<>();
        for (MavenDependency d : dependencies) {
            if (d.isForCompile()) out.add(d);
        }
        return out;
    }

    public List<MavenDependency> testDependencies() {
        List<MavenDependency> out = new ArrayList<>();
        for (MavenDependency d : dependencies) {
            if (d.isForTest()) out.add(d);
        }
        return out;
    }

    public static class MavenDependency {
        public String groupId;
        public String artifactId;
        public String version;
        /** test, provided, runtime, compile (default) */
        public String scope;

        public boolean isForCompile() {
            String s = scope == null ? "compile" : scope.trim();
            return "compile".equalsIgnoreCase(s) || "runtime".equalsIgnoreCase(s) || s.isEmpty();
        }

        public boolean isForTest() {
            String s = scope == null ? "compile" : scope.trim();
            return "test".equalsIgnoreCase(s) || "compile".equalsIgnoreCase(s)
                    || "runtime".equalsIgnoreCase(s) || s.isEmpty();
        }
    }
}
