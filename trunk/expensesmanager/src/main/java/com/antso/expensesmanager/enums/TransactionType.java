package com.antso.expensesmanager.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by asolano on 5/11/2014.
 */
public enum TransactionType {
    Undef(0, "Undef"),
    Single(1, "Single"),
    Recurrent(2, "Recurrent"),
    Transfer(3, "Transfer");

    private int intValue;
    private String strValue;
    private static Map<Integer, TransactionType> reverseMap = null;

    TransactionType(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    public String getStringValue() {
        return strValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static TransactionType valueOf(int value) {
        if (reverseMap == null) {
            reverseMap = new HashMap<Integer, TransactionType>();
            for(TransactionType val : TransactionType.values()) {
                reverseMap.put(val.intValue, val);
            }

        }

        return reverseMap.get(value);
    }

}
