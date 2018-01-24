package com.media.interactive.cs3.hdm.interactivemedia.notification;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;



/**
 * Created by benny on 23.12.17.
 */

public class InteractiveMediaInstanceIdService extends FirebaseInstanceIdService {

    /**
     * The Constant TAG.
     */
    private static final String TAG = InteractiveMediaInstanceIdService.class.getSimpleName();

    /**
     * On token refresh.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        Login.getInstance().getUser().setFcmToken(refreshedToken);
        Login.getInstance().updateFcmToken(this);
        Log.d(TAG, "Setting token: " + Login.getInstance().getUser().getFcmToken());

    }
}
