package com.media.interactive.cs3.hdm.interactivemedia.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;

/**
 * Created by benny on 20.11.17.
 */

public class DebtTable {

    public static final String TABLE_NAME = "debt";
    /**
     * First attribute, type integer.
     */
    public static final String COLUMN_ID = "_id";
    /**
     * Second attribute, type INTEGER references.
     */
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    /**
     * Third attribute, type REAL NOT NULL.
     */
    public static final String COLUMN_AMOUNT = "amount";
    /**
     * Fourth attribute, type INTEGER references.
     */
    public static final String COLUMN_FROM_USER = "from_user";
    /**
     * Fifth attribute, type INTEGER references.
     */
    public static final String COLUMN_TO_USER = "to_user";
    /**
     * Sixth attribute, type TIMESTAMP DEFAULT CURRENT_TIMESTAMP.
     */
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String DATABASE_CREATE =
            "create table if not exists " + TABLE_NAME + "("
                    + COLUMN_ID + " integer unique primary key AUTOINCREMENT,"
                    + COLUMN_TRANSACTION_ID + " INTEGER references " + TransactionTable.TABLE_NAME
                    + " (" + TransactionTable.COLUMN_ID + ") on delete cascade on update cascade,"
                    + COLUMN_AMOUNT + " REAL NOT NULL,"
                    + COLUMN_FROM_USER + " INTEGER references " + UserTable.TABLE_NAME
                    + " (" + UserTable.COLUMN_ID + ") on delete cascade on update cascade,"
                    + COLUMN_TO_USER + " INTEGER references " + UserTable.TABLE_NAME
                    + " (" + UserTable.COLUMN_ID + ") on delete cascade on update cascade,"
                    + COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    public static final String DATABASE_DROP = "drop table if exists " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static Debt extractDebtFromCurrentPosition(Cursor cursor) {
        final long transactionId = cursor.getLong(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
        final long creditorId = cursor.getLong(cursor.getColumnIndex(COLUMN_TO_USER));
        final long debtorId = cursor.getLong(cursor.getColumnIndex(COLUMN_FROM_USER));
        final double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
        return new Debt(creditorId, debtorId, amount, transactionId);
    }
}
