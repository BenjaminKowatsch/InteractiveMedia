package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * The Class SplittingParties.
 */
class SplittingParties {

    /**
     * The paid by.
     */
    private final User paidBy;

    /**
     * The paid for.
     */
    private final List<User> paidFor;

    /**
     * Instantiates a new splitting parties.
     *
     * @param paidBy  the paid by
     * @param paidFor the paid for
     */
    private SplittingParties(User paidBy, List<User> paidFor) {
        this.paidBy = paidBy;
        this.paidFor = paidFor;
    }

    /**
     * Extract from group.
     *
     * @param group        the group
     * @param paidByUserId the paid by user id
     * @return the splitting parties
     */
    public static SplittingParties extractFromGroup(Group group, String paidByUserId) {
        final Iterator<User> iterator = group.getUsers().iterator();
        User paidBy = null;
        final List<User> paidFor = new ArrayList<>();
        while (iterator.hasNext()) {
            final User current = iterator.next();
            if (current.getUserId().equals(paidByUserId)) {
                paidBy = current;
            } else {
                paidFor.add(current);
            }
        }
        return new SplittingParties(paidBy, paidFor);
    }

    /**
     * Gets the paid by.
     *
     * @return the paid by
     */
    public User getPaidBy() {
        return paidBy;
    }

    /**
     * Gets the paid for.
     *
     * @return the paid for
     */
    public List<User> getPaidFor() {
        return paidFor;
    }
}
