package com.antso.expensesmanager.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class Utils {
    static public DateTime DEFAULT_DATE = new DateTime(1970, 1, 1, 0, 0);

    public static Integer[] DaySingleValues = new Integer[] {1, 2, 3, 4, 5, 6, 7};
    public static Integer[] DayValues = new Integer[] {1, 2, 3, 4, 5, 6, 7};
    public static Integer[] WeekValues = new Integer[] {1, 2, 3, 4, 5};
    public static Integer[] MonthValues = new Integer[] {1, 2, 3, 4, 6};
    public static Integer[] YearValues = new Integer[] {1, 2};

    public static int dateTimeToyyyMMdd(DateTime date) {
        int y = date.getYear() * 10000;
        int m = date.getMonthOfYear() * 100;
        int d = date.getDayOfMonth();
        return y + m + d;
    }

    public static int dateTimeTohhMMss(DateTime time) {
        int h = time.getHourOfDay() * 10000;
        int m = time.getMinuteOfHour() * 100;
        int s = time.getSecondOfMinute();
        return h + m + s;
    }

    public static DateTime hhMMssToDateTime(int hhMMss) {
        int hour = hhMMss / 10000;
        int min = hhMMss % 10000 / 100;
        int sec = hhMMss % 100;

        return new LocalTime(hour, min, sec).toDateTime(DEFAULT_DATE);
    }

    public static DateTime yyyyMMddToDate(int yyyyMMdd) {
        int year = yyyyMMdd / 10000;
        int month = yyyyMMdd % 10000 / 100;
        int day = yyyyMMdd % 100;

        return new DateTime(year, month, day, 0, 0);
    }

    public static DateTime yyyyMMddhhMMssToDateTime(int yyyyMMdd, int hhMMss) {
        int hour = hhMMss / 10000;
        int min = hhMMss % 10000 / 100;
        int sec = hhMMss % 100;

        return new LocalTime(hour, min, sec).toDateTime(yyyyMMddToDate(yyyyMMdd));
    }

    public static String getDatePattenForDB() {
        return "yyyyMMddhhmmss";
    }

    public static String getDatePatten() {
        return "MM/dd/yyyy";
    }

    public static DateTimeFormatter getDateFormatter() {
        return DateTimeFormat.forPattern(Utils.getDatePatten());
    }

    public static String getDatePattenEU() {
        return "dd/MM/yyyy";
    }

    public static DateTimeFormatter getDateFormatterEU() {
        return DateTimeFormat.forPattern(Utils.getDatePattenEU());
    }

    public static String getCurrencyString() {
        return "$";
    }

    public static int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        @SuppressWarnings("UnnecessaryLocalVariable")
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(int px, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        @SuppressWarnings("UnnecessaryLocalVariable")
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static String washDecimalNumber(String valueStr) {
        if (valueStr.isEmpty()) {
            return "0.00";
        }

//        int index = valueStr.indexOf(".");
//        if (index == -1) {
//            valueStr = valueStr + ".00";
//        } else if (valueStr.length() - index > 2) {
//            valueStr = valueStr.substring(0, index + 2);
//        } else if (valueStr.length() - index < 2) {
//
//        }

        return valueStr;
    }

    public static String formatDate(DateTime date) {
        if (date.getDayOfMonth() == DateTime.now().getDayOfMonth() &&
            date.getWeekOfWeekyear() == DateTime.now().getWeekOfWeekyear() &&
            date.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
            date.getYear() == DateTime.now().getYear()) {
                return "Today";
        } else if (date.getWeekOfWeekyear() == DateTime.now().getWeekOfWeekyear() &&
                   date.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                   date.getYear() == DateTime.now().getYear()) {
            return date.toString(DateTimeFormat.forPattern("EEEE"));
        } else if (date.getYear() == DateTime.now().getYear()) {
            return date.toString(DateTimeFormat.forPattern("E dd MMM"));
        } else {
            return date.toString(DateTimeFormat.forPattern("dd MMM yyyy"));
        }
    }

    public static String formatDateMonthYearOnly(DateTime date) {
        if (date.getDayOfMonth() == DateTime.now().getDayOfMonth() &&
                date.getWeekOfWeekyear() == DateTime.now().getWeekOfWeekyear() &&
                date.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                date.getYear() == DateTime.now().getYear()) {
            return "Today";
        } else if (date.getYear() == DateTime.now().getYear()) {
            return date.toString(DateTimeFormat.forPattern("MMM"));
        } else {
            return date.toString(DateTimeFormat.forPattern("MMM yyyy"));
        }
    }

    public static boolean isBeforeOrEqual(DateTime d1, DateTime d2) {
        d1 = d1.withTimeAtStartOfDay();
        d2 = d2.withTimeAtStartOfDay();

        return (d1.isBefore(d2) || d1.isEqual(d2));
    }
}
