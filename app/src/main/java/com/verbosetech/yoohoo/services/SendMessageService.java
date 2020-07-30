package com.verbosetech.yoohoo.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Group;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SendMessageService extends IntentService {

    public SendMessageService() {
        super("SendMessageService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Message message = intent.getParcelableExtra("message");
            Group group = intent.getParcelableExtra("group");
            Chat chat = intent.getParcelableExtra("chat");
            ArrayList<String> userPlayerIds = intent.getStringArrayListExtra("attachment_player_ids");
            if (message != null) {
                DatabaseReference chatRef, inboxRef;
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//get firebase instance
                chatRef = firebaseDatabase.getReference(Helper.REF_CHAT);//instantiate chat's firebase reference
                inboxRef = firebaseDatabase.getReference(Helper.REF_INBOX);//instantiate inbox's firebase reference

                message.setId(chatRef.child(message.getChatId()).push().getKey());
                //Add message in chat child
                chatRef.child(message.getChatId()).child(message.getId()).setValue(message);
                //Add message for inbox updates
                if (chat.isGroup()) {
                    if (group != null && group.getUserIds() != null) {
                        for (String memberId : group.getUserIds()) {
                            inboxRef.child(memberId).child(group.getId()).setValue(message);
                        }
                    }
                } else {
                    inboxRef.child(message.getSenderId()).child(message.getRecipientId()).setValue(message);
                    inboxRef.child(message.getRecipientId()).child(message.getSenderId()).setValue(message);
                }

                if (userPlayerIds != null && !userPlayerIds.isEmpty()) {
                    try {
//                        String headings = message.getSenderId() + " " + getString(R.string.new_message_sent);
                        //String headings = message.getSenderId();
                        String headings = (chat.isGroup() && group != null) ? group.getName() : message.getSenderId();
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
        }
    }
}
