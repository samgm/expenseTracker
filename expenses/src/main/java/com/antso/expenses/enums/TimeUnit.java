package com.antso.expenses.enums;

import android.content.Context;

import com.antso.expenses.R;

import java.util.HashMap;
import java.util.Map;

public enum TimeUnit {
    Undef(0, "Undef", R.string.time_unit_undef, R.string.time_unit_undef),
    Day(1, "Day", R.string.time_unit_day, R.string.time_unit_days),
    Week(2, "Week", R.string.time_unit_week, R.string.time_unit_weeks),
    Month(3, "Month", R.string.time_unit_month, R.string.time_unit_months),
    Year(4, "Year", R.string.time_unit_year, R.string.time_unit_years);

    private int intValue;
    private String strValue;
    private int strId;
    private int strIdPlural;
    private static Map<Integer, TimeUnit> reverseMap = null;

    TimeUnit(int intValue, String strValue, int strId, int strIdPlural) {
        this.intValue = intValue;
        this.strValue = strValue;
        this.strId = strId;
        this.strIdPlural = strIdPlural;
    }

    public String getStringValue() {
        return strValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static TimeUnit valueOf(int value) {
        if (reverseMap == null) {
            reverseMap = new HashMap<Integer, TimeUnit>();
            for(TimeUnit val : TimeUnit.values()) {
                reverseMap.put(val.intValue, val);
            }

        }

        return reverseMap.get(value);
    }

    public static TimeUnit[] valuesButUndef() {
        return new TimeUnit[] {
                Day, Week, Month, Year };
    }

    public String getLangStringValue(Context context, boolean plural) {
        if (plural) {
            return context.getText(strIdPlural).toString();
        } else {
            return context.getText(strId).toString();
        }
    }

}
