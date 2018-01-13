package com.media.interactive.cs3.hdm.interactivemedia.contentprovider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;

/**
 * Created by benny on 31.10.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final int DB_VERSION = 1;

    private static final String DATABASE_NAME = "database.db";

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
    
    public Cursor getAllGroupAndUsersByGroup(long groupId){
        final SQLiteDatabase db = this.getWritableDatabase();
        final String query = "SELECT *"
            +" FROM "+ UserTable.TABLE_NAME + " u, "
            + GroupUserTable.TABLE_NAME + " gu"
            + " WHERE u."+UserTable.COLUMN_ID+" = gu." + GroupUserTable.COLUMN_USER_ID
            + " AND gu."+GroupUserTable.COLUMN_GROUP_ID+" =  ?";
        final Cursor data = db.rawQuery(query, new String[]{ String.valueOf(groupId) });
        return data;
    }

    public Cursor getAllGroupsByUserId(String userId, String searchString){
        final SQLiteDatabase db = this.getWritableDatabase();
        final String query = "SELECT g.*"
            +" FROM "
            + GroupTable.TABLE_NAME + " g, "
            + UserTable.TABLE_NAME + " u, "
            + GroupUserTable.TABLE_NAME + " gu"
            + " WHERE u."+UserTable.COLUMN_ID+" = gu." + GroupUserTable.COLUMN_USER_ID
            + " AND g."+GroupTable.COLUMN_ID+" = gu." + GroupUserTable.COLUMN_GROUP_ID
            + " AND u."+UserTable.COLUMN_USER_ID+" = ? "
            + " AND ( g."+ GroupTable.COLUMN_NAME + " like ? OR g." + GroupTable.COLUMN_CREATED_AT + " like ? )";
        if(userId == null){
            userId = "";
        }
        final String search = "%"+searchString+"%";
        final Cursor data = db.rawQuery(query, new String[]{ userId, search, search });
        return data;
    }

    public Cursor getTransactionsForGroup(long groupId) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final String subQuery = "SELECT " + GroupTransactionTable.COLUMN_TRANSACTION_ID + " FROM "
                + GroupTransactionTable.TABLE_NAME
                + " WHERE " + GroupTransactionTable.COLUMN_GROUP_ID + " = ?";
        final String query = "SELECT * FROM " + TransactionTable.TABLE_NAME
                + " WHERE " + TransactionTable.COLUMN_ID + " IN (" + subQuery + " )";
        final Cursor cursor = db.rawQuery(query, new String[]{"" + groupId});
        return cursor;
    }
    
}
