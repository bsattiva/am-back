package com.utils.test;

import com.utils.excel.DupeList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

public class DataSort {
    public static final String ROW_NAME = "row_name";
    public static final String ROW_INFO = "info_row";
    public static JSONArray getSortedArray(final JSONArray array, final JSONArray model) {
        var result = new JSONArray();
        try {
            DupeList.unload();
            checkArray(array);

            var templateObject = array.getJSONObject(0);
            for (var i = 0; i < model.length(); i++) {
                if (i == 97)
                    System.out.println("");
                    var info = model.getJSONObject(i).getString(ROW_INFO).equals("1");
                    var rowName = model.getJSONObject(i).getString(ROW_NAME);
                    if (info) {
                        result.put(populateInfoObject(templateObject, rowName));

                    } else {
                        result.put(getSequenceObject(array, rowName));
                    }
        }

    } catch (JSONException e) {
        e.printStackTrace();
    }
    return result;
}



    public static JSONObject getSequenceObject(final JSONArray array, final String name) throws JSONException {

        var object = new JSONObject();
        var found = 0;
        for (var i = 0; i < array.length(); i++) {
            var rowName = array.getJSONObject(i).getString(ROW_NAME);
            if (rowName.contains("SCJ"))
                System.out.println(rowName);
            if (DupeList.isDupe(rowName)) {
                var met = DupeList.howManyMet(rowName);



                if (rowName.equals(name.replace(" \uF06E", ""))) {

                    if (found == DupeList.howManyMet(rowName)) {
                        object = array.getJSONObject(i);
                        DupeList.dupeMet(rowName);
                        break;

                    }
                    found++;
                }



            } else if (equals(rowName, name)) {

                object = array.getJSONObject(i);
                break;
            }
        }

        if (object.length() == 0) {
            populateBlankObject(array, name, object);

        }
        return object;
    }

    private static void populateBlankObject(final JSONArray array, final String name, JSONObject object) {
        for (var i = 0; i < array.length(); i++) {
            var obj = array.getJSONObject(i);
            if (obj.has(ROW_NAME)) {
                var keys = obj.keys();
                while (keys.hasNext()) {
                    var key = keys.next();
                    if (key.equals(ROW_NAME)) {
                        object.put(ROW_NAME, name);
                    } else {
                        object.put(key, "");
                    }
                }
                break;
            }
        }
    }

    private static boolean equals(final String oneName, final String anotherName) {
        var name1 = oneName.replace("\uF06E", "").replace("'", "");
        var name2 =  anotherName.replace("\uF06E", "").replace("'", "");
        return (name1.trim()).equals(name2.trim());

    }




    public static JSONObject populateInfoObject(final JSONObject object, final String rowName) throws JSONException {
        var keys = object.keys();
        var result = new JSONObject();
        while (keys.hasNext()) {
            var key = keys.next();
            if (key.toString().equals(ROW_NAME)) {

                    result.put(ROW_NAME, rowName);

            } else {
                result.put(key.toString(), "");
            }

        }
        return result;
    }
    private static void checkArray(final JSONArray array) throws JSONException {
        for (var i = 0; i < array.length(); i++) {
            var rowName = array.getJSONObject(i).getString(ROW_NAME);
            DupeList.checkEntry(rowName);
        }

    }
}
