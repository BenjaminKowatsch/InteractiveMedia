package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.content.ContentValues;

import com.google.android.gms.maps.model.LatLng;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.SplitFactory;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class Transaction {
    private long id;
    private String infoName;
    private String paidBy;
    private String split;
    private Date dateTime;
    private String imageUrl;
    private LatLng location;
    private double amount;
    private Group group;
    private boolean synched;
    private Date publishedAt;

    public Transaction() {
    }

    public Transaction(String infoName, String paidBy, String split, Date dateTime,
                       LatLng location, double amount, Group group) {
        this.infoName = infoName;
        this.paidBy = paidBy;
        this.split = split;
        this.dateTime = dateTime;
        this.location = location;
        this.amount = amount;
        this.group = group;
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
        if (location != null) {
            out.put(TransactionTable.COLUMN_INFO_LOCATION_LONG, location.longitude);
            out.put(TransactionTable.COLUMN_INFO_LOCATION_LAT, location.latitude);
        }
        out.put(TransactionTable.COLUMN_SYNCHRONIZED, synched);
        return out;
    }

    public List<Debt> split() {
        return SplitFactory.getSplitByName("").split(this, paidBy);
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

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getGroupId() {
        return group.getGroupId();
    }

    public void setGroup(Group group) {
        this.group = group;
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
                ", groupId='" + getGroupId() + '\'' +
                ", synched=" + synched +
                ", publishedAt=" + publishedAt +
                '}';
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("infoName", infoName);
        result.put("amount", amount);

        final JSONObject infoLocation = new JSONObject();
        infoLocation.put("latitude", location.latitude);
        infoLocation.put("longitude", location.longitude);
        result.put("infoLocation", infoLocation);

        result.put("infoImageUrl", imageUrl != null ? imageUrl : JSONObject.NULL);
        result.put("infoCreatedAt", Helper.FormatDate(dateTime));
        result.put("paidBy", paidBy);
        result.put("split", split);
        return result;
    }
}
