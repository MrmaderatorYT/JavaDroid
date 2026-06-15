package com.ccs.javadroid.learn;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.javadroid.AppPreferences;
import com.ccs.javadroid.AppTheme;
import com.ccs.javadroid.R;

import java.util.List;

/**
 * Показує один урок обраного курсу нативно (без WebView): заголовок + навігація
 * далі/назад + список блоків через {@link LessonBlockAdapter}.
 */
public class LessonActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "course_id";
    public static final String EXTRA_LESSON = "lesson_id";

    private AppPreferences appPrefs;
    private AppTheme theme;

    private LessonBlockAdapter adapter;
    private String courseId;
    private String lessonId;
    private List<Lesson> lessons;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appPrefs = new AppPreferences(this);
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        courseId = getIntent().getStringExtra(EXTRA_COURSE);
        lessonId = getIntent().getStringExtra(EXTRA_LESSON);

        TextView tvTitle = findViewById(R.id.tvLessonTitle);
        RecyclerView rv = findViewById(R.id.rvLessonBlocks);
        View btnBack = findViewById(R.id.btnLessonBack);
        View btnPrev = findViewById(R.id.btnLessonPrev);
        View btnNext = findViewById(R.id.btnLessonNext);

        rv.setBackgroundColor(theme.bg);
        findViewById(android.R.id.content).setBackgroundColor(theme.bg);

        Course course = CourseRegistry.getInstance().getCourse(courseId);
        if (course == null) { Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show(); finish(); return; }
        lessons = course.allLessons();

        // знайти поточний урок
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).id.equals(lessonId)) { currentIndex = i; break; }
        }
        if (currentIndex >= lessons.size()) currentIndex = 0;

        int lang = CourseRegistry.getInstance().getLanguage();
        Lesson lesson = lessons.get(currentIndex);
        tvTitle.setText(lesson.title(lang));
        tvTitle.setTextColor(theme.text);

        adapter = new LessonBlockAdapter(lesson.content(lang), theme);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnPrev.setVisibility(currentIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(currentIndex < lessons.size() - 1 ? View.VISIBLE : View.INVISIBLE);
        btnPrev.setOnClickListener(v -> navigate(-1));
        btnNext.setOnClickListener(v -> navigate(1));
    }

    private void navigate(int delta) {
        int idx = currentIndex + delta;
        if (idx < 0 || idx >= lessons.size()) return;
        currentIndex = idx;
        int lang = CourseRegistry.getInstance().getLanguage();
        Lesson lesson = lessons.get(currentIndex);
        ((TextView) findViewById(R.id.tvLessonTitle)).setText(lesson.title(lang));
        adapter.setBlocks(lesson.content(lang));
        RecyclerView rv = findViewById(R.id.rvLessonBlocks);
        if (rv.getLayoutManager() != null) rv.getLayoutManager().scrollToPosition(0);
        findViewById(R.id.btnLessonPrev).setVisibility(idx > 0 ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.btnLessonNext).setVisibility(idx < lessons.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }
}
