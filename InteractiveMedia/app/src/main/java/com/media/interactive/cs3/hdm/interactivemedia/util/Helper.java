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
  public static final SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

  public static boolean isUrlValid(String url) {
    if (Patterns.WEB_URL.matcher(url).matches()) {
      return true;
    }
    return false;
  }

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


  public static boolean isEmailValid(String email) {
    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      return true;
    }
    return false;
  }

  public static boolean isGroupNameValid(String groupName) {
    if (groupName == null || groupName.length() < 4) {
      return false;
    }
    return true;
  }

  public static String getDateTime() {
    final Date date = new Date(System.currentTimeMillis());
    return formatDate(date);
  }

  public static String formatDate(Date date) {
    if (date != null) {
      return DATE_FORMAT.format(date);
    }
    return null;
  }

  public static Date parseDateString(String dateString) {
    try {
      return DATE_FORMAT.parse(dateString);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

}
