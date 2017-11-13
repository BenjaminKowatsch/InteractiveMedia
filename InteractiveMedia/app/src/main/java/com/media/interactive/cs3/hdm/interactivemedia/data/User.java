package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by benny on 31.10.17.
 */

public class User {
    private static final User ourInstance = new User();
    private String id = null;
    private String username = null;
    private String email = null;
    private String hashedPassword = null;
    private UserType userType;
    private DatabaseHelper databaseHelper;
    private String accessToken = null;

    private User() {
    }

    public static User getInstance() {
        return ourInstance;
    }

    public void clear() {
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


    public boolean loginResponseHandler(JSONObject response) {
        Log.d(TAG, "Response: " + response.toString());
        try {
            if (response.getBoolean("success")) {
                final JSONObject payload = response.getJSONObject("payload");
                accessToken = payload.getString("accessToken");
                userType = UserType.values()[payload.getInt("authType")];
                Log.d(TAG, "Successfully registered and logged in with\naccessToken:" + accessToken + "\nuserType:" + userType + "\n");
                databaseHelper.cacheCredentials(this);
                return true;
            }

            Log.e(TAG, "Received an unsuccessful answer from backend during registration.");
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Void> register(Context context) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        final String url = context.getResources().getString(R.string.web_service_url).concat("/register");
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

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Void> login(Context context) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        boolean usernameAndHashedPassword = username == null || username.length() <= 0 ||
                hashedPassword == null || hashedPassword.length() <= 0;
        Log.d(TAG, "usernameAndHashedPassword: " + usernameAndHashedPassword);
        Log.d(TAG, "user: " + toString());

        databaseHelper.checkForCachedCredentials(this);

        if (userType == null) {
            future.completeExceptionally(new Throwable("No cached credentials available."));
            return future;
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
            final String accessToken = account.getIdToken();
            Log.d(TAG, "Google Token: " + accessToken);

            final String url = context.getResources().getString(R.string.web_service_url).concat("/google_login");
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
                        future.completeExceptionally(error);
                        throw new RuntimeException(error.getMessage());
                    });

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cachedFacebookLogin(CompletableFuture<Void> future, Context context) {
        // If the access token is available already assign it.
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            Log.d(TAG, "Facbook AccessToken Refresh Date: " + accessToken.getLastRefresh());
            final String accessTokenString = accessToken.getToken();
            Log.d(TAG, "Facbook AccessToken: " + accessTokenString);
            final String url = context.getResources().getString(R.string.web_service_url).concat("/facebook_login");
            Log.d(TAG, "url: " + url);
            final JSONObject data = new JSONObject();
            try {
                data.put("accessToken", accessTokenString);
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
                        future.completeExceptionally(error);
                        throw new RuntimeException(error.getMessage());
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cachedDefaultLogin(CompletableFuture<Void> future, Context context) {

        // Send data to Backend and validate data

        final String url = context.getResources().getString(R.string.web_service_url).concat("/launometer_login");
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
                        future.completeExceptionally(new Throwable("Default Login failed"));
                    }
                })
                .exceptionally(error -> {
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
                ", databaseHelper=" + databaseHelper +
                '}';
    }
}
