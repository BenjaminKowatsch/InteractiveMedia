package com.media.interactive.cs3.hdm.interactivemedia.notification;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by benny on 23.12.17.
 */

public class InteractiveMediaInstanceIDService extends FirebaseInstanceIdService{
    private static final String TAG = InteractiveMediaInstanceIDService.class.getSimpleName();

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
