package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by benny on 12.11.17.
 */

public class RestRequestQueue  {

    private static RestRequestQueue instance;
    private RequestQueue requestQueue;
    private static Context context;
    // Long running Class Instance?

    private RestRequestQueue(Context context){
        this.context = context;
        this.requestQueue = getRequestQueue();


    }

    public static synchronized RestRequestQueue getInstance(Context context){
        if(instance == null){
            instance = new RestRequestQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue == null){
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }


}