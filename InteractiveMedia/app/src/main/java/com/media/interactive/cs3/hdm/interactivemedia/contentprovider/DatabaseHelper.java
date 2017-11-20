package com.media.interactive.cs3.hdm.interactivemedia.contentprovider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.UserType;

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
        db.execSQL("PRAGMA foreign_keys=ON;");
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
    /*
    public Cursor getUsers() {
        final SQLiteDatabase db = this.getWritableDatabase();
        final String query = "SELECT * FROM " + TABLE_NAME;
        final Cursor data = db.rawQuery(query, null);
        return data;
    }

    public boolean checkForCachedCredentials(Login login){
            boolean result = false;
            final SQLiteDatabase db = this.getWritableDatabase();
            final String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+COL4+" DESC LIMIT 1";
            final Cursor cursor = db.rawQuery(query, null);
            result = cursor.getCount() > 0;
            while(cursor.moveToNext()){
                login.setId(cursor.getLong(0));
                login.setUsername(cursor.getString(1));
                login.setHashedPassword(cursor.getString(2));
                login.setEmail(cursor.getString(3));
                login.setUserType(UserType.values()[cursor.getInt(4)]);
                Log.d(TAG,"Latest Credentials cache: " + cursor.getString(4)+ " "+ login);
            }
        return result;
    }


    public boolean cacheCredentials(Login login) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, login.getUsername());
        contentValues.put(COL2, login.getHashedPassword());
        contentValues.put(COL4, login.getUserType().getValue());

        Log.d(TAG, "cacheCredentials: Adding " + login + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            login.setId(result);
            return true;
        }
    }

    public boolean deleteUser(Login login) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final String sql = "DELETE FROM "+TABLE_NAME +" WHERE "+COL0+"=?";
        final SQLiteStatement statement = db.compileStatement(sql);

        statement.bindLong(1, login.getId());

        return statement.executeUpdateDelete() > 0;
    }*/
}
