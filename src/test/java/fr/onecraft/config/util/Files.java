package fr.onecraft.config.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Files {

    public static String read(File file) {
        try {
            return new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copy(File from, File to) {
        try {
            com.google.common.io.Files.copy(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copy(InputStream from, Path to) {
        try {
            java.nio.file.Files.copy(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void put(String content, File to) {
        try {
            java.nio.file.Files.write(to.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
