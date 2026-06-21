package com.ccs.javadroid.analysis;

import android.os.Handler;
import android.os.Looper;

import java.io.File;

/**
 * Періодичне оновлення Problems (за замовчуванням кожні 10 с), поки активність на екрані.
 */
public final class LiveProblemsScheduler {

    public interface Sources {
        /** Текст з редактора для активного Java-файлу. */
        String getEditorText();

        /** Відкритий .java або null. */
        File getActiveJavaFile();

        File getProjectRoot();

        /** Під час Run / довгої збірки — пропустити цикл. */
        boolean shouldSkipScan();
    }

    public interface OnProblems {
        void onProblems(java.util.List<ProblemItem> items);
    }

    private static final long FIRST_DELAY_MS = 800L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final android.content.Context appContext;
    private final Sources sources;
    private final OnProblems callback;
    private long intervalMs = 10_000L;

    private volatile boolean stopped = true;
    private volatile boolean scanning;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (stopped) {
                return;
            }
            if (sources.shouldSkipScan() || scanning) {
                mainHandler.postDelayed(this, intervalMs);
                return;
            }
            scanning = true;
            final String text;
            final File active;
            final File root;
            text = sources.getEditorText();
            active = sources.getActiveJavaFile();
            root = sources.getProjectRoot();

            new Thread(() -> {
                try {
                    java.util.List<ProblemItem> list =
                            ProblemsWorkspaceAnalyzer.analyze(appContext, root, text, active);
                    mainHandler.post(() -> {
                        callback.onProblems(list);
                        scanning = false;
                        if (!stopped) {
                            mainHandler.postDelayed(tick, intervalMs);
                        }
                    });
                } catch (Throwable t) {
                    mainHandler.post(() -> {
                        scanning = false;
                        if (!stopped) {
                            mainHandler.postDelayed(tick, intervalMs);
                        }
                    });
                }
            }, "live-problems").start();
        }
    };

    public LiveProblemsScheduler(android.content.Context context, Sources sources, OnProblems callback) {
        this.appContext = context.getApplicationContext();
        this.sources = sources;
        this.callback = callback;
    }

    public void setInterval(long ms) {
        this.intervalMs = ms;
    }

    public void start() {
        stopped = false;
        mainHandler.removeCallbacks(tick);
        mainHandler.postDelayed(tick, FIRST_DELAY_MS);
    }

    public void stop() {
        stopped = true;
        scanning = false;
        mainHandler.removeCallbacks(tick);
    }
}
