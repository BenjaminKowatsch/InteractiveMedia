package com.media.interactive.cs3.hdm.interactivemedia.database.tables;


import android.database.sqlite.SQLiteDatabase;



/**
 * The Class PaymentTable.
 */
public class PaymentTable {

    /**
     * The Constant TABLE_NAME.
     */
    public static final String TABLE_NAME = "payment";

    /**
     * The Constant COLUMN_ID.
     */
    // id, auto-generated
    public static final String COLUMN_ID = "_id";

    /**
     * The Constant COLUMN_GROUP_ID.
     */
    //FK on group
    public static final String COLUMN_GROUP_ID = "group_id";

    /**
     * The Constant COLUMN_AMOUNT.
     */
    public static final String COLUMN_AMOUNT = "amount";

    /**
     * The Constant COLUMN_FROM_USER.
     */
    // FK on user
    public static final String COLUMN_FROM_USER = "from_user";

    /**
     * The Constant COLUMN_TO_USER.
     */
    // FK on user
    public static final String COLUMN_TO_USER = "to_user";

    /**
     * The Constant COLUMN_CREATED_AT.
     */
    // Used to identify payments created in one process. Usually only the newest ones will be displayed
    public static final String COLUMN_CREATED_AT = "created_at";

    /**
     * The Constant DATABASE_CREATE.
     */
    public static final String DATABASE_CREATE =
        "create table if not exists " + TABLE_NAME + "("
            + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
            + COLUMN_GROUP_ID + " INTEGER references " + GroupTable.TABLE_NAME
            + " (" + GroupTable.COLUMN_ID + ") on delete cascade on update cascade,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_FROM_USER + " INTEGER references " + UserTable.TABLE_NAME
            + " (" + UserTable.COLUMN_ID + ") on delete cascade on update cascade,"
            + COLUMN_TO_USER + " INTEGER references " + UserTable.TABLE_NAME
            + " (" + UserTable.COLUMN_ID + ") on delete cascade on update cascade,"
            + COLUMN_CREATED_AT + " TIMESTAMP NOT NULL)";

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
