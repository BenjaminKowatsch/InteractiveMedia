package com.media.interactive.cs3.hdm.interactivemedia.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.LoginTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.PaymentTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.PairBasedSettlement;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.Payment;
import com.media.interactive.cs3.hdm.interactivemedia.util.TransactionSplittingTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable.extractDebtFromCurrentPosition;
import static com.media.interactive.cs3.hdm.interactivemedia.util.Helper.formatDate;

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

    public void addTransactions(JSONArray jsonArray, Group group) throws JSONException {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject transactionObject = (JSONObject) jsonArray.get(i);
            final Transaction transaction = new Transaction();
            transaction.setGroup(group);
            transaction.setAmount(transactionObject.getDouble("amount"));
            transaction.setInfoName(transactionObject.getString("infoName"));

            final JSONObject infoLocation = transactionObject.getJSONObject("infoLocation");
            if (!infoLocation.isNull("latitude") && !infoLocation.isNull("longitude")) {
                final double lat = infoLocation.getDouble("latitude");
                final double lng = infoLocation.getDouble("longitude");
                final LatLng location = new LatLng(lat, lng);
                transaction.setLocation(location);
            }
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                transaction.setDateTime(sdf.parse(transactionObject.getString("infoCreatedAt")));
                transaction.setPublishedAt(sdf.parse(transactionObject.getString("publishedAt")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            transaction.setImageUrl(transactionObject.getString("infoImageUrl"));
            transaction.setPaidByUserId(transactionObject.getString("paidBy"));
            transaction.setSplit(transactionObject.getString("split"));
            Log.d(TAG, "Saving Transaction: " + transaction.toString());
            //using impl and then add them to task in batches to optimize resolving speed
            saveTransactionImpl(transaction);
            transactions.add(transaction);
        }
        calculateSplit(transactions.toArray(new Transaction[]{}));
    }

    public void saveTransaction(Transaction transaction) {
        saveTransactionImpl(transaction);
        calculateSplit(transaction);
    }

    private void saveTransactionImpl(Transaction transaction) {
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

    private void calculateSplit(Transaction... saved) {
        TransactionSplittingTask task = new TransactionSplittingTask(this, new PairBasedSettlement());
        task.execute(saved);
    }

    public void saveDebt(Debt debt) {
        final ContentValues debtContent = new ContentValues();
        debtContent.put(DebtTable.COLUMN_TRANSACTION_ID, debt.getTransactionId());
        debtContent.put(DebtTable.COLUMN_AMOUNT, debt.getAmount());
        debtContent.put(DebtTable.COLUMN_FROM_USER, debt.getDebtorId());
        debtContent.put(DebtTable.COLUMN_TO_USER, debt.getCreditorId());
        final Uri insert = contentResolver.insert(DatabaseProvider.CONTENT_DEBT_URI, debtContent);
        Log.d(TAG, "Inserted debt " + debt + " at " + insert);
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

    public void savePayment(Payment payment, Date creationTimestmap, long groupId) {
        final ContentValues paymentContent = new ContentValues();
        paymentContent.put(PaymentTable.COLUMN_AMOUNT, payment.getAmount());
        paymentContent.put(PaymentTable.COLUMN_FROM_USER, payment.getFromUserId());
        paymentContent.put(PaymentTable.COLUMN_TO_USER, payment.getToUserId());
        paymentContent.put(PaymentTable.COLUMN_CREATED_AT, formatDate(creationTimestmap));
        paymentContent.put(PaymentTable.COLUMN_GROUP_ID, groupId);
        final Uri insert = contentResolver.insert(DatabaseProvider.CONTENT_PAYMENT_URI, paymentContent);
        Log.d(TAG, "Inserted payment " + insert);
    }

    public void completeTransaction(Transaction transaction) {
        if (transaction.getGroup() == null) {
            transaction.setGroup(loadGroupForTransaction(transaction));
        }
        if (transaction.getGroup().getUsers().isEmpty()) {
            transaction.getGroup().getUsers().addAll(loadUsersForGroup(transaction.getGroup()));
        }
    }

    @NonNull
    private List<User> loadUsersForGroup(Group group) {
        final String[] projection = {UserTable.TABLE_NAME + ".*"};
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_ID + " = ?";
        final String[] selectionArgs = {"" + group.getId()};
        final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI,
                projection, selection, selectionArgs, null);
        if (cursor != null) {
            List<User> out = new ArrayList<>();
            while (cursor.moveToNext()) {
                out.add(UserTable.extractUserFromCurrentPosition(cursor));
            }
            return out;
        } else {
            return new ArrayList<>();
        }
    }

    private Group loadGroupForTransaction(Transaction transaction) {
        final String[] projection = {GroupTable.TABLE_NAME + ".*"};
        final String selection = TransactionTable.TABLE_NAME + "." + TransactionTable.COLUMN_ID + " = ?";
        final String[] selectionArgs = {"" + transaction.getId()};
        final Cursor cursor = contentResolver.query(DatabaseProvider.CONTENT_GROUP_TRANSACTION_JOIN_URI,
                projection, selection, selectionArgs, null);
        if (cursor != null) {
            final boolean hadFirst = cursor.moveToFirst();
            if (hadFirst) {
                return GroupTable.extractGroupFromCurrentPosition(cursor);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<Debt> getAllDebtsForGroup(String id) {
        final String[] projection = {DebtTable.COLUMN_ID, DebtTable.COLUMN_AMOUNT,
                DebtTable.COLUMN_FROM_USER, DebtTable.COLUMN_TO_USER,
                DebtTable.TABLE_NAME + "." + DebtTable.COLUMN_TRANSACTION_ID,
                GroupTransactionTable.TABLE_NAME + "." + GroupTransactionTable.COLUMN_GROUP_ID};
        final String selection = GroupTransactionTable.COLUMN_GROUP_ID + " = ?";
        final String[] selectionArgs = new String[]{id};
        final Cursor query = contentResolver.query(DatabaseProvider.CONTENT_GROUP_ID_DEBT_JOIN_URI, projection,
                selection, selectionArgs, null);
        List<Debt> out = new ArrayList<>();
        if (query != null) {
            while (query.moveToNext()) {
                out.add(extractDebtFromCurrentPosition(query));
            }
        } else {
            Log.e(TAG, "Query for getting all debts was null!");
        }
        return out;
    }

}
