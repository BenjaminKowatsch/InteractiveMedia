package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

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
            throw new IllegalArgumentException("Cannot deduce more than its amount from " + transaction);
        } else {
            final Group group = transaction.getGroup();
            String paidByUserId = transaction.getPaidBy();
            SplittingParties splittingParties = SplittingParties.extractFromGroup(group, paidByUserId);
            User toUser = getUserWithIdIn(splittingParties.getPaidFor(), toUserId);
            if (toUser == null) {
                throw new IllegalArgumentException("The given transaction's group does not contain " +
                        "this split's toUserId");
            } else {
                List<Debt> out = new ArrayList<>();
                out.add(new Debt(splittingParties.getPaidBy(), toUser, amount, transaction));
                if(next!=null) {
                    Transaction remaining = new Transaction(transaction);
                    remaining.setAmount(transaction.getAmount() - this.amount);
                    out.addAll(next.split(remaining));
                }
                return out;
            }
        }
    }

    private User getUserWithIdIn(List<User> users, String id) {
        for (User user : users) {
            if (user.getUserId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public Split andThen(Split next) {
        if (next != null) {
            this.next = next;
            return this;
        } else {
            throw new SplitAlreadyChainedException("This split already had " + next + " as next in chain.");
        }
    }

    @Override
    public boolean isTerminating(Transaction transaction) {
        return this.amount >= transaction.getAmount();
    }
}
