package com.utils;

import com.utils.data.HttpClient;
import com.utils.data.QueryHelper;
import com.utils.command.JsonHelper;
import com.utils.excel.DupeList;
import com.utils.excel.ExcelHelper;
import com.utils.test.DataSort;
import org.apache.commons.io.FileUtils;

import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.utils.DateHelper.DATE_FORMAT;

public class AmdsHelper {
    public static final String PULL_LIST = "pull-list";
    public static final String PULL_STRING = "pull-string";
    public static final String PULL_TABLE = "pull-table";
    public static final String MESSAGE = "message";
    public static final String ROW_NAME = "row_name";
    public static final String TEMPLATE = "template";
    public static final String EXTENSION = ".xlsx";
    private static final String INFO_ROW = "info_row";

    public static String getTableName(final String tableId) {
        final String query = "select name from amds.sheets where id='" + tableId + "'";
        var m = QueryHelper.getData(query, PULL_STRING);
        return normalizeTable(QueryHelper.getData(query, PULL_STRING).getString(MESSAGE));
    }

    public static String getColumns(final String tableId) {
        final String query = "select properties from amds.sheets where id='" + tableId + "'";
        return "row_name," + QueryHelper.getData(query, PULL_STRING).getString(MESSAGE);
    }


    private static String normalizeTable(final String name) {
        return name.toLowerCase().replace(" - ", "_")
                .replace(" ", "_")
                .replace("(", "_")
                .replace(")", "_");

    }

    public static final String QUERY_TEMPLATE = "select ? from amds.? where user_id='?'";
    public static final String QUERY_TEMPLATE_ALL = "select ? from amds.?";
    public static final String SHEET_QUERY_TEMPLATE = "select ? from amds.? where id='?' user_id='?'";
    public static final String CREATE_QUERY_TEMPLATE = "insert into amds.? (?,user_id) values(<q>)";
    public static final String ALL_SHEETS_QUERY = "select id,name from amds.sheets where id>2";
    public static final String QUESTION_MASK = "\\?";

    public static String getSheetQuery(final String sheetId, final String userId) {
        var columns = getColumns(sheetId) + ",user_id";
        var table = getTableName(sheetId);
        return QUERY_TEMPLATE.replaceFirst(QUESTION_MASK, columns)
                .replaceFirst(QUESTION_MASK, table)
                .replaceFirst(QUESTION_MASK, userId);
    }



    public static String getUserIdByEmail(final String email) throws Exception {
        var url = Helper.getUrl("user.url") + "/id-by-email?email=" + email;
        var secret = "7js dj^ skdjsd  ds8888dssd!!!";
        Map<String,String> map = new HashMap<>();
        map.put("secret", secret);
        return HttpClient.sendGet(url, map).getString("message");
    }

    private static List<String> getListFromString(final String str) {
        return List.of(str.split(","));
    }

    private static String deductList(final String shortStr, final String longStr) {
        var shortList = getListFromString(shortStr);
        var longList = getListFromString(longStr);

        var builder = new StringBuilder();

        longList.stream().filter(col -> !shortList.contains(col)).forEach(col -> builder.append(col).append(","));
        return builder.toString().substring(0, builder.toString().length() - 1);
    }

    public static int validUsers() {
        var ignored = QueryHelper.getIgnored().size();
        var users = QueryHelper.getUsers().length();
        return users - ignored;
    }
    public static JSONArray getTableStats(final boolean reverse) {
        var result = new JSONArray();
        var tables = getAllTables();
        for (var i = 0; i < tables.length(); i++) {
            var users = getNumberOfUsers(tables.getJSONObject(i).getString("name"));
            var table = new JSONObject();
            table.put("name", tables.getJSONObject(i).getString("name"));
            if (!reverse) {
                table.put("users", users);
            } else {

                table.put("users", validUsers() - users);
            }

            result.put(table);
        }
        return result;
    }
//dummy@user.com
    private static boolean deleteUserFromTable(final int userId, final String tableName) {
        var query = String.format("delete from amds.%s where user_id=%d", tableName, userId);
        var stat = QueryHelper.getData(query, "execute");
        return stat.has(MESSAGE) && stat.getString(MESSAGE).equals("success");
    }

