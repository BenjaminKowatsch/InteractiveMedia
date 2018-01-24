package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.content.ContentValues;

import com.media.interactive.cs3.hdm.interactivemedia.database.tables.DebtTable;



/**
 * The Class Debt.
 */
public class Debt {

    /**
     * The creditor id.
     */
    private final long creditorId;

    /**
     * The debtor id.
     */
    private final long debtorId;

    /**
     * The amount.
     */
    private final double amount;

    /**
     * The transaction id.
     */
    private final long transactionId;

    /**
     * Instantiates a new debt.
     *
     * @param creditor    the creditor
     * @param debtor      the debtor
     * @param amount      the amount
     * @param createdFrom the created from
     */
    public Debt(User creditor, User debtor, double amount, Transaction createdFrom) {
        this.creditorId = creditor.getId();
        this.debtorId = debtor.getId();
        this.amount = amount;
        this.transactionId = createdFrom.getId();
    }

    /**
     * Instantiates a new debt.
     *
     * @param creditorId    the creditor id
     * @param debtorId      the debtor id
     * @param amount        the amount
     * @param transactionId the transaction id
     */
    public Debt(long creditorId, long debtorId, double amount, long transactionId) {
        this.creditorId = creditorId;
        this.debtorId = debtorId;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    /**
     * Instantiates a new debt.
     *
     * @param creditor      the creditor
     * @param debtor        the debtor
     * @param amount        the amount
     * @param transactionId the transaction id
     */
    public Debt(User creditor, User debtor, double amount, long transactionId) {
        this.creditorId = creditor.getId();
        this.debtorId = debtor.getId();
        this.amount = amount;
        this.transactionId = transactionId;
    }

    /**
     * Gets the creditor id.
     *
     * @return the creditor id
     */
    public long getCreditorId() {
        return creditorId;
    }

    /**
     * Gets the debtor id.
     *
     * @return the debtor id
     */
    public long getDebtorId() {
        return debtorId;
    }

    /**
     * Gets the amount.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the transaction id.
     *
     * @return the transaction id
     */
    public long getTransactionId() {
        return transactionId;
    }


    /**
     * To content values.
     *
     * @return the content values
     */
    public ContentValues toContentValues() {
        final ContentValues debtContent = new ContentValues();
        debtContent.put(DebtTable.COLUMN_TO_USER, getCreditorId());
        debtContent.put(DebtTable.COLUMN_FROM_USER, getDebtorId());
        debtContent.put(DebtTable.COLUMN_AMOUNT, getAmount());
        debtContent.put(DebtTable.COLUMN_TRANSACTION_ID, getTransactionId());
        return debtContent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Debt{"
            + "creditorId=" + creditorId
            + ", debtorId=" + debtorId
            + ", amount=" + amount
            + ", transactionId=" + transactionId
            + '}';
    }
}
