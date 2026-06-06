package com.example.securestoragelab.files;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class InternalTextStore {

    private InternalTextStore() {}

    public static void writeUtf8(Context context, String fileName, String content) throws Exception {
        // Utilisation de try-with-resources pour fermer automatiquement le flux
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String readUtf8(Context context, String fileName) throws Exception {
        try (FileInputStream fis = context.openFileInput(fileName);
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
        return context.deleteFile(fileName);
    }
}