    public static List<UsefulBoolean> deleteTables(final int userId) {
        List<UsefulBoolean> statuses = new ArrayList<>();
        var tables = getAllTables();
        for(var i = 0; i < tables.length(); i++) {
            var tableName = tables.getJSONObject(i).getString("name");
            var status = deleteUserFromTable(userId, tableName);
            statuses.add(new UsefulBoolean(status, tableName));
        }
        return statuses;
    }

    public static List<UsefulBoolean> reassignTablesToNewUser(final int newUserId, final int oldUserId) {
        List<UsefulBoolean> statuses = new ArrayList<>();
        var tables = getAllTables();
        for(var i = 0; i < tables.length(); i++) {
            var tableName = tables.getJSONObject(i).getString("name");
            var status = reassignTableToUser(newUserId, oldUserId, tableName);
            statuses.add(new UsefulBoolean(status, tableName));
        }
        return statuses;
    }

    private static boolean reassignTableToUser(final int newUserId, final int oldUserUd, final String tableName) {
        var query = String.format("update amds.%s set user_id=%d where user_id=%d", tableName, newUserId, oldUserUd);
        var stat = QueryHelper.getData(query, "execute");
        return stat.has(MESSAGE) && stat.getString(MESSAGE).equals("success");

    }

    public static void main(String[] args) {
        reassignTableToUser(2500,2300, "ct_clinical_skills");
    }

    private static int getNumberOfUsers(final String table) {
        var query = "select user_id from amds.";
        var rows = QueryHelper.getData(query + table, PULL_LIST).getJSONArray(MESSAGE);
        List<String> users = new ArrayList<>();
        for (var i = 0; i < rows.length(); i++) {
            var user = rows.getString(i);
            if (!users.contains(user)) {
                users.add(user);
            }
        }
        return users.size();
    }

    public static int getMaxTemplate() {
        var query = "select max(seq) from amds.sections";
        var result = QueryHelper.getData(query, PULL_STRING);
        if (result.has(MESSAGE)) {
            return Integer.parseInt(result.getString(MESSAGE)) + 1;
        } else {
            return 0;
        }
    }
    public static JSONObject setSection(final int id, final int seq, final String template) {
        var html = template.replace("\\", "");
        var selectQuery = String.format("select id from amds.sections where id=%d", id);
        var sequence = (seq < 0) ? getMaxTemplate() : seq;
        var query = String.format("insert into amds.sections values(%d,%d,'%s')", id, sequence, html);
        System.out.println(query);
        var obj = QueryHelper.getData(selectQuery, PULL_TABLE);
        if(obj.has( MESSAGE) && !obj.getJSONArray(MESSAGE).isEmpty()) {
            query = String.format("update amds.sections set seq=%d, template='%s' where id=%d", sequence, html, id);
        }
        return QueryHelper.getData(query, "execute");
    }


    public static JSONObject getTemplate(final int id) {
        var result = Helper.getFailedObject();
        var query = String.format("select template from amds.templates where id=%d", id);
        var htmlObj = QueryHelper.getData(query, PULL_STRING);
        if (htmlObj.has(MESSAGE)) {
            var document = Jsoup.parse(htmlObj.getString(MESSAGE));

        }
        return result;
    }

    public static JSONObject getLayout(final int id) {
        var result = Helper.getFailedObject();
        var query = String.format("select template from amds.layouts where id=%d", id);
        var htmlObj = QueryHelper.getData(query, PULL_STRING);
        if (htmlObj.has(MESSAGE)) {
            result = htmlObj;
        }
        return result;
    }
    public static JSONArray getSections() {
        var array = new JSONArray();
        var resp = QueryHelper.getData("select id,seq,template from amds.sections order by seq", PULL_TABLE);
        if (resp.has(MESSAGE)) {
            array = resp.getJSONArray(MESSAGE);
        }
        return array;
    }
    public static JSONObject setTemplate(final int id, final int layout, final String template) {
        var html = template.replace("\\", "");
        var selectQuery = String.format("select id from amds.templates where id=%d", id);

        var query = String.format("insert into amds.templates values(%d,%d,'%s')", id, layout, html);
    
        var obj = QueryHelper.getData(selectQuery, PULL_TABLE);
        if(obj.has( MESSAGE) && !obj.getJSONArray(MESSAGE).isEmpty()) {
            query = String.format("update amds.templates set template='%s' where id=%d", html, id);
        }
        return QueryHelper.getData(query, "execute");
    }

