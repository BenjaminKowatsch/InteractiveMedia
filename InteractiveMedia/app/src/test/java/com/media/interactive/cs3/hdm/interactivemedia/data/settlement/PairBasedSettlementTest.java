package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;

import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;



/**
 * The Class PairBasedSettlementTest.
 */
public class PairBasedSettlementTest {

    /**
     * The Constant DELTA.
     */
    public static final double DELTA = 0.0000001;

    /**
     * The settlement.
     */
    private PairBasedSettlement settlement;

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
     * The debts.
     */
    private ArrayList<Debt> debts;

    /**
     * Inits the.
     */
    @Before
    public void init() {
        settlement = new PairBasedSettlement();
        debts = new ArrayList<>();
        testUser1 = new User("User1", "mail1@mail.com", "userId1", "null", true);
        testUser1.setId(1);
        testUser2 = new User("User2", "mail2@mail.com", "userId2", "null", true);
        testUser2.setId(2);
        testUser3 = new User("User3", "mail3@mail.com", "userId3", "null", true);
        testUser3.setId(3);
    }

    /**
     * Settle no debts returns empty list.
     */
    @Test
    public void settle_noDebts_returnsEmptyList() {
        final List<Payment> payments = settlement.settle(debts);
        assertNotNull(payments);
        assertTrue(payments.isEmpty());
    }

    /**
     * Settle single debt returns one payment.
     */
    @Test
    public void settle_singleDebt_returnsOnePayment() {
        final double amount = 50.12;
        Debt testDebt = new Debt(testUser2, testUser1, amount, 1);
        debts.add(testDebt);
        final List<Payment> payments = settlement.settle(debts);
        assertEquals(1, payments.size());
    }

    /**
     * Settle single debt returns one payment with amount from debtor to creditor.
     */
    @Test
    public void settle_singleDebt_returnsOnePaymentWithAmountFromDebtorToCreditor() {
        final double amount = 50.12;
        Debt testDebt = new Debt(testUser2, testUser1, amount, 1);
        debts.add(testDebt);
        final Payment payment = settlement.settle(debts).get(0);
        //assert that amount and direction is correct
        assertEquals(amount, payment.getAmount(), DELTA);
        assertEquals(payment.getFromUserId(), testDebt.getDebtorId());
        assertEquals(payment.getToUserId(), testDebt.getCreditorId());
    }

    /**
     * Settle multiple debts one pair returns one payment.
     */
    @Test
    public void settle_multipleDebtsOnePair_returnsOnePayment() {
        Debt testDebt1 = new Debt(testUser2, testUser1, 10.0, 1);
        Debt testDebt2 = new Debt(testUser2, testUser1, 11.0, 2);
        Debt testDebt3 = new Debt(testUser1, testUser2, 15.0, 3);
        debts.add(testDebt1);
        debts.add(testDebt2);
        debts.add(testDebt3);
        final List<Payment> payments = settlement.settle(debts);
        assertEquals(1, payments.size());
    }

    /**
     * Settle multiple debts one pair returns one payment with correct amount.
     */
    @Test
    public void settle_multipleDebtsOnePair_returnsOnePaymentWithCorrectAmount() {
        final double summedAmount = 50.0;
        Debt testDebt1 = new Debt(testUser2, testUser1, summedAmount, 1);
        final double negatedAmount = 11.0;
        // amount applied in both direction negates itself
        Debt testDebt2 = new Debt(testUser2, testUser1, negatedAmount, 2);
        Debt testDebt3 = new Debt(testUser1, testUser2, negatedAmount, 3);
        debts.add(testDebt1);
        debts.add(testDebt2);
        debts.add(testDebt3);
        //assert that amount and direction is correct
        final Payment payment = settlement.settle(debts).get(0);
        assertEquals(summedAmount, payment.getAmount(), DELTA);
    }

    /**
     * Settle one debt per pair multiple pairs returns number of pair amount of payments.
     */
    @Test
    public void settle_oneDebtPerPairMultiplePairs_returnsNumberOfPairAmountOfPayments() {
        Debt pair1Debt = new Debt(testUser2, testUser1, 50.0, 1);
        Debt pair2Debt = new Debt(testUser3, testUser1, 50.0, 2);
        Debt pair3Debt = new Debt(testUser3, testUser2, 50.0, 3);
        debts.add(pair1Debt);
        debts.add(pair2Debt);
        debts.add(pair3Debt);
        final List<Payment> payments = settlement.settle(debts);
        assertEquals(3, payments.size());
    }

    /**
     * Settle one debt per pair multiple pairs returns correct amount for each pair.
     */
    @Test
    public void settle_oneDebtPerPairMultiplePairs_returnsCorrectAmountForEachPair() {
        final double amount = 50.0;
        Debt pair1Debt = new Debt(testUser2, testUser1, amount, 1);
        Debt pair2Debt = new Debt(testUser3, testUser1, amount, 2);
        Debt pair3Debt = new Debt(testUser3, testUser2, amount, 3);
        debts.add(pair1Debt);
        debts.add(pair2Debt);
        debts.add(pair3Debt);
        final List<Payment> payments = settlement.settle(debts);
        for (Payment payment : payments) {
            assertEquals(amount, payment.getAmount(), DELTA);
        }
    }


}