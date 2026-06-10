package com.ccs.javadroid;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NdkManager {

    private static final String NDK_URL = "https://github.com/lzhiyong/termux-ndk/releases/download/android-ndk/android-ndk-r27b-aarch64.zip";
    private static final String NDK_DIR_NAME = "android-ndk-r27b";

    public interface NdkInstallCallback {
        void onProgress(String message, int percent);
        void onSuccess();
        void onError(String error);
    }

    public static boolean isNdkInstalled(Context context) {
        File clang = getClangPath(context);
        return clang.exists() && clang.canExecute();
    }

    public static File getNdkDir(Context context) {
        return new File(context.getFilesDir(), "ndk/" + NDK_DIR_NAME);
    }

    public static File getClangPath(Context context) {
        return new File(getNdkDir(context), "toolchains/llvm/prebuilt/linux-aarch64/bin/clang++");
    }

    public static void downloadAndInstallNdk(Context context, NdkInstallCallback callback) {
        new Thread(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            File ndkRoot = new File(context.getFilesDir(), "ndk");
            if (!ndkRoot.exists()) ndkRoot.mkdirs();
            
            File zipFile = new File(ndkRoot, "ndk.zip");

            try {
                // 1. Download
                handler.post(() -> callback.onProgress("Downloading NDK (~130MB)...", 0));
                HttpURLConnection conn = (HttpURLConnection) new URL(NDK_URL).openConnection();
                conn.connect();
                
                int fileLength = conn.getContentLength();
                try (InputStream input = new BufferedInputStream(conn.getInputStream());
                     FileOutputStream output = new FileOutputStream(zipFile)) {
                    
                    byte[] data = new byte[8192];
                    long total = 0;
                    int count;
                    int lastPercent = 0;
                    
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                        if (fileLength > 0) {
                            int percent = (int) (total * 100 / fileLength);
                            if (percent > lastPercent) {
                                lastPercent = percent;
                                handler.post(() -> callback.onProgress("Downloading NDK... " + percent + "%", percent));
                            }
                        }
                    }
                }

                // 2. Extract
                handler.post(() -> callback.onProgress("Extracting NDK (this may take a few minutes)...", 100));
                unzip(zipFile, ndkRoot);
                
                // 3. Make binaries executable
                handler.post(() -> callback.onProgress("Setting up permissions...", 100));
                File binDir = new File(getNdkDir(context), "toolchains/llvm/prebuilt/linux-aarch64/bin");
                makeExecutableRecursive(binDir);
                File libexecDir = new File(getNdkDir(context), "toolchains/llvm/prebuilt/linux-aarch64/libexec");
                makeExecutableRecursive(libexecDir);

                // Cleanup zip
                zipFile.delete();

                handler.post(callback::onSuccess);

            } catch (Exception e) {
                if (zipFile.exists()) zipFile.delete();
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    private static void unzip(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry ze;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.exists()) dir.mkdirs();

                if (!ze.isDirectory()) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void makeExecutableRecursive(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        makeExecutableRecursive(f);
                    } else {
                        f.setExecutable(true, false);
                    }
                }
            }
        }
    }
}
