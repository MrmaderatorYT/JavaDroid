package com.ccs.javadroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LearnActivity extends AppCompatActivity {

    private AppPreferences appPrefs;
    private AppTheme theme;
    private RecyclerView rvChapters;
    private LearnAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appPrefs = new AppPreferences(this);
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.learnToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setBackgroundColor(theme.toolbar);

        rvChapters = findViewById(R.id.rvChapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setBackgroundColor(theme.bg);

        // Apply theme to main layout content
        findViewById(android.R.id.content).setBackgroundColor(theme.bg);

        loadChaptersFromIndexHtml();
    }

    private void loadChaptersFromIndexHtml() {
        List<Chapter> chapters = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("tutorials/index.html"), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            String htmlContent = sb.toString();

            Pattern chapterPattern = Pattern.compile("<li><p><a href=\"([^\"]+)\">(?:Глава|Розділ|Глава|Глава)\\s*([^<]+)</a></p>", Pattern.CASE_INSENSITIVE);
            Pattern subListPattern = Pattern.compile("<ol class=\"subsubcontent\">(.*?)</ol>", Pattern.DOTALL);
            Pattern lessonPattern = Pattern.compile("<li><p><a href=\"([^\"]+)\">([^<]+)</a></p>", Pattern.CASE_INSENSITIVE);

            Matcher chapterMatcher = chapterPattern.matcher(htmlContent);
            int lastEnd = 0;
            while (chapterMatcher.find()) {
                String firstFile = chapterMatcher.group(1);
                String title = chapterMatcher.group(2);
                Chapter chapter = new Chapter("Розділ " + title, firstFile);
                
                int startSearch = chapterMatcher.end();
                Matcher subListMatcher = subListPattern.matcher(htmlContent);
                if (subListMatcher.find(startSearch)) {
                    String subListHtml = subListMatcher.group(1);
                    Matcher lessonMatcher = lessonPattern.matcher(subListHtml);
                    while (lessonMatcher.find()) {
                        String href = lessonMatcher.group(1);
                        String lessonTitle = lessonMatcher.group(2);
                        chapter.lessons.add(new Lesson(lessonTitle, href));
                    }
                }
                
                chapters.add(chapter);
            }

            if (chapters.isEmpty()) {
                chapters = getFallbackChapters();
            }

            adapter = new LearnAdapter(chapters, theme, (chapter, lesson) -> {
                Intent intent = new Intent(this, TutorialActivity.class);
                intent.putExtra("lesson_title", lesson.title);
                intent.putExtra("lesson_file", lesson.href);
                startActivity(intent);
            });
            rvChapters.setAdapter(adapter);

        } catch (Exception e) {
            // Fallback list
            adapter = new LearnAdapter(getFallbackChapters(), theme, (chapter, lesson) -> {
                Intent intent = new Intent(this, TutorialActivity.class);
                intent.putExtra("lesson_title", lesson.title);
                intent.putExtra("lesson_file", lesson.href);
                startActivity(intent);
            });
            rvChapters.setAdapter(adapter);
        }
    }

    private List<Chapter> getFallbackChapters() {
        List<Chapter> list = new ArrayList<>();
        
        Chapter c1 = new Chapter("Розділ 1. Вступ до Java", "1.1.html");
        c1.lessons.add(new Lesson("Що таке Java", "1.1.html"));
        c1.lessons.add(new Lesson("Встановлення JDK", "1.6.html"));
        c1.lessons.add(new Lesson("Перша програма на Java", "1.2.html"));
        c1.lessons.add(new Lesson("Перша програма в IntelliJ IDEA", "1.5.html"));
        list.add(c1);

        Chapter c2 = new Chapter("Розділ 2. Основи програмування Java", "2.1.html");
        c2.lessons.add(new Lesson("Структура програми", "2.11.html"));
        c2.lessons.add(new Lesson("Змінні та константи", "2.1.html"));
        c2.lessons.add(new Lesson("Типи даних", "2.12.html"));
        c2.lessons.add(new Lesson("Консольне введення/виведення", "2.9.html"));
        c2.lessons.add(new Lesson("Арифметичні операції", "2.3.html"));
        c2.lessons.add(new Lesson("Умовні конструкції", "2.5.html"));
        c2.lessons.add(new Lesson("Цикли", "2.6.html"));
        c2.lessons.add(new Lesson("Масиви", "2.4.html"));
        list.add(c2);

        Chapter c3 = new Chapter("Розділ 3. Класи та об'єкти", "3.1.html");
        c3.lessons.add(new Lesson("Класи та об'єкти", "3.1.html"));
        c3.lessons.add(new Lesson("Методи", "2.7.html"));
        c3.lessons.add(new Lesson("Параметри методів", "2.16.html"));
        c3.lessons.add(new Lesson("Конструктори", "3.21.html"));
        c3.lessons.add(new Lesson("Пакетна структура", "3.2.html"));
        list.add(c3);

        return list;
    }

    public static class Chapter {
        public String title;
        public String href;
        public boolean isExpanded = false;
        public List<Lesson> lessons = new ArrayList<>();

        public Chapter(String title, String href) {
            this.title = title;
            this.href = href;
        }
    }

    public static class Lesson {
        public String title;
        public String href;

        public Lesson(String title, String href) {
            this.title = title;
            this.href = href;
        }
    }
}
