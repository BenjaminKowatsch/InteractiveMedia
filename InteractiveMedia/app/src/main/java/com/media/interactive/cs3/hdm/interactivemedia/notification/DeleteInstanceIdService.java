package com.media.interactive.cs3.hdm.interactivemedia.notification;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;



/**
 * Created by benny on 23.12.17.
 */

public class DeleteInstanceIdService extends IntentService {

    /**
     * The Constant TAG.
     */
    public static final String TAG = DeleteInstanceIdService.class.getSimpleName();

    /**
     * Instantiates a new delete instance ID service.
     */
    public DeleteInstanceIdService() {
        super(TAG);
    }

    /* (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            // Check for current token
            final String originalToken = getTokenFromPrefs();
            Log.d(TAG, "Token before deletion: " + originalToken);

            // Resets Instance ID and revokes all tokens.
            FirebaseInstanceId.getInstance().deleteInstanceId();

            // Clear current saved token
            saveTokenToPrefs("");

            // Check for success of empty token
            final String tokenCheck = getTokenFromPrefs();
            Log.d(TAG, "Token deleted. Proof: " + tokenCheck);

            // Now manually call onTokenRefresh()
            Log.d(TAG, "Getting new token");
            FirebaseInstanceId.getInstance().getToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save token to prefs.
     *
     * @param token the token
     */
    private void saveTokenToPrefs(String token) {
        // Access Shared Preferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        // Save to SharedPreferences
        editor.putString("registration_id", token);
        editor.apply();
    }

    /**
     * Gets the token from prefs.
     *
     * @return the token from prefs
     */
    private String getTokenFromPrefs() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("registration_id", null);
    }


}
