package com.antso.expenses.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by asolano on 5/11/2014.
 */
public enum TransactionDirection {
    Undef(0, "Undef"),
    In(1, "In"),
    Out(2, "Out");

    private int intValue;
    private String strValue;
    private static Map<Integer, TransactionDirection> reverseMap = null;

    TransactionDirection(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    public String getStringValue() {
        return strValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static TransactionDirection valueOf(int value) {
        if (reverseMap == null) {
            reverseMap = new HashMap<Integer, TransactionDirection>();
            for(TransactionDirection val : TransactionDirection.values()) {
                reverseMap.put(val.intValue, val);
            }

        }

        return reverseMap.get(value);
    }
}
