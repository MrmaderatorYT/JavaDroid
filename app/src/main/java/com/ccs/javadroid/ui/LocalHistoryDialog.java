package com.ccs.javadroid.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.LocalHistoryManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class LocalHistoryDialog {

    public interface OnRevertListener {
        void onRevert(String content);
    }

    private LocalHistoryDialog() {}

    public static void show(Context context, AppTheme theme, File file, OnRevertListener listener) {
        if (context == null || file == null) return;

        List<LocalHistoryManager.HistoryEntry> history = LocalHistoryManager.getHistory(context, file);
        if (history.isEmpty()) {
            Toast.makeText(context, "No local history for " + file.getName(), Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> displayTitles = new ArrayList<>();
        for (LocalHistoryManager.HistoryEntry entry : history) {
            displayTitles.add(entry.formattedTime + "  (" + entry.label + ")");
        }

        String[] items = displayTitles.toArray(new String[0]);

        com.ccs.javadroid.ui.Dialogs.rounded(context)
                .setTitle("Local History: " + file.getName())
                .setItems(items, (dialog, which) -> {
                    LocalHistoryManager.HistoryEntry selected = history.get(which);
                    showPreviewDialog(context, theme, file, selected, listener);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void showPreviewDialog(Context context, AppTheme theme, File file,
                                          LocalHistoryManager.HistoryEntry entry,
                                          OnRevertListener listener) {
        // Show snapshot contents preview with Revert button
        android.widget.ScrollView scrollView = new android.widget.ScrollView(context);
        android.widget.TextView textView = new android.widget.TextView(context);
        textView.setText(entry.content);
        textView.setTextSize(12f);
        textView.setTypeface(android.graphics.Typeface.MONOSPACE);
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);
        if (theme != null) {
            textView.setTextColor(theme.text);
            scrollView.setBackgroundColor(theme.bg);
        }
        scrollView.addView(textView);

        com.ccs.javadroid.ui.Dialogs.rounded(context)
                .setTitle("Revision: " + entry.formattedTime + " (" + entry.label + ")")
                .setView(scrollView)
                .setPositiveButton("Revert to this state", (dialog, which) -> {
                    if (listener != null) {
                        listener.onRevert(entry.content);
                    }
                    Toast.makeText(context, "Reverted " + file.getName() + " to " + entry.formattedTime, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
