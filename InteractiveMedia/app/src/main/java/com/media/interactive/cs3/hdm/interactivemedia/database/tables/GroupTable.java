package com.media.interactive.cs3.hdm.interactivemedia.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.media.interactive.cs3.hdm.interactivemedia.data.Group;

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

            "create table if not exists " + TABLE_NAME + "(" +
                    COLUMN_ID + " integer unique primary key AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_IMAGE_URL + " TEXT," +
                    COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    COLUMN_GROUP_ID + " TEXT," +
                    COLUMN_SYNCHRONIZED + " INTEGER NOT NULL)";


    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @NonNull
    /**
     * Extract Group out of this cursor. May throw exceptions if any column is missing in cursor.
     * Does not explicitly check for cursor contents. Does neither add users nor transactions to group!
     */
    public static Group extractGroupFromCurrentPosition(Cursor cur) {
        final String name = cur.getString(cur.getColumnIndex(COLUMN_NAME));
        final String imageUrl = cur.getString(cur.getColumnIndex(COLUMN_IMAGE_URL));
        final String groupId = cur.getString(cur.getColumnIndex(COLUMN_GROUP_ID));
        final String createdAt = cur.getString(cur.getColumnIndex(COLUMN_CREATED_AT));
        final boolean synch = cur.getInt(cur.getColumnIndex(COLUMN_SYNCHRONIZED)) == 1;
        final long id = cur.getLong(cur.getColumnIndex(COLUMN_ID));
        final Group group = new Group(name, imageUrl, groupId, createdAt, synch);
        group.setId(id);
        return group;
    }
}
