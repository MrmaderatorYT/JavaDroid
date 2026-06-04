package com.ccs.javadroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;

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
    private View          tabsBar;
    private View          tabBorder;
    private View          bottomTabsBar;
    private Toolbar       toolbar;
    private View          keyAccessoryBar;
    private LinearLayout  accessoryBarLayout;

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
    private AppPreferences    appPrefs;
    private AppTheme          theme;
    private boolean isRunning        = false;
    private int     lastSearchOffset = -1;

    private static final int REQ_SETTINGS    = 4001;
    private static final int REQ_OPEN_FILE   = 4002;
    private static final int REQ_SAVE_AS     = 4003;
    private static final int REQ_EXPORT_PROJ = 4004;

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

        prefs = getSharedPreferences(AppPreferences.PREFS_NAME, MODE_PRIVATE);
        appPrefs = new AppPreferences(this);
        theme    = AppTheme.byId(appPrefs.getThemeId(), appPrefs);

        bindViews();
        applyTheme();
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
        tabsBar          = findViewById(R.id.tabsBar);
        tabBorder        = findViewById(R.id.tabBorder);
        bottomTabsBar    = findViewById(R.id.bottomTabsBar);
        statusBar        = findViewById(R.id.statusBar);
        toolbar          = findViewById(R.id.toolbar);
        keyAccessoryBar  = findViewById(R.id.keyAccessoryBar);
        accessoryBarLayout = findViewById(R.id.accessoryBarLayout);
    }

    /** Перефарбовує статичні UI-елементи відповідно до поточної теми. */
    private void applyTheme() {
        if (drawerLayout != null) drawerLayout.setBackgroundColor(theme.bg);
        View drawerRoot   = findViewById(R.id.drawerRoot);
        View drawerHeader = findViewById(R.id.drawerHeader);
        if (drawerRoot != null)   drawerRoot.setBackgroundColor(theme.toolbar);
        if (drawerHeader != null) drawerHeader.setBackgroundColor(theme.bg);
        if (toolbar != null)      toolbar.setBackgroundColor(theme.toolbar);
        if (toolbarTitle != null) toolbarTitle.setTextColor(theme.text);
        if (tabsBar != null)      tabsBar.setBackgroundColor(theme.toolbar);
        if (tabBorder != null)    tabBorder.setBackgroundColor(theme.accent);
        if (bottomTabsBar != null) bottomTabsBar.setBackgroundColor(theme.toolbar);
        if (statusBar != null)    statusBar.setBackgroundColor(theme.statusBar);
        if (consoleScroll != null) consoleScroll.setBackgroundColor(theme.consoleBg);
        if (consoleOutput != null) consoleOutput.setTextColor(theme.consoleText);
        if (problemsRecycler != null) problemsRecycler.setBackgroundColor(theme.consoleBg);
        if (bytecodeOuterScroll != null) bytecodeOuterScroll.setBackgroundColor(theme.consoleBg);
        if (bytecodeOutput != null) bytecodeOutput.setTextColor(theme.consoleText);
        // Status bar дочірні: дивайдери та текстові підписи (UTF-8 / Java)
        if (statusBar instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) statusBar;
            for (int i = 0; i < g.getChildCount(); i++) {
                View v = g.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(theme.textDim);
                } else if (v.getBackground() != null) {
                    v.setBackgroundColor(theme.separator);
                }
            }
        }
        if (statusLineCol != null) statusLineCol.setTextColor(theme.text);
        if (statusFileName != null) statusFileName.setTextColor(theme.textDim);
        if (keyAccessoryBar != null) keyAccessoryBar.setBackgroundColor(theme.toolbar);
        setupKeyAccessoryBar();
    }

    private void setupKeyAccessoryBar() {
        if (accessoryBarLayout == null) return;
        accessoryBarLayout.removeAllViews();

        final String[] symbols = { "{", "}", "(", ")", "[", "]", ";", ".", "=", "\"", "+", "-", "*", "/", "Tab" };

        for (final String symbol : symbols) {
            TextView btn = new TextView(this);
            btn.setText(symbol);
            btn.setTextColor(theme.text);
            btn.setTextSize(14);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(dp(12), 0, dp(12), 0);

            android.util.TypedValue tv = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
            if (tv.resourceId != 0) {
                btn.setBackgroundResource(tv.resourceId);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            btn.setLayoutParams(lp);
            btn.setClickable(true);
            btn.setFocusable(true);

            btn.setOnClickListener(v -> {
                if (editor != null && editor.isEditable()) {
                    if ("Tab".equals(symbol)) {
                        int tabSize = appPrefs.getTabSize();
                        StringBuilder spaces = new StringBuilder();
                        for (int i = 0; i < tabSize; i++) spaces.append(" ");
                        editor.insertText(spaces.toString(), spaces.length());
                    } else {
                        editor.insertText(symbol, symbol.length());
                    }
                }
            });
            accessoryBarLayout.addView(btn);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        Drawable nav = toolbar.getNavigationIcon();
        if (nav != null) nav.setColorFilter(theme.text, PorterDuff.Mode.SRC_IN);
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
        editor.setEditorLanguage(new JavaDroidLanguage(this, null));
        EditorSettingsApplier.apply(editor, appPrefs, theme);
        editor.setEditable(false);

        // Cursor position → status bar
        statusLineCol.setText(getString(R.string.status_line, 1, 1));
        editor.subscribeEvent(SelectionChangeEvent.class, (event, sub) -> {
            int ln  = event.getLeft().line + 1;
            int col = event.getLeft().column + 1;
            runOnUiThread(() -> statusLineCol.setText(getString(R.string.status_line, ln, col)));
        });

        // Content change → mark tab as modified + auto-save (if enabled)
        editor.subscribeEvent(ContentChangeEvent.class, (event, sub) -> {
            int idx = tabsAdapter.getActiveIndex();
            if (idx >= 0) runOnUiThread(() -> {
                tabsAdapter.markModified(idx, true);
                if (appPrefs.isAutoSave()) {
                    saveCurrentToActiveTab();
                    tabsAdapter.markModified(idx, false);
                }
            });
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

        int activeBg   = blend(theme.toolbar, theme.bg, 0.4f);
        int inactiveBg = theme.toolbar;
        tabRun.setBackgroundColor(mode == PANEL_RUN ? activeBg : inactiveBg);
        tabProblems.setBackgroundColor(mode == PANEL_PROBLEMS ? activeBg : inactiveBg);
        tabBytecode.setBackgroundColor(mode == PANEL_BYTECODE ? activeBg : inactiveBg);
        tabRun.setTextColor(mode == PANEL_RUN ? theme.successText : theme.textDim);
        tabProblems.setTextColor(mode == PANEL_PROBLEMS ? theme.text : theme.textDim);
        tabBytecode.setTextColor(mode == PANEL_BYTECODE ? theme.accent : theme.textDim);
        if (btnClearConsole != null) {
            btnClearConsole.setVisibility(mode == PANEL_RUN ? View.VISIBLE : View.GONE);
            ((TextView) btnClearConsole).setTextColor(theme.textDim);
        }
        if (mode == PANEL_BYTECODE) {
            refreshBytecodePanel();
        }
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
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
            Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    this, "com.ccs.javadroid.fileprovider", tab.file);
            Intent send = new Intent(Intent.ACTION_SEND);
            String mime = tab.file.getName().endsWith(".java") ? "text/x-java" : "text/plain";
            send.setType(mime);
            send.putExtra(Intent.EXTRA_SUBJECT, tab.file.getName());
            send.putExtra(Intent.EXTRA_STREAM, fileUri);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, getString(R.string.share_file_chooser)));
        } catch (Exception e) {
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
        else if (id == R.id.action_settings)         { openSettings();            return true; }
        else if (id == R.id.action_clear_console)    { consoleOutput.setText(""); return true; }
        else if (id == R.id.action_copy_console)     { copyConsoleToClipboard(); return true; }
        else if (id == R.id.action_share_file)       { shareCurrentFile(); return true; }
        else if (id == R.id.action_open_file)        { pickFileToOpen(); return true; }
        else if (id == R.id.action_save_as)          { saveCurrentAs(); return true; }
        else if (id == R.id.action_export_project)   { exportProjectAsZip(); return true; }
        else if (id == R.id.action_git)              { openGit(); return true; }
        else if (id == R.id.action_git_quick_commit) { showQuickCommitDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void openGit() {
        saveCurrentToActiveTab();
        GitActivity.launch(this, projectManager.getProjectDir());
    }

    private void showQuickCommitDialog() {
        saveCurrentToActiveTab();
        File dir = projectManager.getProjectDir();
        if (!GitManager.isGitRepo(dir)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_git_quick_commit)
                    .setMessage(R.string.git_quick_init_first)
                    .setPositiveButton(R.string.git_init, (d, w) -> {
                        new Thread(() -> {
                            try { GitManager.init(dir); }
                            catch (Exception e) { runOnUiThread(() ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()); return; }
                            runOnUiThread(this::showQuickCommitDialog);
                        }, "git-init").start();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
            return;
        }
        EditText et = new EditText(this);
        et.setHint(R.string.git_commit_hint);
        et.setHintTextColor(theme.textDim);
        et.setTextColor(theme.text);
        et.setPadding(dp(16), dp(12), dp(16), dp(12));
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_git_quick_commit)
                .setView(et)
                .setPositiveButton(R.string.git_commit, (d, w) -> {
                    String msg = et.getText().toString().trim();
                    if (msg.isEmpty()) return;
                    GitCredentialsStore creds = new GitCredentialsStore(this);
                    String name  = creds.authorName();
                    String email = creds.authorEmail();
                    new Thread(() -> {
                        try {
                            GitManager.addAll(dir);
                            GitManager.commit(dir, msg, name, email);
                            runOnUiThread(() -> Toast.makeText(this,
                                    R.string.git_quick_commit_done, Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this,
                                    e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    }, "git-commit").start();
                })
                .setNeutralButton(R.string.menu_git, (d, w) -> openGit())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
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
            appendConsole(getString(R.string.console_maven_run), theme.textDim);
            try {
                PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
                ProjectCompiler.mavenCompileAndRun(this, projectManager.getProjectDir(), pom,
                        new ProjectCompiler.Callback() {
                            @Override
                            public void onProgress(String msg) {
                                appendConsole("   " + msg, theme.textDim);
                            }

                            @Override
                            public void onResult(String output) {
                                isRunning = false;
                                appendConsole("", theme.accent);
                                appendConsole(getString(R.string.console_output_separator), theme.accent);
                                if (output == null || output.trim().isEmpty()) {
                                    appendConsole(getString(R.string.console_build_success), theme.successText);
                                    refreshProblemsMergedAsync();
                                    return;
                                }
                                boolean err = output.startsWith("Compilation Error")
                                        || output.startsWith("Execution Exception")
                                        || output.startsWith("System Error")
                                        || output.startsWith("Error:");
                                appendConsole(output.trim(), err ? theme.errorText : theme.consoleText);
                                if (!err) {
                                    appendConsole("\n" + getString(R.string.console_build_success), theme.successText);
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
        appendConsole(getString(R.string.console_running_file, activeTab.file.getName()), theme.textDim);

        ProjectCompiler.runSingleSource(this, editor.getText().toString(), activeTab.file,
                new ProjectCompiler.Callback() {
                    @Override
                    public void onProgress(String msg) {
                        appendConsole("   " + msg, theme.textDim);
                    }

                    @Override
                    public void onResult(String output) {
                        isRunning = false;
                        appendConsole("", theme.accent);
                        appendConsole(getString(R.string.console_output_separator), theme.accent);
                        if (output == null || output.trim().isEmpty()) {
                            appendConsole(getString(R.string.console_process_exit_ok), theme.successText);
                            return;
                        }
                        boolean isError = output.startsWith("Compilation Error")
                                || output.startsWith("Execution Exception")
                                || output.startsWith("System Error")
                                || output.startsWith("Error:");
                        appendConsole(output.trim(), isError ? theme.errorText : theme.consoleText);
                        if (!isError) {
                            appendConsole("\n" + getString(R.string.console_process_exit_ok), theme.successText);
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
        appendConsole(getString(R.string.console_sync_maven), theme.textDim);
        new Thread(() -> {
            try {
                PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
                MavenDependencyResolver.resolve(projectManager.getProjectDir(), pom, line ->
                        runOnUiThread(() -> appendConsole("   " + line, theme.textDim)));
                runOnUiThread(() -> {
                    mavenSyncInProgress = false;
                    appendConsole(getString(R.string.console_sync_done), theme.successText);
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
        appendConsole(getString(R.string.console_mvn_package), theme.textDim);
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
            ProjectCompiler.mavenPackage(this, projectManager.getProjectDir(), pom,
                    new ProjectCompiler.Callback() {
                        @Override public void onProgress(String msg) {
                            appendConsole("   " + msg, theme.textDim);
                        }
                        @Override public void onResult(String output) {
                            appendConsole(output, theme.consoleText);
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
        appendConsole(getString(R.string.console_mvn_test_compile), theme.textDim);
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
            ProjectCompiler.mavenTestCompile(this, projectManager.getProjectDir(), pom,
                    new ProjectCompiler.Callback() {
                        @Override public void onProgress(String msg) {
                            appendConsole("   " + msg, theme.textDim);
                        }
                        @Override public void onResult(String output) {
                            appendConsole(output, theme.consoleText);
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
    //  Settings + new features
    // ══════════════════════════════════════════════════════════

    private void openSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQ_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SETTINGS) {
            // Перезастосовуємо тему/шрифт без повного recreate, щоб не втрачати стан вкладок.
            theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
            applyTheme();
            EditorSettingsApplier.apply(editor, appPrefs, theme);
            switchBottomPanel(bottomPanelMode);
            Drawable nav = toolbar != null ? toolbar.getNavigationIcon() : null;
            if (nav != null) nav.setColorFilter(theme.text, PorterDuff.Mode.SRC_IN);
            return;
        }
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        if (requestCode == REQ_OPEN_FILE)        importExternalJavaFile(uri);
        else if (requestCode == REQ_SAVE_AS)     writeCurrentEditorToUri(uri);
        else if (requestCode == REQ_EXPORT_PROJ) exportProjectToUri(uri);
    }

    // ── External file: open ────────────────────────────────────

    private void pickFileToOpen() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_OPEN_FILE);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importExternalJavaFile(Uri uri) {
        String name = displayName(uri);
        if (name == null) name = "Imported.java";
        if (!name.toLowerCase().endsWith(".java")) {
            // Дозволяємо інші текстові файли — імпортуємо як Imported.txt у проєкт
            // АЛЕ якщо це не текст, кажемо
            // Простіше: створюємо .java якщо розширення не txt
        }
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("Cannot open stream");
            byte[] data = readAll(in);
            String text = new String(data, StandardCharsets.UTF_8);
            String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
            if (!safe.endsWith(".java") && !safe.contains(".")) safe += ".java";
            File target;
            if (safe.endsWith(".java")) {
                String className = safe.substring(0, safe.length() - 5);
                File made = projectManager.createFile(className, text);
                if (made == null) {
                    File dir = projectManager.isMavenProject()
                            ? ProjectLayoutHelper.mainJavaPackageDir(projectManager.getProjectDir())
                            : projectManager.getProjectDir();
                    target = new File(dir, safe);
                    projectManager.writeFile(target, text);
                } else {
                    target = made;
                }
            } else {
                File dir = projectManager.getProjectDir();
                target = new File(dir, safe);
                projectManager.writeFile(target, text);
            }
            refreshFileTree();
            openFile(target);
            Toast.makeText(this, getString(R.string.toast_imported_to, target.getName()),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ── Save as ────────────────────────────────────────────────

    private void saveCurrentAs() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/x-java");
        i.putExtra(Intent.EXTRA_TITLE, tab.file.getName());
        try {
            startActivityForResult(i, REQ_SAVE_AS);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void writeCurrentEditorToUri(Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new IOException("Cannot open output");
            os.write(editor.getText().toString().getBytes(StandardCharsets.UTF_8));
            String name = displayName(uri);
            Toast.makeText(this, getString(R.string.toast_export_done,
                    name != null ? name : uri.toString()), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_export_failed, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── Export project as zip ──────────────────────────────────

    private void exportProjectAsZip() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE, projectManager.getProjectDir().getName() + ".zip");
        try {
            startActivityForResult(i, REQ_EXPORT_PROJ);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportProjectToUri(Uri uri) {
        new Thread(() -> {
            try (OutputStream os = getContentResolver().openOutputStream(uri);
                 ZipOutputStream zos = new ZipOutputStream(os)) {
                if (os == null) throw new IOException("Cannot open output");
                File root = projectManager.getProjectDir();
                zipDir(root, root, zos);
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.toast_export_done, root.getName() + ".zip"),
                        Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.toast_export_failed, e.getMessage()),
                        Toast.LENGTH_SHORT).show());
            }
        }, "export-zip").start();
    }

    private static void zipDir(File rootDir, File current, ZipOutputStream zos) throws IOException {
        File[] children = current.listFiles();
        if (children == null) return;
        for (File f : children) {
            // Не пакуємо target/ та .gradle/.idea — економимо місце
            String n = f.getName();
            if (f.isDirectory() && (n.equals("target") || n.equals(".gradle")
                    || n.equals(".idea") || n.equals("build"))) continue;
            if (f.isDirectory()) {
                zipDir(rootDir, f, zos);
            } else {
                String rel = rootDir.toPath().relativize(f.toPath()).toString().replace('\\', '/');
                zos.putNextEntry(new ZipEntry(rel));
                try (FileInputStream in = new FileInputStream(f)) {
                    byte[] buf = new byte[8192];
                    int n2;
                    while ((n2 = in.read(buf)) != -1) zos.write(buf, 0, n2);
                }
                zos.closeEntry();
            }
        }
    }

    // ── SAF helpers ────────────────────────────────────────────

    private String displayName(Uri uri) {
        try (android.database.Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] buf = new byte[8192];
        int n;
        while ((n = bis.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
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
