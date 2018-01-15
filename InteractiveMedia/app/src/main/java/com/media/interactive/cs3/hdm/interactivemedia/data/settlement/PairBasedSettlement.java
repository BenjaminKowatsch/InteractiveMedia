package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Settles debts by resolving all debts for a user-pair, ignoring cross-user simplification options
 */
class PairBasedSettlement implements Settlement {

    /**
     * Resolves debts by creating one payment for each user-pair (that is all combinations of first
     * user appearing as creditor and the second as debtor and vice versa)
     * If debts between users of a pair are resolved to zero no payment will be generated
     *
     * @param debts to resolve
     * @return one payment for each user-pair from the given debts
     */
    @Override
    public List<Payment> settle(List<Debt> debts) {
        List<Payment> out = new ArrayList<>();
        final Map<UserPair, List<Debt>> uniquePairsWithDebts = findUniquePairsWithDebts(debts);
        for (List<Debt> onePairsDebts : uniquePairsWithDebts.values()) {
            final Payment calculated = calculatePayment(onePairsDebts);
            if (calculated != null) {
                out.add(calculated);
            }
        }
        return out;
    }

    private Payment calculatePayment(List<Debt> onePairsDebts) {
        double currentAmount = 0;
        User initialCreditor = onePairsDebts.get(0).getCreditor();
        User initialDebtor = onePairsDebts.get(0).getDebtor();
        for (Debt debt : onePairsDebts) {
            if (debt.getCreditor().equals(initialCreditor)) {
                currentAmount += debt.getAmount();
            } else {
                currentAmount -= debt.getAmount();
            }
        }
        if (currentAmount > 0) {
            return new Payment(initialDebtor, initialCreditor, currentAmount);
        } else if (currentAmount < 0) {
            return new Payment(initialCreditor, initialDebtor, -currentAmount);
        } else {
            return null;
        }
    }

    private Map<UserPair, List<Debt>> findUniquePairsWithDebts(List<Debt> debts) {
        Map<UserPair, List<Debt>> debtsPerPair = new HashMap<>();
        for (Debt debt : debts) {
            UserPair fromDebt = new UserPair(debt.getCreditor(), debt.getDebtor());
            getOrInitialize(debtsPerPair, fromDebt).add(debt);
        }
        return debtsPerPair;
    }

    private List<Debt> getOrInitialize(Map<UserPair, List<Debt>> debtsPerPair, UserPair fromDebt) {
        if (!debtsPerPair.containsKey(fromDebt)) {
            debtsPerPair.put(fromDebt, new ArrayList<Debt>());
        }
        return debtsPerPair.get(fromDebt);
    }

    /**
     * Helper class that always puts users in alphabetical userId order into it so that two users
     * always build the same object for hashCode() and equals() calculation.
     */
    private final static class UserPair {
        private final User first;
        private final User second;

        private UserPair(User a, User b) {
            if (a.getUserId().compareTo(b.getUserId()) > 0) {
                this.first = a;
                this.second = b;
            } else {
                this.first = b;
                this.second = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserPair userPair = (UserPair) o;

            if (!first.equals(userPair.first)) return false;
            return second.equals(userPair.second);
        }

        @Override
        public int hashCode() {
            int result = first.hashCode();
            result = 31 * result + second.hashCode();
            return result;
        }
    }
}
