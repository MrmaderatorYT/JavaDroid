package com.ccs.javadroid;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Path utilities for Maven project layout:
 * {@code Documents/JavaDroid/<project>/src/main/java}, {@code target/classes}, etc.
 */
public final class MavenPaths {

    private MavenPaths() {}

    public static File getJavaDroidBase(Context context) {
        File doc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(doc, "JavaDroid");
    }

    public static File projectDir(Context context, String projectName) {
        return new File(getJavaDroidBase(context), projectName);
    }

    /** Local artifact cache: {@code .javadroid/repository/...} */
    public static File localRepoDir(File projectRoot) {
        return new File(projectRoot, ".javadroid/repository");
    }

    /** Maven-style local install repo: {@code .javadroid/local-repo/...} */
    public static File installRepoDir(File projectRoot) {
        return new File(projectRoot, ".javadroid/local-repo");
    }

    /** {@code target/} root */
    public static File targetDir(File projectRoot) {
        return new File(projectRoot, "target");
    }

    /** {@code target/classes} */
    public static File targetClassesDir(File projectRoot) {
        return new File(projectRoot, "target/classes");
    }

    /** {@code target/test-classes} */
    public static File targetTestClassesDir(File projectRoot) {
        return new File(projectRoot, "target/test-classes");
    }

    /** {@code pom.xml} */
    public static File pomFile(File projectRoot) {
        return new File(projectRoot, "pom.xml");
    }

    /** {@code src/main/java} */
    public static File mainJavaDir(File projectRoot) {
        return new File(projectRoot, "src/main/java");
    }

    /** {@code src/test/java} */
    public static File testJavaDir(File projectRoot) {
        return new File(projectRoot, "src/test/java");
    }

    /** {@code src/main/resources} */
    public static File mainResourcesDir(File projectRoot) {
        return new File(projectRoot, "src/main/resources");
    }

    /** {@code src/test/resources} */
    public static File testResourcesDir(File projectRoot) {
        return new File(projectRoot, "src/test/resources");
    }
}
