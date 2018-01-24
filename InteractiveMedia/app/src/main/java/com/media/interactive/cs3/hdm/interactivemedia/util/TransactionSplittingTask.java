package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.os.AsyncTask;
import android.util.Log;

import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.Payment;
import com.media.interactive.cs3.hdm.interactivemedia.data.settlement.Settlement;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




/**
 * The Class TransactionSplittingTask.
 */
public class TransactionSplittingTask extends AsyncTask<Transaction, Void, Boolean> {

    /**
     * The Constant TAG.
     */
    private static final String TAG = TransactionSplittingTask.class.getSimpleName();

    /**
     * The helper.
     */
    private final DatabaseProviderHelper helper;

    /**
     * The settlement method.
     */
    private final Settlement settlementMethod;

    /**
     * Instantiates a new transaction splitting task.
     *
     * @param helper           the helper
     * @param settlementMethod the settlement method
     */
    public TransactionSplittingTask(DatabaseProviderHelper helper, Settlement settlementMethod) {
        super();
        this.helper = helper;
        this.settlementMethod = settlementMethod;
    }

    /* (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
     */
    @Override
    protected Boolean doInBackground(Transaction... transactions) {
        Set<Group> groupsInTransactions = new HashSet<>();
        for (Transaction transaction : transactions) {
            try {
                helper.completeTransaction(transaction);
            } catch (Exception e) {
                Log.e(TAG, "An error occured in completing transaction " + transaction, e);
            }
            if (transaction.getGroup().getUsers().size() > 1) {
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
                        helper.saveDebt(debt);
                    } catch (Exception e) {
                        Log.e(TAG, "An error occurred in saving debt " + debt, e);
                        return false;
                    }
                }
                groupsInTransactions.add(transaction.getGroup());
            }
        }
        Log.d(TAG, "Groups in transaction: " + groupsInTransactions);
        for (Group group : groupsInTransactions) {
            List<Debt> allDebts;
            try {
                allDebts = helper.getAllDebtsForGroup(group.getGroupId());
            } catch (Exception e) {
                Log.e(TAG, "An error occurred in loading all debts", e);
                return false;
            }
            Log.d(TAG, "Loaded " + allDebts.size() + " debts for group " + group);
            List<Payment> payments;
            try {
                payments = settlementMethod.settle(allDebts);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred in settling all debts", e);
                return false;
            }
            Log.d(TAG, "Created " + payments.size() + " payments for debts of group " + group);
            final Date paymentGenerationTimestamp = new Date(System.currentTimeMillis());
            for (Payment payment : payments) {
                try {
                    helper.savePayment(payment, paymentGenerationTimestamp, group.getId());
                } catch (Exception e) {
                    Log.e(TAG, "An error occurred in saving payment " + payment, e);
                    return false;
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "Successfully resolved transaction.");
        } else {
            Log.d(TAG, "Error on resolving transactions.");
        }
    }
}
