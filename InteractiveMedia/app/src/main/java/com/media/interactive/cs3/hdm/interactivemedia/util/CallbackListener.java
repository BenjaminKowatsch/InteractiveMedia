package com.media.interactive.cs3.hdm.interactivemedia.util;

/**
 * Created by benny on 21.12.17.
 */

public abstract class CallbackListener<T,O> implements ICallbackListener<T,O> {
  protected CallbackListener<T,O> childCallback = null;
  public CallbackListener(CallbackListener<T,O> callbackListener){
    childCallback = callbackListener;
  }
  public CallbackListener(){
  }
}
