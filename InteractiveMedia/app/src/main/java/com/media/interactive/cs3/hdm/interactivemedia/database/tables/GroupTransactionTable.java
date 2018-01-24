package com.media.interactive.cs3.hdm.interactivemedia.database.tables;

import android.database.sqlite.SQLiteDatabase;



/**
 * Created by benny on 20.11.17.
 */

public class GroupTransactionTable {

    /**
     * The Constant TABLE_NAME.
     */
    public static final String TABLE_NAME = "group_transaction";
    /**
     * First attribute, type TEXT references.
     */
    public static final String COLUMN_GROUP_ID = "group_id";
    /**
     * Second attribute, type integer references.
     */
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";

    /**
     * The Constant DATABASE_CREATE.
     */
    public static final String DATABASE_CREATE =
        "create table if not exists " + TABLE_NAME + "("
            + COLUMN_GROUP_ID + " TEXT references " + GroupTable.TABLE_NAME
            + " (" + GroupTable.COLUMN_GROUP_ID + ") on delete cascade on update cascade,"
            + COLUMN_TRANSACTION_ID + " INTEGER references "
            + TransactionTable.TABLE_NAME + " (" + TransactionTable.COLUMN_ID
            + ") on delete cascade on update cascade)";

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
