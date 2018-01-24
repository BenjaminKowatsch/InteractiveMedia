package com.media.interactive.cs3.hdm.interactivemedia.database.tables;


import android.database.sqlite.SQLiteDatabase;

public class SplitTable {
    public static final String TABLE_NAME = "splits";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TYPE = "split_type";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NEXT = "next";

    public static final String DATABASE_CREATE =
            "create table if not exists " + TABLE_NAME + "("
                    + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
                    + COLUMN_TYPE + " TEXT NOT NULL,"
                    + COLUMN_AMOUNT + " REAL NULL,"
                    + COLUMN_USER_ID + " TEXT NULL references " + UserTable.TABLE_NAME
                + " (" + UserTable.COLUMN_USER_ID + "),"
                    + COLUMN_NEXT + " INTEGER NULL references " + SplitTable.TABLE_NAME
                    + " (" + SplitTable.COLUMN_ID + ") on delete cascade on update cascade"
                    + ")";


    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }
}
