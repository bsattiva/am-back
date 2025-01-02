package com.utils.excel;

import com.utils.UsefulBoolean;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupeList {
    private static List<String> dupes;

    private static Map<String, Integer> dupeMap;
    private static Map<String, Integer> dupesHit;

    private static void populate() {
        if (dupes == null) {
            dupes = new ArrayList<>();
            dupeMap = new HashMap<>();
            dupesHit = new HashMap<>();
        }

    }

    public static void checkEntry(final String entry) {
        populate();
        if (dupes.contains(entry)) {
            var count = dupeMap.getOrDefault(entry, 0);
            dupeMap.put(entry, count + 1);
        } else {
            dupes.add(entry);

        }
    }

    public static boolean isDupe(final String entry) {
        populate();
        return dupeMap.getOrDefault(entry, 0) > 0;
    }

    public static int howManyDupes(final String entry) {
        populate();
        if (dupes.contains(entry)) {
            return dupeMap.get(entry);
        }
        return 0;
    }

    public static void dupeMet(final String entry) {
        populate();
        var count = dupesHit.getOrDefault(entry, 0);
        dupesHit.put(entry, count + 1);
    }

    public static int howManyMet(final String entry) {
        populate();
        return dupesHit.getOrDefault(entry, 0);

    }

    public static void unload() {
        if (dupes != null) {
            dupes.clear();
            dupeMap.clear();
            dupesHit.clear();

        }


    }


}
