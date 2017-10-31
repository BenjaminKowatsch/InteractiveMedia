package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by benny on 31.10.17.
 */

public class User {
    private String id = null;
    private String username = null;
    private String email = null;
    private String hashedPassword = null;
    private UserType userType;
    private DatabaseHelper databaseHelper;

    private static final User ourInstance = new User();

    public static User getInstance() {
        return ourInstance;
    }

    private User() {
    }

    public void clear(){
        id = null;
        username = null;
        email = null;
        hashedPassword = null;
        databaseHelper = null;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserType getUserType() {
        return userType;
    }

    private void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public boolean login(){
        boolean result = false;
        boolean usernameAndHashedPassword = username == null || username.length() <= 0 ||
                hashedPassword == null || hashedPassword.length() <= 0;
        Log.d(TAG,"usernameAndHashedPassword: "+ usernameAndHashedPassword);
        Log.d(TAG,"user: "+ toString());

        if(databaseHelper.checkForCachedCredentials(this) || usernameAndHashedPassword == false ){
                //TODO: Send data to Backend and validate data
                boolean loginSuccessful = true;
                if(loginSuccessful) {
                    databaseHelper.cacheCredentials(this);
                    result = true;
                }
        }
        Log.d(TAG,"login: "+ result);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", hashedPassword='" + hashedPassword + '\'' +
                ", userType=" + userType +
                ", databaseHelper=" + databaseHelper +
                '}';
    }
}
