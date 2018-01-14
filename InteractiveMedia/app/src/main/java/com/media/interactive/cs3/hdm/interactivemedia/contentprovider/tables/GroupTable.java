package com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by benny on 20.11.17.
 */

public class GroupTable {

    public static final String TABLE_NAME = "groups";
    /**
     * First attribute, type integer.
     */
    public static final String COLUMN_ID = "_id";
    /**
     * Second attribute, type TEXT NOT NULL.
     */
    public static final String COLUMN_NAME = "name";
    /**
     * Third attribute, type TEXT.
     */
    public static final String COLUMN_IMAGE_URL = "image_url";
    /**
     * Fourth attribute, type TIMESTAMP DEFAULT CURRENT_TIMESTAMP.
     */
    public static final String COLUMN_CREATED_AT = "created_at";
    /**
     * Fifth attribute, type STRING references the server group id
     */
    public static final String COLUMN_GROUP_ID = "group_id";
    /**
     * Sixth attribute, type INTEGER representing an boolean 0/1 not synced/synched
     */
    public static final String COLUMN_SYNCHRONIZED = "synchronized";

    public static final String DATABASE_CREATE =

        "create table if not exists "+ TABLE_NAME +"(" +
                COLUMN_ID+" integer unique primary key AUTOINCREMENT," +
                COLUMN_NAME+" TEXT NOT NULL," +
                COLUMN_IMAGE_URL+" TEXT," +
                COLUMN_CREATED_AT+" TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                COLUMN_GROUP_ID +" TEXT," +
                COLUMN_SYNCHRONIZED +" INTEGER NOT NULL)";


    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }
}
