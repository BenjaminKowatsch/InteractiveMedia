package com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by benny on 20.11.17.
 */

public class TransactionTable {

    public static final String TABLE_NAME = "transactions";
    /**
     * First attribute, type integer.
     */
    public static final String COLUMN_ID = "_id";
    /**
     * Second attribute, type REAL NOT NULL.
     */
    public static final String COLUMN_AMOUNT = "amount";
    /**
     * Third attribute, type INTEGER reference.
     */
    public static final String COLUMN_PAID_BY = "paid_by";
    /**
     * Fourth attribute, type TEXT NOT NULL.
     */
    public static final String COLUMN_INFO_NAME = "info_name";
    /**
     * Fifth attribute, type TEXT NOT NULL.
     */
    public static final String COLUMN_INFO_LOCATION = "info_location";
    /**
     * Sixth attribute, type TEXT.
     */
    public static final String COLUMN_INFO_IMAGE_URL = "info_image_url";
    /**
     * Seventh attribute, type TIMESTAMP DEFAULT CURRENT_TIMESTAMP.
     */
    public static final String COLUMN_INFO_CREATED_AT = "info_created_at";


    public static final String DATABASE_CREATE =
        "create table if not exists " + TABLE_NAME + "("
           + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
           + COLUMN_AMOUNT + " REAL NOT NULL,"
           + COLUMN_PAID_BY + " INTEGER references " + UserTable.TABLE_NAME
           + " (" + UserTable.COLUMN_ID + ") on delete cascade on update cascade,"
           + COLUMN_INFO_NAME + " TEXT NOT NULL,"
           + COLUMN_INFO_LOCATION + " TEXT NOT NULL,"
           + COLUMN_INFO_IMAGE_URL + " TEXT,"
           + COLUMN_INFO_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }
}
