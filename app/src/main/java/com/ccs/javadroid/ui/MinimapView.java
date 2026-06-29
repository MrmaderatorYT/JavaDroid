package com.ccs.javadroid.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.rosemoe.sora.event.ScrollEvent;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;

public class MinimapView extends View {

    private CodeEditor editor;
    private final Paint bgPaint = new Paint();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint vpBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint vpBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cursorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint currentLineBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF viewportRect = new RectF();

    private float totalContentHeight = 0;
    private int totalLines = 0;
    private int firstVisibleRow = 0;
    private int lastVisibleRow = 0;
    private float scrollY = 0;
    private float editorContentHeight = 1;
    private int cursorLine = 0;
    private boolean isDragging = false;

    private int colorText;
    private int colorKeyword;
    private int colorString;
    private int colorComment;
    private int colorNumber;
    private int colorAccent;

    private static final float VIEWPORT_RADIUS = 3f;

    public MinimapView(Context context) {
        super(context);
        init();
    }

    public MinimapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MinimapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        bgPaint.setColor(0xCC1E1E1E);
        bgPaint.setStyle(Paint.Style.FILL);

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setStrokeCap(Paint.Cap.ROUND);

        vpBgPaint.setColor(0x28FFFFFF);
        vpBgPaint.setStyle(Paint.Style.FILL);

        vpBorderPaint.setColor(0x50FFFFFF);
        vpBorderPaint.setStyle(Paint.Style.STROKE);
        vpBorderPaint.setStrokeWidth(1.5f);

        cursorLinePaint.setColor(0x60FFD700);
        cursorLinePaint.setStyle(Paint.Style.FILL);

        currentLineBg.setColor(0x15FFFFFF);
        currentLineBg.setStyle(Paint.Style.FILL);

