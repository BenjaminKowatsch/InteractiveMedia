package com.media.interactive.cs3.hdm.interactivemedia.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.media.interactive.cs3.hdm.interactivemedia.data.User;



/**
 * Created by benny on 20.11.17.
 */

public class UserTable {

    /**
     * The Constant TABLE_NAME.
     */
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

    /**
     * Sixth attribute, type STRING.
     */
    public static final String COLUMN_USER_ID = "user_id";

    /**
     * Sixth attribute, type INTEGER representing an boolean 0/1 not synced/synched.
     */
    public static final String COLUMN_SYNCHRONIZED = "synchronized";

    /**
     * The Constant DATABASE_CREATE.
     */
    public static final String DATABASE_CREATE =

        "create table if not exists " + TABLE_NAME + "("
            + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_IMAGE_URL + " TEXT,"
            + COLUMN_EMAIL + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_USER_ID + " TEXT,"
            + COLUMN_SYNCHRONIZED + " INTEGER NOT NULL)";

    /**
     * The Constant DATABASE_DROP.
     */
    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;


    /**
     * On create.
     *
     * @param database the database
     */
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    /**
     * Extract user from current position.
     *
     * @param cur the cur
     * @return the user
     */
    public static User extractUserFromCurrentPosition(Cursor cur) {
        final long id = cur.getLong(cur.getColumnIndex(COLUMN_ID));
        final String name = cur.getString(cur.getColumnIndex(COLUMN_USERNAME));
        final String email = cur.getString(cur.getColumnIndex(COLUMN_EMAIL));
        final String userId = cur.getString(cur.getColumnIndex(COLUMN_USER_ID));
        final String imageUrl = cur.getString(cur.getColumnIndex(COLUMN_IMAGE_URL));
        final String createdAt = cur.getString(cur.getColumnIndex(COLUMN_CREATED_AT));
        final boolean synch = cur.getInt(cur.getColumnIndex(COLUMN_SYNCHRONIZED)) == 1;
        final User user = new User(name, email, userId, imageUrl, synch);
        user.setCreatedAt(createdAt);
        user.setId(id);
        return user;
    }

}
