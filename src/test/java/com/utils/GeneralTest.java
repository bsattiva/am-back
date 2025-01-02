package com.utils;

import com.utils.data.QueryHelper;
import com.utils.excel.DupeList;
import com.utils.excel.ExcelHelper;
import com.utils.test.DataSort;
import com.utils.test.FileDownload;
import com.utils.test.FileUpload;
import com.utils.test.HelperTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneralTest {
    public static final String PULL_TABLE = "pull-table";
    public static final String ROW_NAME = "row_name";
    public static final String INFO_ROW = "info_row";
    private static final String MESSAGE = "message";

    @Test
    public void e2eTest() throws JSONException {
        var token = new JSONObject(HelperTest.getTokenTest("mkarpoff@mail.ru", "d"))
                .getString("token");
        var sheetId = "6";

        var managedToken = new JSONObject(HelperTest.getTokenTest("test@test.com", "baraban"))
                .getString("token");

        var managedId = QueryHelper.getIdByToken(managedToken);

        final String url = com.utils.Helper
                .getStringFromProperties(com.utils.Helper.getHomeDir(new String[]{"config.properties"}),
                        "this.url") + "/amds-download-page?id=" + sheetId;
        final var localPath = System.getProperty("user.dir") + "/sample.xlsx";

        downloadFile(localPath, url, token);

        var columns = AmdsHelper.getColumns(sheetId);
        var fileData = ExcelHelper.getSpreadsheetData(localPath, List.of(columns.split(",")));

        var model = QueryHelper.getRowsModel(Integer.parseInt(sheetId)).getJSONArray(MESSAGE);
        var query = AmdsHelper.getSheetQuery(sheetId, QueryHelper.getIdByToken(token));
        var dbData = DataSort
                .getSortedArray(QueryHelper.getData(query, PULL_TABLE).getJSONArray(MESSAGE), model);

        testData(fileData, dbData, model);

        var updatedData = TestHelper.updateArray(model, fileData, List.of(columns.split(",")));
        var savedFile = ExcelHelper.saveExcel(updatedData, sheetId, managedId);
        System.out.println("PATH: " + savedFile);
        var uploadUrl = com.utils.Helper
                .getStringFromProperties(com.utils.Helper.getHomeDir(new String[]{"config.properties"}),
                        "this.url") + "/amds-upload?id=" + sheetId;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);

            FileUpload.sendFile(uploadUrl, savedFile, headers);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        DupeList.unload();
        downloadFile(localPath, url, token);
        fileData = ExcelHelper.getSpreadsheetData(localPath, List.of(columns.split(",")));
        testData(fileData, updatedData, model);

    }

    @Test
    public void totalExcelTest() {
        var path = "C:/Users/Maxim.Karpov/IdeaProjects/testshmestservice/total.xlsx";

        ExcelHelper.saveMassiveExcel(path);

    }

    @Test
    public void nonAdminTest() throws JSONException {
        var token = new JSONObject(HelperTest.getTokenTest("test@test.com", "baraban"))
                .getString("token");
        var sheetId = "6";

        var managedToken = "";
//                new JSONObject(HelperTest.getTokenTest("test@test.com", "baraban"))
//                .getString("token");

        var managedId = QueryHelper.getIdByToken(managedToken);

        final String url = com.utils.Helper
                .getStringFromProperties(com.utils.Helper.getHomeDir(new String[]{"config.properties"}),
                        "this.url") + "/amds-download-page?id=" + sheetId;
        final var localPath = System.getProperty("user.dir") + "/sample.xlsx";
        //  downloadFile(localPath, url, token);

        downloadFile(localPath, url, token);
        var columns = AmdsHelper.getColumns(sheetId);
        var fileData = ExcelHelper.getSpreadsheetData(localPath, List.of(columns.split(",")));

        var model = QueryHelper.getRowsModel(Integer.parseInt(sheetId)).getJSONArray(MESSAGE);
        var query = AmdsHelper.getSheetQuery(sheetId, QueryHelper.getIdByToken(token));
        var dbData = DataSort
                .getSortedArray(QueryHelper.getData(query, PULL_TABLE).getJSONArray(MESSAGE), model);

        testData(fileData, dbData, model);
        var random = new Random();
        var updatedData = (random.nextBoolean()) ?
                TestHelper.updateArray(model, fileData, List.of(columns.split(",")))
                : TestHelper.restoreArray(model, fileData, List.of(columns.split(",")));
        var savedFile = ExcelHelper.saveExcel(updatedData, sheetId, managedId);
        System.out.println("PATH: " + savedFile);
        var uploadUrl = com.utils.Helper
                .getStringFromProperties(com.utils.Helper.getHomeDir(new String[]{"config.properties"}),
                        "this.url") + "/amds-upload?id=" + sheetId;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("token", token);

            FileUpload.sendFile(uploadUrl, savedFile, headers);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }


    }

    private static void downloadFile(final String localPath, final String url, final String token) {
        File file = new File(localPath);
        if (file.exists()) {
            file.delete();
        }
        var downloaded = false;
        try {
            downloaded = FileDownload.downloadFile(url, localPath, token, "");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static JSONObject getObject(final String rowName, final JSONArray array) throws JSONException {
        for (var i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).getString(ROW_NAME).equals(rowName)) {
                return array.getJSONObject(i);

            }

        }
        return null;
    }

    private static void testData(final JSONArray fileData, final JSONArray dbData, final JSONArray model) throws JSONException {

        for (var i = 0; i < model.length(); i++) {

            var notInfo = model.getJSONObject(i).getString(INFO_ROW).equals("0");

            if (notInfo) {
                var fileObject = fileData.getJSONObject(i);
                var dbObject = dbData.getJSONObject(i);
                if (i == 91) {
                    System.out.println(i);

                }

                var keys = dbObject.keys();
                while (keys.hasNext()) {
                    var key = keys.next().toString();
                    if (!key.equals("user_id")) {
                        System.out.println(key + ": " + dbObject.getString(key) + " " + fileObject.getString(key));

                        Assert.assertEquals(cleanForSpecial(dbObject.getString(key)),
                                cleanForSpecial(fileObject.getString(key)), " error in line " + i + " for row: " + model.getJSONObject(i).getString(ROW_NAME));
                    }
                }
            }
        }
    }

    private static String cleanForSpecial(final String st) {
        var result = "";
        if (st.replace(" ", "").length() > 0) {
            var str = st.trim();
            var lastChar = str.charAt(str.length() - 1);

            if (!Character.isDigit(lastChar) && !Character.isAlphabetic(lastChar)
                    && lastChar != ':' && lastChar != '.' && lastChar != ')') {
                result = str.substring(0, str.length() - 1).trim();
            } else {
                result = str;
            }
        }

        return result;
    }
}
