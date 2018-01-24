package com.media.interactive.cs3.hdm.interactivemedia.data.split;

import android.content.ContentValues;
import android.location.Location;
import android.support.annotation.NonNull;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.media.interactive.cs3.hdm.interactivemedia.data.settlement.PairBasedSettlementTest.DELTA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



/**
 * The Class ConstantDeductionTest.
 */
public class ConstantDeductionTest {

    /**
     * The test user 1.
     */
    private User testUser1;

    /**
     * The test user 2.
     */
    private User testUser2;

    /**
     * The test user 3.
     */
    private User testUser3;

    /**
     * The paying user.
     */
    private User payingUser;

    /**
     * The test group.
     */
    private Group testGroup;

    /**
     * The transaction.
     */
    private Transaction transaction;

    /**
     * Inits the.
     *
     * @throws Exception the exception
     */
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
        transaction = new Transaction("TestTransaction", payingUser.getUserId(), new EvenSplit(),
            new Date(System.currentTimeMillis()), new Location(""), 8.0, testGroup);
    }

    /**
     * Split transaction with lower amount than split throws exception.
     *
     * @throws Exception the exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void split_transactionWithLowerAmountThanSplit_throwsException() throws Exception {
        Split split = new ConstantDeduction(transaction.getAmount() + 10, testUser1.getUserId());
        split.split(transaction);
    }

    /**
     * Split transaction without referenced user throws exception.
     *
     * @throws Exception the exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void split_transactionWithoutReferencedUser_throwsException() throws Exception {
        Split split = new ConstantDeduction(1.0, "userIdNotInTestGroup");
        split.split(transaction);
    }

    /**
     * Split amount zero returns empty list.
     *
     * @throws Exception the exception
     */
    @Test
    public void split_amountZero_returnsEmptyList() throws Exception {
        Split split = new ConstantDeduction(0.0, testUser1.getUserId());
        final List<Debt> debts = split.split(transaction);
        assertTrue(debts.isEmpty());
    }

    /**
     * Split amount lower transaction amount returns one debt.
     *
     * @throws Exception the exception
     */
    @Test
    public void split_amountLowerTransactionAmount_returnsOneDebt() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        final List<Debt> debts = split.split(transaction);
        assertEquals(1, debts.size());
    }

    /**
     * Split amount lower transaction amount returns correct amount and users debt.
     *
     * @throws Exception the exception
     */
    @Test
    public void split_amountLowerTransactionAmount_returnsCorrectAmountAndUsersDebt() throws Exception {
        final double splitAmount = 1.0;
        Split split = new ConstantDeduction(splitAmount, testUser1.getUserId());
        final Debt debt = split.split(transaction).get(0);
        assertEquals(splitAmount, debt.getAmount(), DELTA);
        assertEquals(debt.getCreditorId(), payingUser.getId());
        assertEquals(debt.getDebtorId(), testUser1.getId());
    }

    /**
     * Split chained with other split calls other split with remaining amount.
     *
     * @throws Exception the exception
     */
    @Test
    public void split_chainedWithOtherSplit_callsOtherSplitWithRemainingAmount() throws Exception {
        final double splitAmount = 1.0;
        Split split = new ConstantDeduction(splitAmount, testUser1.getUserId());
        final NotSplittingMock next = new NotSplittingMock();
        split.andThen(next);
        split.split(transaction);
        assertEquals(transaction.getAmount() - splitAmount, next.toSplit.getAmount(), DELTA);
    }

    /**
     * And then called once is executed.
     *
     * @throws Exception the exception
     */
    @Test
    public void andThen_calledOnce_isExecuted() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        split.andThen(new EvenSplit());
    }

    /**
     * And then called twice throws exception.
     *
     * @throws Exception the exception
     */
    @Test(expected = SplitAlreadyChainedException.class)
    public void andThen_calledTwice_throwsException() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        split.andThen(new EvenSplit());
        split.andThen(new EvenSplit());
    }

    /**
     * And then valid input returns next.
     */
    @Test
    public void andThen_validInput_returnsNext() {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        final EvenSplit next = new EvenSplit();
        final Split returned = split.andThen(next);
        assertEquals(next, returned);
    }

    /**
     * Checks if is terminating split amount equals transaction amount returns true.
     *
     * @throws Exception the exception
     */
    @Test
    public void isTerminating_splitAmountEqualsTransactionAmount_returnsTrue() throws Exception {
        Split split = new ConstantDeduction(transaction.getAmount(), testUser1.getUserId());
        final boolean terminating = split.isTerminating(transaction);
        assertTrue(terminating);
    }

    /**
     * Checks if is terminating split amount smaller than transaction amount returns false.
     *
     * @throws Exception the exception
     */
    @Test
    public void isTerminating_splitAmountSmallerThanTransactionAmount_returnsFalse() throws Exception {
        Split split = new ConstantDeduction(1.0, testUser1.getUserId());
        final boolean terminating = split.isTerminating(transaction);
        assertFalse(terminating);
    }

    /**
     * The Class NotSplittingMock.
     */
    private final static class NotSplittingMock implements Split {

        /**
         * The to split.
         */
        public Transaction toSplit;

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#split(com.media.interactive.cs3.hdm.interactivemedia.data.Transaction)
         */
        @Override
        public List<Debt> split(Transaction transaction) {
            toSplit = transaction;
            return new ArrayList<>();
        }

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#andThen(com.media.interactive.cs3.hdm.interactivemedia.data.split.Split)
         */
        @NonNull
        @Override
        public Split andThen(Split next) {
            return this;
        }

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#isTerminating(com.media.interactive.cs3.hdm.interactivemedia.data.Transaction)
         */
        @Override
        public boolean isTerminating(Transaction transaction) {
            return false;
        }

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#hasNext()
         */
        @Override
        public boolean hasNext() {
            return false;
        }

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#getNext()
         */
        @Override
        public Split getNext() {
            return null;
        }

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#toJson()
         */
        @Override
        public JSONObject toJson() throws JSONException {
            return null;
        }

        /* (non-Javadoc)
         * @see com.media.interactive.cs3.hdm.interactivemedia.data.split.Split#toContentValues()
         */
        @Override
        public ContentValues toContentValues() {
            return null;
        }
    }
}