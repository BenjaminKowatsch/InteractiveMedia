package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
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
    /*
    //@RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<JSONObject> send(String url, int requestMethod, JSONObject data) {
        //final CompletableFuture<JSONObject> future = new CompletableFuture<>();

        Log.d("RestQueue: ", " Thread Id: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            requestMethod, url, data, future::complete, future::completeExceptionally);

        addToRequestQueue(jsonObjectRequest);

        //return future;
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    public void send(String url, int requestMethod, JSONArray data, Response.Listener<JSONArray> onComplete, Response.ErrorListener onCompleteExceptionally) {
        //final CompletableFuture<JSONArray> future = new CompletableFuture<>();

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
            requestMethod, url, data, onComplete, onCompleteExceptionally);

        addToRequestQueue(jsonArrayRequest);

        //return future;
    }*/

}
