package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.content.ContentValues;
import android.location.Location;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    private long id;
    private String infoName;
    private String paidBy;
    private String split;
    private Date dateTime;
    private String imageUrl;
    private Location location;
    private double amount;
    private String groupId;
    private boolean synched;
    private Date publishedAt;

    public Transaction(){
    }

    public Transaction(String infoName, String paidBy, String split, Date dateTime, String imageUrl,
                       Location location, double amount, String groupId) {
        this.infoName = infoName;
        this.paidBy = paidBy;
        this.split = split;
        this.dateTime = dateTime;
        this.imageUrl = imageUrl;
        this.location = location;
        this.amount = amount;
        this.groupId = groupId;
        this.synched = false;
        this.publishedAt = null;
    }

    public ContentValues toContentValues() {
        final ContentValues out = new ContentValues();
        out.put(TransactionTable.COLUMN_INFO_CREATED_AT, Helper.GetDateTime());
        out.put(TransactionTable.COLUMN_AMOUNT, amount);
        out.put(TransactionTable.COLUMN_INFO_NAME, infoName);
        out.put(TransactionTable.COLUMN_PAID_BY, paidBy);
        out.put(TransactionTable.COLUMN_INFO_IMAGE_URL, imageUrl);
        out.put(TransactionTable.COLUMN_PUBLISHED_AT, Helper.FormatDate(publishedAt));
        out.put(TransactionTable.COLUMN_INFO_LOCATION_LONG, location.getLongitude());
        out.put(TransactionTable.COLUMN_INFO_LOCATION_LAT, location.getLatitude());
        out.put(TransactionTable.COLUMN_SYNCHRONIZED, synched);
        return out;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInfoName() {
        return infoName;
    }

    public void setInfoName(String infoName) {
        this.infoName = infoName;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isSynched() {
        return synched;
    }

    public void setSynched(boolean synched) {
        this.synched = synched;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
            "id=" + id +
            ", infoName='" + infoName + '\'' +
            ", paidBy='" + paidBy + '\'' +
            ", split='" + split + '\'' +
            ", dateTime=" + dateTime +
            ", imageUrl='" + imageUrl + '\'' +
            ", location=" + location +
            ", amount=" + amount +
            ", groupId='" + groupId + '\'' +
            ", synched=" + synched +
            ", publishedAt=" + publishedAt +
            '}';
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("infoName", infoName);
        result.put("amount",amount);

        final JSONObject infoLocation = new JSONObject();
        infoLocation.put("latitude", location.getLatitude());
        infoLocation.put("longitude", location.getLongitude());
        result.put("infoLocation", infoLocation);

        result.put("infoImageUrl", imageUrl != null ? imageUrl : JSONObject.NULL);
        result.put("infoCreatedAt", Helper.FormatDate(dateTime));
        result.put("paidBy", paidBy);
        result.put("split", split);
        return result;
    }
}
