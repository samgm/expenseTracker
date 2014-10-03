package com.antso.expensesmanager.enums;

import java.util.HashMap;
import java.util.Map;

public enum TimeUnit {
    Undef(0, "Undef"),
    Day(1, "Day"),
    Week(2, "Week"),
    Month(3, "Month"),
    Year(4, "Year");

    private int intValue;
    private String strValue;
    private static Map<Integer, TimeUnit> reverseMap = null;

    TimeUnit(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
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

}
