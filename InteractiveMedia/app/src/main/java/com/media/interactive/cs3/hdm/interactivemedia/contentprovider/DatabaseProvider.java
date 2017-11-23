package com.media.interactive.cs3.hdm.interactivemedia.contentprovider;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;

/**
 * Created by benny on 20.11.17.
 */

public class DatabaseProvider extends android.content.ContentProvider {

    private static final String TAG ="DatabaseProvider";


    private DatabaseHelper databaseHelper;
    // Constants
    private static final String AUTHORITY = "com.media.interactive.cs3.hdm.interactivemedia.contentprovider";

    private static final int DEBT_CODE = 0;
    private static final int GROUP_CODE = 1;
    private static final int LOGIN_CODE = 2;
    private static final int TRANSACTION_CODE = 3;
    private static final int USER_CODE = 4;

    private static final int GROUP_TRANSACTION_CODE = 5;
    private static final int GROUP_USER_CODE = 6;

    public static final Uri CONTENT_DEBT_URI = Uri.parse("content://" + AUTHORITY + "/" + DebtTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_URI = Uri.parse("content://" + AUTHORITY + "/" + GroupTable.TABLE_NAME);
    public static final Uri CONTENT_LOGIN_URI = Uri.parse("content://" + AUTHORITY + "/" + LoginTable.TABLE_NAME);
    public static final Uri CONTENT_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + TransactionTable.TABLE_NAME);
    public static final Uri CONTENT_USER_URI = Uri.parse("content://" + AUTHORITY + "/" + UserTable.TABLE_NAME);

    public static final Uri CONTENT_GROUP_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + GroupTransactionTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_USER_URI = Uri.parse("content://" + AUTHORITY + "/" + GroupUserTable.TABLE_NAME);


    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(AUTHORITY, DebtTable.TABLE_NAME, DEBT_CODE);
        mUriMatcher.addURI(AUTHORITY, GroupTable.TABLE_NAME, GROUP_CODE);
        mUriMatcher.addURI(AUTHORITY, LoginTable.TABLE_NAME, LOGIN_CODE);
        mUriMatcher.addURI(AUTHORITY, TransactionTable.TABLE_NAME, TRANSACTION_CODE);
        mUriMatcher.addURI(AUTHORITY, UserTable.TABLE_NAME, USER_CODE);

        mUriMatcher.addURI(AUTHORITY, GroupTransactionTable.TABLE_NAME, GROUP_TRANSACTION_CODE);
        mUriMatcher.addURI(AUTHORITY, GroupUserTable.TABLE_NAME, GROUP_USER_CODE);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //checkColumns(projection);
        switch (mUriMatcher.match(uri))
        {
            case DEBT_CODE:
                sqLiteQueryBuilder.setTables(DebtTable.TABLE_NAME);
                break;
            case GROUP_CODE:
                sqLiteQueryBuilder.setTables(GroupTable.TABLE_NAME);
                break;
            case LOGIN_CODE:
                sqLiteQueryBuilder.setTables(LoginTable.TABLE_NAME);
                break;
            case TRANSACTION_CODE:
                sqLiteQueryBuilder.setTables(TransactionTable.TABLE_NAME);
                break;
            case USER_CODE:
                sqLiteQueryBuilder.setTables(UserTable.TABLE_NAME);
                break;
            case GROUP_TRANSACTION_CODE:
                sqLiteQueryBuilder.setTables(GroupTransactionTable.TABLE_NAME);
                break;
            case GROUP_USER_CODE:
                sqLiteQueryBuilder.setTables(GroupUserTable.TABLE_NAME);
                break;
            default:
                Log.e(TAG,"Error: Calling query method at DatabaseProvider with invalid uri.");
                break;
        }
        Cursor cursor = sqLiteQueryBuilder.query(databaseHelper.getWritableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        long id = 0;
        String tablename="";
        switch (mUriMatcher.match(uri))
        {
            case DEBT_CODE:
                tablename = DebtTable.TABLE_NAME;
                break;
            case GROUP_CODE:
                tablename = GroupTable.TABLE_NAME;
                break;
            case LOGIN_CODE:
                tablename = LoginTable.TABLE_NAME;
                break;
            case TRANSACTION_CODE:
                tablename=TransactionTable.TABLE_NAME;
                break;
            case USER_CODE:
                tablename=UserTable.TABLE_NAME;
                break;
            case GROUP_TRANSACTION_CODE:
                tablename=GroupTransactionTable.TABLE_NAME;
                break;
            case GROUP_USER_CODE:
                tablename=GroupUserTable.TABLE_NAME;
                break;
            default:
                Log.e(TAG,"Error: Calling insert method at DatabaseProvider with invalid uri.");
                break;
        }
        id = sqLiteDatabase.insert(tablename,null,values);
        getContext().getContentResolver().notifyChange(uri,null);
        return Uri.parse(tablename+"/"+id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (mUriMatcher.match(uri))
        {
            case DEBT_CODE:
                rowsDeleted = sqLiteDatabase.delete(DebtTable.TABLE_NAME,selection,selectionArgs);
                break;
            case GROUP_CODE:
                rowsDeleted = sqLiteDatabase.delete(GroupTable.TABLE_NAME,selection,selectionArgs);
                break;
            case LOGIN_CODE:
                rowsDeleted = sqLiteDatabase.delete(LoginTable.TABLE_NAME,selection,selectionArgs);
                break;
            case TRANSACTION_CODE:
                rowsDeleted = sqLiteDatabase.delete(TransactionTable.TABLE_NAME,selection,selectionArgs);
                break;
            case USER_CODE:
                rowsDeleted = sqLiteDatabase.delete(UserTable.TABLE_NAME,selection,selectionArgs);
                break;
            case GROUP_TRANSACTION_CODE:
                rowsDeleted = sqLiteDatabase.delete(GroupTransactionTable.TABLE_NAME,selection,selectionArgs);
                break;
            case GROUP_USER_CODE:
                rowsDeleted = sqLiteDatabase.delete(GroupUserTable.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                Log.e(TAG,"Error: Calling delete method at DatabaseProvider with invalid uri.");
                break;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (mUriMatcher.match(uri))
        {
            case DEBT_CODE:
                rowsUpdated = sqLiteDatabase.update(DebtTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case GROUP_CODE:
                rowsUpdated = sqLiteDatabase.update(GroupTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LOGIN_CODE:
                rowsUpdated = sqLiteDatabase.update(LoginTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRANSACTION_CODE:
                rowsUpdated = sqLiteDatabase.update(TransactionTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case USER_CODE:
                rowsUpdated = sqLiteDatabase.update(UserTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case GROUP_TRANSACTION_CODE:
                rowsUpdated = sqLiteDatabase.update(GroupTransactionTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case GROUP_USER_CODE:
                rowsUpdated = sqLiteDatabase.update(GroupUserTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                Log.e(TAG,"Error: Calling update method at DatabaseProvider with invalid uri.");
                break;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsUpdated;
    }
}