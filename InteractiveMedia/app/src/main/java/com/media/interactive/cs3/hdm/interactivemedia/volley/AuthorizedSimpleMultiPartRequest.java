package com.media.interactive.cs3.hdm.interactivemedia.volley;

import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.request.SimpleMultiPartRequest;

import java.util.Map;



/**
 * Created by benny on 05.01.18.
 */

public class AuthorizedSimpleMultiPartRequest extends SimpleMultiPartRequest {

    /**
     * Instantiates a new authorized simple multi part request.
     *
     * @param method        the method
     * @param url           the url
     * @param listener      the listener
     * @param errorListener the error listener
     */
    public AuthorizedSimpleMultiPartRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    /**
     * Instantiates a new authorized simple multi part request.
     *
     * @param url           the url
     * @param listener      the listener
     * @param errorListener the error listener
     */
    public AuthorizedSimpleMultiPartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
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
