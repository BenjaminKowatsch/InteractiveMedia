package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

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
      if (update > 0) {
        Log.d(TAG, "Updated user entry.");
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
    Log.d(TAG, "Inserted group: " + group.getId());
    findInsertUsersAtDatabase(group);

    for (User user : group.getUsers()) {
      final ContentValues groupUserValues = new ContentValues();
      groupUserValues.put(GroupUserTable.COLUMN_GROUP_ID, group.getId());
      groupUserValues.put(GroupUserTable.COLUMN_USER_ID, user.getId());
      contentResolver.insert(DatabaseProvider.CONTENT_GROUP_USER_URI, groupUserValues);
    }
    contentResolver.notifyChange(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, null);
  }

  public void setImageUrlByResponse(Context context, Group group, JSONObject response) {
    JSONObject payload = null;
    String imageName = null;
    try {
      payload = response.getJSONObject("payload");
      imageName = payload.getString("path");
      Log.d(TAG, "Path returned: " + payload.getString("path"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    final String newImageUrl = context.getResources().getString(R.string.web_service_url)
        .concat("/v1/object-store/download?filename=").concat(imageName);
    group.setImageUrl(newImageUrl);
  }

  public void setTransactionImageUrlByResponse(Context context, Transaction transaction, JSONObject response) {
    JSONObject payload = null;
    String imageName = null;
    try {
      payload = response.getJSONObject("payload");
      imageName = payload.getString("path");
      Log.d(TAG, "Path returned: " + payload.getString("path"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    final String newImageUrl = context.getResources().getString(R.string.web_service_url)
        .concat("/v1/object-store/download?filename=").concat(imageName);
    transaction.setImageUrl(newImageUrl);
  }

  public void updateTransactionWithResponse(Transaction transaction, JSONObject payload) {
    try {
        transaction.setPublishedAt(Helper.ParseDateString(payload.getString("publishedAt")));
        Log.d(TAG, "Transaction PublishedDate: "+ transaction.getPublishedAt());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    // Update transaction values
    final ContentValues transactionUpdateValues = new ContentValues();
    transactionUpdateValues.put(TransactionTable.COLUMN_INFO_IMAGE_URL, transaction.getImageUrl());
    transactionUpdateValues.put(TransactionTable.COLUMN_PUBLISHED_AT, Helper.FormatDate(transaction.getPublishedAt()));
    transactionUpdateValues.put(TransactionTable.COLUMN_SYNCHRONIZED, true);
    final String transactionSelection = TransactionTable.COLUMN_ID.concat(" = ?");
    Log.d(TAG, "Transaction SelectionArgs: "+ transaction.getId());
    final String[] transactionsSelectionArgs = {String.valueOf(transaction.getId())};
    contentResolver.update(DatabaseProvider.CONTENT_TRANSACTION_URI, transactionUpdateValues, transactionSelection, transactionsSelectionArgs);
    Log.d(TAG, "Updated Transaction.");
    contentResolver.notifyChange(DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, null);

  }

  public void updateGroupWithResponse(Group group, JSONObject payload) {
    try {
      final String groupId = payload.getString("groupId");
      // Update group values
      final ContentValues groupUpdateValues = new ContentValues();
      groupUpdateValues.put(GroupTable.COLUMN_GROUP_ID, groupId);
      groupUpdateValues.put(GroupTable.COLUMN_IMAGE_URL, group.getImageUrl());
      groupUpdateValues.put(GroupTable.COLUMN_SYNCHRONIZED, true);
      final String groupSelection = GroupTable.COLUMN_ID.concat(" = ?");
      final String[] groupSelectionArgs = {String.valueOf(group.getId())};
      contentResolver.update(DatabaseProvider.CONTENT_GROUP_URI, groupUpdateValues, groupSelection, groupSelectionArgs);
      // Update user values
      final JSONArray users = payload.getJSONArray("users");
      for (int i = 0; i < users.length(); i++) {
        final JSONObject jsonObject = (JSONObject) users.get(i);
        final ContentValues userUpdateValues = new ContentValues();
        final String userEmail = jsonObject.getString("email");
        if (jsonObject.has("username")) {
          userUpdateValues.put(UserTable.COLUMN_USERNAME, jsonObject.getString("username"));
        }
        userUpdateValues.put(UserTable.COLUMN_USER_ID, jsonObject.getString("userId"));
        userUpdateValues.put(UserTable.COLUMN_SYNCHRONIZED, true);
        final String userSelection = UserTable.COLUMN_EMAIL.concat(" = ?");
        final String[] userSelectionArgs = {userEmail};
        contentResolver.update(DatabaseProvider.CONTENT_USER_URI, userUpdateValues, userSelection, userSelectionArgs);
      }
      contentResolver.notifyChange(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, null);
    } catch (JSONException e) {
      e.printStackTrace();
    }
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
    for (int i = 0; i < jsonArray.length(); i++) {
      final JSONObject transactionObject = (JSONObject) jsonArray.get(i);
      final Transaction transaction = new Transaction();
      transaction.setGroupId(groupId);
      transaction.setSynched(true);
      transaction.setAmount(transactionObject.getDouble("amount"));
      transaction.setInfoName(transactionObject.getString("infoName"));

      final JSONObject infoLocation = (JSONObject) transactionObject.getJSONObject("infoLocation");
      final Location location = new Location("");
      try {
        location.setLatitude(infoLocation.getDouble("latitude"));
        location.setLongitude(infoLocation.getDouble("longitude"));
      } catch (JSONException e) {
        e.printStackTrace();
      }
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
      Log.d(TAG, "Saving Transaction: " + transaction.toString());
      saveTransaction(transaction);
    }
  }

  public void saveTransaction(Transaction transaction) {
    final ContentValues transactionContent = transaction.toContentValues();
    final Uri id = contentResolver.insert(DatabaseProvider.CONTENT_TRANSACTION_URI, transactionContent);
    if (id != null) {
      ContentValues transactionGroupContent = new ContentValues();
      final int transactionId = Integer.parseInt(id.getLastPathSegment());
      transaction.setId(transactionId);
      transactionGroupContent.put(GroupTransactionTable.COLUMN_TRANSACTION_ID, transactionId);
      transactionGroupContent.put(GroupTransactionTable.COLUMN_GROUP_ID, transaction.getGroupId());
      contentResolver.insert(DatabaseProvider.CONTENT_GROUP_TRANSACTION_URI, transactionGroupContent);
    }
    contentResolver.notifyChange(DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, null);
  }

  public long getGroupsByUserId(String userId) {
    final String[] projection = {UserTable.COLUMN_ID};
    final String selection = UserTable.COLUMN_USER_ID.concat(" = ?");
    final String[] selectionArgs = {Login.getInstance().getUser().getUserId()};
    final Cursor userCursor = contentResolver.query(DatabaseProvider.CONTENT_USER_URI, projection, selection, selectionArgs, null);
    if (1 == userCursor.getCount()) {
      userCursor.moveToNext();
      return userCursor.getLong(0);
    }
    return -1;
  }

  public String getUserNameById(String userId) {
    final String[] projection = {UserTable.COLUMN_USERNAME};
    final String selection = UserTable.COLUMN_USER_ID + " = ?";
    final String[] selectionArgs = {userId};
    final Cursor query = contentResolver.query(DatabaseProvider.CONTENT_USER_URI, projection, selection, selectionArgs, null);
    if (query.getCount() == 1 && query.moveToFirst()) {
      return query.getString(query.getColumnIndex(UserTable.COLUMN_USERNAME));
    }
    return null;
  }

  public List<Transaction> getUnsyncedTransactions(String userId) {
    final List<Transaction> result = new ArrayList<>();

    final String[] projection = {TransactionTable.TABLE_NAME + ".*", GroupTable.TABLE_NAME+"."+GroupTable.COLUMN_GROUP_ID};
    final String selection = UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ? AND"
        + " " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_SYNCHRONIZED + " = 1 AND"
        + " " + TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_SYNCHRONIZED + " = 0 ";
    final String[] selectionArgs = {userId};
    final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_TRANSACTION_JOIN_URI, projection, selection, selectionArgs, null);
    Log.d(TAG, "Cursor: Count: " + cursor.getCount());

    while (cursor.moveToNext()) {
      final Transaction transaction = new Transaction();
      transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_ID)));
      transaction.setInfoName(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_NAME)));
      transaction.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_IMAGE_URL)));
      transaction.setDateTime(Helper.ParseDateString(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_CREATED_AT))));
      transaction.setPaidBy(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_PAID_BY)));
      transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_AMOUNT)));
      transaction.setSplit(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_SPLIT)));
      transaction.setGroupId(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_GROUP_ID)));
      result.add(transaction);
    }

    return result;
  }


  public List<Group> getUnsyncedGroups(String userId) {
    final List<Group> result = new ArrayList<>();

    final String[] projection = {GroupTable.TABLE_NAME + ".*", UserTable.TABLE_NAME + "." + UserTable.COLUMN_EMAIL};
    final String selection = "(" + UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " = ?  OR"
        + " " + UserTable.TABLE_NAME + "." + UserTable.COLUMN_USER_ID + " IS NULL ) AND"
        + " " + GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_SYNCHRONIZED + " = 0 ";
    final String[] selectionArgs = {userId};
    final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs, null);
    Log.d(TAG, "Cursor: Count: " + cursor.getCount());
    long oldGroupId = -1;
    Group group = null;

    while (cursor.moveToNext()) {
      long possibleNewGroupId = cursor.getLong(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_ID));
      if (oldGroupId != possibleNewGroupId) {
        oldGroupId = possibleNewGroupId;
        group = new Group();
        group.setId(possibleNewGroupId);
        group.setName(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_NAME)));
        group.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_IMAGE_URL)));
        group.setUsers(new ArrayList<User>());
        group.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_CREATED_AT)));
        int synced = cursor.getInt(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_SYNCHRONIZED));
        group.setSync(synced > 0);
        result.add(group);
      }
      final User user = new User();
      user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_EMAIL)));
      group.getUsers().add(user);
    }

    return result;
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

  public String getLatestTransactionPubDateByGroupId(String groupId) {
    String result = null;
    final String[] projection = {TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_PUBLISHED_AT};
    final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_GROUP_ID + " = ?";
    final String[] selectionArgs = {groupId};
    final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_TRANSACTION_JOIN_URI,
        projection, selection, selectionArgs,
        TransactionTable.COLUMN_PUBLISHED_AT + " DESC LIMIT 1");
    Log.d(TAG, "LatestTransaction Count: " + cursor.getCount());
    if (cursor.moveToFirst()) {
      result = cursor.getString(0);
    }
    return result;
  }

  public List<Group> removeExistingGroupIds(JSONArray groupIds) {
    List<Group> existingGroups = new ArrayList<>();
    for (int i = groupIds.length() - 1; i >= 0; i--) {
      final Group group = new Group();
      final String[] projection = {GroupTable.TABLE_NAME + ".*"};
      final String selection = GroupTable.COLUMN_GROUP_ID + " = ?";
      String groupId = null;
      try {
        groupId = (String) groupIds.get(i);
      } catch (JSONException e) {
        e.printStackTrace();
      }
      final String[] selectionArgs = {groupId};
      final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_URI, projection, selection, selectionArgs, null);
      group.setId(-1);
      if (cursor.moveToFirst()) {
        group.setId(cursor.getLong(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_ID)));
        group.setGroupId(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_GROUP_ID)));
        group.setName(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_NAME)));
        group.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_IMAGE_URL)));
        group.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_CREATED_AT)));
        int synced = cursor.getInt(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_SYNCHRONIZED));
        group.setSync(synced > 0);
      }
      if (group.getId() != -1) {
        Log.d(TAG, "Removing groupId: " + group);
        existingGroups.add(group);
        groupIds.remove(i);
      }
    }
    return existingGroups;
  }
}
