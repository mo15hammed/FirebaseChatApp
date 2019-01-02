package com.example.mo15h.firebasechatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServic";
    private static final String MY_CHANNEL_ID = "AWESOME_CHAT_007";

    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        Log.d(TAG, "onMessageReceived: TRIGGERED !!!!!!!");


        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationText = remoteMessage.getNotification().getBody();
        String clickAction = remoteMessage.getNotification().getClickAction();
        String fromUserId = remoteMessage.getData().get("from_user_id");

        Log.d(TAG, "onMessageReceived: CLICK ACTION = " + clickAction);
        Log.d(TAG, "onMessageReceived: FROM USER ID = " + fromUserId);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, MY_CHANNEL_ID);
        Intent resultIntent = new Intent(clickAction);

        resultIntent.putExtra("userID", fromUserId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 123, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle(notificationTitle);
        mBuilder.setContentText(notificationText);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = MY_CHANNEL_ID;
            NotificationChannel channel = new NotificationChannel(channelId,
                    MY_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        int notificationID = (int) System.currentTimeMillis();
        mNotificationManager.notify(notificationID, mBuilder.build());

    }

}
