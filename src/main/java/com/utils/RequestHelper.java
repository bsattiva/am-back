package com.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RequestHelper {


    public static String cleanOutput(final String out) {
        var blank = '\u0000';
        String output = out.replace('┌', blank)
                .replace('─', blank)
                .replace('┐', blank)
                .replace('│', blank)
                .replace('/', blank)
                .replace('└', blank)
                .replace('┘', blank);
        var sendableOut = output;
        System.out.println();
        System.out.println(output.indexOf("OpenJDK"));
        System.out.println(output.indexOf("Skipped: "));

        if (output.contains("Skipped: ") && output.contains("OpenJDK")) {

            sendableOut = output
                    .substring(output.indexOf("OpenJDK"));
            sendableOut = sendableOut.substring(0, sendableOut.indexOf("Skipped: "));

        } else {
            sendableOut = "CORRUPTED: " + output;

        }
        return sendableOut;
    }

    public static JSONObject getRequestBody(final HttpServletRequest request) {
        JSONObject obj = new JSONObject();

        if ("POST".equalsIgnoreCase(request.getMethod()))
        {
            String test = "";
            try {
                test = IOUtils.toString(request.getReader());;
                obj = new JSONObject(test.replace("\r", "").replace("\n", ""));



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }
}
