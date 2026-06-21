package com.ccs.javadroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

/**
 * Централізований доступ до користувацьких налаштувань JavaDroid.
 * Зберігає шрифти, тему, поведінку редактора та параметри компілятора.
 */
    public final class AppPreferences {

    public static final String PREFS_NAME = "com.ccs.javadroid.prefs";

    // ── Editor ────────────────────────────────────────────────
    private static final String K_FONT_SIZE        = "font_size";
    private static final String K_FONT_FAMILY      = "font_family";
    private static final String K_TAB_SIZE         = "tab_size";
    private static final String K_LINE_NUMBERS     = "line_numbers";
    private static final String K_WORD_WRAP        = "word_wrap";
    private static final String K_LINE_SPACING_X10 = "line_spacing_x10"; // зберігаємо як ціле *10
    private static final String K_AUTO_SAVE        = "auto_save";
    private static final String K_FORMAT_ON_SAVE   = "format_on_save";

    // ── Theme ─────────────────────────────────────────────────
    private static final String K_THEME_ID         = "theme_id";
    private static final String K_CUSTOM_BG        = "custom_bg";
    private static final String K_CUSTOM_FG        = "custom_fg";
    private static final String K_CUSTOM_ACCENT    = "custom_accent";
    private static final String K_CUSTOM_TOOLBAR   = "custom_toolbar";
    private static final String K_CUSTOM_CONSOLE_BG= "custom_console_bg";
    private static final String K_CUSTOM_KEYWORD   = "custom_keyword";
    private static final String K_CUSTOM_STRING    = "custom_string";
    private static final String K_CUSTOM_COMMENT   = "custom_comment";

    // ── Compiler ──────────────────────────────────────────────
    private static final String K_JAVA_TARGET      = "java_target";

    // ── Misc ──────────────────────────────────────────────────
    private static final String K_PROJECT_ROOT     = "project_root";
    private static final String K_FILE_ENCODING    = "file_encoding";
    private static final String K_VERBOSE_LOGGING  = "verbose_logging";
    // ── Power Saving ─────────────────────────────────────────
    private static final String K_POWER_SAVING_MODE = "power_saving_mode";

    // Family constants
    public static final int FONT_MONOSPACE     = 0;
    public static final int FONT_SANS          = 1;
    public static final int FONT_SERIF         = 2;
    public static final int FONT_DEFAULT       = 3;
    public static final int FONT_JETBRAINS     = 4;
    public static final int FONT_FIRA_CODE     = 5;
    public static final int FONT_SOURCE_CODE   = 6;
    public static final int FONT_DEJAVU_MONO   = 7;
    public static final int FONT_ROBOTO_MONO   = 8;

    // Java target constants (передаємо як ECJ -1.8 / -11)
    public static final String JAVA_8  = "1.8";

    private final SharedPreferences prefs;
    private final Context context;

    public AppPreferences(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.prefs = this.context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public SharedPreferences raw() {
        return prefs;
    }

    // ── Editor ────────────────────────────────────────────────

    public int getFontSize()              { return prefs.getInt(K_FONT_SIZE, 14); }
    public void setFontSize(int v)        { prefs.edit().putInt(K_FONT_SIZE, v).apply(); }

    public int getFontFamily()            { return prefs.getInt(K_FONT_FAMILY, FONT_MONOSPACE); }
    public void setFontFamily(int v)      { prefs.edit().putInt(K_FONT_FAMILY, v).apply(); }

    public int getTabSize()               { return prefs.getInt(K_TAB_SIZE, 4); }
    public void setTabSize(int v)         { prefs.edit().putInt(K_TAB_SIZE, v).apply(); }

    public boolean isLineNumbers()        { return prefs.getBoolean(K_LINE_NUMBERS, true); }
    public void setLineNumbers(boolean v) { prefs.edit().putBoolean(K_LINE_NUMBERS, v).apply(); }

    public boolean isWordWrap()           { return prefs.getBoolean(K_WORD_WRAP, false); }
    public void setWordWrap(boolean v)    { prefs.edit().putBoolean(K_WORD_WRAP, v).apply(); }

    /** Повертає множник міжрядкового інтервалу (1.0 .. 2.0). */
    public float getLineSpacing() {
        int x10 = prefs.getInt(K_LINE_SPACING_X10, 12); // 1.2 за замовч.
        return x10 / 10f;
    }
    public void setLineSpacing(float v)   { prefs.edit().putInt(K_LINE_SPACING_X10, Math.round(v * 10)).apply(); }

    public boolean isAutoSave()           { return prefs.getBoolean(K_AUTO_SAVE, false); }
    public void setAutoSave(boolean v)    { prefs.edit().putBoolean(K_AUTO_SAVE, v).apply(); }

    public boolean isFormatOnSave()       { return prefs.getBoolean(K_FORMAT_ON_SAVE, false); }
    public void setFormatOnSave(boolean v) { prefs.edit().putBoolean(K_FORMAT_ON_SAVE, v).apply(); }

    // ── Theme ─────────────────────────────────────────────────

    public String getThemeId()            { return prefs.getString(K_THEME_ID, AppTheme.ID_DARCULA); }
    public void setThemeId(String v)      { prefs.edit().putString(K_THEME_ID, v).apply(); }

    public int getCustomBg()              { return prefs.getInt(K_CUSTOM_BG, 0xFF2B2B2B); }
    public void setCustomBg(int v)        { prefs.edit().putInt(K_CUSTOM_BG, v).apply(); }

    public int getCustomFg()              { return prefs.getInt(K_CUSTOM_FG, 0xFFBBBBBB); }
    public void setCustomFg(int v)        { prefs.edit().putInt(K_CUSTOM_FG, v).apply(); }

    public int getCustomAccent()          { return prefs.getInt(K_CUSTOM_ACCENT, 0xFF4A86C8); }
    public void setCustomAccent(int v)    { prefs.edit().putInt(K_CUSTOM_ACCENT, v).apply(); }

    public int getCustomToolbar()         { return prefs.getInt(K_CUSTOM_TOOLBAR, 0xFF3C3F41); }
    public void setCustomToolbar(int v)   { prefs.edit().putInt(K_CUSTOM_TOOLBAR, v).apply(); }

    public int getCustomConsoleBg()       { return prefs.getInt(K_CUSTOM_CONSOLE_BG, 0xFF1E1E1E); }
    public void setCustomConsoleBg(int v) { prefs.edit().putInt(K_CUSTOM_CONSOLE_BG, v).apply(); }

    public int getCustomKeyword()         { return prefs.getInt(K_CUSTOM_KEYWORD, 0xFFCC7832); }
    public void setCustomKeyword(int v)   { prefs.edit().putInt(K_CUSTOM_KEYWORD, v).apply(); }

    public int getCustomString()          { return prefs.getInt(K_CUSTOM_STRING, 0xFF6A8759); }
    public void setCustomString(int v)    { prefs.edit().putInt(K_CUSTOM_STRING, v).apply(); }

    public int getCustomComment()         { return prefs.getInt(K_CUSTOM_COMMENT, 0xFF808080); }
    public void setCustomComment(int v)   { prefs.edit().putInt(K_CUSTOM_COMMENT, v).apply(); }

    // ── Compiler ──────────────────────────────────────────────

    public String getJavaTarget()         { return prefs.getString(K_JAVA_TARGET, JAVA_8); }
    public void setJavaTarget(String v)   { prefs.edit().putString(K_JAVA_TARGET, v).apply(); }

    // ── Misc ──────────────────────────────────────────────────

    public String getProjectRoot()        { return prefs.getString(K_PROJECT_ROOT, null); }
    public void setProjectRoot(String v)  { prefs.edit().putString(K_PROJECT_ROOT, v).apply(); }

    public String getFileEncoding()       { return prefs.getString(K_FILE_ENCODING, "UTF-8"); }
    public void setFileEncoding(String v) { prefs.edit().putString(K_FILE_ENCODING, v).apply(); }

    public boolean isVerboseLoggingEnabled() { return prefs.getBoolean(K_VERBOSE_LOGGING, true); }
    public void setVerboseLoggingEnabled(boolean v) { prefs.edit().putBoolean(K_VERBOSE_LOGGING, v).apply(); }

    public int getPowerSavingMode()          { return prefs.getInt(K_POWER_SAVING_MODE, 0); }
    public void setPowerSavingMode(int v)    { prefs.edit().putInt(K_POWER_SAVING_MODE, v).apply(); }

    public java.util.List<String> getRecentProjects() {
        String data = prefs.getString("recent_projects", "");
        if (data.isEmpty()) return new java.util.ArrayList<>();
        return new java.util.ArrayList<>(java.util.Arrays.asList(data.split("\\|")));
    }

    public void addRecentProject(String path) {
        if (path == null || path.isEmpty()) return;
        java.util.List<String> list = getRecentProjects();
        list.remove(path); // remove if exists to move to top
        list.add(0, path); // insert at top
        if (list.size() > 20) {
            list = list.subList(0, 20);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(list.get(i));
        }
        prefs.edit().putString("recent_projects", sb.toString()).apply();
    }

    public void removeRecentProject(String path) {
        java.util.List<String> list = getRecentProjects();
        if (list.remove(path)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append("|");
                sb.append(list.get(i));
            }
            prefs.edit().putString("recent_projects", sb.toString()).apply();
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    public Typeface resolveTypeface() {
        switch (getFontFamily()) {
            case FONT_SANS:        return Typeface.SANS_SERIF;
            case FONT_SERIF:       return Typeface.SERIF;
            case FONT_DEFAULT:     return Typeface.DEFAULT;
            case FONT_JETBRAINS:   return loadAssetFont("fonts/JetBrainsMono.ttf");
            case FONT_FIRA_CODE:   return loadAssetFont("fonts/FiraCode.ttf");
            case FONT_SOURCE_CODE: return loadAssetFont("fonts/SourceCodePro.ttf");
            case FONT_DEJAVU_MONO: return loadAssetFont("fonts/DejaVuSansMono.ttf");
            case FONT_ROBOTO_MONO: return loadAssetFont("fonts/RobotoMono.ttf");
            case FONT_MONOSPACE:
            default:               return Typeface.MONOSPACE;
        }
    }

    private Typeface loadAssetFont(String path) {
        try {
            return Typeface.createFromAsset(context.getAssets(), path);
        } catch (Exception e) {
            return Typeface.MONOSPACE;
        }
    }
}
