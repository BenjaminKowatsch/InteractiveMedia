package com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests;

import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.request.SimpleMultiPartRequest;

import java.util.Map;

/**
 * Created by benny on 05.01.18.
 */

public class AuthorizedSimpleMultiPartRequest extends SimpleMultiPartRequest {
    public AuthorizedSimpleMultiPartRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public AuthorizedSimpleMultiPartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthorizedHeader.GetParameters();
    }

}
