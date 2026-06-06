package com.example.securestoragelab.cache;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class CacheStore {

    private CacheStore() {}

    public static void write(Context context, String fileName, String content) throws Exception {
        File file = new File(context.getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String read(Context context, String fileName) throws Exception {
        File file = new File(context.getCacheDir(), fileName);
        if (!file.exists()) return null;

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    public static int purge(Context context) {
        File[] files = context.getCacheDir().listFiles();
        if (files == null) return 0;

        int deleted = 0;
        for (File f : files) {
            if (f.delete()) deleted++;
        }
        return deleted;
    }
}