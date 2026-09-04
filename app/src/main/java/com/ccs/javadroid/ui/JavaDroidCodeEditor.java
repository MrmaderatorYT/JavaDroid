package com.ccs.javadroid.ui;

import android.content.Context;
import android.util.AttributeSet;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentLine;
import io.github.rosemoe.sora.text.Cursor;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

/**
 * The editor with typing over a closing bracket added.
 *
 * <p>sora auto-inserts the closing half of a pair when the opening one is typed,
 * but typing the closing one yourself inserts a second copy: {@code (} gives
 * {@code (|)}, and the {@code )} that follows naturally gives {@code ()|)}. Every
 * IDE steps over the one already there instead, which is what this adds — for
 * the soft keyboard, a hardware keyboard and the symbol bar alike, since all
 * three end up in one of the two methods below.</p>
 *
 * <p>The rule is "the character to the right is the same closing character",
 * not "we put it there": sora keeps no record of which brackets it inserted, and
 * a heuristic that matches what the user sees beats one that needs bookkeeping
 * the editor does not have. Only closings of pairs the current language actually
 * declares take part, so a language with no pairs types exactly as before.</p>
 */
public class JavaDroidCodeEditor extends CodeEditor {

    /** Openings whose closings may be typed over; quotes included, both halves equal. */
    private static final char[] PAIR_OPENINGS = { '(', '[', '{', '"', '\'' };

    public JavaDroidCodeEditor(Context context) {
        super(context);
    }

    public JavaDroidCodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JavaDroidCodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public JavaDroidCodeEditor(Context context, AttributeSet attrs, int defStyleAttr,
                               int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /** Everything the IME commits arrives here, including single typed characters. */
    @Override
    public void commitText(CharSequence text, boolean applyAutoIndent, boolean simulateKeys) {
        if (steppedOver(text)) return;
        super.commitText(text, applyAutoIndent, simulateKeys);
    }

    /** The symbol bar's route in; it writes to the buffer without going through commitText. */
    @Override
    public void insertText(String text, int selectionOffset) {
        if (steppedOver(text)) return;
        super.insertText(text, selectionOffset);
    }

    /**
     * Moves the caret past an identical closing character instead of inserting.
     *
     * @return true when the caret was moved and nothing should be inserted
     */
    private boolean steppedOver(CharSequence text) {
        if (text == null || text.length() != 1 || !isEditable()) return false;
        char typed = text.charAt(0);
        if (!closesAPair(typed)) return false;

        Content content = getText();
        if (content == null) return false;
        Cursor cursor = content.getCursor();
        // With a selection the character typed replaces it, which is an insert
        // by any reading — stepping over would silently drop the selected text.
        if (cursor == null || cursor.isSelected()) return false;

        int line = cursor.getLeftLine();
        int column = cursor.getLeftColumn();
        if (line < 0 || line >= content.getLineCount()) return false;
        ContentLine row = content.getLine(line);
        if (row == null || column < 0 || column >= row.length()) return false;
        if (row.charAt(column) != typed) return false;

        setSelection(line, column + 1);
        return true;
    }

    /** Whether the current language declares a pair that ends with this character. */
    private boolean closesAPair(char typed) {
        Language language = getEditorLanguage();
        if (language == null) return false;
        SymbolPairMatch pairs = language.getSymbolPairs();
        if (pairs == null) return false;
        for (char opening : PAIR_OPENINGS) {
            SymbolPairMatch.SymbolPair pair = pairs.matchBestPairBySingleChar(opening);
            if (pair == null || pair.close == null || pair.close.length() != 1) continue;
            if (pair.close.charAt(0) == typed) return true;
        }
        return false;
    }
}
