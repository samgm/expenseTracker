package com.antso.expensesmanager.enums;

import android.content.Context;

import com.antso.expensesmanager.R;

import java.util.HashMap;
import java.util.Map;

public enum TimeUnit {
    Undef(0, "Undef", R.string.time_unit_undef),
    Day(1, "Day", R.string.time_unit_day),
    Week(2, "Week", R.string.time_unit_week),
    Month(3, "Month", R.string.time_unit_month),
    Year(4, "Year", R.string.time_unit_year);

    private int intValue;
    private String strValue;
    private int strId;
    private static Map<Integer, TimeUnit> reverseMap = null;

    TimeUnit(int intValue, String strValue, int strId) {
        this.intValue = intValue;
        this.strValue = strValue;
        this.strId = strId;
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

    public String getLangStringValue(Context context) {
        return context.getText(strId).toString();
    }

}
