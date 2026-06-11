package com.ccs.javadroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LearnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnLessonClickListener {
        void onLessonSelect(LearnActivity.Chapter chapter, LearnActivity.Lesson lesson);
    }

    private static final int TYPE_CHAPTER = 0;
    private static final int TYPE_LESSON = 1;

    private final List<Object> items = new ArrayList<>();
    private final List<LearnActivity.Chapter> chapters;
    private final AppTheme theme;
    private final OnLessonClickListener listener;

    public LearnAdapter(List<LearnActivity.Chapter> chapters, AppTheme theme, OnLessonClickListener listener) {
        this.chapters = chapters;
        this.theme = theme;
        this.listener = listener;
        flattenList();
    }

    private void flattenList() {
        items.clear();
        for (LearnActivity.Chapter chapter : chapters) {
            items.add(chapter);
            if (chapter.isExpanded) {
                items.addAll(chapter.lessons);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof LearnActivity.Chapter) ? TYPE_CHAPTER : TYPE_LESSON;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CHAPTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
            return new ChapterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
            return new LessonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CHAPTER) {
            ChapterViewHolder chHolder = (ChapterViewHolder) holder;
            LearnActivity.Chapter chapter = (LearnActivity.Chapter) items.get(position);
            chHolder.title.setText(chapter.title);
            chHolder.title.setTextColor(theme.text);
            chHolder.arrow.setText(chapter.isExpanded ? "▲" : "▼");
            chHolder.arrow.setTextColor(theme.textDim);
            chHolder.itemView.setBackgroundColor(theme.toolbar);

            chHolder.itemView.setOnClickListener(v -> {
                chapter.isExpanded = !chapter.isExpanded;
                flattenList();
                notifyDataSetChanged();
            });
        } else {
            LessonViewHolder lesHolder = (LessonViewHolder) holder;
            LearnActivity.Lesson lesson = (LearnActivity.Lesson) items.get(position);
            lesHolder.title.setText(lesson.title);
            lesHolder.title.setTextColor(theme.textDim);
            lesHolder.itemView.setBackgroundColor(theme.bg);

            final LearnActivity.Chapter parentChapter = findParentChapter(lesson);
            lesHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonSelect(parentChapter, lesson);
                }
            });
        }
    }

    private LearnActivity.Chapter findParentChapter(LearnActivity.Lesson lesson) {
        for (LearnActivity.Chapter chapter : chapters) {
            if (chapter.lessons.contains(lesson)) return chapter;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView arrow;

        ChapterViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvChapterTitle);
            arrow = view.findViewById(R.id.tvChapterArrow);
        }
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        final TextView title;

        LessonViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvLessonTitle);
        }
    }
}