    public static JSONObject setLayot(final int id, final String name, final String layout) {
        var html = layout.replace("\\", "");
        var selectQuery = String.format("select id from amds.layouts where id=%d", id);

        var query = String.format("insert into amds.layouts values(%d,'%s','%s')", id, html, name);

        var obj = QueryHelper.getData(selectQuery, PULL_TABLE);
        if(obj.has( MESSAGE) && !obj.getJSONArray(MESSAGE).isEmpty()) {
            query = String.format("update amds.layouts set template='%s' where id=%d", html, id);
        }
        return QueryHelper.getData(query, "execute");
    }
    public static String getAdminColumns(final String sheetId) {
        final var query = "select user from amds.sheets where id=" + sheetId;
        var cols = QueryHelper.getData(query, PULL_STRING);
        var colString = (cols.has(MESSAGE)) ? cols.getString(MESSAGE) : "";
        return deductList(colString, getColumns(sheetId)).replace("row_name,", "");
    }
    public static UsefulBoolean adminColumnsIntact(final JSONArray newTable,
                                                   final String sheetId, final String userId) {
        var adminCols = "row_name," + AmdsHelper.getAdminColumns(sheetId);
        var sheetName = getTableName(sheetId);
        var selQuery = "select " + adminCols + " from amds." + sheetName + " where user_id='" + userId + "'";
        var oldTable = QueryHelper.getData(selQuery, PULL_TABLE).getJSONArray(MESSAGE);

        for (var i = 0; i < newTable.length(); i++) {
            for (var key : newTable.getJSONObject(i).keySet()) {
                if (oldTable.getJSONObject(i).has(key)) {
                    var oldCol = oldTable.getJSONObject(i).getString(key);
                    var newCol = newTable.getJSONObject(i).getString(key);
                    //    if (!TestHelper.appxEqual(oldCol, newCol)) {
                    if (!oldTable.getJSONObject(i).getString(key).equals(newTable.getJSONObject(i).getString(key))
                            && List.of(adminCols.split(",")).contains(key)) {

                        System.out.println("OLD TABLE:");
                        System.out.println();
                        System.out.print(oldTable.getJSONObject(i).toString(5));
                        System.out.println("NEW TABLE:");
                        System.out.println();
                        System.out.print(newTable.getJSONObject(i).toString(5));
                        System.out.println(i);

                        return new UsefulBoolean(false, "row: " + i + " column: " + key + " new value: "
                                + newTable.getJSONObject(i).getString(key));
                    }
                } else if (List.of(adminCols.split(",")).contains(key)
                        && Helper.isThing(newTable.getJSONObject(i).getString(key))) {
                    return new UsefulBoolean(false, "row: " + i + " column: " + key);
                }
            }
        }
        return new UsefulBoolean(true, "");

    }


    private static boolean entryExistButShouldNot(final String column, final String value, final String sheetName) {
        final List<String> booleanSheets = new ArrayList<>() {{
            add("5");
            add("13");
        }};
        if (column.equals(ROW_NAME)) {
            return false;
        } else if (booleanSheets.contains(sheetName)) {
            return value.equals("y");
        } else {
            return Helper.isThing(value);
        }
    }

    public static UsefulBoolean adminColumnsIntact(final JSONArray oldTable,
                                                   final JSONArray newTable,
                                                   final String adminColumns, final String sheetId) {
        TestHelper.sleep(10);
        var adminCols = adminColumns.replace("row_name,", "").replace("row_name", "");



        for (var i = 0; i < newTable.length(); i++) {
            for (var key : newTable.getJSONObject(i).keySet()) {
                if (oldTable.length() > i && oldTable.getJSONObject(i).has(key)) {
                    if (!oldTable.getJSONObject(i).getString(key).equals(newTable.getJSONObject(i).getString(key))
                            && List.of(adminCols.split(",")).contains(key)) {
                        return new UsefulBoolean(false, "row: " + i + " column: " + key + " new value: "
                                + newTable.getJSONObject(i).getString(key));
                    }
                } else if (List.of(adminCols.split(",")).contains(key)
                        && entryExistButShouldNot(key, newTable.getJSONObject(i).getString(key), sheetId)
                    //  Helper.isThing(newTable.getJSONObject(i).getString(key))
                ) {
                    return new UsefulBoolean(false, "row: " + i + " column: " + key);
                }
            }
        }
        return new UsefulBoolean(true, "");
    }


