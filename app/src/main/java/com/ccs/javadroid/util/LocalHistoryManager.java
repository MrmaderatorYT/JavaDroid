package com.ccs.javadroid.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class LocalHistoryManager {

    private static final int MAX_SNAPSHOTS = 50;

    public static final class HistoryEntry {
        public final long timestamp;
        public final String label;
        public final String content;
        public final String formattedTime;

        public HistoryEntry(long timestamp, String label, String content) {
            this.timestamp = timestamp;
            this.label = label;
            this.content = content;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            this.formattedTime = sdf.format(new Date(timestamp));
        }
    }

    private LocalHistoryManager() {}

    private static String hashPath(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(path.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(path.hashCode());
        }
    }

    private static File getHistoryDir(Context context, File sourceFile) {
        if (sourceFile == null) return null;
        File baseDir = new File(context.getFilesDir(), ".local_history");
        File fileDir = new File(baseDir, hashPath(sourceFile.getAbsolutePath()));
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    public static void saveSnapshot(Context context, File sourceFile, String content, String label) {
        if (context == null || sourceFile == null || content == null) return;
        
        File dir = getHistoryDir(context, sourceFile);
        if (dir == null) return;

        // Check last entry to prevent duplicate identical snapshots
        List<HistoryEntry> existing = getHistory(context, sourceFile);
        if (!existing.isEmpty()) {
            HistoryEntry latest = existing.get(0);
            if (content.equals(latest.content)) {
                return; // Content is unchanged
            }
        }

        long ts = System.currentTimeMillis();
        String safeLabel = (label != null ? label : "Save").replaceAll("[^a-zA-Z0-9_-]", "_");
        File snapshotFile = new File(dir, ts + "_" + safeLabel + ".hist");

        try (FileOutputStream fos = new FileOutputStream(snapshotFile)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }

        pruneOldSnapshots(dir);
    }

    private static void pruneOldSnapshots(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".hist"));
        if (files == null || files.length <= MAX_SNAPSHOTS) return;

        Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        int toDelete = files.length - MAX_SNAPSHOTS;
        for (int i = 0; i < toDelete; i++) {
            files[i].delete();
        }
    }

    public static List<HistoryEntry> getHistory(Context context, File sourceFile) {
        if (context == null || sourceFile == null) return Collections.emptyList();
        
        File dir = getHistoryDir(context, sourceFile);
        if (dir == null || !dir.exists()) return Collections.emptyList();

        File[] files = dir.listFiles((d, name) -> name.endsWith(".hist"));
        if (files == null || files.length == 0) return Collections.emptyList();

        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        List<HistoryEntry> entries = new ArrayList<>();
        for (File f : fileList) {
            String name = f.getName();
            String nameNoExt = name.substring(0, name.length() - 5);
            long ts = f.lastModified();
            String label = "Save";
            int underscoreIdx = nameNoExt.indexOf('_');
            if (underscoreIdx > 0) {
                try {
                    ts = Long.parseLong(nameNoExt.substring(0, underscoreIdx));
                    label = nameNoExt.substring(underscoreIdx + 1).replace('_', ' ');
                } catch (NumberFormatException ignored) {
                }
            }

            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] data = new byte[(int) f.length()];
                int read = fis.read(data);
                if (read >= 0) {
                    String content = new String(data, 0, read, StandardCharsets.UTF_8);
                    entries.add(new HistoryEntry(ts, label, content));
                }
            } catch (IOException ignored) {
            }
        }
        return entries;
    }
}
