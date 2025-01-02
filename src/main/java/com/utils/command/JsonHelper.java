package com.utils.command;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

//    public static List<String> getListFromJsonArray(final JSONArray array) {
//        List<String> result = new ArrayList<>();
//        for (var i = 0; i < array.length(); i++) {
//            var obj = array.get(i);
//                result.add(array.getString(i));
//        }
//        return result;
//    }

    public static List<String> getListFromJsonArray(final JSONArray array, final String key) {
        List<String> result = new ArrayList<>();
        for (var i = 0; i < array.length(); i++) {
            var obj = array.getJSONObject(i);
            result.add(obj.getString(key));
        }
        return result;
    }



    public static List<String> getListFromJsonArray(final JSONArray array) {
        List<String> list = new ArrayList<>();
        array.forEach(str -> list.add((String) str));
        return list;
    }

    public static List<Object> getListObjectFromJsonArray(final JSONArray array) {
        List<Object> list = new ArrayList<>();
        array.forEach(list::add);
        return list;
    }

    public static List<JSONObject> getListJsonObjects(final JSONArray array) {
        List<JSONObject> list = new ArrayList<>();
        for (var i = 0; i < array.length(); i++) {
            list.add(array.getJSONObject(i));
        }
        return list;
    }


    public static JSONArray getArrayFromList(final List<String> list) {
        var array = new JSONArray();
        list.forEach(array::put);
        return array;
    }

    public static JSONArray getArrayFromJsonObjectList(final List<JSONObject> list) {
        var array = new JSONArray();
        list.forEach(array::put);
        return array;
    }
}
