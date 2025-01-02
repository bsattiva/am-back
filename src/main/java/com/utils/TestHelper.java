package com.utils;

import com.utils.command.JsonHelper;
import com.utils.data.QueryHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import java.util.stream.Collectors;


public class TestHelper {
    private static final String TESTS = "children";
    private static final String SECTION = "section";
    private static final String ROW_NAME = "row_name";
    private static final String INFO_ROW = "info_row";
    private static final String ARGUMENTS = "arguments";
    private static final String ARGS = "args";
    private static final String STEP = "step";
    private static final String PREFIX = "prefix";
    private static final String MASK = "?";
    private static final String TAG = "tag";
    private static final String TEST_MASK = "<?>";
    private static final String INDENT = "  ";
    private static String indent = "";
    private static final Logger LOGGER = Logger.getLogger(TestHelper.class);

    private static String getStep (final String step, final JSONArray arguments) {
        var i = 1;
        var completeStep = step;
        for (var arg : JsonHelper.getListFromJsonArray(arguments)) {
            var mask = TEST_MASK.replace(MASK, Integer.toString(i));
            var argument = (!Helper.isInteger(arg))
                    ? "\"" + arg + "\"" : arg;
            completeStep = completeStep.replace(mask, argument);
            i++;
        }
        return completeStep;
    }


    public static String getSameLevelProject(final String name) {

        var parts = Helper.getCurrentDir().replace("\\", "/").split("/");
        var path =  Arrays.stream(parts)
                .filter(part -> !Helper.getCurrentDir().endsWith(part)).collect(Collectors.joining("/"))
                +
                "/"
                + name;
        if ((System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows"))) {
            return path;
        } else if (path.startsWith("/")) {
            return path;
        } else {
            return "/" + path;
        }
    }


    public static List<String> getTests(final JSONObject tests) {
        indent = indent + INDENT;
        var testArray = tests.getJSONArray(TESTS);
        var arguments = tests.getJSONArray(ARGUMENTS);
        var prefix = (tests.has(PREFIX)) ? tests.getString(PREFIX) + " " : "";

        var step = prefix + tests.getString(STEP);

        var tag = tests.getString(TAG);
        List<String> result = new ArrayList<>();
        result.add(tag);
        result.add(indent + getStep(step, arguments));
        for (var i = 0; i < testArray.length(); i++) {
            var testList = getTests(testArray.getJSONObject(i), INDENT);
            result.addAll(testList);
        }
        return result;
    }

    private static JSONArray getArguments(final JSONObject obj) {
        var arguments = (obj.has(ARGUMENTS)) ? obj.getJSONArray(ARGUMENTS) : new JSONArray();
        var args = (obj.has(ARGS)) ? obj.getJSONArray(ARGS) : new JSONArray();

         return (arguments.length() > 0) ? arguments : args;

    }
    public static List<String> getTests(final JSONObject tests, final String indent) {
        var indentValue = indent + INDENT;
        var testArray = (tests.has(TESTS)) ? tests.getJSONArray(TESTS) : new JSONArray();
        var arguments = getArguments(tests);
        var prefix = (tests.has(PREFIX)) ? tests.getString(PREFIX) + " " : "";

        var step = prefix + tests.getString(STEP);
        List<String> result = new ArrayList<>();
        result.add(indentValue + getStep(step, arguments));
        for (var i = 0; i < testArray.length(); i++) {
            var testList = getTests(testArray.getJSONObject(i), indentValue);
            result.addAll(testList);
        }
        return result;
    }

    public static void sleep(final int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }

    }

