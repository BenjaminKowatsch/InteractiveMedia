package com.media.interactive.cs3.hdm.interactivemedia.data;


public class Debt {
    public final String fromUser;
    public final String toUser;
    public final double amount;

    public Debt(String fromUser, String toUser, double amount) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }
}
