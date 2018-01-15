package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import java.util.ArrayList;
import java.util.List;

public class EvenSplit implements Split {
    @Override
    public List<Debt> split(Transaction transaction, String paidByUserId) {
        Group group = transaction.getGroup();
        PaymentParties paymentParties = PaymentParties.extractFromGroup(group, paidByUserId);
        validateForGroup(paymentParties, group);
        double amountPerUser = transaction.getAmount() / group.getUsers().size();
        List<Debt> out = new ArrayList<>();
        for (User paidFor : paymentParties.getPaidFor()) {
            out.add(new Debt(paymentParties.getPaidBy(), paidFor, amountPerUser));
        }
        return out;
    }

    private void validateForGroup(PaymentParties paymentParties, Group group) {
        if (paymentParties.getPaidBy() == null) {
            throw new IllegalStateException("payedBy user could not be found in group " + group);
        } else if (paymentParties.getPaidFor().isEmpty()) {
            throw new IllegalStateException("No user to pay for was found in group");
        } else if (paymentParties.getPaidFor().size() != group.getUsers().size() - 1) {
            throw new IllegalStateException("paidFor user count does not match group size - payer");
        }
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
