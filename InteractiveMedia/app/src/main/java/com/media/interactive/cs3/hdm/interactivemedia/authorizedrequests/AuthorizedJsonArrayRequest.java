package com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.request.JsonArrayRequest;

import org.json.JSONArray;

import java.util.Map;

/**
 * Created by benny on 05.01.18.
 */

public class AuthorizedJsonArrayRequest extends JsonArrayRequest {

    private static final int MY_SOCKET_TIMEOUT_MS = 300;
    private static DefaultRetryPolicy POLICY = new DefaultRetryPolicy(
        MY_SOCKET_TIMEOUT_MS,
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public AuthorizedJsonArrayRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        setRetryPolicy(POLICY);
    }

    public AuthorizedJsonArrayRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        setRetryPolicy(POLICY);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthorizedHeader.GetParameters();
    }
}
