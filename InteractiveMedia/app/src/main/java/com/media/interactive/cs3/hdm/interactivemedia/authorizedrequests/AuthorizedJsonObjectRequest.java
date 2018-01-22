package com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by benny on 04.01.18.
 */

public class AuthorizedJsonObjectRequest extends JsonObjectRequest {


    public AuthorizedJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public AuthorizedJsonObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthorizedHeader.GetParameters();
    }
}
