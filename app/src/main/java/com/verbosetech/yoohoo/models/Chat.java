package com.verbosetech.yoohoo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.verbosetech.yoohoo.utils.Helper;

import java.util.Objects;

/**
 * Created by a_man on 1/10/2017.
 */

public class Chat implements Parcelable {
    private String chatChild, userId, dateTimeStamp, timeDiff, lastMessage, chatName, chatImage, chatStatus, lastMessageId;
    @Exclude
    private boolean selected, latest;

    protected Chat(Parcel in) {
        chatChild = in.readString();
        userId = in.readString();
        dateTimeStamp = in.readString();
        timeDiff = in.readString();
        lastMessage = in.readString();
        chatName = in.readString();
        chatImage = in.readString();
        chatStatus = in.readString();
        lastMessageId = in.readString();
        selected = in.readByte() != 0;
        latest = in.readByte() != 0;
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return chatChild.equals(chat.chatChild);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatChild);
    }

    public Chat(Message msg, boolean isMeSender) {
        this.chatChild = msg.getChatId();
        this.userId = isMeSender ? msg.getRecipientId() : msg.getSenderId();
        this.chatName = (isGroup() || isMeSender) ? msg.getRecipientName() : msg.getSenderName();
        this.chatImage = (isGroup() || isMeSender) ? msg.getRecipientImage() : msg.getSenderImage();
        this.chatStatus = (isGroup() || isMeSender) ? msg.getRecipientStatus() : msg.getSenderStatus();
        this.dateTimeStamp = msg.getDateTimeStamp();
        this.lastMessage = msg.getBody();
        this.lastMessageId = msg.getId();
        this.latest = false;
    }

    public Chat(User user, String myId) {
        this.chatChild = Helper.getChatChild(user.getId(), myId);
        this.userId = user.getId();
        this.chatName = user.getName();
        this.chatImage = user.getImage();
        this.chatStatus = user.getStatus();
        this.dateTimeStamp = "";
        this.lastMessage = "";
        this.latest = false;
    }

    public Chat(Group group) {
        this.chatChild = group.getId();
        this.userId = group.getUserIds().get(0);
        this.chatName = group.getName();
        this.chatImage = group.getImage();
        this.chatStatus = group.getStatus();
        this.dateTimeStamp = "";
        this.lastMessage = "";
        this.latest = true;
    }

    public String getChatChild() {
        return chatChild;
    }

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    public String getTimeDiff() {
        this.timeDiff = Helper.timeDiff(Long.valueOf(this.dateTimeStamp)).toString();
        return timeDiff;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageId() {
        return lastMessageId == null ? "" : lastMessageId;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public String getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(String chatStatus) {
        this.chatStatus = chatStatus;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isGroup() {
        return getChatChild().startsWith(Helper.GROUP_PREFIX);
    }

    public String getUserId() {
        return userId;
    }

    public boolean isLatest() {
        return latest;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chatChild);
        dest.writeString(userId);
        dest.writeString(dateTimeStamp);
        dest.writeString(timeDiff);
        dest.writeString(lastMessage);
        dest.writeString(chatName);
        dest.writeString(chatImage);
        dest.writeString(chatStatus);
        dest.writeString(lastMessageId);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (latest ? 1 : 0));
    }
}