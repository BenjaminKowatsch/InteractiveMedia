package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;



/**
 * The Interface Split.
 */
public interface Split {

    /**
     * Splits a transaction into a List of Debts targeting the transactions' payer.
     *
     * @param transaction to be split into debts
     * @return list of debts
     */
    List<Debt> split(Transaction transaction);

    /**
     * Adds a Split to be used after resolving this one.
     *
     * @param next split to be changed
     * @return the split chained to this
     * @throws TerminatingSplitChainedException if this split already reduced the remaining
     *                                          amount of the transaction to 0
     */
    @NonNull
    Split andThen(Split next);

    /**
     * Indicates, whether no more split can be chained after this one using {@link #andThen(Split)}
     * to further split the given transaction.
     *
     * @param transaction transaction for which split may be terminating
     * @return false when another split can be chained or true if none can come after this
     */
    boolean isTerminating(Transaction transaction);


    /**
     * Indicates, whether this split is followed by atleast one other split. Is not opposite to
     * {@link #isTerminating(Transaction)}, as that method only indicates whether another split
     * could be chained after this but not that none is chained.
     *
     * @return true if another split follows this in the chain, otherwise false
     */
    boolean hasNext();

    /**
     * Returns next split in split chain.
     *
     * @return split following this directly in split chain.
     */
    Split getNext();

    /**
     * To json.
     *
     * @return the JSON object
     * @throws JSONException the JSON exception
     */
    JSONObject toJson() throws JSONException;

    /**
     * To content values.
     *
     * @return the content values
     */
    ContentValues toContentValues();
}
