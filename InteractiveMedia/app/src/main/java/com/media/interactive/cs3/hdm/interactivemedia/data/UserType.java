package com.media.interactive.cs3.hdm.interactivemedia.data;



/**
 * Created by benny on 31.10.17.
 */

public enum UserType {

    /**
     * The default.
     */
    DEFAULT(0),

    /**
     * The google.
     */
    GOOGLE(1),

    /**
     * The facebook.
     */
    FACEBOOK(2);

    /**
     * The value.
     */
    private final int value;

    /**
     * Instantiates a new user type.
     *
     * @param value the value
     */
    private UserType(int value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
