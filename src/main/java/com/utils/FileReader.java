package com.utils;

import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import com.utils.data.QueryHelper;
import io.cucumber.java.sl.In;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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


    private static UsefulBoolean schemaOk(final String[] columns, final Sheet sheet) {

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
