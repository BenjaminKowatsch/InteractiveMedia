package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
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
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.volley.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.volley.RestRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;




/**
 * Created by benny on 31.10.17.
 */

public class Login {

    /**
     * The Constant TAG.
     */
    private static final String TAG = Login.class.getSimpleName();

    /**
     * The Constant ourInstance.
     */
    private static final Login ourInstance = new Login();

    /**
     * The user.
     */
    private User user;

    /**
     * The access token.
     */
    private String accessToken = null;

    /**
     * The on user data set list.
     */
    private List<CallbackListener<JSONObject, Exception>> onUserDataSetList = null;

    /**
     * The helper.
     */
    private DatabaseProviderHelper helper;

    /**
     * The synchronisation helper.
     */
    private SynchronisationHelper synchronisationHelper = null;

    /**
     * The id.
     */
    // DB entities
    private long id;

    /**
     * The hashed password.
     */
    private String hashedPassword = null;

    /**
     * The user type.
     */
    private UserType userType = null;

    /**
     * Instantiates a new login.
     */
    private Login() {
        clear();
    }

    /**
     * Gets the single instance of Login.
     *
     * @return single instance of Login
     */
    public static Login getInstance() {
        return ourInstance;
    }

    /**
     * Clear.
     */
    public void clear() {
        id = 0;
        user = new User();
        hashedPassword = null;
        userType = null;
        accessToken = null;
        onUserDataSetList = new ArrayList<>();
    }

