package com.enums;

public enum DateFields {
    DATE("date"), OBSERVED("observed"), TO("to"), FROM("from");
    public final String label;
    private DateFields(String label) {
        this.label = label;
    }
}
