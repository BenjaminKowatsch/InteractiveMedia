package com.media.interactive.cs3.hdm.interactivemedia.data.split;

import android.location.Location;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;



/**
 * The Class EvenSplitTest.
 */
public class EvenSplitTest {

    /**
     * The split.
     */
    private Split split;

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
        split = new EvenSplit();
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
            new Date(System.currentTimeMillis()), new Location(""), 0.0, testGroup);
    }

    /**
     * Split valid input returns one debt per user in group except payer.
     */
    @Test
    public void split_validInput_returnsOneDebtPerUserInGroupExceptPayer() {
        final List<Debt> debts = split.split(transaction);
        assertNotNull(debts);
        assertEquals(testGroup.getUsers().size() - 1, debts.size());
    }

    /**
     * Split valid input returns debts with paying user as creditor.
     */
    @Test
    public void split_validInput_returnsDebtsWithPayingUserAsCreditor() {
        final List<Debt> debts = split.split(transaction);
        for (Debt debt : debts) {
            assertEquals(payingUser.getId(), debt.getCreditorId());
        }
    }

    /**
     * Split valid input returns debts targeting each not paying user.
     */
    @Test
    public void split_validInput_returnsDebtsTargetingEachNotPayingUser() {
        final List<Debt> debts = split.split(transaction);
        final Set<Long> expectedDebtors = new HashSet<>();
        expectedDebtors.add(testUser1.getId());
        expectedDebtors.add(testUser2.getId());
        expectedDebtors.add(testUser3.getId());
        final Set<Long> foundDebtors = new HashSet<>();
        for (Debt debt : debts) {
            foundDebtors.add(debt.getDebtorId());
        }
        assertEquals(expectedDebtors, foundDebtors);
    }

    /**
     * Split valid input returns debts with amount of A third of transaction amount.
     */
    @Test
    public void split_validInput_returnsDebtsWithAmountOfAThirdOfTransactionAmount() {
        final double expectedAmount = 16.32;
        transaction.setAmount(expectedAmount * transaction.getGroup().getUsers().size());
        final List<Debt> debts = split.split(transaction);
        for (Debt debt : debts) {
            assertEquals(expectedAmount, debt.getAmount(), 0.0000000001);
        }
    }

    /**
     * And then new split throws exception.
     *
     * @throws Exception the exception
     */
    @Test(expected = TerminatingSplitChainedException.class)
    public void andThen_newSplit_throwsException() throws Exception {
        split.andThen(new EvenSplit());
    }

    /**
     * Checks if is terminating new split any param returns true.
     *
     * @throws Exception the exception
     */
    @Test
    public void isTerminating_newSplitAnyParam_returnsTrue() throws Exception {
        assertTrue(split.isTerminating(null));
    }

}