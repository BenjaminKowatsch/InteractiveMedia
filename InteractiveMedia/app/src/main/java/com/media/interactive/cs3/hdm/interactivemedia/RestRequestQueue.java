package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by benny on 12.11.17.
 */

public class RestRequestQueue {

    private static RestRequestQueue instance;
    private static Context context;
    private RequestQueue requestQueue;
    // Long running Class Instance?

    private RestRequestQueue(Context context) {
        this.context = context;
        this.requestQueue = getRequestQueue();


    }

    public static synchronized RestRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new RestRequestQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

}
