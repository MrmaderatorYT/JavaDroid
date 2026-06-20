package com.ccs.javadroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Блок коду з обводкою, фоном та кнопкою Copy.
 */
public class CodeBlockView extends LinearLayout {

    private final TextView tvCode;
    private final TextView tvCopy;
    private final TextView tvLang;

    public CodeBlockView(Context context, String code, String lang) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);

        // Border wrapper
        setPadding(dp(1), dp(1), dp(1), dp(1));
        setBackgroundColor(0xFF555555); // border color

        LinearLayout inner = new LinearLayout(context);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setBackgroundColor(0xFF1E1E20);
        inner.setPadding(dp(12), dp(8), dp(12), dp(8));
        LinearLayout.LayoutParams innerLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inner.setLayoutParams(innerLp);

        // Header row: lang label + copy button
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        tvLang = new TextView(context);
        tvLang.setText(lang != null && !lang.isEmpty() ? lang.toUpperCase() : "CODE");
        tvLang.setTextColor(0xFF808080);
        tvLang.setTextSize(10);
        tvLang.setTypeface(Typeface.MONOSPACE);
        LinearLayout.LayoutParams langLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvLang.setLayoutParams(langLp);
        header.addView(tvLang);

        tvCopy = new TextView(context);
        tvCopy.setText(" Copy ");
        tvCopy.setTextColor(0xFF4A86C8);
        tvCopy.setTextSize(11);
        tvCopy.setPadding(dp(8), dp(2), dp(8), dp(2));
        tvCopy.setOnClickListener(v -> {
            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("code", code));
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
            }
        });
        header.addView(tvCopy);

        inner.addView(header);

        // Separator line
        View sep = new View(context);
        sep.setBackgroundColor(0xFF444444);
        inner.addView(sep, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        // Code text
        tvCode = new TextView(context);
        tvCode.setText(code);
        tvCode.setTextColor(0xFFBBBBBB);
        tvCode.setTextSize(12);
        tvCode.setTypeface(Typeface.MONOSPACE);
        tvCode.setLineSpacing(0, 1.3f);
        tvCode.setTextIsSelectable(false);
        tvCode.setFocusable(false);
        tvCode.setFocusableInTouchMode(false);
        tvCode.setCursorVisible(false);
        tvCode.setLongClickable(false);
        tvCode.setClickable(false);
        tvCode.setBackgroundColor(0x00000000);
        // Прибираємо сірий highlight при виділенні
        tvCode.setHighlightColor(0x00000000);
        tvCode.setSelected(false);
        LinearLayout.LayoutParams codeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        codeLp.topMargin = dp(4);
        tvCode.setLayoutParams(codeLp);
        inner.addView(tvCode);

        addView(inner);
    }

    /** Застосовує підсвітку синтаксису */
    public void setHighlighted(SpannableStringBuilder highlighted) {
        tvCode.setText(highlighted);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
