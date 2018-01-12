package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.content.ContentValues;
import android.location.Location;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;

import java.util.Date;

public class Transaction {
    private final String name;
    private final String paidBy;
    private final String split;
    private final Date dateTime;
    private final String imageUrl;
    private final Location location;
    private final double amount;
    private final long groupId;
    private boolean synched;
    private Date publishedAt;

    public Transaction(String name, String paidBy, String split, Date dateTime, String imageUrl,
                       Location location, double amount, long groupId) {
        this.name = name;
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
        ContentValues out = new ContentValues();
        out.put(TransactionTable.COLUMN_INFO_NAME, name);
        Date now = new Date(System.currentTimeMillis());
        out.put(TransactionTable.COLUMN_INFO_CREATED_AT, now.toString());
        out.put(TransactionTable.COLUMN_AMOUNT, amount);
        return out;
    }

    public long getGroupId() {
        return groupId;
    }

    public boolean isSynched() {
        return synched;
    }
}
