package com.ccs.javadroid.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ccs.javadroid.R;
import com.ccs.javadroid.tools.compilers.ProjectCompiler;
import com.ccs.javadroid.util.AppTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class JShellPanelManager {

    private final Activity activity;
    private final View panel;
    private final TextView tab;
    private final ScrollView scrollView;
    private final TextView consoleOutput;
    private final EditText inputField;

    private final List<String> history = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public JShellPanelManager(Activity activity, View panel, TextView tab, ScrollView scrollView, TextView consoleOutput, EditText inputField) {
        this.activity = activity;
        this.panel = panel;
        this.tab = tab;
        this.scrollView = scrollView;
        this.consoleOutput = consoleOutput;
        this.inputField = inputField;

        appendOutput("Welcome to JShell for Android.\nType Java statements (e.g. System.out.println(\"Hi\");)\n\n");

        this.inputField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                String code = this.inputField.getText().toString();
                if (!code.isEmpty()) {
                    this.inputField.setText("");
                    executeCode(code);
                }
                return true;
            }
            return false;
        });
    }

    public void applyTheme(AppTheme theme) {
        if (panel != null) panel.setBackgroundColor(theme.consoleBg);
        if (consoleOutput != null) consoleOutput.setTextColor(theme.consoleText);
        if (inputField != null) {
            inputField.setTextColor(theme.text);
            inputField.setHintTextColor(theme.textDim);
            inputField.setBackgroundColor(theme.toolbar);
        }
    }

    public View getPanel() {
        return panel;
    }

    public TextView getTab() {
        return tab;
    }
    
    public TextView getConsoleOutput() {
        return consoleOutput;
    }

    public void setVisibility(int visibility) {
        panel.setVisibility(visibility);
    }

    private void appendOutput(String text) {
        handler.post(() -> {
            consoleOutput.append(text);
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    private void executeCode(String code) {
        String trimmed = code.trim();
        appendOutput("jshell> " + code + "\n");

        // Handle internal JShell commands
        if (trimmed.startsWith("/")) {
            handleInternalCommand(trimmed);
            return;
        }

        history.add(code);
        
        new Thread(() -> {
            try {
                File cacheDir = new File(activity.getCacheDir(), "jshell_cache");
                if (!cacheDir.exists()) cacheDir.mkdirs();
                
                StringBuilder src = new StringBuilder();
                src.append("import java.util.*;\nimport java.io.*;\nimport java.math.*;\n");
                src.append("public class JShellMain {\n");
                src.append("  public static void main(String[] args) throws Exception {\n");
                for (String line : history) {
                    if (!line.trim().endsWith(";") && !line.trim().endsWith("}") && !line.trim().endsWith("{")) {
                        src.append("    ").append(line).append(";\n");
                    } else {
                        src.append("    ").append(line).append("\n");
                    }
                }
                src.append("  }\n}\n");

                File srcFile = new File(cacheDir, "JShellMain.java");
                ProjectCompiler.writeUtf8Public(srcFile, src.toString());

                File androidJar = ProjectCompiler.ensureAndroidJarPublic(activity, cacheDir);
                
                String ecjErr = ProjectCompiler.compileEcjPublic(androidJar, null, cacheDir, "1.8", srcFile);
                if (ecjErr != null) {
                    history.remove(history.size() - 1);
                    appendOutput("Error: " + ecjErr + "\n");
                    return;
                }

                File classFile = ProjectCompiler.findClassFilePublic(cacheDir, "JShellMain");
                if (classFile == null) {
                    history.remove(history.size() - 1);
                    appendOutput("Error: Could not find compiled class.\n");
                    return;
                }

                File dexDir = new File(cacheDir, "jshell_dex");
                if (!dexDir.exists()) dexDir.mkdirs();
                else {
                    File[] oldFiles = dexDir.listFiles();
                    if (oldFiles != null) for (File f : oldFiles) f.delete();
                }

                ProjectCompiler.runD8DexPublic(androidJar, dexDir, classFile);

                ProjectCompiler.debugRunDex(activity, "JShellMain", dexDir, cacheDir, null, new ProjectCompiler.Callback() {
                    @Override public void onProgress(String msg) { appendOutput(msg + "\n"); }
                    @Override public void onResult(String res) { 
                        if (!res.trim().isEmpty() && !res.equals("Success")) {
                            // Optionally handle result
                        }
                        appendOutput("\n");
                    }
                    @Override public void onProblems(java.util.List<com.ccs.javadroid.analysis.ProblemItem> problems) {}
                });
            } catch (Exception e) {
                history.remove(history.size() - 1);
                appendOutput("Error: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void handleInternalCommand(String cmd) {
        switch (cmd) {
            case "/help":
                appendOutput("Available commands:\n" +
                        "  /help    - Show this help message\n" +
                        "  /clear   - Clear the console output\n" +
                        "  /reset   - Reset JShell memory (forget variables)\n" +
                        "  /history - Show history of entered commands\n\n");
                break;
            case "/clear":
                consoleOutput.setText("");
                appendOutput("Console cleared.\n\n");
                break;
            case "/reset":
                history.clear();
                appendOutput("JShell state reset. All variables are forgotten.\n\n");
                break;
            case "/history":
                if (history.isEmpty()) {
                    appendOutput("History is empty.\n\n");
                } else {
                    appendOutput("History:\n");
                    for (int i = 0; i < history.size(); i++) {
                        appendOutput("  " + (i + 1) + ": " + history.get(i) + "\n");
                    }
                    appendOutput("\n");
                }
                break;
            default:
                appendOutput("Unknown command: " + cmd + ". Type /help for available commands.\n\n");
                break;
        }
    }
}
