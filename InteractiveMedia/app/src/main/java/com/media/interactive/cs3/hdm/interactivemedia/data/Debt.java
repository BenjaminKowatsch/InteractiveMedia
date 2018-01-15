package com.media.interactive.cs3.hdm.interactivemedia.data;


public class Debt {
    private final User creditor;
    private final User debtor;
    private final double amount;

    public Debt(User creditor, User debtor, double amount) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
    }
}
