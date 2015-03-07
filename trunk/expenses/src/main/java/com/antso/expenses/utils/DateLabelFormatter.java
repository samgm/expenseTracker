package com.antso.expenses.utils;

import com.jjoe64.graphview.DefaultLabelFormatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class DateLabelFormatter extends DefaultLabelFormatter {
    private final String currency;
    private final boolean inverted;

    public DateLabelFormatter(final String currency, boolean inverted) {
        this.currency = currency;
        this.inverted = inverted;
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            DateTime date = new DateTime((long) value);
            return "\n\n" + date.toString(DateTimeFormat.forPattern("MMM")) + "\n"
                        + date.toString(DateTimeFormat.forPattern("yyy"));
        } else {
            // format as currency
            String label = super.formatLabel(value, false);
            if (inverted && !label.equals("0")) {
                if (label .startsWith("-")) {
                    label = label.substring(1);
                } else {
                    label = "-" + label;
                }
                return currency + " " + label + "  ";
            } else {
                return currency + " " + label + "  ";
            }
        }
    }
}
