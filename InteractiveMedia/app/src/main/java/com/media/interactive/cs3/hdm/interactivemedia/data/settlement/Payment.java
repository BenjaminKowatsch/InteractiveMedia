package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;


public class Payment {
    private final long fromUserId;
    private final long toUserId;
    private final double amount;

    public Payment(long from, long to, double amount) {
        this.fromUserId = from;
        this.toUserId = to;
        this.amount = amount;
    }

    public long getFromUserId() {
        return fromUserId;
    }

    public long getToUserId() {
        return toUserId;
    }

    public double getAmount() {
        return amount;
    }
}
