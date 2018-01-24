package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentValues;

import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;



/**
 * Created by benny on 04.01.18.
 */

public class User {

    /**
     * The id.
     */
    private long id;

    /**
     * The username.
     */
    private String username;

    /**
     * The email.
     */
    private String email;

    /**
     * The user id.
     */
    private String userId;

    /**
     * The image url.
     */
    private String imageUrl;

    /**
     * The created at.
     */
    private String createdAt;

    /**
     * The sync.
     */
    private boolean sync;

    /**
     * The fcm token.
     */
    // No db entity field
    private String fcmToken;

    /**
     * Instantiates a new user.
     */
    public User() {
    }

    /**
     * Instantiates a new user.
     *
     * @param email the email
     */
    public User(String email) {
        this.email = email;
    }

    /**
     * Instantiates a new user.
     *
     * @param username the username
     * @param email    the email
     * @param userId   the user id
     * @param imageUrl the image url
     * @param sync     the sync
     */
    public User(String username, String email, String userId, String imageUrl, boolean sync) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.sync = sync;
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
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the new user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
     * Gets the fcm token.
     *
     * @return the fcm token
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Sets the fcm token.
     *
     * @param fcmToken the new fcm token
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "User{"
            + "id=" + id
            + ", username='" + username + '\''
            + ", email='" + email + '\''
            + ", userId='" + userId + '\''
            + ", imageUrl='" + imageUrl + '\''
            + ", createdAt='" + createdAt + '\''
            + ", sync=" + sync
            + '}';
    }

    /**
     * To content values.
     *
     * @return the content values
     */
    public ContentValues toContentValues() {
        final ContentValues userValues = new ContentValues();
        userValues.put(UserTable.COLUMN_USERNAME, getUsername());
        userValues.put(UserTable.COLUMN_EMAIL, getEmail());
        userValues.put(UserTable.COLUMN_USER_ID, getUserId());
        userValues.put(UserTable.COLUMN_IMAGE_URL, getImageUrl());
        userValues.put(UserTable.COLUMN_CREATED_AT, getCreatedAt());
        userValues.put(UserTable.COLUMN_SYNCHRONIZED, getSync());
        return userValues;
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

        User user = (User) o;

        return userId != null ? userId.equals(user.userId) : user.userId == null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
