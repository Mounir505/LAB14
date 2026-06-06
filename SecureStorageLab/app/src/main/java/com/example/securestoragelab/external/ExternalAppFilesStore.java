package com.example.securestoragelab.external;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class ExternalAppFilesStore {

    private ExternalAppFilesStore() {}

    public static String write(Context context, String fileName, String content) throws Exception {
        // getExternalFilesDir(null) cible la racine du dossier externe spécifique à l'app
        File dir = context.getExternalFilesDir(null);
        if (dir == null) return null; // Le stockage externe n'est pas disponible

        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return file.getAbsolutePath();
    }

    public static String read(Context context, String fileName) throws Exception {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) return null;

        File file = new File(dir, fileName);
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

    public static boolean delete(Context context, String fileName) {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) return false;

        File file = new File(dir, fileName);
        return file.delete();
    }
}