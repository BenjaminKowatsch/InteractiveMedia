package com.media.interactive.cs3.hdm.interactivemedia.volley;

import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;



/**
 * Created by benny on 04.01.18.
 */

public class AuthorizedJsonObjectRequest extends JsonObjectRequest {


    /**
     * Instantiates a new authorized json object request.
     *
     * @param method        the method
     * @param url           the url
     * @param jsonRequest   the json request
     * @param listener      the listener
     * @param errorListener the error listener
     */
    public AuthorizedJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    /**
     * Instantiates a new authorized json object request.
     *
     * @param url           the url
     * @param jsonRequest   the json request
     * @param listener      the listener
     * @param errorListener the error listener
     */
    public AuthorizedJsonObjectRequest(String url, JSONObject jsonRequest,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    /**
     * Gets the headers.
     *
     * @return the headers
     * @throws AuthFailureError the auth failure error
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthorizedHeader.getParameters();
    }
}
