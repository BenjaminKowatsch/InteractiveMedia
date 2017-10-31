package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by benny on 31.10.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "database";
    private static final String TABLE_NAME = "users";
    private static final String COL0 = "ID";
    private static final String COL1 = "username";
    private static final String COL2 = "password";
    private static final String COL3 = "email";
    private static final String COL4 = "lastLogin";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    // RESET Database for test purposes
    // TODO: Remove later on
    public void resetDatabase() {
        final SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.d(TAG, "onCreate: Dropping table " + TABLE_NAME);

        final String createTable = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 + " TEXT," + COL2 + " TEXT," + COL3 + " TEXT, "+ COL4 +" TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createTable);
        Log.d(TAG, "onCreate: Creating table " + TABLE_NAME + " with " + createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteAllUsers() {
        final SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public Cursor getUsers() {
        final SQLiteDatabase db = this.getWritableDatabase();
        final String query = "SELECT * FROM " + TABLE_NAME;
        final Cursor data = db.rawQuery(query, null);
        return data;
    }

    public boolean checkForCachedCredentials(User user){
            boolean result = false;
            final SQLiteDatabase db = this.getWritableDatabase();
            final String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+COL4+" DESC LIMIT 1";
            final Cursor cursor = db.rawQuery(query, null);
            result = cursor.getCount() > 0;
            while(cursor.moveToNext()){
                user.setId(cursor.getInt(0)+"");
                user.setUsername(cursor.getString(1));
                user.setHashedPassword(cursor.getString(2));
                user.setEmail(cursor.getString(3));
                Log.d(TAG,"Latest Credentials cache: " + cursor.getString(4)+ " "+ user);
            }
        return result;
    }


    public boolean cacheCredentials(User user) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, user.getUsername());
        contentValues.put(COL2, user.getHashedPassword());

        Log.d(TAG, "cacheCredentials: Adding " + user + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
}
