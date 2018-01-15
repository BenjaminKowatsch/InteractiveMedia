package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import java.util.List;

public interface Split {

    /**
     * Splits a transaction into a List of Debts targeting the transactions' payer
     *
     * @param transaction to be split into debts
     * @param paidBy user that paid for the transaction
     * @return list of debts
     */
    List<Debt> split(Transaction transaction, User paidBy);

    /**
     * Adds a Split to be used after resolving this one.
     *
     * @param next split to be changed
     * @return a split chaining this and next
     * @throws TerminatingSplitChainedException if this split already reduced the remaining
     * amount of the transaction to 0
     */
    Split andThen(Split next);

    /**
     * Indicates, whether no more split can be chained after this one using {@link #andThen(Split)}
     *
     * @return false when another split can be chained or true if none can come after this
     */
    boolean isTerminating();
}