    public static String getSheetQuery(final String sheetId) {
        var columns = getColumns(sheetId) + ",user_id";
        var table = getTableName(sheetId);
        return QUERY_TEMPLATE_ALL.replaceFirst(QUESTION_MASK, columns)
                .replaceFirst(QUESTION_MASK, table);
    }

    public static String getAmdsFilesPath(final String name) {
        return TestHelper.getSameLevelProject("files") + "/" + name + ".xlsx";
    }

    public static String getAmdsFilesPathByName(final String name, final String userId) {
        return TestHelper.getSameLevelProject("files") + "/"
                + name
                + "_"
                + userId
                + EXTENSION;
    }
    public static String getAmdsFilesPathForDownload(final String name, final String userId) {
        return TestHelper.getSameLevelProject("files/download") + "/"
                + name
                + "_"
                + userId
                + EXTENSION;
    }

    public static List<String[]> getUsers() {
        List<String[]> result = new ArrayList<>();
        var path = Helper.getCurrentDir() + "\\src\\main\\resources\\users.csv";

        var file = new File(path);
        try {
            var lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            var j = 0;
            for (var line:lines) {
                if (j != 0) {
                    var first = "";
                    var last = "";

                    var email = "";
                    var i = 0;
                    var columns = line.split(",");
                    for (var column:columns) {
                        if (i == 0) {
                            first = first + column + " ";
                        } else if (i == 1) {
                            first = first + column;
                        } else if (i == 2) {
                            last = column;
                        } else {
                            email = column;
                        }
                        i++;
                    }

                    var arr = new String[] {first, last, email};
                    result.add(arr);

                }

                j++;
            }


        } catch (IOException e) {
            System.out.println(e);
        }

        return result;
    }

    public static List<String> getAllQueries(final String userId) {
        List<String> queries = new ArrayList<>();
        var query = "";
        return queries;
    }



    public static String saveSheet(final String id, final String userId) {
        var query = getSheetQuery(id, userId);
        if (id.equals("21")) {
            System.out.println("");

        }
        var data = QueryHelper.getData(query, PULL_TABLE);
        return saveSheetByUser(id, data, userId);
    }


