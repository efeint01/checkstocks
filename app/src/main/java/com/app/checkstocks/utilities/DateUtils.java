package com.app.checkstocks.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class DateUtils {

    @NonNull
    public static String getFormattedDate(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, daysOffset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(calendar.getTime());
    }

    public static long dateToTimestamp(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy",Locale.getDefault());
        Date date = dateFormat.parse(dateString);
        return date != null ? date.getTime() : 0;
    }

    public static String getTodayDate() {
        // Get the current date
        Date currentDate = new Date();
        // Create a date formatter
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Format the date as a string
        return sdf.format(currentDate);
    }

    public static int getYTDDays(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar today = Calendar.getInstance();
        try {
            today.setTime(sdf.parse(dateString));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // Set the start date of the year
        Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.MONTH, Calendar.JANUARY);
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        startDate.set(Calendar.YEAR, today.get(Calendar.YEAR));
        // Use the YTD calculation formula and return the result as an integer number of days
        long millisecondsPerDay = 24 * 60 * 60 * 1000;
        return (int) ((today.getTimeInMillis() - startDate.getTimeInMillis()) / millisecondsPerDay);
    }
}
