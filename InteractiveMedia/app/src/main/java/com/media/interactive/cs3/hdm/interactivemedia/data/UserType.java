package com.media.interactive.cs3.hdm.interactivemedia.data;

/**
 * Created by benny on 31.10.17.
 */

public enum UserType {
    DEFAULT(0),
    GOOGLE(1),
    FACEBOOK(2);

    private final int value;
    private UserType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
