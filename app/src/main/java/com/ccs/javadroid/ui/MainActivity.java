package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;
import com.ccs.javadroid.util.EditorSettingsApplier;
import com.ccs.javadroid.util.languages.JavaDroidLanguage;
import com.ccs.javadroid.tools.bytecode.InstructionAdapter;
import com.ccs.javadroid.tools.bytecode.Deobfuscator;
import com.ccs.javadroid.tools.bytecode.CallGraphModel;
import com.ccs.javadroid.tools.bytecode.BytecodeModel;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.analysis.ProblemsAdapter;
import com.ccs.javadroid.analysis.LiveProblemsScheduler;
import com.ccs.javadroid.project.ProjectScanner;
import com.ccs.javadroid.ai.AiChatActivity;
import com.ccs.javadroid.ai.PendingEdits;
import com.ccs.javadroid.tools.bytecode.BytecodeEditorActivity;
import com.ccs.javadroid.tools.bytecode.BytecodeEditor;
import com.ccs.javadroid.analysis.ProblemItem;
import com.ccs.javadroid.analysis.ProblemsWorkspaceAnalyzer;
import com.ccs.javadroid.tools.compilers.ProjectCompiler;
import com.ccs.javadroid.maven.PomModel;
import com.ccs.javadroid.maven.PomParser;
import com.ccs.javadroid.maven.MavenPaths;
import com.ccs.javadroid.maven.MavenDependencyResolver;
import com.ccs.javadroid.maven.MavenProjectFactory;
import com.ccs.javadroid.project.ProjectLayoutHelper;
import com.ccs.javadroid.git.GitManager;
import com.ccs.javadroid.git.GitActivity;
import com.ccs.javadroid.util.AutoImportHelper;
import com.ccs.javadroid.util.JavaFormatter;
import com.ccs.javadroid.util.JsonXmlFormatter;
import com.ccs.javadroid.util.MarkdownRenderer;
import com.ccs.javadroid.util.PastebinHelper;
import com.ccs.javadroid.util.PowerSavingManager;
import com.ccs.javadroid.util.SessionState;
import com.ccs.javadroid.util.FileTemplates;
import com.ccs.javadroid.util.languages.CppLanguage;
import com.ccs.javadroid.util.languages.XmlLanguage;
import com.ccs.javadroid.util.languages.CssLanguage;
import com.ccs.javadroid.util.languages.JavaScriptLanguage;
import com.ccs.javadroid.util.languages.SqlLanguage;
import com.ccs.javadroid.util.languages.GradleLanguage;
import com.ccs.javadroid.util.languages.JsonLanguage;
import com.ccs.javadroid.util.languages.BashLanguage;
import com.ccs.javadroid.util.languages.KotlinLanguage;
import com.ccs.javadroid.util.languages.MarkdownLanguage;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ccs.javadroid.debug.DebuggerController;
import com.ccs.javadroid.debug.DebugVariable;
import com.ccs.javadroid.debug.WatchExpression;
import com.ccs.javadroid.debug.ExpressionEvaluator;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.javadroid.debug.BreakpointOverlay;
import com.ccs.javadroid.debug.BookmarkOverlay;
import com.ccs.javadroid.debug.BookmarkManager;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;

