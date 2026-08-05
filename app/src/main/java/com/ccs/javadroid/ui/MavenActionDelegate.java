package com.ccs.javadroid.ui;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ccs.javadroid.R;
import com.ccs.javadroid.analysis.ProblemItem;
import com.ccs.javadroid.analysis.ProblemsAdapter;
import com.ccs.javadroid.project.BuildSystem;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.ui.panels.BottomPanel;
import com.ccs.javadroid.tools.compilers.ProjectCompiler;
import com.ccs.javadroid.maven.MavenDependencyResolver;
import com.ccs.javadroid.maven.PomModel;
import com.ccs.javadroid.util.AppTheme;

import java.util.List;

/**
 * Drives the build lifecycle for both Maven and Gradle projects — the Gradle
 * build script is read into the same {@link PomModel} the Maven pipeline uses.
 */
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

    // Panel ids come from BottomPanel; they used to be copied here as 0 and 1.
    private static final int PANEL_RUN = BottomPanel.RUN.mode;
    private static final int PANEL_PROBLEMS = BottomPanel.PROBLEMS.mode;

    private final Callback callback;
    private volatile boolean syncInProgress = false;

    public MavenActionDelegate(Callback callback) {
        this.callback = callback;
    }

    public boolean isSyncInProgress() { return syncInProgress; }

    public void syncDependencies() {
        ProjectManager pm = callback.getProjectManager();
        if (pm == null || !BuildSystem.isBuildable(pm.getProjectDir())) {
            Toast.makeText(callback.getActivity(), R.string.toast_no_build_script, Toast.LENGTH_SHORT).show();
            return;
        }
        if (syncInProgress) return;
        syncInProgress = true;
        callback.switchBottomPanel(PANEL_RUN);
        callback.setConsoleText("");
        BuildSystem.Kind kind = BuildSystem.detect(pm.getProjectDir());
        callback.appendConsole(callback.getActivity().getString(
                R.string.console_sync_build, BuildSystem.displayName(kind)), callback.getTheme().textDim);
        new Thread(() -> {
            try {
                BuildSystem.Model model = BuildSystem.model(pm.getProjectDir());
                reportWarnings(model);
                MavenDependencyResolver.resolve(pm.getProjectDir(), model.pom, line ->
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

    public void mavenPackage() { runMavenCommand("package", "build", true); }
    public void mavenTestCompile() { runMavenCommand("test-compile", "testClasses", true); }
    public void mavenTestRun() { runMavenCommand("test", "test", true); }
    public void mavenClean() { runMavenCommand("clean", "clean", false); }
    public void mavenInstall() { runMavenCommand("install", "publishToMavenLocal", true); }

    /**
     * @param cmd         the Maven lifecycle phase, also the internal dispatch key
     * @param gradleTask  the equivalent Gradle task name, shown when the project uses Gradle
     * @param needsPom    whether the phase needs a parsed build model
     */
    private void runMavenCommand(String cmd, String gradleTask, boolean needsPom) {
        ProjectManager pm = callback.getProjectManager();
        if (pm == null || !BuildSystem.isBuildable(pm.getProjectDir())) {
            Toast.makeText(callback.getActivity(), R.string.toast_build_script_required, Toast.LENGTH_SHORT).show();
            return;
        }
        callback.saveCurrentToActiveTab();
        callback.switchBottomPanel(PANEL_RUN);
        callback.setConsoleText("");

        BuildSystem.Kind kind = BuildSystem.detect(pm.getProjectDir());
        String tool = kind == BuildSystem.Kind.GRADLE ? "gradle" : "mvn";
        String task = kind == BuildSystem.Kind.GRADLE ? gradleTask : cmd;
        callback.appendConsole(callback.getActivity().getString(R.string.console_build_task, tool, task),
                callback.getTheme().textDim);

        try {
            PomModel pom = null;
            if (needsPom) {
                BuildSystem.Model model = BuildSystem.model(pm.getProjectDir());
                reportWarnings(model);
                pom = model.pom;
            }
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

    /** Surfaces anything the Gradle parser had to skip, so it is never silent. */
    private void reportWarnings(BuildSystem.Model model) {
        if (model.warnings.isEmpty()) return;
        for (String w : model.warnings) {
            callback.runOnUiThread(() ->
                    callback.appendConsole("   ⚠ " + w, callback.getTheme().errorText));
        }
    }
}
