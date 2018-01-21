package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.SplitTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConstantDeduction implements Split {
    private Split next = null;
    private final double amount;
    private final String toUserId;

    public ConstantDeduction(double amount, String toUserId) {
        this.amount = amount;
        this.toUserId = toUserId;
    }

    @Override
    public List<Debt> split(Transaction transaction) {
        if (transaction.getAmount() < this.amount) {
            throw new IllegalArgumentException("Cannot deduce more than its amount from " + transaction.getInfoName());
        } else {
            final Group group = transaction.getGroup();
            String paidByUserId = transaction.getPaidBy();
            SplittingParties splittingParties = SplittingParties.extractFromGroup(group, paidByUserId);
            User toUser = getUserWithIdIn(splittingParties.getPaidFor(), toUserId);
            if (toUser == null) {
                if (toUserId.equals(splittingParties.getPaidBy().getUserId())) {
                    if (hasNext()) {
                        return next.split(getRemainingFrom(transaction));
                    } else {
                        return new ArrayList<>();
                    }
                } else {
                    throw new IllegalArgumentException("The given transaction's group does not contain " +
                            "this split's toUserId");
                }
            } else {
                List<Debt> out = new ArrayList<>();
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

    @NonNull
    private Transaction getRemainingFrom(Transaction transaction) {
        Transaction remaining = new Transaction(transaction);
        remaining.setAmount(transaction.getAmount() - this.amount);
        return remaining;
    }

    private User getUserWithIdIn(List<User> users, String id) {
        for (User user : users) {
            if (user.getUserId().equals(id)) {
                return user;
            }
        }
        return null;
    }

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

    @Override
    public boolean isTerminating(Transaction transaction) {
        return this.amount >= transaction.getAmount();
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public Split getNext() {
        if (hasNext()) {
            return next;
        } else {
            throw new LastSplitInChainException();
        }
    }

    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("type", "constand deduction");
        result.put("amount", amount);
        result.put("userId", toUserId);
        return result;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues out = new ContentValues();
        out.put(SplitTable.COLUMN_TYPE, "constant deduction");
        out.put(SplitTable.COLUMN_AMOUNT, amount);
        out.put(SplitTable.COLUMN_USER_ID, toUserId);
        return out;
    }


    public double getAmount() {
        return amount;
    }

    public String getToUserId() {
        return toUserId;
    }
}