    public static String saveSheetByUser(final String id, final JSONObject data, final String userId) {
        final var name = getTableName(id);
        if (name.toLowerCase().contains("skills")) {
            System.out.println(name);

        }
        var userName = (Helper.isThing(userId)) ? QueryHelper.getNameById(userId) : null;
        List<String> columns = new ArrayList<>(List.of(getColumns(id).split(",")));
        columns.add("user_id");
        var dataArray = data.getJSONArray("message");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(name);

        Row header = sheet.createRow(0);
        var ci = 0;
        for (var col: columns) {
            Cell head = header.createCell(ci);

            head.setCellValue(col);

            ci++;
        }

//        Cell uHead = header.createCell(ci + 1);
//        uHead.setCellValue("user-id");


        for (var i = 0; i < dataArray.length(); i++) {
            Row row = sheet.createRow(i + 1);
            var object = dataArray.getJSONObject(i);
            var count = 0;
            for (var column:columns) {
                Cell cell = row.createCell(count);
                var value = (object.has(column)) ? object.getString(column) : "";
                if (value.equals("on")) {
                    value = " ";
                } else if (value.equals("y")) {
                    value = "yes";

                }
                if (count == (columns.size() - 1)) {
                    value = QueryHelper.getNameById(value);
                }
                cell.setCellValue(value);
                count++;
            }

        }
        var path = getAmdsFilesPathByName(name, userId);
        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            workbook.write(outputStream);
            System.out.println("Excel file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    private static String getCelContent(final String modelRowName,
                                        final JSONArray table,
                                        final String column,
                                        final boolean info) {
        var content = "";
        var found = 0;
        boolean dupe = DupeList.isDupe(modelRowName);
        if (dupe) {
            found++;
        }

        var met = DupeList.howManyMet(modelRowName);
        if (column.equals(ROW_NAME)) {
            content = modelRowName;
            DupeList.dupeMet(modelRowName);

        } else if (!info) {
            outerLoop:
            for (var i = 0; i < table.length(); i++) {
                var row = table.getJSONObject(i);
                var rowName = row.getString(ROW_NAME);

                var toProcess = !dupe || (found == met);

                //check if the current row is what we need.
                if (rowName.equals(modelRowName)) {
                    //skip if already met
                    if (toProcess) {
                        for (var col : row.keySet()) {
                            if (col.equals(column)) {
                                content = row.getString(col);
                                break outerLoop;
                            }
                        }

                    }
                    found++;
                }

            }
        }

        return content;
    }

    private static String getContent(final JSONObject row, final String column) {
        try {
            return row.getString(column);
        } catch (JSONException e) {
            return "";
        }

    }

    private static void applyDateValidationToColumn(Sheet sheet, final int rowCount, int columnIndex) {

        CellRangeAddressList dateRange = new CellRangeAddressList(1, rowCount, columnIndex, columnIndex);

        XSSFDataValidationConstraint dateConstraint = (XSSFDataValidationConstraint)
                sheet.getDataValidationHelper().createDateConstraint(
                        DataValidationConstraint.OperatorType.BETWEEN,
                        "1900-01-01",
                        "9999-12-31", DATE_FORMAT);

        DataValidation dataValidation = sheet.getDataValidationHelper().createValidation(dateConstraint, dateRange);
        dataValidation.setShowErrorBox(true);
        sheet.addValidationData(dataValidation);
    }

    private static List<Integer> getDateColumns(final List<String> columns) {
        var i = 0;
        List<Integer> dateColumns = new ArrayList<>();
        for (var column : columns) {
            if (Constants.DATE_COLUMNS.contains(column)) {
                dateColumns.add(i);
            }
            i++;
        }
        return dateColumns;
    }

    private static void setDataCells(Sheet sheet, final List<String> columns, final int rowsCount) {
        var dateColumns = getDateColumns(columns);
        var validationHelper = sheet.getDataValidationHelper();
        for (var i : dateColumns) {
            applyDateValidationToColumn(sheet, rowsCount, i);
        }
    }

    /**
     * This method saves page data into excel file
     * @param id String sheetId
     * @param data - JSONArray of sheet data
     * @param managerId - String userId
     */
    public static void saveUserSheet(final String id, final JSONObject data, final String managerId) {
        final var name = getTableName(id);
        final var isAdmin = QueryHelper.isAdmin(managerId);
        List<String> columns = new ArrayList<>(List.of(getColumns(id).split(",")));
        var rowModel = QueryHelper.getRowsModel(Integer.parseInt(id)).getJSONArray(MESSAGE);
        checkModelForeDupes(rowModel);
        var dateColumns = getDateColumns(columns);
        columns.add("user_id");
        var dataArray = data.getJSONArray("message");
        var fullArray = DataSort.getSortedArray(dataArray, rowModel);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(name);
        sheet.protectSheet("aksjdwdkz28");
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));
        CellStyle infoCellStyle = workbook.createCellStyle();
        infoCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        infoCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle unlockedCellStyle = workbook.createCellStyle();
        unlockedCellStyle.setLocked(false);
        // setDataCells(sheet, columns, rowModel.length());
        var adminColumns = List.of(getAdminColumns(id).split(","));

        Row header = sheet.createRow(0);
        var ci = 0;
        for (var col: columns) {
            Cell head = header.createCell(ci);
            head.setCellStyle(headerCellStyle);
            head.setCellValue(col);

            ci++;
        }



