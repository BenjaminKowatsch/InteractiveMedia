package com.media.interactive.cs3.hdm.interactivemedia.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benny on 04.01.18.
 */

public class Group {

    private long id;
    private String name;
    private String imageUrl;
    private String groupId;
    private String createdAt;
    private List<User> users;
    private List<Transaction> transactions;
    private boolean sync;

    public Group(String name, String imageUrl, String groupId, String createdAt, boolean sync) {
        this();
        this.name = name;
        this.imageUrl = imageUrl;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.sync = sync;
    }

    public Group(){
        this.users = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public boolean getSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public String toString() {
        return "Group{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", groupId='" + groupId + '\'' +
            ", createdAt='" + createdAt + '\'' +
            ", users=" + users +
            ", transactions=" + transactions +
            ", sync=" + sync +
            '}';
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("imageUrl", imageUrl != null ? imageUrl : JSONObject.NULL);
        final JSONArray userEmails = new JSONArray();
        for(int i=0; i < users.size(); i++){
            userEmails.put(i, users.get(i).getEmail());
        }
        object.put("users", userEmails);
        final JSONArray transactionsArray = new JSONArray();
        for(int i=0; i < transactions.size(); i++){
            transactionsArray.put(i, users.get(i).toJson());
        }
        return object;
    }
}
