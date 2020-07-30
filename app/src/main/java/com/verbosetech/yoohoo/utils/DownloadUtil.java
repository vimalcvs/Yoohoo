package com.verbosetech.yoohoo.utils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import android.widget.Toast;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.DownloadFileEvent;

import java.io.File;

/**
 * Created by mayank on 11/5/17.
 */

public class DownloadUtil {

    public void checkAndLoad(Context context, DownloadFileEvent downloadFileEvent) {

        File file = new File(Helper.getFileBase(context) + "/"
                +
                AttachmentTypes.getTypeName(downloadFileEvent.getAttachmentType()), downloadFileEvent.getAttachment().getName());

        if (file.exists()) {
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            //newIntent.setDataAndType(FileProvider.getUriForFile(context, context.getString(R.string.authority), file), Helper.getMimeType(context, downloadFileEvent.getAttachment().getData()));
            newIntent.setDataAndType(FileProvider.getUriForFile(context, context.getString(R.string.authority), file), Helper.getMimeType(context, Uri.fromFile(file)));
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, R.string.error_detail, Toast.LENGTH_LONG).show();
            }
        } else {
            downloadFile(context, downloadFileEvent.getAttachment().getUrl(), AttachmentTypes.getTypeName(downloadFileEvent.getAttachmentType()), downloadFileEvent.getAttachment().getName());
            Toast.makeText(context, R.string.downloading, Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(Context context, String url, String type, String fileName) {
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setTitle(context.getString(R.string.app_name))
                .setDescription(context.getString(R.string.downloading) + " " + fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, context.getString(R.string.app_name) + "/" + type + "/" + fileName);
        mgr.enqueue(request);
    }

    private String getDirectoryPath(Context context, String type) {
        return Environment.DIRECTORY_DOWNLOADS + "/" + context.getString(R.string.app_name) + "/" + type;
    }
}
