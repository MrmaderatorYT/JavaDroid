package com.ccs.javadroid;

import java.io.File;

public class ProblemItem {

    public enum Severity { ERROR, WARNING, INFO }

    public final Severity severity;
    public final String message;
    public final File file;
    public final int line;

    public ProblemItem(Severity severity, String message, File file, int line) {
        this.severity = severity;
        this.message = message;
        this.file = file;
        this.line = line;
    }
}
