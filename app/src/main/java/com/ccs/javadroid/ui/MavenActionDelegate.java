package com.ccs.javadroid.ui;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ccs.javadroid.R;
import com.ccs.javadroid.analysis.ProblemItem;
import com.ccs.javadroid.analysis.ProblemsAdapter;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.tools.compilers.ProjectCompiler;
import com.ccs.javadroid.maven.MavenDependencyResolver;
import com.ccs.javadroid.maven.MavenPaths;
import com.ccs.javadroid.maven.PomModel;
import com.ccs.javadroid.maven.PomParser;
import com.ccs.javadroid.util.AppTheme;

import java.util.List;

public final class MavenActionDelegate {

    public interface Callback {
        void runOnUiThread(@NonNull Runnable r);
        ProjectManager getProjectManager();
        AppTheme getTheme();
        void appendConsole(String text, int color);
        void switchBottomPanel(int panel);
        void setConsoleText(String text);
        void setProblemsItems(List<ProblemItem> items);
        void saveCurrentToActiveTab();
        Activity getActivity();
    }

    private static final int PANEL_RUN = 0;
    private static final int PANEL_PROBLEMS = 1;

    private final Callback callback;
    private volatile boolean syncInProgress = false;

    public MavenActionDelegate(Callback callback) {
        this.callback = callback;
    }

    public boolean isSyncInProgress() { return syncInProgress; }

    public void syncDependencies() {
        ProjectManager pm = callback.getProjectManager();
        if (pm == null || !pm.isMavenProject()) {
            Toast.makeText(callback.getActivity(), R.string.toast_not_maven, Toast.LENGTH_SHORT).show();
            return;
        }
        if (syncInProgress) return;
        syncInProgress = true;
        callback.switchBottomPanel(PANEL_RUN);
        callback.setConsoleText("");
        callback.appendConsole(callback.getActivity().getString(R.string.console_sync_maven), callback.getTheme().textDim);
        new Thread(() -> {
            try {
                PomModel pom = PomParser.parse(MavenPaths.pomFile(pm.getProjectDir()));
                MavenDependencyResolver.resolve(pm.getProjectDir(), pom, line ->
                        callback.runOnUiThread(() -> callback.appendConsole("   " + line, callback.getTheme().textDim)));
                callback.runOnUiThread(() -> {
                    syncInProgress = false;
                    callback.appendConsole(callback.getActivity().getString(R.string.console_sync_done), callback.getTheme().successText);
                    Toast.makeText(callback.getActivity(), R.string.toast_sync_done, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                callback.runOnUiThread(() -> {
                    syncInProgress = false;
                    Toast.makeText(callback.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }, "maven-sync").start();
    }

    public void mavenPackage() { runMavenCommand("package", R.string.console_mvn_package, true); }
    public void mavenTestCompile() { runMavenCommand("test-compile", R.string.console_mvn_test_compile, true); }
    public void mavenTestRun() { runMavenCommand("test", R.string.console_mvn_test_run, true); }
    public void mavenClean() { runMavenCommand("clean", R.string.console_mvn_clean, false); }
    public void mavenInstall() { runMavenCommand("install", R.string.console_mvn_install, true); }

    private void runMavenCommand(String cmd, int labelRes, boolean needsPom) {
        ProjectManager pm = callback.getProjectManager();
        if (pm == null || !pm.isMavenProject()) {
            Toast.makeText(callback.getActivity(), R.string.toast_pom_required, Toast.LENGTH_SHORT).show();
            return;
        }
        callback.saveCurrentToActiveTab();
        callback.switchBottomPanel(PANEL_RUN);
        callback.setConsoleText("");
        callback.appendConsole(callback.getActivity().getString(labelRes), callback.getTheme().textDim);

        try {
            PomModel pom = needsPom ? PomParser.parse(MavenPaths.pomFile(pm.getProjectDir())) : null;
            ProjectCompiler.Callback cc = new ProjectCompiler.Callback() {
                @Override public void onProgress(String msg) {
                    callback.appendConsole("   " + msg, callback.getTheme().textDim);
                }
                @Override public void onResult(String output) {
                    callback.appendConsole(output, callback.getTheme().consoleText);
                }
                @Override public void onProblems(List<ProblemItem> problems) {
                    if (problems != null && !problems.isEmpty()) {
                        callback.setProblemsItems(problems);
                        callback.switchBottomPanel(PANEL_PROBLEMS);
                    }
                }
            };
            switch (cmd) {
                case "package": ProjectCompiler.mavenPackage(callback.getActivity(), pm.getProjectDir(), pom, cc); break;
                case "test-compile": ProjectCompiler.mavenTestCompile(callback.getActivity(), pm.getProjectDir(), pom, cc); break;
                case "test": ProjectCompiler.mavenTestRun(callback.getActivity(), pm.getProjectDir(), pom, cc); break;
                case "clean": ProjectCompiler.mavenClean(callback.getActivity(), pm.getProjectDir(), cc); break;
                case "install": ProjectCompiler.mavenInstall(callback.getActivity(), pm.getProjectDir(), pom, cc); break;
            }
        } catch (Exception e) {
            Toast.makeText(callback.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
