package com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests;

import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.data.Login;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by benny on 05.01.18.
 */

public class AuthorizedHeader {
    public static Map<String, String> GetParameters() {
        final Map<String, String> params = new HashMap<String, String>();
        if (Login.getInstance().getUserType() != null) {
            params.put("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
        } else {
            Log.e("Debug:", "UserType is null");
        }
        return params;
    }
}
