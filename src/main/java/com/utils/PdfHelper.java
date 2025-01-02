package com.utils;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.utils.data.QueryHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static com.utils.AmdsHelper.MESSAGE;
import static com.utils.AmdsHelper.saveSheet;

public class PdfHelper {

    public static final String ROW_NAME = "row_name";
    public static final String INFO_ROW = "info_row";
    public static final String SKILLS = "Skills";
    public static List<String> saveAllXls(final String userId) {
        final var tableIds = QueryHelper.getEnabledAmdsSheets(userId);
        List<String> paths = new ArrayList<>();
        for (var id : tableIds) {
            paths.add(saveSheet(id, userId));
        }
        return paths;
    }

    public static String capitalizeStart(final String value) {
        final var chars = value.toCharArray();
        var resultBuilder = new StringBuilder();
        var started = false;
        for (var ch : chars) {
            if (!started) {
                resultBuilder.append(Character.toUpperCase(ch));
            } else {
                resultBuilder.append(Character.toLowerCase(ch));

            }
            started = true;
        }
        return resultBuilder.toString();
    }

    public static String normalizeHeader(final String value, final String id) {
        final var trimmed = (value.trim().startsWith("_")) ? value.trim().substring(1) : value.trim();
        final var prepared = (trimmed.endsWith("_")) ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        var result = new StringBuilder();
        var parts = prepared.toLowerCase().split("_");
        Stream.of(parts).map(PdfHelper::capitalizeStart).forEach(part -> result.append(part).append(" "));
        return result.toString();
    }

    private static String parseTableName(final String name, final String id) {
        var regex = "files/(.*?)_" + id + ".xlsx$";
        return normalizeHeader(Helper.parseStringWithRegex(regex, name, 1), id);
    }

    private static float[] getColumnWidths(final int colNum, final float first, final float others) {
        var result = new float[colNum];
        var i  = 0;
        for (var item : result) {
            if (i == 0) {
                result[i] = first;
            } else {
                result[i] = others;
            }
            i++;
        }
        return result;
    }

    public static void cleanUp(final String id) {
        var maskXlsx = "_" + id + ".xlsx";
        var maskPdf = "_" + id + ".pdf";

        var path = TestHelper.getSameLevelProject("files");
        var dir = new File(path);
        var files = dir.listFiles();
        for (var file : files) {
            if (file.getName().contains(maskXlsx) || file.getName().contains(maskPdf)) {
                file.delete();
            }

        }

    }

    private static void addColumnNames(final PdfPTable document, final Sheet sheet, final int columnsCount) {
        PdfPCell blueStyle = new PdfPCell();
        blueStyle.setBackgroundColor(new BaseColor(102, 139, 139));
        blueStyle.setHorizontalAlignment(Element.ALIGN_CENTER);
        blueStyle.setVerticalAlignment(Element.ALIGN_MIDDLE);
        blueStyle.setPadding(5);
        blueStyle.setBorder(Rectangle.NO_BORDER);
        Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

        var row = sheet.getRow(0);
        for (var i = 0; i < columnsCount; i++) {
            var cell = new PdfPCell(blueStyle);
            var text = row.getCell(i).toString().replace("_", " ");
            cell.addElement(new Phrase(text, font));
            document.addCell(cell);

        }

    }

    private static void setMriSkillsHeader(PdfPTable table) {


        setImageOrText(table, "", false);
        setImageOrText(table, "img/portal/philips-healthcare.png", true);
        setImageOrText(table, "img/portal/siemens.png", true);
        setImageOrText(table, "img/portal/ge.png", true);

    }

    private static int getSheetId(final String header) {
        var sheetId = -1;
        try {
            sheetId = QueryHelper.getSheetIdByName(header);
            if (sheetId == 5) {
                System.out.println(sheetId);

            }
        } catch (Exception e) {
            System.out.println("CANNOT FIND ID FOR HEADER: " + header);
        }
        return sheetId;

    }

