package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import java.util.List;

public class EvenSplit implements Split {
    @Override
    public List<Debt> split(Transaction transaction) {

        return null;
    }

    @Override
    public Split andThen(Split next) {
        throw new TerminatingSplitChainedException(this.getClass().getName() + " is always terminating");
    }

    @Override
    public boolean isTerminating() {
        return true;
    }
}