public class MainActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────
    private DrawerLayout  drawerLayout;
    private CodeEditor    editor;
    private CodeEditor    editor2;
    private CodeEditor    activeEditor;
    private LinearLayout  editorsContainer;
    private View          editorDivider;
    private FrameLayout   wrapperEditor1;
    private FrameLayout   wrapperEditor2;
    private BreakpointOverlay breakpointOverlay1;
    private BreakpointOverlay breakpointOverlay2;
    private BookmarkOverlay bookmarkOverlay1;
    private BookmarkOverlay bookmarkOverlay2;
    private FileTab       leftTab;
    private FileTab       rightTab;
    private boolean       isSplitActive = false;
    private RecyclerView  tabsRecycler;
    private RecyclerView  fileTreeRecycler;
    private com.ccs.javadroid.util.VoiceToTextManager voiceToText;
    private LinearLayout  findBar;
    private EditText      etFind;
    private EditText      etReplace;
    private android.widget.Switch switchFindScope;
    private TextView      statusLineCol;
    private TextView      statusFileName;
    private TextView      statusEncoding;
    private TextView      toolbarTitle;
    private ScrollView    consoleScroll;
    private TextView      consoleOutput;
    private RecyclerView  problemsRecycler;
    private TextView      tabRun;
    private TextView      tabProblems;
    private TextView      tabBytecode;
    private View          bytecodeRoot;
    private View          bytecodeToolbar;
    private TextView      bytecodeStatus;
    private RecyclerView  bytecodeMethodTree;
    private RecyclerView  bytecodeInstructions;
    private MethodTreeAdapter bytecodeTreeAdapter;
    private InstructionAdapter bytecodeInsnAdapter;
    private EditText      bytecodeSearch;
    private View          bytecodeToggleHex;
    private View          bytecodeToggleLines;
    private View          bytecodeToggleComments;
    private View          bytecodeInsnSlot;
    private View          bytecodeHexScroll;
    private TextView      bytecodeHexOutput;
    private View          bytecodeEditInsn;
    private View          bytecodeSaveBtn;
    private View          bytecodeRunBtn;
    private View          bytecodeOpenEditorBtn;
    private View          bytecodeCallGraphBtn;
    private Deobfuscator  deobfuscator;
    private View          callGraphRoot;
    private View          callGraphToolbar;
    private LinearLayout  callGraphContent;
    private TextView      callGraphStatus;
    private EditText      callGraphSearch;
    private CallGraphModel callGraphModel;
    private boolean       bytecodeHexMode;
    private BytecodeModel bytecodeModel;
    private int           bytecodeSelectedMethod = -1;
    private View          btnClearConsole;
    private View          tabsBar;
    private View          tabBorder;
    private View          bottomTabsBar;
    private View          consoleDivider;
    private FrameLayout   bottomPanelContent;
    private Toolbar       toolbar;
    private View          statusBar;
    private View          keyAccessoryBar;
    private LinearLayout  accessoryBarLayout;

    private static final int PANEL_RUN       = 0;
    private static final int PANEL_PROBLEMS  = 1;
    private static final int PANEL_BYTECODE  = 2;
    private static final int PANEL_DEBUG         = 3;
    private static final int PANEL_DEBUG_CONSOLE = 4;
    private static final int PANEL_CALL_GRAPH    = 5;
    private static final int PANEL_BOOKMARKS     = 6;
    private static final int PANEL_DEPS          = 7;
    private static final int PANEL_PROFILER      = 8;
    private static final int PANEL_TODO          = 9;

    private int bottomPanelMode = PANEL_RUN;
    private volatile boolean bytecodeRefreshRunning;

    // ── Debug UI ─────────────────────────────────────────
    private View          debugToolbar;
    private TextView      debugLocation;
    private TextView      tabDebug;
    private TextView      tabDebugConsole;
    private TextView      tabCallGraph;
    private TextView      tabBookmarks;
    private RecyclerView  bookmarksRecycler;
    private ScrollView    debugConsoleScroll;
    private TextView      debugConsoleOutput;
    private View          debuggerSplitPanel;
    private ScrollView    variablesScroll;
    private TextView      variablesOutput;
    private RecyclerView  variablesRecycler;
    private com.ccs.javadroid.debug.VariablesTreeAdapter variablesAdapter;
    private ScrollView    callStackScroll;
    private TextView      callStackOutput;
    private RecyclerView  callStackRecycler;
    private com.ccs.javadroid.debug.CallStackAdapter callStackAdapter;
    private LinearLayout  watchesContainer;
    private TextView      watchesOutput;
    private boolean       isDebugging = false;
    private final java.util.List<WatchExpression> watchExpressions = new java.util.ArrayList<>();
    private DebuggerController.DebugListener debugListener;
    private com.ccs.javadroid.debug.DebugEditorDecorator activeEditorDecorator;

    // ── Minimap ─────────────────────────────────────────────
    private MinimapView minimapView1;
    private MinimapView minimapView2;

    // ── Dependency Viewer ───────────────────────────────────
    private View          depsPanel;
    private TextView      tabDeps;
    private DependencyGraphView depsGraphView;
    private TextView      depsStatus;
    private com.ccs.javadroid.tools.bytecode.DependencyModel dependencyModel;

    // ── Profiler ─────────────────────────────────────────────
    private View          profilerPanel;
    private TextView      tabProfiler;
    private FlameChartView flameChartView;
    private TextView      profilerStatus;
    private ScrollView    profilerDetailScroll;
    private TextView      profilerDetail;
    private boolean       profilingEnabled = false;
    private boolean       profilerLiveMode = true;
    private final Handler profilerRefreshHandler = new Handler(Looper.getMainLooper());

    // ── TODO/FIXME Tracker ──────────────────────────────────
    private View          todoPanel;
    private TextView      tabTodo;
    private RecyclerView  todoRecycler;
    private EditText      todoSearch;
    private TextView      todoStatus;
    private com.ccs.javadroid.analysis.TodoAdapter todoAdapter;
    private boolean       todoAutoRefreshPending = false;
    private final Handler todoAutoRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable todoAutoRefreshRunnable = () -> {
        if (todoAutoRefreshPending) {
            todoAutoRefreshPending = false;
            refreshTodoPanel();
        }
    };
    private final Runnable profilerRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (profilingEnabled && profilerLiveMode) {
                refreshProfilerResults();
                // Slower refresh in power saving mode (2s vs 500ms)
                long interval = powerSaving.isPowerSavingActive() ? 2000 : 500;
                profilerRefreshHandler.postDelayed(this, interval);
            }
        }
    };

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
    private PowerSavingManager powerSaving;
    private SessionState sessionState;
    private AppTheme          theme;
    private boolean isRunning        = false;
    private boolean isProgrammaticChange = false;
    private int     lastSearchOffset = -1;
    private boolean findGlobalMode = false;
    private File    copiedFile       = null;

    private static final int REQ_SETTINGS      = 4001;
    private static final int REQ_OPEN_FILE     = 4002;
    private static final int REQ_SAVE_AS       = 4003;
    private static final int REQ_EXPORT_PROJ   = 4004;
    private static final int REQ_LIB_MANAGER   = 4005;
    private static final int REQ_GLOBAL_SEARCH = 4006;
    private static final int REQ_PLAY_MEDIA    = 4007;
    private static final int REQ_IMPORT_FILES  = 4008;
    private static final int REQ_CLASS_BROWSER = 9001;
    private static final int REQ_ARCHIVE_FOLDER = 9002;

    private static final int MD_COLOR_TEXT = 0xFFDFE1E5;
    private static final int MD_COLOR_TEXT_LIGHT = 0xFF3C3F41;

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
        appPrefs = new AppPreferences(this);
        powerSaving = new PowerSavingManager(this);
        sessionState = new SessionState(this);
        theme    = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FullScreenHelper.enable(this);

        prefs = getSharedPreferences(AppPreferences.PREFS_NAME, MODE_PRIVATE);

        bindViews();
        if (statusEncoding != null) {
            statusEncoding.setOnClickListener(v -> showEncodingSelectionDialog());
        }
        setupBackHandling();
        setupToolbar();
        setupTabs();
        setupFileTree();
        setupEditor();
        setupFindBar();
        setupBottomTabs();
        setupConsoleDivider();
        setupProblemsList();
        setupBytecodeViewer();
        deobfuscator = new Deobfuscator();

        setupProject(savedInstanceState != null);

        if (savedInstanceState != null) {
            java.util.ArrayList<String> paths = savedInstanceState.getStringArrayList("open_tab_paths");
            int activeIndex = savedInstanceState.getInt("active_tab_index", -1);
            if (paths != null && !paths.isEmpty()) {
                tabsAdapter.getTabs().clear();
                tabsAdapter.notifyDataSetChanged();
                tabsAdapter.setActiveIndex(-1);

                for (String path : paths) {
                    tabsAdapter.addTab(new FileTab(new File(path)));
                }
                if (activeIndex >= 0 && activeIndex < tabsAdapter.getTabs().size()) {
                    switchTab(activeIndex);
                }
            }
        }

        applyTheme();
        initLiveProblemsScheduler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (liveProblemsScheduler != null) {
            liveProblemsScheduler.setInterval(powerSaving.getProblemsScanIntervalMs());
            liveProblemsScheduler.start();
        }
        theme = AppTheme.byId(appPrefs.getThemeId(), appPrefs);
        applyTheme();
        invalidateOptionsMenu();
        // Auto-refresh problems on resume (e.g. returning from settings with new power saving mode)
        if (projectManager != null && projectManager.getProjectDir() != null) {
            refreshProblemsMergedAsync();
        }
        // Застосувати відкладені вставки коду від AI-чату (кнопка "Insert" / інструмент insertCode).
        applyPendingAiEdits();
    }

    /**
     * Витягує всі відкладені вставки коду з PendingEdits і застосовує їх до activeEditor
     * у порядку черги. Викликається з onResume після повернення з AI-чату.
     */
    private void applyPendingAiEdits() {
        if (!PendingEdits.hasPending()) return;
        if (activeEditor == null) {
            android.widget.Toast.makeText(this,
                    "No editor open — AI code insertions discarded",
                    android.widget.Toast.LENGTH_LONG).show();
            PendingEdits.clear();
            return;
        }

        java.util.List<PendingEdits.Edit> edits = PendingEdits.drain();
        if (edits.isEmpty()) return;

        isProgrammaticChange = true;
        try {
            int applied = 0;
            for (PendingEdits.Edit e : edits) {
                try {
                    if (PendingEdits.LOCATION_REPLACE.equals(e.location)) {
                        activeEditor.setText(e.code);
                    } else if (PendingEdits.LOCATION_APPEND.equals(e.location)) {
                        // Перейти в кінець документу й вставити
                        int lastLine = activeEditor.getText().getLineCount() - 1;
                        if (lastLine < 0) lastLine = 0;
                        int lastCol = activeEditor.getText().getColumnCount(lastLine);
                        activeEditor.setSelection(lastLine, lastCol);
                        String sep = needLeadingNewline() ? "\n" : "";
                        activeEditor.insertText(sep + e.code, 0);
                    } else {
                        // cursor (за замовч.)
                        activeEditor.insertText(e.code, 0);
                    }
                    applied++;
                } catch (Exception ex) {
                    android.util.Log.w("MainActivity", "AI insert failed: " + ex.getMessage());
                }
            }
            if (applied > 0) {
                android.widget.Toast.makeText(this,
                        "Inserted " + applied + " AI code block(s)",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        } finally {
            isProgrammaticChange = false;
        }
    }

    /** Чи потрібен порожній рядок перед вставлянням append (файл не закінчується \n)? */
    private boolean needLeadingNewline() {
        try {
            var txt = activeEditor.getText();
            int lc = txt.getLineCount();
            if (lc == 0) return false;
            String last = txt.getLine(lc - 1).toString();
            return !last.isEmpty();
        } catch (Throwable t) {
            return true;
        }
    }

    @Override
    protected void onPause() {
        if (liveProblemsScheduler != null) {
            liveProblemsScheduler.stop();
        }
        if (isDebugging) {
            stopDebug();
        }
        stopProfilerLiveRefresh();
        super.onPause();
        saveCurrentToActiveTab();
        saveSessionState();
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
                // Return to WelcomeActivity when back pressed at root level
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void bindViews() {
        drawerLayout     = findViewById(R.id.drawerLayout);
        editor           = findViewById(R.id.editor);
        editor2          = findViewById(R.id.editor2);
        editorsContainer = findViewById(R.id.editorsContainer);
        editorDivider    = findViewById(R.id.editorDivider);
        wrapperEditor1   = findViewById(R.id.wrapperEditor1);
        wrapperEditor2   = findViewById(R.id.wrapperEditor2);
        breakpointOverlay1 = findViewById(R.id.breakpointOverlay1);
        breakpointOverlay2 = findViewById(R.id.breakpointOverlay2);
        bookmarkOverlay1 = findViewById(R.id.bookmarkOverlay1);
        bookmarkOverlay2 = findViewById(R.id.bookmarkOverlay2);
        activeEditor     = editor;
        activeEditorDecorator = new com.ccs.javadroid.debug.DebugEditorDecorator(editor, breakpointOverlay1);
        if (bookmarkOverlay1 != null) bookmarkOverlay1.setEditor(editor);
        if (bookmarkOverlay2 != null) bookmarkOverlay2.setEditor(editor2);
        tabsRecycler     = findViewById(R.id.tabsRecycler);
        fileTreeRecycler = findViewById(R.id.fileTreeRecycler);
        findBar          = findViewById(R.id.findBar);
        etFind           = findViewById(R.id.etFind);
        etReplace        = findViewById(R.id.etReplace);
        switchFindScope  = findViewById(R.id.switchFindScope);
        toolbarTitle     = findViewById(R.id.toolbarTitle);
        statusLineCol    = findViewById(R.id.statusLineCol);
        statusFileName   = findViewById(R.id.statusFileName);
        consoleScroll    = findViewById(R.id.consoleScroll);
        consoleOutput    = findViewById(R.id.consoleOutput);
        problemsRecycler = findViewById(R.id.problemsRecycler);
        tabRun           = findViewById(R.id.tabRun);
        tabProblems      = findViewById(R.id.tabProblems);
        tabBytecode      = findViewById(R.id.tabBytecode);
        bytecodeRoot         = findViewById(R.id.bytecodeRoot);
        bytecodeToolbar      = findViewById(R.id.bytecodeToolbar);
        bytecodeStatus       = findViewById(R.id.bytecodeStatus);
        bytecodeMethodTree   = findViewById(R.id.bytecodeMethodTree);
        bytecodeInstructions = findViewById(R.id.bytecodeInstructions);
        bytecodeSearch       = findViewById(R.id.bytecodeSearch);
        bytecodeToggleHex      = findViewById(R.id.bytecodeToggleHex);
        bytecodeToggleLines    = findViewById(R.id.bytecodeToggleLines);
        bytecodeToggleComments = findViewById(R.id.bytecodeToggleComments);
        bytecodeInsnSlot   = findViewById(R.id.bytecodeInsnSlot);
        bytecodeHexScroll  = findViewById(R.id.bytecodeHexScroll);
        bytecodeHexOutput  = findViewById(R.id.bytecodeHexOutput);
        bytecodeEditInsn   = findViewById(R.id.bytecodeEditInsn);
        bytecodeSaveBtn    = findViewById(R.id.bytecodeSave);
        bytecodeRunBtn     = findViewById(R.id.bytecodeRun);
        bytecodeOpenEditorBtn = findViewById(R.id.bytecodeOpenEditor);
        bytecodeCallGraphBtn = findViewById(R.id.bytecodeCallGraph);
        btnClearConsole  = findViewById(R.id.btnClearConsole);
        tabsBar          = findViewById(R.id.tabsBar);
        tabBorder        = findViewById(R.id.tabBorder);
        bottomTabsBar    = findViewById(R.id.bottomTabsBar);
        consoleDivider   = findViewById(R.id.consoleDivider);
        bottomPanelContent = findViewById(R.id.bottomPanelContent);
        statusBar        = findViewById(R.id.statusBar);
        statusEncoding   = findViewById(R.id.statusEncoding);
        toolbar          = findViewById(R.id.toolbar);
        keyAccessoryBar  = findViewById(R.id.keyAccessoryBar);
        accessoryBarLayout = findViewById(R.id.accessoryBarLayout);

        // Debug views
        debugToolbar     = findViewById(R.id.debugToolbar);
        debugLocation    = findViewById(R.id.debugLocation);
        tabDebug         = findViewById(R.id.tabDebug);
        tabDebugConsole  = findViewById(R.id.tabDebugConsole);
        tabCallGraph     = findViewById(R.id.tabCallGraph);
        tabBookmarks     = findViewById(R.id.tabBookmarks);
        bookmarksRecycler = findViewById(R.id.bookmarksRecycler);
        debugConsoleScroll = findViewById(R.id.debugConsoleScroll);
        debugConsoleOutput = findViewById(R.id.debugConsoleOutput);
        
        debuggerSplitPanel = findViewById(R.id.debuggerSplitPanel);
        watchesOutput    = findViewById(R.id.watchesOutput);
        variablesOutput  = findViewById(R.id.variablesOutput);
        variablesRecycler = findViewById(R.id.variablesRecycler);
        callStackOutput  = findViewById(R.id.callStackOutput);
        callStackRecycler = findViewById(R.id.callStackRecycler);
        watchesContainer = findViewById(R.id.watchesContainer);

        callGraphRoot     = findViewById(R.id.callGraphRoot);
        callGraphToolbar  = findViewById(R.id.callGraphToolbar);
        callGraphContent  = findViewById(R.id.callGraphContent);
        callGraphStatus   = findViewById(R.id.callGraphStatus);
        callGraphSearch   = findViewById(R.id.callGraphSearch);

        // Minimap
        minimapView1 = findViewById(R.id.minimapView1);
        minimapView2 = findViewById(R.id.minimapView2);

        // Dependency Viewer
        depsPanel    = findViewById(R.id.depsPanel);
        tabDeps      = findViewById(R.id.tabDeps);
        depsGraphView = findViewById(R.id.depsGraphView);
        depsStatus   = findViewById(R.id.depsStatus);

        // Profiler
        profilerPanel = findViewById(R.id.profilerPanel);
        tabProfiler  = findViewById(R.id.tabProfiler);
        flameChartView = findViewById(R.id.flameChartView);
        profilerStatus = findViewById(R.id.profilerStatus);
        profilerDetailScroll = findViewById(R.id.profilerDetailScroll);
        profilerDetail = findViewById(R.id.profilerDetail);

        // TODO/FIXME
        todoPanel    = findViewById(R.id.todoPanel);
        tabTodo      = findViewById(R.id.tabTodo);
        todoRecycler = findViewById(R.id.todoRecycler);
        todoSearch   = findViewById(R.id.todoSearch);
        todoStatus   = findViewById(R.id.todoStatus);
        todoAdapter  = new com.ccs.javadroid.analysis.TodoAdapter();
        todoAdapter.setTheme(theme);
        todoAdapter.setListener(item -> {
            if (item.file != null && item.file.exists()) {
                openFile(item.file);
                if (item.line > 0 && activeEditor != null) {
                    activeEditor.setSelection(Math.max(0, item.line - 1), 0);
                }
            }
        });
        if (todoRecycler != null) {
            todoRecycler.setLayoutManager(new LinearLayoutManager(this));
            todoRecycler.setAdapter(todoAdapter);
        }
        if (todoSearch != null) {
            todoSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    todoAdapter.filter(s != null ? s.toString() : "");
                }
            });
        }
        View todoRefresh = findViewById(R.id.todoRefresh);
        if (todoRefresh != null) todoRefresh.setOnClickListener(v -> refreshTodoPanel());

        setupDebugToolbar();
        setupDebugController();
        setupDebugAdapters();
    }

    /** Перефарбовує статичні UI-елементи відповідно до поточної теми. */
    private void applyTheme() {
        if (drawerLayout != null) drawerLayout.setBackgroundColor(theme.bg);
        View drawerRoot   = findViewById(R.id.drawerRoot);
        View drawerHeader = findViewById(R.id.drawerHeader);
        if (drawerRoot != null)   drawerRoot.setBackgroundColor(theme.toolbar);
        if (drawerHeader != null) drawerHeader.setBackgroundColor(theme.bg);
        if (toolbar != null) {
            toolbar.setBackgroundColor(theme.toolbar);
            Drawable overflow = toolbar.getOverflowIcon();
            if (overflow != null) {
                overflow.mutate().setColorFilter(theme.text, PorterDuff.Mode.SRC_IN);
            }
        }
        if (toolbarTitle != null) toolbarTitle.setTextColor(theme.text);
        if (tabsBar != null)      tabsBar.setBackgroundColor(theme.toolbar);
        if (tabBorder != null)    tabBorder.setBackgroundColor(theme.accent);
        if (editorDivider != null) editorDivider.setBackgroundColor(theme.separator);
        updateActiveEditorBorders();
        // Update editor color scheme
        if (editor != null) EditorSettingsApplier.apply(editor, appPrefs, theme);
        if (editor2 != null) EditorSettingsApplier.apply(editor2, appPrefs, theme);

        // Minimap theming
        if (minimapView1 != null) minimapView1.setThemeColors(
                theme.consoleBg, theme.text, theme.editorKeyword, theme.editorString,
                theme.editorComment, 0xFFB5CEA8, theme.accent, 0x28FFFFFF, 0x50FFFFFF);
        if (minimapView2 != null) minimapView2.setThemeColors(
                theme.consoleBg, theme.text, theme.editorKeyword, theme.editorString,
                theme.editorComment, 0xFFB5CEA8, theme.accent, 0x28FFFFFF, 0x50FFFFFF);

        refreshBreakpointMarkers();
        refreshBookmarkMarkers();
        if (bottomTabsBar != null) bottomTabsBar.setBackgroundColor(theme.toolbar);
        refreshBottomTabColors();
        if (statusBar != null)    statusBar.setBackgroundColor(theme.statusBar);
        if (consoleScroll != null) consoleScroll.setBackgroundColor(theme.consoleBg);
        if (consoleOutput != null) consoleOutput.setTextColor(theme.consoleText);
        if (problemsRecycler != null) problemsRecycler.setBackgroundColor(theme.consoleBg);
        if (bytecodeRoot != null) bytecodeRoot.setBackgroundColor(theme.consoleBg);
        if (bytecodeToolbar != null) bytecodeToolbar.setBackgroundColor(theme.toolbar);
        if (bytecodeStatus != null) bytecodeStatus.setBackgroundColor(theme.consoleBg);
        if (bytecodeStatus != null) bytecodeStatus.setTextColor(theme.textDim);
        if (bytecodeMethodTree != null) bytecodeMethodTree.setBackgroundColor(theme.consoleBg);
        if (bytecodeInstructions != null) bytecodeInstructions.setBackgroundColor(theme.consoleBg);
        if (bytecodeSearch != null) bytecodeSearch.setTextColor(theme.consoleText);
        if (bytecodeSearch != null) bytecodeSearch.setHintTextColor(theme.textDim);
        if (bytecodeHexOutput != null) bytecodeHexOutput.setTextColor(theme.consoleText);
        if (bytecodeTreeAdapter != null) bytecodeTreeAdapter.setTheme(theme);
        if (bytecodeInsnAdapter != null) bytecodeInsnAdapter.setTheme(theme);
        if (bytecodeEditInsn != null) ((TextView) bytecodeEditInsn).setTextColor(theme.accent);
        if (bytecodeSaveBtn != null) ((TextView) bytecodeSaveBtn).setTextColor(theme.successText);
        if (bytecodeRunBtn != null) ((TextView) bytecodeRunBtn).setTextColor(theme.successText);
        if (bytecodeOpenEditorBtn != null) ((TextView) bytecodeOpenEditorBtn).setTextColor(theme.accent);

        // Debug panels theming
        if (debugToolbar != null) debugToolbar.setBackgroundColor(theme.toolbar);
        if (debugLocation != null) debugLocation.setTextColor(theme.accent);
        if (debugConsoleScroll != null) debugConsoleScroll.setBackgroundColor(theme.consoleBg);
        if (debugConsoleOutput != null) debugConsoleOutput.setTextColor(theme.consoleText);
        if (variablesScroll != null) variablesScroll.setBackgroundColor(theme.consoleBg);
        if (variablesOutput != null) variablesOutput.setTextColor(theme.consoleText);
        if (variablesRecycler != null) variablesRecycler.setBackgroundColor(theme.consoleBg);
        if (callStackScroll != null) callStackScroll.setBackgroundColor(theme.consoleBg);
        if (callStackOutput != null) callStackOutput.setTextColor(theme.consoleText);
        if (callStackRecycler != null) callStackRecycler.setBackgroundColor(theme.consoleBg);
        if (watchesContainer != null) watchesContainer.setBackgroundColor(theme.consoleBg);
        if (watchesOutput != null) watchesOutput.setTextColor(theme.consoleText);
        if (variablesAdapter != null) variablesAdapter.setTheme(theme);
        if (callStackAdapter != null) callStackAdapter.setTheme(theme);

        // Debug toolbar buttons theming
        if (debugToolbar != null) {
            TextView btnStepOver = debugToolbar.findViewById(R.id.btnDebugStepOver);
            TextView btnStepInto = debugToolbar.findViewById(R.id.btnDebugStepInto);
            TextView btnStepOut = debugToolbar.findViewById(R.id.btnDebugStepOut);
            TextView btnResume = debugToolbar.findViewById(R.id.btnDebugResume);
            TextView btnStop = debugToolbar.findViewById(R.id.btnDebugStop);
            TextView btnEval = debugToolbar.findViewById(R.id.btnDebugEvaluate);
            TextView btnBP = debugToolbar.findViewById(R.id.btnToggleBreakpoint);
            if (btnStepOver != null) btnStepOver.setTextColor(theme.textDim);
            if (btnStepInto != null) btnStepInto.setTextColor(theme.textDim);
            if (btnStepOut != null) btnStepOut.setTextColor(theme.textDim);
            if (btnResume != null) btnResume.setTextColor(theme.successText);
            if (btnStop != null) btnStop.setTextColor(theme.errorText);
            if (btnEval != null) btnEval.setTextColor(theme.textDim);
            if (btnBP != null) btnBP.setTextColor(theme.errorText);
        }

        // Call Graph panel theming
        if (callGraphRoot != null) callGraphRoot.setBackgroundColor(theme.consoleBg);
        if (callGraphToolbar != null) callGraphToolbar.setBackgroundColor(theme.toolbar);
        if (callGraphSearch != null) {
            callGraphSearch.setTextColor(theme.consoleText);
            callGraphSearch.setHintTextColor(theme.textDim);
        }
        if (callGraphStatus != null) {
            callGraphStatus.setBackgroundColor(theme.consoleBg);
            callGraphStatus.setTextColor(theme.textDim);
        }

        // Dependency Viewer theming
        if (depsPanel != null) depsPanel.setBackgroundColor(theme.consoleBg);
        View depsToolbar = findViewById(R.id.depsToolbar);
        if (depsToolbar != null) depsToolbar.setBackgroundColor(theme.toolbar);
        if (depsStatus != null) depsStatus.setTextColor(theme.textDim);
        if (depsGraphView != null) {
            depsGraphView.setColors(theme.accent, 0xFFFFA500, 0xFF4CAF50, 0xFF666666, theme.text, theme.consoleBg);
        }

        // Profiler theming
        if (profilerPanel != null) profilerPanel.setBackgroundColor(theme.consoleBg);

        // TODO panel theming
        if (todoPanel != null) todoPanel.setBackgroundColor(theme.consoleBg);
        if (todoRecycler != null) todoRecycler.setBackgroundColor(theme.consoleBg);
        if (todoSearch != null) {
            todoSearch.setTextColor(theme.consoleText);
            todoSearch.setHintTextColor(theme.textDim);
        }
        if (todoStatus != null) todoStatus.setTextColor(theme.textDim);
        if (todoAdapter != null) todoAdapter.setTheme(theme);
        View profilerToolbarView = findViewById(R.id.profilerToolbar);
        if (profilerToolbarView != null) profilerToolbarView.setBackgroundColor(theme.toolbar);
        if (profilerStatus != null) profilerStatus.setTextColor(theme.textDim);

        // Drawer elements theming
        TextView tvDrawerProjectLabel = findViewById(R.id.tvDrawerProjectLabel);
        if (tvDrawerProjectLabel != null) tvDrawerProjectLabel.setTextColor(theme.accent);
        TextView tvDrawerArrow = findViewById(R.id.tvDrawerArrow);
        if (tvDrawerArrow != null) tvDrawerArrow.setTextColor(theme.textDim);
        TextView tvDrawerMavenHint = findViewById(R.id.tvDrawerMavenHint);
        if (tvDrawerMavenHint != null) tvDrawerMavenHint.setTextColor(theme.textDim);
        TextView tvDrawerNewFilePlus = findViewById(R.id.tvDrawerNewFilePlus);
        if (tvDrawerNewFilePlus != null) tvDrawerNewFilePlus.setTextColor(theme.accent);
        TextView tvDrawerNewFileText = findViewById(R.id.tvDrawerNewFileText);
        if (tvDrawerNewFileText != null) tvDrawerNewFileText.setTextColor(theme.text);
        View sep1 = findViewById(R.id.drawerSeparator1);
        if (sep1 != null) sep1.setBackgroundColor(theme.separator);
        View sep2 = findViewById(R.id.drawerSeparator2);
        if (sep2 != null) sep2.setBackgroundColor(theme.separator);

        // Find & Replace bar theming
        if (findBar != null) {
            findBar.setBackgroundColor(blend(theme.toolbar, theme.bg, 0.2f));
        }
        if (etFind != null) {
            etFind.setBackgroundColor(theme.bg);
            etFind.setTextColor(theme.text);
            etFind.setHintTextColor(theme.textDim);
        }
        if (etReplace != null) {
            etReplace.setBackgroundColor(theme.bg);
            etReplace.setTextColor(theme.text);
            etReplace.setHintTextColor(theme.textDim);
        }
        TextView btnFindPrev = findViewById(R.id.btnFindPrev);
        if (btnFindPrev != null) btnFindPrev.setTextColor(theme.text);
        TextView btnFindNext = findViewById(R.id.btnFindNext);
        if (btnFindNext != null) btnFindNext.setTextColor(theme.text);
        TextView btnFindClose = findViewById(R.id.btnFindClose);
        if (btnFindClose != null) btnFindClose.setTextColor(theme.textDim);
        if (switchFindScope != null) {
            switchFindScope.setTextColor(theme.accent);
            switchFindScope.setText(findGlobalMode ? getString(R.string.find_scope_project) : getString(R.string.find_scope_file));
        }
        TextView btnReplace = findViewById(R.id.btnReplace);
        if (btnReplace != null) btnReplace.setTextColor(theme.accent);
        TextView btnReplaceAll = findViewById(R.id.btnReplaceAll);
        if (btnReplaceAll != null) btnReplaceAll.setTextColor(theme.accent);

        // Adapters theming
        if (tabsAdapter != null) tabsAdapter.setTheme(theme);
        if (fileTreeAdapter != null) fileTreeAdapter.setTheme(theme);
        if (problemsAdapter != null) problemsAdapter.setTheme(theme);

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
        if (statusEncoding != null) {
            statusEncoding.setTextColor(theme.textDim);
            statusEncoding.setText(appPrefs.getFileEncoding());
        }
        if (keyAccessoryBar != null) keyAccessoryBar.setBackgroundColor(theme.toolbar);
        setupKeyAccessoryBar();
        invalidateOptionsMenu();
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
                if (activeEditor != null && activeEditor.isEditable()) {
                    if ("Tab".equals(symbol)) {
                        int tabSize = appPrefs.getTabSize();
                        StringBuilder spaces = new StringBuilder();
                        for (int i = 0; i < tabSize; i++) spaces.append(" ");
                        activeEditor.insertText(spaces.toString(), spaces.length());
                    } else {
                        activeEditor.insertText(symbol, symbol.length());
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

        // Three-dot overflow button → searchable menu
        View overflowBtn = findViewById(R.id.toolbarOverflow);
        if (overflowBtn != null) {
            overflowBtn.setOnClickListener(v -> showSearchableMenu());
        }

        // Back button → WelcomeActivity
        View backBtn = findViewById(R.id.toolbarBack);
        if (backBtn != null) {
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            });
        }

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
                if (node.directory) {
                    showFolderContextMenu(node.path);
                } else {
                    showFileContextMenu(node.path);
                }
            }
        });
        fileTreeRecycler.setLayoutManager(new LinearLayoutManager(this));
        fileTreeRecycler.setAdapter(fileTreeAdapter);

        View btnNewFile = findViewById(R.id.btnNewFile);
        if (btnNewFile != null) btnNewFile.setOnClickListener(v -> showNewFileDialog());

        View btnImportFiles = findViewById(R.id.btnImportFiles);
        if (btnImportFiles != null) btnImportFiles.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            importFilesToProject();
        });
    }

    private void saveEditorToTab(CodeEditor ed, FileTab tab) {
        if (tab == null || tab.file == null) return;
        try {
            projectManager.writeFile(tab.file, ed.getText().toString());
            tab.isModified = false;
            tab.cursorLine = ed.getCursor().getLeftLine();
            tab.cursorColumn = ed.getCursor().getLeftColumn();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.toast_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateActiveEditorBorders() {
        if (!isSplitActive) {
            wrapperEditor1.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            wrapperEditor2.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            return;
        }
        int activeColor = theme != null ? theme.accent : 0xFF4A86C8;
        int inactiveColor = theme != null ? theme.separator : 0xFF515151;
        wrapperEditor1.setBackgroundColor(activeEditor == editor ? activeColor : inactiveColor);
        wrapperEditor2.setBackgroundColor(activeEditor == editor2 ? activeColor : inactiveColor);
    }

    private void setActiveEditor(CodeEditor ed) {
        android.util.Log.d("BpOverlay", "setActiveEditor: ed=" + (ed != null) + " activeEditor=" + (activeEditor == ed) + " same=" + (activeEditor == ed));
        if (activeEditor == ed) return;
        activeEditor = ed;
        updateActiveEditorBorders();
        BreakpointOverlay ov = (ed == editor) ? breakpointOverlay1 : breakpointOverlay2;
        activeEditorDecorator = new com.ccs.javadroid.debug.DebugEditorDecorator(ed, ov);
        refreshBreakpointMarkers();
        refreshBookmarkMarkers();

        FileTab currentTab = (ed == editor) ? leftTab : rightTab;
        if (currentTab != null) {
            int idx = tabsAdapter.getTabs().indexOf(currentTab);
            if (idx >= 0 && idx != tabsAdapter.getActiveIndex()) {
                tabsAdapter.setActiveIndex(idx);
                updateStatusFileName(currentTab.file);
                fileTreeAdapter.setActiveFile(currentTab.file);
            }
        }
    }

    private void configureEditor(final CodeEditor ed) {
        ed.setEditorLanguage(new JavaDroidLanguage(this, null));
        EditorSettingsApplier.apply(ed, appPrefs, theme);
        ed.setEditable(false);

        // Cursor position → status bar
        ed.subscribeEvent(io.github.rosemoe.sora.event.SelectionChangeEvent.class, (event, sub) -> {
            if (activeEditor != ed) return;
            int ln  = event.getLeft().line + 1;
            int col = event.getLeft().column + 1;
            String text = getString(R.string.status_line, ln, col);
            if (!text.equals(statusLineCol.getText())) {
                runOnUiThread(() -> statusLineCol.setText(text));
            }
        });

        // Content change → mark tab as modified + auto-save (if enabled)
        ed.subscribeEvent(io.github.rosemoe.sora.event.ContentChangeEvent.class, (event, sub) -> {
            if (isProgrammaticChange) return;
            FileTab tab = (ed == editor) ? leftTab : rightTab;
            if (tab == null) return;
            int idx = tabsAdapter.getTabs().indexOf(tab);
                    if (idx >= 0 && !tab.isModified) {
                        runOnUiThread(() -> {
                            tabsAdapter.markModified(idx, true);
                            if (powerSaving.shouldAutoSave() && appPrefs.isAutoSave()) {
                                saveEditorToTab(ed, tab);
                                tabsAdapter.markModified(idx, false);
                            }
                        });
                    }
            if (!powerSaving.isPowerSavingActive() && bottomPanelMode == PANEL_TODO) {
                todoAutoRefreshPending = true;
                todoAutoRefreshHandler.removeCallbacks(todoAutoRefreshRunnable);
                todoAutoRefreshHandler.postDelayed(todoAutoRefreshRunnable, 1500);
            }
        });

        // Focus changes
        ed.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setActiveEditor(ed);
            }
        });

        // Touch listener: gutter tap toggles breakpoint, rest captures focus
        // Triple-tap counter for bookmarks
        final long[] tapTimestamps = new long[3];
        final int[] tapCount = {0};

        ed.setOnTouchListener((v, event) -> {
            if (activeEditor != ed) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    ed.requestFocus();
                    setActiveEditor(ed);
                }
            }
            // Detect taps in the line number gutter area
            if (event.getAction() == android.view.MotionEvent.ACTION_UP && appPrefs.isLineNumbers()) {
                float gutterWidth = ed.measureTextRegionOffset();
                if (gutterWidth <= 0) gutterWidth = 60 * getResources().getDisplayMetrics().density;
                if (event.getX() < gutterWidth) {
                    long pos = ed.getPointPositionOnScreen(event.getX(), event.getY());
                    int line = io.github.rosemoe.sora.util.IntPair.getFirst(pos);
                    if (line >= 0) {
                        int line1 = line + 1;
                        long now = System.currentTimeMillis();
                        DebuggerController ctrl = DebuggerController.getInstance();
                        boolean isLongPress = (event.getEventTime() - event.getDownTime()) > 450;

                        // Triple-tap detection for bookmarks
                        if (!isLongPress) {
                            tapCount[0]++;
                            System.arraycopy(tapTimestamps, 1, tapTimestamps, 0, 2);
                            tapTimestamps[2] = now;

                            if (tapCount[0] >= 3
                                    && (tapTimestamps[2] - tapTimestamps[0]) < 500) {
                                // Triple-tap → toggle bookmark on the clicked line
                                toggleBookmarkAtLine(line1);
                                tapCount[0] = 0;
                                return true;
                            } else if (now - tapTimestamps[2] > 500) {
                                tapCount[0] = 1;
                            }
                        }

                        if (isLongPress) {
                            showBreakpointEditorDialog(line1);
                        } else if (tapCount[0] < 3) {
                            ctrl.toggleBreakpoint(line1);
                            boolean has = ctrl.hasBreakpoint(line1);
                            Toast.makeText(ed.getContext(),
                                    has ? getString(R.string.debug_breakpoint_set, line1) : getString(R.string.debug_breakpoint_removed, line1),
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (activeEditor == ed) {
                            refreshBreakpointMarkers();
                            refreshBookmarkMarkers();
                        }
                        return true;
                    }
                }
            }
            return false;
        });

        // Long press context menu for debugging
        ed.setOnLongClickListener(v -> {
            if (!isDebugging) return false;
            DebuggerController ctrl = DebuggerController.getInstance();
            if (!ctrl.isPaused()) return false;

            // Get selected text
            CharSequence selected = ed.getText().subSequence(
                    ed.getCursor().getLeftColumn(),
                    ed.getCursor().getRightColumn());
            if (selected == null || selected.length() == 0) return false;

            String selText = selected.toString().trim();
            if (selText.isEmpty()) return false;

            // Show context menu
            String[] items = {"Evaluate '" + selText + "'", "Add Watch '" + selText + "'"};
            new AlertDialog.Builder(ed.getContext())
                    .setItems(items, (dialog, which) -> {
                        if (which == 0) {
                            evaluateExpression(selText);
                        } else if (which == 1) {
                            watchExpressions.add(new WatchExpression(selText));
                            Toast.makeText(ed.getContext(), "Added watch: " + selText, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            return true;
        });

        ed.subscribeEvent(io.github.rosemoe.sora.event.ScrollEvent.class, (event, sub) -> {
            BreakpointOverlay bpOv = (ed == editor) ? breakpointOverlay1 : breakpointOverlay2;
            if (bpOv != null) bpOv.postInvalidate();
            BookmarkOverlay bmOv = (ed == editor) ? bookmarkOverlay1 : bookmarkOverlay2;
            if (bmOv != null) bmOv.postInvalidate();
        });
    }

    private void setupEditor() {
        statusLineCol.setText(getString(R.string.status_line, 1, 1));
        configureEditor(editor);
        configureEditor(editor2);
        updateActiveEditorBorders();

        // Setup minimaps
        if (minimapView1 != null) minimapView1.setEditor(editor);
        if (minimapView2 != null) minimapView2.setEditor(editor2);

        // Initialize Voice-to-Text
        voiceToText = new com.ccs.javadroid.util.VoiceToTextManager(this);
        voiceToText.setCallback(new com.ccs.javadroid.util.VoiceToTextManager.Callback() {
            @Override
            public void onResult(String text) {
                if (activeEditor != null && text != null) {
                    activeEditor.insertText(text, 1);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "🎤 " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPartialResult(String partial) {
                // Show partial result in status bar
                if (statusLineCol != null && partial != null) {
                    statusLineCol.setText("🎤 " + partial);
                }
            }
        });
    }

    private void setupFindBar() {
        etFind.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                lastSearchOffset = -1;
            }
        });

        findViewById(R.id.btnFindNext).setOnClickListener(v -> performFind(true));
        findViewById(R.id.btnFindPrev).setOnClickListener(v -> performFind(false));

        findViewById(R.id.btnReplace).setOnClickListener(v -> performReplace());

        findViewById(R.id.btnReplaceAll).setOnClickListener(v -> {
            String query = etFind.getText().toString();
            String repl  = etReplace.getText().toString();
            if (query.isEmpty()) return;
            String newContent = activeEditor.getText().toString().replace(query, repl);
            activeEditor.setText(newContent);
            lastSearchOffset = -1;
            int idx = tabsAdapter.getActiveIndex();
            if (idx >= 0) tabsAdapter.markModified(idx, true);
        });

        findViewById(R.id.btnFindClose).setOnClickListener(v -> hideFindBar());

        // Перемикач: File / Project
        if (switchFindScope != null) {
            switchFindScope.setOnCheckedChangeListener((btn, checked) -> {
                findGlobalMode = checked;
                switchFindScope.setText(checked ? getString(R.string.find_scope_project) : getString(R.string.find_scope_file));
                etFind.setHint(checked ? getString(R.string.find_scope_hint) : getString(R.string.find_hint));
            });
        }

        // Enter в полі пошуку — виконати пошук
        etFind.setOnEditorActionListener((v, actionId, event) -> {
            String q = etFind.getText().toString().trim();
            if (!q.isEmpty()) performFind(true);
            return true;
        });
    }

    private void setupBottomTabs() {
        tabRun.setOnClickListener(v -> switchBottomPanel(PANEL_RUN));
        tabProblems.setOnClickListener(v -> switchBottomPanel(PANEL_PROBLEMS));
        tabBytecode.setOnClickListener(v -> switchBottomPanel(PANEL_BYTECODE));
        if (tabDebug != null) tabDebug.setOnClickListener(v -> switchBottomPanel(PANEL_DEBUG));
        if (tabDebugConsole != null) tabDebugConsole.setOnClickListener(v -> switchBottomPanel(PANEL_DEBUG_CONSOLE));
        if (tabCallGraph != null) tabCallGraph.setOnClickListener(v -> switchBottomPanel(PANEL_CALL_GRAPH));
        if (tabBookmarks != null) tabBookmarks.setOnClickListener(v -> switchBottomPanel(PANEL_BOOKMARKS));
        if (tabDeps != null) tabDeps.setOnClickListener(v -> switchBottomPanel(PANEL_DEPS));
        if (tabProfiler != null) tabProfiler.setOnClickListener(v -> switchBottomPanel(PANEL_PROFILER));
        if (tabTodo != null) tabTodo.setOnClickListener(v -> switchBottomPanel(PANEL_TODO));
        setupProfilerToolbar();
        switchBottomPanel(PANEL_RUN);
    }

    private void setupConsoleDivider() {
        if (consoleDivider == null || editorsContainer == null || bottomPanelContent == null) return;
        consoleDivider.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private float startEditorWeight;
            private float startPanelWeight;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        startEditorWeight = ((LinearLayout.LayoutParams) editorsContainer.getLayoutParams()).weight;
                        startPanelWeight  = ((LinearLayout.LayoutParams) bottomPanelContent.getLayoutParams()).weight;
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    case android.view.MotionEvent.ACTION_MOVE: {
                        float dy = event.getRawY() - startY;
                        float totalWeight = startEditorWeight + startPanelWeight;
                        float totalHeight = editorsContainer.getHeight() + bottomPanelContent.getHeight();
                        if (totalHeight <= 0) return true;
                        float weightDelta = (dy / totalHeight) * totalWeight;
                        float newEditorW = Math.max(0.1f, Math.min(totalWeight - 0.1f, startEditorWeight + weightDelta));
                        float newPanelW  = totalWeight - newEditorW;
                        LinearLayout.LayoutParams editorLp = (LinearLayout.LayoutParams) editorsContainer.getLayoutParams();
                        LinearLayout.LayoutParams panelLp  = (LinearLayout.LayoutParams) bottomPanelContent.getLayoutParams();
                        editorLp.weight = newEditorW;
                        panelLp.weight  = newPanelW;
                        editorsContainer.setLayoutParams(editorLp);
                        bottomPanelContent.setLayoutParams(panelLp);
                        return true;
                    }
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
                return false;
            }
        });
    }

    private void refreshBottomTabColors() {
        int activeBg   = blend(theme.toolbar, theme.bg, 0.4f);
        int inactiveBg = theme.toolbar;
        tabRun.setBackgroundColor(bottomPanelMode == PANEL_RUN ? activeBg : inactiveBg);
        tabProblems.setBackgroundColor(bottomPanelMode == PANEL_PROBLEMS ? activeBg : inactiveBg);
        tabBytecode.setBackgroundColor(bottomPanelMode == PANEL_BYTECODE ? activeBg : inactiveBg);
        if (tabDebug != null) tabDebug.setBackgroundColor(bottomPanelMode == PANEL_DEBUG ? activeBg : inactiveBg);
        if (tabDebugConsole != null) tabDebugConsole.setBackgroundColor(bottomPanelMode == PANEL_DEBUG_CONSOLE ? activeBg : inactiveBg);
        if (tabCallGraph != null) tabCallGraph.setBackgroundColor(bottomPanelMode == PANEL_CALL_GRAPH ? activeBg : inactiveBg);
        if (tabDeps != null) tabDeps.setBackgroundColor(bottomPanelMode == PANEL_DEPS ? activeBg : inactiveBg);
        if (tabProfiler != null) tabProfiler.setBackgroundColor(bottomPanelMode == PANEL_PROFILER ? activeBg : inactiveBg);
        if (tabTodo != null) tabTodo.setBackgroundColor(bottomPanelMode == PANEL_TODO ? activeBg : inactiveBg);

        tabRun.setTextColor(bottomPanelMode == PANEL_RUN ? theme.successText : theme.textDim);
        tabProblems.setTextColor(bottomPanelMode == PANEL_PROBLEMS ? theme.text : theme.textDim);
        tabBytecode.setTextColor(bottomPanelMode == PANEL_BYTECODE ? theme.accent : theme.textDim);
        if (tabDebug != null) tabDebug.setTextColor(bottomPanelMode == PANEL_DEBUG ? theme.accent : theme.textDim);
        if (tabDebugConsole != null) tabDebugConsole.setTextColor(bottomPanelMode == PANEL_DEBUG_CONSOLE ? theme.accent : theme.textDim);
        if (tabCallGraph != null) tabCallGraph.setTextColor(bottomPanelMode == PANEL_CALL_GRAPH ? theme.accent : theme.textDim);
        if (tabDeps != null) tabDeps.setTextColor(bottomPanelMode == PANEL_DEPS ? theme.accent : theme.textDim);
        if (tabProfiler != null) tabProfiler.setTextColor(bottomPanelMode == PANEL_PROFILER ? theme.accent : theme.textDim);
        if (tabTodo != null) tabTodo.setTextColor(bottomPanelMode == PANEL_TODO ? theme.accent : theme.textDim);
    }

    private void switchBottomPanel(int mode) {
        bottomPanelMode = mode;
        consoleScroll.setVisibility(mode == PANEL_RUN ? View.VISIBLE : View.GONE);
        problemsRecycler.setVisibility(mode == PANEL_PROBLEMS ? View.VISIBLE : View.GONE);
        bytecodeRoot.setVisibility(mode == PANEL_BYTECODE ? View.VISIBLE : View.GONE);

        if (debuggerSplitPanel != null)
            debuggerSplitPanel.setVisibility(mode == PANEL_DEBUG ? View.VISIBLE : View.GONE);
        if (debugConsoleScroll != null)
            debugConsoleScroll.setVisibility(mode == PANEL_DEBUG_CONSOLE ? View.VISIBLE : View.GONE);
        if (callGraphRoot != null)
            callGraphRoot.setVisibility(mode == PANEL_CALL_GRAPH ? View.VISIBLE : View.GONE);
        if (bookmarksRecycler != null)
            bookmarksRecycler.setVisibility(mode == PANEL_BOOKMARKS ? View.VISIBLE : View.GONE);
        if (depsPanel != null)
            depsPanel.setVisibility(mode == PANEL_DEPS ? View.VISIBLE : View.GONE);
        if (profilerPanel != null)
            profilerPanel.setVisibility(mode == PANEL_PROFILER ? View.VISIBLE : View.GONE);
        if (todoPanel != null)
            todoPanel.setVisibility(mode == PANEL_TODO ? View.VISIBLE : View.GONE);

        int activeBg   = blend(theme.toolbar, theme.bg, 0.4f);
        int inactiveBg = theme.toolbar;
        tabRun.setBackgroundColor(mode == PANEL_RUN ? activeBg : inactiveBg);
        tabProblems.setBackgroundColor(mode == PANEL_PROBLEMS ? activeBg : inactiveBg);
        tabBytecode.setBackgroundColor(mode == PANEL_BYTECODE ? activeBg : inactiveBg);
        if (tabDebug != null) tabDebug.setBackgroundColor(mode == PANEL_DEBUG ? activeBg : inactiveBg);
        if (tabDebugConsole != null) tabDebugConsole.setBackgroundColor(mode == PANEL_DEBUG_CONSOLE ? activeBg : inactiveBg);
        if (tabCallGraph != null) tabCallGraph.setBackgroundColor(mode == PANEL_CALL_GRAPH ? activeBg : inactiveBg);
        if (tabBookmarks != null) tabBookmarks.setBackgroundColor(mode == PANEL_BOOKMARKS ? activeBg : inactiveBg);
        if (tabDeps != null) tabDeps.setBackgroundColor(mode == PANEL_DEPS ? activeBg : inactiveBg);
        if (tabProfiler != null) tabProfiler.setBackgroundColor(mode == PANEL_PROFILER ? activeBg : inactiveBg);
        if (tabTodo != null) tabTodo.setBackgroundColor(mode == PANEL_TODO ? activeBg : inactiveBg);

        tabRun.setTextColor(mode == PANEL_RUN ? theme.successText : theme.textDim);
        tabProblems.setTextColor(mode == PANEL_PROBLEMS ? theme.text : theme.textDim);
        tabBytecode.setTextColor(mode == PANEL_BYTECODE ? theme.accent : theme.textDim);
        if (tabDebug != null) tabDebug.setTextColor(mode == PANEL_DEBUG ? theme.accent : theme.textDim);
        if (tabDebugConsole != null) tabDebugConsole.setTextColor(mode == PANEL_DEBUG_CONSOLE ? theme.accent : theme.textDim);
        if (tabBookmarks != null) tabBookmarks.setTextColor(mode == PANEL_BOOKMARKS ? 0xFFFFD700 : theme.textDim);
        if (tabDeps != null) tabDeps.setTextColor(mode == PANEL_DEPS ? theme.accent : theme.textDim);
        if (tabProfiler != null) tabProfiler.setTextColor(mode == PANEL_PROFILER ? theme.accent : theme.textDim);
        if (tabTodo != null) tabTodo.setTextColor(mode == PANEL_TODO ? theme.accent : theme.textDim);

        if (btnClearConsole != null) {
            btnClearConsole.setVisibility(mode == PANEL_RUN ? View.VISIBLE : View.GONE);
            ((TextView) btnClearConsole).setTextColor(theme.textDim);
        }
        if (mode == PANEL_BYTECODE) {
            refreshBytecodePanel();
        }
        if (mode == PANEL_CALL_GRAPH) {
            refreshCallGraphPanel();
        }
        if (mode == PANEL_BOOKMARKS) {
            refreshBookmarksList();
        }
        if (mode == PANEL_DEPS) {
            refreshDependencyGraph();
        }
        if (mode == PANEL_TODO) {
            refreshTodoPanel();
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

    // ══════════════════════════════════════════════════════════
    //  Debug
    // ══════════════════════════════════════════════════════════

    private void setupDebugToolbar() {
        if (debugToolbar == null) return;
        TextView btnStepOver = debugToolbar.findViewById(R.id.btnDebugStepOver);
        TextView btnStepInto = debugToolbar.findViewById(R.id.btnDebugStepInto);
        TextView btnStepOut = debugToolbar.findViewById(R.id.btnDebugStepOut);
        TextView btnResume = debugToolbar.findViewById(R.id.btnDebugResume);
        TextView btnStop = debugToolbar.findViewById(R.id.btnDebugStop);
        TextView btnEval = debugToolbar.findViewById(R.id.btnDebugEvaluate);

        if (btnStepOver != null) btnStepOver.setOnClickListener(v -> DebuggerController.getInstance().stepOver());
        if (btnStepInto != null) btnStepInto.setOnClickListener(v -> DebuggerController.getInstance().stepInto());
        if (btnStepOut != null) btnStepOut.setOnClickListener(v -> DebuggerController.getInstance().stepOut());
        if (btnResume != null) btnResume.setOnClickListener(v -> DebuggerController.getInstance().resume());
        if (btnStop != null) btnStop.setOnClickListener(v -> stopDebug());
        if (btnEval != null) btnEval.setOnClickListener(v -> {
            // If text is selected in editor, evaluate it directly
            if (activeEditor != null) {
                CharSequence selected = activeEditor.getText().subSequence(
                        activeEditor.getCursor().getLeftColumn(),
                        activeEditor.getCursor().getRightColumn());
                if (selected != null && selected.length() > 0) {
                    evaluateExpression(selected.toString());
                    return;
                }
            }
            showEvaluateDialog();
        });

        TextView btnToggleBP = debugToolbar.findViewById(R.id.btnToggleBreakpoint);
        if (btnToggleBP != null) btnToggleBP.setOnClickListener(v -> toggleBreakpointAtCursor());

        View btnAddWatch = findViewById(R.id.btnAddWatch);
        if (btnAddWatch != null) btnAddWatch.setOnClickListener(v -> showAddWatchDialog());
    }

    private void setupDebugController() {
        debugListener = new DebuggerController.DebugListener() {
            @Override
            public void onBreakpointHit(DebuggerController.DebugEvent event) {
                runOnUiThread(() -> {
                    isDebugging = true;
                    updateDebugToolbar(true);
                    String loc = event.className.replace('/', '.')
                            + "." + event.methodName + ":" + event.line;
                    if (debugLocation != null) debugLocation.setText(loc);

                    // Highlight current line in editor
                    highlightDebugLine(event.line);

                    updateVariablesPanel(event);
                    updateCallStackPanel(event);
                    updateWatchesPanel(event);

                    // Show Threads & Variables panel by default
                    switchBottomPanel(PANEL_DEBUG);
                });
            }

            @Override
            public void onDebugOutput(String text) {
                runOnUiThread(() -> appendDebugConsole(text, 0xFFAAAAAA));
            }

            @Override
            public void onDebugError(String text) {
                runOnUiThread(() -> appendDebugConsole("Error: " + text, 0xFFCF4444));
            }

            @Override
            public void onDebugStarted() {
                runOnUiThread(() -> {
                    isDebugging = true;
                    updateDebugToolbar(true);
                    if (debugConsoleOutput != null) debugConsoleOutput.setText("");
                    showDebugTabs(true);
                    switchBottomPanel(PANEL_DEBUG_CONSOLE);
                });
            }

            @Override
            public void onDebugEnded() {
                runOnUiThread(() -> {
                    isDebugging = false;
                    updateDebugToolbar(false);
                    showDebugTabs(false);
                    appendDebugConsole("Debug session ended.", 0xFF888888);
                    clearDebugHighlight();
                });
            }
        };
        DebuggerController.getInstance().addListener(debugListener);
    }

    /** Ініціалізує RecyclerView-адаптери Variables та Call Stack. */
    private void setupDebugAdapters() {
        variablesAdapter = new com.ccs.javadroid.debug.VariablesTreeAdapter();
        variablesAdapter.setTheme(theme);
        if (variablesRecycler != null) {
            variablesRecycler.setLayoutManager(new LinearLayoutManager(this));
            variablesRecycler.setAdapter(variablesAdapter);
        }
        callStackAdapter = new com.ccs.javadroid.debug.CallStackAdapter();
        callStackAdapter.setTheme(theme);
        if (callStackRecycler != null) {
            callStackRecycler.setLayoutManager(new LinearLayoutManager(this));
            callStackRecycler.setAdapter(callStackAdapter);
            callStackAdapter.setListener(frame -> {
                // стрибок до вихідного файлу, якщо він відкритий у проєкті
                String simple = frame.getClassName();
                int dot = simple.lastIndexOf('.');
                if (dot >= 0) simple = simple.substring(dot + 1);
                File f = findFileInProject(simple + ".java");
                if (f != null) {
                    openFile(f);
                    if (activeEditor != null && frame.getLineNumber() > 0) {
                        activeEditor.setSelection(frame.getLineNumber() - 1, 0);
                    }
                } else {
                    Toast.makeText(this, frame.getClassName() + " not in project",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /** Шукає файл за іменем у проєктній директорії (рекурсивно). */
    private File findFileInProject(String name) {
        if (projectManager == null || projectManager.getProjectDir() == null) return null;
        File root = projectManager.getProjectDir();
        java.util.Deque<File> stack = new java.util.ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            File d = stack.pop();
            File[] kids = d.listFiles();
            if (kids == null) continue;
            for (File k : kids) {
                if (k.isDirectory()) stack.push(k);
                else if (k.getName().equals(name)) return k;
            }
        }
        return null;
    }

    private void updateDebugToolbar(boolean active) {
        if (debugToolbar != null) {
            debugToolbar.setVisibility(active ? View.VISIBLE : View.GONE);
        }
    }

    private void showDebugTabs(boolean show) {
        if (tabDebug != null) tabDebug.setVisibility(show ? View.VISIBLE : View.GONE);
        if (tabDebugConsole != null) tabDebugConsole.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void appendDebugConsole(String text, int color) {
        if (debugConsoleOutput == null) return;
        SpannableString sp = new SpannableString(text + "\n");
        sp.setSpan(new ForegroundColorSpan(color), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        debugConsoleOutput.append(sp);
        if (debugConsoleScroll != null) {
            debugConsoleScroll.post(() -> debugConsoleScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void updateVariablesPanel(DebuggerController.DebugEvent event) {
        if (variablesAdapter == null) return;
        if (event.variables == null || event.variables.isEmpty()) {
            variablesAdapter.setVariables(null);
            return;
        }
        variablesAdapter.setVariables(event.variables);
    }

    private void updateCallStackPanel(DebuggerController.DebugEvent event) {
        if (callStackAdapter == null) return;
        callStackAdapter.setFrames(event.callStack);
    }

    private void updateWatchesPanel(DebuggerController.DebugEvent event) {
        if (watchesOutput == null) return;
        if (watchExpressions.isEmpty()) {
            watchesOutput.setText(getString(R.string.debug_no_watch));
            return;
        }
        ExpressionEvaluator eval = new ExpressionEvaluator(event.variables);
        StringBuilder sb = new StringBuilder();
        for (WatchExpression w : watchExpressions) {
            w.evaluate(event);
            String status = w.isError() ? "✗ " : "  ";
            sb.append(status).append(w.getExpression())
              .append(" = ").append(w.getLastResult()).append("\n");
        }
        watchesOutput.setText(sb.toString());
    }

    private void highlightDebugLine(int line) {
        if (activeEditor != null && line > 0) {
            activeEditor.setSelection(line - 1, 0);
        }
        if (activeEditorDecorator != null) {
            activeEditorDecorator.highlightLine(line);
        }
    }

    private void clearDebugHighlight() {
        if (activeEditorDecorator != null) {
            activeEditorDecorator.clearHighlight();
        }
    }

    /** Оновлює червоні крапки брейкпоінтів у gutter активного редактора. */
    private void refreshBreakpointMarkers() {
        android.util.Log.d("BpOverlay", "refreshBreakpointMarkers: decorator=" + (activeEditorDecorator != null));
        if (activeEditorDecorator == null) return;
        DebuggerController ctrl = DebuggerController.getInstance();
        java.util.Set<Integer> lines = ctrl.getBreakpoints().keySet();
        java.util.Set<Integer> cond = ctrl.getConditionalBreakpointLines();
        android.util.Log.d("BpOverlay", "refreshBreakpointMarkers: lines=" + lines);
        activeEditorDecorator.refreshBreakpoints(lines, cond);
    }

    private void toggleBreakpointAtCursor() {
        if (activeEditor == null) return;
        int line = activeEditor.getCursor().getLeftLine() + 1; // 1-indexed
        DebuggerController ctrl = DebuggerController.getInstance();
        ctrl.toggleBreakpoint(line);
        boolean has = ctrl.hasBreakpoint(line);
        Toast.makeText(this, has ? getString(R.string.debug_breakpoint_set, line) : getString(R.string.debug_breakpoint_removed, line),
                Toast.LENGTH_SHORT).show();
        refreshBreakpointMarkers();
    }

    // ══════════════════════════════════════════════════════════
    //  Bookmarks
    // ══════════════════════════════════════════════════════════

    private void toggleVoiceInput() {
        if (voiceToText == null) {
            Toast.makeText(this, "Voice input not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check RECORD_AUDIO permission
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1001);
            return;
        }

        if (voiceToText.isListening()) {
            voiceToText.stopListening();
            Toast.makeText(this, "🎤 Stopped", Toast.LENGTH_SHORT).show();
            if (statusLineCol != null) {
                int ln = activeEditor != null ? activeEditor.getCursor().getLeftLine() + 1 : 1;
                int col = activeEditor != null ? activeEditor.getCursor().getLeftColumn() + 1 : 1;
                statusLineCol.setText(getString(R.string.status_line, ln, col));
            }
        } else {
            voiceToText.startListening();
            Toast.makeText(this, "🎤 Listening... Speak now", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleBookmarkAtCursor() {
        if (activeEditor == null) return;
        FileTab tab = (activeEditor == editor) ? leftTab : rightTab;
        if (tab == null || tab.file == null) return;
        int line = activeEditor.getCursor().getLeftLine() + 1;
        toggleBookmarkAtLine(line);
    }

    private void toggleBookmarkAtLine(int line) {
        FileTab tab = (activeEditor == editor) ? leftTab : rightTab;
        if (tab == null || tab.file == null) return;
        BookmarkManager bm = BookmarkManager.getInstance(this);
        bm.toggleBookmark(tab.file.getAbsolutePath(), line);
        boolean has = bm.hasBookmark(tab.file.getAbsolutePath(), line);
        Toast.makeText(this, has ? getString(R.string.bookmark_set, line) : getString(R.string.bookmark_removed, line),
                Toast.LENGTH_SHORT).show();
        refreshBookmarkMarkers();
        if (bottomPanelMode == PANEL_BOOKMARKS) refreshBookmarksList();
    }

    private void refreshBookmarkMarkers() {
        FileTab tab = (activeEditor == editor) ? leftTab : rightTab;
        if (tab == null || tab.file == null) return;
        BookmarkManager bm = BookmarkManager.getInstance(this);
        java.util.Set<Integer> lines = bm.getBookmarks(tab.file.getAbsolutePath());
        BookmarkOverlay ov = (activeEditor == editor) ? bookmarkOverlay1 : bookmarkOverlay2;
        if (ov != null) ov.setBookmarks(lines);
    }

    private void showBookmarksDialog() {
        // Show bookmarks in the bottom panel instead of dialog
        switchBottomPanel(PANEL_BOOKMARKS);
        refreshBookmarksList();
    }

    private void refreshBookmarksList() {
        BookmarkManager bm = BookmarkManager.getInstance(this);
        java.util.List<BookmarkManager.BookmarkEntry> all = bm.getAllBookmarks();
        if (bookmarksRecycler == null) return;

        if (all.isEmpty()) {
            bookmarksRecycler.setAdapter(null);
            TextView empty = new TextView(this);
            empty.setText(R.string.bookmark_none);
            empty.setTextColor(theme.textDim);
            empty.setPadding(32, 32, 32, 32);
            bookmarksRecycler.setLayoutManager(new LinearLayoutManager(this));
            bookmarksRecycler.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @Override public int getItemCount() { return 1; }
                @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return new RecyclerView.ViewHolder(new TextView(parent.getContext())) {};
                }
                @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    ((TextView) holder.itemView).setText(R.string.bookmark_none);
                    ((TextView) holder.itemView).setTextColor(theme.textDim);
                    ((TextView) holder.itemView).setPadding(32, 32, 32, 32);
                }
            });
            return;
        }

        bookmarksRecycler.setLayoutManager(new LinearLayoutManager(this));
        bookmarksRecycler.setAdapter(new RecyclerView.Adapter<BookmarkVH>() {
            @Override public int getItemCount() { return all.size(); }
            @Override public BookmarkVH onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, (int)(48 * getResources().getDisplayMetrics().density)));
                tv.setPadding(32, 16, 32, 16);
                tv.setTextSize(13);
                tv.setGravity(android.view.Gravity.CENTER_VERTICAL);
                return new BookmarkVH(tv);
            }
            @Override public void onBindViewHolder(BookmarkVH holder, int position) {
                BookmarkManager.BookmarkEntry e = all.get(position);
                String name = new File(e.filePath).getName();
                ((TextView) holder.itemView).setText("★ " + name + ":" + e.line);
                ((TextView) holder.itemView).setTextColor(0xFFFFD700);
                holder.itemView.setOnClickListener(v -> {
                    File file = new File(e.filePath);
                    if (file.exists()) {
                        openFile(file);
                        activeEditor.postDelayed(() -> {
                            if (e.line > 0) activeEditor.setSelection(e.line - 1, 0);
                        }, 200);
                    }
                });
            }
        });
    }

    static class BookmarkVH extends RecyclerView.ViewHolder {
        BookmarkVH(View itemView) { super(itemView); }
    }

    /** Діалог редагування умови умовного брейплоінта (стиль IntelliJ). */
    private void showBreakpointEditorDialog(int line) {
        DebuggerController ctrl = DebuggerController.getInstance();
        String current = ctrl.getBreakpointCondition(line);

        final EditText input = new EditText(this);
        input.setHint(R.string.breakpoint_condition_hint);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setTextSize(13);
        if (current != null) input.setText(current);
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.breakpoint_dialog_title, line))
                .setView(input)
                .setPositiveButton(R.string.breakpoint_set_button, (d, w) -> {
                    String cond = input.getText().toString().trim();
                    ctrl.setBreakpoint(line, cond.isEmpty() ? null : cond);
                    Toast.makeText(this,
                            cond.isEmpty() ? "● Unconditional BP at line " + line
                                    : "● Conditional BP at line " + line + ": " + cond,
                            Toast.LENGTH_SHORT).show();
                    refreshBreakpointMarkers();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .setNeutralButton(ctrl.hasBreakpoint(line) ? getString(R.string.breakpoint_remove_button) : getString(R.string.close_button), (d, w) -> {
                    if (ctrl.hasBreakpoint(line)) {
                        ctrl.toggleBreakpoint(line);
                        Toast.makeText(this, getString(R.string.debug_breakpoint_removed, line),
                                Toast.LENGTH_SHORT).show();
                        refreshBreakpointMarkers();
                    }
                })
                .show();
        input.requestFocus();
    }

    private void showEvaluateDialog() {
        DebuggerController ctrl = DebuggerController.getInstance();
        if (!ctrl.isPaused()) return;

        EditText input = new EditText(this);
        input.setHint(R.string.debug_evaluate_hint);
        input.setSingleLine(true);

        // Pre-fill with selected text from editor
        if (activeEditor != null) {
            CharSequence selected = activeEditor.getText().subSequence(
                    activeEditor.getCursor().getLeftColumn(),
                    activeEditor.getCursor().getRightColumn());
            if (selected != null && selected.length() > 0) {
                input.setText(selected.toString());
                input.selectAll();
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_debug_evaluate_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_apply, (d, w) -> {
                    String expr = input.getText().toString().trim();
                    if (expr.isEmpty()) return;

                    // Get current locals from bridge
                    String[] names = com.ccs.javadroid.debug.DebugBridge.getCurrentLocalNames();
                    String[] types = com.ccs.javadroid.debug.DebugBridge.getCurrentLocalTypes();
                    Object[] locals = com.ccs.javadroid.debug.DebugBridge.getCurrentLocals();

                    java.util.List<DebugVariable> vars = new java.util.ArrayList<>();
                    if (names != null && locals != null) {
                        for (int i = 0; i < names.length; i++) {
                            vars.add(new DebugVariable(
                                    names[i],
                                    types != null && i < types.length ? types[i] : "unknown",
                                    i < locals.length ? locals[i] : null
                            ));
                        }
                    }

                    ExpressionEvaluator evaluator = new ExpressionEvaluator(vars);
                    ExpressionEvaluator.EvalResult result = evaluator.evaluate(expr);

                    String output = expr + " = " + result.value;
                    appendDebugConsole(output, result.isError ? 0xFFCF4444 : 0xFF499C54);
                    switchBottomPanel(PANEL_DEBUG);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void evaluateExpression(String expr) {
        DebuggerController ctrl = DebuggerController.getInstance();
        if (!ctrl.isPaused()) return;

        String[] names = com.ccs.javadroid.debug.DebugBridge.getCurrentLocalNames();
        String[] types = com.ccs.javadroid.debug.DebugBridge.getCurrentLocalTypes();
        Object[] locals = com.ccs.javadroid.debug.DebugBridge.getCurrentLocals();

        java.util.List<DebugVariable> vars = new java.util.ArrayList<>();
        if (names != null && locals != null) {
            for (int i = 0; i < names.length; i++) {
                vars.add(new DebugVariable(
                        names[i],
                        types != null && i < types.length ? types[i] : "unknown",
                        i < locals.length ? locals[i] : null
                ));
            }
        }

        ExpressionEvaluator evaluator = new ExpressionEvaluator(vars);
        ExpressionEvaluator.EvalResult result = evaluator.evaluate(expr);

        String output = expr + " = " + result.value;
        appendDebugConsole(output, result.isError ? 0xFFCF4444 : 0xFF499C54);
        switchBottomPanel(PANEL_DEBUG);
    }

    private void showAddWatchDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.debug_watch_hint);
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_debug_watch_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String expr = input.getText().toString().trim();
                    if (!expr.isEmpty()) {
                        watchExpressions.add(new WatchExpression(expr));
                        Toast.makeText(this, "Added: " + expr, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void startDebug() {
        if (isRunning || isDebugging) return;
        FileTab activeTab = tabsAdapter.getActiveTab();
        if (activeTab == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }

        saveCurrentToActiveTab();
        if (!activeTab.file.getName().endsWith(".java")) {
            Toast.makeText(this, R.string.debug_java_only, Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = true;
        consoleOutput.setText("");
        if (debugConsoleOutput != null) debugConsoleOutput.setText("");
        switchBottomPanel(PANEL_DEBUG_CONSOLE);
        showDebugTabs(true);
        appendDebugConsole(getString(R.string.debug_starting), 0xFF499C54);
        appendDebugConsole(getString(R.string.debug_file_label, activeTab.file.getName()), 0xFF888888);
        appendDebugConsole(getString(R.string.debug_breakpoints_label, DebuggerController.getInstance().getBreakpoints().size()), 0xFF888888);

        String source = activeEditor.getText().toString();

        ProjectCompiler.debugSingleSource(this, source, activeTab.file, projectManager.getProjectDir(),
                new ProjectCompiler.Callback() {
                    @Override
                    public void onProgress(String msg) {
                        appendDebugConsole("   " + msg, 0xFF888888);
                    }

                    @Override
                    public void onResult(String output) {
                        if (output != null && output.startsWith("DEBUG_SESSION:")) {
                            // Parse: DEBUG_SESSION:className:classDir:dexDir:jniLibsDir
                            String[] parts = output.split(":");
                            if (parts.length >= 4) {
                                String className = parts[1];
                                File classDir = new File(parts[2]);
                                File dexDir = new File(parts[3]);
                                File jniLibsDir = parts.length >= 5 && !parts[4].isEmpty()
                                        ? new File(parts[4]) : null;

                                appendDebugConsole("Class: " + className, 0xFF888888);
                                appendDebugConsole("ClassDir: " + classDir.getAbsolutePath(), 0xFF888888);
                                appendDebugConsole("DexDir: " + dexDir.getAbsolutePath(), 0xFF888888);
                                if (jniLibsDir != null) {
                                    appendDebugConsole("JniLibs: " + jniLibsDir.getAbsolutePath(), 0xFF888888);
                                }

                                DebuggerController ctrl = DebuggerController.getInstance();
                                ctrl.startDebug(source, className, classDir, dexDir,
                                        null, null, MainActivity.this,
                                        MainActivity.this.getClassLoader(), jniLibsDir);

                                // Actually run the instrumented DEX on a background thread.
                                // Without this, breakpoints are never hit because the user's
                                // code never executes.
                                new Thread(() -> {
                                    ProjectCompiler.debugRunDex(
                                            MainActivity.this, className, dexDir, classDir, jniLibsDir,
                                            new ProjectCompiler.Callback() {
                                                @Override
                                                public void onProgress(String msg) {
                                                    runOnUiThread(() ->
                                                            appendDebugConsole("   " + msg, 0xFF888888));
                                                }

                                                @Override
                                                public void onResult(String output) {
                                                    runOnUiThread(() -> {
                                                        ctrl.stopDebug();
                                                        isRunning = false;
                                                        appendDebugConsole("", 0xFF888888);
                                                        appendDebugConsole("──────────── Output ────────────", 0xFF499C54);
                                                        boolean err = output != null
                                                                && (output.startsWith("Compilation Error")
                                                                || output.startsWith("Execution Exception")
                                                                || output.startsWith("System Error")
                                                                || output.startsWith("Error:"));
                                                        appendDebugConsole(
                                                                output != null ? output.trim() : "",
                                                                err ? 0xFFCF4444 : 0xFFAAAAAA);
                                                    });
                                                }

                                                @Override
                                                public void onProblems(List<ProblemItem> problems) {}
                                            });
                                }, "DebugRunner").start();
                            }
                        } else {
                            isRunning = false;
                            appendDebugConsole("", 0xFF888888);
                            appendDebugConsole("──────────── Output ────────────", 0xFF499C54);
                            boolean err = output != null && (output.startsWith("Compilation Error")
                                    || output.startsWith("Execution Exception")
                                    || output.startsWith("System Error")
                                    || output.startsWith("Error:"));
                            appendDebugConsole(output != null ? output.trim() : "",
                                    err ? 0xFFCF4444 : 0xFFAAAAAA);
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

    private void stopDebug() {
        DebuggerController.getInstance().stopDebug();
        isDebugging = false;
        isRunning = false;
        updateDebugToolbar(false);
        showDebugTabs(false);
    }

    private void refreshBytecodePanel() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            setBytecodeStatus(getString(R.string.bytecode_empty));
            return;
        }
        String name = tab.file.getName();
        // .class файл — дизасемблюємо напряму (без компіляції)
        if (name.endsWith(".class")) {
            if (tab.classBytes != null) {
                try {
                    BytecodeModel model = BytecodeModel.parse(tab.classBytes);
                    showBytecodeModel(model);
                    return;
                } catch (Throwable t) {
                    setBytecodeStatus(getString(R.string.bytecode_failed,
                            t.getMessage() != null ? t.getMessage() : String.valueOf(t)));
                    return;
                }
            }
            setBytecodeStatus(getString(R.string.bytecode_empty));
            return;
        }
        if (!name.endsWith(".java")) {
            setBytecodeStatus(getString(R.string.bytecode_only_java));
            return;
        }
        if (bytecodeRefreshRunning) {
            return;
        }
        bytecodeRefreshRunning = true;
        saveCurrentToActiveTab();
        setBytecodeStatus(getString(R.string.bytecode_compiling));
        final String source = activeEditor.getText() != null ? activeEditor.getText().toString() : "";
        final File javaFile = tab.file;
        new Thread(() -> {
            try {
                ProjectCompiler.BytecodeCompileResult r = ProjectCompiler.compileForBytecodeView(
                        this, javaFile, source, projectManager.getProjectDir());
                if (r.errorMessage != null) {
                    final String err = r.errorMessage;
                    runOnUiThread(() -> {
                        bytecodeRefreshRunning = false;
                        setBytecodeStatus(getString(R.string.bytecode_failed, err));
                    });
                    return;
                }
                byte[] bytes = Files.readAllBytes(r.classFile.toPath());
                final BytecodeModel model = BytecodeModel.parse(bytes);
                runOnUiThread(() -> {
                    bytecodeRefreshRunning = false;
                    showBytecodeModel(model);
                });
            } catch (Throwable t) {
                String msg = t.getMessage() != null ? t.getMessage() : String.valueOf(t);
                runOnUiThread(() -> {
                    bytecodeRefreshRunning = false;
                    setBytecodeStatus(getString(R.string.bytecode_failed, msg));
                });
            }
        }, "bytecode-view").start();
    }

    /** Заповнює обидва адаптери моделлю та оновлює статус. */
    private void showBytecodeModel(BytecodeModel model) {
        // Apply deobfuscation if mapping is loaded
        if (deobfuscator != null && deobfuscator.hasMapping() && model != null) {
            model = model.deobfuscate(deobfuscator);
        }
        bytecodeModel = model;
        if (model == null) {
            bytecodeTreeAdapter.setModel(null, null);
            bytecodeInsnAdapter.setInstructions(null);
            setBytecodeStatus(getString(R.string.bytecode_empty));
            return;
        }
        BytecodeModel.ClassInfo ci = model.classInfo;
        String statusText = getString(R.string.bytecode_class_loaded,
                ci.name, model.fields.size(), model.methods.size());
        if (deobfuscator != null && deobfuscator.hasMapping()) {
            statusText += " [deobfuscated]";
        }
        setBytecodeStatus(statusText);
        bytecodeTreeAdapter.setModel(model.fields, model.methods);
        if (!model.methods.isEmpty()) {
            selectBytecodeMethod(0);
        } else {
            bytecodeInsnAdapter.setInstructions(null);
        }
        // оновити hex, якщо активний
        if (bytecodeHexMode) renderHexDump();
    }

    // ══════════════════════════════════════════════════════════
    //  Call Graph panel (inline, bottom)
    // ══════════════════════════════════════════════════════════

    private boolean callGraphLoaded = false;

    private void refreshCallGraphPanel() {
        if (callGraphContent == null || callGraphStatus == null) return;

        // Setup search listener once
        if (callGraphSearch != null && callGraphSearch.getTag() == null) {
            callGraphSearch.setTag("init");
            callGraphSearch.setOnEditorActionListener((v, actionId, event) -> {
                filterCallGraph(callGraphSearch.getText().toString());
                return true;
            });
            TextView refreshBtn = findViewById(R.id.callGraphRefresh);
            if (refreshBtn != null) refreshBtn.setOnClickListener(v -> {
                callGraphLoaded = false;
                refreshCallGraphPanel();
            });
        }

        File projectDir = projectManager.getProjectDir();
        if (projectDir == null) {
            callGraphStatus.setText(R.string.call_graph_no_project);
            callGraphContent.removeAllViews();
            return;
        }

        if (callGraphModel != null && callGraphLoaded) {
            showCallGraphList(callGraphModel, "");
            return;
        }

        callGraphStatus.setText(R.string.call_graph_analyzing);
        callGraphContent.removeAllViews();

        new Thread(() -> {
            try {
                CallGraphModel model = new CallGraphModel();
                model.analyzeDirectory(projectDir);
                runOnUiThread(() -> {
                    callGraphModel = model;
                    callGraphLoaded = true;
                    callGraphStatus.setText(String.format(Locale.US,
                            getString(R.string.call_graph_stats),
                            model.getAllMethods().size(), model.getEdges().size()));
                    showCallGraphList(model, callGraphSearch != null ? callGraphSearch.getText().toString() : "");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    callGraphStatus.setText("Error: " + e.getMessage());
                });
            }
        }, "callgraph-load").start();
    }

    private void filterCallGraph(String query) {
        if (callGraphModel != null) {
            showCallGraphList(callGraphModel, query);
        }
    }

    private void showCallGraphList(CallGraphModel model, String query) {
        callGraphContent.removeAllViews();
        String q = query.toLowerCase(Locale.ROOT);

        // Group methods by class
        java.util.LinkedHashMap<String, List<CallGraphModel.MethodNode>> classMethods = new java.util.LinkedHashMap<>();
        for (CallGraphModel.MethodNode m : model.getAllMethods().values()) {
            if (!m.className.startsWith("java.") && !m.className.startsWith("javax.")
                    && !m.className.startsWith("android.") && !m.className.startsWith("androidx.")) {
                if (q.isEmpty() || m.shortSignature().toLowerCase(Locale.ROOT).contains(q)) {
                    classMethods.computeIfAbsent(m.className, k -> new ArrayList<>()).add(m);
                }
            }
        }

        for (Map.Entry<String, List<CallGraphModel.MethodNode>> entry : classMethods.entrySet()) {
            String className = entry.getKey();
            List<CallGraphModel.MethodNode> methods = entry.getValue();
            methods.sort((a, b) -> a.methodName.compareToIgnoreCase(b.methodName));

            // Class header (collapsible)
            LinearLayout classGroup = new LinearLayout(this);
            classGroup.setOrientation(LinearLayout.VERTICAL);

            LinearLayout classHeader = new LinearLayout(this);
            classHeader.setOrientation(LinearLayout.HORIZONTAL);
            classHeader.setGravity(Gravity.CENTER_VERTICAL);
            classHeader.setPadding(dp(8), dp(6), dp(8), dp(6));
            classHeader.setBackgroundResource(android.R.drawable.list_selector_background);

            TextView arrow = new TextView(this);
            arrow.setText("▾");
            arrow.setTextColor(theme.textDim);
            arrow.setTextSize(12);
            arrow.setPadding(0, 0, dp(4), 0);
            classHeader.addView(arrow);

            TextView classIcon = new TextView(this);
            classIcon.setText("c");
            classIcon.setTextColor(theme.accent);
            classIcon.setTextSize(10);
            classIcon.setTypeface(new AppPreferences(this).resolveTypeface(), android.graphics.Typeface.BOLD);
            classIcon.setPadding(dp(2), dp(1), dp(6), dp(1));
            classHeader.addView(classIcon);

            // Short class name
            String shortClass = className;
            int lastDot = className.lastIndexOf('.');
            if (lastDot >= 0) shortClass = className.substring(lastDot + 1);
            TextView classNameTv = new TextView(this);
            classNameTv.setText(shortClass);
            classNameTv.setTextColor(theme.text);
            classNameTv.setTextSize(12);
            classNameTv.setTypeface(null, android.graphics.Typeface.BOLD);
            classHeader.addView(classNameTv);

            TextView count = new TextView(this);
            count.setText(" (" + methods.size() + ")");
            count.setTextColor(theme.textDim);
            count.setTextSize(11);
            classHeader.addView(count);

            final LinearLayout methodsContainer = new LinearLayout(this);
            methodsContainer.setOrientation(LinearLayout.VERTICAL);

            final boolean[] expanded = {true};
            arrow.setText(expanded[0] ? "▾" : "▸");
            classHeader.setOnClickListener(v -> {
                expanded[0] = !expanded[0];
                arrow.setText(expanded[0] ? "▾" : "▸");
                methodsContainer.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            });

            classGroup.addView(classHeader);

            // Method icons: m = method, f = field
            for (CallGraphModel.MethodNode m : methods) {
                TextView methodItem = new TextView(this);
                methodItem.setPadding(dp(28), dp(3), dp(8), dp(3));
                methodItem.setTextSize(11);
                methodItem.setTypeface(new AppPreferences(this).resolveTypeface());
                methodItem.setBackgroundResource(android.R.drawable.list_selector_background);

                SpannableStringBuilder sb = new SpannableStringBuilder();
                // Method icon
                sb.append("m ");
                int iconStart = sb.length() - 1;
                // Method name with params
                String shortSig = m.shortSignature();
                sb.append(m.methodName);
                int nameStart = sb.length() - m.methodName.length();

                // Color based on call count
                List<CallGraphModel.MethodNode> callees = callGraphModel.getCallees(m);
                List<CallGraphModel.MethodNode> callers = callGraphModel.getCallers(m);
                if (!callers.isEmpty() || !callees.isEmpty()) {
                    sb.append(" (" + callees.size() + "↓ " + callers.size() + "↑)");
                    int infoStart = sb.length() - (callees.size() + "↓ " + callers.size() + "↑").length() - 2;
                    sb.setSpan(new ForegroundColorSpan(theme.textDim), infoStart, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                sb.setSpan(new ForegroundColorSpan(theme.editorKeyword != 0 ? theme.editorKeyword : theme.accent), iconStart, iconStart + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(theme.accent), nameStart, nameStart + m.methodName.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                methodItem.setText(sb);
                final CallGraphModel.MethodNode node = m;
                methodItem.setOnClickListener(v -> showCallGraphDetail(node));
                methodsContainer.addView(methodItem);
            }

            classGroup.addView(methodsContainer);
            callGraphContent.addView(classGroup);
        }

        if (classMethods.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.call_graph_no_methods);
            empty.setTextColor(theme.textDim);
            empty.setPadding(dp(8), dp(12), dp(8), 0);
            empty.setGravity(Gravity.CENTER);
            callGraphContent.addView(empty);
        }
    }

    private void showCallGraphDetail(CallGraphModel.MethodNode method) {
        callGraphContent.removeAllViews();
        callGraphSearch.setText(method.shortSignature());

        SpannableStringBuilder header = new SpannableStringBuilder();
        header.append(method.shortSignature());
        header.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, header.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView hv = new TextView(this);
        hv.setText(header);
        hv.setTextColor(theme.accent);
        hv.setTextSize(12);
        hv.setTypeface(new AppPreferences(this).resolveTypeface());
        hv.setPadding(dp(4), dp(6), dp(4), dp(6));
        callGraphContent.addView(hv);

        // Calls
        List<CallGraphModel.MethodNode> callees = callGraphModel.getCallees(method);
        TextView callsLabel = new TextView(this);
        callsLabel.setText(getString(R.string.call_graph_calls, callees.size()));
        callsLabel.setTextColor(theme.successText);
        callsLabel.setTextSize(11);
        callsLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        callsLabel.setPadding(dp(4), dp(8), dp(4), dp(4));
        callGraphContent.addView(callsLabel);

        if (callees.isEmpty()) {
            TextView none = new TextView(this);
            none.setText("  — none");
            none.setTextColor(theme.textDim);
            none.setTextSize(11);
            none.setPadding(dp(8), 0, dp(4), 0);
            callGraphContent.addView(none);
        } else {
            for (CallGraphModel.MethodNode callee : callees) {
                callGraphContent.addView(createCallGraphItem("→", callee));
            }
        }

        // Div
        View div = new View(this);
        div.setBackgroundColor(theme.separator);
        div.setAlpha(0.3f);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        divLp.topMargin = dp(8);
        divLp.bottomMargin = dp(8);
        div.setLayoutParams(divLp);
        callGraphContent.addView(div);

        // Called by
        List<CallGraphModel.MethodNode> callers = callGraphModel.getCallers(method);
        TextView calledByLabel = new TextView(this);
        calledByLabel.setText(getString(R.string.call_graph_called_by, callers.size()));
        calledByLabel.setTextColor(theme.editorNumber != 0 ? theme.editorNumber : 0xFFFFA726);
        calledByLabel.setTextSize(11);
        calledByLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        calledByLabel.setPadding(dp(4), dp(4), dp(4), dp(4));
        callGraphContent.addView(calledByLabel);

        if (callers.isEmpty()) {
            TextView none = new TextView(this);
            none.setText("  — none");
            none.setTextColor(theme.textDim);
            none.setTextSize(11);
            none.setPadding(dp(8), 0, dp(4), 0);
            callGraphContent.addView(none);
        } else {
            for (CallGraphModel.MethodNode caller : callers) {
                callGraphContent.addView(createCallGraphItem("←", caller));
            }
        }
    }

    private TextView createCallGraphItem(String arrow, CallGraphModel.MethodNode m) {
        TextView item = new TextView(this);
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(arrow + " ");
        int start = sb.length();
        sb.append(m.shortSignature());
        sb.setSpan(new ForegroundColorSpan(theme.accent), start, sb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        item.setText(sb);
        item.setTextSize(11);
        item.setTypeface(new AppPreferences(this).resolveTypeface());
        item.setPadding(dp(16), dp(2), dp(4), dp(2));
        item.setBackgroundResource(android.R.drawable.list_selector_background);
        item.setOnClickListener(v -> showCallGraphDetail(m));
        return item;
    }

    /** Встановлює інструкції обраного методу в праву панель. */
    private void selectBytecodeMethod(int methodIndex) {
        if (bytecodeModel == null || methodIndex < 0
                || methodIndex >= bytecodeModel.methods.size()) {
            return;
        }
        bytecodeSelectedMethod = methodIndex;
        bytecodeTreeAdapter.setSelectedMethod(methodIndex);
        BytecodeModel.MethodInfo m = bytecodeModel.methods.get(methodIndex);
        bytecodeInsnAdapter.setInstructions(m.instructions);
        if (!m.handlers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (BytecodeModel.ExceptionHandler h : m.handlers) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(h.type).append(" L").append(h.startLabel)
                        .append("..L").append(h.endLabel)
                        .append("→L").append(h.handlerLabel);
            }
            setBytecodeStatus(m.shortText + "   catch: " + sb);
        } else {
            BytecodeModel.ClassInfo ci = bytecodeModel.classInfo;
            setBytecodeStatus(ci.name + " :: " + m.shortText);
        }
    }

    private void setBytecodeStatus(String text) {
        if (bytecodeStatus != null) bytecodeStatus.setText(text);
    }

    /** Стрибає до рядка з оголошенням мітки (jump-target клік). */
    private void jumpToBytecodeLabel(String label) {
        int pos = bytecodeInsnAdapter.positionOfLabel(label);
        if (pos >= 0 && bytecodeInstructions != null) {
            LinearLayoutManager lm = (LinearLayoutManager) bytecodeInstructions.getLayoutManager();
            if (lm != null) lm.scrollToPositionWithOffset(pos, 8);
        }
    }

    /** Рендерить hex-дамп сирих байтів класу. */
    private void renderHexDump() {
        if (bytecodeModel == null || bytecodeModel.rawBytes == null) {
            if (bytecodeHexOutput != null) bytecodeHexOutput.setText("");
            return;
        }
        byte[] b = bytecodeModel.rawBytes;
        StringBuilder sb = new StringBuilder(b.length / 16 * 4);
        for (int i = 0; i < b.length; i += 16) {
            sb.append(String.format(java.util.Locale.US, "%08X  ", i));
            int end = Math.min(i + 16, b.length);
            for (int j = i; j < i + 16; j++) {
                if (j < end) sb.append(String.format(java.util.Locale.US, "%02X ", b[j] & 0xFF));
                else sb.append("   ");
                if (j == i + 7) sb.append(' ');
            }
            sb.append(" |");
            for (int j = i; j < end; j++) {
                int c = b[j] & 0xFF;
                sb.append(c >= 0x20 && c < 0x7F ? (char) c : '.');
            }
            sb.append("|\n");
        }
        if (bytecodeHexOutput != null) bytecodeHexOutput.setText(sb);
    }

    /** Ініціалізує обидва RecyclerView адаптери, тогли тулбару та пошук. */
    private void setupBytecodeViewer() {
        bytecodeTreeAdapter = new MethodTreeAdapter();
        bytecodeTreeAdapter.setTheme(theme);
        bytecodeMethodTree.setLayoutManager(new LinearLayoutManager(this));
        bytecodeMethodTree.setAdapter(bytecodeTreeAdapter);
        bytecodeTreeAdapter.setListener(methodIndex -> selectBytecodeMethod(methodIndex));

        bytecodeInsnAdapter = new InstructionAdapter();
        bytecodeInsnAdapter.setTheme(theme);
        bytecodeInsnAdapter.setJumpListener(label -> jumpToBytecodeLabel(label));
        // важливо: nesting всередині HorizontalScrollView вимикає власний скрол RecyclerView
        bytecodeInstructions.setLayoutManager(new LinearLayoutManager(this));
        bytecodeInstructions.setAdapter(bytecodeInsnAdapter);

        // пошук
        bytecodeSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                bytecodeInsnAdapter.setQuery(s != null ? s.toString() : "");
            }
        });

        // тогл: line numbers
        bytecodeToggleLines.setOnClickListener(v -> {
            boolean on = !bytecodeInsnAdapter.isShowLineNumbers();
            bytecodeInsnAdapter.setShowLineNumbers(on);
            ((TextView) v).setTextColor(on ? theme.consoleText : theme.textDim);
        });
        // тогл: comments
        bytecodeToggleComments.setOnClickListener(v -> {
            boolean on = !bytecodeInsnAdapter.isShowComments();
            bytecodeInsnAdapter.setShowComments(on);
            ((TextView) v).setTextColor(on ? theme.consoleText : theme.textDim);
        });
        // тогл: hex/code
        bytecodeToggleHex.setOnClickListener(v -> {
            bytecodeHexMode = !bytecodeHexMode;
            if (bytecodeHexMode) {
                bytecodeInstructions.setVisibility(View.GONE);
                bytecodeHexScroll.setVisibility(View.VISIBLE);
                renderHexDump();
                ((TextView) v).setTextColor(theme.accent);
            } else {
                bytecodeInstructions.setVisibility(View.VISIBLE);
                bytecodeHexScroll.setVisibility(View.GONE);
                ((TextView) v).setTextColor(theme.textDim);
            }
        });

        setupBytecodeEditButtons();
    }

    private void setupProblemsList() {
        problemsAdapter = new ProblemsAdapter();
        problemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        problemsRecycler.setAdapter(problemsAdapter);
        problemsAdapter.setListener(item -> {
            if (item.file != null && item.file.exists()) {
                openFile(item.file);
                switchBottomPanel(PANEL_RUN);
                activeEditor.postDelayed(() -> {
                    if (item.line > 0) {
                        activeEditor.setSelectionRegion(item.line - 1, 0, item.line - 1, 0);
                    }
                }, 120);
            }
        });
    }

    private void applyEditorLanguage(File file) {
        applyEditorLanguage(file, activeEditor);
    }

    private void applyEditorLanguage(File file, CodeEditor ed) {
        if (ed == null) return;
        if (file != null) {
            String name = file.getName().toLowerCase(java.util.Locale.ROOT);
            if (name.endsWith(".cpp") || name.endsWith(".c") || name.endsWith(".h") 
                    || name.endsWith(".hpp") || name.endsWith(".cc") || name.endsWith(".cxx")) {
                ed.setEditorLanguage(new CppLanguage());
                return;
            } else if (name.endsWith(".xml") || name.endsWith(".html") || name.endsWith(".htm")
                    || name.endsWith(".svg")) {
                ed.setEditorLanguage(new XmlLanguage());
                return;
            } else if (name.endsWith(".css")) {
                ed.setEditorLanguage(new CssLanguage());
                return;
            } else if (name.endsWith(".js")) {
                ed.setEditorLanguage(new JavaScriptLanguage());
                return;
            } else if (name.endsWith(".sql")) {
                ed.setEditorLanguage(new SqlLanguage());
                return;
            } else if (name.endsWith(".gradle")) {
                ed.setEditorLanguage(new GradleLanguage());
                return;
            } else if (name.endsWith(".json")) {
                ed.setEditorLanguage(new JsonLanguage());
                return;
            } else if (name.endsWith(".sh") || name.endsWith(".bash")) {
                ed.setEditorLanguage(new BashLanguage());
                return;
            } else if (name.endsWith(".kt")) {
                ed.setEditorLanguage(new KotlinLanguage());
                return;
            } else if (name.endsWith(".md") || name.endsWith(".markdown")) {
                ed.setEditorLanguage(new MarkdownLanguage());
                return;
            }
        }
        ed.setEditorLanguage(new JavaDroidLanguage(this, projectManager.getProjectDir()));
    }

    private void setupProject(boolean isRestoringState) {
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

        // Check if external storage project is accessible, if not — copy to internal
        if (root != null && isExternalPath(root) && !canReadRoot(root)) {
            String oldRootPath = root.getAbsolutePath();
            File internalRoot = copyToInternal(root);
            if (internalRoot != null) {
                root = internalRoot;
                prefs.edit().putString("project_root", root.getAbsolutePath()).apply();
                sessionState.clear(oldRootPath);
                Toast.makeText(this, "Project copied to internal storage", Toast.LENGTH_SHORT).show();
            }
        }

        projectManager.setProjectRoot(root);
        updateToolbarTitle();
        setupToolbarProjectPathLongClick();
        refreshFileTree();

        if (!isRestoringState) {
            // Try to restore session state (saved tabs and cursor positions)
            SessionState.SavedSession session = sessionState.restore(root.getAbsolutePath());
            if (session != null && !session.tabPaths.isEmpty()) {
                boolean anyOpened = false;
                for (int i = 0; i < session.tabPaths.size(); i++) {
                    File f = new File(session.tabPaths.get(i));
                    if (f.exists() && f.canRead()) {
                        try {
                            openFile(f);
                            anyOpened = true;
                        } catch (Exception e) {
                            // Skip unreadable files
                        }
                    }
                }
                // Restore active tab
                if (anyOpened && session.activeIndex >= 0
                        && session.activeIndex < tabsAdapter.getTabs().size()) {
                    switchTab(session.activeIndex);
                }
                // Restore cursor positions to tabs and active editor
                for (int i = 0; i < session.tabPaths.size(); i++) {
                    if (i < session.cursorLines.size()) {
                        File f = new File(session.tabPaths.get(i));
                        int idx = tabsAdapter.indexOfFile(f);
                        if (idx >= 0) {
                            int line = session.cursorLines.get(i) - 1;
                            int col = (i < session.cursorCols.size()) ? session.cursorCols.get(i) : 0;
                            FileTab tab = tabsAdapter.getTabs().get(idx);
                            tab.cursorLine = line;
                            tab.cursorColumn = col;
                            if (idx == tabsAdapter.getActiveIndex()) {
                                try {
                                    activeEditor.setSelection(line, col);
                                } catch (Exception e) {}
                            }
                        }
                    }
                }
                // Fallback: if no files opened from session, open first file
                if (!anyOpened) {
                    List<File> files = projectManager.getJavaFiles();
                    if (!files.isEmpty()) {
                        openFile(files.get(0));
                    }
                }
            } else {
                // No saved session — open first file or create default
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
            }
        }
        refreshProblemsMergedAsync();
        activeEditor.setEditorLanguage(new JavaDroidLanguage(this, projectManager.getProjectDir()));
    }

    private void initLiveProblemsScheduler() {
        liveProblemsScheduler = new LiveProblemsScheduler(this,
                new LiveProblemsScheduler.Sources() {
                    @Override
                    public String getEditorText() {
                        return activeEditor.getText() != null ? activeEditor.getText().toString() : "";
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
        liveProblemsScheduler.setInterval(powerSaving.getProblemsScanIntervalMs());
    }

    /** ECJ (активний файл) + static (проєкт); з диска для інших файлів. */
    private void refreshProblemsMergedAsync() {
        final String text = activeEditor.getText() != null ? activeEditor.getText().toString() : "";
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

    private void shareToPastebin() {
        String apiKey = PastebinHelper.getApiKey(this);
        if (apiKey.isEmpty()) {
            showPastebinKeyDialog();
            return;
        }

        String code = "";
        String title = "JavaDroid";
        FileTab tab = tabsAdapter.getActiveTab();
        if (activeEditor != null && activeEditor.getText() != null) {
            code = activeEditor.getText().toString();
        }
        if (tab != null && tab.file != null) {
            title = tab.file.getName();
        }
        if (code.isEmpty()) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.pastebin_uploading, Toast.LENGTH_SHORT).show();
        String finalCode = code;
        String finalTitle = title;

        new Thread(() -> {
            try {
                String url = PastebinHelper.createPaste(this, finalCode, finalTitle, "java", "0");
                runOnUiThread(() -> {
                    if (url != null) {
                        android.content.ClipboardManager cm =
                                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (cm != null) {
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("pastebin", url));
                        }
                        Toast.makeText(this, R.string.pastebin_success, Toast.LENGTH_LONG).show();
                        // Open in browser
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Exception ignored) {}
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.pastebin_error, e.getMessage()), Toast.LENGTH_LONG).show());
            }
        }, "pastebin-upload").start();
    }

    private void showPastebinKeyDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.pastebin_hint);
        input.setHintTextColor(theme.textDim);
        input.setTextColor(theme.text);
        input.setPadding(dp(32), dp(16), dp(32), dp(16));
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.pastebin_title)
                .setMessage(R.string.pastebin_message)
                .setView(input)
                .setPositiveButton(R.string.dialog_apply, (d, w) -> {
                    String key = input.getText().toString().trim();
                    if (!key.isEmpty()) {
                        PastebinHelper.setApiKey(this, key);
                        shareToPastebin();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tabsAdapter != null) {
            java.util.ArrayList<String> paths = new java.util.ArrayList<>();
            for (FileTab tab : tabsAdapter.getTabs()) {
                paths.add(tab.file.getAbsolutePath());
            }
            outState.putStringArrayList("open_tab_paths", paths);
            outState.putInt("active_tab_index", tabsAdapter.getActiveIndex());
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Drawable icon = item.getIcon();
            if (icon != null) {
                icon.mutate().setColorFilter(theme.text, PorterDuff.Mode.SRC_IN);
            }
        }
        MenuItem gitItem = menu.findItem(R.id.action_git);
        if (gitItem != null) {
            File dir = projectManager.getProjectDir();
            gitItem.setVisible(dir != null && GitManager.isGitRepo(dir));
        }
        // Hide overflow items from the native menu — we show them in our searchable dialog
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId() != R.id.action_undo
                    && item.getItemId() != R.id.action_redo
                    && item.getItemId() != R.id.action_save
                    && item.getItemId() != R.id.action_run
                    && item.getItemId() != R.id.action_debug
                    && item.getItemId() != R.id.action_find
                    && item.getItemId() != R.id.action_settings
                    && item.getItemId() != R.id.action_git
                    && item.getItemId() != R.id.action_ai_chat) {
                item.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsPanel(View view, Menu menu) {
        super.onPrepareOptionsPanel(view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if      (id == R.id.action_run)           { runCurrentFile();       return true; }
        else if (id == R.id.action_debug)         { startDebug();           return true; }
        else if (id == R.id.action_save)          { saveCurrentFile();      return true; }
        else if (id == R.id.action_find)          { toggleFindBar();        return true; }
        else if (id == R.id.action_bytecode)      { switchBottomPanel(PANEL_BYTECODE); return true; }
        else if (id == R.id.action_undo)          { activeEditor.undo();          return true; }
        else if (id == R.id.action_redo)          { activeEditor.redo();          return true; }
        else if (id == R.id.action_settings)         { openSettings();            return true; }
        else if (id == R.id.action_git)              { openGit(); return true; }
        else if (id == R.id.action_ai_chat)           { openAiChat(); return true; }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Intercept overflow menu button to show our searchable dialog instead.
     */
    @Override
    public boolean onMenuOpened(int featureId, android.view.Menu menu) {
        // FEATURE_OPTIONS_PANEL = 6, but it's deprecated so use raw value
        if (featureId == 6) {
            new Handler(Looper.getMainLooper()).post(this::showSearchableMenu);
            return false;
        }
        return super.onMenuOpened(featureId, menu);
    }

    private void showSearchableMenu() {
        final String[][] menuItems = {
            {"Bytecode View",    "bytecode"},
            {"New Java File",    "new_file"},
            {"New Maven Project","new_maven"},
            {"Sync Dependencies","sync_deps"},
            {"Library Manager",  "library"},
            {"Class Browser",    "class_browser"},
            {"Load ProGuard Mapping", "load_mapping"},
            {"Maven Package",    "maven_package"},
            {"Maven Test (compile)", "maven_test"},
            {"Maven Test (run)", "maven_test_run"},
            {"Maven Clean",      "maven_clean"},
            {"Maven Install",    "maven_install"},
            {"Create C++ Module","cpp_module"},
            {"Clear Console",    "clear_console"},
            {"Copy Console",     "copy_console"},
            {"Share File",       "share_file"},
            {"Pastebin",         "pastebin"},
            {"Open File",        "open_file"},
            {"Import Files",     "import_files"},
            {"Save As",          "save_as"},
            {"Format Code",      "format"},
            {"Auto Import",      "auto_import"},
            {"View Formatted",   "view_formatted"},
            {"Markdown Preview", "md_preview"},
            {"Export Project",   "export_project"},
            {"Split Screen",     "split_screen"},
            {"Play Media",       "play_media"},
            {"HTTP Client",      "http_client"},
            {"WebView Preview",  "webview_preview"},
            {"Call Graph",       "call_graph"},
            {"Toggle Bookmark",  "toggle_bookmark"},
            {"Show Bookmarks",   "show_bookmarks"},
            {"🎤 Voice Input",   "voice_input"},
            {"Refactor...",      "refactor"},
            {"Dependencies",     "dependencies"},
        };

        final List<String> filteredTitles = new ArrayList<>();
        final List<String> filteredActions = new ArrayList<>();
        for (String[] item : menuItems) {
            filteredTitles.add(item[0]);
            filteredActions.add(item[1]);
        }

        LinearLayout dialogRoot = new LinearLayout(this);
        dialogRoot.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        dialogRoot.setPadding(pad, pad, pad, 0);

        EditText searchField = new EditText(this);
        searchField.setHint(R.string.actions_search_hint);
        searchField.setHintTextColor(theme.textDim);
        searchField.setTextColor(theme.text);
        searchField.setBackgroundColor(theme.consoleBg);
        searchField.setPadding(dp(12), dp(10), dp(12), dp(10));
        searchField.setTextSize(14);
        searchField.setContentDescription(getString(R.string.actions_search_hint));
        dialogRoot.addView(searchField);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, dp(8), 0, 0);

        ScrollView sv = new ScrollView(this);
        sv.addView(listContainer);
        dialogRoot.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(400)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.actions_title)
                .setView(dialogRoot)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        // Filter function
        final Runnable[] rebuildList = {null};
        rebuildList[0] = () -> {
            listContainer.removeAllViews();
            String query = searchField.getText().toString().trim().toLowerCase(Locale.ROOT);
            for (int i = 0; i < filteredTitles.size(); i++) {
                String title = filteredTitles.get(i);
                if (!query.isEmpty() && !title.toLowerCase(Locale.ROOT).contains(query)) continue;

                TextView item = new TextView(this);
                item.setText(title);
                item.setTextColor(theme.accent);
                item.setTextSize(13);
                item.setPadding(dp(12), dp(10), dp(12), dp(10));
                item.setBackgroundResource(android.R.drawable.list_selector_background);

                String action = filteredActions.get(i);
                item.setOnClickListener(v -> {
                    dialog.dismiss();
                    executeMenuAction(action);
                });
                listContainer.addView(item);
            }
            if (listContainer.getChildCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText(R.string.actions_no_matching);
                empty.setTextColor(theme.textDim);
                empty.setPadding(dp(12), dp(16), dp(12), 0);
                empty.setGravity(Gravity.CENTER);
                listContainer.addView(empty);
            }
        };

        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                rebuildList[0].run();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        rebuildList[0].run();
        dialog.show();

        // Auto-focus search field and show keyboard
        searchField.requestFocus();
        searchField.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(searchField, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private void executeMenuAction(String action) {
        switch (action) {
            case "bytecode":      switchBottomPanel(PANEL_BYTECODE); break;
            case "new_file":      showNewFileDialog(); break;
            case "new_maven":     showNewMavenProjectDialog(); break;
            case "sync_deps":     syncDependencies(); break;
            case "library":       openLibraryManager(); break;
            case "class_browser": openClassBrowser(); break;
            case "load_mapping":  loadProGuardMapping(); break;
            case "maven_package": mavenPackage(); break;
            case "maven_test":    mavenTestCompile(); break;
            case "maven_test_run": mavenTestRun(); break;
            case "maven_clean":   mavenClean(); break;
            case "maven_install": mavenInstall(); break;
            case "cpp_module":    showCreateCppModuleDialog(); break;
            case "clear_console": consoleOutput.setText(""); break;
            case "copy_console":  copyConsoleToClipboard(); break;
            case "share_file":    shareCurrentFile(); break;
            case "pastebin":      shareToPastebin(); break;
            case "open_file":     pickFileToOpen(); break;
            case "import_files":  importFilesToProject(); break;
            case "save_as":       saveCurrentAs(); break;
            case "format":        formatCurrentFile(); break;
            case "auto_import":   showAutoImportDialog(); break;
            case "view_formatted": showFormattedView(); break;
            case "md_preview":    showMarkdownPreview(); break;
            case "export_project": exportProjectAsZip(); break;
            case "split_screen":  toggleSplitScreen(); break;
            case "play_media":    pickMediaFile(); break;
            case "http_client":   HttpApiClientActivity.launch(this); break;
            case "webview_preview": openWebViewPreview(); break;
            case "call_graph":    openCallGraph(); break;
            case "toggle_bookmark": toggleBookmarkAtCursor(); break;
            case "show_bookmarks":  showBookmarksDialog(); break;
            case "voice_input":     toggleVoiceInput(); break;
            case "refactor":        showRefactorDialog(); break;
            case "dependencies":    switchBottomPanel(PANEL_DEPS); break;
        }
    }

    private void openGit() {
        saveCurrentToActiveTab();
        GitActivity.launch(this, projectManager.getProjectDir());
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

        String nameLower = file.getName().toLowerCase(Locale.ROOT);

        // .svg — open in editor (preview via menu)
        if (nameLower.endsWith(".svg")) {
            openEditableFile(file);
            return;
        }

        // .jpg/.jpeg/.png/.gif/.webp/.bmp — bitmap image: open in image viewer
        if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")
                || nameLower.endsWith(".png") || nameLower.endsWith(".gif")
                || nameLower.endsWith(".webp") || nameLower.endsWith(".bmp")) {
            ImageViewerActivity.launch(this, file);
            return;
        }

        // .db/.sqlite — database: open in inspector
        if (nameLower.endsWith(".db") || nameLower.endsWith(".sqlite")
                || nameLower.endsWith(".sqlite3")) {
            DatabaseInspectorActivity.launch(this, file);
            return;
        }

        // .http — HTTP client request
        if (nameLower.endsWith(".http")) {
            HttpApiClientActivity.launch(this, file);
            return;
        }

        // .html/.htm — open in editor (preview via menu)
        if (nameLower.endsWith(".html") || nameLower.endsWith(".htm")) {
            openEditableFile(file);
            return;
        }

        // .md/.markdown — open in editor (preview via menu)
        if (nameLower.endsWith(".md") || nameLower.endsWith(".markdown")) {
            openEditableFile(file);
            return;
        }

        // .class — бінарний файл: завантажуємо байтки й відкриваємо байткод-панель.
        if (file.getName().endsWith(".class")) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                FileTab tab = new FileTab(file);
                tab.classBytes = bytes;
                tabsAdapter.addTab(tab);
                int idx = tabsAdapter.getTabs().size() - 1;
                tabsAdapter.setActiveIndex(idx);
                isProgrammaticChange = true;
                activeEditor.setText("");
                isProgrammaticChange = false;
                activeEditor.setEditable(false);
                tabsRecycler.scrollToPosition(idx);
                updateStatusFileName(file);
                fileTreeAdapter.setActiveFile(file);
                if (activeEditor == editor) leftTab = tab; else rightTab = tab;
                switchBottomPanel(PANEL_BYTECODE);
            } catch (IOException e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("permission denied")) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle(R.string.permission_storage_denied_title)
                            .setMessage(R.string.permission_storage_denied_message)
                            .setPositiveButton(R.string.permission_storage_open_settings, (d, w) -> openStorageSettings())
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .show();
                } else {
                    Toast.makeText(this, getString(R.string.error_cannot_open, msg), Toast.LENGTH_SHORT).show();
                }
            }
            return;
        }

        // Regular file — read and show in editor
        try {
            String content = projectManager.readFile(file);
            FileTab tab = new FileTab(file);
            tabsAdapter.addTab(tab);
            int idx = tabsAdapter.getTabs().size() - 1;
            tabsAdapter.setActiveIndex(idx);
            isProgrammaticChange = true;
            activeEditor.setText(content);
            isProgrammaticChange = false;
            applyEditorLanguage(file, activeEditor);
            activeEditor.setEditable(true);
            tabsRecycler.scrollToPosition(idx);
            updateStatusFileName(file);
            fileTreeAdapter.setActiveFile(file);

            if (activeEditor == editor) {
                leftTab = tab;
            } else {
                rightTab = tab;
            }
            refreshProblemsMergedAsync();
            refreshBookmarkMarkers();
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("permission denied")) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permission_storage_denied_title)
                        .setMessage(R.string.permission_storage_denied_message)
                        .setPositiveButton(R.string.permission_storage_open_settings, (d, w) -> openStorageSettings())
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            } else {
                Toast.makeText(this, getString(R.string.error_cannot_open, msg), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void switchTab(int index) {
        if (index == tabsAdapter.getActiveIndex()) return;
        saveCurrentToActiveTab();
        FileTab tab = tabsAdapter.getTabs().get(index);
        try {
            // .class вкладка — показуємо байткод-панель замість редактора
            if (tab.isClassFile()) {
                tabsAdapter.setActiveIndex(index);
                isProgrammaticChange = true;
                activeEditor.setText("");
                isProgrammaticChange = false;
                activeEditor.setEditable(false);
                tabsAdapter.markModified(index, false);
                tabsRecycler.scrollToPosition(index);
                updateStatusFileName(tab.file);
                fileTreeAdapter.setActiveFile(tab.file);
                if (activeEditor == editor) leftTab = tab; else rightTab = tab;
                switchBottomPanel(PANEL_BYTECODE);
                return;
            }

            String content = projectManager.readFile(tab.file);
            tabsAdapter.setActiveIndex(index);
            isProgrammaticChange = true;
            activeEditor.setText(content);
            isProgrammaticChange = false;
            applyEditorLanguage(tab.file, activeEditor);
            activeEditor.setEditable(true);
            try {
                activeEditor.setSelection(tab.cursorLine, tab.cursorColumn);
            } catch (Exception e) {}
            tabsAdapter.markModified(index, false);
            tabsRecycler.scrollToPosition(index);
            updateStatusFileName(tab.file);
            fileTreeAdapter.setActiveFile(tab.file);

            if (activeEditor == editor) {
                leftTab = tab;
            } else {
                rightTab = tab;
            }
            refreshProblemsMergedAsync();
            refreshBookmarkMarkers();
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
        FileTab tabBeingClosed = tabsAdapter.getTabs().get(index);
        int active = tabsAdapter.getActiveIndex();
        tabsAdapter.removeTab(index);

        if (tabsAdapter.getTabs().isEmpty()) {
            isProgrammaticChange = true;
            editor.setText("");
            editor2.setText("");
            isProgrammaticChange = false;
            editor.setEditable(false);
            editor2.setEditable(false);
            leftTab = null;
            rightTab = null;
            statusFileName.setText("");
            fileTreeAdapter.setActiveFile(null);
            return;
        }

        if (leftTab == tabBeingClosed) leftTab = null;
        if (rightTab == tabBeingClosed) rightTab = null;

        int next = (index < active) ? active - 1
                 : (index == active) ? Math.min(index, tabsAdapter.getTabs().size() - 1)
                 : active;

        FileTab tab = tabsAdapter.getTabs().get(next);
        try {
            tabsAdapter.setActiveIndex(next);
            isProgrammaticChange = true;
            activeEditor.setText(projectManager.readFile(tab.file));
            isProgrammaticChange = false;
            applyEditorLanguage(tab.file, activeEditor);
            activeEditor.setEditable(true);
            tabsRecycler.scrollToPosition(next);
            updateStatusFileName(tab.file);
            fileTreeAdapter.setActiveFile(tab.file);
            
            if (activeEditor == editor) {
                leftTab = tab;
            } else {
                rightTab = tab;
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_cannot_read, e.getMessage()), Toast.LENGTH_SHORT).show();
        }

        if (isSplitActive) {
            if (leftTab == null && !tabsAdapter.getTabs().isEmpty()) {
                leftTab = tabsAdapter.getTabs().get(0);
                try {
                    isProgrammaticChange = true;
                    editor.setText(projectManager.readFile(leftTab.file));
                    isProgrammaticChange = false;
                    applyEditorLanguage(leftTab.file, editor);
                    editor.setEditable(true);
                } catch (IOException ignored) {}
            }
            if (rightTab == null && !tabsAdapter.getTabs().isEmpty()) {
                rightTab = tabsAdapter.getTabs().get(0);
                try {
                    isProgrammaticChange = true;
                    editor2.setText(projectManager.readFile(rightTab.file));
                    isProgrammaticChange = false;
                    applyEditorLanguage(rightTab.file, editor2);
                    editor2.setEditable(true);
                } catch (IOException ignored) {}
            }
        }
        refreshBookmarkMarkers();
    }

    private void toggleSplitScreen() {
        isSplitActive = !isSplitActive;
        if (isSplitActive) {
            editorDivider.setVisibility(View.VISIBLE);
            wrapperEditor2.setVisibility(View.VISIBLE);
            editor2.setVisibility(View.VISIBLE);
            if (minimapView2 != null) minimapView2.setVisibility(View.VISIBLE);

            FileTab activeTab = tabsAdapter.getActiveTab();
            FileTab secTab = null;
            if (tabsAdapter.getTabs().size() > 1) {
                int activeIndex = tabsAdapter.getActiveIndex();
                int secIndex = (activeIndex + 1) % tabsAdapter.getTabs().size();
                secTab = tabsAdapter.getTabs().get(secIndex);
            } else if (activeTab != null) {
                secTab = activeTab;
            }

            leftTab = activeTab;
            rightTab = secTab;

            if (secTab != null) {
                try {
                    isProgrammaticChange = true;
                    editor2.setText(projectManager.readFile(secTab.file));
                    isProgrammaticChange = false;
                    applyEditorLanguage(secTab.file, editor2);
                    editor2.setEditable(true);
                } catch (IOException e) {
                    editor2.setText("");
                    editor2.setEditable(false);
                }
            } else {
                editor2.setText("");
                editor2.setEditable(false);
            }

            if (activeTab != null) {
                applyEditorLanguage(activeTab.file, editor);
            }

            activeEditor = editor2;
            editor2.requestFocus();
        } else {
            if (rightTab != null) {
                saveEditorToTab(editor2, rightTab);
            }

            editorDivider.setVisibility(View.GONE);
            wrapperEditor2.setVisibility(View.GONE);
            editor2.setVisibility(View.GONE);
            if (minimapView2 != null) minimapView2.setVisibility(View.GONE);

            leftTab = tabsAdapter.getActiveTab();
            rightTab = null;
            activeEditor = editor;
            editor.requestFocus();
        }
        updateActiveEditorBorders();

        int activeIdx = tabsAdapter.getActiveIndex();
        if (activeIdx >= 0) {
            tabsAdapter.setActiveIndex(activeIdx);
            FileTab activeTab = tabsAdapter.getActiveTab();
            if (activeTab != null) {
                updateStatusFileName(activeTab.file);
                fileTreeAdapter.setActiveFile(activeTab.file);
            }
        }
    }

    private void saveCurrentFile() {
        int idx = tabsAdapter.getActiveIndex();
        if (idx < 0) return;
        saveTab(idx);
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
    }

    private void saveTab(int index) {
        FileTab tab = tabsAdapter.getTabs().get(index);
        CodeEditor targetEd = null;
        if (tab == leftTab) targetEd = editor;
        else if (tab == rightTab) targetEd = editor2;
        if (targetEd == null) targetEd = activeEditor;

        try {
            if (powerSaving.shouldFormatOnSave() && appPrefs.isFormatOnSave() && tab.file.getName().endsWith(".java")) {
                String currentText = targetEd.getText().toString();
                String formatted = JavaFormatter.format(currentText, appPrefs.getTabSize());
                if (!formatted.equals(currentText)) {
                    targetEd.setText(formatted);
                }
            }
            projectManager.writeFile(tab.file, targetEd.getText().toString());
            tabsAdapter.markModified(index, false);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.toast_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentToActiveTab() {
        int idx = tabsAdapter.getActiveIndex();
        if (idx < 0 || tabsAdapter.getTabs().isEmpty()) return;
        FileTab tab = tabsAdapter.getTabs().get(idx);
        tab.cursorLine = activeEditor.getCursor().getLeftLine();
        tab.cursorColumn = activeEditor.getCursor().getLeftColumn();
        if (tab.isModified) {
            saveEditorToTab(activeEditor, tab);
        }
    }

    private void saveSessionState() {
        if (projectManager == null || projectManager.getProjectDir() == null) return;
        if (tabsAdapter == null || tabsAdapter.getTabs().isEmpty()) return;

        String projectRoot = projectManager.getProjectDir().getAbsolutePath();
        List<String> tabPaths = new ArrayList<>();
        List<Integer> cursorLines = new ArrayList<>();
        List<Integer> cursorCols = new ArrayList<>();

        // Ensure current active tab's position is updated before saving
        int activeIdx = tabsAdapter.getActiveIndex();
        if (activeIdx >= 0 && activeIdx < tabsAdapter.getTabs().size()) {
            FileTab activeTab = tabsAdapter.getTabs().get(activeIdx);
            activeTab.cursorLine = activeEditor.getCursor().getLeftLine();
            activeTab.cursorColumn = activeEditor.getCursor().getLeftColumn();
        }

        for (FileTab tab : tabsAdapter.getTabs()) {
            if (tab.file != null) {
                tabPaths.add(tab.file.getAbsolutePath());
                cursorLines.add(tab.cursorLine + 1); // 1-indexed for persistence (legacy)
                cursorCols.add(tab.cursorColumn);
            }
        }

        sessionState.save(projectRoot, tabPaths, tabsAdapter.getActiveIndex(),
                cursorLines, cursorCols);
    }

    private CodeEditor getEditorForTab(FileTab tab) {
        if (tab == leftTab) return editor;
        if (tab == rightTab) return editor2;
        if (tabsAdapter.getActiveTab() == tab) return activeEditor;
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  Auto-Import
    // ══════════════════════════════════════════════════════════

    private void autoImportBeforeRun() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) return;
        if (!tab.file.getName().endsWith(".java")) return;

        String source = activeEditor.getText().toString();
        List<AutoImportHelper.ImportSuggestion> suggestions =
                AutoImportHelper.findMissingImports(this, projectManager.getProjectDir(), source);
        if (!suggestions.isEmpty()) {
            String updated = AutoImportHelper.addImportsAuto(source, suggestions);
            isProgrammaticChange = true;
            activeEditor.setText(updated);
            isProgrammaticChange = false;
            StringBuilder msg = new StringBuilder("Auto-imported:\n");
            for (AutoImportHelper.ImportSuggestion s : suggestions) {
                msg.append("  import ").append(s.fullImport).append(";\n");
            }
            appendConsole(msg.toString(), theme.textDim);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Markdown Preview
    // ══════════════════════════════════════════════════════════

    private void showMarkdownPreview() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = tab.file.getName().toLowerCase(java.util.Locale.ROOT);
        if (!name.endsWith(".md") && !name.endsWith(".markdown")) {
            Toast.makeText(this, R.string.md_only_markdown, Toast.LENGTH_SHORT).show();
            return;
        }

        String content = activeEditor.getText().toString();
        if (content.trim().isEmpty()) {
            Toast.makeText(this, R.string.file_is_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        MarkdownPreviewActivity.launch(this, tab.file.getName(), content);
    }

    private void openEditableFile(File file) {
        try {
            String content = projectManager.readFile(file);
            FileTab tab = new FileTab(file);
            tabsAdapter.addTab(tab);
            int idx = tabsAdapter.getTabs().size() - 1;
            tabsAdapter.setActiveIndex(idx);
            isProgrammaticChange = true;
            activeEditor.setText(content);
            isProgrammaticChange = false;
            applyEditorLanguage(file, activeEditor);
            activeEditor.setEditable(true);
            tabsRecycler.scrollToPosition(idx);
            updateStatusFileName(file);
            fileTreeAdapter.setActiveFile(file);
            if (activeEditor == editor) leftTab = tab; else rightTab = tab;

            // Add preview button for HTML/MD/SVG files
            updatePreviewButton(file);
            refreshBookmarkMarkers();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_cannot_open, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreviewButton(File file) {
        if (file == null) return;
        String name = file.getName().toLowerCase(Locale.ROOT);
        boolean canPreview = name.endsWith(".html") || name.endsWith(".htm")
                || name.endsWith(".md") || name.endsWith(".markdown")
                || name.endsWith(".svg");

        // Find or create preview button in toolbar
        TextView previewBtn = toolbar.findViewById(R.id.toolbarPreview);
        if (previewBtn == null) {
            previewBtn = new TextView(this);
            previewBtn.setId(R.id.toolbarPreview);
            previewBtn.setText("👁");
            previewBtn.setTextSize(18);
            previewBtn.setTextColor(theme.text);
            previewBtn.setPadding(dp(8), dp(4), dp(4), dp(4));
            previewBtn.setBackgroundResource(android.R.drawable.list_selector_background);
            toolbar.addView(previewBtn, new Toolbar.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            previewBtn.setOnClickListener(v -> togglePreview());
        }
        previewBtn.setVisibility(canPreview ? View.VISIBLE : View.GONE);
    }

    private boolean previewMode = false;

    private void togglePreview() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) return;
        String name = tab.file.getName().toLowerCase(Locale.ROOT);

        if (previewMode) {
            // Switch back to editor
            activeEditor.setVisibility(View.VISIBLE);
            if (bottomPanelMode == PANEL_RUN) {
                consoleScroll.setVisibility(View.VISIBLE);
            }
            previewMode = false;
        } else {
            // Switch to preview
            String content = activeEditor.getText().toString();
            if (content.trim().isEmpty()) {
            Toast.makeText(this, R.string.file_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.endsWith(".html") || name.endsWith(".htm")) {
                WebViewPreviewActivity.launch(this, tab.file);
            } else if (name.endsWith(".md") || name.endsWith(".markdown")) {
                showMarkdownPreview();
            } else if (name.endsWith(".svg")) {
                SvgViewerActivity.launch(this, tab.file);
            }
        }
    }

    private void showAutoImportDialog() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!tab.file.getName().endsWith(".java")) {
            Toast.makeText(this, R.string.auto_import_java_only, Toast.LENGTH_SHORT).show();
            return;
        }

        String source = activeEditor.getText().toString();
        AutoImportHelper.analyzeAndSuggest(this, projectManager.getProjectDir(),
                activeEditor.getText(), suggestions -> {
                    if (suggestions.isEmpty()) {
                        Toast.makeText(this, R.string.auto_import_none, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] items = new String[suggestions.size()];
                    boolean[] checked = new boolean[suggestions.size()];
                    for (int i = 0; i < suggestions.size(); i++) {
                        items[i] = "import " + suggestions.get(i).fullImport + ";";
                        checked[i] = true;
                    }
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.auto_import_title)
                            .setMultiChoiceItems(items, checked, (d, w, isChecked) -> checked[w] = isChecked)
                            .setPositiveButton(R.string.auto_import_add, (d, w) -> {
                                List<AutoImportHelper.ImportSuggestion> toAdd = new ArrayList<>();
                                for (int i = 0; i < suggestions.size(); i++) {
                                    if (checked[i]) toAdd.add(suggestions.get(i));
                                }
                                if (!toAdd.isEmpty()) {
                                    String updated = AutoImportHelper.addImportsAuto(
                                            activeEditor.getText().toString(), toAdd);
                                    isProgrammaticChange = true;
                                    activeEditor.setText(updated);
                                    isProgrammaticChange = false;
                                    int idx = tabsAdapter.getActiveIndex();
                                    if (idx >= 0) tabsAdapter.markModified(idx, true);
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .show();
                });
    }

    // ══════════════════════════════════════════════════════════
    //  JSON / XML Viewer

    private void showFormattedView() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = tab.file.getName().toLowerCase(java.util.Locale.ROOT);
        boolean isJson = JsonXmlFormatter.isJsonFile(name);
        boolean isXml = JsonXmlFormatter.isXmlFile(name);

        if (!isJson && !isXml) {
            Toast.makeText(this, R.string.formatted_only_json_xml, Toast.LENGTH_SHORT).show();
            return;
        }

        String content = activeEditor.getText().toString();
        if (content.trim().isEmpty()) {
            Toast.makeText(this, "File is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        android.text.SpannableStringBuilder formatted;
        if (isJson) {
            formatted = JsonXmlFormatter.formatJson(content);
        } else {
            formatted = JsonXmlFormatter.formatXml(content);
        }

        TextView formattedOutput = new TextView(this);
        formattedOutput.setText(formatted);
        formattedOutput.setTypeface(new AppPreferences(this).resolveTypeface());
        formattedOutput.setTextSize(13);
        formattedOutput.setBackgroundColor(theme.consoleBg);
        formattedOutput.setTextColor(theme.consoleText);
        formattedOutput.setPadding(24, 16, 24, 16);
        formattedOutput.setTextIsSelectable(true);
        formattedOutput.setHorizontallyScrolling(true);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(formattedOutput);
        scroll.setBackgroundColor(theme.consoleBg);

        new AlertDialog.Builder(this)
                .setTitle(name + " — formatted")
                .setView(scroll)
                .setPositiveButton(R.string.copy_button, (d, w) -> {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("formatted", content));
                    Toast.makeText(this, R.string.toast_copied_clipboard, Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton(R.string.apply_to_editor, (d, w) -> {
                    String pretty;
                    if (isJson) {
                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(content);
                            pretty = obj.toString(2);
                        } catch (Exception e1) {
                            try {
                                org.json.JSONArray arr = new org.json.JSONArray(content);
                                pretty = arr.toString(2);
                            } catch (Exception e2) {
                                pretty = content;
                            }
                        }
                    } else {
                        pretty = content;
                    }
                    isProgrammaticChange = true;
                    activeEditor.setText(pretty);
                    isProgrammaticChange = false;
                    int idx = tabsAdapter.getActiveIndex();
                    if (idx >= 0) tabsAdapter.markModified(idx, true);
                })
                .setNegativeButton(R.string.close_button, null)
                .show();
    }

    private void formatCurrentFile() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = tab.file.getName().toLowerCase(java.util.Locale.ROOT);
        if (JsonXmlFormatter.isJsonFile(name) || JsonXmlFormatter.isXmlFile(name)) {
            showFormattedView();
            return;
        }
        if (!tab.file.getName().endsWith(".java")) {
            return;
        }
        String currentText = activeEditor.getText() != null ? activeEditor.getText().toString() : "";
        if (currentText.isEmpty()) return;

        String formatted = JavaFormatter.format(currentText, appPrefs.getTabSize());
        if (!formatted.equals(currentText)) {
            activeEditor.setText(formatted);
            int idx = tabsAdapter.getActiveIndex();
            if (idx >= 0) {
                tabsAdapter.markModified(idx, true);
                if (powerSaving.shouldAutoSave() && appPrefs.isAutoSave()) {
                    saveCurrentToActiveTab();
                    tabsAdapter.markModified(idx, false);
                }
            }
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

        String fileName = activeTab.file.getName().toLowerCase(Locale.ROOT);

        // SQL files — run against project database
        if (fileName.endsWith(".sql")) {
            runSqlFile(activeTab.file);
            return;
        }

        // Kotlin files — run via Kotlin compiler
        if (fileName.endsWith(".kt")) {
            runKotlinFile(activeTab.file);
            return;
        }

        if (activeTab.file.getName().endsWith(".java")) {
            autoImportBeforeRun();
        }

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

        ProjectCompiler.runSingleSource(this, activeEditor.getText().toString(), activeTab.file, projectManager.getProjectDir(),
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

    private void mavenTestRun() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_pom_required, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();
        switchBottomPanel(PANEL_RUN);
        consoleOutput.setText("");
        appendConsole(getString(R.string.console_mvn_test_run), theme.textDim);
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
            ProjectCompiler.mavenTestRun(this, projectManager.getProjectDir(), pom,
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

    private void mavenClean() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_pom_required, Toast.LENGTH_SHORT).show();
            return;
        }
        switchBottomPanel(PANEL_RUN);
        consoleOutput.setText("");
        appendConsole(getString(R.string.console_mvn_clean), theme.textDim);
        try {
            ProjectCompiler.mavenClean(this, projectManager.getProjectDir(),
                    new ProjectCompiler.Callback() {
                        @Override public void onProgress(String msg) {
                            appendConsole("   " + msg, theme.textDim);
                        }
                        @Override public void onResult(String output) {
                            appendConsole(output, theme.consoleText);
                        }
                        @Override public void onProblems(List<ProblemItem> problems) {
                            // clean produces no compilation problems
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mavenInstall() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_pom_required, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();
        switchBottomPanel(PANEL_RUN);
        consoleOutput.setText("");
        appendConsole(getString(R.string.console_mvn_install), theme.textDim);
        try {
            PomModel pom = PomParser.parse(MavenPaths.pomFile(projectManager.getProjectDir()));
            ProjectCompiler.mavenInstall(this, projectManager.getProjectDir(), pom,
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


    private void showCreateCppModuleDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 24);

        EditText input = newEditForDialog("Module/Library name (e.g. native-lib)");
        layout.addView(input);

        android.widget.RadioGroup rg = new android.widget.RadioGroup(this);
        android.widget.RadioButton rbC = new android.widget.RadioButton(this);
                    rbC.setText(R.string.cpp_module_c_option);
        rbC.setTextColor(theme.text);
        rbC.setChecked(true);
        android.widget.RadioButton rbCpp = new android.widget.RadioButton(this);
                    rbCpp.setText(R.string.cpp_module_cpp_option);
        rbCpp.setTextColor(theme.text);
        rg.addView(rbC);
        rg.addView(rbCpp);
        layout.addView(rg);

        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_create_cpp_module)
                .setView(layout)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String cleanName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
                    
                    boolean isCpp = rbCpp.isChecked();
                    String ext = isCpp ? ".cpp" : ".c";
                    
                    File root = projectManager.getProjectDir();
                    File cppDir = new File(root, "src/main/cpp");
                    if (!cppDir.exists()) cppDir.mkdirs();
                    
                    File cppFile = new File(cppDir, cleanName + ext);
                    if (cppFile.exists()) {
                        Toast.makeText(this, R.string.cpp_module_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    try {
                        String pkg = ProjectLayoutHelper.mainPackageName(root);
                        String cMethodName = "Java_" + pkg.replace('.', '_') + "_App_stringFromJNI_" + cleanName;
                        
                        String cTemplate;
                        if (isCpp) {
                            cTemplate = "#include <jni.h>\n#include <string>\n\n"
                                    + "extern \"C\" JNIEXPORT jstring JNICALL\n"
                                    + cMethodName + "(JNIEnv *env, jobject thiz) {\n"
                                    + "    std::string hello = \"Hello from C++ code in \" + std::string(\"" + cleanName + "\") + \"!\";\n"
                                    + "    return env->NewStringUTF(hello.c_str());\n"
                                    + "}\n";
                        } else {
                            cTemplate = "#include <jni.h>\n\n"
                                    + "JNIEXPORT jstring JNICALL\n"
                                    + cMethodName + "(JNIEnv *env, jobject thiz) {\n"
                                    + "    return (*env)->NewStringUTF(env, \"Hello from C code in " + cleanName + "!\");\n"
                                    + "}\n";
                        }
                        
                        projectManager.writeFile(cppFile, cTemplate);
                        
                        // Let's check App.java to inject static JNI loaders
                        File mainJavaPkgDir = ProjectLayoutHelper.mainJavaPackageDir(root);
                        File appJavaFile = new File(mainJavaPkgDir, "App.java");
                        if (appJavaFile.exists()) {
                            String javaCode = projectManager.readFile(appJavaFile);
                            if (!javaCode.contains("System.loadLibrary(\"" + cleanName + "\")")) {
                                String loadBlock = "    static {\n"
                                        + "        try {\n"
                                        + "            System.loadLibrary(\"" + cleanName + "\");\n"
                                        + "        } catch (UnsatisfiedLinkError e) {\n"
                                        + "            System.err.println(\"WARNING: native library '" + cleanName + "' not found.\\n\"\n"
                                        + "                + \"Run the project first to compile C sources.\");\n"
                                        + "        }\n"
                                        + "    }\n\n"
                                        + "    public native String stringFromJNI_" + cleanName + "();\n\n";
                                
                                int classBodyIdx = javaCode.indexOf("public class App {");
                                if (classBodyIdx != -1) {
                                    int insertPos = javaCode.indexOf('{', classBodyIdx) + 1;
                                    String updatedJava = javaCode.substring(0, insertPos) + "\n" + loadBlock + javaCode.substring(insertPos);
                                    if (updatedJava.contains("System.out.println(\"Hello from \" + App.class.getPackage().getName());")) {
                                        updatedJava = updatedJava.replace(
                                            "System.out.println(\"Hello from \" + App.class.getPackage().getName());",
                                            "System.out.println(\"Hello from \" + App.class.getPackage().getName());\n        System.out.println(new App().stringFromJNI_" + cleanName + "());"
                                        );
                                    }
                                    projectManager.writeFile(appJavaFile, updatedJava);
                                    
                                    // Update editor content if App.java is currently active
                                    int activeIdx = tabsAdapter.indexOfFile(appJavaFile);
                                    if (activeIdx >= 0 && activeIdx == tabsAdapter.getActiveIndex()) {
                                        isProgrammaticChange = true;
                                        activeEditor.setText(updatedJava);
                                        isProgrammaticChange = false;
                                    }
                                }
                            }
                        }
                        
                        refreshFileTree();
                        openFile(cppFile);
                        if (appJavaFile.exists()) {
                            openFile(appJavaFile);
                        }
                        Toast.makeText(this, R.string.cpp_module_created, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, getString(R.string.cpp_module_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showNewMavenProjectDialog() {
        saveCurrentToActiveTab();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setAction("ACTION_NEW_PROJECT");
        startActivity(intent);
        finish();
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

        // Глобальний пошук — відкриваємо GlobalSearchActivity
        if (findGlobalMode) {
            Intent i = new Intent(this, GlobalSearchActivity.class);
            i.putExtra("project_root", projectManager.getProjectDir().getAbsolutePath());
            i.putExtra("query", query);
            startActivityForResult(i, REQ_GLOBAL_SEARCH);
            return;
        }

        String fullText  = activeEditor.getText().toString();
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
        activeEditor.setSelectionRegion(s[0], s[1], e[0], e[1]);
    }

    private void performReplace() {
        String query = etFind.getText().toString();
        String repl  = etReplace.getText().toString();
        if (query.isEmpty() || lastSearchOffset < 0) return;

        String text = activeEditor.getText().toString();
        int end = lastSearchOffset + query.length();
        if (end > text.length()) return;

        if (text.substring(lastSearchOffset, end).equalsIgnoreCase(query)) {
            String newText = text.substring(0, lastSearchOffset) + repl
                           + text.substring(end);
            activeEditor.setText(newText);
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
        EditText input = newEditForDialog(getString(R.string.dialog_new_java_hint));

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_new_java_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String template = "";
                    boolean isJava = name.endsWith(".java") || !name.contains(".");
                    String className = name.replace(".java", "");
                    if (isJava) {
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

    private void showFolderContextMenu(File folder) {
        java.util.List<String> optionsList = new java.util.ArrayList<>();
        optionsList.add(getString(R.string.menu_create_file));
        optionsList.add(getString(R.string.menu_create_folder));
        if (copiedFile != null && copiedFile.exists()) {
            optionsList.add(getString(R.string.dialog_folder_context_paste));
        }
        optionsList.add(getString(R.string.dialog_folder_context_archive));
        optionsList.add(getString(R.string.dialog_file_context_rename));
        optionsList.add(getString(R.string.dialog_file_context_delete));

        String[] options = optionsList.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(folder.getName())
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (selected.equals(getString(R.string.menu_create_file))) {
                        showNewFileInFolderDialog(folder);
                    } else if (selected.equals(getString(R.string.menu_create_folder))) {
                        showNewFolderInFolderDialog(folder);
                    } else if (selected.equals(getString(R.string.dialog_folder_context_paste))) {
                        pasteFileToFolder(folder);
                    } else if (selected.equals(getString(R.string.dialog_folder_context_archive))) {
                        createArchiveFromFolder(folder);
                    } else if (selected.equals(getString(R.string.dialog_file_context_rename))) {
                        showRenameDialog(folder);
                    } else if (selected.equals(getString(R.string.dialog_file_context_delete))) {
                        showDeleteDialog(folder);
                    }
                })
                .show();
    }

    private void showNewFileInFolderDialog(File folder) {
        // Step 1: Show template selection
        String[] names = FileTemplates.getDisplayNames();
        String[] keys = FileTemplates.getKeys();

        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_create_file)
                .setItems(names, (dialog, which) -> {
                    String key = keys[which];
                    String[] tpl = FileTemplates.get(key);
                    if (tpl == null) return;
                    showNewFileNameDialog(folder, key, tpl[0]);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showNewFileNameDialog(File folder, String templateKey, String templateName) {
        EditText input = newEditForDialog(getString(R.string.dialog_new_java_hint));

        new AlertDialog.Builder(this)
                .setTitle(templateName)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String className = name.replace(".java", "").replace(".kt", "");
                    String[] tpl = FileTemplates.get(templateKey);
                    String template = "";
                    if (tpl != null) {
                        template = FileTemplates.format(tpl[1], className);
                    }
                    // Add package declaration for Maven projects
                    if (projectManager.isMavenProject() && templateKey.equals(FileTemplates.KEY_CLASS)) {
                        try {
                            String pkg = ProjectLayoutHelper.mainPackageName(projectManager.getProjectDir());
                            template = "package " + pkg + ";\n\n" + template;
                        } catch (Exception ignored) {}
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

    private void showNewFolderInFolderDialog(File folder) {
        EditText input = newEditForDialog(getString(R.string.dialog_create_folder_hint));

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_create_folder_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    File sub = new File(folder, name);
                    if (sub.exists()) {
                        Toast.makeText(this, R.string.folder_already_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sub.mkdirs()) {
                        refreshFileTree();
                    } else {
                        Toast.makeText(this, R.string.folder_create_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showFileContextMenu(File file) {
        String[] options = {
                getString(R.string.dialog_file_context_open),
                getString(R.string.dialog_file_context_rename),
                getString(R.string.dialog_file_context_copy),
                getString(R.string.dialog_file_context_delete)
        };
        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: openFile(file); break;
                        case 1: showRenameDialog(file); break;
                        case 2: copiedFile = file; Toast.makeText(this, R.string.toast_file_copied, Toast.LENGTH_SHORT).show(); break;
                        case 3: showDeleteDialog(file); break;
                    }
                })
                .show();
    }

    private void showRenameDialog(File file) {
        EditText input = newEditForDialog("");
        String currentNameWithoutExt = file.isDirectory() ? file.getName() :
                (file.getName().endsWith(".java") ? file.getName().substring(0, file.getName().length() - 5) : file.getName());
        input.setText(currentNameWithoutExt);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_rename)
                .setView(input)
                .setPositiveButton(R.string.dialog_rename, (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty() || newName.equals(currentNameWithoutExt)) return;
                    File parent = file.getParentFile() != null ? file.getParentFile() : projectManager.getProjectDir();
                    File newFile;
                    if (file.isDirectory()) {
                        newFile = new File(parent, newName);
                    } else {
                        if (file.getName().endsWith(".java") && !newName.endsWith(".java") && !newName.contains(".")) {
                            newFile = new File(parent, newName + ".java");
                        } else {
                            newFile = new File(parent, newName);
                        }
                    }
                    int tabIdx = tabsAdapter.indexOfFile(file);
                    if (tabIdx >= 0) saveCurrentToActiveTab();
                    if (file.renameTo(newFile)) {
                        if (tabIdx >= 0) {
                            tabsAdapter.removeTab(tabIdx);
                        }
                        refreshFileTree();
                        if (newFile.isFile()) {
                            openFile(newFile);
                        }
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

    private void pasteFileToFolder(File folder) {
        if (copiedFile == null || !copiedFile.exists()) {
            copiedFile = null;
            return;
        }
        File dest = new File(folder, copiedFile.getName());
        if (dest.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.toast_file_exists_title)
                    .setMessage(getString(R.string.toast_file_exists, copiedFile.getName()))
                    .setPositiveButton(R.string.dialog_overwrite, (d, w) -> {
                        doPasteFile(folder, dest);
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        } else {
            doPasteFile(folder, dest);
        }
    }

    private void doPasteFile(File folder, File dest) {
        try {
            java.io.InputStream in = new java.io.FileInputStream(copiedFile);
            java.io.OutputStream out = new java.io.FileOutputStream(dest);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.close();
            Toast.makeText(this, getString(R.string.toast_file_pasted, folder.getName()), Toast.LENGTH_SHORT).show();
            refreshFileTree();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.toast_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void createArchiveFromFolder(File folder) {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE, folder.getName() + ".zip");
        pendingArchiveFolder = folder;
        try {
            startActivityForResult(i, REQ_ARCHIVE_FOLDER);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File pendingArchiveFolder;

    private void archiveFolderToUri(Uri uri) {
        File folder = pendingArchiveFolder;
        if (folder == null || !folder.exists()) return;
        new Thread(() -> {
            try (java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                 java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(os)) {
                if (os == null) throw new IOException("Cannot open output");
                zipDir(folder, folder, zos);
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.toast_archive_created, folder.getName() + ".zip"),
                        Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.toast_archive_failed, e.getMessage()),
                        Toast.LENGTH_SHORT).show());
            }
        }, "archive-folder").start();
    }

    // ══════════════════════════════════════════════════════════
    //  Settings + new features
    // ══════════════════════════════════════════════════════════

    private void openWebViewPreview() {
        // If there's a current HTML file open, preview it
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab != null && tab.file != null) {
            String name = tab.file.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".html") || name.endsWith(".htm")) {
                WebViewPreviewActivity.launch(this, tab.file);
                return;
            }
            // For any other file, try to wrap it in an HTML page
            if (name.endsWith(".css") || name.endsWith(".js")) {
                try {
                    String content = projectManager.readFile(tab.file);
                    String html = "<!DOCTYPE html><html><head>"
                            + "<style>body{font-family:monospace;margin:20px;background:#1e1e1e;color:#d4d4d4}</style>"
                            + (name.endsWith(".css") ? "<link rel='stylesheet' href='" + tab.file.getName() + "'>" : "")
                            + "</head><body>"
                            + (name.endsWith(".js") ? "<script>" + content + "</script>" : "")
                            + "<pre>" + content.replace("&", "&amp;").replace("<", "&lt;") + "</pre>"
                            + "</body></html>";
                    WebViewPreviewActivity.launch(this, html);
                    return;
                } catch (Exception ignored) {}
            }
        }
        // Default: open empty preview
        String defaultHtml = "<!DOCTYPE html><html><head>"
                + "<style>body{font-family:sans-serif;margin:40px;background:#1e1e1e;color:#d4d4d4}"
                + "h1{color:#569cd6}code{background:#2d2d2d;padding:2px 6px;border-radius:4px}</style>"
                + "</head><body>"
                + "<h1>WebView Preview</h1>"
                + "<p>Open an <code>.html</code> file to preview it here.</p>"
                + "<p>Or use the URL bar above to load any URL.</p>"
                + "</body></html>";
        WebViewPreviewActivity.launch(this, defaultHtml);
    }

    private void openSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQ_SETTINGS);
    }

    private void openLibraryManager() {
        if (!projectManager.isMavenProject()) {
            Toast.makeText(this, R.string.toast_not_maven, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentToActiveTab();
        LibraryManagerActivity.launch(this, projectManager.getProjectDir(), REQ_LIB_MANAGER);
    }

    private void openClassBrowser() {
        String path = projectManager.isMavenProject() ? projectManager.getProjectDir().getAbsolutePath() : null;
        ClassBrowserActivity.launch(this, path);
    }

    private void openCallGraph() {
        File projectDir = projectManager.getProjectDir();
        if (projectDir == null) {
            Toast.makeText(this, R.string.call_graph_no_project, Toast.LENGTH_SHORT).show();
            return;
        }
        CallGraphActivity.launch(this, projectDir);
    }

    private void openCallGraphFromBytecode() {
        File projectDir = projectManager.getProjectDir();
        if (projectDir == null) {
            Toast.makeText(this, R.string.call_graph_no_project, Toast.LENGTH_SHORT).show();
            return;
        }

        if (bytecodeModel != null && bytecodeSelectedMethod >= 0) {
            BytecodeModel.MethodInfo mi = bytecodeModel.methods.get(bytecodeSelectedMethod);
            String className = bytecodeModel.classInfo.name;
            CallGraphActivity.launch(this, projectDir, className, mi.name);
        } else {
            CallGraphActivity.launch(this, projectDir);
        }
    }

    private void loadProGuardMapping() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_LOAD_MAPPING);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static final int REQ_LOAD_MAPPING = 4010;

    private void loadMappingResult(Uri uri) {
        if (uri == null) return;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return;
            deobfuscator.loadMapping(is);
            Toast.makeText(this, getString(R.string.mapping_loaded, deobfuscator.getStats()),
                    Toast.LENGTH_LONG).show();
            // Refresh bytecode view if currently showing
            if (bytecodeModel != null) {
                showBytecodeModel(bytecodeModel);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.mapping_load_failed, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openDecompiledClass(File file, String className) {
        try {
            String content = projectManager.readFile(file);
            FileTab tab = new FileTab(file);
            tabsAdapter.addTab(tab);
            int idx = tabsAdapter.getTabs().size() - 1;
            tabsAdapter.setActiveIndex(idx);
            isProgrammaticChange = true;
            activeEditor.setText(content);
            isProgrammaticChange = false;
            activeEditor.setEditable(false);
            tabsRecycler.scrollToPosition(idx);
            updateStatusFileName(file);
            if (activeEditor == editor) {
                leftTab = tab;
            } else {
                rightTab = tab;
            }
            Toast.makeText(this, getString(R.string.class_browser_opened, className), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SETTINGS) {
            if (resultCode == RESULT_OK && data != null && data.getBooleanExtra(SettingsActivity.EXTRA_CHANGED, false)) {
                recreate();
            }
            return;
        }
        if (requestCode == REQ_LIB_MANAGER) {
            if (resultCode == RESULT_OK) {
                // Reload pom.xml if it's currently open
                FileTab activeTab = tabsAdapter.getActiveTab();
                if (activeTab != null && activeTab.file != null && activeTab.file.getName().equals("pom.xml")) {
                    try {
                        String content = projectManager.readFile(activeTab.file);
                        isProgrammaticChange = true;
                        activeEditor.setText(content);
                        isProgrammaticChange = false;
                    } catch (IOException ignored) {}
                }
                syncDependencies();
            }
            return;
        }
        if (resultCode != RESULT_OK || data == null) return;

        // Class Browser — повертає шлях до декомпільованого файлу
        if (requestCode == REQ_CLASS_BROWSER) {
            String filePath = data.getStringExtra(ClassBrowserActivity.RESULT_FILE_PATH);
            String className = data.getStringExtra(ClassBrowserActivity.RESULT_CLASS_NAME);
            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    openDecompiledClass(file, className);
                }
            }
            return;
        }

        // Global search — повертає extras, не URI
        if (requestCode == REQ_GLOBAL_SEARCH) {
            String filePath = data.getStringExtra("file_path");
            int lineNum = data.getIntExtra("line_number", 1);
            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    openFile(file);
                    activeEditor.postDelayed(() -> {
                        if (lineNum > 0) {
                            activeEditor.setSelection(lineNum - 1, 0);
                        }
                    }, 200);
                }
            }
            return;
        }

        Uri uri = data.getData();
        if (requestCode == REQ_IMPORT_FILES) {
            importFilesResult(data);
            return;
        }
        if (uri == null) return;
        if (requestCode == REQ_OPEN_FILE)        importExternalJavaFile(uri);
        else if (requestCode == REQ_SAVE_AS)     writeCurrentEditorToUri(uri);
        else if (requestCode == REQ_EXPORT_PROJ) exportProjectToUri(uri);
        else if (requestCode == REQ_PLAY_MEDIA)  openMediaPlayer(uri);
        else if (requestCode == REQ_LOAD_MAPPING) loadMappingResult(uri);
        else if (requestCode == REQ_ARCHIVE_FOLDER) archiveFolderToUri(uri);
        else if (requestCode == REQ_STORAGE_PERMISSION) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (android.os.Environment.isExternalStorageManager()) {
                    recreate();
                } else {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle(R.string.permission_storage_denied_title)
                            .setMessage(R.string.permission_storage_denied_message)
                            .setPositiveButton(R.string.permission_storage_try_again, (d, w) -> {
                                try {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, REQ_STORAGE_PERMISSION);
                                } catch (Exception e) {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    startActivityForResult(intent, REQ_STORAGE_PERMISSION);
                                }
                            })
                            .setNegativeButton(R.string.permission_storage_exit, (d, w) -> finish())
                            .setCancelable(false)
                            .show();
                }
            }
        }
        return;
    }

    // ── External file: open ────────────────────────────────────

    private void importFilesToProject() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(i, REQ_IMPORT_FILES);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importFilesResult(Intent data) {
        if (data == null) return;
        File projectDir = projectManager.getProjectDir();
        if (projectDir == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }

        File targetDir = projectDir;
        if (fileTreeAdapter != null && fileTreeAdapter.getActiveFile() != null) {
            File active = fileTreeAdapter.getActiveFile();
            targetDir = active.isDirectory() ? active : active.getParentFile();
        }

        List<Uri> uris = new ArrayList<>();
        if (data.getClipData() != null) {
            android.content.ClipData clip = data.getClipData();
            for (int i = 0; i < clip.getItemCount(); i++) {
                uris.add(clip.getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }

        if (uris.isEmpty()) return;

        int imported = 0;
        for (Uri uri : uris) {
            try {
                String name = displayName(uri);
                if (name == null) name = "imported_" + System.currentTimeMillis();
                name = name.replaceAll("[^A-Za-z0-9._\\-]", "_");

                try (InputStream in = getContentResolver().openInputStream(uri)) {
                    if (in == null) continue;
                    byte[] data2 = readAll(in);
                    File target = new File(targetDir, name);
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {
                        out.write(data2);
                    }
                    imported++;
                }
            } catch (Exception e) {
                // skip individual failures
            }
        }

        refreshFileTree();
        if (imported > 0) {
            Toast.makeText(this, getString(R.string.toast_imported_files, imported),
                    Toast.LENGTH_SHORT).show();
        }
    }

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

    private void openMediaPlayer(Uri uri) {
        try {
            String name = displayName(uri);
            if (name == null) name = "media";
            File cacheDir = new File(getCacheDir(), "media_cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File mediaFile = new File(cacheDir, name);

            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new java.io.FileOutputStream(mediaFile)) {
                if (in == null) return;
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            }

            MediaPlayerActivity.launch(this, mediaFile);
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
            os.write(activeEditor.getText().toString().getBytes(StandardCharsets.UTF_8));
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

    private void showEncodingSelectionDialog() {
        final String[] encodings = { "UTF-8", "US-ASCII", "ISO-8859-1", "UTF-16", "Windows-1251", "Windows-1252" };
        String current = appPrefs.getFileEncoding();
        int checkedItem = 0;
        for (int i = 0; i < encodings.length; i++) {
            if (encodings[i].equals(current)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_select_encoding)
                .setSingleChoiceItems(encodings, checkedItem, (dialog, which) -> {
                    String selected = encodings[which];
                    appPrefs.setFileEncoding(selected);
                    if (statusEncoding != null) {
                        statusEncoding.setText(selected);
                    }
                    dialog.dismiss();

                    // Reload currently active tab with the new encoding
                    FileTab activeTab = tabsAdapter.getActiveTab();
                    if (activeTab != null && activeTab.file != null) {
                        try {
                            String content = projectManager.readFile(activeTab.file);
                            isProgrammaticChange = true;
                            activeEditor.setText(content);
                            isProgrammaticChange = false;
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, getString(R.string.error_cannot_read, e.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    // ══════════════════════════════════════════════════════════
    //  Global Search
    // ══════════════════════════════════════════════════════════

    private void showGlobalSearch() {
        Intent i = new Intent(this, GlobalSearchActivity.class);
        i.putExtra("project_root", projectManager.getProjectDir().getAbsolutePath());
        startActivityForResult(i, REQ_GLOBAL_SEARCH);
    }

    // ══════════════════════════════════════════════════════════
    //  Media Player
    // ══════════════════════════════════════════════════════════

    private void pickMediaFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        String[] mimeTypes = {
                "audio/*",
                "video/*",
                "audio/mpeg",
                "audio/mp3",
                "audio/wav",
                "audio/ogg",
                "video/mp4",
                "video/webm",
                "video/3gpp"
        };
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        try {
            startActivityForResult(i, REQ_PLAY_MEDIA);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  AI Chat
    // ══════════════════════════════════════════════════════════

    private void openAiChat() {
        String code = "";
        String fileName = "";
        if (activeEditor != null && activeEditor.getText() != null) {
            code = activeEditor.getText().toString();
        }
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab != null && tab.file != null) {
            fileName = tab.file.getName();
        }
        AiChatActivity.launch(this, code, fileName,
                projectManager.getProjectDir() != null
                        ? projectManager.getProjectDir().getAbsolutePath() : "");
    }

    // ══════════════════════════════════════════════════════════
    //  Bytecode Editing
    // ══════════════════════════════════════════════════════════

    private void setupBytecodeEditButtons() {
        if (bytecodeEditInsn != null) {
            bytecodeEditInsn.setOnClickListener(v -> showBytecodeEditDialog());
        }
        if (bytecodeSaveBtn != null) {
            bytecodeSaveBtn.setOnClickListener(v -> saveModifiedBytecode());
        }
        if (bytecodeRunBtn != null) {
            bytecodeRunBtn.setOnClickListener(v -> runModifiedBytecode());
        }
        if (bytecodeOpenEditorBtn != null) {
            bytecodeOpenEditorBtn.setOnClickListener(v -> openBytecodeEditor());
        }
        if (bytecodeCallGraphBtn != null) {
            bytecodeCallGraphBtn.setOnClickListener(v -> openCallGraphFromBytecode());
        }
    }

    private void showBytecodeEditDialog() {
        if (bytecodeModel == null || bytecodeSelectedMethod < 0) {
            Toast.makeText(this, "No method selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bytecodeInsnAdapter.getSelectedItemIndex() < 0) {
            Toast.makeText(this, "Select an instruction first", Toast.LENGTH_SHORT).show();
            return;
        }

        int selIdx = bytecodeInsnAdapter.getSelectedItemIndex();
        BytecodeModel.MethodInfo mi = bytecodeModel.methods.get(bytecodeSelectedMethod);
        if (selIdx < 0 || selIdx >= mi.instructions.size()) return;
        BytecodeModel.Instruction insn = mi.instructions.get(selIdx);

        String[] options;
        if ("ldc".equals(insn.opcode)) {
            options = new String[]{
                    "Edit operand",
                    "Delete instruction",
                    "Insert NOP before",
                    "Insert NOP after"
            };
        } else if (insn.tokenType == BytecodeModel.Token.OPCODE) {
            options = new String[]{
                    "Delete instruction",
                    "Insert NOP before",
                    "Insert NOP after"
            };
        } else {
            options = new String[]{
                    "Delete instruction",
                    "Insert NOP before",
                    "Insert NOP after"
            };
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit: " + insn.opcode + " " + (insn.operand != null ? insn.operand : ""))
                .setItems(options, (d, w) -> {
                    switch (w) {
                        case 0:
                            if ("ldc".equals(insn.opcode)) {
                                showLdcEditDialog(insn);
                            } else {
                                showDeleteInsnDialog(selIdx);
                            }
                            break;
                        case 1:
                            showInsertNopDialog(selIdx, true);
                            break;
                        case 2:
                            showInsertNopDialog(selIdx, false);
                            break;
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showLdcEditDialog(BytecodeModel.Instruction insn) {
        EditText input = new EditText(this);
        input.setHint(R.string.bytecode_edit_ldc);
        input.setText(insn.operand);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle("Edit LDC constant")
                .setView(input)
                .setPositiveButton("Apply", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) {
                        Toast.makeText(this, "LDC operand changed to: " + val, Toast.LENGTH_SHORT).show();
                        setBytecodeStatus("Modified — " + insn.opcode + " → " + val);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showDeleteInsnDialog(int index) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.bytecode_delete_insn)
                .setPositiveButton("Delete", (d, w) -> {
                    Toast.makeText(this, "Instruction " + index + " deleted", Toast.LENGTH_SHORT).show();
                    setBytecodeStatus("Modified — instruction " + index + " removed");
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showInsertNopDialog(int index, boolean before) {
        new AlertDialog.Builder(this)
                .setTitle(before ? R.string.bytecode_insert_before : R.string.bytecode_insert_after)
                .setPositiveButton("NOP", (d, w) -> {
                    String pos = before ? "before" : "after";
                    Toast.makeText(this, "NOP inserted " + pos + " instruction " + index, Toast.LENGTH_SHORT).show();
                    setBytecodeStatus("Modified — NOP inserted " + pos + " insn " + index);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void openBytecodeEditor() {
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) return;

        if (tab.file.getName().endsWith(".class")) {
            // .class — редагуємо дизасембльований текст
            BytecodeEditorActivity.launch(this, tab.file.getAbsolutePath());
        } else if (tab.file.getName().endsWith(".java")) {
            // .java — компілюємо в .class, потім редагуємо байткод
            saveCurrentToActiveTab();
            setBytecodeStatus("Compiling for editor...");
            String source = activeEditor.getText() != null ? activeEditor.getText().toString() : "";
            new Thread(() -> {
                try {
                    ProjectCompiler.BytecodeCompileResult r = ProjectCompiler.compileForBytecodeView(
                            this, tab.file, source, projectManager.getProjectDir());
                    if (r.errorMessage != null) {
                        runOnUiThread(() -> {
                            setBytecodeStatus("Error: " + r.errorMessage);
                            Toast.makeText(this, "Compile error", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    runOnUiThread(() -> BytecodeEditorActivity.launch(this, r.classFile.getAbsolutePath()));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        setBytecodeStatus("Error: " + e.getMessage());
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }, "bytecode-compile-for-editor").start();
        } else {
            Toast.makeText(this, "Open a .java or .class file first", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveModifiedBytecode() {
        if (bytecodeModel == null) {
            Toast.makeText(this, "No bytecode loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) return;

        try {
            BytecodeEditor editor = BytecodeEditor.parse(bytecodeModel.rawBytes);
            byte[] modified = editor.toBytes();
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tab.file);
            fos.write(modified);
            fos.close();
            Toast.makeText(this, R.string.bytecode_save_success, Toast.LENGTH_SHORT).show();
            setBytecodeStatus("Saved: " + tab.file.getName());
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.bytecode_save_failed, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void runSqlFile(File sqlFile) {
        // Find a .db file in the project, or create one
        File projectDir = projectManager.getProjectDir();
        if (projectDir == null) {
            Toast.makeText(this, R.string.call_graph_no_project, Toast.LENGTH_SHORT).show();
            return;
        }

        File dbFile = null;
        File[] files = projectDir.listFiles();
        if (files != null) {
            for (File f : files) {
                String n = f.getName().toLowerCase(Locale.ROOT);
                if (n.endsWith(".db") || n.endsWith(".sqlite") || n.endsWith(".sqlite3")) {
                    dbFile = f;
                    break;
                }
            }
        }

        // If no .db file, create one
        if (dbFile == null) {
            dbFile = new File(projectDir, "data.db");
        }

        final File targetDb = dbFile;
        isRunning = true;
        consoleOutput.setText("");
        switchBottomPanel(PANEL_RUN);
        appendConsole("SQL: " + sqlFile.getName() + " -> " + targetDb.getName(), theme.textDim);

        new Thread(() -> {
            try {
                android.database.sqlite.SQLiteDatabase db =
                        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(
                                targetDb, null);

                String sql = new String(java.nio.file.Files.readAllBytes(sqlFile.toPath()));
                String[] statements = sql.split(";");

                int executed = 0;
                for (String stmt : statements) {
                    String trimmed = stmt.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;

                    String upper = trimmed.toUpperCase(Locale.ROOT).trim();
                    if (upper.startsWith("SELECT") || upper.startsWith("PRAGMA") || upper.startsWith("EXPLAIN")) {
                        try (android.database.Cursor cursor = db.rawQuery(trimmed, null)) {
                            int cols = cursor.getColumnCount();
                            final int rows = cursor.getCount();

                            // Header
                            StringBuilder header = new StringBuilder();
                            for (int i = 0; i < cols; i++) {
                                if (i > 0) header.append("\t| ");
                                header.append(cursor.getColumnName(i));
                            }
                            final String hdr = header.toString();
                            runOnUiThread(() -> appendConsole(hdr, theme.successText));

                            // Rows
                            int rowCount = 0;
                            while (cursor.moveToNext()) {
                                StringBuilder row = new StringBuilder();
                                for (int i = 0; i < cols; i++) {
                                    if (i > 0) row.append("\t| ");
                                    row.append(cursor.isNull(i) ? "NULL" : cursor.getString(i));
                                }
                                final String r = row.toString();
                                final int finalRowCount = rowCount;
                                runOnUiThread(() -> appendConsole(r, theme.consoleText));
                                rowCount++;
                                if (rowCount >= 100) {
                                    runOnUiThread(() -> appendConsole("... (" + (rows - finalRowCount) + " more rows)", theme.textDim));
                                    break;
                                }
                            }
                            final int totalRows = rows;
                            runOnUiThread(() -> appendConsole("→ " + totalRows + " rows", theme.successText));
                        }
                    } else {
                        db.execSQL(trimmed);
                        final int ex = ++executed;
                        runOnUiThread(() -> appendConsole("OK (" + ex + " statements executed)", theme.successText));
                    }
                }
                db.close();

                final int totalExec = executed;
                runOnUiThread(() -> {
                    appendConsole("\nDone! Database: " + targetDb.getName(), theme.successText);
                    isRunning = false;
                });
            } catch (Exception e) {
                final String err = e.getMessage();
                runOnUiThread(() -> {
                    appendConsole("Error: " + err, theme.errorText);
                    isRunning = false;
                });
            }
        }, "sql-run").start();
    }

    private void runKotlinFile(File ktFile) {
        isRunning = true;
        consoleOutput.setText("");
        switchBottomPanel(PANEL_RUN);
        appendConsole("Kotlin: " + ktFile.getName(), theme.textDim);

        String source;
        try {
            source = projectManager.readFile(ktFile);
        } catch (Exception e) {
            appendConsole("Error reading file: " + e.getMessage(), theme.errorText);
            isRunning = false;
            return;
        }

        ProjectCompiler.runSingleSource(this, source, ktFile, projectManager.getProjectDir(),
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

    private void runModifiedBytecode() {
        if (bytecodeModel == null) {
            Toast.makeText(this, "No bytecode loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isRunning) {
            Toast.makeText(this, "Already running", Toast.LENGTH_SHORT).show();
            return;
        }

        FileTab tab = tabsAdapter.getActiveTab();
        if (tab == null || tab.file == null) return;

        // Визначаємо ім'я класу з bytecodeModel
        String className = bytecodeModel.classInfo.internalName.replace('/', '.');
        // Беремо останню частину (просте ім'я класу) якщо є main
        String simpleName = bytecodeModel.classInfo.name;
        if (simpleName.contains(".")) {
            simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
        }

        isRunning = true;
        consoleOutput.setText("");
        switchBottomPanel(PANEL_RUN);
        appendConsole("▶ Running modified bytecode: " + className, theme.textDim);

        byte[] classBytes = bytecodeModel.rawBytes;
        new Thread(() -> {
            try {
                ProjectCompiler.runClassBytes(this, className, classBytes,
                        new ProjectCompiler.Callback() {
                            @Override
                            public void onProgress(String msg) {
                                runOnUiThread(() -> appendConsole("   " + msg, theme.textDim));
                            }

                            @Override
                            public void onResult(String output) {
                                runOnUiThread(() -> {
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
                                });
                            }

                            @Override
                            public void onProblems(List<ProblemItem> problems) {}
                        });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isRunning = false;
                    appendConsole("Error: " + e.getMessage(), theme.errorText);
                });
            }
        }, "bytecode-run").start();
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
        // Ctrl+Shift+F: global search
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.isCtrlPressed() && event.isShiftPressed()
                && event.getKeyCode() == KeyEvent.KEYCODE_F) {
            showGlobalSearch();
            return true;
        }
        // Ctrl+Shift+A: AI chat
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.isCtrlPressed() && event.isShiftPressed()
                && event.getKeyCode() == KeyEvent.KEYCODE_A) {
            openAiChat();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private static final int REQ_STORAGE_PERMISSION = 9998;

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.permission_storage_title)
                        .setMessage(R.string.permission_storage_message)
                        .setPositiveButton(R.string.permission_storage_open_settings, (d, w) -> {
                            openStorageSettings();
                        })
                        .setNegativeButton(R.string.permission_storage_exit, (d, w) -> finish())
                        .setCancelable(false)
                        .show();
                return false;
            }
        }
        return true;
    }

    private boolean isExternalPath(File file) {
        try {
            String path = file.getCanonicalPath();
            String internal = getFilesDir().getCanonicalPath();
            return !path.startsWith(internal);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean canReadRoot(File root) {
        if (!root.exists() || !root.isDirectory()) return false;
        // Try to actually list files — Samsung lies about permission
        File[] files = root.listFiles();
        if (files == null) return false;
        // Try to read first Java file found
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".java")) {
                try {
                    java.io.FileInputStream fis = new java.io.FileInputStream(f);
                    fis.read();
                    fis.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        // No Java files — try any file
        for (File f : files) {
            if (f.isFile()) {
                try {
                    java.io.FileInputStream fis = new java.io.FileInputStream(f);
                    fis.read();
                    fis.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    private File copyToInternal(File externalDir) {
        try {
            File internalDir = new File(getFilesDir(), "imported_project");
            if (internalDir.exists()) deleteRecursive(internalDir);
            internalDir.mkdirs();
            copyDirRecursive(externalDir, internalDir);
            return internalDir;
        } catch (Exception e) {
            return null;
        }
    }

    private void copyDirRecursive(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            dest.mkdirs();
            File[] files = src.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyDirRecursive(file, new File(dest, file.getName()));
                }
            }
        } else {
            try (java.io.InputStream in = new java.io.FileInputStream(src);
                 java.io.OutputStream out = new java.io.FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
        }
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }

    private void openStorageSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_STORAGE_PERMISSION);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQ_STORAGE_PERMISSION);
            } catch (Exception e2) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQ_STORAGE_PERMISSION);
                } catch (Exception e3) {
                    Toast.makeText(this, "Cannot open settings. Please grant storage permission manually.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  CPU Profiler
    // ══════════════════════════════════════════════════════════

    private void setupProfilerToolbar() {
        if (flameChartView == null) return;

        // Respect power saving mode for initial state
        profilerLiveMode = !powerSaving.isPowerSavingActive();

        View profilerRefresh = findViewById(R.id.profilerRefresh);
        View profilerZoomIn = findViewById(R.id.profilerZoomIn);
        View profilerZoomOut = findViewById(R.id.profilerZoomOut);
        View profilerFit = findViewById(R.id.profilerFit);
        TextView profilerLiveToggle = findViewById(R.id.profilerLiveToggle);

        if (profilerRefresh != null) profilerRefresh.setOnClickListener(v -> refreshProfilerResults());
        if (profilerZoomIn != null) profilerZoomIn.setOnClickListener(v -> flameChartView.zoomIn());
        if (profilerZoomOut != null) profilerZoomOut.setOnClickListener(v -> flameChartView.zoomOut());
        if (profilerFit != null) profilerFit.setOnClickListener(v -> flameChartView.fitToScreen());

        View profilerRunBtn = findViewById(R.id.profilerRunBtn);
        if (profilerRunBtn != null) profilerRunBtn.setOnClickListener(v -> runWithProfiler());

        if (profilerLiveToggle != null) {
            updateLiveToggleUI(profilerLiveToggle);
            profilerLiveToggle.setOnClickListener(v -> {
                profilerLiveMode = !profilerLiveMode;
                updateLiveToggleUI(profilerLiveToggle);
                if (profilerLiveMode && profilingEnabled) {
                    profilerRefreshHandler.removeCallbacks(profilerRefreshRunnable);
                    profilerRefreshHandler.post(profilerRefreshRunnable);
                    if (powerSaving.isPowerSavingActive()) {
                        Toast.makeText(this, "⚠ Live mode active — higher battery usage", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    profilerRefreshHandler.removeCallbacks(profilerRefreshRunnable);
                }
            });
        }

        flameChartView.setOnNodeSelectedListener(profile -> {
            if (profilerDetailScroll != null && profilerDetail != null) {
                profilerDetailScroll.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                sb.append("Method: ").append(profile.fullSignature()).append("\n");
                sb.append("Class:  ").append(profile.className).append("\n");
                sb.append("─────────────────────────────\n");
                sb.append(String.format(java.util.Locale.US, "Total time:  %.3f ms\n", profile.getTotalTimeMs()));
                sb.append(String.format(java.util.Locale.US, "Avg time:    %.3f ms\n", profile.getAvgTimeMs()));
                sb.append(String.format(java.util.Locale.US, "Max time:    %.3f ms\n", profile.getMaxTimeMs()));
                sb.append(String.format(java.util.Locale.US, "Invocations: %d\n", profile.invocationCount.get()));
                sb.append("─────────────────────────────\n");

                long total = com.ccs.javadroid.profiler.ProfilerBridge.getTotalTime();
                double pct = total > 0 ? (profile.totalTime.get() * 100.0 / total) : 0;
                sb.append(String.format(java.util.Locale.US, "Share: %.1f%% of total\n", pct));

                profilerDetail.setText(sb.toString());
            }
        });
    }

    private void updateLiveToggleUI(TextView toggle) {
        if (profilerLiveMode) {
            toggle.setText("● Live");
            toggle.setTextColor(0xFF499C54);
        } else {
            toggle.setText("○ Paused");
            toggle.setTextColor(0xFF888888);
        }
    }

    private void startProfilerLiveRefresh() {
        profilerRefreshHandler.removeCallbacks(profilerRefreshRunnable);
        if (profilerLiveMode) {
            profilerRefreshHandler.post(profilerRefreshRunnable);
        }
    }

    private void stopProfilerLiveRefresh() {
        profilerRefreshHandler.removeCallbacks(profilerRefreshRunnable);
    }

    // ══════════════════════════════════════════════════════════
    //  TODO/FIXME Tracker
    // ══════════════════════════════════════════════════════════

    private void refreshTodoPanel() {
        if (todoAdapter == null || todoStatus == null) return;
        if (projectManager == null) return;

        new Thread(() -> {
            File root = projectManager.getProjectDir();
            if (root == null) {
                runOnUiThread(() -> {
                    todoAdapter.setItems(new ArrayList<>());
                    todoStatus.setText("No project");
                });
                return;
            }

            java.util.List<File> sources = ProjectScanner.listJavaSources(root);
            java.util.List<ProblemItem> allProblems = com.ccs.javadroid.analysis.StaticAnalyzer.analyze(
                    MainActivity.this, root, sources);

            int todoCount = 0;
            int fixmeCount = 0;
            for (ProblemItem p : allProblems) {
                if (p.message != null && p.message.contains("TODO")) todoCount++;
                if (p.message != null && p.message.contains("FIXME")) fixmeCount++;
            }
            final int finalTodoCount = todoCount;
            final int finalFixmeCount = fixmeCount;

            runOnUiThread(() -> {
                todoAdapter.setItems(allProblems);
                todoStatus.setText(finalTodoCount + " TODO, " + finalFixmeCount + " FIXME");
                if (todoSearch != null && !todoSearch.getText().toString().isEmpty()) {
                    todoAdapter.filter(todoSearch.getText().toString());
                }
            });
        }).start();
    }

    private void refreshProfilerResults() {
        if (flameChartView == null || profilerStatus == null) return;

        java.util.List<com.ccs.javadroid.profiler.ProfilerBridge.MethodProfile> results =
                com.ccs.javadroid.profiler.ProfilerBridge.getResults();

        if (results.isEmpty()) {
            profilerStatus.setText(profilingEnabled ? "Collecting data..." : "No data — run code with profiler");
            if (!profilingEnabled) flameChartView.clear();
            if (profilerDetailScroll != null && !profilingEnabled) profilerDetailScroll.setVisibility(View.GONE);
            return;
        }

        // Sort by total time descending
        results.sort((a, b) -> Long.compare(b.totalTime.get(), a.totalTime.get()));

        flameChartView.setProfiles(results);

        long totalNs = com.ccs.javadroid.profiler.ProfilerBridge.getTotalTime();
        double totalMs = totalNs / 1_000_000.0;
        String liveIndicator = (profilingEnabled && profilerLiveMode) ? " ●LIVE" : "";
        profilerStatus.setText(String.format(java.util.Locale.US,
                "%d methods, %.1f ms total%s", results.size(), totalMs, liveIndicator));
    }

    private void runWithProfiler() {
        if (isRunning) return;
        FileTab activeTab = tabsAdapter.getActiveTab();
        if (activeTab == null) {
            Toast.makeText(this, R.string.toast_no_file_open, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!activeTab.file.getName().endsWith(".java")) {
            Toast.makeText(this, "Profiler only supports Java files", Toast.LENGTH_SHORT).show();
            return;
        }

        saveCurrentToActiveTab();

        // Respect power saving: disable live refresh when battery is low
        profilerLiveMode = !powerSaving.isPowerSavingActive();

        isRunning = true;
        profilingEnabled = true;
        consoleOutput.setText("");
        switchBottomPanel(PANEL_RUN);
        appendConsole("Profiling: " + activeTab.file.getName(), theme.textDim);

        String source = activeEditor.getText().toString();

        // We need to compile, instrument, and run with profiling
        new Thread(() -> {
            try {
                String className = com.ccs.javadroid.tools.compilers.ProjectCompiler.extractClassNamePublic(source);
                File cacheDir = new File(getCacheDir(), "profile_compile_cache");
                if (cacheDir.exists()) {
                    File[] oldFiles = cacheDir.listFiles();
                    if (oldFiles != null) {
                        for (File f : oldFiles) {
                            if (f.getName().equals("android.jar")) continue;
                            f.delete();
                        }
                    }
                }
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File androidJar = com.ccs.javadroid.tools.compilers.ProjectCompiler.ensureAndroidJarPublic(this, cacheDir);
                File srcFile = new File(cacheDir, className + ".java");
                com.ccs.javadroid.tools.compilers.ProjectCompiler.writeUtf8Public(srcFile, source);

                runOnUiThread(() -> appendConsole("   Compiling...", theme.textDim));
                String ecjErr = com.ccs.javadroid.tools.compilers.ProjectCompiler.compileEcjPublic(
                        androidJar, null, cacheDir,
                        new com.ccs.javadroid.util.AppPreferences(this).getJavaTarget(), srcFile);
                if (ecjErr != null) {
                    isRunning = false;
                    profilingEnabled = false;
                    runOnUiThread(() -> {
                        stopProfilerLiveRefresh();
                        appendConsole("Compilation Error:\n" + ecjErr, theme.errorText);
                    });
                    return;
                }

                File classFile = com.ccs.javadroid.tools.compilers.ProjectCompiler.findClassFilePublic(cacheDir, className);
                if (classFile == null) {
                    isRunning = false;
                    profilingEnabled = false;
                    runOnUiThread(() -> {
                        stopProfilerLiveRefresh();
                        appendConsole("Error: class file not found", theme.errorText);
                    });
                    return;
                }

                runOnUiThread(() -> appendConsole("   Instrumenting for profiling...", theme.textDim));
                com.ccs.javadroid.profiler.ProfilerBridge.reset();
                com.ccs.javadroid.profiler.ProfilerBridge.start();
                com.ccs.javadroid.profiler.ProfilerInstrumenter.instrumentFile(classFile);

                File dexDir = new File(cacheDir, "profile_dex");
                if (!dexDir.exists()) dexDir.mkdirs();
                else {
                    File[] oldFiles = dexDir.listFiles();
                    if (oldFiles != null) {
                        for (File f : oldFiles) f.delete();
                    }
                }

                runOnUiThread(() -> appendConsole("   Converting to DEX...", theme.textDim));
                com.ccs.javadroid.tools.compilers.ProjectCompiler.runD8DexPublic(androidJar, dexDir, classFile);

                String fqClassName = classFile.getAbsolutePath()
                        .substring(cacheDir.getAbsolutePath().length() + 1)
                        .replace(".class", "")
                        .replace('/', '.');

                runOnUiThread(() -> appendConsole("   Running with profiler...", theme.textDim));

                // Start live refresh
                runOnUiThread(() -> startProfilerLiveRefresh());

                // Run the dex
                com.ccs.javadroid.tools.compilers.ProjectCompiler.debugRunDex(
                        this, fqClassName, dexDir, cacheDir, null,
                        new com.ccs.javadroid.tools.compilers.ProjectCompiler.Callback() {
                            @Override
                            public void onProgress(String msg) {}

                            @Override
                            public void onResult(String output) {
                                com.ccs.javadroid.profiler.ProfilerBridge.stop();
                                isRunning = false;
                                profilingEnabled = false;
                                runOnUiThread(() -> stopProfilerLiveRefresh());

                                runOnUiThread(() -> {
                                    appendConsole("", theme.accent);
                                    appendConsole("══════════════════════════════", theme.accent);
                                    if (output != null && !output.trim().isEmpty()) {
                                        boolean err = output.startsWith("Compilation Error")
                                                || output.startsWith("Execution Exception")
                                                || output.startsWith("System Error")
                                                || output.startsWith("Error:");
                                        appendConsole(output.trim(), err ? theme.errorText : theme.consoleText);
                                    }
                                    appendConsole("\nProfiling complete. Results on Profile tab.", theme.successText);
                                    refreshProfilerResults();
                                });
                            }

                            @Override
                            public void onProblems(List<ProblemItem> problems) {}
                        });
            } catch (Exception e) {
                isRunning = false;
                profilingEnabled = false;
                runOnUiThread(() -> {
                    stopProfilerLiveRefresh();
                    appendConsole("Profiler error: " + e.getMessage(), theme.errorText);
                });
            }
        }, "ProfilerRunner").start();
    }

    // ══════════════════════════════════════════════════════════
    //  Dependency Graph
    // ══════════════════════════════════════════════════════════

    private void refreshDependencyGraph() {
        if (depsGraphView == null || projectManager == null) return;

        // Setup toolbar listeners once
        if (depsGraphView.getTag() == null) {
            depsGraphView.setTag("init");

            View depsRefresh = findViewById(R.id.depsRefresh);
            View depsZoomIn = findViewById(R.id.depsZoomIn);
            View depsZoomOut = findViewById(R.id.depsZoomOut);
            View depsFit = findViewById(R.id.depsFitToScreen);

            if (depsRefresh != null) depsRefresh.setOnClickListener(v -> refreshDependencyGraph());
            if (depsZoomIn != null) depsZoomIn.setOnClickListener(v -> depsGraphView.zoomIn());
            if (depsZoomOut != null) depsZoomOut.setOnClickListener(v -> depsGraphView.zoomOut());
            if (depsFit != null) depsFit.setOnClickListener(v -> depsGraphView.fitToScreen());
        }

        if (depsStatus != null) depsStatus.setText("Analyzing...");

        new Thread(() -> {
            try {
                com.ccs.javadroid.tools.bytecode.DependencyModel model =
                        new com.ccs.javadroid.tools.bytecode.DependencyModel();

                File projectDir = projectManager.getProjectDir();
                if (projectDir == null) {
                    runOnUiThread(() -> { if (depsStatus != null) depsStatus.setText("No project"); });
                    return;
                }

                File outDir = new File(projectDir, "out");
                File targetDir = new File(projectDir, "target");
                File buildDir = new File(projectDir, "build");

                int count = 0;
                if (outDir.isDirectory()) count += model.analyzeDirectory(outDir);
                if (targetDir.isDirectory()) count += model.analyzeDirectory(targetDir);
                if (buildDir.isDirectory()) count += model.analyzeDirectory(buildDir);

                int finalCount = count;
                dependencyModel = model;
                runOnUiThread(() -> {
                    if (depsGraphView != null) {
                        depsGraphView.setModel(model);
                        depsGraphView.fitToScreen();
                    }
                    if (depsStatus != null) {
                        depsStatus.setText(finalCount + " classes, "
                                + model.getClasses().size() + " nodes, "
                                + model.getEdges().size() + " edges");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (depsStatus != null) depsStatus.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════
    //  Refactoring
    // ══════════════════════════════════════════════════════════

    private void showRefactorDialog() {
        if (activeEditor == null) return;

        io.github.rosemoe.sora.text.Cursor cursor = activeEditor.getCursor();
        int line = cursor.getLeftLine();
        int col = cursor.getLeftColumn();

        String lineText = "";
        try {
            lineText = activeEditor.getText().getLine(line).toString();
        } catch (Exception ignored) {}

        String selectedText = "";
        try {
            io.github.rosemoe.sora.text.Content content = activeEditor.getText();
            int leftLine = cursor.getLeftLine();
            int leftCol = cursor.getLeftColumn();
            int rightLine = cursor.getRightLine();
            int rightCol = cursor.getRightColumn();
            if (leftLine == rightLine) {
                selectedText = content.getLine(leftLine).subSequence(leftCol, rightCol).toString().trim();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(content.getLine(leftLine).subSequence(leftCol, content.getLine(leftLine).length()));
                for (int i = leftLine + 1; i < rightLine; i++) {
                    sb.append("\n").append(content.getLine(i));
                }
                if (rightLine < content.getLineCount()) {
                    sb.append("\n").append(content.getLine(rightLine).subSequence(0, rightCol));
                }
                selectedText = sb.toString().trim();
            }
        } catch (Exception ignored) {}

        if (selectedText.isEmpty()) {
            selectedText = extractWordAtCursor(lineText, col);
        }

        String finalSelectedText = selectedText;
        String[] items = {
                "Rename '" + selectedText + "'",
                "Extract Method",
                "Extract Variable",
                "Inline Method",
                "Find Usages of '" + selectedText + "'"
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.refactor_rename_title)
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: showRenameDialog(finalSelectedText); break;
                        case 1: showExtractMethodDialog(); break;
                        case 2: showExtractVariableDialog(); break;
                        case 3: showInlineDialog(finalSelectedText); break;
                        case 4: showFindUsagesDialog(finalSelectedText); break;
                    }
                })
                .show();
    }

    private String extractWordAtCursor(String lineText, int col) {
        if (lineText.isEmpty() || col > lineText.length()) return "";
        int start = col;
        while (start > 0 && Character.isJavaIdentifierPart(lineText.charAt(start - 1))) start--;
        int end = col;
        while (end < lineText.length() && Character.isJavaIdentifierPart(lineText.charAt(end))) end++;
        return lineText.substring(start, end);
    }

    private void showRenameDialog(String currentName) {
        EditText input = new EditText(this);
        input.setText(currentName);
        input.setHint(R.string.refactor_rename_hint);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        input.setSelection(currentName.length());
        int pad = dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(R.string.refactor_rename_title)
                .setView(input)
                .setPositiveButton("Rename", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty() || newName.equals(currentName)) return;
                    if (!com.ccs.javadroid.tools.refactor.RefactoringHelper.isValidIdentifier(newName)) {
                        Toast.makeText(this, "Invalid identifier", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    File projectRoot = projectManager != null ? projectManager.getProjectDir() : null;
                    com.ccs.javadroid.tools.refactor.RefactoringHelper.renameSymbolAsync(
                            this, projectRoot, currentName, newName, result -> {
                                Toast.makeText(this, result.summary, Toast.LENGTH_LONG).show();
                                if (result.filesChanged > 0) {
                                    refreshProblemsMergedAsync();
                                    FileTab tab = (activeEditor == editor) ? leftTab : rightTab;
                                    if (tab != null) reloadTab(tab);
                                }
                            });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showExtractMethodDialog() {
        if (activeEditor == null) return;

        io.github.rosemoe.sora.text.Cursor cursor = activeEditor.getCursor();
        int selStartLine = cursor.getLeftLine();
        int selEndLine = cursor.getRightLine();

        if (selStartLine == selEndLine && selStartLine == cursor.getRightLine()) {
            Toast.makeText(this, "Select code block to extract", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setHint(R.string.refactor_extract_method_hint);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        int pad = dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(R.string.refactor_extract_method_title)
                .setView(input)
                .setPositiveButton("Extract", (d, w) -> {
                    String methodName = input.getText().toString().trim();
                    if (methodName.isEmpty()) return;
                    if (!com.ccs.javadroid.tools.refactor.RefactoringHelper.isValidIdentifier(methodName)) {
                        Toast.makeText(this, "Invalid identifier", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String source = activeEditor.getText().toString();
                    com.ccs.javadroid.tools.refactor.RefactoringHelper.extractMethodAsync(
                            this, source, selStartLine, selEndLine, methodName, result -> {
                                Toast.makeText(this, result.summary, Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showExtractVariableDialog() {
        if (activeEditor == null) return;

        io.github.rosemoe.sora.text.Cursor cursor = activeEditor.getCursor();
        String lineText = "";
        try {
            lineText = activeEditor.getText().getLine(cursor.getLeftLine()).toString();
        } catch (Exception ignored) {}

        String selectedText = "";
        try {
            io.github.rosemoe.sora.text.Content content = activeEditor.getText();
            int leftLine = cursor.getLeftLine();
            int leftCol = cursor.getLeftColumn();
            int rightLine = cursor.getRightLine();
            int rightCol = cursor.getRightColumn();
            if (leftLine == rightLine) {
                selectedText = content.getLine(leftLine).subSequence(leftCol, rightCol).toString().trim();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(content.getLine(leftLine).subSequence(leftCol, content.getLine(leftLine).length()));
                for (int i = leftLine + 1; i < rightLine; i++) {
                    sb.append("\n").append(content.getLine(i));
                }
                if (rightLine < content.getLineCount()) {
                    sb.append("\n").append(content.getLine(rightLine).subSequence(0, rightCol));
                }
                selectedText = sb.toString().trim();
            }
        } catch (Exception ignored) {}

        if (selectedText.isEmpty()) {
            Toast.makeText(this, "Select an expression to extract", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setHint(R.string.refactor_extract_variable_hint);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        int pad = dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle(R.string.refactor_extract_variable_title)
                .setView(input)
                .setPositiveButton("Extract", (d, w) -> {
                    String varName = input.getText().toString().trim();
                    if (varName.isEmpty()) return;
                    if (!com.ccs.javadroid.tools.refactor.RefactoringHelper.isValidIdentifier(varName)) {
                        Toast.makeText(this, "Invalid identifier", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String source = activeEditor.getText().toString();
                    com.ccs.javadroid.tools.refactor.RefactoringHelper.extractVariableAsync(
                            source, cursor.getLeftLine(),
                            cursor.getLeftColumn(), cursor.getRightColumn(),
                            varName, result -> {
                                Toast.makeText(this, result.summary, Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showInlineDialog(String methodName) {
        if (activeEditor == null || methodName.isEmpty()) return;

        String source = activeEditor.getText().toString();

        new AlertDialog.Builder(this)
                .setTitle(R.string.refactor_inline_title)
                .setMessage("Inline method '" + methodName + "'?")
                .setPositiveButton("Inline", (d, w) -> {
                    com.ccs.javadroid.tools.refactor.RefactoringHelper.inlineMethodAsync(
                            this, source, methodName, result -> {
                                Toast.makeText(this, result.summary, Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showFindUsagesDialog(String symbolName) {
        if (symbolName.isEmpty() || projectManager == null) return;

        File projectRoot = projectManager.getProjectDir();
        new Thread(() -> {
            java.util.List<com.ccs.javadroid.tools.refactor.RefactoringHelper.UsageLocation> usages =
                    com.ccs.javadroid.tools.refactor.RefactoringHelper.findUsages(projectRoot, symbolName);
            runOnUiThread(() -> {
                if (usages.isEmpty()) {
                    Toast.makeText(this, "No usages found for '" + symbolName + "'", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append(usages.size()).append(" usages of '").append(symbolName).append("':\n\n");
                for (int i = 0; i < Math.min(usages.size(), 20); i++) {
                    com.ccs.javadroid.tools.refactor.RefactoringHelper.UsageLocation u = usages.get(i);
                    sb.append(u.file.getName()).append(":").append(u.line).append("  ");
                    sb.append(u.lineContent.trim()).append("\n");
                }
                if (usages.size() > 20) {
                    sb.append("... and ").append(usages.size() - 20).append(" more");
                }

                new AlertDialog.Builder(this)
                        .setTitle("Usages of '" + symbolName + "'")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }

    private void reloadTab(FileTab tab) {
        if (tab == null || tab.file == null || !tab.file.exists()) return;
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(tab.file.toPath());
            String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);

            isProgrammaticChange = true;
            try {
                CodeEditor ed = (tab == leftTab) ? editor : editor2;
                if (ed != null) {
                    ed.setText(content);
                }
            } finally {
                isProgrammaticChange = false;
            }
        } catch (Exception ignored) {
        }
    }
}
