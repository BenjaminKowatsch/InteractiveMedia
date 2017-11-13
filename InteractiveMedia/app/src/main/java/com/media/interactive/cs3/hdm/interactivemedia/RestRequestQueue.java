package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<JSONObject> send(String url, int requestMethod, JSONObject data){
        final CompletableFuture<JSONObject> future = new CompletableFuture<>();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (requestMethod, url, data, future::complete, future::completeExceptionally);

        addToRequestQueue(jsonObjectRequest);

        return future;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<JSONArray> send(String url, int requestMethod, JSONArray data){
        final CompletableFuture<JSONArray> future = new CompletableFuture<>();

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (requestMethod, url, data, future::complete, future::completeExceptionally);

        addToRequestQueue(jsonArrayRequest);

        return future;
    }

}
