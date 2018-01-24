package com.media.interactive.cs3.hdm.interactivemedia.volley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;




/**
 * Created by benny on 12.11.17.
 */

public class RestRequestQueue {

    /**
     * The instance.
     */
    private static RestRequestQueue instance;

    /**
     * The context.
     */
    private static Context context;

    /**
     * The request queue.
     */
    private RequestQueue requestQueue;

    /**
     * Instantiates a new rest request queue.
     *
     * @param context the context
     */
    private RestRequestQueue(Context context) {
        this.context = context;
        this.requestQueue = getRequestQueue();
    }

    /**
     * Gets the single instance of RestRequestQueue.
     *
     * @param context the context
     * @return single instance of RestRequestQueue
     */
    public static synchronized RestRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new RestRequestQueue(context);
        }
        return instance;
    }

    /**
     * Gets the request queue.
     *
     * @return the request queue
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Adds the to request queue.
     *
     * @param <T>     the generic type
     * @param request the request
     */
    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

}
