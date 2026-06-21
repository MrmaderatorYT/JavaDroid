package com.ccs.javadroid.learn;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.R;

import java.util.List;

/**
 * Рендерить блоки уроку нативно (без WebView).
 * Кольори з активної теми; код підсвічується через {@link JavaSyntaxHighlighter}.
 */
public class LessonBlockAdapter extends RecyclerView.Adapter<LessonBlockAdapter.VH> {

    private List<LessonBlock> blocks;
    private AppTheme theme;

    public LessonBlockAdapter(List<LessonBlock> blocks, AppTheme theme) {
        this.blocks = blocks;
        this.theme = theme;
    }

    public void setBlocks(List<LessonBlock> blocks) {
        this.blocks = blocks;
        notifyDataSetChanged();
    }

    public void setTheme(AppTheme theme) {
        this.theme = theme;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return blocks == null ? 0 : blocks.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_block, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LessonBlock b = blocks.get(position);
        hideAll(h);

        switch (b.type) {
            case LessonBlock.HEADING:
                h.heading.setVisibility(View.VISIBLE);
                h.heading.setText(b.text);
                h.heading.setTextColor(theme != null ? theme.text : 0xFFA9B7C6);
                break;
            case LessonBlock.PARAGRAPH:
                h.paragraph.setVisibility(View.VISIBLE);
                h.paragraph.setText(b.text);
                h.paragraph.setTextColor(theme != null ? theme.text : 0xFFC9CEC9);
                break;
            case LessonBlock.CODE:
                h.codeScroll.setVisibility(View.VISIBLE);
                SpannableString ss = JavaSyntaxHighlighter.highlight(b.text, theme);
                h.code.setText(ss);
                // фон код-блоку: злегка затемнена консоль
                h.code.setBackgroundColor(theme != null
                        ? blend(theme.consoleBg, theme.bg, 0.5f)
                        : 0xFF2B2B2B);
                h.code.setTextColor(theme != null ? theme.consoleText : 0xFFA9B7C6);
                break;
            case LessonBlock.LIST:
                h.list.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (String item : b.text.split("\n")) {
                    sb.append("  •  ").append(item).append('\n');
                }
                if (sb.length() > 0) sb.setLength(sb.length() - 1);
                h.list.setText(sb.toString());
                h.list.setTextColor(theme != null ? theme.text : 0xFFC9CEC9);
                break;
            case LessonBlock.NOTE:
            case LessonBlock.WARNING:
                h.noteBox.setVisibility(View.VISIBLE);
                boolean warn = b.type == LessonBlock.WARNING;
                h.noteIcon.setText(warn ? "⚠️" : "💡");
                h.noteText.setText(b.text);
                int bg = warn
                        ? (0x55FFA500)
                        : (theme != null ? (theme.accent & 0x33FFFFFF) : 0x339876AA);
                h.noteBox.setBackgroundColor(bg);
                h.noteText.setTextColor(theme != null ? theme.text : 0xFFC9CEC9);
                break;
            case LessonBlock.TABLE:
                h.table.setVisibility(View.VISIBLE);
                StringBuilder tb = new StringBuilder();
                if (b.tableHeader != null) {
                    tb.append(b.tableHeader.replace("\t", "   ")).append('\n');
                    tb.append("─".repeat(40)).append('\n');
                }
                for (String row : b.text.split("\n")) {
                    tb.append(row.replace("\t", "   ")).append('\n');
                }
                if (tb.length() > 0) tb.setLength(tb.length() - 1);
                h.table.setText(tb.toString());
                h.table.setBackgroundColor(theme != null ? theme.consoleBg : 0xFF2B2B2B);
                h.table.setTextColor(theme != null ? theme.consoleText : 0xFFA9B7C6);
                break;
        }
    }

    private void hideAll(VH h) {
        h.heading.setVisibility(View.GONE);
        h.paragraph.setVisibility(View.GONE);
        h.codeScroll.setVisibility(View.GONE);
        h.list.setVisibility(View.GONE);
        h.noteBox.setVisibility(View.GONE);
        h.table.setVisibility(View.GONE);
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView heading;
        final TextView paragraph;
        final HorizontalScrollView codeScroll;
        final TextView code;
        final TextView list;
        final LinearLayout noteBox;
        final TextView noteIcon;
        final TextView noteText;
        final TextView table;

        VH(View v) {
            super(v);
            heading   = v.findViewById(R.id.blockHeading);
            paragraph = v.findViewById(R.id.blockParagraph);
            codeScroll= v.findViewById(R.id.blockCodeScroll);
            code      = v.findViewById(R.id.blockCode);
            list      = v.findViewById(R.id.blockList);
            noteBox   = v.findViewById(R.id.blockNoteBox);
            noteIcon  = v.findViewById(R.id.blockNoteIcon);
            noteText  = v.findViewById(R.id.blockNoteText);
            table     = v.findViewById(R.id.blockTable);
        }
    }
}
