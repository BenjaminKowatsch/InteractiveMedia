package com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables;


import android.database.sqlite.SQLiteDatabase;

public class PaymentTable {

    public static final String TABLE_NAME = "payment";
    // id, auto-generated
    public static final String COLUMN_ID = "_id";
    //FK on group
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_AMOUNT = "amount";
    // FK on user
    public static final String COLUMN_FROM_USER = "from_user";
    // FK on user
    public static final String COLUMN_TO_USER = "to_user";
    // Used to identify payments created in one process. Usually only the newest ones will be displayed
    public static final String COLUMN_CREATED_AT = "created_at";

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

    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }
}
