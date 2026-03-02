package com.utils;


import org.apache.poi.openxml4j.util.ZipSecureFile;
import com.utils.data.QueryHelper;
import org.apache.poi.ss.util.CellRangeAddress;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.utils.PdfHelper.normalizeHeader;

public class FileReader {
    public static final String MESSAGE = "message";
    public static final String ROW_NAME = "row_name";
    public static final String INFO_ROW = "info_row";
    public static final List<String> DATE_COLUMNS = new ArrayList<>() {{
        add("date");
        add("observed");
        add("to");
        add("from");
    }};
    public static JSONObject persistExcel(final String fileName, final int sheetId, final String userId) throws IOException {
        ZipSecureFile.setMinInflateRatio(0.001);
        var array = new JSONArray();
        var rowModel = QueryHelper.getRowsModel(sheetId);

        if (rowModel.has(MESSAGE) && rowModel.getJSONArray(MESSAGE).length() > 0) {
            var model = rowModel.getJSONArray(MESSAGE);
            Workbook workbook = new XSSFWorkbook(fileName);
            Sheet sheet = workbook.getSheetAt(0);
            var columns = AmdsHelper.getColumns(Integer.toString(sheetId)).split(",");
            var stat = schemaOk(columns, sheet);
            if (!stat.isOk()) {
                throw new RuntimeException(stat.getMessage() + sheetId);
            }

            for (var i = 0; i < model.length(); i++) {

                var start = Integer.parseInt(stat.getMessage()) + i;
                    System.out.println();
                    if (model.getJSONObject(i).getString(ROW_NAME).startsWith("Preset protocol database."))
                        System.out.println();
                if (model.getJSONObject(i).getString(INFO_ROW).equals("0")) {
                    var rowName = model.getJSONObject(i).getString(ROW_NAME);

                    var row = sheet.getRow(start);
                    var j = 0;
                    var object = new JSONObject();
                    for (var col : columns) {

                        Cell cell = row.getCell(j);
                        var content = cell.toString();

                        if (Constants.DATE_COLUMNS.contains(col)) {
                            var format = DateHelper
                                                .formatOk(content);
                            content = format.getMessage();
                            if (!format.isOk()) {
                                throw new RuntimeException(format.getMessage());
                            }

                        }
                        object.put(col, content);

                         j++;
                    }

                    array.put(object);

                }

            }
            return new JSONObject() {{put(MESSAGE, array);}};
        } else {
            if (Constants.NO_MODEL_SHEETS.contains(Integer.toString(sheetId))) {
                var model = rowModel.getJSONArray(MESSAGE);
                Workbook workbook = new XSSFWorkbook(fileName);
                Sheet sheet = workbook.getSheetAt(0);
                var columns = AmdsHelper.getColumns(Integer.toString(sheetId)).split(",");
                var stat = schemaOk(columns, sheet);
                if (!stat.isOk()) {
                    throw new RuntimeException(stat.getMessage() + sheetId);
                }
                for (var i = 0; i < Constants.NO_MODEL_LINES.get(Integer.toString(sheetId)); i++) {
                    var row = sheet.getRow(Integer.parseInt(stat.getMessage()) + i);
                    var j = 0;
                    var object = new JSONObject();
                    for (var col : columns) {

                        Cell cell = row.getCell(j);
                        var content = cell.toString();

                        if (Constants.DATE_COLUMNS.contains(col)) {
                            var format = DateHelper
                                    .formatOk(content);
                            content = format.getMessage();
                            if (!format.isOk()) {
                                throw new RuntimeException(format.getMessage());
                            }

                        }
                        object.put(col, content);
                        j++;
                    }
                    array.put(object);
                }


                return new JSONObject() {{put(MESSAGE, array);}};
            }

        }
        return new JSONObject() {{put("message", "unknown table");}};
    }


    private static List<String> getNumberedColumns(final Row row, final List<String> columnNames) {

        List<String> columns = new ArrayList<>();
        for (var i = 0; i < columnNames.size(); i++) {
            final var cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            columns.add(cell.toString());

        }
        return columns;
    }
    public static List<String> getServiceColumns(final Row row) {
        List<String> links = new ArrayList<>();
        var i = 0;
        for (var cell : row) {
             links.add(cell.toString());
        }
        return links;
    }

