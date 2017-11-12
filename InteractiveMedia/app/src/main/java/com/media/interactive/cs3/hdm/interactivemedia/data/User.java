package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.activties.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

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

    private String accessToken = null;

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

    public void setUserType(UserType userType) {
        this.userType = userType;
    }


    public void register(Context context){
        final String url = context.getResources().getString(R.string.web_service_url).concat("/register");
        Log.d(TAG,"url: "+ url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"data: "+ data.toString());

        final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, data, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                final JSONObject payload = response.getJSONObject("payload");
                                accessToken = payload.getString("accessToken");
                                userType = UserType.values()[payload.getInt("authType")];
                                Log.d(TAG,"Successfully registered and logged in with\naccessToken:"+accessToken+"\nuserType:"+userType+"\n");
                            }else {
                                Log.e(TAG,"Received an unsuccessful answer from backend during registration.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG,"Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"Error occurred. "+ error.getMessage());

                    }
                });

        RestRequestQueue.getInstance(context).addToRequestQueue(jsObjRequest);
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
