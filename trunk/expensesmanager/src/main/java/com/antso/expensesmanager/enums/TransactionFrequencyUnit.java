package com.antso.expensesmanager.enums;

import java.util.HashMap;
import java.util.Map;

public enum TransactionFrequencyUnit {
    Undef(0, "Undef"),
    Day(1, "Day"),
    Week(2, "Week"),
    Month(3, "Month"),
    Year(4, "Year");

    private int intValue;
    private String strValue;
    private static Map<Integer, TransactionFrequencyUnit> reverseMap = null;

    TransactionFrequencyUnit(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    public String getStringValue() {
        return strValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static TransactionFrequencyUnit valueOf(int value) {
        if (reverseMap == null) {
            reverseMap = new HashMap<Integer, TransactionFrequencyUnit>();
            for(TransactionFrequencyUnit val : TransactionFrequencyUnit.values()) {
                reverseMap.put(val.intValue, val);
            }

        }

        return reverseMap.get(value);
    }

    public static TransactionFrequencyUnit[] valuesButUndef() {
        return new TransactionFrequencyUnit[] {
                Day, Week, Month, Year };
    }

}
