package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.util.Patterns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benny on 05.01.18.
 */

public class Helper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    public static boolean IsUrlValid(String url) {
        if (Patterns.WEB_URL.matcher(url).matches()) {
            return true;
        }
        return false;
    }

    public static boolean IsEmailValid(String email) {
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        return false;
    }

    public static boolean IsGroupNameValid(String groupName) {
        if(groupName == null || groupName.length() < 4){
            return false;
        }
        return true;
    }

    public static String getDateTime() {
        final Date date = new Date(System.currentTimeMillis());
        return formatDate(date);
    }

    public static String formatDate(Date date){
        if(date != null){
            return DATE_FORMAT.format(date);
        }
        return null;
    }

    public static Date ParseDateString(String dateString){
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
