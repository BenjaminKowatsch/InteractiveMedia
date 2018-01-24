package com.media.interactive.cs3.hdm.interactivemedia.database.tables;


import android.database.sqlite.SQLiteDatabase;



/**
 * The Class SplitTable.
 */
public class SplitTable {

    /**
     * The Constant TABLE_NAME.
     */
    public static final String TABLE_NAME = "splits";

    /**
     * The Constant COLUMN_ID.
     */
    public static final String COLUMN_ID = "_id";

    /**
     * The Constant COLUMN_TYPE.
     */
    public static final String COLUMN_TYPE = "split_type";

    /**
     * The Constant COLUMN_AMOUNT.
     */
    public static final String COLUMN_AMOUNT = "amount";

    /**
     * The Constant COLUMN_USER_ID.
     */
    public static final String COLUMN_USER_ID = "user_id";

    /**
     * The Constant COLUMN_NEXT.
     */
    public static final String COLUMN_NEXT = "next";

    /**
     * The Constant DATABASE_CREATE.
     */
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
}
