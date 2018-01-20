package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.RestRequestQueue;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedJsonObjectRequest;
import com.media.interactive.cs3.hdm.interactivemedia.authorizedrequests.AuthorizedSimpleMultiPartRequest;
import com.media.interactive.cs3.hdm.interactivemedia.notification.DeleteInstanceIDService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by benny on 20.01.18.
 */

public class SynchronisationHelper {

    private static final String TAG = SynchronisationHelper.class.getSimpleName();
    private DatabaseProviderHelper helper;
    private List<CallbackListener<JSONObject, Exception>> onUserDataSetList = null;

    public SynchronisationHelper(DatabaseProviderHelper helper, List<CallbackListener<JSONObject, Exception>> onUserDataSetList) {
        this.onUserDataSetList = onUserDataSetList;
        this.helper = helper;
    }

    public void synchronize(final Context context, final JSONObject response, final CallbackListener<JSONObject, Exception> callbackListener){
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/users/user");
        Log.d(TAG, "Get: " + url);
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean success = response.getBoolean("success");
                    Log.d(TAG, "user: " + response);
                    if (success) {
                        final JSONObject payload = response.getJSONObject("payload");
                        Login.getInstance().getUser().setEmail(payload.getString("email"));
                        try {
                            final String username = payload.getString("username");
                            Login.getInstance().getUser().setUsername(username);
                        } catch (JSONException e) {
                            Log.d(TAG, "Username is not set.");
                        }
                        if (!payload.has("fcmToken")){
                            // Delete current local FcmToken
                            final Intent deleteTokenIntent = new Intent(context, DeleteInstanceIDService.class);
                            context.startService(deleteTokenIntent);
                        }

                        Login.getInstance().getUser().setUserId(payload.getString("userId"));
                        JSONArray groupIds = null;
                        try {
                            Login.getInstance().getUser().setImageUrl(payload.getString("imageUrl"));
                        } catch (JSONException e) {
                            Log.d(TAG, "No imageUrl exists for this user");
                        }
                        try {
                            groupIds = payload.getJSONArray("groupIds");
                        } catch (JSONException e) {
                            Log.d(TAG, "No groups exist for this user");
                        }
                        if (groupIds != null && groupIds.length() > 0) {
                            Log.d(TAG, "Before Removal: " + groupIds.length() + " " + groupIds.toString());
                            List<Group> existingGroups = helper.removeExistingGroupIds(groupIds);
                            Log.d(TAG, "After Removal: " + groupIds.length() + " " + groupIds.toString());
                            for (final Group group : existingGroups) {
                                requestTransactionsByGroupId(context, group.getGroupId(), group.getCreatedAt(), null);
                            }
                            requestNewGroups(context, groupIds);
                        } else {
                            for (CallbackListener<JSONObject, Exception> onUserDataSet : onUserDataSetList) {
                                onUserDataSet.onSuccess(response);
                            }
                        }
                        uploadUnsyncedGroups(context);
                        uploadUnsyncedTransactions(context);
                        if(response != null && callbackListener != null) {
                            callbackListener.onSuccess(response);
                        }
                        Login.getInstance().getUser().setSync(true);
                        helper.upsertUser(Login.getInstance().getUser());
                        Log.d(TAG, "Upserted User: " + Login.getInstance().getUser());

                    } else {
                        Log.e(TAG, "Error while setting user data.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while setting user data.");
                for (CallbackListener<JSONObject, Exception> onUserDataSet : onUserDataSetList) {
                    onUserDataSet.onFailure(error);
                }
            }
        });
        jsonObjectRequest.setShouldCache(false);
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private void uploadUnsyncedTransactions(final Context context) {
        final List<Transaction> transactions = helper.getUnsyncedTransactions(Login.getInstance().getUser().getUserId());

        Log.d(TAG, "-----------------------------" + transactions.size() + " unsynced transactions available ");
        for (final Transaction transaction : transactions) {
            Log.d(TAG,"Transaction to be uploaded: "+ transaction.toString());
            if (transaction.getImageUrl() != null) {
                final String url = context.getResources().getString(R.string.web_service_url) + "/v1/object-store/upload?filename=image.png";
                final AuthorizedSimpleMultiPartRequest simpleMultiPartRequest = new AuthorizedSimpleMultiPartRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject object = null;
                        try {
                            object = new JSONObject(response);
                            if (object.getBoolean("success") == false) {
                                Log.d(TAG, "Transaction Image upload failed due to success false");
                            } else {
                                helper.setTransactionImageUrlByResponse(context, transaction, object);
                                uploadTransaction(transaction, context, new CallbackListener<JSONObject, Exception>() {
                                    @Override
                                    public void onSuccess(JSONObject payload) {
                                        helper.updateTransactionWithResponse(transaction, payload);
                                    }

                                    @Override
                                    public void onFailure(Exception error) {
                                        error.printStackTrace();
                                        Log.e(TAG, "Error while uploading Transaction");
                                    }
                                });
                            }
                            Log.d(TAG, response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "JSON Exception failed to parse");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "Image upload failed due to network.");
                    }
                });

                simpleMultiPartRequest.addFile("uploadField", transaction.getImageUrl());
                RestRequestQueue.getInstance(context).addToRequestQueue(simpleMultiPartRequest);
            } else {
                uploadTransaction(transaction, context, new CallbackListener<JSONObject, Exception>() {
                    @Override
                    public void onSuccess(JSONObject payload) {
                        Log.d(TAG,"uploadTransactionResponse: "+ payload.toString());
                        helper.updateTransactionWithResponse(transaction, payload);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "Error while uploading Transaction");
                    }
                });
            }
            try {
                Log.d(TAG, transaction.toJson().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadTransaction(final Transaction transaction, final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/groups/").concat(transaction.getGroup().getGroupId()).concat("/transactions");
        Log.d(TAG, "url: " + url);
        try {
            final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
                Request.Method.POST, url, transaction.toJson(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Response from backend: "+ response.toString());
                    // Update Transaction with Response
                    try {
                        if (response.getBoolean("success") == true) {
                            callbackListener.onSuccess(response.getJSONObject("payload"));
                        } else {
                            callbackListener.onFailure(new Exception("Success is not set"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callbackListener.onFailure(e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.d(TAG,"Error while uploading Transaction.");
                    callbackListener.onFailure(error);
                }
            });
            RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG,"Error while ");
            callbackListener.onFailure(e);
        }
    }

    private void uploadUnsyncedGroups(final Context context) {

        final List<Group> groups = helper.getUnsyncedGroups(Login.getInstance().getUser().getUserId());
        for (final Group group : groups) {
            if (group.getImageUrl() != null) {
                final String url = context.getResources().getString(R.string.web_service_url) + "/v1/object-store/upload?filename=image.png";
                final AuthorizedSimpleMultiPartRequest simpleMultiPartRequest = new AuthorizedSimpleMultiPartRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject object = null;
                        try {
                            object = new JSONObject(response);
                            if (object.getBoolean("success") == false) {
                                Log.d(TAG, "Image upload failed");
                            } else {
                                helper.setImageUrlByResponse(context, group, object);
                                uploadGroup(group, context, new CallbackListener<JSONObject, Exception>() {
                                    @Override
                                    public void onSuccess(JSONObject payload) {
                                        helper.updateGroupWithResponse(group, payload);
                                    }

                                    @Override
                                    public void onFailure(Exception error) {
                                        Log.e(TAG, "Error while uploading group");
                                    }
                                });
                            }
                            Log.d(TAG, response);
                        } catch (JSONException e) {
                            Log.d(TAG, "Image upload failed");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "Group Image upload failed");
                    }
                });

                simpleMultiPartRequest.addFile("uploadField", group.getImageUrl());
                RestRequestQueue.getInstance(context).addToRequestQueue(simpleMultiPartRequest);
            } else {
                uploadGroup(group, context, new CallbackListener<JSONObject, Exception>() {
                    @Override
                    public void onSuccess(JSONObject payload) {
                        helper.updateGroupWithResponse(group, payload);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "Error while uploading group");
                    }
                });
            }
            try {
                Log.d(TAG, group.toJson().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadGroup(Group group, final Context context, final CallbackListener<JSONObject, Exception> callbackListener) {
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/groups/");
        Log.d(TAG, "url: " + url);
        AuthorizedJsonObjectRequest jsonObjectRequest = null;
        try {
            jsonObjectRequest = new AuthorizedJsonObjectRequest(
                Request.Method.POST, url, group.toJson(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    // Update group and user data
                    try {
                        if (response.getBoolean("success") == true) {
                            callbackListener.onSuccess(response.getJSONObject("payload"));
                        } else {
                            callbackListener.onFailure(new Exception("Success is not set"));
                        }
                    } catch (JSONException e) {
                        callbackListener.onFailure(e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callbackListener.onFailure(error);
                }
            });
        } catch (JSONException e) {
            callbackListener.onFailure(e);
        }
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    public void requestTransactionsByGroupId(Context context, final String groupId, String groupCreatedAt, final CallbackListener<JSONObject, Exception> callback) {

        String latestPublishedDate = helper.getLatestTransactionPubDateByGroupId(groupId);
        Log.d(TAG, "latest published date: " + latestPublishedDate);
        if (latestPublishedDate == null) {
            latestPublishedDate = groupCreatedAt;
        }
        final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/groups/").concat(groupId).concat("/transactions?after=").concat(latestPublishedDate);
        Log.d(TAG, "Get: " + url);
        final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
            Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {
                try {
                    final boolean success = response.getBoolean("success");
                    Log.d(TAG, "TransactionsAfter: " + response);
                    if (success) {
                        final JSONArray payload = response.getJSONArray("payload");
                        helper.addTransactions(payload, groupId);
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    } else {
                        Log.e(TAG, "Error while requesting transaction data.");
                        if (callback != null) {
                            callback.onFailure(null);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(null);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error while setting user data.");
                if (callback != null) {
                    callback.onFailure(null);
                }
            }
        });
        jsonObjectRequest.setShouldCache(false);
        RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    private void requestNewGroups(Context context,final JSONArray groupIds) {
        final AtomicInteger atomicInteger = new AtomicInteger();
        for (int i = 0; i < groupIds.length(); i++) {
            try {
                final String groupId = (String) groupIds.get(i);
                final String url = context.getResources().getString(R.string.web_service_url).concat("/v1/groups/").concat(groupId);
                Log.d(TAG, "Get: " + url);
                final AuthorizedJsonObjectRequest jsonObjectRequest = new AuthorizedJsonObjectRequest(
                    Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            final boolean success = response.getBoolean("success");
                            Log.d(TAG, "group: " + response);
                            if (success) {
                                final JSONObject payload = response.getJSONObject("payload");
                                final Group newGroup = new Group();
                                newGroup.setName(payload.getString("name"));
                                newGroup.setImageUrl(payload.getString("imageUrl"));
                                newGroup.setGroupId(payload.getString("groupId"));
                                newGroup.setCreatedAt(payload.getString("createdAt"));
                                newGroup.setSync(true);
                                // Set Users
                                final JSONArray users = payload.getJSONArray("users");
                                for (int j = 0; j < users.length(); j++) {
                                    final JSONObject jsonObject = (JSONObject) users.get(j);
                                    final User user = new User();
                                    user.setEmail(jsonObject.getString("email"));
                                    user.setUserId(jsonObject.getString("userId"));
                                    user.setUsername(jsonObject.getString("username"));
                                    user.setSync(true);
                                    newGroup.getUsers().add(user);
                                }
                                helper.insertGroupAtDatabase(newGroup);
                                //Add Transactions
                                helper.addTransactions(payload.getJSONArray("transactions"), newGroup.getGroupId());
                                if (groupIds.length() - 1 == atomicInteger.incrementAndGet()) {
                                    for (CallbackListener<JSONObject, Exception> onUserDataSet : onUserDataSetList) {
                                        onUserDataSet.onSuccess(response);
                                    }
                                }
                            } else {
                                Log.e(TAG, "Error while setting user data.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error while setting user data.");
                    }
                });
                jsonObjectRequest.setShouldCache(false);
                RestRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
