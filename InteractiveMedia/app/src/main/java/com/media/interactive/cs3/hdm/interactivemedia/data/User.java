package com.media.interactive.cs3.hdm.interactivemedia.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benny on 04.01.18.
 */

public class User {
    private long id;
    private String username;
    private String email;
    private String userId;
    private String imageUrl;
    private String createdAt;
    private boolean sync;

    public User(){}

    public User(String email){
        this.email = email;
    }


    public User(String username, String email, String userId, String imageUrl, boolean sync) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.sync = sync;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", userId='" + userId + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", createdAt='" + createdAt + '\'' +
            ", sync=" + sync +
            '}';
    }

    public JSONObject toJson() {
        // TODO: Implement method
        return null;
    }
}
