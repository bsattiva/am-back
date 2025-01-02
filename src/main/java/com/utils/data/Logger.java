package com.utils.data;


import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Logger {

    @Getter
    private String className;
    private static final String MASK = "\\?";
    private static final String QUERY = "insert into log.template values('?','?','";
    private static final String QUERY_START = "insert into log.template values('";
    private static final String QUERY_MID = "','";



    private static final String END_OF_QUERY = "',NOW())";
    private static final String DELETE_QUERY = "delete from log.template where time < '?'";

    public Logger(final Class classInstance) {
        populate(classInstance);
    }

    public Logger(final String className) {
        this.className = className;
    }

    final void populate(final Class classInstance) {
        className = classInstance.getName();
    }

    public void log(final String message, final String error) {
//        String query = QUERY
//                .replaceFirst(MASK, className)
//                .replaceFirst(MASK, message)
//                .replaceFirst(MASK, error);
        String query = QUERY_START + className + QUERY_MID + message + QUERY_MID + error + END_OF_QUERY;
        QueryHelper.getData(query, "execute");
        cleanLog();

    }

    public void log(final String message, final StackTraceElement[] trace) {
        try {
            StringBuilder builder = new StringBuilder();
            Arrays.asList(trace).forEach(str -> builder.append(str.toString())
                    .append("\r\n")
            );
            String mess = ( message != null) ? message : "";

//            String query = QUERY
//                    .replaceFirst(MASK, className)
//                    .replaceFirst(MASK, mess)
//                    + builder.substring(0,1999)
//                    + END_OF_QUERY;

            String query = QUERY_START + className + QUERY_MID + mess + QUERY_MID + builder.substring(0,1999) + END_OF_QUERY;

            QueryHelper.getData(query, "execute");
        } catch (Exception e) {
            e.printStackTrace();
        }
        cleanLog();
    }

    private void cleanLog() {
        try {
            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String shortDate = date.format(formatter);

            QueryHelper
                    .getData(DELETE_QUERY
                            .replaceFirst(MASK, shortDate),
                            "execute");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
