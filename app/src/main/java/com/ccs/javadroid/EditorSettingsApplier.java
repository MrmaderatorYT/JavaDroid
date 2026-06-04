package com.ccs.javadroid;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Застосовує користувацькі налаштування (шрифт, тема, поведінка) на {@link CodeEditor}.
 */
public final class EditorSettingsApplier {

    private EditorSettingsApplier() {}

    public static void apply(CodeEditor editor, AppPreferences prefs, AppTheme theme) {
        if (editor == null) return;
        editor.setColorScheme(theme.buildEditorScheme());
        editor.setTextSize(prefs.getFontSize());
        editor.setTypefaceText(prefs.resolveTypeface());
        editor.setTypefaceLineNumber(prefs.resolveTypeface());
        editor.setLineNumberEnabled(prefs.isLineNumbers());
        editor.setWordwrap(prefs.isWordWrap());
        editor.setTabWidth(prefs.getTabSize());
        editor.setLineSpacing(0f, prefs.getLineSpacing());
    }
}
