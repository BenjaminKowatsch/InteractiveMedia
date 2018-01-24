package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.SplitTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



/**
 * The Class ConstantDeduction.
 */
public class ConstantDeduction implements Split {

    /**
     * The amount.
     */
    private final double amount;
    /**
     * The to user id.
     */
    private final String toUserId;
    /**
     * The next.
     */
    private Split next = null;

    /**
     * Instantiates a new constant deduction.
     *
     * @param amount   the amount
     * @param toUserId the to user id
     */
    public ConstantDeduction(double amount, String toUserId) {
        this.amount = amount;
        this.toUserId = toUserId;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#split(com.media.interactive.cs3.hdm.interactivemedia.data.Transaction)
     */
    @Override
    public List<Debt> split(Transaction transaction) {
        if (transaction.getAmount() < this.amount) {
            throw new IllegalArgumentException("Cannot deduce more than its amount from " + transaction.getInfoName());
        } else {
            final Group group = transaction.getGroup();
            final String paidByUserId = transaction.getPaidBy();
            final SplittingParties splittingParties = SplittingParties.extractFromGroup(group, paidByUserId);
            final User toUser = getUserWithIdIn(splittingParties.getPaidFor(), toUserId);
            if (toUser == null) {
                if (toUserId.equals(splittingParties.getPaidBy().getUserId())) {
                    if (hasNext()) {
                        return next.split(getRemainingFrom(transaction));
                    } else {
                        return new ArrayList<>();
                    }
                } else {
                    throw new IllegalArgumentException("The given transaction's group does not contain "
                       + "this split's toUserId");
                }
            } else {
                final List<Debt> out = new ArrayList<>();
                if (amount > 0.0) {
                    out.add(new Debt(splittingParties.getPaidBy(), toUser, amount, transaction));
                }
                if (hasNext()) {
                    Transaction remaining = getRemainingFrom(transaction);
                    out.addAll(next.split(remaining));
                }
                return out;
            }
        }
    }

    /**
     * Gets the remaining from.
     *
     * @param transaction the transaction
     * @return the remaining from
     */
    @NonNull
    private Transaction getRemainingFrom(Transaction transaction) {
        final Transaction remaining = new Transaction(transaction);
        remaining.setAmount(transaction.getAmount() - this.amount);
        return remaining;
    }

    /**
     * Gets the user with id in.
     *
     * @param users the users
     * @param id    the id
     * @return the user with id in
     */
    private User getUserWithIdIn(List<User> users, String id) {
        for (User user : users) {
            if (user.getUserId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#andThen(com.media.interactive.cs3.hdm.interactivemedia.data.split.Split)
     */
    @NonNull
    @Override
    public Split andThen(Split next) {
        if (!hasNext()) {
            this.next = next;
            return next;
        } else {
            throw new SplitAlreadyChainedException("This split already had " + next + " as next in chain.");
        }
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#isTerminating(
     * com.media.interactive.cs3.hdm.interactivemedia.data.Transaction)
     */
    @Override
    public boolean isTerminating(Transaction transaction) {
        return this.amount >= transaction.getAmount();
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#hasNext()
     */
    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#getNext()
     */
    @Override
    public Split getNext() {
        if (hasNext()) {
            return next;
        } else {
            throw new LastSplitInChainException();
        }
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#toJson()
     */
    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("type", "constant deduction");
        result.put("amount", amount);
        result.put("userId", toUserId);
        return result;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#toContentValues()
     */
    @Override
    public ContentValues toContentValues() {
        final ContentValues out = new ContentValues();
        out.put(SplitTable.COLUMN_TYPE, "constant deduction");
        out.put(SplitTable.COLUMN_AMOUNT, amount);
        out.put(SplitTable.COLUMN_USER_ID, toUserId);
        return out;
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
     * Gets the to user id.
     *
     * @return the to user id
     */
    public String getToUserId() {
        return toUserId;
    }
}
