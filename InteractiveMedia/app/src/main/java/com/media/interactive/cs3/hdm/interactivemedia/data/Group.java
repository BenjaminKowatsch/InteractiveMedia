package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentValues;

import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by benny on 04.01.18.
 */

public class Group {

    /**
     * The id.
     */
    private long id;

    /**
     * The name.
     */
    private String name;

    /**
     * The image url.
     */
    private String imageUrl;

    /**
     * The group id.
     */
    private String groupId;

    /**
     * The created at.
     */
    private String createdAt;

    /**
     * The sync.
     */
    private boolean sync;

    /**
     * The users.
     */
    // No direct DB entities
    private List<User> users;

    /**
     * The transactions.
     */
    private List<Transaction> transactions;

    /**
     * Instantiates a new group.
     *
     * @param name      the name
     * @param imageUrl  the image url
     * @param groupId   the group id
     * @param createdAt the created at
     * @param sync      the sync
     */
    public Group(String name, String imageUrl, String groupId, String createdAt, boolean sync) {
        this();
        this.name = name;
        this.imageUrl = imageUrl;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.sync = sync;
    }

    /**
     * Instantiates a new group.
     */
    public Group() {
        this.users = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }


    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image url.
     *
     * @param imageUrl the new image url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the group id.
     *
     * @return the group id
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId the new group id
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Gets the created at.
     *
     * @return the created at
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the new created at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets the users.
     *
     * @param users the new users
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Gets the transactions.
     *
     * @return the transactions
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Sets the transactions.
     *
     * @param transactions the new transactions
     */
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Gets the sync.
     *
     * @return the sync
     */
    public boolean getSync() {
        return sync;
    }

    /**
     * Sets the sync.
     *
     * @param sync the new sync
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Group{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", imageUrl='" + imageUrl + '\''
            + ", groupId='" + groupId + '\''
            + ", createdAt='" + createdAt + '\''
            + ", users=" + users
            + ", transactions=" + transactions
            + ", sync=" + sync
            + '}';
    }

    /**
     * To content values.
     *
     * @return the content values
     */
    public ContentValues toContentValues() {
        final ContentValues groupValues = new ContentValues();
        groupValues.put(GroupTable.COLUMN_NAME, getName());
        groupValues.put(GroupTable.COLUMN_IMAGE_URL, getImageUrl());
        groupValues.put(GroupTable.COLUMN_GROUP_ID, getGroupId());
        groupValues.put(GroupTable.COLUMN_CREATED_AT, getCreatedAt());
        groupValues.put(GroupTable.COLUMN_SYNCHRONIZED, getSync());
        return groupValues;
    }

    /**
     * To json.
     *
     * @return the JSON object
     * @throws JSONException the JSON exception
     */
    public JSONObject toJson() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("imageUrl", imageUrl != null ? imageUrl : JSONObject.NULL);
        final JSONArray userEmails = new JSONArray();
        for (int i = 0; i < users.size(); i++) {
            userEmails.put(i, users.get(i).getEmail());
        }
        object.put("users", userEmails);
        return object;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Group group = (Group) o;

        if (sync != group.sync) {
            return false;
        }
        return groupId != null ? groupId.equals(group.groupId) : group.groupId == null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + (sync ? 1 : 0);
        return result;
    }
}