        colorText = 0xFFBBBBBB;
        colorKeyword = 0xFF569CD6;
        colorString = 0xFF6A9955;
        colorComment = 0xFF608B4E;
        colorNumber = 0xFFB5CEA8;
        colorAccent = 0xFFDCDCAA;
    }

    public void setEditor(CodeEditor editor) {
        this.editor = editor;
        if (editor != null) {
            editor.subscribeEvent(ScrollEvent.class, (event, subs) -> {
                post(this::invalidate);
            });
        }
    }

    public void setThemeColors(int bg, int text, int keyword, int string, int comment,
                                int number, int accent, int vpBg, int vpBdr) {
        this.colorText = text;
        this.colorKeyword = keyword;
        this.colorString = string;
        this.colorComment = comment;
        this.colorNumber = number;
        this.colorAccent = accent;
        bgPaint.setColor(bg);
        vpBgPaint.setColor(vpBg);
        vpBorderPaint.setColor(vpBdr);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (editor == null) return;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawRect(0, 0, w, h, bgPaint);

        Content text = editor.getText();
        if (text == null) return;

        totalLines = text.getLineCount();
        if (totalLines == 0) return;

        float editorH = editor.getHeight();
        float rowH = editor.getRowHeight();
        if (rowH <= 0) rowH = 14f;
        editorContentHeight = totalLines * rowH;

        firstVisibleRow = editor.getFirstVisibleRow();
        lastVisibleRow = editor.getLastVisibleRow();
        scrollY = editor.getScrollY();
        cursorLine = editor.getCursor().getLeftLine();

        float scale = h / editorContentHeight;
        float lineH = Math.max(1.5f, rowH * scale);
        float padding = 2f;
        float maxBarWidth = w - padding * 2;
        float indentUnit = maxBarWidth / 100f;

        // Current line highlight
        float clY = (cursorLine * rowH / editorContentHeight) * h;
        canvas.drawRect(0, clY - lineH * 0.3f, w, clY + lineH * 1.3f, currentLineBg);

        // Code bars
        for (int i = 0; i < totalLines; i++) {
            float y = i * lineH;
            if (y > h) break;

            String line;
            try {
                CharSequence cs = text.getLine(i);
                line = cs != null ? cs.toString() : "";
            } catch (Exception e) {
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            int indent = getIndentLevel(line);
            int lineLen = trimmed.length();

            float barWidth;
            if (lineLen < 3) {
                barWidth = indentUnit * 2;
            } else {
                barWidth = Math.min(maxBarWidth - indent * indentUnit,
                        Math.max(indentUnit * 1.5f, lineLen * indentUnit * 0.55f));
            }
            if (barWidth < 1.5f) barWidth = 1.5f;

            float barX = padding + indent * indentUnit;
            float barY = y + lineH * 0.15f;
            float barH = Math.max(1.2f, lineH * 0.65f);

            barPaint.setColor(classifyLine(trimmed));
            barPaint.setAlpha(isInRange(i) ? 220 : 100);

            canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barH, 1f, 1f, barPaint);
        }

        // Viewport
        float vpTop = Math.max(0, (scrollY / editorContentHeight) * h);
        float vpHeight = Math.max(4, (editorH / editorContentHeight) * h);
        viewportRect.set(0, vpTop, w, vpTop + vpHeight);
        canvas.drawRoundRect(viewportRect, VIEWPORT_RADIUS, VIEWPORT_RADIUS, vpBgPaint);
        canvas.drawRoundRect(viewportRect, VIEWPORT_RADIUS, VIEWPORT_RADIUS, vpBorderPaint);

        // Cursor line
        float cursorY = (cursorLine * rowH / editorContentHeight) * h;
        canvas.drawRect(0, cursorY, w, Math.min(cursorY + 1.5f, h), cursorLinePaint);
    }

    private boolean isInRange(int line) {
        return line >= firstVisibleRow && line <= lastVisibleRow;
    }

    private int classifyLine(String line) {
        if (line.startsWith("//") || line.startsWith("*") || line.startsWith("/*") || line.startsWith("*/")) {
            return colorComment;
        }
        if (line.startsWith("@")) return colorAccent;
        if (line.startsWith("import ") || line.startsWith("package ")) return colorKeyword;
        if (line.startsWith("return ") || line.startsWith("throw ")) return colorKeyword;
        if (line.startsWith("if ") || line.startsWith("else ") || line.startsWith("for ")
                || line.startsWith("while ") || line.startsWith("try ") || line.startsWith("catch ")
                || line.startsWith("switch ") || line.startsWith("case ") || line.startsWith("break")) {
            return colorKeyword;
        }
        if (line.startsWith("public ") || line.startsWith("private ") || line.startsWith("protected ")
                || line.startsWith("static ") || line.startsWith("final ") || line.startsWith("abstract ")
                || line.startsWith("class ") || line.startsWith("interface ") || line.startsWith("enum ")
                || line.startsWith("void ") || line.startsWith("new ")) {
            return colorKeyword;
        }
        if (line.contains("\"") || line.contains("'")) return colorString;
        if (line.matches("^\\s*\\d+.*")) return colorNumber;
        if (line.contains("(") && line.contains(")")) return colorAccent;
        return colorText;
    }

    private int getIndentLevel(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') count++;
            else if (c == '\t') count += 4;
            else break;
        }
        return count / 4;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (editor == null) return false;

        float y = event.getY();
        int h = getHeight();
        if (h <= 0) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = true;
                scrollToPosition(y);
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    scrollToPosition(y);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void scrollToPosition(float minimapY) {
        if (editor == null) return;
        int h = getHeight();
        if (h <= 0) return;

        float ratio = minimapY / h;
        int totalRows = editor.getText().getLineCount();
        int targetRow = (int) (ratio * totalRows);
        targetRow = Math.max(0, Math.min(targetRow, totalRows - 1));

        float rowH = editor.getRowHeight();
        int targetScroll = (int) (targetRow * rowH - editor.getHeight() / 2f);
        targetScroll = Math.max(0, targetScroll);

        editor.scrollBy(0, targetScroll - (int) editor.getScrollY());
    }
}
