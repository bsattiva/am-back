package com.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final List<String> DATE_COLUMNS = new ArrayList<>() {{
        add("date");
        add("observed");
        add("to");
        add("from");
        add("trainig_date");
        add("competency_date");
        add("col_from");
        add("col_to");
    }};
    public static final List<String> NO_MODEL_SHEETS = new ArrayList<>() {{
        add("20");
    }};

    public static final Map<String, Integer> NO_MODEL_LINES = new HashMap<>() {{
       put("20", 8);
    }};

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String MAIN_RESOURCES = USER_DIR + "/src/main/resources/";

}
