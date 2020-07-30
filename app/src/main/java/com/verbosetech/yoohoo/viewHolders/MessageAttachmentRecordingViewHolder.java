package com.verbosetech.yoohoo.viewHolders;

import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.OnMessageItemClick;
import com.verbosetech.yoohoo.models.Attachment;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.utils.FileUtils;
import com.verbosetech.yoohoo.utils.Helper;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by a_man on 02-02-2018.
 */

public class MessageAttachmentRecordingViewHolder extends BaseMessageViewHolder {
    TextView text;
    TextView durationOrSize;
    LinearLayout ll;
    ProgressBar progressBar;
    ImageView playPauseToggle;

    private RecordingViewInteractor recordingViewInteractor;

    public MessageAttachmentRecordingViewHolder(View itemView, OnMessageItemClick itemClickListener, RecordingViewInteractor recordingViewInteractor) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        durationOrSize = itemView.findViewById(R.id.duration);
        ll = itemView.findViewById(R.id.container);
        progressBar = itemView.findViewById(R.id.progressBar);
        playPauseToggle = itemView.findViewById(R.id.playPauseToggle);

        this.recordingViewInteractor = recordingViewInteractor;

        itemView.setOnClickListener(v -> onItemClick(true));
        itemView.setOnLongClickListener(v -> {
            onItemClick(false);
            return true;
        });
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);

        Attachment attachment = message.getAttachment();

        boolean loading = message.getAttachment().getUrl().equals("loading");
        if (progressBar.getProgressDrawable() != null)
            progressBar.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        if (progressBar.getIndeterminateDrawable() != null)
            progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        playPauseToggle.setVisibility(loading ? View.GONE : View.VISIBLE);

        File file = Helper.getFile(context, message, isMine());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(context, uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long millis = Long.parseLong(durationStr);
                durationOrSize.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis)));
                mmr.release();
            } catch (Exception e) {
            }
        } else
            durationOrSize.setText(FileUtils.getReadableFileSize(attachment.getBytesCount()));
        Glide.with(context).load(file.exists() ? recordingViewInteractor.isRecordingPlaying(message.getAttachment().getName()) ? (isMine() ? R.drawable.ic_stop_white_36dp : R.drawable.ic_stop_blue_36dp) : (isMine() ? R.drawable.ic_play_circle_outline_white_36dp : R.drawable.ic_play_circle_outline_blue_36dp) : (isMine() ? R.drawable.ic_file_download_white_36dp : R.drawable.ic_file_download_blue_36dp)).into(playPauseToggle);
        text.setTextColor(ContextCompat.getColor(context, isMine() ? android.R.color.white : android.R.color.black));
        durationOrSize.setTextColor(ContextCompat.getColor(context, isMine() ? R.color.colorBg : R.color.textColor4));
    }

    public interface RecordingViewInteractor {
        boolean isRecordingPlaying(String fileName);
    }

}
