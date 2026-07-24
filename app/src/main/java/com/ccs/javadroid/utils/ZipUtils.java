package com.ccs.javadroid.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static void unzip(InputStream zipInputStream, File destDirectory) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }
        try (ZipInputStream zipIn = new ZipInputStream(zipInputStream)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory.getAbsolutePath() + File.separator + entry.getName();
                File newFile = new File(filePath);
                
                // Protect against zip slip
                if (!newFile.getCanonicalPath().startsWith(destDirectory.getCanonicalPath() + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + entry.getName());
                }

                if (!entry.isDirectory()) {
                    extractFile(zipIn, newFile);
                } else {
                    newFile.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
