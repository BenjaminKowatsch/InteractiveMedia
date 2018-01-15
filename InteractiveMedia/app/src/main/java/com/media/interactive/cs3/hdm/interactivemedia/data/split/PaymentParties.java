package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class PaymentParties {
    private final User paidBy;
    private final List<User> paidFor;

    private PaymentParties(User paidBy, List<User> paidFor) {
        this.paidBy = paidBy;
        this.paidFor = paidFor;
    }

    public static PaymentParties extractFromGroup(Group group, String paidByUserId) {
        final Iterator<User> iterator = group.getUsers().iterator();
        User paidBy = null;
        List<User> paidFor = new ArrayList<>();
        while (iterator.hasNext()) {
            final User current = iterator.next();
            if (current.getUserId().equals(paidByUserId)) {
                paidBy = current;
            } else {
                paidFor.add(current);
            }
        }
        return new PaymentParties(paidBy, paidFor);
    }

    public User getPaidBy() {
        return paidBy;
    }

    public List<User> getPaidFor() {
        return paidFor;
    }
}
