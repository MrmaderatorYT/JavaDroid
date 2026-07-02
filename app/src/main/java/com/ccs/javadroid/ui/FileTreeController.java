package com.ccs.javadroid.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ccs.javadroid.R;
import com.ccs.javadroid.project.ProjectManager;
import com.ccs.javadroid.project.ProjectScanner;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FileTemplates;
import com.ccs.javadroid.project.ProjectLayoutHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FileTreeController {

    public interface Callback {
        void onFileOpened(File file);
        void onRefreshNeeded();
        AppTheme getTheme();
        ProjectManager getProjectManager();
        FileTreeAdapter getFileTreeAdapter();
        TabsAdapter getTabsAdapter();
        DrawerLayout getDrawerLayout();
        int dp(int v);
    }

    private final Activity activity;
    private final Callback callback;
    private File copiedFile;
    private File pendingArchiveFolder;

    public FileTreeController(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void refreshFileTree() {
        ProjectManager pm = callback.getProjectManager();
        if (pm != null) {
            callback.getFileTreeAdapter().setNodes(ProjectScanner.listIdeaStyleTree(pm.getProjectDir()));
        }
    }

    public void showNewFileDialog() {
        ProjectManager pm = callback.getProjectManager();
        if (pm == null) return;
        AppTheme theme = callback.getTheme();

        EditText input = newEditText("");
        input.setHint(R.string.dialog_new_java_hint);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_new_java_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    String template = "";
                    boolean isJava = name.endsWith(".java") || !name.contains(".");
                    String className = name.replace(".java", "");
                    if (isJava) {
                        if (pm.isMavenProject()) {
                            try {
                                String pkg = com.ccs.javadroid.project.ProjectLayoutHelper.mainPackageName(pm.getProjectDir());
                                template = "package " + pkg + ";\n\npublic class " + className + " {\n\n}\n";
                            } catch (Exception e) {
                                template = "public class " + className + " {\n\n}\n";
                            }
                        } else {
                            template = "public class " + className + " {\n\n}\n";
                        }
                    }
                    try {
                        File f = pm.createFile(className, template);
                        if (f != null) {
                            refreshFileTree();
                            callback.onFileOpened(f);
                            DrawerLayout drawer = callback.getDrawerLayout();
                            if (drawer != null) drawer.closeDrawer(Gravity.START);
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.toast_file_exists, className), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(activity, activity.getString(R.string.toast_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    public void showFolderContextMenu(File folder) {
        List<String> optionsList = new ArrayList<>();
        optionsList.add(activity.getString(R.string.menu_create_file));
        optionsList.add(activity.getString(R.string.menu_create_folder));
        if (copiedFile != null && copiedFile.exists()) {
            optionsList.add(activity.getString(R.string.dialog_folder_context_paste));
        }
        optionsList.add(activity.getString(R.string.dialog_folder_context_archive));
        optionsList.add(activity.getString(R.string.dialog_file_context_rename));
        optionsList.add(activity.getString(R.string.dialog_file_context_delete));

        String[] options = optionsList.toArray(new String[0]);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(folder.getName())
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (selected.equals(activity.getString(R.string.menu_create_file))) {
                        showNewFileInFolderDialog(folder);
                    } else if (selected.equals(activity.getString(R.string.menu_create_folder))) {
                        showNewFolderInFolderDialog(folder);
                    } else if (selected.equals(activity.getString(R.string.dialog_folder_context_paste))) {
                        pasteFileToFolder(folder);
                    } else if (selected.equals(activity.getString(R.string.dialog_folder_context_archive))) {
                        createArchiveFromFolder(folder);
                    } else if (selected.equals(activity.getString(R.string.dialog_file_context_rename))) {
                        showRenameDialog(folder);
                    } else if (selected.equals(activity.getString(R.string.dialog_file_context_delete))) {
                        showDeleteDialog(folder);
                    }
                })
                .show();
    }

    public void showFileContextMenu(File file) {
        String[] options = {
                activity.getString(R.string.dialog_file_context_open),
                activity.getString(R.string.dialog_file_context_rename),
                activity.getString(R.string.dialog_file_context_copy),
                activity.getString(R.string.dialog_file_context_delete)
        };
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(file.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: callback.onFileOpened(file); break;
                        case 1: showRenameDialog(file); break;
                        case 2: copiedFile = file; Toast.makeText(activity, R.string.toast_file_copied, Toast.LENGTH_SHORT).show(); break;
                        case 3: showDeleteDialog(file); break;
                    }
                })
                .show();
    }

    private void showNewFileInFolderDialog(File folder) {
        String[] names = FileTemplates.getDisplayNames();
        String[] keys = FileTemplates.getKeys();

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
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
        ProjectManager pm = callback.getProjectManager();
        EditText input = newEditText("");
        input.setHint(R.string.dialog_new_java_hint);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
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
                    if (pm != null && pm.isMavenProject() && templateKey.equals(FileTemplates.KEY_CLASS)) {
                        try {
                            String pkg = com.ccs.javadroid.project.ProjectLayoutHelper.mainPackageName(pm.getProjectDir());
                            template = "package " + pkg + ";\n\n" + template;
                        } catch (Exception ignored) {}
                    }
                    try {
                        File f = pm != null ? pm.createFile(className, template) : null;
                        if (f != null) {
                            refreshFileTree();
                            callback.onFileOpened(f);
                            DrawerLayout drawer = callback.getDrawerLayout();
                            if (drawer != null) drawer.closeDrawer(Gravity.START);
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.toast_file_exists, className), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(activity, activity.getString(R.string.toast_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showNewFolderInFolderDialog(File folder) {
        EditText input = newEditText("");
        input.setHint(R.string.dialog_create_folder_hint);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_create_folder_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_create, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    File sub = new File(folder, name);
                    if (sub.exists()) {
                        Toast.makeText(activity, R.string.folder_already_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sub.mkdirs()) {
                        refreshFileTree();
                    } else {
                        Toast.makeText(activity, R.string.folder_create_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    public void showRenameDialog(File file) {
        ProjectManager pm = callback.getProjectManager();
        TabsAdapter tabs = callback.getTabsAdapter();
        EditText input = newEditText("");
        String currentNameWithoutExt = file.isDirectory() ? file.getName() :
                (file.getName().endsWith(".java") ? file.getName().substring(0, file.getName().length() - 5) : file.getName());
        input.setText(currentNameWithoutExt);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_rename)
                .setView(input)
                .setPositiveButton(R.string.dialog_rename, (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty() || newName.equals(currentNameWithoutExt)) return;
                    File parent = file.getParentFile() != null ? file.getParentFile() : (pm != null ? pm.getProjectDir() : null);
                    if (parent == null) return;
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
                    int tabIdx = tabs != null ? tabs.indexOfFile(file) : -1;
                    if (tabIdx >= 0) callback.onRefreshNeeded();
                    if (file.renameTo(newFile)) {
                        if (tabIdx >= 0 && tabs != null) tabs.removeTab(tabIdx);
                        refreshFileTree();
                        if (newFile.isFile()) callback.onFileOpened(newFile);
                    } else {
                        Toast.makeText(activity, R.string.toast_rename_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    public void showDeleteDialog(File file) {
        ProjectManager pm = callback.getProjectManager();
        TabsAdapter tabs = callback.getTabsAdapter();
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_delete_file_title)
                .setMessage(activity.getString(R.string.dialog_delete_file_message, file.getName()))
                .setPositiveButton(R.string.dialog_delete, (d, w) -> {
                    int tabIdx = tabs != null ? tabs.indexOfFile(file) : -1;
                    if (tabIdx >= 0) callback.onRefreshNeeded();
                    if (pm != null) pm.deleteFile(file);
                    refreshFileTree();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    public void pasteFileToFolder(File folder) {
        if (copiedFile == null || !copiedFile.exists()) {
            copiedFile = null;
            return;
        }
        File dest = new File(folder, copiedFile.getName());
        if (dest.exists()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.toast_file_exists_title)
                    .setMessage(activity.getString(R.string.toast_file_exists, copiedFile.getName()))
                    .setPositiveButton(R.string.dialog_overwrite, (d, w) -> doPasteFile(folder, dest))
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
            Toast.makeText(activity, activity.getString(R.string.toast_file_pasted, folder.getName()), Toast.LENGTH_SHORT).show();
            refreshFileTree();
        } catch (IOException e) {
            Toast.makeText(activity, activity.getString(R.string.toast_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    public void createArchiveFromFolder(File folder) {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE, folder.getName() + ".zip");
        pendingArchiveFolder = folder;
        try {
            activity.startActivityForResult(i, 4009);
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void archiveFolderToUri(Uri uri) {
        File folder = pendingArchiveFolder;
        if (folder == null || !folder.exists()) return;
        new Thread(() -> {
            try (java.io.OutputStream os = activity.getContentResolver().openOutputStream(uri);
                 java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(os)) {
                if (os == null) throw new IOException("Cannot open output");
                zipDir(folder, folder, zos);
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        activity.getString(R.string.toast_archive_created, folder.getName() + ".zip"),
                        Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void zipDir(File root, File current, java.util.zip.ZipOutputStream zos) throws IOException {
        File[] files = current.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                zipDir(root, f, zos);
            } else {
                String path = root.toURI().relativize(f.toURI()).getPath();
                java.io.FileInputStream fis = new java.io.FileInputStream(f);
                zos.putNextEntry(new java.util.zip.ZipEntry(path));
                byte[] buf = new byte[4096];
                int len;
                while ((len = fis.read(buf)) > 0) zos.write(buf, 0, len);
                fis.close();
                zos.closeEntry();
            }
        }
    }

    private EditText newEditText(String hint) {
        EditText input = new EditText(activity);
        input.setText(hint);
        input.setTextColor(callback.getTheme().text);
        input.setHintTextColor(callback.getTheme().textDim);
        int pad = callback.dp(16);
        input.setPadding(pad, pad, pad, pad);
        return input;
    }
}
