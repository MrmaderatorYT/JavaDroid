package com.ccs.javadroid.ui;

import java.io.File;

/**
 * Елемент дерева проєкту (як у IntelliJ IDEA): файл або каталог з глибиною для відступу.
 */
public class FileTreeNode {

    public final File path;
    public final int depth;
    public final boolean directory;

    public FileTreeNode(File path, int depth, boolean directory) {
        this.path = path;
        this.depth = depth;
        this.directory = directory;
    }

    /** Коротка назва (останній сегмент шляху). */
    public String shortName() {
        return path.getName();
    }
}
