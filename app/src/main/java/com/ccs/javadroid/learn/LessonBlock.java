package com.ccs.javadroid.learn;

import java.util.List;

/**
 * Один блок контенту уроку. {@code type} визначає як рендерер його показує.
 * Замість HTML/WebView — нативні блоки, швидко й компактно.
 */
public final class LessonBlock {

    public static final int HEADING   = 0;  // підрозділ уроку (h2)
    public static final int PARAGRAPH = 1;  // звичайний текст
    public static final int CODE      = 2;  // Java-код з підсвіткою
    public static final int LIST      = 3;  // маркований список
    public static final int NOTE      = 4;  // інформаційна рамка
    public static final int WARNING   = 5;  // попереджувальна рамка
    public static final int TABLE     = 6;  // таблиця (header + rows)

    public final int type;
    /** Для HEADING/PARAGRAPH/CODE/NOTE/WARNING — основний текст.
     *  Для LIST — рядки розділені \n.
     *  Для TABLE — рядки розділені \n, колонки \t. */
    public final String text;
    /** Для TABLE — рядок заголовків з колонками розділеними \t. */
    public final String tableHeader;

    private LessonBlock(int type, String text, String tableHeader) {
        this.type = type;
        this.text = text;
        this.tableHeader = tableHeader;
    }

    // ── Фабричні методи ────────────────────────────────────────────────────

    public static LessonBlock heading(String text) {
        return new LessonBlock(HEADING, text, null);
    }

    public static LessonBlock paragraph(String text) {
        return new LessonBlock(PARAGRAPH, text, null);
    }

    public static LessonBlock code(String code) {
        return new LessonBlock(CODE, code, null);
    }

    public static LessonBlock list(List<String> items) {
        return new LessonBlock(LIST, String.join("\n", items), null);
    }

    public static LessonBlock list(String... items) {
        return new LessonBlock(LIST, String.join("\n", items), null);
    }

    public static LessonBlock note(String text) {
        return new LessonBlock(NOTE, text, null);
    }

    public static LessonBlock warning(String text) {
        return new LessonBlock(WARNING, text, null);
    }

    /**
     * Таблиця. {@code header} — колонки через табуляцію,
     * {@code rows} — по рядку, колонки через табуляцію.
     */
    public static LessonBlock table(String header, List<String> rows) {
        return new LessonBlock(TABLE, String.join("\n", rows), header);
    }
}
