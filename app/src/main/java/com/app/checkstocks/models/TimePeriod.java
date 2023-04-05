package com.app.checkstocks.models;

public class TimePeriod {
    private final int daysOffset;
    private final String dateFormat;

    public TimePeriod(int daysOffset, String dateFormat) {
        this.daysOffset = daysOffset;
        this.dateFormat = dateFormat;
    }

    public int getDaysOffset() {
        return daysOffset;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
