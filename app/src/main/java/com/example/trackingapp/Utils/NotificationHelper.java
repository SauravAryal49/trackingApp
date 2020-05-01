package com.example.trackingapp.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.trackingapp.R;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationHelper extends ContextWrapper {
    public static final String CHANNEL_ID="com.example.trackingapp";
    private static final String CHANNEL_NAME="Tracking";

    private NotificationManager notificationManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            createChannel();
    }

    @RequiresApi(api=Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel newChannel=new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);

        newChannel.enableLights(false);
        newChannel.enableVibration(true);
        newChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(newChannel);

    }

    public NotificationManager getManager() {
        if(notificationManager==null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    @RequiresApi(api= Build.VERSION_CODES.O)
    public Notification.Builder getRealtimeNotification(String title, String content, Uri defaultSound) {

        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);

    }
}