        for (var i = 0; i < rowModel.length(); i++) {

            var rowName = rowModel.getJSONObject(i).getString(ROW_NAME);


            var info = rowModel.getJSONObject(i).getString(INFO_ROW).equals("1");
            var special = rowModel.getJSONObject(i).getString(INFO_ROW).equals("2");
            Row row = sheet.createRow(i + 1);
            var count = 0;
            LocalDate dateContent = null;
            for (var col : columns) {

                // var content = getCelContent(rowName, dataArray, col, info);
                var content = getContent(fullArray.getJSONObject(i), col);
                Cell cell = row.createCell(count);
                if (info) {
                    cell.setCellStyle(ExcelHelper.Styles.getInfoCellStyle(workbook));
                    cell.setCellValue(content);
                } else if (special) {
                    cell.setCellStyle(ExcelHelper.Styles.getSpecialCellStyle(workbook));
                    cell.setCellValue(content);

                } else if (dateColumns.contains(count)) {
                    if (Helper.isThing(content))
                        dateContent = DateHelper.parseDate(content);
                    cell.setCellStyle(ExcelHelper.Styles.getDateCellStyle(workbook));
                    cell.setCellValue(dateContent);
                } else {
                    dateContent = null;
                    cell.setCellValue(content);
                }




                if (isAdmin || info || !adminColumns.contains(col) || col.equals(ROW_NAME)) {

                    if (dateContent != null) {
                        cell.setCellStyle(ExcelHelper.Styles.getUnlockedDateCellStyle(workbook));
                    } else {
                        cell.setCellStyle(unlockedCellStyle);
                    }
                    if (info)
                        cell.setCellStyle(ExcelHelper.Styles.getInfoCellStyle(workbook));
                    if (special)
                        cell.setCellStyle(ExcelHelper.Styles.getSpecialCellStyle(workbook));
                }
                count++;
            }



        }
        LocalDate dateContent = null;
        if (Constants.NO_MODEL_SHEETS.contains(id)) {
            for (var i = 0; i < data.getJSONArray(MESSAGE).length(); i++) {
                var rowObject = data.getJSONArray(MESSAGE).getJSONObject(i);
                var row = sheet.createRow(i + 1);
                var j = 0;
                for (var col : columns) {
                    var content = getContent(rowObject, col);
                    Cell cell = row.createCell(j);
                    if (dateColumns.contains(j)) {
                        if (Helper.isThing(content))
                            dateContent = DateHelper.parseDate(content);
                        cell.setCellStyle(ExcelHelper.Styles.getDateCellStyle(workbook));
                        cell.setCellValue(dateContent);
                    } else {
                        dateContent = null;
                        cell.setCellValue(content);
                    }




                    if (isAdmin || !adminColumns.contains(col)) {

                        if (dateContent != null) {
                            cell.setCellStyle(ExcelHelper.Styles.getUnlockedDateCellStyle(workbook));
                        } else {
                            cell.setCellStyle(unlockedCellStyle);
                        }

                    }
                    j++;
                }

            }



        }

        for (var l = 0; l < columns.size(); l++) {
            sheet.autoSizeColumn(l);
        }



