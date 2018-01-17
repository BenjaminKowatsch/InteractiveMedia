package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.os.AsyncTask;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.Payment;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.Settlement;

import java.util.Date;
import java.util.List;


public class TransactionSplittingTask extends AsyncTask<Transaction, Void, Boolean> {
    private static final String TAG = TransactionSplittingTask.class.getSimpleName();

    private final DatabaseProviderHelper databaseProviderHelper;
    private final DatabaseHelper databaseHelper;

    private final Settlement settlementMethod;

    public TransactionSplittingTask(DatabaseProviderHelper databaseProviderHelper, DatabaseHelper databaseHelper, Settlement settlementMethod) {
        super();
        this.databaseProviderHelper = databaseProviderHelper;
        this.databaseHelper = databaseHelper;
        this.settlementMethod = settlementMethod;
    }

    @Override
    protected Boolean doInBackground(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            List<Debt> debts;
            try {
                debts = transaction.split();
            } catch (Exception e) {
                Log.e(TAG, "An error occurred in resolving transaction " + transaction, e);
                return false;
            }
            if (isCancelled()) {
                return false;
            }
            for (Debt debt : debts) {
                try {
                    databaseProviderHelper.saveDebt(debt);
                } catch (Exception e) {
                    Log.e(TAG, "An error occurred in saving debt " + debt, e);
                    return false;
                }
            }
            List<Debt> allDebts;
            try {
                allDebts = databaseHelper.getAllDebts();
            } catch (Exception e) {
                Log.e(TAG, "An error occurred in loading all debts", e);
                return false;
            }
            List<Payment> payments;
            try {
                payments = settlementMethod.settle(allDebts);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred in settling all debts", e);
                return false;
            }
            final Date paymentGenerationTimestamp = new Date(System.currentTimeMillis());
            for (Payment payment : payments) {
                try {
                    databaseProviderHelper.savePayment(payment, paymentGenerationTimestamp, transaction.getGroup().getId());
                } catch (Exception e) {
                    Log.e(TAG, "An error occurred in saving payment " + payment, e);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "Successfully resolved transaction.");
        } else {
            Log.e(TAG, "Error on resolving transactions.");
        }
    }
}
