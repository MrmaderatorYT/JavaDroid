package com.ccs.javadroid.ui;

import java.io.File;

public class FileTab {

    public final File file;
    public boolean isModified;
    /** Байтки .class файлу (для байткод-в'ювера); null для текстових файлів. */
    public byte[] classBytes;

    public FileTab(File file) {
        this.file = file;
        this.isModified = false;
    }

    /** Чи є ця вкладка .class-файлом (бінарним). */
    public boolean isClassFile() {
        return file != null && file.getName().endsWith(".class");
    }

    public String getDisplayName() {
        return isModified ? file.getName() + " \u25cf" : file.getName();
    }
}
