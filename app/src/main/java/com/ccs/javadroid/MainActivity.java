package com.ccs.javadroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;

public class MainActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────
    private DrawerLayout  drawerLayout;
    private CodeEditor    editor;
    private RecyclerView  tabsRecycler;
    private RecyclerView  fileTreeRecycler;
    private LinearLayout  findBar;
    private EditText      etFind;
    private EditText      etReplace;
    private TextView      statusLineCol;
    private TextView      statusFileName;
    private TextView      toolbarTitle;
    private ScrollView    consoleScroll;
    private TextView      consoleOutput;
    private RecyclerView  problemsRecycler;
    private TextView      tabRun;
    private TextView      tabProblems;
    private TextView      tabBytecode;
    private ScrollView    bytecodeOuterScroll;
    private TextView      bytecodeOutput;
    private View          btnClearConsole;

    private static final int PANEL_RUN       = 0;
    private static final int PANEL_PROBLEMS  = 1;
    private static final int PANEL_BYTECODE  = 2;
    private int bottomPanelMode = PANEL_RUN;
    private volatile boolean bytecodeRefreshRunning;

    // ── Adapters & Managers ────────────────────────────────
    private TabsAdapter      tabsAdapter;
    private FileTreeAdapter  fileTreeAdapter;
    private ProjectManager   projectManager;
    private ProblemsAdapter  problemsAdapter;
    private LiveProblemsScheduler liveProblemsScheduler;

    private volatile boolean mavenSyncInProgress;

    // ── State ──────────────────────────────────────────────
    private SharedPreferences prefs;
    private int     currentFontSize  = 14;
    private boolean isRunning        = false;
    private int     lastSearchOffset = -1;

    // ── Colors ─────────────────────────────────────────────
    private static final int COL_PROGRESS  = 0xFF808080;
    private static final int COL_OUTPUT    = 0xFFA9B7C6;
    private static final int COL_ERROR     = 0xFFFF6B6B;
    private static final int COL_SUCCESS   = 0xFF499C54;
    private static final int COL_SEPARATOR = 0xFF4A86C8;

    private static final String DEFAULT_CODE =
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "}\n";

    // ══════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("com.ccs.javadroid.prefs", MODE_PRIVATE);
        currentFontSize = prefs.getInt("font_size", 14);

        bindViews();
        setupBackHandling();
        setupToolbar();
        setupTabs();
        setupFileTree();
        setupEditor();
        setupFindBar();
        setupBottomTabs();
        setupProblemsList();
        setupProject();
        initLiveProblemsScheduler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (liveProblemsScheduler != null) {
            liveProblemsScheduler.start();
        }
    }

    @Override
    protected void onPause() {
        if (liveProblemsScheduler != null) {
            liveProblemsScheduler.stop();
        }
        super.onPause();
        saveCurrentToActiveTab();
    }

    // ══════════════════════════════════════════════════════════
    //  Setup
    // ══════════════════════════════════════════════════════════

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (findBar.getVisibility() == View.VISIBLE) {
                    hideFindBar();
                    return;
                }
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                setEnabled(false);
                MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
    }

    private void bindViews() {
        drawerLayout     = findViewById(R.id.drawerLayout);
        editor           = findViewById(R.id.editor);
        tabsRecycler     = findViewById(R.id.tabsRecycler);
        fileTreeRecycler = findViewById(R.id.fileTreeRecycler);
        findBar          = findViewById(R.id.findBar);
        etFind           = findViewById(R.id.etFind);
        etReplace        = findViewById(R.id.etReplace);
        toolbarTitle     = findViewById(R.id.toolbarTitle);
        statusLineCol    = findViewById(R.id.statusLineCol);
        statusFileName   = findViewById(R.id.statusFileName);
        consoleScroll    = findViewById(R.id.consoleScroll);
        consoleOutput    = findViewById(R.id.consoleOutput);
        problemsRecycler = findViewById(R.id.problemsRecycler);
        tabRun           = findViewById(R.id.tabRun);
        tabProblems      = findViewById(R.id.tabProblems);
        tabBytecode      = findViewById(R.id.tabBytecode);
        bytecodeOuterScroll = findViewById(R.id.bytecodeOuterScroll);
        bytecodeOutput   = findViewById(R.id.bytecodeOutput);
        btnClearConsole  = findViewById(R.id.btnClearConsole);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        // Clear console button in bottom panel
        if (btnClearConsole != null) {
            btnClearConsole.setOnClickListener(v -> consoleOutput.setText(""));
        }
    }

    private void setupTabs() {
        tabsAdapter = new TabsAdapter();
        tabsAdapter.setTabListener(new TabsAdapter.TabListener() {
            @Override public void onTabSelected(int index) { switchTab(index); }
            @Override public void onTabClosed(int index)   { closeTab(index);  }
        });
        tabsRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tabsRecycler.setAdapter(tabsAdapter);

        // "+" new tab button
        View btnNewTab = findViewById(R.id.btnNewTab);
        if (btnNewTab != null) btnNewTab.setOnClickListener(v -> showNewFileDialog());
    }

    private void setupFileTree() {
        fileTreeAdapter = new FileTreeAdapter();
        fileTreeAdapter.setNodeListener(new FileTreeAdapter.NodeListener() {
            @Override
            public void onNodeClicked(FileTreeNode node) {
                if (node.directory) return;
                openFile(node.path);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            @Override
            public void onNodeLongClicked(FileTreeNode node) {
                if (node.directory) return;
                showFileContextMenu(node.path);
            }
        });
        fileTreeRecycler.setLayoutManager(new LinearLayoutManager(this));
        fileTreeRecycler.setAdapter(fileTreeAdapter);

        View btnNewFile = findViewById(R.id.btnNewFile);
        if (btnNewFile != null) btnNewFile.setOnClickListener(v -> showNewFileDialog());
    }

    private void setupEditor() {
        editor.setColorScheme(new SchemeDarcula());
        editor.setEditorLanguage(new JavaDroidLanguage(this, null));
        editor.setTextSize(currentFontSize);
        editor.setLineNumberEnabled(true);
        editor.setEditable(false);

        // Cursor position → status bar
        statusLineCol.setText(getString(R.string.status_line, 1, 1));
        editor.subscribeEvent(SelectionChangeEvent.class, (event, sub) -> {
            int ln  = event.getLeft().line + 1;
            int col = event.getLeft().column + 1;
            runOnUiThread(() -> statusLineCol.setText(getString(R.string.status_line, ln, col)));
        });

        // Content change → mark tab as modified
        editor.subscribeEvent(ContentChangeEvent.class, (event, sub) -> {
            int idx = tabsAdapter.getActiveIndex();
            if (idx >= 0) runOnUiThread(() -> tabsAdapter.markModified(idx, true));
        });
    }

    private void setupFindBar() {
        etFind.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                lastSearchOffset = -1;
                if (!s.toString().isEmpty()) performFind(true);
            }
        });

        findViewById(R.id.btnFindNext).setOnClickListener(v -> performFind(true));
        findViewById(R.id.btnFindPrev).setOnClickListener(v -> performFind(false));

        findViewById(R.id.btnReplace).setOnClickListener(v -> performReplace());

        findViewById(R.id.btnReplaceAll).setOnClickListener(v -> {
            String query = etFind.getText().toString();
            String repl  = etReplace.getText().toString();
            if (query.isEmpty()) return;
            String newContent = editor.getText().toString().replace(query, repl);
            editor.setText(newContent);
            lastSearchOffset = -1;
            int idx = tabsAdapter.getActiveIndex();
            if (idx >= 0) tabsAdapter.markModified(idx, true);
        });

        findViewById(R.id.btnFindClose).setOnClickListener(v -> hideFindBar());
    }

    private void setupBottomTabs() {
        tabRun.setOnClickListener(v -> switchBottomPanel(PANEL_RUN));
        tabProblems.setOnClickListener(v -> switchBottomPanel(PANEL_PROBLEMS));
        tabBytecode.setOnClickListener(v -> switchBottomPanel(PANEL_BYTECODE));
        switchBottomPanel(PANEL_RUN);
    }

    private void switchBottomPanel(int mode) {
        bottomPanelMode = mode;
        consoleScroll.setVisibility(mode == PANEL_RUN ? View.VISIBLE : View.GONE);
        problemsRecycler.setVisibility(mode == PANEL_PROBLEMS ? View.VISIBLE : View.GONE);
        bytecodeOuterScroll.setVisibility(mode == PANEL_BYTECODE ? View.VISIBLE : View.GONE);
        tabRun.setBackgroundColor(mode == PANEL_RUN ? 0xFF4E5254 : 0xFF3C3F41);
        tabProblems.setBackgroundColor(mode == PANEL_PROBLEMS ? 0xFF4E5254 : 0xFF3C3F41);
        tabBytecode.setBackgroundColor(mode == PANEL_BYTECODE ? 0xFF4E5254 : 0xFF3C3F41);
        tabRun.setTextColor(mode == PANEL_RUN ? 0xFF499C54 : 0xFF808080);
        tabProblems.setTextColor(mode == PANEL_PROBLEMS ? 0xFFBBBBBB : 0xFF808080);
        tabBytecode.setTextColor(mode == PANEL_BYTECODE ? 0xFFCCC77A : 0xFF808080);
        if (btnClearConsole != null) {
            btnClearConsole.setVisibility(mode == PANEL_RUN ? View.VISIBLE : View.GONE);
        }
        if (mode == PANEL_BYTECODE) {
            refreshBytecodePanel();
        }
    }

    private void refreshBytecodePanel() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null || !tab.file.getName().endsWith(".java")) {
            bytecodeOutput.setText(getString(R.string.bytecode_only_java));
            return;
        }
        if (bytecodeRefreshRunning) {
            return;
        }
        bytecodeRefreshRunning = true;
        saveCurrentToActiveTab();
        bytecodeOutput.setText(getString(R.string.bytecode_compiling));
        final String source = editor.getText() != null ? editor.getText().toString() : "";
        final File javaFile = tab.file;
        new Thread(() -> {
            try {
                ProjectCompiler.BytecodeCompileResult r = ProjectCompiler.compileForBytecodeView(
                        this, javaFile, source, projectManager.getProjectDir());
                if (r.errorMessage != null) {
                    final String err = r.errorMessage;
                    runOnUiThread(() -> {
                        bytecodeRefreshRunning = false;
                        bytecodeOutput.setText(getString(R.string.bytecode_failed, err));
                    });
                    return;
                }
                byte[] bytes = Files.readAllBytes(r.classFile.toPath());
                String raw = BytecodeDisassembler.disassemble(bytes);
                Spannable sp = BytecodeHighlighter.highlight(raw);
                runOnUiThread(() -> {
                    bytecodeRefreshRunning = false;
                    bytecodeOutput.setText(sp);
                });
            } catch (Throwable t) {
                String msg = t.getMessage() != null ? t.getMessage() : String.valueOf(t);
                runOnUiThread(() -> {
                    bytecodeRefreshRunning = false;
                    bytecodeOutput.setText(getString(R.string.bytecode_failed, msg));
                });
            }
        }, "bytecode-view").start();
    }

    private void setupProblemsList() {
        problemsAdapter = new ProblemsAdapter();
        problemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        problemsRecycler.setAdapter(problemsAdapter);
        problemsAdapter.setListener(item -> {
            if (item.file != null && item.file.exists()) {
                openFile(item.file);
                switchBottomPanel(PANEL_RUN);
                editor.postDelayed(() -> {
                    if (item.line > 0) {
                        editor.setSelectionRegion(item.line - 1, 0, item.line - 1, 0);
                    }
                }, 120);
            }
        });
    }

    private void applyEditorLanguage(File file) {
        // Для .xml зараз той самий рушій, що й для Java (окремий XML-lexer недоступний у цій версії артефактів).
        editor.setEditorLanguage(new JavaDroidLanguage(this, projectManager.getProjectDir()));
    }

    private void setupProject() {
        projectManager = new ProjectManager(this);

        String saved = prefs.getString("project_root", null);
        File root = null;
        if (saved != null) {
            File t = new File(saved);
            if (t.isDirectory()) root = t;
        }
        if (root == null) {
            File candidate = MavenPaths.projectDir(this, "MyApp");
            if (candidate.exists() && new File(candidate, "pom.xml").exists()) {
                root = candidate;
            } else {
                try {
                    root = MavenProjectFactory.create(this, "MyApp");
                } catch (Exception e) {
                    root = new File(getFilesDir(), "project");
                    root.mkdirs();
                }
            }
            prefs.edit().putString("project_root", root.getAbsolutePath()).apply();
        }

        projectManager.setProjectRoot(root);
        updateToolbarTitle();
        setupToolbarProjectPathLongClick();
        refreshFileTree();

        List<File> files = projectManager.getJavaFiles();
        if (files.isEmpty()) {
            if (projectManager.isMavenProject()) {
                try {
                    File pkgDir = ProjectLayoutHelper.mainJavaPackageDir(root);
                    String pkg = ProjectLayoutHelper.mainPackageName(root);
                    File app = new File(pkgDir, "App.java");
                    projectManager.writeFile(app,
                            "package " + pkg + ";\n\npublic class App {\n"
                                    + "    public static void main(String[] args) {\n"
                                    + "        System.out.println(\"Hello\");\n"
                                    + "    }\n}\n");
                    refreshFileTree();
                    openFile(app);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    File main = projectManager.createFile("Main", DEFAULT_CODE);
                    if (main != null) openFile(main);
                } catch (IOException e) {
                    Toast.makeText(this, R.string.error_cannot_create_main, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            openFile(files.get(0));
        }
        refreshProblemsMergedAsync();
        editor.setEditorLanguage(new JavaDroidLanguage(this, projectManager.getProjectDir()));
    }

    private void initLiveProblemsScheduler() {
        liveProblemsScheduler = new LiveProblemsScheduler(this,
                new LiveProblemsScheduler.Sources() {
                    @Override
                    public String getEditorText() {
                        return editor.getText() != null ? editor.getText().toString() : "";
                    }

                    @Override
                    public File getActiveJavaFile() {
                        FileTab t = tabsAdapter.getActiveTab();
                        if (t == null || t.file == null) {
                            return null;
                        }
                        if (!t.file.getName().endsWith(".java")) {
                            return null;
                        }
                        return t.file;
                    }

                    @Override
                    public File getProjectRoot() {
                        return projectManager.getProjectDir();
                    }

                    @Override
                    public boolean shouldSkipScan() {
                        return isRunning;
                    }
                },
                problemsAdapter::setItems);
    }

    /** ECJ (активний файл) + static (проєкт); з диска для інших файлів. */
    private void refreshProblemsMergedAsync() {
        final String text = editor.getText() != null ? editor.getText().toString() : "";
        FileTab tab = tabsAdapter.getActiveTab();
        final File active = (tab != null && tab.file != null && tab.file.getName().endsWith(".java"))
                ? tab.file
                : null;
        new Thread(() -> {
            List<ProblemItem> list = ProblemsWorkspaceAnalyzer.analyze(
                    getApplicationContext(), projectManager.getProjectDir(), text, active);
            runOnUiThread(() -> problemsAdapter.setItems(list));
        }, "problems-refresh").start();
    }

    private void updateToolbarTitle() {
        if (toolbarTitle != null) {
            toolbarTitle.setText(projectManager.getProjectDir().getName());
        }
    }

    /** Long-press project name in the toolbar to copy the project root path. */
    private void setupToolbarProjectPathLongClick() {
        if (toolbarTitle == null) return;
        toolbarTitle.setContentDescription(getString(R.string.toolbar_project_a11y));
        toolbarTitle.setOnLongClickListener(v -> {
            if (projectManager == null) return false;
            File dir = projectManager.getProjectDir();
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("JavaDroid project", dir.getAbsolutePath()));
                Toast.makeText(this, R.string.toast_project_path_copied, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void copyConsoleToClipboard() {
        CharSequence text = consoleOutput.getText();
        if (text == null || text.length() == 0) {
            Toast.makeText(this, R.string.toast_console_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("JavaDroid console", text));
            Toast.makeText(this, R.string.toast_copied_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareCurrentFile() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();
        try {
            String content = projectManager.readFile(tab.file);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, tab.file.getName());
            send.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(send, getString(R.string.share_file_chooser)));
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_cannot_read, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Menu
    // ══════════════════════════════════════════════════════════

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if      (id == R.id.action_run)           { runCurrentFile();       return true; }
        else if (id == R.id.action_save)          { saveCurrentFile();      return true; }
        else if (id == R.id.action_find)          { toggleFindBar();        return true; }
        else if (id == R.id.action_bytecode)      { switchBottomPanel(PANEL_BYTECODE); return true; }
        else if (id == R.id.action_undo)          { editor.undo();          return true; }
        else if (id == R.id.action_redo)          { editor.redo();          return true; }
        else if (id == R.id.action_new_file)         { showNewFileDialog();       return true; }
        else if (id == R.id.action_new_maven_project){ showNewMavenProjectDialog(); return true; }
        else if (id == R.id.action_sync_deps)        { syncDependencies();        return true; }
        else if (id == R.id.action_maven_package)    { mavenPackage();            return true; }
        else if (id == R.id.action_maven_test)       { mavenTestCompile();        return true; }
        else if (id == R.id.action_settings)         { showSettingsDialog();      return true; }
        else if (id == R.id.action_clear_console)    { consoleOutput.setText(""); return true; }
        else if (id == R.id.action_copy_console)     { copyConsoleToClipboard(); return true; }
        else if (id == R.id.action_share_file)       { shareCurrentFile(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // ══════════════════════════════════════════════════════════
    //  Tab management
    // ══════════════════════════════════════════════════════════

    private void openFile(File file) {
        int existing = tabsAdapter.indexOfFile(file);
        if (existing >= 0) {
            switchTab(existing);
            return;
        }
        saveCurrentToActiveTab();
        try {
            String content = projectManager.readFile(file);
            FileTab tab = new FileTab(file);
            tabsAdapter.addTab(tab);
            int idx = tabsAdapter.getTabs().size() - 1;
            tabsAdapter.setActiveIndex(idx);
            editor.setText(content);
            applyEditorLanguage(file);
            editor.setEditable(true);
            tabsRecycler.scrollToPosition(idx);
            updateStatusFileName(file);
            fileTreeAdapter.setActiveFile(file);
            refreshProblemsMergedAsync();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_cannot_open, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void switchTab(int index) {
        if (index == tabsAdapter.getActiveIndex()) return;
        saveCurrentToActiveTab();
        FileTab tab = tabsAdapter.getTabs().get(index);
        try {
            String content = projectManager.readFile(tab.file);
            editor.setText(content);
            applyEditorLanguage(tab.file);
            editor.setEditable(true);
            tabsAdapter.setActiveIndex(index);
            tabsAdapter.markModified(index, false);
            tabsRecycler.scrollToPosition(index);
            updateStatusFileName(tab.file);
            fileTreeAdapter.setActiveFile(tab.file);
            refreshProblemsMergedAsync();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_cannot_read, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeTab(int index) {
        if (index < 0 || index >= tabsAdapter.getTabs().size()) return;
        FileTab tab = tabsAdapter.getTabs().get(index);
        if (tab.isModified) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_unsaved_title)
                    .setMessage(getString(R.string.dialog_unsaved_message, tab.file.getName()))
                    .setPositiveButton(R.string.dialog_save_close, (d, w) -> { saveTab(index); doCloseTab(index); })
                    .setNegativeButton(R.string.dialog_discard, (d, w) -> doCloseTab(index))
                    .setNeutralButton(R.string.dialog_cancel, null)
                    .show();
        } else {
            doCloseTab(index);
        }
    }

    private void doCloseTab(int index) {
        int active = tabsAdapter.getActiveIndex();
        tabsAdapter.removeTab(index);

        if (tabsAdapter.getTabs().isEmpty()) {
            editor.setText("");
            editor.setEditable(false);
            statusFileName.setText("");
            fileTreeAdapter.setActiveFile(null);
            return;
        }

        int next = (index < active) ? active - 1
                 : (index == active) ? Math.min(index, tabsAdapter.getTabs().size() - 1)
                 : active;

        // Load the next tab without saving (we already removed it)
        FileTab tab = tabsAdapter.getTabs().get(next);
        try {
            editor.setText(projectManager.readFile(tab.file));
            applyEditorLanguage(tab.file);
            editor.setEditable(true);
            tabsAdapter.setActiveIndex(next);
            tabsRecycler.scrollToPosition(next);
            updateStatusFileName(tab.file);
            fileTreeAdapter.setActiveFile(tab.file);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_cannot_read, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentFile() {
        int idx = tabsAdapter.getActiveIndex();
        if (idx < 0) return;
        saveTab(idx);
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
    }

    private void saveTab(int index) {
        if (index != tabsAdapter.getActiveIndex()) return;
        FileTab tab = tabsAdapter.getTabs().get(index);
        try {
            projectManager.writeFile(tab.file, editor.getText().toString());
            tabsAdapter.markModified(index, false);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.toast_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentToActiveTab() {
        int idx = tabsAdapter.getActiveIndex();
        if (idx < 0 || tabsAdapter.getTabs().isEmpty()) return;
        FileTab tab = tabsAdapter.getTabs().get(idx);
        try {
            projectManager.writeFile(tab.file, editor.getText().toString());
            tab.isModified = false;
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.toast_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Run
    // ══════════════════════════════════════════════════════════

    private void runCurrentFile() {
        if (isRunning) return;
        FileTab activeTab = tabsAdapter.getActiveTab();
        if (activeTab == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();

        if (projectManager.isMavenProject()) {
            isRunning = true;
            consoleOutput.setText("");
            switchBottomPanel(PANEL_RUN);
            appendConsole(getString(R.string.console_maven_run), COL_PROGRESS);
            try {
                PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
                ProjectCompiler.mavenCompileAndRun(this, projectManager.getProjectDir(), pom,
                        new ProjectCompiler.Callback() {
                            @Override
                            public void onProgress(String msg) {
                                appendConsole("   " + msg, COL_PROGRESS);
                            }

                            @Override
                            public void onResult(String output) {
                                isRunning = false;
                                appendConsole("", COL_SEPARATOR);
                                appendConsole(getString(R.string.console_output_separator), COL_SEPARATOR);
                                if (output == null || output.trim().isEmpty()) {
                                    appendConsole(getString(R.string.console_build_success), COL_SUCCESS);
                                    refreshProblemsMergedAsync();
                                    return;
                                }
                                boolean err = output.startsWith("Compilation Error")
                                        || output.startsWith("Execution Exception")
                                        || output.startsWith("System Error")
                                        || output.startsWith("Error:");
                                appendConsole(output.trim(), err ? COL_ERROR : COL_OUTPUT);
                                if (!err) {
                                    appendConsole("\n" + getString(R.string.console_build_success), COL_SUCCESS);
                                }
                                boolean compilationFailed = output.startsWith("Compilation Error");
                                if (!compilationFailed) {
                                    refreshProblemsMergedAsync();
                                }
                            }

                            @Override
                            public void onProblems(List<ProblemItem> problems) {
                                problemsAdapter.setItems(problems);
                                if (problems != null && !problems.isEmpty()) {
                                    switchBottomPanel(PANEL_PROBLEMS);
                                }
                            }
                        });
            } catch (Exception e) {
                isRunning = false;
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        isRunning = true;
        consoleOutput.setText("");
        switchBottomPanel(PANEL_RUN);
        appendConsole(getString(R.string.console_running_file, activeTab.file.getName()), COL_PROGRESS);

        ProjectCompiler.runSingleSource(this, editor.getText().toString(), activeTab.file,
                new ProjectCompiler.Callback() {
                    @Override
                    public void onProgress(String msg) {
                        appendConsole("   " + msg, COL_PROGRESS);
                    }

                    @Override
                    public void onResult(String output) {
                        isRunning = false;
                        appendConsole("", COL_SEPARATOR);
                        appendConsole(getString(R.string.console_output_separator), COL_SEPARATOR);
                        if (output == null || output.trim().isEmpty()) {
                            appendConsole(getString(R.string.console_process_exit_ok), COL_SUCCESS);
                            return;
                        }
                        boolean isError = output.startsWith("Compilation Error")
                                || output.startsWith("Execution Exception")
                                || output.startsWith("System Error")
                                || output.startsWith("Error:");
                        appendConsole(output.trim(), isError ? COL_ERROR : COL_OUTPUT);
                        if (!isError) {
                            appendConsole("\n" + getString(R.string.console_process_exit_ok), COL_SUCCESS);
                        }
                    }

                    @Override
                    public void onProblems(List<ProblemItem> problems) {
                        problemsAdapter.setItems(problems);
                        if (problems != null && !problems.isEmpty()) {
                            switchBottomPanel(PANEL_PROBLEMS);
                        }
                    }
                });
    }

    private void syncDependencies() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_not_maven, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mavenSyncInProgress) {
            return;
        }
        mavenSyncInProgress = true;
        switchBottomPanel(PANEL_RUN);
        consoleOutput.setText("");
        appendConsole(getString(R.string.console_sync_maven), COL_PROGRESS);
        new Thread(() -> {
            try {
                PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
                MavenDependencyResolver.resolve(projectManager.getProjectDir(), pom, line ->
                        runOnUiThread(() -> appendConsole("   " + line, COL_PROGRESS)));
                runOnUiThread(() -> {
                    mavenSyncInProgress = false;
                    appendConsole(getString(R.string.console_sync_done), COL_SUCCESS);
                    Toast.makeText(this, R.string.toast_sync_done, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mavenSyncInProgress = false;
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }, "maven-sync").start();
    }

    private void mavenPackage() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_pom_required, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();
        switchBottomPanel(PANEL_RUN);
        consoleOutput.setText("");
        appendConsole(getString(R.string.console_mvn_package), COL_PROGRESS);
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
            ProjectCompiler.mavenPackage(this, projectManager.getProjectDir(), pom,
                    new ProjectCompiler.Callback() {
                        @Override public void onProgress(String msg) {
                            appendConsole("   " + msg, COL_PROGRESS);
                        }
                        @Override public void onResult(String output) {
                            appendConsole(output, COL_OUTPUT);
                        }
                        @Override public void onProblems(List<ProblemItem> problems) {
                            if (problems != null) problemsAdapter.setItems(problems);
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mavenTestCompile() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_pom_required, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();
        switchBottomPanel(PANEL_RUN);
        consoleOutput.setText("");
        appendConsole(getString(R.string.console_mvn_test_compile), COL_PROGRESS);
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
            ProjectCompiler.mavenTestCompile(this, projectManager.getProjectDir(), pom,
                    new ProjectCompiler.Callback() {
                        @Override public void onProgress(String msg) {
                            appendConsole("   " + msg, COL_PROGRESS);
                        }
                        @Override public void onResult(String output) {
                            appendConsole(output, COL_OUTPUT);
                        }
                        @Override public void onProblems(List<ProblemItem> problems) {
                            problemsAdapter.setItems(problems);
                            if (problems != null && !problems.isEmpty()) switchBottomPanel(PANEL_PROBLEMS);
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showNewMavenProjectDialog() {
        int pad = dp(12);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad, pad, pad);

        EditText etName = newEditForDialog(getString(R.string.dialog_maven_name_hint));
        EditText etGroup = newEditForDialog(getString(R.string.dialog_maven_group_hint));
        EditText etArtifact = newEditForDialog(getString(R.string.dialog_maven_artifact_hint));

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
                        prefs.edit().putString("project_root", root.getAbsolutePath()).apply();
                        recreate();
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
        e.setHintTextColor(0xFF666666);
        e.setTextColor(0xFFBBBBBB);
        e.setBackgroundColor(0xFF2B2B2B);
        e.setPadding(32, 16, 32, 16);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    // ══════════════════════════════════════════════════════════
    //  Console
    // ══════════════════════════════════════════════════════════

    private void appendConsole(String text, int color) {
        SpannableString ss = new SpannableString(text + "\n");
        ss.setSpan(new ForegroundColorSpan(color), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        consoleOutput.append(ss);
        consoleScroll.post(() -> consoleScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    // ══════════════════════════════════════════════════════════
    //  Find & Replace
    // ══════════════════════════════════════════════════════════

    private void toggleFindBar() {
        if (findBar.getVisibility() == View.VISIBLE) {
            hideFindBar();
        } else {
            findBar.setVisibility(View.VISIBLE);
            etFind.requestFocus();
            showKeyboard(etFind);
        }
    }

    private void hideFindBar() {
        findBar.setVisibility(View.GONE);
        lastSearchOffset = -1;
        etFind.setText("");
        etReplace.setText("");
        hideKeyboard();
    }

    private void performFind(boolean forward) {
        String query = etFind.getText().toString();
        if (query.isEmpty()) return;

        String fullText  = editor.getText().toString();
        String textLower = fullText.toLowerCase();
        String queryLow  = query.toLowerCase();

        int foundIdx;
        if (forward) {
            int start = (lastSearchOffset >= 0) ? lastSearchOffset + 1 : 0;
            foundIdx = textLower.indexOf(queryLow, start);
            if (foundIdx == -1) foundIdx = textLower.indexOf(queryLow);
        } else {
            int start = (lastSearchOffset > 0) ? lastSearchOffset - 1 : fullText.length() - 1;
            foundIdx = textLower.lastIndexOf(queryLow, start);
            if (foundIdx == -1) foundIdx = textLower.lastIndexOf(queryLow);
        }

        if (foundIdx == -1) {
            Toast.makeText(this, getString(R.string.find_not_found, query), Toast.LENGTH_SHORT).show();
            return;
        }
        lastSearchOffset = foundIdx;

        int[] s = offsetToLineCol(fullText, foundIdx);
        int[] e = offsetToLineCol(fullText, foundIdx + query.length());
        editor.setSelectionRegion(s[0], s[1], e[0], e[1]);
    }

    private void performReplace() {
        String query = etFind.getText().toString();
        String repl  = etReplace.getText().toString();
        if (query.isEmpty() || lastSearchOffset < 0) return;

        String text = editor.getText().toString();
        int end = lastSearchOffset + query.length();
        if (end > text.length()) return;

        if (text.substring(lastSearchOffset, end).equalsIgnoreCase(query)) {
            String newText = text.substring(0, lastSearchOffset) + repl
                           + text.substring(end);
            editor.setText(newText);
            int idx = tabsAdapter.getActiveIndex();
            if (idx >= 0) tabsAdapter.markModified(idx, true);
            lastSearchOffset = -1;
            performFind(true);
        }
    }

    private int[] offsetToLineCol(String text, int offset) {
        int line = 0, col = 0;
        int len = Math.min(offset, text.length());
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) == '\n') { line++; col = 0; }
            else col++;
        }
        return new int[]{line, col};
    }

    // ══════════════════════════════════════════════════════════
    //  File Manager
    // ══════════════════════════════════════════════════════════

    private void refreshFileTree() {
        fileTreeAdapter.setNodes(ProjectScanner.listIdeaStyleTree(projectManager.getProjectDir()));
    }

    private void showNewFileDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.dialog_new_java_hint);
        input.setHintTextColor(0xFF666666);
        input.setTextColor(0xFFBBBBBB);
        input.setBackgroundColor(0xFF2B2B2B);
        input.setPadding(32, 24, 32, 24);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_new_java_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String className = name.replace(".java", "");
                    String template;
                    if (projectManager.isMavenProject()) {
                        try {
                            String pkg = ProjectLayoutHelper.mainPackageName(projectManager.getProjectDir());
                            template = "package " + pkg + ";\n\npublic class " + className + " {\n\n}\n";
                        } catch (Exception e) {
                            template = "public class " + className + " {\n\n}\n";
                        }
                    } else {
                        template = "public class " + className + " {\n\n}\n";
                    }
                    try {
                        File f = projectManager.createFile(className, template);
                        if (f != null) {
                            refreshFileTree();
                            openFile(f);
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            Toast.makeText(this, getString(R.string.toast_file_exists, className),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(this, getString(R.string.toast_error_prefix, e.getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showFileContextMenu(File file) {
        String[] options = {
                getString(R.string.dialog_file_context_open),
                getString(R.string.dialog_file_context_rename),
                getString(R.string.dialog_file_context_delete)
        };
        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: openFile(file); break;
                        case 1: showRenameDialog(file); break;
                        case 2: showDeleteDialog(file); break;
                    }
                })
                .show();
    }

    private void showRenameDialog(File file) {
        EditText input = new EditText(this);
        input.setText(file.getName().replace(".java", ""));
        input.setTextColor(0xFFBBBBBB);
        input.setBackgroundColor(0xFF2B2B2B);
        input.setPadding(32, 24, 32, 24);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_rename)
                .setView(input)
                .setPositiveButton(R.string.dialog_rename, (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty() || newName.equals(file.getName().replace(".java", ""))) return;
                    File parent = file.getParentFile() != null ? file.getParentFile() : projectManager.getProjectDir();
                    File newFile = new File(parent, newName + ".java");
                    int tabIdx = tabsAdapter.indexOfFile(file);
                    if (tabIdx >= 0) saveCurrentToActiveTab();
                    if (file.renameTo(newFile)) {
                        if (tabIdx >= 0) {
                            tabsAdapter.removeTab(tabIdx);
                        }
                        refreshFileTree();
                        openFile(newFile);
                    } else {
                        Toast.makeText(this, R.string.toast_rename_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showDeleteDialog(File file) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_file_title)
                .setMessage(getString(R.string.dialog_delete_file_message, file.getName()))
                .setPositiveButton(R.string.dialog_delete, (d, w) -> {
                    int tabIdx = tabsAdapter.indexOfFile(file);
                    if (tabIdx >= 0) doCloseTab(tabIdx);
                    projectManager.deleteFile(file);
                    refreshFileTree();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    // ══════════════════════════════════════════════════════════
    //  Settings
    // ══════════════════════════════════════════════════════════

    private void showSettingsDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF3C3F41);
        int pad = dp(20);
        layout.setPadding(pad, pad, pad, pad / 2);

        TextView fontLabel = new TextView(this);
        fontLabel.setText(getString(R.string.dialog_font_size, currentFontSize));
        fontLabel.setTextColor(0xFFBBBBBB);
        fontLabel.setTextSize(13);
        layout.addView(fontLabel);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(20);  // range: 8–28
        seekBar.setProgress(currentFontSize - 8);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int size = progress + 8;
                fontLabel.setText(getString(R.string.dialog_font_size, size));
                editor.setTextSize(size);
                currentFontSize = size;
            }
        });
        layout.addView(seekBar);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_settings_title)
                .setView(layout)
                .setPositiveButton(R.string.dialog_apply, (d, w) ->
                        prefs.edit().putInt("font_size", currentFontSize).apply())
                .setNegativeButton(R.string.dialog_cancel, (d, w) -> {
                    // restore
                    int saved = prefs.getInt("font_size", 14);
                    editor.setTextSize(saved);
                    currentFontSize = saved;
                })
                .show();
    }

    // ══════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════

    private void updateStatusFileName(File file) {
        statusFileName.setText(file.getName());
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_S:
                    saveCurrentFile();
                    return true;
                case KeyEvent.KEYCODE_F:
                    if (findBar.getVisibility() != View.VISIBLE) {
                        toggleFindBar();
                    } else {
                        etFind.requestFocus();
                        showKeyboard(etFind);
                    }
                    return true;
                case KeyEvent.KEYCODE_R:
                    runCurrentFile();
                    return true;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
