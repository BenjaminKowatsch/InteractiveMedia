package com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by benny on 20.11.17.
 */

public class UserTable {

    public static final String TABLE_NAME = "user";
    /**
     * First attribute, type integer.
     */
    public static final String COLUMN_ID = "_id";
    /**
     * Second attribute, type TEXT NOT NULL.
     */
    public static final String COLUMN_USERNAME = "username";
    /**
     * Third attribute, type TEXT.
     */
    public static final String COLUMN_IMAGE_URL = "image_url";
    /**
     * Fourth attribute, type TEXT NOT NULL.
     */
    public static final String COLUMN_EMAIL = "email";
    /**
     * Fiths attribute, type TIMESTAMP DEFAULT CURRENT_TIMESTAMP.
     */
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String DATABASE_CREATE =

        "create table if not exists " + TABLE_NAME + "("
           + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
           + COLUMN_USERNAME + " TEXT NOT NULL,"
           + COLUMN_IMAGE_URL + " TEXT,"
           + COLUMN_EMAIL + " TEXT NOT NULL,"
           + COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;


    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }
}
