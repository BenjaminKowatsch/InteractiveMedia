package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by benny on 31.10.17.
 */

public class Login {
    private static final String TAG = "Login";
    private static final Login ourInstance = new Login();
    private long id;
    private User user;
    private String hashedPassword = null;
    private UserType userType = null;
    private String accessToken = null;
    private CallbackListener<JSONObject,Exception> onUserDataSet = null;

    private DatabaseProviderHelper helper;
    private ContentResolver contentResolver = null;


    private Login() {
    }

    public static Login getInstance() {
        return ourInstance;
    }

    public void clear() {
        id = 0;
        user = new User();
        hashedPassword = null;
        userType = null;
        accessToken = null;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public User getUser() {
        return user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void initUserData(Context context){
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/user");
        Log.d(TAG,"Get: "+ url);
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean success = response.getBoolean("success");
                    Log.d(TAG,"user: "+ response);
                    if(success){
                        final JSONObject payload = response.getJSONObject("payload");
                        user.setEmail(payload.getString("email"));
                        try {
                            final String username = payload.getString("username");
                            user.setUsername(username);
                        } catch(JSONException e){
                            Log.d(TAG, "Username is not set.");
                        }
                        user.setUserId(payload.getString("userId"));

                        JSONArray groupIds = null;
                        try {
                            payload.getJSONArray("groupIds");
                        } catch (JSONException e){
                            Log.d(TAG,"No groups exist for this user");
                        }
                        if(groupIds != null) {
                            Log.d(TAG, "Before Removal: " + groupIds.toString());
                            helper.removeExistingGroupIds(groupIds);
                            Log.d(TAG, "After Removal: " + groupIds.toString());
                            requestNewGroups(groupIds);
                        }
                        user.setSync(true);
                        helper.upsertUser(user);
                        Log.d(TAG, "Upserted User: "+ user);
                    }else {
                        Log.e(TAG,"Error while setting user data.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(onUserDataSet != null){
                    onUserDataSet.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Error while setting user data.");
                if(onUserDataSet != null){
                    onUserDataSet.onFailure(error);
                }
            }
        });
        jsonObjectRequest.setShouldCache(false);
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void requestNewGroups(JSONArray groupIds){

    }

    public boolean loginResponseHandler(JSONObject response) {

        Log.d("User: ", "loginResponseHandler: Thread Id: "
            + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        Log.d(TAG, "Response: " + response.toString());
        try {
            if (response.getBoolean("success")) {
                final JSONObject payload = response.getJSONObject("payload");
                setAccessToken(payload.getString("accessToken"));
                setUserType(UserType.values()[payload.getInt("authType")]);
                Log.d(TAG, "Successfully registered and logged in with\naccessToken:"
                    + accessToken + "\nuserType:" + userType + "\n");
                if (id == 0) {
                    cacheCredentials(this);
                }

                return true;
            }

            Log.e(TAG, "Received an unsuccessful answer from backend during registration.");
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean cacheCredentials(Login login) {
        if (contentResolver != null) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(LoginTable.COLUMN_USERNAME, login.getUser().getUsername());
            contentValues.put(LoginTable.COLUMN_HASHED_PASSWORD, login.getHashedPassword());
            contentValues.put(LoginTable.COLUMN_LOGIN_TYPE, login.getUserType().getValue());
            final Uri result = contentResolver.insert(DatabaseProvider.CONTENT_LOGIN_URI, contentValues);
            Log.d(TAG, "cacheCredentials: Adding " + login + " to " + DatabaseProvider.CONTENT_LOGIN_URI);
            long id = Long.parseLong(result.getLastPathSegment());
            if (id > 0) {
                login.setId(id);
                return true;
            } else {
                Log.e(TAG, "Could not cache credentials, error at database.");
                return false;
            }
        }
        Log.e(TAG, "Could not cache credentials, contentResolver is null.");
        return false;
    }

    private boolean logoutResponseHandler(JSONObject response) {
        Log.d(TAG, "Response: " + response.toString());
        try {
            if (response.getBoolean("success")) {
                deleteLogin(this);
                this.clear();
                Log.d(TAG, "Received an successful answer from backend during logout.");
                return true;
            }

            Log.e(TAG, "Received an unsuccessful answer from backend during logout.");
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean deleteLogin(Login login) {
        if (contentResolver != null) {
            final int result = contentResolver.delete(DatabaseProvider.CONTENT_LOGIN_URI,
                LoginTable.COLUMN_ID + "=?", new String[] {String.valueOf(login.getId())});
            Log.d(TAG, "delete Login: " + login + " to "
                + DatabaseProvider.CONTENT_LOGIN_URI + "  " + result);
            return result > 0;
        }
        Log.e(TAG, "Could not cache credentials, contentResolver is null.");
        return false;
    }

    public void register(Context context, final CallbackListener<JSONObject,Exception> callbackListener) {

        contentResolver = context.getContentResolver();
        helper = new DatabaseProviderHelper(context.getContentResolver());

        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(response);
                callbackListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callbackListener.onFailure(error);
            }
        });
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void login(Context context, CallbackListener<JSONObject, Exception> callbackListener) {

        contentResolver = context.getContentResolver();
        helper = new DatabaseProviderHelper(context.getContentResolver());

        boolean usernameAndHashedPassword = user.getUsername() == null || user.getUsername().length() <= 0
            || hashedPassword == null || hashedPassword.length() <= 0;
        Log.d(TAG, "usernameAndHashedPassword: " + usernameAndHashedPassword);
        Log.d(TAG, "user: " + toString());

        if (userType == null) {
            helper.checkForCachedCredentials(this);

            if (userType == null) {
                callbackListener.onFailure(new Exception("No cached credentials available."));
                return; //future;
            }
        }
        // Login with latest cached account type

        switch (userType.getValue()) {
            case 0: // UserType.DEFAULT
                cachedDefaultLogin(context, callbackListener);
                break;
            case 1: //UserType.GOOGLE
                cachedGoogleLogin(context, callbackListener);
                break;
            case 2: //UserType.FACEBOOK
                cachedFacebookLogin(context, callbackListener);
                break;
            default:
                callbackListener.onFailure(new Exception("No cached credentials available."));
                break;
        }

    }

    private void cachedGoogleLogin(final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {
        // Check if the accessToken is not set
        // If the accessToken is not set there is no need to check the cache
        if (accessToken == null) {

            final String serverClientId = context.getString(R.string.server_client_id);
            final GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build();
            final GoogleApiClient googleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();

            final OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi
                .silentSignIn(googleApiClient);
            if (pendingResult.isDone()) {
                final GoogleSignInResult result = pendingResult.get();
                final GoogleSignInAccount account = result.getSignInAccount();
                accessToken = account.getIdToken();
            }
        }

        Log.d(TAG, "Google Token: " + accessToken);

        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/login?type=1");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("accessToken", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(response);
                initUserData(context);
                callbackListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callbackListener.onFailure(error);
            }
        });
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void cachedFacebookLogin(final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {
        // Check if the accessToken is not set
        // If the accessToken is not set there is no need to check the cache
        if (accessToken == null) {
            // If the access token is available already assign it.
            final AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();

            if (facebookAccessToken != null) {
                Log.d(TAG, "Facbook AccessToken Refresh Date: " + facebookAccessToken.getLastRefresh());
                accessToken = facebookAccessToken.getToken();
            }
        }
        Log.d(TAG, "Facbook AccessToken: " + accessToken);
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/login?type=2");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("accessToken", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(response);
                initUserData(context);
                callbackListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callbackListener.onFailure(error);
            }
        });
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void cachedDefaultLogin(final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {

        // Send data to Backend and validate data

        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/login?type=0");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", user.getUsername());
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(response);
                initUserData(context);
                callbackListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callbackListener.onFailure(error);
            }
        });
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "Login{"
            + "id='" + id + '\''
            + ", user='" + user + '\''
            + ", hashedPassword='" + hashedPassword + '\''
            + ", userType=" + userType
            + ", accessToken=" + accessToken
            + '}';
    }

    public void logout(Activity activity, final CallbackListener<JSONObject, Exception> callbackListener) {


        // Local Facebook logout
        LoginManager.getInstance().logOut();
        // Local Google logout
        final String serverClientId = activity.getString(R.string.server_client_id);
        final GoogleSignInOptions signInOptions = new GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build();
        final GoogleApiClient googleApiClient = new GoogleApiClient
            .Builder(activity)
            .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
            .build();
        if (googleApiClient.isConnected()) {
            Auth.GoogleSignInApi
                .signOut(googleApiClient)
                .setResultCallback(new ResolvingResultCallbacks<Status>(activity, 0) {
                    @Override
                    public void onSuccess(@NonNull Status status) {
                        Log.i(TAG, "Google signed out.");
                    }

                    @Override
                    public void onUnresolvableFailure(@NonNull Status status) {
                        Log.e(TAG, "Google signed out failed.");
                    }
                });
        }
        // Logout at backend
        final String url = activity.getResources().getString(R.string.web_service_url).concat("/v1/users/logout");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("accessToken", accessToken);
            data.put("authType", userType.getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                logoutResponseHandler(response);
                callbackListener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callbackListener.onFailure(error);
            }
        });

        RestRequestQueue.getInstance(activity).addToRequestQueue(jsonObjectRequest);
    }

    public void setOnUserDataSet(CallbackListener<JSONObject, Exception> onUserDataSet) {
        this.onUserDataSet = onUserDataSet;
    }
}
