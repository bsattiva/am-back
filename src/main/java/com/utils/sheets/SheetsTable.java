package com.utils.sheets;

import com.utils.AmdsHelper;
import com.utils.Helper;
import com.utils.data.QueryHelper;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
public class SheetsTable {
    @Setter
    private int id;
    @Setter
    private String name;
    @Setter
    private String properties;
    @Setter
    private String maskName;
    @Setter
    private String rowFields;
    @Setter
    private String uiFields;
    @Setter
    private String userUiFields;
    @Setter
    private String head;
    @Setter
    String sheetName;

    public SheetsTable(final String name, final JSONObject tableObject) {
        this.name = name;
        this.id = getId();
        this.maskName = getMask();
        this.rowFields = getColumns(tableObject.getJSONArray("columns"));
        this.uiFields = getUserColumns(tableObject.getJSONArray("columns"));


    }
    public SheetsTable(final String name,
                       final String properties,
                       final String rowFields,
                       final String uiFields,
                       final String userUiFields,
                       final String head) {

        this.name = name;
        this.properties = properties;
        this.maskName = getMask();
        this.rowFields = rowFields;
        this.uiFields = uiFields;
        this.userUiFields = userUiFields;
        this.head = head;
        this.sheetName = getSheetName();

    }

    private String getColumns(final JSONArray columns) {
        var builder = new StringBuilder();
        for (var i = 0; i < columns.length(); i++) {
            if (i < columns.length() - 1) {
                builder.append(columns.getJSONObject(i).getString("name"));
                builder.append(",");
            } else {
                builder.append(columns.getJSONObject(i).getString("name"));
            }
        }
        return builder.toString();
    }


    private JSONObject getMapping(final JSONArray columns) {
        var mappings = new JSONObject();
        for (var i = 0; i < columns.length(); i++) {
            mappings.put(columns.getJSONObject(i).getString("name"),
                    columns.getJSONObject(i).getString("mapping"));
        }
        return mappings;
    }

    private JSONObject getSavedMappings(final JSONArray columns) {
        var mappings = new JSONObject();
        for (var i = 0; i < columns.length(); i++) {

        }
        return mappings;
    }

//    private JSONArray getLinkedMappigns(final JSONArray columns, final String name) {
//        var result = new JSONArray();
//        for (var i = 0; i < columns.length(); i++) {
//            if (columns.getJSONObject(i).getString("saved-mappings").equals(name)) {
//                var obj = new JSONObject();
//                obj.put("")
//            }
//        }
//        return result;
//    }
    private String getRowColumns(final JSONArray columns) {
        var mappings = getMapping(columns);
        var savedMappings = new JSONObject(getSavedMappings(columns));
        var rowColumns = new JSONObject();
        rowColumns.put("mappings", mappings);
        rowColumns.put("saved-mappings", savedMappings);

        return rowColumns.toString();
    }

    private String getUserColumns(final JSONArray columns) {
        var builder = new StringBuilder();
        for (var i = 0; i < columns.length(); i++) {
            if (Helper.isThing(columns.getJSONObject(i).getString("scoped"))
                    && columns.getJSONObject(i).getString("scoped").equals("user")) {
            if (i < columns.length() - 1) {
                builder.append(columns.getJSONObject(i).getString("name"));
                builder.append(",");
            } else {
                builder.append(columns.getJSONObject(i).getString("name"));
            }
            }
        }
        return builder.toString();
    }


    private String getMask() {
        return name
                .replace("(", " ")
                .replace(")", "");
    }

    private String getSheetName() {
        return name.replace(" ", "_")
                .replace("(", "_")
                .replace(")", "");
    }
    private String getQuery() {
        final String query = "insert into amds.sheets values(%d,'%s','%s','%s','%s','%s','%s','%s')";
        return String.format(query, id, name, properties, maskName, rowFields, uiFields, userUiFields, head);
    }

    public JSONObject setInSheetsTable() {
        setId(AmdsHelper.getNewSheetId());
        return QueryHelper.createSheet(getQuery());
    }

    public JSONObject setSheet(final JSONArray rows, final int id) {
        var status = AmdsHelper.createSheetQuery(id);
        return status;
    }
//TODO: add actual call to db
    private JSONObject populateRow(final JSONObject row, final int sequence) {
        final var rowName = row.getString("row-name");
        final var infoRow = row.getInt("info-row");
        final var query = String.format("insert into amds.sheet_model values('%s',%d,%d,%d)",
                rowName, sequence, infoRow, id);
        //return QueryHelper.execute(query);
        return new JSONObject();
    }
    public JSONArray populateModel(final JSONArray rows) {
        var result = new JSONArray();
        for (var i = 0; i < rows.length(); i++) {
            result.put(populateRow(rows.getJSONObject(i), i));
        }
        return result;
    }
}
