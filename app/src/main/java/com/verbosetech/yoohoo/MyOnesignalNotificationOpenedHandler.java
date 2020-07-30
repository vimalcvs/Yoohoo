package com.verbosetech.yoohoo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.verbosetech.yoohoo.activities.ChatActivity;
import com.verbosetech.yoohoo.activities.MainActivity;
import com.verbosetech.yoohoo.activities.SignInActivity;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Contact;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.utils.Helper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

class MyOnesignalNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    private final Context context;
    private HashMap<String, Contact> savedContacts;

    MyOnesignalNotificationOpenedHandler(Context context) {
        this.context = context;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        Helper.DISABLE_SPLASH_HANDLER = true;
        JSONObject data = result.notification.payload.additionalData;
        Log.d("notificationOpened", data.toString());
        Helper helper = new Helper(context);
        if (helper.isLoggedIn()) {
            Chat newChat = null;
            savedContacts = helper.getMyContacts();
            User userMe = helper.getLoggedInUser();
            Gson gson = new Gson();
            Message message = gson.fromJson(data.toString(), new TypeToken<Message>() {
            }.getType());
            if (message != null && message.getId() != null && message.getChatId() != null) {
                if (message.getAttachmentType() == AttachmentTypes.NONE_NOTIFICATION)
                    setNotificationMessageNames(message, userMe);
                newChat = new Chat(message, message.getSenderId().equals(userMe.getId()));
                if (!newChat.isGroup()) newChat.setChatName(getNameById(newChat.getUserId()));
            }

            if (newChat != null) {
                Intent intent = ChatActivity.newIntent(context, new ArrayList<>(), newChat);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(context, MainActivity.class));
            }
        } else {
            context.startActivity(new Intent(context, SignInActivity.class));
        }
    }

    private void setNotificationMessageNames(Message newMessage, User userMe) {
        String[] bodySplit = newMessage.getBody().split(" ");
        if (bodySplit.length > 1) {
            String userOneIDEndTrim = Helper.getEndTrim(bodySplit[0]);
            if (TextUtils.isDigitsOnly(userOneIDEndTrim)) {
                String userNameInPhone;
                if (bodySplit[0].equals(userMe.getId()))
                    userNameInPhone = context.getString(R.string.you);
                else
                    userNameInPhone = getNameById(userOneIDEndTrim);
                if (userNameInPhone.equals(userOneIDEndTrim))
                    userNameInPhone = bodySplit[0];
                newMessage.setBody(userNameInPhone + newMessage.getBody().substring(bodySplit[0].length()));
            }
            String userTwoIDEndTrim = Helper.getEndTrim(bodySplit[bodySplit.length - 1]);
            if (TextUtils.isDigitsOnly(userTwoIDEndTrim)) {
                String userNameInPhone;
                if (bodySplit[bodySplit.length - 1].equals(userMe.getId()))
                    userNameInPhone = context.getString(R.string.you);
                else
                    userNameInPhone = getNameById(userTwoIDEndTrim);
                if (userNameInPhone.equals(userTwoIDEndTrim))
                    userNameInPhone = bodySplit[bodySplit.length - 1];
                newMessage.setBody(newMessage.getBody().substring(0, newMessage.getBody().indexOf(bodySplit[bodySplit.length - 1])) + userNameInPhone);
            }
        }
    }

    private String getNameById(String senderId) {
        String senderIDEndTrim = Helper.getEndTrim(senderId);
        if (savedContacts.containsKey(senderIDEndTrim))
            return savedContacts.get(senderIDEndTrim).getName();
        else
            return senderId;
    }
}
