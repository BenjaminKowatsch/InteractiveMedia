package com.media.interactive.cs3.hdm.interactivemedia.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.PaymentTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.SplitTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;

import static android.database.DatabaseUtils.dumpCursorToString;
import static com.media.interactive.cs3.hdm.interactivemedia.database.tables.GroupTable.extractGroupFromCurrentPosition;
import static com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable.extractUserFromCurrentPosition;



/**
 * Created by benny on 31.10.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * The Constant PAYMENT_USER_JOIN_COLUMN_FROM_USER.
     */
    public static final String PAYMENT_USER_JOIN_COLUMN_FROM_USER = "from_name";
    /**
     * The Constant PAYMENT_USER_JOIN_COLUMN_TO_USER.
     */
    public static final String PAYMENT_USER_JOIN_COLUMN_TO_USER = "to_name";
    /**
     * The Constant TAG.
     */
    private static final String TAG = "DatabaseHelper";
    /**
     * The Constant DB_VERSION.
     */
    private static final int DB_VERSION = 1;
    /**
     * The Constant DATABASE_NAME.
     */
    private static final String DATABASE_NAME = "database.db";

    /**
     * Instantiates a new database helper.
     *
     * @param context the context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    /**
     * Reset database.
     */
    // Resets the database for test purposes
    public void resetDatabase() {
        final SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
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
        db.execSQL(SplitTable.DATABASE_DROP);

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
        SplitTable.onCreate(db);
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        onCreate(db);
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onDowngrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * Delete all users.
     */
    public void deleteAllUsers() {
        final SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", null, null);
        db.delete("login", null, null);
    }

    /**
     * Gets the group with users.
     *
     * @param groupId the group id
     * @return the group with users
     */
    public Group getGroupWithUsers(String groupId) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String query = "SELECT *"
            + " FROM " + GroupTable.TABLE_NAME
            + " WHERE " + GroupTable.COLUMN_GROUP_ID + " =  ?";
        final Cursor groupData = db.rawQuery(query, new String[] {String.valueOf(groupId)});
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

    /**
     * Gets the users for group.
     *
     * @param groupId the group id
     * @return the users for group
     */
    public Cursor getUsersForGroup(long groupId) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String subQuery = "SELECT " + GroupUserTable.COLUMN_USER_ID + " FROM "
            + GroupUserTable.TABLE_NAME
            + " WHERE " + GroupUserTable.COLUMN_GROUP_ID + " = ?";
        final String query = "SELECT * FROM " + UserTable.TABLE_NAME
            + " WHERE " + UserTable.COLUMN_ID + " IN (" + subQuery + " )";
        final Cursor cursor = db.rawQuery(query, new String[] {"" + groupId});
        return cursor;
    }

    /**
     * Gets the newest payments with user names for group.
     *
     * @param groupId the group id
     * @return the newest payments with user names for group
     */
    public Cursor getNewestPaymentsWithUserNamesForGroup(long groupId) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String fromUserTable = "u1";
        final String toUserTable = "u2";
        final String subQuery = "(SELECT " + "MAX(t." + PaymentTable.COLUMN_CREATED_AT + ") FROM "
            + PaymentTable.TABLE_NAME + " t WHERE t." + PaymentTable.COLUMN_GROUP_ID + " = ?" + ")";
        final String joinQuery = "SELECT " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_ID
            + ", " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_CREATED_AT
            + ", " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_AMOUNT
            + ", " + buildAlias(fromUserTable, PAYMENT_USER_JOIN_COLUMN_FROM_USER)
            + ", " + buildAlias(toUserTable, PAYMENT_USER_JOIN_COLUMN_TO_USER)
            + " FROM " + PaymentTable.TABLE_NAME + " "
            + getUserNameLeftJoinString(fromUserTable, PaymentTable.COLUMN_FROM_USER)
            + getUserNameLeftJoinString(toUserTable, PaymentTable.COLUMN_TO_USER);
        final String query = joinQuery
            + "\nWHERE " + PaymentTable.TABLE_NAME + "." + PaymentTable.COLUMN_CREATED_AT
            + " = " + subQuery;
        Log.d(TAG, dumpCursorToString(db.rawQuery(joinQuery, null)));
        return db.rawQuery(query, new String[] {"" + groupId});
    }

    /**
     * Builds the alias.
     *
     * @param tableAlias the table alias
     * @param columnName the column name
     * @return the string
     */
    @NonNull
    private String buildAlias(String tableAlias, String columnName) {
        return tableAlias + "." + UserTable.COLUMN_USERNAME + " " + columnName;
    }

    /**
     * Gets the user name left join string.
     *
     * @param tableAlias the table alias
     * @param column     the column
     * @return the user name left join string
     */
    @NonNull
    private String getUserNameLeftJoinString(String tableAlias, String column) {
        return "\nleft join " + UserTable.TABLE_NAME + " " + tableAlias + " ON (" + PaymentTable.TABLE_NAME
            + "." + column + " = " + tableAlias + "." + UserTable.COLUMN_ID + ") ";
    }

}
