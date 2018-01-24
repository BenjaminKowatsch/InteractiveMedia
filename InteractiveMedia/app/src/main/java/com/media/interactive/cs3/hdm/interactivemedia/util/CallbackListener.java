package com.media.interactive.cs3.hdm.interactivemedia.util;



/**
 * Created by benny on 21.12.17.
 *
 * @param <T> the generic type
 * @param <O> the generic type
 * @see CallbackEvent
 */

public abstract class CallbackListener<T, O> implements ICallbackListener<T, O> {

    /**
     * The child callback.
     */
    protected CallbackListener<T, O> childCallback = null;

    /**
     * Instantiates a new callback listener.
     *
     * @param callbackListener the callback listener
     */
    public CallbackListener(CallbackListener<T, O> callbackListener) {
        childCallback = callbackListener;
    }

    /**
     * Instantiates a new callback listener.
     */
    public CallbackListener() {
    }
}
