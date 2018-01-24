package com.media.interactive.cs3.hdm.interactivemedia.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.activties.MainActivity;

import java.util.Map;



/**
 * Created by benny on 23.12.17.
 */

public class InteractiveMediaMessagingService extends FirebaseMessagingService {

    /**
     * The Constant TAG.
     */
    private static final String TAG = InteractiveMediaMessagingService.class.getSimpleName();

    /**
     * On message received.
     *
     * @param remoteMessage the remote message
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final Map<String, String> data = remoteMessage.getData();
        sendNotification(data);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Log.d(TAG, entry.getKey() + ": " + entry.getValue());
        }
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

    /**
     * Send notification to the UI.
     *
     * @param data the data
     */
    private void sendNotification(Map<String, String> data) {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("transactionReload", true);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.logo_notificatoin_mdpi)
            .setContentTitle(data.get("title"))
            .setContentText(data.get("body"))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
