package com.media.interactive.cs3.hdm.interactivemedia.contentprovider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.PaymentTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;

import static com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable.extractGroupFromCurrentPosition;
import static com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable.extractUserFromCurrentPosition;

/**
 * Created by benny on 31.10.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final int DB_VERSION = 1;

    private static final String DATABASE_NAME = "database.db";
    public static final String PAYMENT_USER_JOIN_COLUMN_FROM_USER = "from_name";
    public static final String PAYMENT_USER_JOIN_COLUMN_TO_USER = "to_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    // RESET Database for test purposes
    // TODO: Remove later on
    public void resetDatabase() {
        final SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LoginTable.DATABASE_DROP);
        db.execSQL(GroupTable.DATABASE_DROP);
        db.execSQL(PaymentTable.DATABASE_DROP);
        db.execSQL(UserTable.DATABASE_DROP);
        db.execSQL(TransactionTable.DATABASE_DROP);
        db.execSQL(DebtTable.DATABASE_DROP);
        db.execSQL(GroupUserTable.DATABASE_DROP);
        db.execSQL(GroupTransactionTable.DATABASE_DROP);

        // Enable foreign keys
        //db.execSQL("PRAGMA foreign_keys=ON;");

        // Call table onCreate methods
        LoginTable.onCreate(db);
        GroupTable.onCreate(db);
        UserTable.onCreate(db);
        TransactionTable.onCreate(db);
        DebtTable.onCreate(db);
        PaymentTable.onCreate(db);
        GroupTransactionTable.onCreate(db);
        GroupUserTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void deleteAllUsers() {
        final SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", null, null);
        db.delete("login", null, null);
    }

    public Group getGroupWithUsers(String groupId) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String query = "SELECT *"
                + " FROM " + GroupTable.TABLE_NAME
                + " WHERE " + GroupTable.COLUMN_GROUP_ID + " =  ?";
        final Cursor groupData = db.rawQuery(query, new String[]{String.valueOf(groupId)});
        if (groupData.moveToFirst()) {
            final Group group = extractGroupFromCurrentPosition(groupData);
            final Cursor userData = getUsersForGroup(group.getId());
            while (userData.moveToNext()) {
                group.getUsers().add(extractUserFromCurrentPosition(userData));
            }
            return group;
        } else {
            return null;
        }
    }

    public Cursor getUsersForGroup(long groupId) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String subQuery = "SELECT " + GroupUserTable.COLUMN_USER_ID + " FROM "
                + GroupUserTable.TABLE_NAME
                + " WHERE " + GroupUserTable.COLUMN_GROUP_ID + " = ?";
        final String query = "SELECT * FROM " + UserTable.TABLE_NAME
                + " WHERE " + UserTable.COLUMN_ID + " IN (" + subQuery + " )";
        final Cursor cursor = db.rawQuery(query, new String[]{"" + groupId});
        return cursor;
    }

    public Cursor getNewestPaymentsWithUserNamesForGroup(long groupId) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String fromUserTable = "u1";
        final String toUserTable = "u2";
        final String subQuery = "(SELECT " + "MAX(t." + PaymentTable.COLUMN_CREATED_AT + ") FROM " +
                PaymentTable.TABLE_NAME + " t WHERE t." + PaymentTable.COLUMN_GROUP_ID + " = ?" + ")";
        final String query = "SELECT " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_ID
                + ", " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_AMOUNT
                + ", " + buildAlias(fromUserTable, PAYMENT_USER_JOIN_COLUMN_FROM_USER)
                + ", " + buildAlias(toUserTable, PAYMENT_USER_JOIN_COLUMN_TO_USER)
                + " FROM " + PaymentTable.TABLE_NAME + " "
                + getUserNameLeftJoinString(fromUserTable, PaymentTable.COLUMN_FROM_USER)
                + getUserNameLeftJoinString(toUserTable, PaymentTable.COLUMN_TO_USER)
                + "\nWHERE " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_CREATED_AT
                + " = " + subQuery;
        return db.rawQuery(query, new String[]{"" + groupId});
    }

    @NonNull
    private String buildAlias(String fromUserTable, String columnName) {
        return fromUserTable + "." +
                UserTable.COLUMN_USERNAME + " " + columnName;
    }

    @NonNull
    private String getUserNameLeftJoinString(String fromUserTable, String column) {
        return "\nleft join " + UserTable.TABLE_NAME + " " + fromUserTable + " ON (" + PaymentTable.TABLE_NAME
                + "." + column + " = " + fromUserTable + "." + UserTable.COLUMN_ID + ") ";
    }

}
