package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import java.util.List;

public interface Split {

    /**
     * Splits a transaction into a List of Debts targeting the transactions' payer
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
     * amount of the transaction to 0
     */
    Split andThen(Split next);

    /**
     * Indicates, whether no more split can be chained after this one using {@link #andThen(Split)}
     * to further split the given transaction
     *
     * @param transaction transaction for which split may be terminating
     * @return false when another split can be chained or true if none can come after this
     */
    boolean isTerminating(Transaction transaction);
}
