package com.media.interactive.cs3.hdm.interactivemedia.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;



/**
 * Created by benny on 20.01.18.
 */

public class NetworkStateChangeReceiver extends BroadcastReceiver {

    /**
     * The Constant NETWORK_AVAILABLE_ACTION.
     */
    public static final String NETWORK_AVAILABLE_ACTION = "NetworkAvailable";
    /**
     * The Constant IS_NETWORK_AVAILABLE.
     */
    public static final String IS_NETWORK_AVAILABLE = "IsNetworkAvailable";
    /**
     * The Constant TAG.
     */
    private static final String TAG = NetworkStateChangeReceiver.class.getSimpleName();

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent networkStateIntent = new Intent(NETWORK_AVAILABLE_ACTION);
        networkStateIntent.putExtra(IS_NETWORK_AVAILABLE, isConnectedToInternet(context));
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);
    }

    /**
     * Checks if is connected to internet.
     *
     * @param context the context
     * @return true, if is connected to internet
     */
    private boolean isConnectedToInternet(Context context) {
        try {
            if (context != null) {
                final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
}
