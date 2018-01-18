package com.media.interactive.cs3.hdm.interactivemedia.data.split;

import com.google.android.gms.maps.model.LatLng;
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

public class EvenSplitTest {
    private Split split;
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User payingUser;
    private Group testGroup;
    private Transaction transaction;

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
        transaction = new Transaction("TestTransaction", payingUser.getUserId(), "even",
                new Date(System.currentTimeMillis()), new LatLng(0.0, 0.0), 0.0, testGroup);
    }

    @Test
    public void split_validInput_returnsOneDebtPerUserInGroupExceptPayer() {
        final List<Debt> debts = split.split(transaction);
        assertNotNull(debts);
        assertEquals(testGroup.getUsers().size() - 1, debts.size());
    }

    @Test
    public void split_validInput_returnsDebtsWithPayingUserAsCreditor() {
        final List<Debt> debts = split.split(transaction);
        for (Debt debt : debts) {
            assertEquals(payingUser.getId(), debt.getCreditorId());
        }
    }

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

    @Test
    public void split_validInput_returnsDebtsWithAmountOfAThirdOfTransactionAmount() {
        final double expectedAmount = 16.32;
        transaction.setAmount(expectedAmount * transaction.getGroup().getUsers().size());
        final List<Debt> debts = split.split(transaction);
        for (Debt debt : debts) {
            assertEquals(expectedAmount, debt.getAmount(), 0.0000000001);
        }
    }

    @Test(expected = TerminatingSplitChainedException.class)
    public void andThen_newSplit_throwsException() throws Exception {
        split.andThen(new EvenSplit());
    }

    @Test
    public void isTerminating_newSplit_returnsTrue() throws Exception {
        assertTrue(split.isTerminating());
    }

}