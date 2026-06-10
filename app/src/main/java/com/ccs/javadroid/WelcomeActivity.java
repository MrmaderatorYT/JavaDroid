package com.ccs.javadroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {

    private AppPreferences appPrefs;
    private AppTheme theme;

    private EditText etSearchProjects;
    private Button btnNewProject;
    private Button btnOpenProject;
    private Button btnCloneRepo;
    private View layoutEmptyProjects;
    private RecyclerView rvRecentProjects;

    private RecentProjectsAdapter adapter;
    private final List<String> allRecentPaths = new ArrayList<>();
    private final List<String> filteredPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appPrefs = new AppPreferences(this);
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        bindViews();
        applyThemeStyles();
        setupRecentProjects();
        setupActions();

        // Request MANAGE_EXTERNAL_STORAGE permission if needed (Android 11+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }

        // If launched with an action to immediately show new project creation dialog
        if ("ACTION_NEW_PROJECT".equals(getIntent().getAction())) {
            showNewMavenProjectDialog();
        }
    }

    private void bindViews() {
        etSearchProjects = findViewById(R.id.etSearchProjects);
        btnNewProject = findViewById(R.id.btnNewProject);
        btnOpenProject = findViewById(R.id.btnOpenProject);
        btnCloneRepo = findViewById(R.id.btnCloneRepo);
        layoutEmptyProjects = findViewById(R.id.layoutEmptyProjects);
        rvRecentProjects = findViewById(R.id.rvRecentProjects);
    }

    private void applyThemeStyles() {
        // Apply theme color tokens to welcome layout elements
        View rootLayout = findViewById(android.R.id.content);
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(theme.bg);
        }
        
        // Find right workspace area parent layout and sidebar layout
        LinearLayout rightWorkspace = findViewById(R.id.rvRecentProjects) != null ? 
                (LinearLayout) findViewById(R.id.rvRecentProjects).getParent().getParent() : null;
        if (rightWorkspace != null) {
            rightWorkspace.setBackgroundColor(theme.bg);
        }

        View sidebarLayout = findViewById(R.id.sidebarProjects) != null ? 
                (View) findViewById(R.id.sidebarProjects).getParent() : null;
        if (sidebarLayout != null) {
            sidebarLayout.setBackgroundColor(theme.toolbar);
        }

        TextView appTitle = null;
        if (sidebarLayout instanceof LinearLayout) {
            LinearLayout sl = (LinearLayout) sidebarLayout;
            if (sl.getChildCount() > 0 && sl.getChildAt(0) instanceof LinearLayout) {
                LinearLayout header = (LinearLayout) sl.getChildAt(0);
                if (header.getChildCount() > 1 && header.getChildAt(1) instanceof LinearLayout) {
                    LinearLayout textContainer = (LinearLayout) header.getChildAt(1);
                    if (textContainer.getChildCount() > 0 && textContainer.getChildAt(0) instanceof TextView) {
                        appTitle = (TextView) textContainer.getChildAt(0);
                    }
                }
            }
        }
        if (appTitle != null) {
            appTitle.setTextColor(theme.text);
        }

        if (etSearchProjects != null) {
            etSearchProjects.setBackgroundColor(blend(theme.toolbar, theme.bg, 0.2f));
            etSearchProjects.setTextColor(theme.text);
            etSearchProjects.setHintTextColor(theme.textDim);
        }
        if (btnNewProject != null) {
            btnNewProject.setBackgroundColor(theme.accent);
            btnNewProject.setTextColor(Color.WHITE);
        }
        if (btnOpenProject != null) {
            btnOpenProject.setBackgroundColor(theme.bg);
            btnOpenProject.setTextColor(theme.text);
        }
        if (btnCloneRepo != null) {
            btnCloneRepo.setBackgroundColor(theme.bg);
            btnCloneRepo.setTextColor(theme.textDim);
        }
        // Sidebar items styling
        TextView sidebarProjects = findViewById(R.id.sidebarProjects);
        if (sidebarProjects != null) {
            sidebarProjects.setBackgroundColor(theme.accent);
            sidebarProjects.setTextColor(Color.WHITE);
        }
        TextView sidebarCustomize = findViewById(R.id.sidebarCustomize);
        if (sidebarCustomize != null) {
            sidebarCustomize.setTextColor(theme.textDim);
        }
        TextView sidebarPlugins = findViewById(R.id.sidebarPlugins);
        if (sidebarPlugins != null) {
            sidebarPlugins.setTextColor(theme.textDim);
        }
        TextView sidebarLearn = findViewById(R.id.sidebarLearn);
        if (sidebarLearn != null) {
            sidebarLearn.setTextColor(theme.textDim);
        }
        android.widget.ImageView sidebarSettings = findViewById(R.id.sidebarSettings);
        if (sidebarSettings != null) {
            sidebarSettings.setColorFilter(theme.textDim);
        }
    }

    private void setupRecentProjects() {
        allRecentPaths.clear();
        // Load recent projects, filter out paths that no longer exist
        List<String> saved = appPrefs.getRecentProjects();
        for (String path : saved) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                allRecentPaths.add(path);
            } else {
                appPrefs.removeRecentProject(path);
            }
        }

        // If list is empty, default it to our base folder items
        if (allRecentPaths.isEmpty()) {
            File base = MavenPaths.getJavaDroidBase(this);
            File[] dirs = base.listFiles();
            if (dirs != null) {
                for (File d : dirs) {
                    if (d.isDirectory() && !d.getName().startsWith(".")) {
                        allRecentPaths.add(d.getAbsolutePath());
                        appPrefs.addRecentProject(d.getAbsolutePath());
                    }
                }
            }
        }

        filteredPaths.clear();
        filteredPaths.addAll(allRecentPaths);

        adapter = new RecentProjectsAdapter(filteredPaths, theme, this::openProject, this::showProjectOptions);
        rvRecentProjects.setLayoutManager(new LinearLayoutManager(this));
        rvRecentProjects.setAdapter(adapter);

        updateEmptyStateVisibility();
    }

    private void setupActions() {
        etSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        
        View btnSettings = findViewById(R.id.sidebarSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        btnNewProject.setOnClickListener(v -> showNewMavenProjectDialog());
        btnOpenProject.setOnClickListener(v -> showOpenFolderDialog());
        btnCloneRepo.setOnClickListener(v -> Toast.makeText(this, R.string.soon, Toast.LENGTH_SHORT).show());
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener themeChangeListener = (sharedPreferences, key) -> {
        if ("theme_id".equals(key) || key.startsWith("custom_")) {
            theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
            setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);
            recreate();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        appPrefs.raw().registerOnSharedPreferenceChangeListener(themeChangeListener);
        // Refresh preferences and redraw in case theme changed inside SettingsActivity
        AppTheme currentTheme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        if (!currentTheme.id.equals(theme.id) || currentTheme.bg != theme.bg) {
            theme = currentTheme;
            setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);
            recreate();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        appPrefs.raw().unregisterOnSharedPreferenceChangeListener(themeChangeListener);
    }

    private void filterProjects(String query) {
        filteredPaths.clear();
        if (query.isEmpty()) {
            filteredPaths.addAll(allRecentPaths);
        } else {
            String q = query.toLowerCase(Locale.ROOT);
            for (String path : allRecentPaths) {
                File f = new File(path);
                if (f.getName().toLowerCase(Locale.ROOT).contains(q)) {
                    filteredPaths.add(path);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyStateVisibility();
    }

    private void updateEmptyStateVisibility() {
        if (filteredPaths.isEmpty()) {
            layoutEmptyProjects.setVisibility(View.VISIBLE);
            rvRecentProjects.setVisibility(View.GONE);
        } else {
            layoutEmptyProjects.setVisibility(View.GONE);
            rvRecentProjects.setVisibility(View.VISIBLE);
        }
    }

    private void openProject(String path) {
        appPrefs.setProjectRoot(path);
        appPrefs.addRecentProject(path);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showProjectOptions(String path, View anchor) {
        final File file = new File(path);
        String[] options = { "Open", "Remove from history", "Delete project from disk" };
        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openProject(path);
                    } else if (which == 1) {
                        appPrefs.removeRecentProject(path);
                        setupRecentProjects();
                    } else if (which == 2) {
                        new AlertDialog.Builder(this)
                                .setTitle("Delete Project")
                                .setMessage("Are you sure you want to permanently delete " + file.getName() + " and all its files? This action cannot be undone.")
                                .setPositiveButton(R.string.dialog_delete, (d, w) -> {
                                    deleteRecursive(file);
                                    appPrefs.removeRecentProject(path);
                                    setupRecentProjects();
                                })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show();
                    }
                })
                .show();
    }

    private void showOpenFolderDialog() {
        File base = MavenPaths.getJavaDroidBase(this);
        final File[] dirs = base.listFiles(File::isDirectory);
        if (dirs == null || dirs.length == 0) {
            Toast.makeText(this, "No projects inside Documents/JavaDroid yet.", Toast.LENGTH_LONG).show();
            return;
        }

        final String[] names = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            names[i] = dirs[i].getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Open Project Folder")
                .setItems(names, (dialog, which) -> openProject(dirs[which].getAbsolutePath()))
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showNewMavenProjectDialog() {
        int pad = dp(12);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad, pad, pad);

        EditText etName = newEditForDialog("Project folder name (e.g. MyApp)");
        EditText etGroup = newEditForDialog("Group ID / package (e.g. com.company.app)");
        EditText etArtifact = newEditForDialog("Artifact ID (e.g. myapp), optional");

        box.addView(etName);
        box.addView(etGroup);
        box.addView(etArtifact);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_new_maven_title)
                .setMessage(R.string.dialog_new_maven_message)
                .setView(box)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String group = etGroup.getText().toString().trim();
                    String artifact = etArtifact.getText().toString().trim();
                    try {
                        File root = MavenProjectFactory.create(this, name, group, artifact);
                        appPrefs.setProjectRoot(root.getAbsolutePath());
                        appPrefs.addRecentProject(root.getAbsolutePath());
                        
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private EditText newEditForDialog(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(theme.textDim);
        e.setTextColor(theme.text);
        e.setBackgroundColor(blend(theme.bg, theme.text, 0.05f));
        e.setPadding(32, 16, 32, 16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return file.delete();
    }

    // ── Adapter & ViewHolder implementation ──────────────────

    private interface ProjectClickListener {
        void onClick(String path);
    }

    private interface ProjectOptionsListener {
        void onOptionsClick(String path, View anchor);
    }

    private static class RecentProjectsAdapter extends RecyclerView.Adapter<RecentProjectsAdapter.ViewHolder> {

        private final List<String> paths;
        private final AppTheme theme;
        private final ProjectClickListener clickListener;
        private final ProjectOptionsListener optionsListener;

        RecentProjectsAdapter(List<String> paths, AppTheme theme, ProjectClickListener clickListener, ProjectOptionsListener optionsListener) {
            this.paths = paths;
            this.theme = theme;
            this.clickListener = clickListener;
            this.optionsListener = optionsListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_project, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String path = paths.get(position);
            File file = new File(path);
            holder.projectName.setText(file.getName());
            holder.projectName.setTextColor(theme.text);
            holder.projectPath.setText(path);
            holder.projectPath.setTextColor(theme.textDim);
            holder.btnProjectOptions.setTextColor(theme.textDim);

            String name = file.getName();
            String avatarText = name.substring(0, Math.min(name.length(), 2)).toUpperCase(Locale.ROOT);
            holder.projectAvatar.setText(avatarText);

            // Give a random colored background to the avatar card
            int[] colors = {0xFF3574F0, 0xFFE74C3C, 0xFF2ECC71, 0xFFF1C40F, 0xFF9B59B6, 0xFF1ABC9C, 0xFFE67E22};
            int color = colors[Math.abs(name.hashCode()) % colors.length];
            GradientDrawable d = new GradientDrawable();
            d.setCornerRadius(18f); // fully rounded
            d.setColor(color);
            holder.projectAvatar.setBackground(d);

            holder.itemView.setOnClickListener(v -> clickListener.onClick(path));
            holder.btnProjectOptions.setOnClickListener(v -> optionsListener.onOptionsClick(path, holder.btnProjectOptions));
        }

        @Override
        public int getItemCount() {
            return paths.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView projectAvatar;
            TextView projectName;
            TextView projectPath;
            TextView btnProjectOptions;

            ViewHolder(@NonNull View view) {
                super(view);
                projectAvatar = view.findViewById(R.id.projectAvatar);
                projectName = view.findViewById(R.id.projectName);
                projectPath = view.findViewById(R.id.projectPath);
                btnProjectOptions = view.findViewById(R.id.btnProjectOptions);
            }
        }
    }
}
