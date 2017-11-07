package com.github.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: 吴海旭
 * Date: 2017-10-31
 * Time: 下午3:40
 */
public class TimeUtils {
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String yyyy_MM_dd = "yyyy-MM-dd HH:mm:ss";
    public static final String COMMON_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public static long betweenMills(Date fromDate, Date toDate) {
        return toDate.getTime() - fromDate.getTime();
    }

    public static Date parseDate(String format, String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(dateStr);
    }

    public static String formatDate(String format, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    public static String formatDateNow() {
        SimpleDateFormat sdf = new SimpleDateFormat(yyyyMMddHHmmss);
        return sdf.format(new Date());
    }

    public static String formatDateLimitInHours(Date date,int hours){
        Calendar Cal = Calendar.getInstance();
        Cal.setTime(new Date());
        Cal.add(Calendar.HOUR_OF_DAY,hours*-1);
        return formatDate(yyyy_MM_dd,Cal.getTime());
    }
    
    public static void main(String[] args) {
      /*  Calendar fromDate = Calendar.getInstance();
        fromDate.set(Calendar.MONTH, Calendar.OCTOBER);
        fromDate.set(Calendar.DAY_OF_MONTH, 15);
        fromDate.set(Calendar.HOUR_OF_DAY, 0);
        fromDate.set(Calendar.MINUTE, 0);
        fromDate.set(Calendar.SECOND, 0);
        System.out.println(fromDate.getTime());
        Calendar toDate = Calendar.getInstance();
        toDate.set(Calendar.MONTH, Calendar.OCTOBER);
        toDate.set(Calendar.DAY_OF_MONTH, 16);
        toDate.set(Calendar.HOUR_OF_DAY, 0);
        toDate.set(Calendar.MINUTE, 0);
        toDate.set(Calendar.SECOND, 0);
        System.out.println(toDate.getTime());
        System.out.println(betweenDays(fromDate.getTime(), toDate.getTime()));
        System.out.println(betweenMills(fromDate.getTime(), toDate.getTime()));*/
    }
}
