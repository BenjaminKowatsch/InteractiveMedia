package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by benny on 31.10.17.
 */

public class Login {
    private static final Login ourInstance = new Login();
    private long id;
    private String username = null;
    private String email = null;
    private String hashedPassword = null;
    private UserType userType = null;
    private String accessToken = null;
    //private DatabaseHelper databaseHelper = null;
    private ContentResolver contentResolver = null;

    private Login() {
    }

    public static Login getInstance() {
        return ourInstance;
    }

    public void clear() {
        id = 0;
        username = null;
        email = null;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public boolean loginResponseHandler(JSONObject response) {

        Log.d("User: ", "loginResponseHandler: Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        Log.d(TAG, "Response: " + response.toString());
        try {
            if (response.getBoolean("success")) {
                final JSONObject payload = response.getJSONObject("payload");
                setAccessToken(payload.getString("accessToken"));
                setUserType(UserType.values()[payload.getInt("authType")]);
                Log.d(TAG, "Successfully registered and logged in with\naccessToken:" + accessToken + "\nuserType:" + userType + "\n");
                if(id == 0) {
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
            contentValues.put(LoginTable.COLUMN_USERNAME, login.getUsername());
            contentValues.put(LoginTable.COLUMN_HASHED_PASSWORD, login.getHashedPassword());
            contentValues.put(LoginTable.COLUMN_LOGIN_TYPE, login.getUserType().getValue());
            final Uri result = contentResolver.insert(DatabaseProvider.CONTENT_LOGIN_URI, contentValues);
            Log.d(TAG, "cacheCredentials: Adding " + login + " to " + DatabaseProvider.CONTENT_LOGIN_URI);
            long id = Long.parseLong(result.getLastPathSegment());
            if(id > 0) {
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

    private boolean checkForCachedCredentials(Login login) {
        if (contentResolver != null) {
            boolean result = false;
            final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_LOGIN_URI, null, null, null, LoginTable.COLUMN_CREATED_AT + " DESC LIMIT 1");
            result = cursor.getCount() > 0;
            while (cursor.moveToNext()) {
                login.setId(cursor.getLong(0));
                login.setUsername(cursor.getString(1));
                login.setHashedPassword(cursor.getString(2));
                login.setEmail(cursor.getString(3));
                login.setUserType(UserType.values()[cursor.getInt(4)]);
                Log.d(TAG, "Latest Credentials cache: " + cursor.getString(4) + " " + login);
            }
            return result;
        }
        Log.e(TAG, "Could not find cached credentials, contentResolver is null.");
        return false;
    }

    private boolean logoutResponseHandler(JSONObject response) {
        Log.d(TAG, "Response: " + response.toString());
        try {
            if (response.getBoolean("success")) {
                deleteUser(this);
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

    private boolean deleteUser(Login login) {
        if (contentResolver != null) {
            final int result = contentResolver.delete(DatabaseProvider.CONTENT_LOGIN_URI, LoginTable.COLUMN_ID + "=?", new String[]{String.valueOf(login.getId())});
            Log.d(TAG, "delete Login: Adding " + login + " to " + DatabaseProvider.CONTENT_LOGIN_URI+ "  "+ result);
            return result > 0;
        }
        Log.e(TAG, "Could not cache credentials, contentResolver is null.");
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Void> register(Context context) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        contentResolver = context.getContentResolver();

        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        RestRequestQueue.getInstance(context)
                .send(url, Request.Method.POST, data)
                .thenApply(this::loginResponseHandler)
                .thenAccept(loginResult -> {
                    if (loginResult) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new Throwable("Login Failed"));
                    }
                })
                .exceptionally(error -> {
                    throw new RuntimeException(error.getMessage(), error.getCause());
                });
        return future;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Void> login(Context context) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        contentResolver = context.getContentResolver();

        boolean usernameAndHashedPassword = username == null || username.length() <= 0 ||
                hashedPassword == null || hashedPassword.length() <= 0;
        Log.d(TAG, "usernameAndHashedPassword: " + usernameAndHashedPassword);
        Log.d(TAG, "user: " + toString());

        if (userType == null) {
            checkForCachedCredentials(this);

            if (userType == null) {
                future.completeExceptionally(new Throwable("No cached credentials available."));
                return future;
            }
        }
        // Login with latest cached account type

        switch (userType.getValue()) {
            case 0: // UserType.DEFAULT
                cachedDefaultLogin(future, context);
                break;
            case 1: //UserType.GOOGLE
                cachedGoogleLogin(future, context);
                break;
            case 2: //UserType.FACEBOOK
                cachedFacebookLogin(future, context);
                break;
            default:
                future.completeExceptionally(new Throwable("No cached credentials available."));
                break;
        }

        return future;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cachedGoogleLogin(CompletableFuture<Void> future, Context context) {
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

            final OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
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

        RestRequestQueue.getInstance(context)
                .send(url, Request.Method.POST, data)
                .thenApply(this::loginResponseHandler)
                .thenAccept(loginResult -> {
                    if (loginResult) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new Throwable("Google Login failed"));
                    }
                })
                .exceptionally(error -> {
                    Log.e("User: ", "Exceptionally Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
                    future.completeExceptionally(error);
                    throw new RuntimeException(error.getMessage());
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cachedFacebookLogin(CompletableFuture<Void> future, Context context) {
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

        RestRequestQueue.getInstance(context)
                .send(url, Request.Method.POST, data)
                .thenApply(this::loginResponseHandler)
                .thenAccept(loginResult -> {
                    if (loginResult) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new Throwable("Facebook Login failed"));
                    }
                })
                .exceptionally(error -> {
                    Log.e("User: ", "Exceptionally Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
                    future.completeExceptionally(error);
                    throw new RuntimeException(error.getMessage());
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cachedDefaultLogin(CompletableFuture<Void> future, Context context) {

        // Send data to Backend and validate data

        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/login?type=0");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        RestRequestQueue.getInstance(context)
                .send(url, Request.Method.POST, data)
                .thenApply(this::loginResponseHandler)
                .thenAccept(loginResult -> {
                    if (loginResult) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new Throwable("Default Logout failed"));
                    }
                })
                .exceptionally(error -> {

                    Log.e("User: ", "Exceptionally Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
                    future.completeExceptionally(error);
                    throw new RuntimeException(error.getMessage());
                });
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
                ", accessToken=" + accessToken +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Void> logout(Context context) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        // Local Facebook logout
        LoginManager.getInstance().logOut();
        // Local Google logout
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
        if (googleApiClient.isConnected()) {
            Auth.GoogleSignInApi
                    .signOut(googleApiClient)
                    .setResultCallback((status) -> {
                        Log.i(TAG, "Google signed out.");
                    });
        }
        // Logout at backend
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/logout");
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("accessToken", accessToken);
            data.put("authType", userType.getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        RestRequestQueue.getInstance(context)
                .send(url, Request.Method.POST, data)
                .thenApply(this::logoutResponseHandler)
                .thenAccept(loginResult -> {
                    if (loginResult) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new Throwable("Default Login failed"));
                    }
                })
                .exceptionally(error -> {
                    future.completeExceptionally(error);
                    throw new RuntimeException(error.getMessage());
                });


        return future;
    }
}
