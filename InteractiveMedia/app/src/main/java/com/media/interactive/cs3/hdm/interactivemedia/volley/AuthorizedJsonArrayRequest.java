package com.media.interactive.cs3.hdm.interactivemedia.volley;

import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.request.JsonArrayRequest;

import org.json.JSONArray;

import java.util.Map;

/**
 * Created by benny on 05.01.18.
 */

public class AuthorizedJsonArrayRequest extends JsonArrayRequest {


    public AuthorizedJsonArrayRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public AuthorizedJsonArrayRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthorizedHeader.GetParameters();
    }
}
