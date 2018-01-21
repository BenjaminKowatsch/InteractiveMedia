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

public class EvenSplit implements Split {
    @Override
    public List<Debt> split(Transaction transaction) {
        String paidByUserId = transaction.getPaidBy();
        Group group = transaction.getGroup();
        SplittingParties splittingParties = SplittingParties.extractFromGroup(group, paidByUserId);
        validateForGroup(splittingParties, group);
        double amountPerUser = transaction.getAmount() / group.getUsers().size();
        List<Debt> out = new ArrayList<>();
        for (User paidFor : splittingParties.getPaidFor()) {
            out.add(new Debt(splittingParties.getPaidBy(), paidFor, amountPerUser, transaction));
        }
        return out;
    }

    private void validateForGroup(SplittingParties splittingParties, Group group) {
        if (splittingParties.getPaidBy() == null) {
            throw new IllegalStateException("payedBy user could not be found in group " + group);
        } else if (splittingParties.getPaidFor().isEmpty()) {
            throw new IllegalStateException("No user to pay for was found in group");
        } else if (splittingParties.getPaidFor().size() != group.getUsers().size() - 1) {
            throw new IllegalStateException("paidFor user count does not match group size - payer");
        }
    }

    @NonNull
    @Override
    public Split andThen(Split next) {
        throw new TerminatingSplitChainedException(this.getClass().getName() + " is always terminating");
    }

    @Override
    public boolean isTerminating(Transaction transaction) {
        return true;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Split getNext() {
        throw new LastSplitInChainException();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("type", "even");
        return result;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues out = new ContentValues();
        out.put(SplitTable.COLUMN_TYPE, "even");
        return out;
    }

}
