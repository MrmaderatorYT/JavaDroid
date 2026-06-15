package com.ccs.javadroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.javadroid.learn.Course;
import com.ccs.javadroid.learn.CourseOutlineAdapter;
import com.ccs.javadroid.learn.CourseRegistry;
import com.ccs.javadroid.learn.LessonActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Навчальний центр: список курсів (Spinner) з перемикачем uk/en.
 * Використовує нативні моделі {@link CourseRegistry} (без HTML/WebView).
 */
public class LearnActivity extends AppCompatActivity {

    private AppPreferences appPrefs;
    private AppTheme theme;
    private RecyclerView rvChapters;
    private Spinner spinnerCourse;
    private TextView btnLanguage;
    private TextView tvDescription;
    private ArrayAdapter<String> courseAdapter;

    private List<Course> courses;
    private int currentCourseIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appPrefs = new AppPreferences(this);
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        Toolbar toolbar = findViewById(R.id.learnToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setBackgroundColor(theme.toolbar);

        spinnerCourse = findViewById(R.id.spinnerCourse);
        btnLanguage = findViewById(R.id.btnLanguage);
        tvDescription = findViewById(R.id.tvCourseDescription);
        rvChapters = findViewById(R.id.rvChapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setBackgroundColor(theme.bg);
        findViewById(android.R.id.content).setBackgroundColor(theme.bg);

        courses = CourseRegistry.getInstance().getCourses();

        // Spinner курсів
        List<String> titles = new ArrayList<>();
        int lang = CourseRegistry.getInstance().getLanguage();
        for (Course c : courses) titles.add(c.title(lang));
        courseAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, titles);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);
        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                currentCourseIndex = pos;
                showCourse();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Перемикач мови
        updateLanguageButton();
        btnLanguage.setOnClickListener(v -> {
            CourseRegistry reg = CourseRegistry.getInstance();
            reg.setLanguage(reg.isEnglish() ? CourseRegistry.LANG_UK : CourseRegistry.LANG_EN);
            updateLanguageButton();
            rebuildSpinnerTitles();
            showCourse();
        });
    }

    private void updateLanguageButton() {
        btnLanguage.setText(CourseRegistry.getInstance().isEnglish() ? "EN" : "UK");
        btnLanguage.setTextColor(theme.accent);
    }

    private void rebuildSpinnerTitles() {
        int lang = CourseRegistry.getInstance().getLanguage();
        courseAdapter.clear();
        for (Course c : courses) courseAdapter.add(c.title(lang));
        courseAdapter.notifyDataSetChanged();
        if (currentCourseIndex < courseAdapter.getCount()) {
            spinnerCourse.setSelection(currentCourseIndex);
        }
    }

    private void showCourse() {
        if (currentCourseIndex >= courses.size()) return;
        Course course = courses.get(currentCourseIndex);
        int lang = CourseRegistry.getInstance().getLanguage();
        tvDescription.setText(course.description(lang));
        tvDescription.setTextColor(theme.textDim);

        CourseOutlineAdapter adapter = new CourseOutlineAdapter(
                course.chapters, theme, lang,
                lesson -> openLesson(course.id, lesson.id));
        rvChapters.setAdapter(adapter);
    }

    private void openLesson(String courseId, String lessonId) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra(LessonActivity.EXTRA_COURSE, courseId);
        intent.putExtra(LessonActivity.EXTRA_LESSON, lessonId);
        startActivity(intent);
    }
}
