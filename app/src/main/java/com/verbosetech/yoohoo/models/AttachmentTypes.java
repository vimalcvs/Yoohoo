package com.verbosetech.yoohoo.models;

import android.content.Context;

import androidx.annotation.IntDef;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.services.UploadAndSendService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mayank on 10/5/17.
 */

public class AttachmentTypes {

    public static final int CONTACT = 0;
    public static final int VIDEO = 1;
    public static final int IMAGE = 2;
    public static final int AUDIO = 3;
    public static final int LOCATION = 4;
    public static final int DOCUMENT = 5;
    public static final int NONE_TEXT = 6;
    public static final int NONE_TYPING = 7;
    public static final int RECORDING = 8;
    public static final int NONE_NOTIFICATION = 9;

    public static String getTypeNameDisplay(Context mContext, int type) {
        switch (type) {
            case AUDIO:
                return mContext.getString(R.string.audio);
            case VIDEO:
                return mContext.getString(R.string.video);
            case CONTACT:
                return mContext.getString(R.string.contact);
            case DOCUMENT:
                return mContext.getString(R.string.document);
            case IMAGE:
                return mContext.getString(R.string.image);
            case LOCATION:
                return mContext.getString(R.string.location);
            case NONE_TEXT:
                return mContext.getString(R.string.none_text);
            case NONE_TYPING:
                return mContext.getString(R.string.none_typing);
            case RECORDING:
                return mContext.getString(R.string.recording);
            case NONE_NOTIFICATION:
                return mContext.getString(R.string.notification);
            default:
                return mContext.getString(R.string.none);
        }
    }

    @IntDef({CONTACT, VIDEO, IMAGE, AUDIO, LOCATION, DOCUMENT, NONE_TEXT, NONE_TYPING, RECORDING, NONE_NOTIFICATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AttachmentType {
    }

    public static String getTypeName(@AttachmentType int attachmentType) {
        switch (attachmentType) {
            case AUDIO:
                return "Audio";
            case VIDEO:
                return "Video";
            case CONTACT:
                return "Contact";
            case DOCUMENT:
                return "Document";
            case IMAGE:
                return "Image";
            case LOCATION:
                return "Location";
            case NONE_TEXT:
                return "none_text";
            case NONE_TYPING:
                return "none_typing";
            case RECORDING:
                return "Recording";
            case NONE_NOTIFICATION:
                return "Notification";
            default:
                return "none";
        }
    }
}
