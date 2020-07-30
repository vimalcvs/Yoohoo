package com.verbosetech.yoohoo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.verbosetech.yoohoo.utils.Helper;

/**
 * Created by a_man on 1/10/2017.
 */

public class Message implements Parcelable {
    private String id, senderName, senderImage, senderStatus, recipientName, recipientImage, recipientStatus, recipientId, senderId, chatId, timeDiff, dateTimeStamp, body;
    private boolean delivered, sent;
    @AttachmentTypes.AttachmentType
    private int attachmentType;
    private Attachment attachment;

    @Exclude
    private boolean selected;

    public Message() {
    }

    public Message(int attachmentType) {
        this.attachmentType = attachmentType;
        this.senderId = "";
    }

    protected Message(Parcel in) {
        id = in.readString();
        senderName = in.readString();
        senderImage = in.readString();
        senderStatus = in.readString();
        recipientName = in.readString();
        recipientImage = in.readString();
        recipientStatus = in.readString();
        recipientId = in.readString();
        senderId = in.readString();
        chatId = in.readString();
        timeDiff = in.readString();
        dateTimeStamp = in.readString();
        body = in.readString();
        delivered = in.readByte() != 0;
        sent = in.readByte() != 0;
        attachmentType = in.readInt();
        attachment = in.readParcelable(Attachment.class.getClassLoader());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public int getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(int attachmentType) {
        this.attachmentType = attachmentType;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getSenderStatus() {
        return senderStatus;
    }

    public void setSenderStatus(String senderStatus) {
        this.senderStatus = senderStatus;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientImage() {
        return recipientImage;
    }

    public void setRecipientImage(String recipientImage) {
        this.recipientImage = recipientImage;
    }

    public String getRecipientStatus() {
        return recipientStatus;
    }

    public void setRecipientStatus(String recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getTimeDiff() {
        this.timeDiff = Helper.timeDiff(Long.valueOf(this.dateTimeStamp)).toString();
        return timeDiff;
    }

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(String dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(senderName);
        dest.writeString(senderImage);
        dest.writeString(senderStatus);
        dest.writeString(recipientName);
        dest.writeString(recipientImage);
        dest.writeString(recipientStatus);
        dest.writeString(recipientId);
        dest.writeString(senderId);
        dest.writeString(chatId);
        dest.writeString(timeDiff);
        dest.writeString(dateTimeStamp);
        dest.writeString(body);
        dest.writeByte((byte) (delivered ? 1 : 0));
        dest.writeByte((byte) (sent ? 1 : 0));
        dest.writeInt(attachmentType);
        dest.writeParcelable(attachment, flags);
    }
}