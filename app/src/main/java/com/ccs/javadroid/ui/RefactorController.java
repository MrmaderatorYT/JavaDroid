package com.ccs.javadroid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ccs.javadroid.R;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.tools.refactor.RefactoringHelper;
import com.ccs.javadroid.util.AppTheme;

import java.io.File;

import io.github.rosemoe.sora.widget.CodeEditor;

public final class RefactorController {

    public interface Callback {
        CodeEditor getActiveEditor();
        ProjectManager getProjectManager();
        AppTheme getTheme();
        void runOnUiThread(@NonNull Runnable r);
        void refreshProblemsAsync();
        FileTab getActiveTab();
        FileTab getLeftTab();
        FileTab getRightTab();
        CodeEditor getEditor();
        CodeEditor getEditor2();
        void reloadTab(FileTab tab);
        int dp(int v);
    }

    private final Activity activity;
    private final Callback callback;

    public RefactorController(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void showDialog() {
        CodeEditor ed = callback.getActiveEditor();
        if (ed == null) return;

        io.github.rosemoe.sora.text.Cursor cursor = ed.getCursor();
        int line = cursor.getLeftLine();
        int col = cursor.getLeftColumn();
        AppTheme theme = callback.getTheme();

        String lineText = "";
        try { lineText = ed.getText().getLine(line).toString(); } catch (Exception ignored) {}

        String selectedText = "";
        try {
            io.github.rosemoe.sora.text.Content content = ed.getText();
            int leftLine = cursor.getLeftLine();
            int leftCol = cursor.getLeftColumn();
            int rightLine = cursor.getRightLine();
            int rightCol = cursor.getRightColumn();
            if (leftLine == rightLine) {
                selectedText = content.getLine(leftLine).subSequence(leftCol, rightCol).toString().trim();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(content.getLine(leftLine).subSequence(leftCol, content.getLine(leftLine).length()));
                for (int i = leftLine + 1; i < rightLine; i++) sb.append("\n").append(content.getLine(i));
                if (rightLine < content.getLineCount()) sb.append("\n").append(content.getLine(rightLine).subSequence(0, rightCol));
                selectedText = sb.toString().trim();
            }
        } catch (Exception ignored) {}

        if (selectedText.isEmpty()) selectedText = extractWordAtCursor(lineText, col);

        String finalSelectedText = selectedText;
        String[] items = {
                "Rename '" + selectedText + "'",
                "Extract Method", "Extract Variable",
                "Inline Method", "Find Usages of '" + selectedText + "'"
        };

        new AlertDialog.Builder(activity)
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

    public void reloadTab(FileTab tab) {
        if (tab == null || tab.file == null || !tab.file.exists()) return;
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(tab.file.toPath());
            String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            CodeEditor ed = (tab == callback.getLeftTab()) ? callback.getEditor() : callback.getEditor2();
            if (ed != null) ed.setText(content);
        } catch (Exception ignored) {}
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
        AppTheme theme = callback.getTheme();
        EditText input = new EditText(activity);
        input.setText(currentName);
        input.setHint(R.string.refactor_rename_hint);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        input.setSelection(currentName.length());
        int pad = callback.dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.refactor_rename_title)
                .setView(input)
                .setPositiveButton("Rename", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty() || newName.equals(currentName)) return;
                    if (!RefactoringHelper.isValidIdentifier(newName)) {
                        Toast.makeText(activity, activity.getString(R.string.toast_invalid_identifier), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File projectRoot = callback.getProjectManager() != null ? callback.getProjectManager().getProjectDir() : null;
                    RefactoringHelper.renameSymbolAsync(activity, projectRoot, currentName, newName, result -> {
                        Toast.makeText(activity, result.summary, Toast.LENGTH_LONG).show();
                        if (result.filesChanged > 0) {
                            callback.refreshProblemsAsync();
                            callback.reloadTab(callback.getActiveTab());
                        }
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showExtractMethodDialog() {
        CodeEditor ed = callback.getActiveEditor();
        if (ed == null) return;
        AppTheme theme = callback.getTheme();
        io.github.rosemoe.sora.text.Cursor cursor = ed.getCursor();
        int selStartLine = cursor.getLeftLine();
        int selEndLine = cursor.getRightLine();

        if (selStartLine == selEndLine && selStartLine == cursor.getRightLine()) {
            Toast.makeText(activity, activity.getString(R.string.toast_select_code_block), Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(activity);
        input.setHint(R.string.refactor_extract_method_hint);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        int pad = callback.dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.refactor_extract_method_title)
                .setView(input)
                .setPositiveButton("Extract", (d, w) -> {
                    String methodName = input.getText().toString().trim();
                    if (methodName.isEmpty()) return;
                    if (!RefactoringHelper.isValidIdentifier(methodName)) {
                        Toast.makeText(activity, activity.getString(R.string.toast_invalid_identifier), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String source = ed.getText().toString();
                    RefactoringHelper.extractMethodAsync(activity, source, selStartLine, selEndLine, methodName, result ->
                            Toast.makeText(activity, result.summary, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showExtractVariableDialog() {
        CodeEditor ed = callback.getActiveEditor();
        if (ed == null) return;
        AppTheme theme = callback.getTheme();
        io.github.rosemoe.sora.text.Cursor cursor = ed.getCursor();

        String selectedText = "";
        try {
            io.github.rosemoe.sora.text.Content content = ed.getText();
            int leftLine = cursor.getLeftLine();
            int leftCol = cursor.getLeftColumn();
            int rightLine = cursor.getRightLine();
            int rightCol = cursor.getRightColumn();
            if (leftLine == rightLine) {
                selectedText = content.getLine(leftLine).subSequence(leftCol, rightCol).toString().trim();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(content.getLine(leftLine).subSequence(leftCol, content.getLine(leftLine).length()));
                for (int i = leftLine + 1; i < rightLine; i++) sb.append("\n").append(content.getLine(i));
                if (rightLine < content.getLineCount()) sb.append("\n").append(content.getLine(rightLine).subSequence(0, rightCol));
                selectedText = sb.toString().trim();
            }
        } catch (Exception ignored) {}

        if (selectedText.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.toast_select_expression), Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(activity);
        input.setHint(R.string.refactor_extract_variable_hint);
        input.setTextColor(theme.text);
        input.setHintTextColor(theme.textDim);
        int pad = callback.dp(16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.refactor_extract_variable_title)
                .setView(input)
                .setPositiveButton("Extract", (d, w) -> {
                    String varName = input.getText().toString().trim();
                    if (varName.isEmpty()) return;
                    if (!RefactoringHelper.isValidIdentifier(varName)) {
                        Toast.makeText(activity, activity.getString(R.string.toast_invalid_identifier), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String source = ed.getText().toString();
                    RefactoringHelper.extractVariableAsync(source, cursor.getLeftLine(),
                            cursor.getLeftColumn(), cursor.getRightColumn(), varName,
                            result -> Toast.makeText(activity, result.summary, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showInlineDialog(String methodName) {
        CodeEditor ed = callback.getActiveEditor();
        if (ed == null || methodName.isEmpty()) return;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.refactor_inline_title)
                .setMessage("Inline method '" + methodName + "'?")
                .setPositiveButton("Inline", (d, w) -> {
                    String source = ed.getText().toString();
                    RefactoringHelper.inlineMethodAsync(activity, source, methodName,
                            result -> Toast.makeText(activity, result.summary, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showFindUsagesDialog(String symbolName) {
        if (symbolName.isEmpty() || callback.getProjectManager() == null) return;

        File projectRoot = callback.getProjectManager().getProjectDir();
        new Thread(() -> {
            java.util.List<RefactoringHelper.UsageLocation> usages = RefactoringHelper.findUsages(projectRoot, symbolName);
            callback.runOnUiThread(() -> {
                if (usages.isEmpty()) {
                    Toast.makeText(activity, activity.getString(R.string.toast_no_usages, symbolName), Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(usages.size()).append(" usages of '").append(symbolName).append("':\n\n");
                for (int i = 0; i < Math.min(usages.size(), 20); i++) {
                    RefactoringHelper.UsageLocation u = usages.get(i);
                    sb.append(u.file.getName()).append(":").append(u.line).append("  ").append(u.lineContent.trim()).append("\n");
                }
                if (usages.size() > 20) sb.append("... and ").append(usages.size() - 20).append(" more");

                new AlertDialog.Builder(activity)
                        .setTitle("Usages of '" + symbolName + "'")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }
}
