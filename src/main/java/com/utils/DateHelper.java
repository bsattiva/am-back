package com.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateHelper {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String EXCEL_FORMAT = "dd-MMM-yyyy";


    public static UsefulBoolean formatOk(final String date) {
        if (!Helper.isThing(date))
            return new UsefulBoolean(true, "");
        try {
            var formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            formatter.parse(date);
            return new UsefulBoolean(true, date);
        } catch (DateTimeParseException e) {
            try {
                var formatter = DateTimeFormatter.ofPattern(EXCEL_FORMAT);
                LocalDate localDate = LocalDate.parse(date, formatter);

                return new UsefulBoolean(true, localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            } catch (Exception ex) {
                return new UsefulBoolean(false, ex.getMessage());
            }
        }

    }

    public static UsefulBoolean formatOk(final String date, final String format) {

        try {
            var formatter = DateTimeFormatter.ofPattern(format);
            formatter.parse(date);
            var localDate = LocalDate.parse(date, formatter);
            return new UsefulBoolean(true,
                    localDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        } catch (DateTimeParseException e) {
            return new UsefulBoolean(false, "");
        }

    }

    public static LocalDate parseDate(final String date) {
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (Exception e) {
            localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(EXCEL_FORMAT));
        }
        return localDate;
    }

}
