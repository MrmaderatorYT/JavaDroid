package com.ccs.javadroid.project;

import com.ccs.javadroid.gradle.GradleBuildParser;
import com.ccs.javadroid.gradle.GradlePaths;
import com.ccs.javadroid.maven.MavenPaths;
import com.ccs.javadroid.maven.PomModel;
import com.ccs.javadroid.maven.PomParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tells Maven and Gradle projects apart and produces the single {@link PomModel}
 * the compile/run pipeline consumes for either one.
 */
public final class BuildSystem {

    public enum Kind {
        /** {@code pom.xml} present. */
        MAVEN,
        /** {@code build.gradle} or {@code build.gradle.kts} present. */
        GRADLE,
        /** Loose sources with no build script. */
        NONE
    }

    /** A resolved build model plus anything the parser could not represent. */
    public static final class Model {
        public final Kind kind;
        public final PomModel pom;
        public final List<String> warnings;

        Model(Kind kind, PomModel pom, List<String> warnings) {
            this.kind = kind;
            this.pom = pom;
            this.warnings = warnings;
        }
    }

    private BuildSystem() {}

    /** Maven wins when a directory somehow carries both build scripts. */
    public static Kind detect(File projectRoot) {
        if (projectRoot == null) return Kind.NONE;
        if (MavenPaths.pomFile(projectRoot).isFile()) return Kind.MAVEN;
        if (GradlePaths.isGradleProject(projectRoot)) return Kind.GRADLE;
        return Kind.NONE;
    }

    /** True when the project can be built — either build system counts. */
    public static boolean isBuildable(File projectRoot) {
        return detect(projectRoot) != Kind.NONE;
    }

    /** Human-readable name for console output and dialogs. */
    public static String displayName(Kind kind) {
        switch (kind) {
            case MAVEN:  return "Maven";
            case GRADLE: return "Gradle";
            default:     return "—";
        }
    }

    /** The build script driving this project, or {@code null} when there is none. */
    public static File buildScript(File projectRoot) {
        switch (detect(projectRoot)) {
            case MAVEN:  return MavenPaths.pomFile(projectRoot);
            case GRADLE: return GradlePaths.buildFile(projectRoot);
            default:     return null;
        }
    }

    /**
     * Reads the project's build model.
     *
     * @throws IOException when the project has no build script, or it is unreadable
     */
    public static Model model(File projectRoot) throws IOException {
        Kind kind = detect(projectRoot);
        switch (kind) {
            case MAVEN:
                try {
                    return new Model(kind, PomParser.parse(MavenPaths.pomFile(projectRoot)),
                            new ArrayList<>());
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    // Malformed XML — surface it as an IO failure so callers that
                    // already handle unreadable build scripts keep working.
                    throw new IOException("Cannot parse pom.xml: " + e.getMessage(), e);
                }
            case GRADLE: {
                GradleBuildParser.Result r = GradleBuildParser.parse(projectRoot);
                return new Model(kind, r.pom, r.warnings);
            }
            default:
                throw new IOException("No pom.xml or build.gradle in "
                        + (projectRoot != null ? projectRoot.getName() : "null"));
        }
    }
}
