package com.verbosetech.yoohoo.services;

import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;
import com.verbosetech.yoohoo.models.Contact;
import com.verbosetech.yoohoo.utils.Helper;

import java.util.HashMap;

public class MyOnesignalNotificationService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {
        // Read properties from result.
        Log.d("OSNotificationReceived1", notification.payload.title);
        Log.d("OSNotificationReceived2", notification.payload.body);
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = builder -> {
            if (notification.payload.title != null) {
                String notificationSenderIDEndTrim = Helper.getEndTrim(notification.payload.title);
                HashMap<String, Contact> savedContacts = new Helper(this).getMyContacts();
                if (savedContacts.containsKey(notificationSenderIDEndTrim)) {
                    return builder.setContentTitle(savedContacts.get(notificationSenderIDEndTrim).getName());
                }
            }
//            if (notification.payload.title != null) {
//                String[] bodySplit = notification.payload.title.split(" ");
//                if (bodySplit.length >= 1) {
//                    String notificationSenderIDEndTrim = Helper.getEndTrim(bodySplit[0]);
//                    HashMap<String, Contact> savedContacts = new Helper(this).getMyContacts();
//                    if (savedContacts.containsKey(notificationSenderIDEndTrim)) {
//                        return builder.setContentText(savedContacts.get(notificationSenderIDEndTrim).getName() + notification.payload.title.substring(bodySplit[0].length()));
//                    }
//                }
//            }
            return builder;
        };

        OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
        Log.d("OneSignalExample", "Notification displayed with id: " + displayedResult.androidNotificationId);
        // Return true to stop the notification from displaying.
        return false;
    }
}
