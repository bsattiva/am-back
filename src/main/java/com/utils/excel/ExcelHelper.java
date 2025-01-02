package com.utils.excel;

import com.utils.AmdsHelper;
import com.utils.Constants;
import com.utils.Helper;
import com.utils.data.QueryHelper;
import com.utils.test.DataSort;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {
    public static final Logger LOGGER = Logger.getLogger(ExcelHelper.class);
    private static final String ROW_NAME = "row_name";
    private static final String INFO_ROW = "info_row";
    private static final String MESSAGE = "message";
    private static final String MASK = "\\?";


    public static class Styles {
        private static CellStyle dateCellStyle;
        private static CellStyle infoCellStyle;
        private static CellStyle headerCellStyle;
        private static CellStyle unlockedDateCellStyle;
        private static CellStyle specialCellStyle;

        public static CellStyle getDateCellStyle(final Workbook workbook) {
                dateCellStyle = workbook.createCellStyle();
                dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(Constants.DATE_FORMAT ));
            return dateCellStyle;
        }

        public static CellStyle getUnlockedDateCellStyle(final Workbook workbook) {

                unlockedDateCellStyle = workbook.createCellStyle();
                unlockedDateCellStyle
                        .setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(Constants.DATE_FORMAT ));
                unlockedDateCellStyle.setLocked(false);

            return unlockedDateCellStyle;
        }

        public static CellStyle getInfoCellStyle(final Workbook workbook) {
            CellStyle infoCellStyle = workbook.createCellStyle();
            infoCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            infoCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            return infoCellStyle;
        }

        public static CellStyle getSpecialCellStyle(final Workbook workbook) {
            specialCellStyle = workbook.createCellStyle();
            specialCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            specialCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            return specialCellStyle;
        }
        
        private static CellStyle getHeaderCellStyle(final Workbook workbook) {
            if (headerCellStyle == null) {
                headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            return headerCellStyle;
        }


    }

    public static JSONArray getSpreadsheetData(final String fileName, final List<String> columns) {
        var array = new JSONArray();
        ZipSecureFile.setMinInflateRatio(0.1);
        try {
            Workbook workbook = new XSSFWorkbook(fileName);
            Sheet sheet = workbook.getSheetAt(0);
            var count = 0;
            var started = false;
            var endFound = false;

            while (sheet.iterator().hasNext() && !endFound) {

                var row = sheet.getRow(count);
                if (row != null && row.getLastCellNum() > 1) {
                    if (row.getCell(0).toString().equals(ROW_NAME)) {
                        // columns = getColumns(row);
                        started = true;
                    } else if (started) {

                        var rowObject = new JSONObject();
                        var cols = 0;
                        for (var col : columns) {
                            rowObject.put(col, row.getCell(cols));
                            cols++;
                        }
                        array.put(rowObject);
                    }

                } else {
                    endFound = true;

                }


                count++;
            }



        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return array;
    }

    private static void addHeader(final Workbook workbook, final Sheet sheet, final List<String> columns) {
        Row header = sheet.createRow(0);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var ci = 0;
        for (var col: columns) {
            Cell head = header.createCell(ci);
            head.setCellStyle(headerCellStyle);
            head.setCellValue(col);
            ci++;
        }
    }


    private static JSONObject cleanRow(final JSONObject row) {
        var obj = new JSONObject();
        var keys = row.keys();
        while (keys.hasNext()) {
            var key = keys.next();
            var value = (row.get(key) instanceof String)
                    ? row.getString(key) : row.getInt(key);
            obj.put(key.replace("a.", "").replace("b.content", "user_id").replace("''", ""), value);
        }
        return obj;
    }
    private static void setCell(final JSONObject object,
                                final String column,
                                final CellStyle dateStyle,
                                Cell cell, final String userName) {

        var cleanObject = cleanRow(object);
        if (cleanObject.has(column)) {
            var cont = cleanObject.getString(column);
            try {
                var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                cell.setCellStyle(dateStyle);
            } catch (Exception e) {
                LOGGER.info("skipping");
            }
            if (column.equals("user_id")) {
                cont = userName;
            }
            cell.setCellValue(cont);

        } else {
            cell.setCellValue("");

        }

    }
    private static void setCell(final JSONObject object,
                                final String column,
                                final CellStyle dateStyle,
                                Cell cell) {

        var cleanObject = cleanRow(object);
        if (cleanObject.has(column)) {
            var cont = cleanObject.getString(column);
            try {
                var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                cell.setCellStyle(dateStyle);
            } catch (Exception e) {
                LOGGER.info("skipping");
            }

            cell.setCellValue(cont);

        } else {
            cell.setCellValue("");

        }

    }
    private static void setRow(final JSONObject rowObject,
                               final CellStyle dateStyle,
                               Sheet sheet,
                               final List<String> columns,
                               final int rowInd, final String userName) {
        var row = sheet.createRow(rowInd);
        var i = 0;
        for (var column : columns) {
            var cell = row.createCell(i);
            try {
                setCell(rowObject, column, dateStyle, cell, userName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    private static void setRow(final JSONObject rowObject,
                               final CellStyle dateStyle,
                               Sheet sheet,
                               final List<String> columns,
                               final int rowInd) {
        var row = sheet.createRow(rowInd);
        var i = 0;
        for (var column : columns) {
            var cell = row.createCell(i);
            try {
                setCell(rowObject, column, dateStyle, cell);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    private static void setTotalRow(final JSONObject rowObject,
                               final CellStyle dateStyle,
                               Sheet sheet,
                               final List<String> columns,
                               final int rowInd) {
        var row = sheet.createRow(rowInd);
        var i = 0;
        for (var column : columns) {
            var cell = row.createCell(i);
                setCell(rowObject, column, dateStyle, cell);
            i++;
        }
    }

    private static void addInfoRow(final String rowName,
                                   final CellStyle style,
                                   Sheet sheet,
                                   final List<String> columns,
                                   final int rowInd) {
        var i = 0;
        var row = sheet.createRow(rowInd);
        for (var column : columns) {
            var cell = row.createCell(i);
            cell.setCellStyle(style);
            if (i == 0) {
                cell.setCellValue(rowName);
            } else {
                cell.setCellValue("");
            }
            i++;
        }

    }
    public static String saveExcel(final JSONArray array,
                                 final String sheetId,
                                 final String userId) {

        final var columns = List.of(AmdsHelper.getColumns(sheetId).split(","));
        final var name = AmdsHelper.getTableName(sheetId);
        final var model = QueryHelper.getRowsModel(Integer.parseInt(sheetId)).getJSONArray(MESSAGE);
        var path = AmdsHelper.getAmdsFilesPathByName(name, userId);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));
        CellStyle infoCellStyle = workbook.createCellStyle();
        infoCellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        infoCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        addHeader(workbook, sheet, columns);

        for (var i = 0; i < model.length(); i++) {
            var rowName = model.getJSONObject(i).getString(ROW_NAME);
            if (model.getJSONObject(i).getString(INFO_ROW).equals("1")) {
                addInfoRow(rowName, infoCellStyle, sheet, columns, i);
            } else {
                setRow(array.getJSONObject(i), dateCellStyle, sheet, columns, i);
            }
        }

        for (var l = 0; l < columns.size(); l++) {
            sheet.autoSizeColumn(l);
        }

        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            workbook.write(outputStream);

        } catch (IOException e) {
            path = null;
        }
        return path;
    }

    private static String getJoinColumns(final String columns) {
        var cols = columns.split(",");
        var builder = new StringBuilder();
        for (var col : cols) {
            builder.append("a.").append(col).append(",");
        }
        builder.append("b.content");
        return builder.toString();
    }

    private static void addSheetToWorkbook(final Workbook workbook, final String sheetId) {
        var sheetName = AmdsHelper.getTableName(sheetId);
        if (sheetName.equals("mri_new_starters"))
            System.out.println("here");
        var users = QueryHelper.getApplicableUsers(sheetId, sheetName);
        var sheet = workbook.createSheet(sheetName);
        var cols = AmdsHelper.getColumns(sheetId);
        var columns = List.of((cols + ",user_id").split(","));
        System.out.println(sheetName);
        final var model = QueryHelper.getRowsModel(Integer.parseInt(sheetId)).getJSONArray(MESSAGE);

        addHeader(workbook, sheet, columns);
        var count = 1;
        for (var u = 0; u < users.size(); u++) {
            var userId = users.get(u);

            var userName = QueryHelper.getNameById(userId);
            System.out.println("processing user: " + userName);
            var sheetQuery = AmdsHelper.getSheetQuery(sheetId, userId);
            var userArray = QueryHelper.getData(sheetQuery, QueryHelper.PULL_TABLE).getJSONArray(MESSAGE);
            if (userArray.length() > 0) {
                var fullArray = DataSort.getSortedArray(userArray, model);

                for (var i = 0; i < fullArray.length(); i++) {
                    if (i == 139)
                        System.out.println();
                    var infoRow = model.getJSONObject(i).getString(INFO_ROW).equals("1");
                    var rowName = fullArray.getJSONObject(i).getString(ROW_NAME);
                    if (infoRow) {
                        addInfoRow(rowName, Styles.getInfoCellStyle(workbook), sheet, columns, count);
                    } else {
                        setRow(fullArray.getJSONObject(i),
                                Styles.getDateCellStyle(workbook), sheet, columns, count, userName);

                    }
                    count++;
                    System.out.println(count);
                }
            }
        }

        for (var l = 0; l < columns.size(); l++) {
            sheet.autoSizeColumn(l);
        }
    }

    /**
     * DON'T FORGET TO CHANGE ROW_QUERY TO START FROM 2
     * @param path
     */
    public static void saveMassiveExcel(final String path) {


        Workbook workbook = new XSSFWorkbook();
        var rowsQuery = "select id from amds.sheets where id > 2 order by id";
       // var rowsQuery = "select id from amds.sheets where id > 5 order by id";
        var sheetIds = QueryHelper.getData(rowsQuery, QueryHelper.PULL_LIST).getJSONArray(MESSAGE);

        for (var i = 0; i < sheetIds.length(); i++) {
            addSheetToWorkbook(workbook, sheetIds.getString(i));
        }

        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            workbook.write(outputStream);

        } catch (IOException e) {
             LOGGER.info(e.getMessage());
        }

    }

    public static void main(String[] args) {
        var path = "C:/Users/Maxim.Karpov/IdeaProjects/testshmestservice/total.xlsx";
        saveMassiveExcel(path);
    }


    private static List<String> getColumns(final Row row) {
       List<String> columns = new ArrayList<>();
       var cols = row.getLastCellNum();
       for (var i = 0; i < row.getLastCellNum() - 1; i++) {
           try {
               if (Helper.isThing(row.getCell(i).toString()))
               columns.add(row.getCell(i).toString());
           } catch (Exception e) {
               System.out.println(cols);
               System.out.println(i);
           }
       }
        return columns;
    }

}