    private static int getHeaderPositon(final Sheet sheet) {
        var position = -1;
        for (var row : sheet) {
            position++;
            for (var cell : row) {
                if (cell.toString().equals("info_row")) {
                    return position;
                }
            }

        }
        return position;
    }

    private static List<String> getTableColumns(final Sheet sheet) {
        var pos = getHeaderPositon(sheet);
        List<String> columns = new ArrayList<>();
  //      for (var row : sheet) {
        var row = sheet.getRow(pos);
            if (!row.getCell(0).toString().equals("row_name")) {
                row.getCell(0).setCellValue("row_name");
            }
            columns = getServiceColumns(row);
              //  break;
           // }
//        }
        return columns;
    }
    private static int getColspan(Sheet sheet, int rowIndex, int colIndex) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress region : mergedRegions) {
            if (region.isInRange(rowIndex, colIndex)) {
                return region.getLastColumn() - region.getFirstColumn() + 1;
            }
        }
        return 1;
    }

    private static int getRowspan(Sheet sheet, int rowIndex, int colIndex) {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress region : mergedRegions) {
            // Check if the given cell is part of the merged region
            if (region.isInRange(rowIndex, colIndex)) {
                // Return the number of rows the merged region spans
                return region.getLastRow() - region.getFirstRow() + 1;
            }
        }
        return 1; // No rowspan, the cell is not merged vertically
    }
    private static String getHtmnlHeader(final Sheet sheet) {
        var headPosition = getHeaderPositon(sheet);

        var htmlTable = new StringBuilder("<table class='table font-table'><thead class='table__thead'>");
        var i = 0;
        for (var row: sheet) {
            if (i < headPosition + 1) {
                htmlTable.append("<tr>");
                var j = 0;
                for (Cell cell : row) {
                    var content = cell.toString();
                    if (!content.equals("info_row")) {
                        var colspan = getColspan(sheet, i, cell.getColumnIndex());
                        var rowspan = getRowspan(sheet, i,  cell.getColumnIndex());

                        var th = "<th colspan='%d' rowspan='%d' style='padding:15px' class='%s'>";
                        var cspan = (j == 0) ? "table__th table__z" : "table__title";
                        if (Helper.isThing(cell.toString())) {
                            htmlTable.append(String.format(th, colspan, rowspan, cspan))
                                    .append(cell.toString()).append("</th>");
                        }


                    }
                    j++;
                }

                htmlTable.append("</tr>");
            }
            i++;
        }
        htmlTable.append("</thead></table>");
        return htmlTable.toString();
    }

    private static JSONObject preprocessHeader(final Sheet sheet) {
        var id = AmdsHelper.getNewSheetId();
        var html = getHtmnlHeader(sheet).replace("'", "\\'");
        var query = "insert into amds.preprocess_headers values(%d,'%s')";
        return QueryHelper.execute(String.format(query, id, html));
    }

    public static JSONArray getValuesFromExcel(final String fileLocation, final String[] columns) {
        ZipSecureFile.setMinInflateRatio(0.001);
        var usersArray = new JSONArray();

        try (Workbook workbook = new XSSFWorkbook(fileLocation)){

            Sheet sheet = workbook.getSheetAt(0);

            var stat = schemaOk(columns, sheet);
            if (!stat.isOk()) {
                throw new RuntimeException(stat.getMessage());
            }
            var i = 0;
            for (var row: sheet) {
                if (i > 0) {
                    var col = 0;
                    var rowObject = new JSONObject();
                    for (Cell cell: row) {
                        var val = cell.toString();
                        if (col < columns.length) {
                            rowObject.put(columns[col], val);
                        }
                        col++;
                    }
                    usersArray.put(rowObject);
                }
                i++;
            }
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException(e);
        }
        return usersArray;
    }

    public static String preprocessTable(final String fileName, final String tableName) throws IOException {
        ZipSecureFile.setMinInflateRatio(0.001);
        var array = new JSONArray();

        var result = Helper.getFailedObject();
        try {
            Workbook workbook = new XSSFWorkbook(fileName);
            Sheet sheet = workbook.getSheetAt(0);
            var headerStat = preprocessHeader(sheet);
            var headerPosition = getHeaderPositon(sheet);
            List<String> columnNames = getTableColumns(sheet);
            var infoRow = -1;
            for (var i = 0; i < columnNames.size(); i++) {
                if (columnNames.get(i).equals("info_row")) {
                    infoRow = i;
                    break;
                }
            }
            var j = 0;
            for (var row : sheet) {
                if (!row.getCell(0).toString().contains("row_name") && j > headerPosition) {
                    if (Helper.isThing(row.getCell(0).toString())) {
                        var obj = new JSONObject();
                        obj.put("row-name", row.getCell(0).toString().replace("'", "\\'"));
                        obj.put("info-row", row.getCell(infoRow).toString());
                        array.put(obj);
                    }

                }
                j++;
            }

            var cols = String.join(",", columnNames).toLowerCase().replace(" ", "_");
            var query = AmdsHelper.createPreprocessQuery(tableName, cols, array);

            result = QueryHelper.execute(query);
            result.put("header", headerStat);


        } catch (IOException e) {
            return result.toString();
        }


        return result.toString();
    }
    public static JSONObject readTable(final String fileName) throws IOException {
        ZipSecureFile.setMinInflateRatio(0.001);
        var array = new JSONArray();
        var result = new JSONObject();
        Workbook workbook = new XSSFWorkbook(fileName);
        Sheet sheet = workbook.getSheetAt(0);

        List<String> scopeColumns = new ArrayList<>();
        List<String> linkedColumns = new ArrayList<>();
        List<String> typeColumns = new ArrayList<>();
        List<String> savedMappingColumns = new ArrayList<>();
        List<String> columnNames = getTableColumns(sheet);
        var columnObjects = new JSONArray();
        var rowNames = new JSONArray();

        var i = 0;
        for (var row : sheet) {


            if (i == 0 && row.getCell(0).toString().contains("mappings")) {
                linkedColumns = getNumberedColumns(row, columnNames);
            } else if (i == 1 && row.getCell(0).toString().contains("scope")) {
                scopeColumns = getNumberedColumns(row, columnNames);
            } else if (i == 2 && row.getCell(0).toString().contains("type")) {
                typeColumns = getNumberedColumns(row, columnNames);
            } else if (i == 3 && row.getCell(0).toString().contains("saved-mappings")) {
                savedMappingColumns = getNumberedColumns(row, columnNames);
            } else if (row.getCell(0).toString().contains("row_name")) {
                columnNames = getServiceColumns(row);
                var count = 0;
                for (var col : columnNames) {
                    var obj = new JSONObject();
                    obj.put("name", col);
                    var linked = (!linkedColumns.isEmpty()) ? linkedColumns.get(count) : "";
                    var scoped = (!scopeColumns.isEmpty()) ? scopeColumns.get(count) : "";
                    var typed = (!typeColumns.isEmpty()) ? typeColumns.get(count) : "";
                    obj.put("mappings", linked);
                    obj.put("scoped", scoped);
                    obj.put("type", typed);
                    obj.put("saved-mappings", savedMappingColumns);
                    columnObjects.put(obj);
                    count++;
                }
            }
            else {

                var rowName = row.getCell(0);
                var rowObject = new JSONObject();
                rowObject.put("row_name", rowName);
                rowNames.put(rowName);
                var j = 0;
                var object = new JSONObject();
                for (var cell : row) {

                    var content = cell.toString();
                    if (columnObjects.getJSONObject(j).getString("name").equals("info_row")) {
                        object.put("info_row", content);
                    }

                    j++;
                }

                array.put(object);


            }

            i++;
        }

        result.put("columns", columnObjects);
        result.put("rows", array);
        return result;

    }


    public static UsefulBoolean schemaOk(final String[] columns, final Sheet sheet) {

        var error = "";
        var usefuleRow = 0;
        for (var row : sheet) {
            if (row.getCell(0).toString().equals(ROW_NAME)) {
                for (var column : columns) {
                    var col = 0;
                    var found = false;
                    while (row.cellIterator().hasNext()) {
                        if (row.getCell(col).toString().equals(column)) {
                            found = true;
                            break;
                        }
                        col++;
                    }
                    if (!found) {
                        error = "column ? is not found in the spreadsheet ";
                        return new UsefulBoolean(false, error);

                    }
                }
                usefuleRow++;
            }

        }
        return new UsefulBoolean(true, Integer.toString(usefuleRow));

    }

    private static String parseExcelTableName(final String name, final String id) {
        var regex = "files/uploads/(.*?)_" + id + ".xlsx$";
        return normalizeHeader(Helper.parseStringWithRegex(regex, name, 1), id);
    }

}
