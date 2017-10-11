package com.antso.expenses.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.antso.expenses.R;
import com.antso.expenses.enums.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;

public class Utils {
    static public DateTime DEFAULT_DATE = new DateTime(1970, 1, 1, 0, 0);

    public static Integer[] DayValues = new Integer[] {1, 2, 3, 4, 5};
    public static Integer[] WeekValues = new Integer[] {1, 2};
    public static Integer[] MonthValues = new Integer[] {1, 2, 4, 6};
    public static Integer[] YearValues = new Integer[] {1};

    public static int dateTimeToyyyyMMdd(DateTime date) {
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

    public static String getCurrencyString(Context context) {
        String curr = Settings.getCurrencySymbol(context);
        if (curr != null) {
            return curr.substring(0, 1);
        }

        return context.getText(R.string.currency).toString();
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
        if (date.getDayOfMonth() == Utils.now().getDayOfMonth() &&
            date.getWeekOfWeekyear() == Utils.now().getWeekOfWeekyear() &&
            date.getMonthOfYear() == Utils.now().getMonthOfYear() &&
            date.getYear() == Utils.now().getYear()) {
                return "Today";
        } else if (date.getWeekOfWeekyear() == Utils.now().getWeekOfWeekyear() &&
                   date.getMonthOfYear() == Utils.now().getMonthOfYear() &&
                   date.getYear() == Utils.now().getYear()) {
            return date.toString(DateTimeFormat.forPattern("EEEE"));
        } else if (date.getYear() == Utils.now().getYear()) {
            return date.toString(DateTimeFormat.forPattern("E dd MMM"));
        } else {
            return date.toString(DateTimeFormat.forPattern("dd MMM yyyy"));
        }
    }

    public static String formatDateMonthYearOnly(DateTime date) {
        if (date.getDayOfMonth() == Utils.now().getDayOfMonth() &&
                date.getWeekOfWeekyear() == Utils.now().getWeekOfWeekyear() &&
                date.getMonthOfYear() == Utils.now().getMonthOfYear() &&
                date.getYear() == Utils.now().getYear()) {
            return "Today";
        } else if (date.getYear() == Utils.now().getYear()) {
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

    public static boolean isAfterOrEqual(DateTime d1, DateTime d2) {
        d1 = d1.withTimeAtStartOfDay();
        d2 = d2.withTimeAtStartOfDay();

        return (d1.isAfter(d2) || d1.isEqual(d2));
    }

    public static boolean isBefore(DateTime d1, DateTime d2) {
        d1 = d1.withTimeAtStartOfDay();
        d2 = d2.withTimeAtStartOfDay();

        return d1.isBefore(d2);
    }

    public static boolean isAfter(DateTime d1, DateTime d2) {
        d1 = d1.withTimeAtStartOfDay();
        d2 = d2.withTimeAtStartOfDay();

        return d1.isAfter(d2);
    }

    private static DateTime instrumentedTodayDate;
    public static void instrumentDateTimeNow(final DateTime now) {
        instrumentedTodayDate = now;
    }
    public static DateTime now() {
        if(instrumentedTodayDate != null) {
            return instrumentedTodayDate;
        }

        return DateTime.now();
    }

    public static int getPercentage(BigDecimal in, BigDecimal out, BigDecimal threshold) {
        BigDecimal balance = out.subtract(in);
        if(balance.compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        } else {
            if (threshold.compareTo(BigDecimal.ZERO) == 0) {
                return 0;
            }

            double result = balance.doubleValue();
            result = result / (threshold.doubleValue()) * 100;
            return (int) result;
        }
    }

    public static DateTime getNextDate(DateTime now, DateTime periodStart, int periodLength, TimeUnit periodUnit) {
        DateTime result = periodStart;
        while (Utils.isBefore(result, now)) {
            switch (periodUnit) {
                case Day:
                    result = result.plusDays(periodLength);
                    break;
                case Month:
                    result = result.plusMonths(periodLength);
                    break;
                case Week:
                    result = result.plusWeeks(periodLength);
                    break;
                case Year:
                    result = result.plusYears(periodLength);
                    break;
            }
        }

        return result;
    }

    public static void showDeletedToast(Context context, String entityAsText) {
        Toast.makeText(context, entityAsText +
                context.getText(R.string.message_deleted), Toast.LENGTH_LONG).show();
    }

    public static void showAddedToast(Context context, String entityAsText) {
        Toast.makeText(context, entityAsText +
                context.getText(R.string.message_added), Toast.LENGTH_LONG).show();
    }

    public static void showUpdatedToast(Context context, String entityAsText) {
        Toast.makeText(context, entityAsText +
                context.getText(R.string.message_updated), Toast.LENGTH_LONG).show();
    }
}
