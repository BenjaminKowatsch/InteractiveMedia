package com.media.interactive.cs3.hdm.interactivemedia.data;


public class Debt {
    private final long creditorId;
    private final long debtorId;
    private final double amount;
    private final long transactionId;

    public Debt(User creditor, User debtor, double amount, Transaction createdFrom) {
        this.creditorId = creditor.getId();
        this.debtorId = debtor.getId();
        this.amount = amount;
        this.transactionId = createdFrom.getId();
    }

    public Debt(long creditorId, long debtorId, double amount, long transactionId) {
        this.creditorId = creditorId;
        this.debtorId = debtorId;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public Debt(User creditor, User debtor, double amount, long transactionId) {
        this.creditorId = creditor.getId();
        this.debtorId = debtor.getId();
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public long getCreditorId() {
        return creditorId;
    }

    public long getDebtorId() {
        return debtorId;
    }

    public double getAmount() {
        return amount;
    }

    public long getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return "Debt{" +
                "creditorId=" + creditorId +
                ", debtorId=" + debtorId +
                ", amount=" + amount +
                ", transactionId=" + transactionId +
                '}';
    }
}
