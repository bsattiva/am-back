package com.enums;

public enum Area {
    STARTER("starter"),CUBE("cube"), QUERY_HELPER("query helper"), ATTENTION("attention"),
    LOGGING("logging");
    public final String label;
    private Area(String label) {
        this.label = label;
    }
}
