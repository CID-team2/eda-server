package eda.domain;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public enum DataType {
    INT,
    STRING,
    FLOAT,
    DATE,
    BOOL;

    private static Integer convertStringToInteger(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String convertString(String s) {
        if (!s.equals(""))
            return s;
        else
            return null;
    }

    private static Double convertStringToDouble(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Date convertStringToDate(String s) {
        String[] dateFormats = {
                "yyyy-mm-dd"
        };
        try {
            return DateUtils.parseDate(s, dateFormats);
        } catch (ParseException e) {
            return null;
        }
    }

    private static Boolean convertStringToBoolean(String s) {
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t"))
            return Boolean.TRUE;
        else if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("f"))
            return Boolean.FALSE;
        else return null;
    }

    public List<Object> convertStringList(List<String> list) {
        return switch (this) {
            case INT -> list.stream().map(DataType::convertStringToInteger).map(Object.class::cast).toList();
            case STRING -> list.stream().map(DataType::convertString).map(Object.class::cast).toList();
            case FLOAT -> list.stream().map(DataType::convertStringToDouble).map(Object.class::cast).toList();
            case DATE -> list.stream().map(DataType::convertStringToDate).map(Object.class::cast).toList();
            case BOOL -> list.stream().map(DataType::convertStringToBoolean).map(Object.class::cast).toList();
        };
    }
}
