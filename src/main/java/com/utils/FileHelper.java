package com.utils;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileHelper {

    private static final Logger LOGGER = Logger.getLogger(FileHelper.class);
    private static final String MASK = "?";

    public static boolean saveTests(final List<String> tests, final String path) {
        var ok = false;
            File file = new File(path);
        try {
            FileUtils.writeLines(file, tests);
            ok = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return ok;
    }

    public static File getResourceFile(final String fileName, final boolean test) {
        var path = Helper.getCurrentDir() + "/src/?/resources/" + fileName;
        var finalPath = (test) ? path.replace(MASK, "test") : path.replace(MASK, "main");
        return new File(finalPath);
    }

    public static String getResourcePath(final String fileName, final boolean test) {
        var path = Helper.getCurrentDir() + "/src/?/resources/" + fileName;
        var finalPath = (test) ? path.replace(MASK, "test") : path.replace(MASK, "main");
        return finalPath;
    }

    public static boolean saveResourceFile(final String fileName, final String content, final boolean test) {
        var result = false;
        try {
            var path = Helper.getCurrentDir() + "/src/?/resources/" + fileName;
            var finalPath = (test) ? path.replace(MASK, "test") : path.replace(MASK, "main");
            var file = new File(finalPath);
            FileUtils.write(file, content, StandardCharsets.UTF_8);
            result = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static boolean moveFile(final String pathFrom, final String pathTo) {
        var result = false;
        try {
            var fileFrom = new File(pathFrom);
            var fileTo = new File(pathTo);
            if(fileTo.exists()) {
                fileTo.delete();
            }
            FileUtils.moveFile(fileFrom, fileTo);
            result = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


}
