package com.ccs.javadroid;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Базова папка: {@code Documents/JavaDroid} у зовнішньому сховищі застосунку
 * (доступна через файловий менеджер: Android/data/.../files/Documents/JavaDroid).
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

    public static File localRepoDir(File projectRoot) {
        return new File(projectRoot, ".javadroid/repository");
    }

    public static File targetClassesDir(File projectRoot) {
        return new File(projectRoot, "target/classes");
    }

    public static File targetTestClassesDir(File projectRoot) {
        return new File(projectRoot, "target/test-classes");
    }

    public static File pomFile(File projectRoot) {
        return new File(projectRoot, "pom.xml");
    }

    public static File mainJavaDir(File projectRoot) {
        return new File(projectRoot, "src/main/java");
    }

    public static File testJavaDir(File projectRoot) {
        return new File(projectRoot, "src/test/java");
    }
}
