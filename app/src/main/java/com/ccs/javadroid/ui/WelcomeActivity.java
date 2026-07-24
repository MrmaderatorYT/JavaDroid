package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;
import com.ccs.javadroid.maven.MavenPaths;
import com.ccs.javadroid.git.GitManager;
import com.ccs.javadroid.project.PlaygroundProjectFactory;
import com.ccs.javadroid.maven.MavenProjectFactory;
import com.ccs.javadroid.project.GradleProjectFactory;
import com.ccs.javadroid.tools.bytecode.BytecodeProjectFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appPrefs = new AppPreferences(this);
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FullScreenHelper.enable(this);

        bindViews();
        applyThemeStyles();
        setupRecentProjects();
        setupActions();

        // Request MANAGE_EXTERNAL_STORAGE permission if needed (Android 11+)
        requestStoragePermission();

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
        // Root
        View rootLayout = findViewById(R.id.welcome_root);
        if (rootLayout == null) {
            rootLayout = findViewById(android.R.id.content);
        }
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(theme.bg);
        }

        // Navigation Bar / Sidebar
        View sidebarLayout = findViewById(R.id.welcome_sidebar);
        if (sidebarLayout == null) {
            sidebarLayout = findViewById(R.id.sidebarProjects) != null ?
                    (View) findViewById(R.id.sidebarProjects).getParent() : null;
        }
        if (sidebarLayout != null) {
            sidebarLayout.setBackgroundColor(theme.toolbar);
        }

        // App title & version
        TextView tvAppName = findViewById(R.id.tvAppName);
        if (tvAppName != null) tvAppName.setTextColor(theme.text);
        TextView tvAppVersion = findViewById(R.id.tvAppVersion);
        if (tvAppVersion != null) {
            tvAppVersion.setTextColor(theme.textDim);
            try {
                String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                tvAppVersion.setText(version);
            } catch (Exception e) {}
        }

        // Search field
        if (etSearchProjects != null) {
            etSearchProjects.setBackgroundColor(blend(theme.toolbar, theme.bg, 0.2f));
            etSearchProjects.setTextColor(theme.text);
            etSearchProjects.setHintTextColor(theme.textDim);
        }

        // Buttons (using ColorStateList for compatibility with MaterialButton)
        if (btnNewProject != null) {
            btnNewProject.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.accent));
            btnNewProject.setTextColor(Color.WHITE);
        }
        if (btnOpenProject != null) {
            btnOpenProject.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.toolbar));
            btnOpenProject.setTextColor(theme.text);
        }
        if (btnCloneRepo != null) {
            btnCloneRepo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.toolbar));
            btnCloneRepo.setTextColor(theme.textDim);
        }

        // Sidebar items
        TextView sidebarProjects = findViewById(R.id.sidebarProjects);
        if (sidebarProjects != null) {
            float density = getResources().getDisplayMetrics().density;
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(theme.accent);
            gd.setCornerRadius(6f * density); // 6dp corner radius
            sidebarProjects.setBackground(gd);
            sidebarProjects.setTextColor(Color.WHITE);
        }
        TextView sidebarMaterials = findViewById(R.id.sidebarMaterials);
        if (sidebarMaterials != null) sidebarMaterials.setTextColor(theme.textDim);

        android.widget.ImageView sidebarSettings = findViewById(R.id.sidebarSettings);
        if (sidebarSettings != null) sidebarSettings.setColorFilter(theme.textDim);

        // Empty state
        TextView tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        if (tvEmptyTitle != null) tvEmptyTitle.setTextColor(theme.textDim);
        TextView tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        if (tvEmptySubtitle != null) tvEmptySubtitle.setTextColor(theme.textDim);
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
        btnCloneRepo.setOnClickListener(v -> showCloneRepoDialog());

        View sidebarMaterials = findViewById(R.id.sidebarMaterials);
        if (sidebarMaterials != null) {
            sidebarMaterials.setOnClickListener(v -> {
                Intent intent = new Intent(this, LearnActivity.class);
                startActivity(intent);
            });
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        // Check if permission was granted after returning from Settings
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (android.os.Environment.isExternalStorageManager()) {
                // Permission granted, can proceed
            }
        }
    }

    private static final int REQ_STORAGE_PERMISSION = 9999;

    private void requestStoragePermission() {
        // Only request if we actually need external storage access
        // If project is on internal storage, skip permission request
        String savedPath = appPrefs.getProjectRoot();
        if (savedPath != null) {
            try {
                String internalPath = getFilesDir().getCanonicalPath();
                if (savedPath.startsWith(internalPath)) {
                    return; // Project is on internal storage, no permission needed
                }
            } catch (Exception ignored) {}
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                newRoundedDialog()
                        .setTitle(R.string.permission_storage_title)
                        .setMessage(R.string.permission_storage_message)
                        .setPositiveButton(R.string.permission_storage_open_settings, (d, w) -> {
                            openStorageSettings();
                        })
                        .setNegativeButton(R.string.permission_storage_exit, (d, w) -> finish())
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void openStorageSettings() {
        try {
            // Try to open app-specific storage settings
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            try {
                // Fallback: open all files access settings
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } catch (Exception e2) {
                // Last resort: open app settings
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e3) {
                    Toast.makeText(this, R.string.toast_cannot_open_settings, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_STORAGE_PERMISSION) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (!android.os.Environment.isExternalStorageManager()) {
                    newRoundedDialog()
                            .setTitle(R.string.permission_storage_denied_title)
                            .setMessage(R.string.permission_storage_denied_message)
                            .setPositiveButton(R.string.permission_storage_try_again, (d, w) -> requestStoragePermission())
                            .setNegativeButton(R.string.permission_storage_exit, (d, w) -> finish())
                            .setCancelable(false)
                            .show();
                }
            }
        } else if (requestCode == REQUEST_IMPORT_ZIP && resultCode == RESULT_OK && data != null) {
            importZipResult(data.getData());
        } else if (requestCode == REQUEST_IMPORT_FOLDER && resultCode == RESULT_OK && data != null) {
            importFolderResult(data.getData());
        }
    }

    private void importZipResult(android.net.Uri uri) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("Extracting ZIP...");
        pd.setCancelable(false);
        pd.show();

        new Thread(() -> {
            try {
                java.io.InputStream is = getContentResolver().openInputStream(uri);
                if (is == null) throw new java.io.IOException("Cannot open URI");
                
                String fileName = "ImportedProject_" + System.currentTimeMillis();
                try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex);
                            if (fileName.toLowerCase().endsWith(".zip")) {
                                fileName = fileName.substring(0, fileName.length() - 4);
                            }
                        }
                    }
                }
                
                File dest = new File(MavenPaths.getJavaDroidBase(this), fileName);
                com.ccs.javadroid.utils.ZipUtils.unzip(is, dest);
                is.close();
                
                File[] children = dest.listFiles();
                if (children != null && children.length == 1 && children[0].isDirectory()) {
                    dest = children[0];
                }
                final String projectPath = dest.getAbsolutePath();
                
                runOnUiThread(() -> {
                    pd.dismiss();
                    Toast.makeText(WelcomeActivity.this, "Imported successfully", Toast.LENGTH_SHORT).show();
                    appPrefs.addRecentProject(projectPath);
                    setupRecentProjects();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    newRoundedDialog().setTitle("Error").setMessage(e.getMessage()).setPositiveButton(android.R.string.ok, null).show();
                });
            }
        }).start();
    }

    private void importFolderResult(android.net.Uri uri) {
        android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
        pd.setMessage("Copying Folder...");
        pd.setCancelable(false);
        pd.show();

        new Thread(() -> {
            try {
                androidx.documentfile.provider.DocumentFile tree = androidx.documentfile.provider.DocumentFile.fromTreeUri(this, uri);
                if (tree == null) throw new java.io.IOException("Cannot open folder");
                
                String folderName = tree.getName();
                if (folderName == null) folderName = "ImportedFolder_" + System.currentTimeMillis();
                
                File dest = new File(MavenPaths.getJavaDroidBase(this), folderName);
                if (!dest.exists()) dest.mkdirs();
                
                copyDocumentFile(tree, dest);
                
                runOnUiThread(() -> {
                    pd.dismiss();
                    Toast.makeText(WelcomeActivity.this, "Imported successfully", Toast.LENGTH_SHORT).show();
                    appPrefs.addRecentProject(dest.getAbsolutePath());
                    setupRecentProjects();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pd.dismiss();
                    newRoundedDialog().setTitle("Error").setMessage(e.getMessage()).setPositiveButton(android.R.string.ok, null).show();
                });
            }
        }).start();
    }

    private void copyDocumentFile(androidx.documentfile.provider.DocumentFile sourceFile, File destDir) throws java.io.IOException {
        for (androidx.documentfile.provider.DocumentFile file : sourceFile.listFiles()) {
            if (file.isDirectory()) {
                File newDir = new File(destDir, file.getName());
                newDir.mkdirs();
                copyDocumentFile(file, newDir);
            } else {
                File newFile = new File(destDir, file.getName());
                try (java.io.InputStream in = getContentResolver().openInputStream(file.getUri());
                     java.io.OutputStream out = new java.io.FileOutputStream(newFile)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        }
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
        String[] options = { getString(R.string.welcome_project_options_open), getString(R.string.welcome_project_options_remove), getString(R.string.welcome_project_options_delete) };
        newRoundedDialog()
                .setTitle(file.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openProject(path);
                    } else if (which == 1) {
                        appPrefs.removeRecentProject(path);
                        setupRecentProjects();
                    } else if (which == 2) {
                        newRoundedDialog()
                                .setTitle(R.string.welcome_delete_project_title)
                                .setMessage(getString(R.string.welcome_delete_project_message, file.getName()))
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
        
        int dirCount = (dirs == null) ? 0 : dirs.length;
        final String[] names = new String[dirCount + 1];
        names[0] = "➕ Import from ZIP / Folder";
        
        for (int i = 0; i < dirCount; i++) {
            names[i + 1] = dirs[i].getName();
        }

        newRoundedDialog()
                .setTitle(R.string.welcome_open_folder_title)
                .setItems(names, (dialog, which) -> {
                    if (which == 0) {
                        showImportTypeDialog();
                    } else {
                        openProject(dirs[which - 1].getAbsolutePath());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showImportTypeDialog() {
        String[] options = {"Select ZIP Archive", "Select Folder"};
        newRoundedDialog()
                .setTitle("Import Project")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        importZip();
                    } else {
                        importFolder();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private static final int REQUEST_IMPORT_ZIP = 2001;
    private static final int REQUEST_IMPORT_FOLDER = 2002;

    private void importZip() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/zip", "application/x-zip-compressed"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_IMPORT_ZIP);
    }

    private void importFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_IMPORT_FOLDER);
    }

    private void showCloneRepoDialog() {
        int pad = dp(12);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad, pad, pad);

        EditText etUrl = newEditForDialog(getString(R.string.welcome_clone_url_hint));
        EditText etName = newEditForDialog(getString(R.string.welcome_clone_name_hint));
        EditText etUser = newEditForDialog(getString(R.string.welcome_clone_user_hint));
        EditText etToken = newEditForDialog(getString(R.string.welcome_clone_token_hint));
        etToken.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        box.addView(etUrl);
        box.addView(etName);
        box.addView(etUser);
        box.addView(etToken);

        newRoundedDialog()
                .setTitle(R.string.welcome_clone_repo_title)
                .setMessage(R.string.welcome_clone_repo_message)
                .setView(box)
                .setPositiveButton(R.string.welcome_clone_button, (d, w) -> {
                    String url = etUrl.getText().toString().trim();
                    String name = etName.getText().toString().trim();
                    String user = etUser.getText().toString().trim();
                    String token = etToken.getText().toString().trim();

                    if (url.isEmpty()) {
                        Toast.makeText(this, getString(R.string.welcome_url_required), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!GitManager.isValidGitUrl(url)) {
                        Toast.makeText(this, getString(R.string.welcome_invalid_git_url), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (name.isEmpty()) {
                        // Extract name from URL
                        name = url.substring(url.lastIndexOf('/') + 1);
                        if (name.endsWith(".git")) name = name.substring(0, name.length() - 4);
                    }

                    File projectDir = new File(MavenPaths.getJavaDroidBase(this), name);

                    if (projectDir.exists() && projectDir.isDirectory()) {
                        String[] files = projectDir.list();
                        if (files != null && files.length > 0) {
                            Toast.makeText(this, "Папка з такою назвою (" + name + ") вже існує і не є порожньою. Будь ласка, вкажіть іншу назву.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
                    pd.setMessage(getString(R.string.welcome_cloning));
                    pd.setCancelable(false);
                    pd.show();

                    io.execute(() -> {
                        try {
                            org.eclipse.jgit.lib.ProgressMonitor monitor = new org.eclipse.jgit.lib.ProgressMonitor() {
                                private int totalWork;
                                private int completed;
                                @Override public void start(int totalTasks) { }
                                @Override public void beginTask(String title, int total) {
                                    this.totalWork = total;
                                    this.completed = 0;
                                    android.util.Log.i("GitManager", "Git task: " + title + " (total=" + totalWork + ")");
                                    ui.post(() -> {
                                        if (pd.isShowing()) pd.setMessage("Клонування: " + title);
                                    });
                                }
                                @Override public void update(int c) {
                                    this.completed += c;
                                    if (totalWork > 0 && this.completed % 10 == 0) { // Update occasionally
                                        // Optional: update progress bar if we had one
                                    }
                                }
                                @Override public void endTask() { }
                                @Override public boolean isCancelled() { return !pd.isShowing(); }
                            };

                            GitManager.clone(url, projectDir,
                                    user.isEmpty() ? null : user,
                                    token.isEmpty() ? null : token,
                                    monitor);
                            ui.post(() -> {
                                if (pd.isShowing()) pd.dismiss();
                                appPrefs.setProjectRoot(projectDir.getAbsolutePath());
                                appPrefs.addRecentProject(projectDir.getAbsolutePath());
                                Toast.makeText(getApplicationContext(), getString(R.string.welcome_cloned_success), Toast.LENGTH_SHORT).show();
                                openProject(projectDir.getAbsolutePath());
                            });
                        } catch (Throwable e) {
                            android.util.Log.e("GitManager", "Clone failed", e);
                            ui.post(() -> {
                                if (pd.isShowing()) pd.dismiss();
                                Toast.makeText(getApplicationContext(), getString(R.string.welcome_clone_failed, e.toString()),
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showNewMavenProjectDialog() {
        String[] projectTypes = {
            getString(R.string.project_type_maven),
            getString(R.string.project_type_gradle),
            getString(R.string.project_type_bytecode),
            getString(R.string.project_type_playground)
        };

        newRoundedDialog()
                .setTitle(R.string.dialog_new_project_title)
                .setItems(projectTypes, (dialog, which) -> {
                    if (which == 0) {
                        showMavenProjectForm();
                    } else if (which == 1) {
                        showGradleProjectForm();
                    } else if (which == 2) {
                        showBytecodeProjectForm();
                    } else if (which == 3) {
                        createPlaygroundProject();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void createPlaygroundProject() {
        Toast.makeText(this, getString(R.string.welcome_creating_playground), Toast.LENGTH_SHORT).show();
        io.execute(() -> {
            try {
                File root = PlaygroundProjectFactory.create(this);
                ui.post(() -> {
                    appPrefs.setProjectRoot(root.getAbsolutePath());
                    appPrefs.addRecentProject(root.getAbsolutePath());
                    Toast.makeText(this, getString(R.string.welcome_playground_created), Toast.LENGTH_SHORT).show();
                    openProject(root.getAbsolutePath());
                });
            } catch (Exception e) {
                ui.post(() -> Toast.makeText(this, getString(R.string.welcome_error, e.getMessage()),
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private void showMavenProjectForm() {
        int pad = dp(12);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad, pad, pad);

        EditText etName = newEditForDialog(getString(R.string.welcome_project_name_hint));
        EditText etGroup = newEditForDialog(getString(R.string.welcome_project_group_hint));
        EditText etArtifact = newEditForDialog(getString(R.string.welcome_project_artifact_hint));

        box.addView(etName);
        box.addView(etGroup);
        box.addView(etArtifact);

        newRoundedDialog()
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

    private void showGradleProjectForm() {
        int pad = dp(12);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad, pad, pad);

        EditText etName = newEditForDialog(getString(R.string.welcome_project_name_hint));
        EditText etGroup = newEditForDialog(getString(R.string.welcome_project_group_hint));

        box.addView(etName);
        box.addView(etGroup);

        newRoundedDialog()
                .setTitle(R.string.dialog_new_gradle_title)
                .setMessage(R.string.dialog_new_gradle_message)
                .setView(box)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String group = etGroup.getText().toString().trim();
                    try {
                        File root = GradleProjectFactory.create(this, name, group);
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

    private void showBytecodeProjectForm() {
        int pad = dp(12);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad, pad, pad);

        EditText etName = newEditForDialog(getString(R.string.welcome_project_name_bytecode_hint));

        box.addView(etName);

        newRoundedDialog()
                .setTitle(R.string.dialog_new_bytecode_title)
                .setMessage(R.string.dialog_new_bytecode_message)
                .setView(box)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    try {
                        File root = BytecodeProjectFactory.create(this, name);
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

    private com.google.android.material.dialog.MaterialAlertDialogBuilder newRoundedDialog() {
        return new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
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
