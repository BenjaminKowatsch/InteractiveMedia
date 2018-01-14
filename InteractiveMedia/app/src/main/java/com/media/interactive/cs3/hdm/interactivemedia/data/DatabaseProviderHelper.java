package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benny on 08.01.18.
 */

public class DatabaseProviderHelper {

    private static final String TAG = DatabaseProviderHelper.class.getSimpleName();
    private ContentResolver contentResolver;

    public DatabaseProviderHelper(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void upsertUser(User user) {
        final String[] projection = {UserTable.COLUMN_ID};
        final String selection = UserTable.COLUMN_EMAIL + " = ?";
        final String[] selectionArgs = {user.getEmail()};
        final Cursor search = contentResolver.query(DatabaseProvider.CONTENT_USER_URI, projection, selection, selectionArgs, null);
        long foundId = -1;
        if (search.moveToNext()) {
            foundId = search.getLong(0);
        }
        final ContentValues userValues = new ContentValues();
        userValues.put(UserTable.COLUMN_USERNAME, user.getUsername());
        userValues.put(UserTable.COLUMN_IMAGE_URL, user.getImageUrl());
        userValues.put(UserTable.COLUMN_USER_ID, user.getUserId());
        userValues.put(UserTable.COLUMN_EMAIL, user.getEmail());
        userValues.put(UserTable.COLUMN_SYNCHRONIZED, user.getSync());
        if (foundId < 0) {
            final Uri result = contentResolver.insert(DatabaseProvider.CONTENT_USER_URI, userValues);
            user.setId(Long.parseLong(result.getLastPathSegment()));
        } else {
            final int update = contentResolver.update(DatabaseProvider.CONTENT_USER_URI, userValues, selection, selectionArgs);
            if(update > 0){
                Log.d(TAG,"Updated user entry.");
            }
            user.setId(foundId);
        }

    }

    public void insertGroupAtDatabase(Group group) {
        final ContentValues groupValues = new ContentValues();
        groupValues.put(GroupTable.COLUMN_NAME, group.getName());
        groupValues.put(GroupTable.COLUMN_IMAGE_URL, group.getImageUrl());
        groupValues.put(GroupTable.COLUMN_GROUP_ID, group.getGroupId());
        groupValues.put(GroupTable.COLUMN_CREATED_AT, group.getCreatedAt());
        groupValues.put(GroupTable.COLUMN_SYNCHRONIZED, group.getSync());
        final Uri result = contentResolver.insert(DatabaseProvider.CONTENT_GROUP_URI, groupValues);

        group.setId(Long.parseLong(result.getLastPathSegment()));
        Log.d(TAG,"Inserted group: "+ group.getId());
        findInsertUsersAtDatabase(group);

        for (User user : group.getUsers()) {
            final ContentValues groupUserValues = new ContentValues();
            groupUserValues.put(GroupUserTable.COLUMN_GROUP_ID, group.getId());
            groupUserValues.put(GroupUserTable.COLUMN_USER_ID, user.getId());
            contentResolver.insert(DatabaseProvider.CONTENT_GROUP_USER_URI, groupUserValues);
        }
        contentResolver.notifyChange(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI,null);
    }

    public void findInsertUsersAtDatabase(Group group) {
        for (User user : group.getUsers()) {
            final String[] projection = {UserTable.COLUMN_ID};
            final String selection = UserTable.COLUMN_EMAIL + " = ?";
            final String[] selectionArgs = {user.getEmail()};
            final Cursor search = contentResolver.query(DatabaseProvider.CONTENT_USER_URI, projection, selection, selectionArgs, null);
            long foundId = -1;
            if (search.moveToNext()) {
                foundId = search.getLong(0);
            }
            if (foundId < 0) {
                final ContentValues userValues = new ContentValues();
                userValues.put(UserTable.COLUMN_USERNAME, user.getUsername());
                userValues.put(UserTable.COLUMN_IMAGE_URL, user.getImageUrl());
                userValues.put(UserTable.COLUMN_EMAIL, user.getEmail());
                userValues.put(UserTable.COLUMN_USER_ID, user.getUserId());
                userValues.put(UserTable.COLUMN_SYNCHRONIZED, user.getSync());
                final Uri result = contentResolver.insert(DatabaseProvider.CONTENT_USER_URI, userValues);
                user.setId(Long.parseLong(result.getLastPathSegment()));
            } else {
                user.setId(foundId);
            }
        }
    }

    public void addTransactions(JSONArray jsonArray, String groupId) throws JSONException {
        for(int i = 0; i < jsonArray.length() ;i++){
            final JSONObject transactionObject = (JSONObject) jsonArray.get(i);
            final Transaction transaction = new Transaction();
            transaction.setGroupId(groupId);
            transaction.setAmount(transactionObject.getDouble("amount"));
            transaction.setInfoName(transactionObject.getString("infoName"));

            final JSONObject infoLocation = (JSONObject) transactionObject.getJSONObject("infoLocation");
            final Location location = new Location("");
            location.setLatitude(infoLocation.getDouble("latitude"));
            location.setLongitude(infoLocation.getDouble("longitude"));
            transaction.setLocation(location);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                transaction.setDateTime(sdf.parse(transactionObject.getString("infoCreatedAt")));
                transaction.setPublishedAt(sdf.parse(transactionObject.getString("publishedAt")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            transaction.setImageUrl(transactionObject.getString("infoImageUrl"));
            transaction.setPaidBy(transactionObject.getString("paidBy"));
            transaction.setSplit(transactionObject.getString("split"));
            Log.d(TAG, "Saving Transaction: "+ transaction.toString());
            saveTransaction(transaction);
        }
    }

    public void saveTransaction(Transaction transaction) {
        final ContentValues transactionContent = transaction.toContentValues();
        final Uri id = contentResolver.insert(DatabaseProvider.CONTENT_TRANSACTION_URI, transactionContent);
        if (id != null) {
            ContentValues transactionGroupContent = new ContentValues();
            final int transactionId = Integer.parseInt(id.getLastPathSegment());
            transactionGroupContent.put(GroupTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
            transactionGroupContent.put(GroupTransactionTable.COLUMN_GROUP_ID, transaction.getGroupId());
            contentResolver.insert(DatabaseProvider.CONTENT_GROUP_TRANSACTION_URI, transactionGroupContent);
        }
        contentResolver.notifyChange(DatabaseProvider.CONTENT_GROUP_TRANSACTION_JOIN_URI,null);
    }

    public long getGroupsByUserId(String userId){
        final String[] projection = {UserTable.COLUMN_ID};
        final String selection = UserTable.COLUMN_USER_ID.concat(" = ?");
        final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
        final Cursor userCursor = contentResolver.query(DatabaseProvider.CONTENT_USER_URI,projection,selection,selectionArgs,null);
        if(1 == userCursor.getCount()){
            userCursor.moveToNext();
            return userCursor.getLong(0);
        }
        return -1;
    }

    public boolean checkForCachedCredentials(Login login) {
        if (contentResolver != null) {
            boolean result = false;
            final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_LOGIN_URI,
                null, null, null,
                LoginTable.COLUMN_CREATED_AT + " DESC LIMIT 1");
            result = cursor.getCount() > 0;
            while (cursor.moveToNext()) {
                login.setId(cursor.getLong(0));
                login.getUser().setUsername(cursor.getString(1));
                login.setHashedPassword(cursor.getString(2));
                login.setUserType(UserType.values()[cursor.getInt(3)]);
                Log.d(TAG, "Latest Credentials cache: " + cursor.getString(3) + " " + login);
            }
            return result;
        }
        Log.e(TAG, "Could not find cached credentials, contentResolver is null.");
        return false;
    }

    public String getLatestTransactionPubDateByGroupId(String groupId){
        String result = null;
        final String[] projection = {TransactionTable.TABLE_NAME+ "." + TransactionTable.COLUMN_PUBLISHED_AT};
        final String selection = GroupTable.TABLE_NAME+"."+GroupTable.COLUMN_GROUP_ID + " = ?";
        final String[] selectionArgs = {groupId};
        final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_TRANSACTION_JOIN_URI,
            projection, selection, selectionArgs,
            TransactionTable.COLUMN_PUBLISHED_AT + " DESC LIMIT 1");
        Log.d(TAG, "LatestTransaction Count: " + cursor.getCount());
        if(cursor.moveToFirst()){
            result = cursor.getString(0);
        }
        return result;
    }

    public List<String> removeExistingGroupIds(JSONArray groupIds) {
        List<String> existingGroupIds = new ArrayList<>();
        for(int i = groupIds.length()-1; i>=0 ;i--){
            final String[] projection = {GroupTable.COLUMN_ID};
            final String selection = GroupTable.COLUMN_GROUP_ID + " = ?";
            String groupId = null;
            try {
                groupId = (String)groupIds.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String[] selectionArgs = {groupId};
            final Cursor search = contentResolver.query(DatabaseProvider.CONTENT_GROUP_URI, projection, selection, selectionArgs, null);
            long foundId = -1;
            if(search.moveToFirst()) {
                foundId = search.getLong(0);
            }
            if(foundId != -1){
                Log.d(TAG, "Removing groupId: "+ groupId);
                existingGroupIds.add(groupId);
                groupIds.remove(i);
            }
        }
        return existingGroupIds;
    }
}
