package com.antso.expensesmanager.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by asolano on 5/11/2014.
 */
public class Utils {
    static public DateTime DEFAULT_DATE = new DateTime(1970, 1, 1, 0, 0);

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

    public static DateTime yyyMMddhhMMssToDateTime(int yyyyMMdd, int hhMMss) {
        int hour = hhMMss / 10000;
        int min = hhMMss % 10000 / 100;
        int sec = hhMMss % 100;

        return new LocalTime(hour, min, sec).toDateTime(yyyyMMddToDate(yyyyMMdd));
    }

    public static String getDatePatten() {
        return "MM/dd/yyyy";
    }

    public static DateTimeFormatter getDateFormatter() {
        return DateTimeFormat.forPattern(Utils.getDatePatten());
    }

}
