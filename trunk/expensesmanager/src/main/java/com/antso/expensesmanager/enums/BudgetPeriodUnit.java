package com.antso.expensesmanager.enums;

import java.util.HashMap;
import java.util.Map;

public enum BudgetPeriodUnit {
    Undef(0, "Undef"),
    Day(1, "Day"),
    Week(2, "Week"),
    Month(3, "Month"),
    Year(4, "Year");

    private int intValue;
    private String strValue;
    private static Map<Integer, BudgetPeriodUnit> reverseMap = null;

    BudgetPeriodUnit(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    public String getStringValue() {
        return strValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static BudgetPeriodUnit valueOf(int value) {
        if (reverseMap == null) {
            reverseMap = new HashMap<Integer, BudgetPeriodUnit>();
            for(BudgetPeriodUnit val : BudgetPeriodUnit.values()) {
                reverseMap.put(val.intValue, val);
            }

        }

        return reverseMap.get(value);
    }

    public static BudgetPeriodUnit[] valuesButUndef() {
        return new BudgetPeriodUnit[] {
                Day, Week, Month, Year };
    }

}
