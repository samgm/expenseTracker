package com.antso.expenses.utils;

import com.jjoe64.graphview.DefaultLabelFormatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class DateLabelFormatter extends DefaultLabelFormatter {
    final String currency;

    public DateLabelFormatter(final String currency) {
        this.currency = currency;
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
            return currency + " " + super.formatLabel(value, isValueX) + "  ";
        }
    }
}
