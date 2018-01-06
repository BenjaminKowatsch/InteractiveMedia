package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.content.ContentValues;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

public class Transaction {
    private final String name;
    private final String split;
    private final Date dateTime;
    private final double amount;
    private final long groupId;

    public Transaction(String name, String split, Date dateTime, double amount, long groupId) {
        this.name = name;
        this.split = split;
        this.dateTime = dateTime;
        this.amount = amount;
        this.groupId = groupId;
    }

    public ContentValues toContentValues() {
        ContentValues out = new ContentValues();
        out.put(TransactionTable.COLUMN_INFO_NAME, name);
        Date now = new Date(System.currentTimeMillis());
        out.put(TransactionTable.COLUMN_INFO_CREATED_AT, now.toString());
        out.put(TransactionTable.COLUMN_AMOUNT, amount);
        return out;
    }
}
