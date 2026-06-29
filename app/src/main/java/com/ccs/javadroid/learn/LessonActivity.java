package com.ccs.javadroid.learn;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;
import com.ccs.javadroid.R;

import java.util.List;

/**
 * Показує один матеріал обраного розділу нативно (без WebView): заголовок +
 * навігація далі/назад + список блоків через {@link LessonBlockAdapter}.
 */
public class LessonActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE = "course_id";
    public static final String EXTRA_LESSON = "lesson_id";

    private AppPreferences appPrefs;
    private AppTheme theme;

    private LessonBlockAdapter adapter;
    private String courseId;
    private String lessonId;
    private List<Lesson> materials;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appPrefs = new AppPreferences(this);
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);
        FullScreenHelper.enable(this);

        courseId = getIntent().getStringExtra(EXTRA_COURSE);
        lessonId = getIntent().getStringExtra(EXTRA_LESSON);

        TextView tvTitle = findViewById(R.id.tvMaterialTitle);
        RecyclerView rv = findViewById(R.id.rvMaterialBlocks);
        View btnBack = findViewById(R.id.btnMaterialBack);
        View btnPrev = findViewById(R.id.btnMaterialPrev);
        View btnNext = findViewById(R.id.btnMaterialNext);

        rv.setBackgroundColor(theme.bg);
        findViewById(android.R.id.content).setBackgroundColor(theme.bg);

        Course section = CourseRegistry.getInstance().getCourse(courseId);
        if (section == null) {
            Toast.makeText(this, "Section not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        materials = section.allMaterials();

        // знайти поточний матеріал
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).id.equals(lessonId)) { currentIndex = i; break; }
        }
        if (currentIndex >= materials.size()) currentIndex = 0;

        int lang = CourseRegistry.getInstance().getLanguage();
        Lesson material = materials.get(currentIndex);
        tvTitle.setText(material.title(lang));
        tvTitle.setTextColor(theme.text);

        adapter = new LessonBlockAdapter(material.content(lang), theme);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnPrev.setVisibility(currentIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(currentIndex < materials.size() - 1 ? View.VISIBLE : View.INVISIBLE);
        btnPrev.setOnClickListener(v -> navigate(-1));
        btnNext.setOnClickListener(v -> navigate(1));
    }

    private void navigate(int delta) {
        int idx = currentIndex + delta;
        if (idx < 0 || idx >= materials.size()) return;
        currentIndex = idx;
        int lang = CourseRegistry.getInstance().getLanguage();
        Lesson material = materials.get(currentIndex);
        ((TextView) findViewById(R.id.tvMaterialTitle)).setText(material.title(lang));
        adapter.setBlocks(material.content(lang));
        RecyclerView rv = findViewById(R.id.rvMaterialBlocks);
        if (rv.getLayoutManager() != null) rv.getLayoutManager().scrollToPosition(0);
        findViewById(R.id.btnMaterialPrev).setVisibility(idx > 0 ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.btnMaterialNext).setVisibility(idx < materials.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }
}