    public static void saveUserPdf(final String userId, final String fileId) {
        cleanUp(userId);
        var name = QueryHelper.getNameById(userId);
        var files = saveAllXls(userId);
        var document = new Document(PageSize.A4.rotate());

        try {
            var path = TestHelper.getSameLevelProject("files") + "/output_" + fileId + "_" + userId + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            Paragraph titleParagraph = new Paragraph("Competency assessment form for: " + name);
            titleParagraph.setSpacingAfter(5);
            titleParagraph.setAlignment(Element.ALIGN_LEFT);
            document.add(titleParagraph);

            // Create the navbar-like stripe table
            PdfPTable stripeTable = new PdfPTable(1);
            stripeTable.setWidthPercentage(100);
            stripeTable.getDefaultCell().setPadding(0);
            stripeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Create the stripe cell
            PdfPCell stripeCell = new PdfPCell();
            stripeCell.setBackgroundColor(new BaseColor(0, 0, 128)); // Set the background color to dark navy
            stripeCell.setFixedHeight(90); // Set the height of the stripe
            stripeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            // Create a nested table for the image and text
            PdfPTable nestedTable = new PdfPTable(2);
            nestedTable.setWidthPercentage(100);
            nestedTable.setSpacingBefore(10);
            nestedTable.setWidths(new float[]{4, 96}); // Adjust the widths as needed

            PdfPCell imageCell = new PdfPCell();
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setPaddingTop(120);
           // imageCell.setPaddingLeft(50);

            String imagePath = "logo.png";
            Image image = Image.getInstance(imagePath);
            image.scaleToFit(220, 160);

           imageCell.addElement(image);

          //  nestedTable.addCell(image);
            stripeCell.addElement(image);

            stripeTable.addCell(stripeCell);
        //    document.add(image);
            document.add(stripeTable);
            //  document.add(imageCell);

            for (var file : files) {
                var header = parseTableName(file, userId);
                var sheetId = getSheetId(header);

                var rowModel = QueryHelper.getRowsModel(sheetId);
                Workbook workbook = new XSSFWorkbook(file);

                com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph headingParagraph = new Paragraph(header, headerFont);
                headingParagraph.setAlignment(Element.ALIGN_CENTER);

                Sheet sheet = workbook.getSheetAt(0);
                var columnCount = sheet.getRow(0).getLastCellNum();


                PdfPTable table = new PdfPTable(sheet.getRow(0).getLastCellNum());
                table.setWidths(getColumnWidths(columnCount, 6f, 2f));

                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                PdfPCell simpleStyle = new PdfPCell();
                simpleStyle.setPadding(5);


                var modelArray = rowModel.getJSONArray(MESSAGE);

                if (sheet.getLastRowNum() > 0)
                if (modelArray.length() > 0) {
                    addColumnNames(table, sheet, columnCount);

                    document.add(headingParagraph);
                    Paragraph spaceParagraph = new Paragraph(10);
                    document.add(spaceParagraph);
                    document.add(spaceParagraph);
                    document.add(spaceParagraph);
                    document.add(new Paragraph("\n"));
                    for (var m = 0; m < modelArray.length(); m++) {
                        var found = false;
                        var rowName = modelArray.getJSONObject(m).getString(ROW_NAME);
                        var info_row = modelArray.getJSONObject(m).getString(INFO_ROW);
                        if (Integer.parseInt(info_row) > 0) {

                            PdfPCell blueStyle = new PdfPCell();
                            blueStyle.setBackgroundColor(BaseColor.LIGHT_GRAY);
                            blueStyle.setHorizontalAlignment(Element.ALIGN_CENTER);
                            blueStyle.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            blueStyle.setPadding(5);
                            blueStyle.setBorder(Rectangle.NO_BORDER);
                            Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);

                            for (var n = 0; n < columnCount; n++) {
                                if (n == 0) {
                                    var cell = new PdfPCell(blueStyle);
                                    cell.addElement(new Phrase(rowName, font));
                                    table.addCell(cell);
                                } else {
                                    var cell = new PdfPCell(blueStyle);
                                    cell.addElement(new Phrase("", font));
                                    table.addCell(cell);
                                   // table.addCell("");
                                }
                            }

                        } else {
                            for (Row row : sheet) {
                                var rName = row.getCell(0).toString();
                                if (rName.equals(rowName)) {
                                    setCell(table, row, columnCount, header);
                                    found = true;
                                    break;
                                }

                            }

                            if (!found) {
                                if (!header.contains(SKILLS)) {
                                    setCell(table, simpleStyle, rowName, columnCount);

                                } else {
                                    setBlankCheckCell(table, simpleStyle, rowName, columnCount);

                                }


                            }

                        }


                    }

                }


                document.add(table);

                document.add(new Paragraph("\n"));

                workbook.close();
            }
            

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            //Logger logger = new Logger(PdfHelper.class);
           // logger.log(e.getMessage(), "ERROR IN PDF HELPER");
        }

