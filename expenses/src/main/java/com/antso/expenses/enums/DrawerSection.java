package com.antso.expenses.enums;

import java.util.HashMap;
import java.util.Map;

public enum DrawerSection {
    TRANSACTIONS(0),
    ACCOUNTS(1),
    BUDGETS(2),
    STATISTICS(3),
    SETTINGS(4),
    ABOUT(5);


    private int intValue;
    private static Map<Integer, DrawerSection> reverseMap = null;

    DrawerSection(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static DrawerSection valueOf(int value) {
        if (reverseMap == null) {
            reverseMap = new HashMap<Integer, DrawerSection>();
            for(DrawerSection val : DrawerSection.values()) {
                reverseMap.put(val.intValue, val);
            }

        }

        return reverseMap.get(value);
    }
}
