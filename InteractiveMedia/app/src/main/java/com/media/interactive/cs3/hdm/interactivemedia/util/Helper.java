package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.util.Patterns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



/**
 * Created by benny on 05.01.18.
 */

public class Helper {

    /**
     * The Constant READABLE_DATE_FORMAT.
     */
    public static final SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * The Constant DATE_FORMAT.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    /**
     * Checks if is url valid.
     *
     * @param url the url
     * @return true, if is url valid
     */
    public static boolean isUrlValid(String url) {
        if (Patterns.WEB_URL.matcher(url).matches()) {
            return true;
        }
        return false;
    }

    /**
     * Copy file.
     *
     * @param src the src
     * @param dst the dst
     */
    public static void copyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            try {
                OutputStream out = new FileOutputStream(dst);
                try {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Checks if is email valid.
     *
     * @param email the email
     * @return true, if is email valid
     */
    public static boolean isEmailValid(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is group name valid.
     *
     * @param groupName the group name
     * @return true, if is group name valid
     */
    public static boolean isGroupNameValid(String groupName) {
        if (groupName == null || groupName.length() < 4) {
            return false;
        }
        return true;
    }

    /**
     * Gets the date time.
     *
     * @return the date time
     */
    public static String getDateTime() {
        final Date date = new Date(System.currentTimeMillis());
        return formatDate(date);
    }

    /**
     * Format date.
     *
     * @param date the date
     * @return the string
     */
    public static String formatDate(Date date) {
        if (date != null) {
            return DATE_FORMAT.format(date);
        }
        return null;
    }

    /**
     * Parses the date string.
     *
     * @param dateString the date string
     * @return the date
     */
    public static Date parseDateString(String dateString) {
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
