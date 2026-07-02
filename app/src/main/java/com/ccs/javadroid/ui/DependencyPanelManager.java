package com.ccs.javadroid.ui;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ccs.javadroid.R;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.tools.bytecode.DependencyModel;
import com.ccs.javadroid.util.AppTheme;

import java.io.File;

public final class DependencyPanelManager {

    public interface Callback {
        void runOnUiThread(@NonNull Runnable r);
        ProjectManager getProjectManager();
        AppTheme getTheme();
    }

    private final Activity activity;
    private final Callback callback;

    private View panel;
    private TextView tab;
    private DependencyGraphView graphView;
    private TextView status;
    private DependencyModel model;
    private boolean toolbarInitialized = false;

    public DependencyPanelManager(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void bind() {
        panel     = activity.findViewById(R.id.depsPanel);
        tab       = activity.findViewById(R.id.tabDeps);
        graphView = activity.findViewById(R.id.depsGraphView);
        status    = activity.findViewById(R.id.depsStatus);
    }

    public void applyTheme(@NonNull AppTheme theme) {
        if (panel != null) panel.setBackgroundColor(theme.consoleBg);
        if (status != null) status.setTextColor(theme.textDim);
        View depsToolbar = activity.findViewById(R.id.depsToolbar);
        if (depsToolbar != null) depsToolbar.setBackgroundColor(theme.toolbar);
        View depsRefresh = activity.findViewById(R.id.depsRefresh);
        if (depsRefresh != null) ((TextView) depsRefresh).setTextColor(theme.accent);
        View depsZoomIn = activity.findViewById(R.id.depsZoomIn);
        if (depsZoomIn != null) ((TextView) depsZoomIn).setTextColor(theme.text);
        View depsZoomOut = activity.findViewById(R.id.depsZoomOut);
        if (depsZoomOut != null) ((TextView) depsZoomOut).setTextColor(theme.text);
        View depsFit = activity.findViewById(R.id.depsFitToScreen);
        if (depsFit != null) ((TextView) depsFit).setTextColor(theme.text);
        if (graphView != null) {
            graphView.setColors(theme.accent, 0xFFFFA500, 0xFF4CAF50, 0xFF666666, theme.text, theme.consoleBg);
        }
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

    public void refresh() {
        if (graphView == null || callback.getProjectManager() == null) return;

        if (!toolbarInitialized) {
            toolbarInitialized = true;

            View depsRefresh = activity.findViewById(R.id.depsRefresh);
            View depsZoomIn = activity.findViewById(R.id.depsZoomIn);
            View depsZoomOut = activity.findViewById(R.id.depsZoomOut);
            View depsFit = activity.findViewById(R.id.depsFitToScreen);

            if (depsRefresh != null) depsRefresh.setOnClickListener(v -> refresh());
            if (depsZoomIn != null) depsZoomIn.setOnClickListener(v -> graphView.zoomIn());
            if (depsZoomOut != null) depsZoomOut.setOnClickListener(v -> graphView.zoomOut());
            if (depsFit != null) depsFit.setOnClickListener(v -> graphView.fitToScreen());
        }

        if (status != null) status.setText(R.string.label_analyzing);

        new Thread(() -> {
            try {
                DependencyModel m = new DependencyModel();
                File projectDir = callback.getProjectManager().getProjectDir();
                if (projectDir == null) {
                    callback.runOnUiThread(() -> { if (status != null) status.setText(R.string.toast_no_project); });
                    return;
                }

                File outDir = new File(projectDir, "out");
                File targetDir = new File(projectDir, "target");
                File buildDir = new File(projectDir, "build");

                int count = 0;
                if (outDir.isDirectory()) count += m.analyzeDirectory(outDir);
                if (targetDir.isDirectory()) count += m.analyzeDirectory(targetDir);
                if (buildDir.isDirectory()) count += m.analyzeDirectory(buildDir);

                int finalCount = count;
                model = m;
                callback.runOnUiThread(() -> {
                    if (graphView != null) {
                        graphView.setModel(m);
                        graphView.fitToScreen();
                    }
                    if (status != null) {
                        status.setText(finalCount + " classes, "
                                + m.getClasses().size() + " nodes, "
                                + m.getEdges().size() + " edges");
                    }
                });
            } catch (Exception e) {
                callback.runOnUiThread(() -> {
                    if (status != null) status.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    public View getPanel() { return panel; }
    public TextView getTab() { return tab; }
    public DependencyModel getModel() { return model; }
}
