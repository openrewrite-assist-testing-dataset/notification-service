package com.notification.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class DateTimeUtil {
    
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        DISPLAY_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static String formatISO(Date date) {
        if (date == null) {
            return null;
        }
        return ISO_FORMAT.format(date);
    }
    
    public static String formatDisplay(Date date) {
        if (date == null) {
            return null;
        }
        return DISPLAY_FORMAT.format(date);
    }
    
    public static Date parseISO(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return ISO_FORMAT.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Date getCurrentTime() {
        return new Date();
    }
    
    public static Date addMinutes(Date date, int minutes) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
    
    public static Date addHours(Date date, int hours) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hours);
        return calendar.getTime();
    }
    
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
    
    public static boolean isBefore(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.before(date2);
    }
    
    public static boolean isAfter(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.after(date2);
    }
    
    public static long getTimeDifferenceInMinutes(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        return (end.getTime() - start.getTime()) / (1000 * 60);
    }
}