        document.close();

    }

    private static void setImage(final PdfPTable table, final String imagePath) {
        PdfPCell imageCell = new PdfPCell();
        imageCell.setBorder(Rectangle.BOX);
        // imageCell.setPaddingTop(120);
        // imageCell.setPaddingLeft(50);


        try {
            Image image = Image.getInstance(imagePath);
            image.scaleToFit(160, 90);

            imageCell.addElement(image);
            table.addCell(imageCell);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }

    }

    private static void setCheckImage(final PdfPTable table, final boolean checked) {
        PdfPCell imageCell = new PdfPCell();
        imageCell.setBorder(Rectangle.BOX);
       // imageCell.setPaddingTop(120);
        // imageCell.setPaddingLeft(50);

        String imagePath = (checked) ? "checked.png" : "unchecked.png";
        try {
            Image image = Image.getInstance(imagePath);
            image.scaleToFit(60, 60);

            imageCell.addElement(image);
            table.addCell(imageCell);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }

    }

    private static void setImageOrText(final PdfPTable table,
                                       final String imageOrText, final boolean image) {
        System.out.println("");
        if (image) {
            setImage(table, imageOrText);
        } else  {
            table.addCell(imageOrText);;
        }
    }

    private static void setImageOrText(final PdfPTable table,
                                 final String text, final String tableName) {

        if (tableName.contains(SKILLS) && text.equals("yes")) {
            setCheckImage(table, true);
        } else if (tableName.contains(SKILLS) && text.equals("f")) {
            setCheckImage(table, false);
        } else {
            table.addCell(text);
        }

    }

    private static void setCell(final PdfPTable table,
                                final Row row,
                                final int columnCount,
                                final String tableName) {
        for (var i = 0; i < columnCount; i++) {
            if (row.getCell(i) != null) {
                setImageOrText(table, row.getCell(i).toString(), tableName);
            } else {
                table.addCell("");

            }

        }

    }

    private static void setCell(final PdfPTable table, final PdfPCell cellStyle, final String row, final int columnCount) {

        for (var i = 0; i < columnCount; i++) {
            var cell = new PdfPCell(cellStyle);
            Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            var text = (i == 0) ? row : "";
            cell.addElement(new Phrase(text, font));
            table.addCell(cell);
        }

    }

    private static void setBlankCheckCell(final PdfPTable table, final PdfPCell cellStyle, final String row, final int columnCount) {

        for (var i = 0; i < columnCount; i++) {
            var cell = new PdfPCell(cellStyle);
            Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);

            if (i == 0) {
                cell.addElement(new Phrase(row, font));
                table.addCell(cell);
            } else if (i > columnCount - 3) {
                cell.addElement(new Phrase("", font));
                table.addCell(cell);
            } else {
                setCheckImage(table, false);


            }
        }
    }

    public static String getMaskedTableName(final String key) {
        Map<String, String> map = new HashMap<>();
        map.put("Mri Responsible Person", "MRI - Responsible Person");
        map.put("Injectors  Mrxperion", "Injectors (MRXperion)");
        map.put("Injectors  Stellant", "Injectors (Stellant)");
        map.put("Injectors  Centargo", "Injectors (Centargo)");
        map.put("Injectors  Spectris Solaris", "Injectors (Spectris Solaris)");

        return map.getOrDefault(key, key);

    }

}
