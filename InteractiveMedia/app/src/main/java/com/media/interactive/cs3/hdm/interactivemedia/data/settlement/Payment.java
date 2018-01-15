package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;


import com.media.interactive.cs3.hdm.interactivemedia.data.User;

public class Payment {
    private final User from;
    private final User to;
    private final double amount;

    public Payment(User from, User to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }
}