        try (FileOutputStream outputStream = new FileOutputStream(getAmdsFilesPath(name))) {
            workbook.write(outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * *
     * this method populate a list of duplicates with the number of dupes for each entry
     * @see DupeList class with static list of dupes with number of repetitions for each dupe
     * @param model JSONArray containing the compentency table model to check
     */
    private static void checkModelForeDupes(final JSONArray model) {
        DupeList.unload();
        for (var i = 0; i < model.length(); i++) {
            var rowName = model.getJSONObject(i).getString(ROW_NAME);
            var infoRow = model.getJSONObject(i).getString(INFO_ROW).equals("1");

            if (!infoRow)
                DupeList.checkEntry(rowName);
        }
    }

    public static void saveSheet(final String id, final JSONObject data, final String userId) {
        final var name = getTableName(id);
        var userName = (Helper.isThing(userId)) ? QueryHelper.getNameById(userId) : null;
        List<String> columns = new ArrayList<>(List.of(getColumns(id).split(",")));
        var dateColumns = getDateColumns(columns);
        columns.add("user_id");
        var dataArray = data.getJSONArray("message");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(name);
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));
        Row header = sheet.createRow(0);
        var ci = 0;
        for (var col: columns) {
            Cell head = header.createCell(ci);

            head.setCellValue(col);

            ci++;
        }

        Cell uHead = header.createCell(ci + 1);
        uHead.setCellValue("user-id");


        for (var i = 0; i < dataArray.length(); i++) {
            Row row = sheet.createRow(i + 1);
            var object = dataArray.getJSONObject(i);
            var count = 0;
            for (var column:columns) {
                Cell cell = row.createCell(count);
                var value = (object.has(column)) ? object.getString(column) : "";
                if (count == (columns.size() - 1)) {
                    value = QueryHelper.getNameById(value);
                }
                if (dateColumns.contains(count)) {
                    cell.setCellStyle(dateCellStyle);

                }
                cell.setCellValue(value);
                count++;
            }

        }

        try (FileOutputStream outputStream = new FileOutputStream(getAmdsFilesPath(name))) {
            workbook.write(outputStream);
            System.out.println("Excel file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private static String maskColumns(final String columns) {
        var parts = columns.split(",");
        var builder = new StringBuilder();
        List.of(parts).forEach(str -> builder.append("'?'").append(","));
        return builder.toString() + "'?'";
    }

    public static String createSheetPopulateQuery(final String sheetId,
                                                  final String userId,
                                                  final JSONArray data) {
        var sheetName = getTableName(sheetId);
        var columns = getColumns(sheetId);
        var cols = List.of(columns.split(","));
        var name = userId;

        var query = "insert into amds." + sheetName + "(" + columns + ",user_id) values ";
        for (var i = 0; i < data.length(); i++) {
            var row = "(";
            var j = 0;

            for (var column : cols) {
                var rw = data.getJSONObject(i);
                var tp = rw.get(column) instanceof String;
                var value =  data.getJSONObject(i).getString(column).replace('\uF06E', '\0')
                        .replace("'", "''");
                row = row + "'" + value + "',";

                j++;
            }
            row = row + "'" + name + "')";
            if (i < data.length() - 1) {
                row = row + ",";

            }
            query = query + row;
        }
        return query;
    }
    private static JSONObject getTestObject(List<String> cols, String num) {
        var object = new JSONObject();
        for (var col : cols) {
            object.put(col, col + " " + num);
        }
        return object;
    }


    public static String getCreateSheetQuery(final String sheetId, final String userId, final JSONObject row) {
        var columns = getColumns(sheetId);
        var table = getTableName(sheetId);
        var query = CREATE_QUERY_TEMPLATE.replace("<q>", maskColumns(columns))
                .replaceFirst(QUESTION_MASK, table)
                .replaceFirst(QUESTION_MASK, columns);
        for (var col : columns.split(",")) {
            query = query.replaceFirst(QUESTION_MASK, row.getString(col));
        }
        query = query.replaceFirst(QUESTION_MASK, userId);
        return query;
    }

    public static String enrichColumns(final String columns) {
        var builder = new StringBuilder();
        for (var column : columns.split(",")) {
            if (!column.contains("comment") && !column.contains("detail")) {
                builder.append(normalizeColumn(column) + " varchar(100)").append(",");
            } else {
                builder.append(normalizeColumn(column) + " varchar(1000)").append(",");
            }
        }
        return builder.toString() + "user_id varchar(45)";
    }

    public static String normalizeColumn(final String column) {
        var col = column.trim();
        return col.replace(" ", "_");
    }

    public static JSONArray getAllTables() {

        var array = QueryHelper.getData(ALL_SHEETS_QUERY, "pull-table").getJSONArray("message");
        var result = new JSONArray();
        for (var i = 0; i < array.length(); i++) {
            var obj = array.getJSONObject(i);
            var newObject = new JSONObject();
            newObject.put("name", normalizeTable(obj.getString("name")));
            newObject.put("id", obj.getString("id"));
            result.put(newObject);
        }

        return result;
    }


    public static void createSheetsTables() {
        final String namesQuery = "select name from amds.sheets";
        final String colsQuery = "select properties from amds.sheets";
        var names = JsonHelper
                .getListFromJsonArray(QueryHelper.getData(namesQuery, PULL_LIST).getJSONArray(MESSAGE));
        var columnList = JsonHelper
                .getListFromJsonArray(QueryHelper.getData(colsQuery, PULL_LIST).getJSONArray(MESSAGE));;

        for (var i = 0; i < names.size(); i++) {
            var name = names.get(i);
            var columns = columnList.get(i);

            if (!name.equals("MRI Clinical Skills")) {
                var query = "create table amds." + normalizeTable(name) + "(row_name varchar(300)," + enrichColumns(columns) + ")";
                var status = QueryHelper.getData(query, "execute");
                if (status.getString(MESSAGE).contains("SQL syntax")) {
                    System.out.println(name + "::::::::::" + status);

                }

            }

        }

    }
}