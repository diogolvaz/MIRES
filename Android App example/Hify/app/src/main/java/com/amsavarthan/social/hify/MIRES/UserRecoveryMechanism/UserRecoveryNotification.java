package com.amsavarthan.social.hify.MIRES.UserRecoveryMechanism;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.amsavarthan.social.hify.R;

import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;



//#############################################################################
//
//                           User Recovery Notification
//
//              Class that creates the undo notification that will
//                  allow users to undo their last request
//
//#############################################################################

public class UserRecoveryNotification {

    // Client Undo Notification
    private static int MIRESClientRecoveryNotificationID=2;

    // Max time that users will have to undo their last request (Default 15 seconds)
    public static int time = 15000;

    // Create the recovery notification
    public static void createRecoveryNotification(Context context, String message, Map<String, Object> transaction_state){

        // Create intent
        Intent intentAction = new Intent(context, UserRecoveryProcess.class);
        // Add option Undo
        intentAction.putExtra("documents", (ArrayList) transaction_state.get("documents"));
        // Get request ID
        intentAction.putExtra("transaction_id",(String) transaction_state.get("transaction_id"));

        PendingIntent pIntentlogin = PendingIntent.getBroadcast(context,1,intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create undo notification
        Notification notification = new NotificationCompat.Builder(context,"MIRES_user_recovery_channel")
                .setContentTitle("MIRES User Recovery")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .addAction(0,"RECOVER",pIntentlogin)
                .setTimeoutAfter(time)
                .setAutoCancel(true)
                .build();

        // Create manager
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MIRES_user_recovery_channel", "MIRES user channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        // Create recovery notification
        manager.notify(MIRESClientRecoveryNotificationID, notification);
    }

    // Remove the undo notification
    public static void deleteRecoveryNotification(Context context) {

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(MIRESClientRecoveryNotificationID);
    }

}
