package com.ccs.javadroid.learn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.javadroid.AppTheme;
import com.ccs.javadroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Список глав/уроків курсу. Глава — розгортуваний заголовок; урок — пункт.
 * Логіка розкриття аналогічна старому LearnAdapter, але під нові моделі.
 */
public class CourseOutlineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    private static final int TYPE_CHAPTER = 0;
    private static final int TYPE_LESSON = 1;

    private static final class ChapterItem {
        final Chapter chapter;
        boolean expanded;
        ChapterItem(Chapter c) { chapter = c; }
    }

    private final List<Object> flat = new ArrayList<>();
    private final List<ChapterItem> chapters = new ArrayList<>();
    private final AppTheme theme;
    private final OnLessonClickListener listener;
    private final int lang;

    public CourseOutlineAdapter(List<Chapter> chapterList, AppTheme theme, int lang,
                                OnLessonClickListener listener) {
        this.theme = theme;
        this.lang = lang;
        this.listener = listener;
        for (Chapter c : chapterList) chapters.add(new ChapterItem(c));
        rebuild();
    }

    private void rebuild() {
        flat.clear();
        for (ChapterItem ci : chapters) {
            flat.add(ci);
            if (ci.expanded) flat.addAll(ci.chapter.lessons);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return flat.get(position) instanceof ChapterItem ? TYPE_CHAPTER : TYPE_LESSON;
    }

    @Override
    public int getItemCount() {
        return flat.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View v = viewType == TYPE_CHAPTER
                ? inf.inflate(R.layout.item_chapter, parent, false)
                : inf.inflate(R.layout.item_lesson, parent, false);
        return new VH(v, viewType == TYPE_CHAPTER);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        VH vh = (VH) h;
        Object item = flat.get(position);
        if (item instanceof ChapterItem) {
            ChapterItem ci = (ChapterItem) item;
            vh.title.setText(ci.chapter.title(lang));
            vh.title.setTextColor(theme.text);
            if (vh.arrow != null) {
                vh.arrow.setVisibility(View.VISIBLE);
                vh.arrow.setText(ci.expanded ? "▲" : "▼");
                vh.arrow.setTextColor(theme.textDim);
            }
            vh.itemView.setBackgroundColor(theme.toolbar);
            vh.itemView.setOnClickListener(v -> {
                ci.expanded = !ci.expanded;
                rebuild();
                notifyDataSetChanged();
            });
        } else {
            Lesson l = (Lesson) item;
            vh.title.setText("   " + l.title(lang));
            vh.title.setTextColor(theme.textDim);
            vh.itemView.setBackgroundColor(theme.bg);
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onLessonClick(l);
            });
        }
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView arrow;
        VH(View v, boolean isChapter) {
            super(v);
            title = v.findViewById(isChapter ? R.id.tvChapterTitle : R.id.tvLessonTitle);
            arrow = isChapter ? (TextView) v.findViewById(R.id.tvChapterArrow) : null;
        }
    }
}
