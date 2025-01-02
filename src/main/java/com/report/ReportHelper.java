package com.report;

import com.utils.Helper;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReportHelper {
    private static final String HTML_TEMPLATE_PATH = "/html/test.html";

    public static Document getDocument() {
        var path = Helper.getCurrentDir() + HTML_TEMPLATE_PATH;
        var file = new File(path);
        Document document = null;
        var text = "";
        try {
            text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            document = Jsoup.parse(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }


}
