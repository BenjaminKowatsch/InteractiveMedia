package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.content.ContentValues;
import android.location.Location;

import com.media.interactive.cs3.hdm.interactivemedia.data.split.Split;
import com.media.interactive.cs3.hdm.interactivemedia.data.split.SplitFactory;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;



/**
 * The Class Transaction.
 */
public class Transaction {

    /**
     * The id.
     */
    private long id;

    /**
     * The info name.
     */
    private String infoName;

    /**
     * The paid by.
     */
    private String paidBy;

    /**
     * The split.
     */
    private Split split;

    /**
     * The date time.
     */
    private Date dateTime;

    /**
     * The image url.
     */
    private String imageUrl;

    /**
     * The location.
     */
    private Location location;

    /**
     * The amount.
     */
    private double amount;

    /**
     * The group.
     */
    private Group group;

    /**
     * The synched.
     */
    private boolean synched;

    /**
     * The published at.
     */
    private Date publishedAt;

    /**
     * Instantiates a new transaction.
     */
    public Transaction() {
        this.group = new Group();
    }

    /**
     * Instantiates a new transaction.
     *
     * @param infoName the info name
     * @param paidBy   the paid by
     * @param split    the split
     * @param dateTime the date time
     * @param location the location
     * @param amount   the amount
     * @param group    the group
     */
    public Transaction(String infoName, String paidBy, Split split, Date dateTime,
                       Location location, double amount, Group group) {
        this.infoName = infoName;
        this.paidBy = paidBy;
        this.split = split;
        this.group = group;
        this.dateTime = dateTime;
        this.location = location;
        this.amount = amount;
        this.group = group;
        this.synched = false;
        this.publishedAt = null;
    }

    /**
     * Instantiates a new transaction.
     *
     * @param infoName the info name
     * @param paidBy   the paid by
     * @param split    the split
     * @param dateTime the date time
     * @param location the location
     * @param amount   the amount
     * @param groupId  the group id
     */
    public Transaction(String infoName, String paidBy, Split split, Date dateTime,
                       Location location, double amount, String groupId) {
        this.infoName = infoName;
        this.paidBy = paidBy;
        this.split = split;
        this.group = new Group();
        this.group.setGroupId(groupId);
        this.dateTime = dateTime;
        this.location = location;
        this.amount = amount;
        this.group = group;
        this.synched = false;
        this.publishedAt = null;
    }


    /**
     * Instantiates a new transaction.
     *
     * @param transaction the transaction
     */
    public Transaction(Transaction transaction) {
        this.id = transaction.id;
        this.infoName = transaction.infoName;
        this.paidBy = transaction.paidBy;
        this.split = transaction.split;
        this.group = transaction.group;
        this.dateTime = transaction.dateTime;
        this.location = transaction.location;
        this.amount = transaction.amount;
        this.group = transaction.group;
        this.synched = transaction.synched;
        this.publishedAt = transaction.publishedAt;
    }

    /**
     * To content values.
     *
     * @return the content values
     */
    public ContentValues toContentValues() {
        final ContentValues out = new ContentValues();
        out.put(TransactionTable.COLUMN_INFO_CREATED_AT, Helper.getDateTime());
        out.put(TransactionTable.COLUMN_AMOUNT, amount);
        out.put(TransactionTable.COLUMN_INFO_NAME, infoName);
        out.put(TransactionTable.COLUMN_PAID_BY, paidBy);
        out.put(TransactionTable.COLUMN_INFO_IMAGE_URL, imageUrl);
        out.put(TransactionTable.COLUMN_PUBLISHED_AT, Helper.formatDate(publishedAt));

        out.put(TransactionTable.COLUMN_INFO_LOCATION_LONG, location != null ? location.getLongitude() : null);
        out.put(TransactionTable.COLUMN_INFO_LOCATION_LAT, location != null ? location.getLatitude() : null);

        out.put(TransactionTable.COLUMN_SYNCHRONIZED, synched);
        return out;
    }

    /**
     * Split.
     *
     * @return the list
     */
    public List<Debt> split() {
        return split.split(this);
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
     * Gets the info name.
     *
     * @return the info name
     */
    public String getInfoName() {
        return infoName;
    }

    /**
     * Sets the info name.
     *
     * @param infoName the new info name
     */
    public void setInfoName(String infoName) {
        this.infoName = infoName;
    }

    /**
     * Gets the paid by.
     *
     * @return the paid by
     */
    public String getPaidBy() {
        return paidBy;
    }

    /**
     * Sets the paid by.
     *
     * @param paidBy the new paid by
     */
    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    /**
     * Gets the split.
     *
     * @return the split
     */
    public Split getSplit() {
        return split;
    }

    /**
     * Sets the split.
     *
     * @param split the new split
     */
    public void setSplit(Split split) {
        this.split = split;
    }

    /**
     * Gets the date time.
     *
     * @return the date time
     */
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * Sets the date time.
     *
     * @param dateTime the new date time
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
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
     * Gets the location.
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location.
     *
     * @param location the new location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Gets the amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount.
     *
     * @param amount the new amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Checks if is synched.
     *
     * @return true, if is synched
     */
    public boolean isSynched() {
        return synched;
    }

    /**
     * Sets the synched.
     *
     * @param synched the new synched
     */
    public void setSynched(boolean synched) {
        this.synched = synched;
    }

    /**
     * Gets the published at.
     *
     * @return the published at
     */
    public Date getPublishedAt() {
        return publishedAt;
    }

    /**
     * Sets the published at.
     *
     * @param publishedAt the new published at
     */
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Transaction{"
            + "id=" + id
            + ", infoName='" + infoName + '\''
            + ", paidBy='" + paidBy + '\''
            + ", split='" + split + '\''
            + ", dateTime=" + dateTime
            + ", imageUrl='" + imageUrl + '\''
            + ", location=" + location
            + ", amount=" + amount
            + ", synched=" + synched
            + ", publishedAt=" + publishedAt
            + '}';
    }

    /**
     * To json.
     *
     * @return the JSON object
     * @throws JSONException the JSON exception
     */
    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("infoName", infoName);
        result.put("amount", amount);

        final JSONObject infoLocation = new JSONObject();
        infoLocation.put("latitude", location != null ? location.getLatitude() : JSONObject.NULL);
        infoLocation.put("longitude", location != null ? location.getLongitude() : JSONObject.NULL);
        result.put("infoLocation", infoLocation);

        result.put("infoImageUrl", imageUrl != null ? imageUrl : JSONObject.NULL);
        result.put("infoCreatedAt", Helper.formatDate(dateTime));
        result.put("paidBy", paidBy);
        result.put("split", SplitFactory.toJsonArray(split));
        return result;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the group.
     *
     * @param group the new group
     */
    public void setGroup(Group group) {
        this.group = group;
    }

}