    /**
     * Update fcm token.
     *
     * @param context the context
     */
    public void updateFcmToken(final Context context) {
        final String url = context.getResources().getString(R.string.web_service_url).concat(context.getString(R.string.requestPathUpdateUser));
        final JSONObject data = new JSONObject();
        try {
            data.put("fcmToken", user.getFcmToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.PUT, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {
                try {
                    final boolean success = response.getBoolean("success");
                    if (success) {
                        Log.d(TAG, "Updated fcmToken");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while setting user data.");
            }
        });
        jsonObjectRequest.setShouldCache(false);
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Update image url.
     *
     * @param context the context
     */
    public void updateImageUrl(final Context context) {
        final String url = context.getResources().getString(R.string.web_service_url).concat(context.getString(R.string.requestPathUpdateUser));
        final JSONObject data = new JSONObject();
        try {
            data.put("imageUrl", user.getImageUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.PUT, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {
                try {
                    final boolean success = response.getBoolean("success");
                    if (success) {
                        Log.d(TAG, "Updated imageUrl");
                        helper.upsertUser(user);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while setting user data.");
            }
        });
        jsonObjectRequest.setShouldCache(false);
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Send dummy push notification.
     *
     * @param context the context
     */
    private void sendDummyPushNotification(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final String url = context.getResources().getString(R.string.web_service_url)
                    .concat(context.getString(R.string.requestPathTestPushNotification));
                final JSONObject data = new JSONObject();
                try {
                    data.put("dryRun", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
                    Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            final boolean success = response.getBoolean("success");
                            if (success) {
                                Log.d(TAG, "Successfully send a push notification");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error while setting user data.");
                    }
                });
                jsonObjectRequest.setShouldCache(false);
                RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
            }
        }).start();
    }

    /**
     * Gets the hashed password.
     *
     * @return the hashed password
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Sets the hashed password.
     *
     * @param hashedPassword the new hashed password
     */
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the user type.
     *
     * @return the user type
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Sets the user type.
     *
     * @param userType the new user type
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /**
     * Login response handler.
     *
     * @param context          the context
     * @param response         the response
     * @param callbackListener the callback listener
     * @return true, if successful
     */
    public boolean loginResponseHandler(Context context, JSONObject response, CallbackListener<JSONObject, Exception> callbackListener) {
        boolean result = false;
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
                    helper.cacheCredentials(this);
                }
                result = true;
            } else {
                Log.e(TAG, "Received an unsuccessful answer from backend during registration.");
                result = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            result = false;
        }
        if (synchronisationHelper == null) {
            synchronisationHelper = new SynchronisationHelper(helper, onUserDataSetList);
        }
        synchronisationHelper.synchronize(context, response, callbackListener);
        return result;
    }

    /**
     * To content values.
     *
     * @return the content values
     */
    public ContentValues toContentValues() {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(LoginTable.COLUMN_USERNAME, getUser().getUsername());
        contentValues.put(LoginTable.COLUMN_HASHED_PASSWORD, getHashedPassword());
        contentValues.put(LoginTable.COLUMN_LOGIN_TYPE, getUserType().getValue());
        return contentValues;
    }

    /**
     * Logout response handler.
     *
     * @param response the response
     * @return true, if successful
     */
    private boolean logoutResponseHandler(JSONObject response) {
        Log.d(TAG, "Response: " + response.toString());
        try {
            if (response.getBoolean("success")) {
                helper.deleteLogin(this);
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


    /**
     * Register.
     *
     * @param context          the context
     * @param callbackListener the callback listener
     */
    public void register(final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {

        helper = new DatabaseProviderHelper(context.getContentResolver());

        final String url = context.getResources().getString(R.string.web_service_url).concat(context.getString(R.string.requestPathRegisterUser));
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("imageUrl", JSONObject.NULL);
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(context, response, callbackListener);
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

    /**
     * Login.
     *
     * @param context          the context
     * @param callbackListener the callback listener
     */
    public void login(Context context, CallbackListener<JSONObject, Exception> callbackListener) {

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

    /**
     * Cached google login.
     *
     * @param context          the context
     * @param callbackListener the callback listener
     */
    private void cachedGoogleLogin(final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {
        // Check if the accessToken is not set
        // If the accessToken is set there is no need to check the cache
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

        final String url = context.getResources().getString(R.string.web_service_url).concat(context.getString(R.string.requestPathGoogleLogin));
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("accessToken", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Google Login data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(context, response, callbackListener);
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

    /**
     * Cached facebook login.
     *
     * @param context          the context
     * @param callbackListener the callback listener
     */
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

        final String url = context.getResources().getString(R.string.web_service_url).concat(context.getString(R.string.requestPathFacebookLogin));
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("accessToken", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Google Facebook data: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(context, response, callbackListener);
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

    /**
     * Cached default login.
     *
     * @param context          the context
     * @param callbackListener the callback listener
     */
    private void cachedDefaultLogin(final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {

        // Send data to Backend and validate data

        final String url = context.getResources().getString(R.string.web_service_url).concat(context.getString(R.string.requestPathDefaultLogin));
        Log.d(TAG, "url: " + url);
        final JSONObject data = new JSONObject();
        try {
            data.put("username", user.getUsername());
            data.put("password", hashedPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Default Login data:: " + data.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loginResponseHandler(context, response, callbackListener);
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

    /**
     * Gets the access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken the new access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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

    /**
     * Logout.
     *
     * @param activity         the activity
     * @param callbackListener the callback listener
     */
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
        final String url = activity.getResources().getString(R.string.web_service_url).concat(activity.getString(R.string.requestPathLogout));
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

    /**
     * Adds the on user data set listener.
     *
     * @param onUserDataSet the on user data set
     */
    public void addOnUserDataSetListener(CallbackListener<JSONObject, Exception> onUserDataSet) {
        this.onUserDataSetList.add(onUserDataSet);
    }

    /**
     * Removes the on user data set listener.
     *
     * @param onUserDataSet the on user data set
     */
    public void removeOnUserDataSetListener(CallbackListener<JSONObject, Exception> onUserDataSet) {
        this.onUserDataSetList.remove(onUserDataSet);
    }

    /**
     * Gets the synchronisation helper.
     *
     * @return the synchronisation helper
     */
    public SynchronisationHelper getSynchronisationHelper() {
        return synchronisationHelper;
    }
}
