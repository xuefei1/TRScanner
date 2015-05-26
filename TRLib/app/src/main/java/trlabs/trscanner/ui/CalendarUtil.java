package trlabs.trscanner.ui;

import android.graphics.Color;
import android.graphics.ColorFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import trlabs.trscanner.R;
import trlabs.trscanner.cameras.graycamera.Constant;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.utils.FileTools;

/**
 * Created by intern2 on 26/02/2015.
 */
public class CalendarUtil {

    public static String DateToStringYYmmdd(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return (new StringBuilder(Integer.toString(cal.get(Calendar.YEAR)) +
                                  Integer.toString(cal.get(Calendar.MONTH)) +
                                  Integer.toString(cal.get(Calendar.DAY_OF_MONTH)))).toString();
    }

    public static Map<Date, Integer> getScanDatesMap(String RootPath) {
        File[] files = new File(RootPath).listFiles();
        Map<Date, Integer> scanDates = new HashMap<Date, Integer>();
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        for (File file : files) {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for (File subFile : subFiles) {
                    if (FileTools.getFileExtension(subFile.getName()).equals(Constant.LogfileType))
                        try {
                            BufferedReader log = new BufferedReader(new FileReader(subFile));
                            String firstLine = log.readLine();
                            Date date = formatter.parse(firstLine.substring(4).trim());

                            if (scanDates.containsKey(date)) {
                                scanDates.put(date, scanDates.get(date) + 1);
                            } else {
                                scanDates.put(date, 1);
                            }
                            log.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }
        }
        return scanDates;
    }

    public static List<Date> getSortedDates(Map<Date, Integer> dateMap) {
        List<Date> dates = new ArrayList<Date>(dateMap.keySet());
        Collections.sort(dates);
        return dates;
    }

    public static Date getMinDate(Map<Date, Integer> dateMap) {
        List<Date> dates = new ArrayList<Date>(dateMap.keySet());
        if(dates.size() != 0) {
            return Collections.min(dates);
        }
        return Calendar.getInstance().getTime();
    }

    public static Date getMaxDate(Map<Date, Integer> dateMap) {
        List<Date> dates = new ArrayList<Date>(dateMap.keySet());
        if(dates.size() != 0) {
            return Collections.max(dates);
        }
        return Calendar.getInstance().getTime();
    }

    public static List<Date> getScanDatesList(String RootPath) {
        File[] files = new File(RootPath).listFiles();
        List<Date> scanDates = new ArrayList<Date>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");

        for (File file : files) {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for (File subFile : subFiles) {
                    if (FileTools.getFileExtension(subFile.getName()).equals(Constant.LogfileType))
                        try {
                            BufferedReader log = new BufferedReader(new FileReader(subFile));
                            String firstLine = log.readLine();
                            Date date = formatter.parse(firstLine.substring(4).trim());
                            scanDates.add(date);
                            log.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }
        }
        return scanDates;
    }

    /*
    public static Map<String, Integer> getMinYearFromList(List<Date> dates) {
        Map<Date, Integer> scanDates = new HashMap<Date, Integer>(dates.size());
        List<Integer> yearList = new ArrayList<Integer>(dates.size());
        List<Integer> yearList = new ArrayList<Integer>(dates.size());
        List<Integer> yearList = new ArrayList<Integer>(dates.size());
        List<Integer> yearList = new ArrayList<Integer>(dates.size());
        Calendar cal = Calendar.getInstance();
        Iterator<Date> iterator = dates.iterator();

        while (iterator.hasNext()) {
            cal
        }


    }
    */

    public static String getMonthInWords(String month) {
        switch (Integer.parseInt(month)) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
        }
        return null;
    }


    public static String getMonthInWords(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "Aug";
            case 9:
                return "Sept";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
        }
        return null;
    }

    public static String getMonthInNumber(String month) {
        if (month.equals("Jan"))
                return "1";
        else if (month.equals("Feb"))
            return "2";
        else if (month.equals("Mar"))
            return "3";
        else if (month.equals("Apr"))
            return "4";
        else if (month.equals("May"))
            return "5";
        else if (month.equals("June"))
            return "6";
        else if (month.equals("July"))
            return "7";
        else if (month.equals("Aug"))
            return "8";
        else if (month.equals("Sept"))
            return "9";
        else if (month.equals("Oct"))
            return "10";
        else if (month.equals("Nov"))
            return "11";
        else if (month.equals("Dec"))
            return "12";

        return null;
    }

    public static String getDayInWords(String day) {
            switch (Integer.parseInt(day)) {
                case 1:
                    return "Monday";
                case 2:
                    return "Tuesday";
                case 3:
                    return "Wednesday";
                case 4:
                    return "Thursday";
                case 5:
                    return "Friday";
                case 6:
                    return "Saturday";
                case 7:
                    return "Sunday";
            }
            return null;
        }
}