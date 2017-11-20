package com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by benny on 20.11.17.
 */

public class GroupUserTable {

    public static final String TABLE_NAME = "group_user";
    /**
     * First attribute, type integer references
     */
    public static final String COLUMN_GROUP_ID = "group_id";
    /**
     * Second attribute, type integer references
     */
    public static final String COLUMN_USER_ID = "user_id";

    public static final String DATABASE_CREATE =
            "create table if not exists "+ TABLE_NAME +"(" +
                    COLUMN_GROUP_ID+" INTEGER references "+GroupTable.TABLE_NAME+" ("+GroupTable.COLUMN_ID+") on delete cascade on update cascade," +
                    COLUMN_USER_ID+" INTEGER references "+UserTable.TABLE_NAME+" ("+UserTable.COLUMN_ID+") on delete cascade on update cascade)";

    public static final String DATABASE_DROP = "drop table if exists "+ TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }
}
