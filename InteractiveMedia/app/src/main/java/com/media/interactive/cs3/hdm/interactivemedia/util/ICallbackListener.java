package com.media.interactive.cs3.hdm.interactivemedia.util;



/**
 * Created by benny on 21.12.17.
 *
 * @param <T> the generic type
 * @param <O> the generic type
 * @see ICallbackEvent
 */

public interface ICallbackListener<T, O> {

    /**
     * On success.
     *
     * @param response the response
     */
    void onSuccess(T response);

    /**
     * On failure.
     *
     * @param error the error
     */
    void onFailure(O error);
}
