package com.amsavarthan.social.hify.MIRES.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.amsavarthan.social.hify.R;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.Context.NOTIFICATION_SERVICE;


//#############################################################################
//
//                           Notifications
//
//              This class handles with the MIRES recovery messages
//
//#############################################################################

public class Notifications {

    // MIRES admin recovery notification
    public static int MIRES_admin_recovery_notification=1;

    // Handle notifications coming from MIRES
    public static void createMIRESMessage(RemoteMessage remoteMessage, Context context){

            //  Create notification
            Notification notification = new NotificationCompat.Builder(context,"MIRES_notification_channel")
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(remoteMessage.getNotification().getBody()))
                    .build();

            // Create manager
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("MIRES_notification_channel", "MIRES notification channel", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }

            // Notify user
            manager.notify(MIRES_admin_recovery_notification, notification);
    }

    // Check if it is a notification from MIRES
    public static boolean isMIRESNotification(RemoteMessage message){
        return (message.getNotification()!=null && message.getNotification().getTitle()!=null && message.getNotification().getTitle().equals("MIRES_RECOVERY_SERVICE"));
    }

}
