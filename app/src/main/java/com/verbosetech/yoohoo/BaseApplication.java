package com.verbosetech.yoohoo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.onesignal.OneSignal;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.verbosetech.yoohoo.receivers.ConnectivityReceiver;

/**
 * Created by mayank on 11/2/17.
 */

public class BaseApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.provideUserConsent(true);
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new MyOnesignalNotificationOpenedHandler(this))
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();
        ConnectivityReceiver.init(this);
        EmojiManager.install(new GoogleEmojiProvider());

//        String admobAppId = getString(R.string.admob_app_id);
//        if (!TextUtils.isEmpty(admobAppId))
//            MobileAds.initialize(this, admobAppId);
    }
}
