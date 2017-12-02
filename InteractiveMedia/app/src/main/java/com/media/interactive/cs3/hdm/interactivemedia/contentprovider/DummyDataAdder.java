package com.media.interactive.cs3.hdm.interactivemedia.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;

/**
 * Created by Pirmin Rehm on 02.12.2017.
 */

public class DummyDataAdder {

    private Context context;
    ContentResolver dummyContentResolver;


    public DummyDataAdder(Context context) {
        this.context = context;
        dummyContentResolver = context.getContentResolver();
    }

    private void addSingleTransaction(int counter) {

        ContentValues dummyContentValues = new ContentValues();
        dummyContentValues.put(TransactionTable.COLUMN_AMOUNT, ((counter*1494)%1111) );
        dummyContentValues.put(TransactionTable.COLUMN_PAID_BY, 1);
        dummyContentValues.put(TransactionTable.COLUMN_INFO_NAME, "Transaction " + counter);
        dummyContentValues.put(TransactionTable.COLUMN_INFO_LOCATION, "Ghetto Netto");
        dummyContentResolver.insert(DatabaseProvider.CONTENT_TRANSACTION_URI, dummyContentValues);
    }

    private void addSingleGroup(int counter) {
        ContentValues dummyContentValues = new ContentValues();
        dummyContentValues.put(GroupTable.COLUMN_NAME, "Gruppe " + counter);
        dummyContentResolver.insert(DatabaseProvider.CONTENT_GROUP_URI, dummyContentValues);
    }

    public void addTransactions(int nrOfTransactions) {
        for (int i=0; i<nrOfTransactions; i++ ) {
            addSingleTransaction(i);
        }
    }

    public void addGroups(int nrOfGroups) {
        for (int i=0; i<nrOfGroups; i++ ) {
            addSingleGroup(i);
        }
    }

}
