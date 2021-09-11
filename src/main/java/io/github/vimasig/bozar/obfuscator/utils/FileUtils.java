package io.github.vimasig.bozar.obfuscator.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<File> getAllFiles(File file) {
        if(file.isFile()) return List.of(file);

        File[] files = file.listFiles();
        if(files == null) return List.of();

        final var fileList = new ArrayList<File>();
        Arrays.stream(files).forEach(f -> {
            if(f.isFile()) fileList.add(f);
            else fileList.addAll(getAllFiles(f));
        });
        return fileList;
    }
}
