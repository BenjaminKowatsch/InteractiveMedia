package com.media.interactive.cs3.hdm.interactivemedia.data.split;

import android.location.Location;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.media.interactive.cs3.hdm.interactivemedia.data.settlement.PairBasedSettlementTest.DELTA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConstantDeductionTest {
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User payingUser;
    private Group testGroup;
    private Transaction transaction;

    @Before
    public void init() throws Exception {
        testUser1 = new User("User1", "mail1@mail.com", "userId1", "null", true);
        testUser2 = new User("User2", "mail2@mail.com", "userId2", "null", true);
        testUser3 = new User("User3", "mail3@mail.com", "userId3", "null", true);
        payingUser = new User("PayingUser", "mailPay@mail.com", "userIdPaying", "null", true);
        testGroup = new Group("TestGroup", "null", "testGroupId", "2016-10-17T03:00:00Z", true);
        testGroup.getUsers().add(testUser1);
        testGroup.getUsers().add(testUser2);
        testGroup.getUsers().add(testUser3);
        testGroup.getUsers().add(payingUser);
        transaction = new Transaction("TestTransaction", payingUser.getUserId(), "even",
                new Date(System.currentTimeMillis()), new Location(""), 8.0, testGroup);
    }

    @Test(expected = IllegalArgumentException.class)
    public void split_transactionWithLowerAmountThanSplit_throwsException() throws Exception {
        Split split = new ConstantDeduction(transaction.getAmount() + 10, testUser1.getUserId());
        split.split(transaction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void split_transactionWithoutReferencedUser_throwsException() throws Exception {
        Split split = new ConstantDeduction(1.0, "userIdNotInTestGroup");
        split.split(transaction);
    }

    @Test
    public void split_amountZero_returnsEmptyList() throws Exception {
        Split split = new ConstantDeduction(0.0, testUser1.getUserId());
        final List<Debt> debts = split.split(transaction);
        assertTrue(debts.isEmpty());
    }

    @Test
    public void split_amountLowerTransactionAmount_returnsOneDebt() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        final List<Debt> debts = split.split(transaction);
        assertEquals(1, debts.size());
    }

    @Test
    public void split_amountLowerTransactionAmount_returnsCorrectAmountAndUsersDebt() throws Exception {
        final double splitAmount = 1.0;
        Split split = new ConstantDeduction(splitAmount, testUser1.getUserId());
        final Debt debt = split.split(transaction).get(0);
        assertEquals(splitAmount, debt.getAmount(), DELTA);
        assertEquals(debt.getCreditorId(), payingUser.getId());
        assertEquals(debt.getDebtorId(), testUser1.getId());
    }

    @Test
    public void split_chainedWithOtherSplit_callsOtherSplitWithRemainingAmount() throws Exception {
        final double splitAmount = 1.0;
        Split split = new ConstantDeduction(splitAmount, testUser1.getUserId());
        final NotSplittingMock next = new NotSplittingMock();
        split.andThen(next);
        split.split(transaction);
        assertEquals(transaction.getAmount() - splitAmount, next.toSplit.getAmount(), DELTA);
    }

    @Test
    public void andThen_calledOnce_isExecuted() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        split.andThen(new EvenSplit());
    }

    @Test (expected = SplitAlreadyChainedException.class)
    public void andThen_calledTwice_throwsException() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        split.andThen(new EvenSplit());
        split.andThen(new EvenSplit());
    }

    @Test
    public void andThen_validInput_returnsNext() {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        final EvenSplit next = new EvenSplit();
        final Split returned = split.andThen(next);
        assertEquals(next, returned);
    }

    @Test
    public void isTerminating_splitAmountEqualsTransactionAmount_returnsTrue() throws Exception {
        Split split = new ConstantDeduction(transaction.getAmount(), testUser1.getUserId());
        final boolean terminating = split.isTerminating(transaction);
        assertTrue(terminating);
    }

    @Test
    public void isTerminating_splitAmountSmallerThanTransactionAmount_returnsFalse() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        final boolean terminating = split.isTerminating(transaction);
        assertFalse(terminating);
    }

    private final static class NotSplittingMock implements Split {
        public Transaction toSplit;

        @Override
        public List<Debt> split(Transaction transaction) {
            toSplit = transaction;
            return new ArrayList<>();
        }

        @Override
        public Split andThen(Split next) {
            return this;
        }

        @Override
        public boolean isTerminating(Transaction transaction) {
            return false;
        }
    }
}