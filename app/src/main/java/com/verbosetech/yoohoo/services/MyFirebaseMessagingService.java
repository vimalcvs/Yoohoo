package com.verbosetech.yoohoo.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("FCM", "onMessageReceived");
//        if (new Helper(this).isLoggedIn()) {
//            Intent intent = new Intent(this, FirebaseChatService.class);
//            PendingIntent pendingIntent = PendingIntent.getService(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pendingIntent);
//            Log.e("FCM", "scheduled");
//        }
    }
}
