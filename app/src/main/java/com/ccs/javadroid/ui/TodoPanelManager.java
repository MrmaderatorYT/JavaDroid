package com.ccs.javadroid.ui;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccs.javadroid.R;
import com.ccs.javadroid.analysis.ProblemItem;
import com.ccs.javadroid.analysis.TodoAdapter;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.project.ProjectScanner;
import com.ccs.javadroid.util.AppTheme;

import java.io.File;
import java.util.ArrayList;

public final class TodoPanelManager {

    public interface Callback {
        void onTodoItemClicked(File file, int line);
        void runOnUiThread(@NonNull Runnable r);
        ProjectManager getProjectManager();
        AppTheme getTheme();
    }

    private final Activity activity;
    private final Callback callback;
    private final TodoAdapter adapter;

    private View panel;
    private TextView tab;
    private RecyclerView recycler;
    private EditText search;
    private TextView status;

    private boolean autoRefreshPending = false;
    private final Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = () -> {
        if (autoRefreshPending) {
            autoRefreshPending = false;
            refresh();
        }
    };

    public TodoPanelManager(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.adapter = new TodoAdapter();
    }

    public void bind() {
        panel   = activity.findViewById(R.id.todoPanel);
        tab     = activity.findViewById(R.id.tabTodo);
        recycler = activity.findViewById(R.id.todoRecycler);
        search  = activity.findViewById(R.id.todoSearch);
        status  = activity.findViewById(R.id.todoStatus);

        adapter.setTheme(callback.getTheme());
        adapter.setListener(item -> {
            if (item.file != null && item.file.exists()) {
                callback.onTodoItemClicked(item.file, item.line);
            }
        });

        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(activity));
            recycler.setAdapter(adapter);
        }
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) {
                    adapter.filter(s != null ? s.toString() : "");
                }
            });
        }
        View refreshBtn = activity.findViewById(R.id.todoRefresh);
        if (refreshBtn != null) refreshBtn.setOnClickListener(v -> refresh());
    }

    public void applyTheme(@NonNull AppTheme theme) {
        if (panel != null) panel.setBackgroundColor(theme.consoleBg);
        if (recycler != null) recycler.setBackgroundColor(theme.consoleBg);
        if (search != null) {
            search.setTextColor(theme.consoleText);
            search.setHintTextColor(theme.textDim);
        }
        if (status != null) status.setTextColor(theme.textDim);
        adapter.setTheme(theme);
    }

    public void updateTabStyle(boolean active, @NonNull AppTheme theme, int activeBg) {
        if (tab != null) {
            tab.setBackgroundColor(active ? activeBg : theme.toolbar);
            tab.setTextColor(active ? theme.accent : theme.textDim);
        }
    }

    public void setVisibility(boolean visible) {
        if (panel != null) panel.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void scheduleAutoRefresh() {
        autoRefreshPending = true;
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 1500);
    }

    public void refresh() {
        if (adapter == null || status == null) return;
        ProjectManager pm = callback.getProjectManager();
        if (pm == null) return;

        new Thread(() -> {
            File root = pm.getProjectDir();
            if (root == null) {
                callback.runOnUiThread(() -> {
                    adapter.setItems(new ArrayList<>());
                    status.setText(R.string.toast_no_project);
                });
                return;
            }

            java.util.List<File> sources = ProjectScanner.listJavaSources(root);
            java.util.List<ProblemItem> allProblems =
                    com.ccs.javadroid.analysis.StaticAnalyzer.analyze(activity, root, sources);

            int todoCount = 0;
            int fixmeCount = 0;
            for (ProblemItem p : allProblems) {
                if (p.message != null && p.message.contains("TODO")) todoCount++;
                if (p.message != null && p.message.contains("FIXME")) fixmeCount++;
            }
            final int finalTodoCount = todoCount;
            final int finalFixmeCount = fixmeCount;

            callback.runOnUiThread(() -> {
                adapter.setItems(allProblems);
                status.setText(finalTodoCount + " TODO, " + finalFixmeCount + " FIXME");
                if (search != null && !search.getText().toString().isEmpty()) {
                    adapter.filter(search.getText().toString());
                }
            });
        }).start();
    }

    public View getPanel() { return panel; }
    public TextView getTab() { return tab; }
}
