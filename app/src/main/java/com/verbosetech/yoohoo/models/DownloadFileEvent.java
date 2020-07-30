package com.verbosetech.yoohoo.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mayank on 11/5/17.
 */

public class DownloadFileEvent implements Parcelable {
    private Attachment attachment;
    private int attachmentType;
    private String messageId;

    public DownloadFileEvent(int attachmentType, Attachment attachment, String msgId) {
        this.attachment = attachment;
        this.attachmentType = attachmentType;
        this.messageId = msgId;
    }

    protected DownloadFileEvent(Parcel in) {
        attachment = in.readParcelable(Attachment.class.getClassLoader());
        attachmentType = in.readInt();
        messageId = in.readString();
    }

    public static final Creator<DownloadFileEvent> CREATOR = new Creator<DownloadFileEvent>() {
        @Override
        public DownloadFileEvent createFromParcel(Parcel in) {
            return new DownloadFileEvent(in);
        }

        @Override
        public DownloadFileEvent[] newArray(int size) {
            return new DownloadFileEvent[size];
        }
    };

    @AttachmentTypes.AttachmentType
    public int getAttachmentType() {
        return attachmentType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(attachment, flags);
        dest.writeInt(attachmentType);
        dest.writeString(messageId);
    }
}

