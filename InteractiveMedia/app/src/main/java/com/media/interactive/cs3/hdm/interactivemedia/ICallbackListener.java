package com.media.interactive.cs3.hdm.interactivemedia;

/**
 * Created by benny on 21.12.17.
 */

public interface ICallbackListener<T,O> {
  void onSuccess(T response);
  void onFailure(O error);
}
