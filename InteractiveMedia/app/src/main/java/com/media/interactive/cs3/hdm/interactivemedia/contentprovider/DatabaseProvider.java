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
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.PaymentTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;

/**
 * Created by benny on 20.11.17.
 */

public class DatabaseProvider extends android.content.ContentProvider {

    private static final String TAG = "DatabaseProvider";
    // Constants
    private static final String AUTHORITY =
            "com.media.interactive.cs3.hdm.interactivemedia.contentprovider";
    private static final String GROUP_USER_JOIN_TABLE = GroupTable.TABLE_NAME
            + "_" + GroupUserTable.TABLE_NAME
            + "_" + UserTable.TABLE_NAME;
    private static final String GROUP_TRANSACTION_JOIN_TABLE = GroupTable.TABLE_NAME
            + "_" + GroupTransactionTable.TABLE_NAME
            + "_" + TransactionTable.TABLE_NAME;
    private static final String GROUP_USER_TRANSACTION_JOIN_TABLE = GroupTable.TABLE_NAME
            + "_" + GroupTransactionTable.TABLE_NAME
            + "_" + TransactionTable.TABLE_NAME
            + "_" + UserTable.TABLE_NAME;
    private static final String GROUP_ID_DEBT_JOIN_TABLE = DebtTable.TABLE_NAME
            + "_" + GroupTransactionTable.TABLE_NAME;

