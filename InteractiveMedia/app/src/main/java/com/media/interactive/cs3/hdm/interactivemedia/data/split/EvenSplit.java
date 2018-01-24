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
 * The Class EvenSplit.
 */
public class EvenSplit implements Split {

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#split(com.media.interactive.cs3.hdm.interactivemedia.data.Transaction)
     */
    @Override
    public List<Debt> split(Transaction transaction) {
        final String paidByUserId = transaction.getPaidBy();
        final Group group = transaction.getGroup();
        final SplittingParties splittingParties = SplittingParties.extractFromGroup(group, paidByUserId);
        validateForGroup(splittingParties, group);
        final double amountPerUser = transaction.getAmount() / group.getUsers().size();
        final List<Debt> out = new ArrayList<>();
        for (User paidFor : splittingParties.getPaidFor()) {
            out.add(new Debt(splittingParties.getPaidBy(), paidFor, amountPerUser, transaction));
        }
        return out;
    }

    /**
     * Validate for group.
     *
     * @param splittingParties the splitting parties
     * @param group            the group
     */
    private void validateForGroup(SplittingParties splittingParties, Group group) {
        if (splittingParties.getPaidBy() == null) {
            throw new IllegalStateException("payedBy user could not be found in group " + group);
        } else if (splittingParties.getPaidFor().isEmpty()) {
            throw new IllegalStateException("No user to pay for was found in group");
        } else if (splittingParties.getPaidFor().size() != group.getUsers().size() - 1) {
            throw new IllegalStateException("paidFor user count does not match group size - payer");
        }
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#andThen(com.media.interactive.cs3.hdm.interactivemedia.data.split.Split)
     */
    @NonNull
    @Override
    public Split andThen(Split next) {
        throw new TerminatingSplitChainedException(this.getClass().getName() + " is always terminating");
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#isTerminating(com.media.interactive.cs3.hdm.interactivemedia.data.Transaction)
     */
    @Override
    public boolean isTerminating(Transaction transaction) {
        return true;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#hasNext()
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#getNext()
     */
    @Override
    public Split getNext() {
        throw new LastSplitInChainException();
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#toJson()
     */
    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("type", "even");
        return result;
    }

    /* (non-Javadoc)
     * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#toContentValues()
     */
    @Override
    public ContentValues toContentValues() {
        final ContentValues out = new ContentValues();
        out.put(SplitTable.COLUMN_TYPE, "even");
        return out;
    }

}
