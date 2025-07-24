package ru.drughack.utils.other;

import java.nio.file.*;
import java.io.*;

public class FileUtils {

    public static void resetFile(String path) throws IOException {
        if (Files.exists(Paths.get(path))) new File(path).delete();
        Files.createFile(Paths.get(path));
    }

    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }
}