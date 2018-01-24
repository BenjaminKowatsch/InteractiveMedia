package com.media.interactive.cs3.hdm.interactivemedia.database.tables;

import android.database.sqlite.SQLiteDatabase;



/**
 * Created by benny on 20.11.17.
 */

public class TransactionTable {

    /**
     * The Constant TABLE_NAME.
     */
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
     * Fifth attribute, type TEXT.
     */
    public static final String COLUMN_INFO_LOCATION_LONG = "info_location_long";

    /**
     * The Constant COLUMN_INFO_LOCATION_LAT.
     */
    public static final String COLUMN_INFO_LOCATION_LAT = "info_location_lat";

    /**
     * Seventh attribute, type TEXT.
     */
    public static final String COLUMN_INFO_IMAGE_URL = "info_image_url";
    /**
     * Eighth attribute, type TIMESTAMP DEFAULT CURRENT_TIMESTAMP.
     */
    public static final String COLUMN_INFO_CREATED_AT = "info_created_at";

    /**
     * Indicates if transaction was already sent to server.
     */
    public static final String COLUMN_SYNCHRONIZED = "synchronized";

    /**
     * The time at which the transaction was published, also used to pull transactions.
     */
    public static final String COLUMN_PUBLISHED_AT = "published_at";

    /**
     * The Constant COLUMN_SPLIT.
     */
    public static final String COLUMN_SPLIT = "split";

    /**
     * The Constant DATABASE_CREATE.
     */
    public static final String DATABASE_CREATE =
        "create table if not exists " + TABLE_NAME + "("
            + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_PAID_BY + " TEXT NOT NULL references " + UserTable.TABLE_NAME
            + " (" + UserTable.COLUMN_USER_ID + "), "
            + COLUMN_INFO_NAME + " TEXT NOT NULL,"
            + COLUMN_INFO_LOCATION_LONG + " REAL,"
            + COLUMN_INFO_LOCATION_LAT + " REAL,"
            + COLUMN_INFO_IMAGE_URL + " TEXT,"
            + COLUMN_INFO_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_SYNCHRONIZED + " INTEGER NOT NULL,"
            + COLUMN_PUBLISHED_AT + " TIMESTAMP, "
            + COLUMN_SPLIT + " INTEGER references " + SplitTable.TABLE_NAME
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
