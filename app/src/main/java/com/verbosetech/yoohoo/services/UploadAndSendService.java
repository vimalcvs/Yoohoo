package com.verbosetech.yoohoo.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.models.Attachment;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.utils.FirebaseUploader;
import com.verbosetech.yoohoo.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class UploadAndSendService extends Service {
    public static final String CHANNEL_ID = "UploadAndSendChannel";
    private ArrayList<String> userPlayerIds;
    private Message message;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.noti_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.uploading))
                .setSound(null)
                //.setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Attachment attachment = intent.getParcelableExtra("attachment");
        int type = intent.getIntExtra("attachment_type", -1);
        String attachmentFilePath = intent.getStringExtra("attachment_file_path");
        Message message = intent.getParcelableExtra("attachment_message");
        ArrayList<String> groupIds = intent.getParcelableExtra("attachment_group_ids");
        userPlayerIds = intent.getStringArrayListExtra("attachment_player_ids");
        sendPrepareMessage(type, message);
        upload(new File(attachmentFilePath), attachment, type, groupIds);
        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void upload(final File fileToUpload,
                        final Attachment attachment, final int attachmentType,
                        final ArrayList<String> groupIds) {
        if (!fileToUpload.exists())
            return;
        final String fileName = Uri.fromFile(fileToUpload).getLastPathSegment();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.app_name)).child(AttachmentTypes.getTypeName(attachmentType)).child(fileName);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            //If file is already uploaded
            Attachment attachment1 = attachment;
            if (attachment1 == null) attachment1 = new Attachment();
            attachment1.setName(fileName);
            attachment1.setUrl(uri.toString());
            attachment1.setBytesCount(fileToUpload.length());
            sendMessage(attachment1, groupIds);
        }).addOnFailureListener(exception -> {
            //Elase upload and then send message
            FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
                @Override
                public void onUploadFail(String message1) {
                    Log.e("DatabaseException", message1);
                    Toast.makeText(UploadAndSendService.this, R.string.upload_fail, Toast.LENGTH_SHORT).show();
                    removePreparedMessage();
                    stopSelf();
                }

                @Override
                public void onUploadSuccess(String downloadUrl) {
                    Attachment attachment1 = attachment;
                    if (attachment1 == null) attachment1 = new Attachment();
                    attachment1.setName(fileToUpload.getName());
                    attachment1.setUrl(downloadUrl);
                    attachment1.setBytesCount(fileToUpload.length());
                    sendMessage(attachment1, groupIds);
                }

                @Override
                public void onUploadProgress(int progress) {

                }

                @Override
                public void onUploadCancelled() {
                    removePreparedMessage();
                    stopSelf();
                }
            }, storageReference);
            firebaseUploader.uploadOthers(getApplicationContext(), fileToUpload);
        });
    }

    private void sendMessage(Attachment attachment, ArrayList<String> groupIds) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);
        DatabaseReference inboxRef = firebaseDatabase.getReference(Helper.REF_INBOX);
        message.setAttachment(attachment);
        //message.setDateTimeStamp(String.valueOf(System.currentTimeMillis()));
        message.setSent(true);
        //Add message in chat child
        chatRef.child(message.getChatId()).child(message.getId()).setValue(message);
        //Add message for inbox updates
        if (message.getChatId().startsWith(Helper.GROUP_PREFIX) && groupIds != null) {
            for (String memberId : groupIds) {
                inboxRef.child(memberId).child(message.getChatId()).setValue(message);
            }
        } else {
            inboxRef.child(message.getSenderId()).child(message.getRecipientId()).setValue(message);
            inboxRef.child(message.getRecipientId()).child(message.getSenderId()).setValue(message);
        }
        notifyMessage(message.getSenderId(), message);
        stopSelf();
    }

    private void removePreparedMessage() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);

        chatRef.child(message.getChatId()).child(message.getId()).setValue(null);
    }

    private void sendPrepareMessage(int type, Message message) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);
        message.setAttachmentType(type);
        Attachment attachment = new Attachment();
        attachment.setUrl("loading");
        message.setAttachment(attachment);
        message.setDateTimeStamp(String.valueOf(System.currentTimeMillis()));
        message.setSent(false);
        //message.setBody(getStringResourceByName(AttachmentTypes.getTypeName(type).toLowerCase()));
        message.setBody(AttachmentTypes.getTypeNameDisplay(this, type));
        message.setId(chatRef.child(message.getChatId()).push().getKey());
        //Add message in chat child
        chatRef.child(message.getChatId()).child(message.getId()).setValue(message);
        this.message = message;
    }

    private void notifyMessage(String userMeId, Message message) {
        if (userPlayerIds != null && !userPlayerIds.isEmpty()) {
            try {
//                String headings = userMeId + " " + getString(R.string.new_message_sent);
                //String headings = userMeId;
                Chat chat = new Chat(message, message.getSenderId().equals(userMeId));
                String headings = chat.isGroup() ? chat.getChatName() : userMeId;
                OneSignal.postNotification(new JSONObject("{'headings': {'en':'" + headings + "'}, 'contents': {'en':'" + message.getBody() + "'}, 'include_player_ids': " + userPlayerIds.toString() + ",'data': " + new Gson().toJson(message) + ",'android_group':" + message.getChatId() + " }"),
                        new OneSignal.PostNotificationResponseHandler() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                            }

                            @Override
                            public void onFailure(JSONObject response) {
                                Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.up_and_send),
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

//    private String getStringResourceByName(String aString) {
//        String packageName = getPackageName();
//        int resId = getResources().getIdentifier(aString, "string", packageName);
//        return getString(resId);
//    }

}
