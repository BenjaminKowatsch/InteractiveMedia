package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;


import android.content.ContentValues;

import com.media.interactive.cs3.hdm.interactivemedia.database.tables.PaymentTable;



/**
 * The Class Payment.
 */
public class Payment {

    /**
     * The from user id.
     */
    private final long fromUserId;

    /**
     * The to user id.
     */
    private final long toUserId;

    /**
     * The amount.
     */
    private final double amount;

    /**
     * Instantiates a new payment.
     *
     * @param from   the from
     * @param to     the to
     * @param amount the amount
     */
    public Payment(long from, long to, double amount) {
        this.fromUserId = from;
        this.toUserId = to;
        this.amount = amount;
    }

    /**
     * Gets the from user id.
     *
     * @return the from user id
     */
    public long getFromUserId() {
        return fromUserId;
    }

    /**
     * Gets the to user id.
     *
     * @return the to user id
     */
    public long getToUserId() {
        return toUserId;
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
     * To content values.
     *
     * @return the content values
     */
    public ContentValues toContentValues() {
        final ContentValues paymentContent = new ContentValues();
        paymentContent.put(PaymentTable.COLUMN_AMOUNT, getAmount());
        paymentContent.put(PaymentTable.COLUMN_FROM_USER, getFromUserId());
        paymentContent.put(PaymentTable.COLUMN_TO_USER, getToUserId());
        return paymentContent;
    }
}
