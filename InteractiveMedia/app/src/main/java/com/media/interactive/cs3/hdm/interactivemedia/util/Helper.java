package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.util.Patterns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benny on 05.01.18.
 */

public class Helper {

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

    public static String GetDateTime() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final Date date = new Date();
        return dateFormat.format(date);
    }

}
