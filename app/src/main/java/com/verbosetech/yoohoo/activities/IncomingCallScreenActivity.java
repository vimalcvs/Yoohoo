package com.verbosetech.yoohoo.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.models.LogCall;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.services.SinchService;
import com.verbosetech.yoohoo.utils.AudioPlayer;

import java.util.ArrayList;
import java.util.List;

public class IncomingCallScreenActivity extends BaseActivity {
    static final String TAG = IncomingCallScreenActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_CALL = 951;
    private static final String CHANNEL_ID_USER_MISSCALL = "my_channel_04";

    private String mCallId;
    private AudioPlayer mAudioPlayer;
    private User user;
    private String[] recordPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.VIBRATE};

    private TextView remoteUser;
    private ImageView userImage1, userImage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);

        remoteUser = (TextView) findViewById(R.id.remoteUser);
        userImage1 = findViewById(R.id.userImage1);
        userImage2 = findViewById(R.id.userImage2);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();

        Intent intent = getIntent();
        mCallId = intent.getStringExtra(SinchService.CALL_ID);

        findViewById(R.id.answerButton).setOnClickListener(mClickListener);
        findViewById(R.id.declineButton).setOnClickListener(mClickListener);
    }

    @Override
    void onSinchConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());

            ArrayList<User> myUsers = helper.getMyUsers();
            for (User mUser : myUsers) {
                if (mUser.getId().equals(call.getRemoteUserId())) {
                    user = mUser;
                    break;
                }
            }

            remoteUser.setText(user != null ? user.getName() : call.getRemoteUserId());
            if (user != null) {
                Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(userImage1);
                Glide.with(this).load(user.getImage()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.yoohoo_placeholder)).into(userImage2);
            } else {
                usersRef.child(call.getRemoteUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mContext != null) {
                            User userIn = dataSnapshot.getValue(User.class);
                            if (userIn != null) {
                                user = userIn;
                                remoteUser.setText(user.getName());
                                Glide.with(mContext).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(userImage1);
                                Glide.with(mContext).load(user.getImage()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.yoohoo_placeholder)).into(userImage2);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            TextView callingType = findViewById(R.id.yoohoo_calling);
            callingType.setText(getString(call.getDetails().isVideoOffered() ? R.string.video_calling_in : R.string.voice_calling_in));
        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            try {
                call.answer();
                startActivity(CallScreenActivity.newIntent(this, user, mCallId, "IN"));
                finish();
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            finish();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            answerClicked();
        } else {
            Toast.makeText(this, R.string.permission_mic, Toast.LENGTH_LONG).show();
        }
    }

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private boolean recordPermissionsAvailable() {
        boolean available = true;
        for (String permission : recordPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                available = false;
                break;
            }
        }
        return available;
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            if (cause.toString().equals("CANCELED") || cause.toString().equals("DENIED")) {
                LogCall logCall = null;
                if (user == null) {
                    helper.getMyContacts();
                    user = new User(call.getRemoteUserId(), getNameById(call.getRemoteUserId()), getString(R.string.app_name), "");
                }

                logCall = new LogCall(user, System.currentTimeMillis(), 0, call.getDetails().isVideoOffered(), cause.toString(), userMe.getId());
                helper.saveCallLog(logCall);

                if (cause.toString().equals("CANCELED")) {
                    notifyMisscall(logCall);
                }
            }
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }

    private void notifyMisscall(LogCall logCall) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 56, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_USER_MISSCALL, "YooHoo misscall notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_USER_MISSCALL);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSmallIcon(R.drawable.noti_icon)
                .setContentTitle(logCall.getUserName())
                .setContentText(getString(R.string.gave_miss))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        int msgId = 0;
        try {
            msgId = Integer.parseInt(logCall.getUserId());
        } catch (NumberFormatException ex) {
            msgId = Integer.parseInt(logCall.getUserId().substring(logCall.getUserId().length() / 2));
        }
        notificationManager.notify(msgId, notificationBuilder.build());
    }

    private View.OnClickListener mClickListener = v -> {
        switch (v.getId()) {
            case R.id.answerButton:
                if (recordPermissionsAvailable()) {
                    answerClicked();
                } else {
                    ActivityCompat.requestPermissions(IncomingCallScreenActivity.this, recordPermissions, REQUEST_PERMISSION_CALL);
                }
                break;
            case R.id.declineButton:
                declineClicked();
                break;
        }
    };

    @Override
    void onSinchDisconnected() {

    }
}