    public static boolean saveTest(final JSONObject object, final String path) {
        var ok = false;
        try {
            FileUtils.writeLines(new File(path), getTests(object));
            ok = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return ok;
    }

    public static JSONObject getStatus(final String status) {
        var object = new JSONObject();
        object.put("status", status);
        return object;
    }

    public static boolean savePages(final JSONObject object, final String path) {
        var ok = false;
        if (!object.has("pages")) {
            object.put("pages", new JSONArray());

        }
        try {
            FileUtils.write(new File(path), object.toString(5), StandardCharsets.UTF_8);
            ok = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return ok;
    }

    public static String getManagedUserId(final String token, final String userToken) {
        var id = QueryHelper.getIdByToken(token);
        if (Helper.isThing(userToken) && !userToken.equals("null") && QueryHelper.isAdmin(id)) {
            return QueryHelper.getIdByToken(userToken);
        }
        return id;
    }

    private static String stripOdds(final String str) {
        var accept = ".~,~:~".split("~");
        var last = str.charAt(str.length() - 1);
        if (Character.isAlphabetic(last) || Character.isDigit(last) || List.of(accept).contains("" + last)) {
            return str;
        } else {
            return str.substring(0, str.length() - 1).trim();
        }
    }
    public static boolean appxEqual(final String one, final String two) {

        return stripOdds(one).equals(stripOdds(two));
    }

    public static void main(String[] args) {
        System.out.println(appxEqual("slovo", "slovo ?"));
    }

    public static boolean saveConfig(final List<String> map, final String path) {
        var ok = false;
        try {
            System.out.println(path);
            FileUtils.writeLines(new File(path), map, false);
            ok = true;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        return ok;
    }

    private static String getUpdatedCellContent(final String cell, final String col) {
        var content = cell;
        final var dateFormat = "yyyy-MM-dd";
        try {
            final var formatter = DateTimeFormatter.ofPattern(dateFormat);
            var localDate = LocalDate.parse(content, formatter);
            var updatedDate = localDate.plusDays(1);
            content = updatedDate.format(formatter);
        } catch (Exception e) {
            if (!List.of("date,observed,to,from".split(",")).contains(col)) {
                content = "updated" + content;
            } else {
                content = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

        }
        return content;
    }

    private static String getRestoredCellContent(final String cell, final String col) {
        var content = cell;
        final var dateFormat = "yyyy-MM-dd";
        try {
            final var formatter = DateTimeFormatter.ofPattern(dateFormat);
            var localDate = LocalDate.parse(content, formatter);

            content = LocalDate.now().format(formatter);
        } catch (Exception e) {
            if (!List.of("date,observed,to,from".split(",")).contains(col)) {
                content = "";
            } else {
                content = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

        }
        return content;
    }

    public static JSONArray restoreArray(final JSONArray model, final JSONArray array, final List<String> columns) {
        final JSONArray newArray = new JSONArray();
        for (var i = 0; i < array.length(); i++) {
            var row = array.getJSONObject(i);
            var rowName = model.getJSONObject(i).getString(ROW_NAME);
            var infoRow = model.getJSONObject(i).getString(INFO_ROW).equals("1");
            if (!infoRow) {
                var object = new JSONObject();
                for (var col : columns) {
                    if (col.equals(ROW_NAME)) {
                        object.put(ROW_NAME, rowName);
                    } else {
                        object.put(col, getRestoredCellContent(row.getString(col), col));
                    }
                }
                newArray.put(object);

            } else {
                newArray.put(array.getJSONObject(i));
            }

        }
        return newArray;
    }


    public static JSONArray dedupe(final JSONArray data) {
        var ignored = QueryHelper.getIgnored();
        var array = new JSONArray();
        List<String> list = new ArrayList<>();
        for (var i = 0; i < data.length(); i++) {
            var obj = data.getJSONObject(i);
            if (!list.contains(obj.getString(SECTION)) &&!ignored.contains(obj.getString(SECTION))) {
                array.put(obj);
                list.add(obj.getString(SECTION));
            }
        }
        return array;
    }

    public static JSONArray updateArray(final JSONArray model, final JSONArray array, final List<String> columns) {
        final JSONArray newArray = new JSONArray();
        for (var i = 0; i < array.length(); i++) {
            var row = array.getJSONObject(i);
            var rowName = model.getJSONObject(i).getString(ROW_NAME);
            if (rowName.contains("Preset control"))
                System.out.println(rowName);
            var infoRow = model.getJSONObject(i).getString(INFO_ROW).equals("1");
            if (!infoRow) {
                var object = new JSONObject();
                for (var col : columns) {
                    if (col.equals(ROW_NAME)) {
                        object.put(ROW_NAME, rowName);
                    } else {
                        if (col.equals("observed")) {
                            System.out.println(col);

                        }
                        object.put(col, getUpdatedCellContent(row.getString(col), col));
                    }
                }
                newArray.put(object);

            } else {
                newArray.put(array.getJSONObject(i));
            }

        }
        return newArray;
    }


}