    public static final Uri CONTENT_DEBT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + DebtTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GroupTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_USER_JOIN_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GROUP_USER_JOIN_TABLE);
    public static final Uri CONTENT_GROUP_TRANSACTION_JOIN_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GROUP_TRANSACTION_JOIN_TABLE);
    public static final Uri CONTENT_GROUP_USER_TRANSACTION_JOIN_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GROUP_USER_TRANSACTION_JOIN_TABLE);
    public static final Uri CONTENT_LOGIN_URI = Uri.parse("content://"
            + AUTHORITY + "/" + LoginTable.TABLE_NAME);
    public static final Uri CONTENT_TRANSACTION_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TransactionTable.TABLE_NAME);
    public static final Uri CONTENT_USER_URI = Uri.parse("content://"
            + AUTHORITY + "/" + UserTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_TRANSACTION_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GroupTransactionTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_USER_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GroupUserTable.TABLE_NAME);
    public static final Uri CONTENT_GROUP_ID_DEBT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GROUP_ID_DEBT_JOIN_TABLE);
    public static final Uri CONTENT_PAYMENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + PaymentTable.TABLE_NAME);
    private static final int DEBT_CODE = 0;
    private static final int GROUP_CODE = 1;
    private static final int LOGIN_CODE = 2;
    private static final int TRANSACTION_CODE = 3;
    private static final int USER_CODE = 4;
    private static final int GROUP_TRANSACTION_CODE = 5;
    private static final int GROUP_USER_CODE = 6;
    private static final int GROUP_USER_JOIN_CODE = 7;
    private static final int GROUP_TRANSACTION_JOIN_CODE = 8;
    private static final int GROUP_USER_TRANSACTION_JOIN_CODE = 9;
    private static final int PAYMENT_CODE = 10;
    private static final int DEBT_GROUP_ID_JOIN_CODE = 11;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(AUTHORITY, DebtTable.TABLE_NAME, DEBT_CODE);
        mUriMatcher.addURI(AUTHORITY, GroupTable.TABLE_NAME, GROUP_CODE);
        mUriMatcher.addURI(AUTHORITY, LoginTable.TABLE_NAME, LOGIN_CODE);
        mUriMatcher.addURI(AUTHORITY, TransactionTable.TABLE_NAME, TRANSACTION_CODE);
        mUriMatcher.addURI(AUTHORITY, UserTable.TABLE_NAME, USER_CODE);
        mUriMatcher.addURI(AUTHORITY, PaymentTable.TABLE_NAME, PAYMENT_CODE);

        mUriMatcher.addURI(AUTHORITY, GROUP_USER_JOIN_TABLE, GROUP_USER_JOIN_CODE);
        mUriMatcher.addURI(AUTHORITY, GROUP_TRANSACTION_JOIN_TABLE, GROUP_TRANSACTION_JOIN_CODE);
        mUriMatcher.addURI(AUTHORITY, GROUP_USER_TRANSACTION_JOIN_TABLE, GROUP_USER_TRANSACTION_JOIN_CODE);
        mUriMatcher.addURI(AUTHORITY, GROUP_ID_DEBT_JOIN_TABLE, DEBT_GROUP_ID_JOIN_CODE);

        mUriMatcher.addURI(AUTHORITY, GroupTransactionTable.TABLE_NAME, GROUP_TRANSACTION_CODE);
        mUriMatcher.addURI(AUTHORITY, GroupUserTable.TABLE_NAME, GROUP_USER_CODE);
    }

    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "***************** Init DB Provider + Rest all DBs ****************");
        databaseHelper = new DatabaseHelper(getContext());
        //databaseHelper.resetDatabase();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //checkColumns(projection);
        switch (mUriMatcher.match(uri)) {
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
            case PAYMENT_CODE:
                sqLiteQueryBuilder.setTables(PaymentTable.TABLE_NAME);
                break;
            case GROUP_USER_JOIN_CODE:
                sqLiteQueryBuilder.setTables(UserTable.TABLE_NAME
                        + " INNER JOIN "
                        + GroupUserTable.TABLE_NAME + " ON " + GroupUserTable.TABLE_NAME + "." + GroupUserTable.COLUMN_USER_ID + " = " + UserTable.TABLE_NAME + "." + UserTable.COLUMN_ID
                        + " INNER JOIN "
                        + GroupTable.TABLE_NAME + " ON " + GroupUserTable.TABLE_NAME + "." + GroupUserTable.COLUMN_GROUP_ID + " = " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_ID);
                break;
            case GROUP_TRANSACTION_JOIN_CODE:
                sqLiteQueryBuilder.setTables(TransactionTable.TABLE_NAME
                        + " INNER JOIN "
                        + GroupTransactionTable.TABLE_NAME + " ON " + GroupTransactionTable.TABLE_NAME + "." + GroupTransactionTable.COLUMN_TRANSACTION_ID + " = " + TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_ID
                        + " INNER JOIN "
                        + GroupTable.TABLE_NAME + " ON " + GroupTransactionTable.TABLE_NAME + "." + GroupTransactionTable.COLUMN_GROUP_ID + " = " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID);
                break;
            case GROUP_USER_TRANSACTION_JOIN_CODE:
                sqLiteQueryBuilder.setTables(TransactionTable.TABLE_NAME
                        + " INNER JOIN "
                        + GroupTransactionTable.TABLE_NAME + " ON " + GroupTransactionTable.TABLE_NAME + "." + GroupTransactionTable.COLUMN_TRANSACTION_ID + " = " + TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_ID
                        + " INNER JOIN "
                        + GroupTable.TABLE_NAME + " ON " + GroupTransactionTable.TABLE_NAME + "." + GroupTransactionTable.COLUMN_GROUP_ID + " = " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID
                        + " INNER JOIN "
                        + UserTable.TABLE_NAME + " ON " + UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = " + TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_PAID_BY);
                break;
            case DEBT_GROUP_ID_JOIN_CODE:
                sqLiteQueryBuilder.setTables(DebtTable.TABLE_NAME
                        + " INNER JOIN "
                        + GroupTransactionTable.TABLE_NAME + " ON " + GroupTransactionTable.TABLE_NAME + "." + GroupTransactionTable.COLUMN_TRANSACTION_ID + " = " + DebtTable.TABLE_NAME + "." + DebtTable.COLUMN_TRANSACTION_ID);
                break;
            default:
                Log.e(TAG, "Error: Calling query method at DatabaseProvider with invalid uri.");
                break;
        }
        final Cursor cursor = sqLiteQueryBuilder.query(databaseHelper.getWritableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        long id = 0;
        String tablename = "";
        switch (mUriMatcher.match(uri)) {
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
                tablename = TransactionTable.TABLE_NAME;
                break;
            case USER_CODE:
                tablename = UserTable.TABLE_NAME;
                break;
            case GROUP_TRANSACTION_CODE:
                tablename = GroupTransactionTable.TABLE_NAME;
                break;
            case GROUP_USER_CODE:
                tablename = GroupUserTable.TABLE_NAME;
                break;
            case PAYMENT_CODE:
                tablename = PaymentTable.TABLE_NAME;
                break;
            default:
                Log.e(TAG, "Error: Calling insert method at DatabaseProvider with invalid uri.");
                break;
        }
        id = sqLiteDatabase.insert(tablename, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(tablename + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (mUriMatcher.match(uri)) {
            case DEBT_CODE:
                rowsDeleted = sqLiteDatabase.delete(DebtTable.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUP_CODE:
                rowsDeleted = sqLiteDatabase.delete(GroupTable.TABLE_NAME, selection, selectionArgs);
                break;
            case LOGIN_CODE:
                rowsDeleted = sqLiteDatabase.delete(LoginTable.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTION_CODE:
                rowsDeleted = sqLiteDatabase.delete(TransactionTable.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_CODE:
                rowsDeleted = sqLiteDatabase.delete(UserTable.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUP_TRANSACTION_CODE:
                rowsDeleted = sqLiteDatabase.delete(GroupTransactionTable.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUP_USER_CODE:
                rowsDeleted = sqLiteDatabase.delete(GroupUserTable.TABLE_NAME, selection, selectionArgs);
                break;
            case PAYMENT_CODE:
                rowsDeleted = sqLiteDatabase.delete(PaymentTable.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                Log.e(TAG, "Error: Calling delete method at DatabaseProvider with invalid uri.");
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (mUriMatcher.match(uri)) {
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
            case PAYMENT_CODE:
                rowsUpdated = sqLiteDatabase.update(PaymentTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                Log.e(TAG, "Error: Calling update method at DatabaseProvider with invalid uri.");
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
