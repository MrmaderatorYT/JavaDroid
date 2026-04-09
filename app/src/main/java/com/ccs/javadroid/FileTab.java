package com.ccs.javadroid;

import java.io.File;

public class FileTab {

    public final File file;
    public boolean isModified;

    public FileTab(File file) {
        this.file = file;
        this.isModified = false;
    }

    public String getDisplayName() {
        return isModified ? file.getName() + " \u25cf" : file.getName();
    }
}
