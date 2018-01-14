package com.media.interactive.cs3.hdm.interactivemedia.util;


import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.CallbackListener;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageUploadCallbackListener extends CallbackListener<JSONObject, Exception> {
    private String imageUrl;
    private final String webServiceUrl;

    public ImageUploadCallbackListener(String webServiceUrl) {
        this.webServiceUrl = webServiceUrl;
    }

    @Override
    public void onSuccess(JSONObject response) {
        JSONObject payload;
        String imageName = null;
        try {
            payload = response.getJSONObject("payload");
            imageName = payload.getString("path");
            Log.d(this.getClass().getName(), "Path returned: " + payload.getString("path"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        imageUrl = webServiceUrl.concat("/v1/object-store/download?filename=").concat(imageName);
    }

    @Override
    public void onFailure(Exception error) {
        imageUrl = null;
        Log.e(this.getClass().getSimpleName(), "Error in image upload: ", error);
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
