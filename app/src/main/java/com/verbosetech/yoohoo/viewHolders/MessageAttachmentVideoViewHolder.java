package com.verbosetech.yoohoo.viewHolders;

import android.os.Environment;

import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.OnMessageItemClick;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.utils.FileUtils;
import com.verbosetech.yoohoo.utils.Helper;

import java.io.File;

/**
 * Created by mayank on 11/5/17.
 */

public class MessageAttachmentVideoViewHolder extends BaseMessageViewHolder {
    TextView text;
    TextView durationOrSize;
    ImageView videoThumbnail;
    ImageView videoPlay;
    LinearLayout ll;
    ProgressBar progressBar;
    FrameLayout videoActionFrame;

    public MessageAttachmentVideoViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        durationOrSize = itemView.findViewById(R.id.videoSize);
        videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
        videoPlay = itemView.findViewById(R.id.videoPlay);
        ll = itemView.findViewById(R.id.container);
        progressBar = itemView.findViewById(R.id.progressBar);
        videoActionFrame = itemView.findViewById(R.id.videoActionFrame);

        itemView.setOnClickListener(v -> onItemClick(true));
        itemView.setOnLongClickListener(v -> {
            onItemClick(false);
            return true;
        });
    }

    @Override
    public void setData(Message message, int position) {
        super.setData(message, position);
//        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
//        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));

        boolean loading = message.getAttachment().getUrl().equals("loading");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        videoPlay.setVisibility(loading ? View.GONE : View.VISIBLE);

        File file = Helper.getFile(context, message, isMine());

        if (!file.exists())
            durationOrSize.setText(FileUtils.getReadableFileSize(message.getAttachment().getBytesCount()));
        durationOrSize.setBackground(ContextCompat.getDrawable(context, isMine() ? R.drawable.round_white : R.drawable.round_blue_10dp));
        durationOrSize.setTextColor(ContextCompat.getColor(context, isMine() ? R.color.colorPrimary : android.R.color.white));
        durationOrSize.setVisibility(file.exists() ? View.GONE : View.VISIBLE);
        text.setText(message.getAttachment().getName());
        text.setTextColor(ContextCompat.getColor(context, isMine() ? android.R.color.white : android.R.color.black));
        Glide.with(context).load(message.getAttachment().getData()).apply(new RequestOptions().placeholder(R.drawable.ic_video_24dp).centerCrop()).into(videoThumbnail);
        videoActionFrame.setBackground(ContextCompat.getDrawable(context, isMine() ? R.drawable.round_white_more : R.drawable.round_blue_more));
        videoPlay.setImageDrawable(ContextCompat.getDrawable(context, file.exists() ? (isMine() ? R.drawable.ic_play_circle_outline_blue : R.drawable.ic_play_circle_outline_white) : (isMine() ? R.drawable.ic_file_download_40dp : R.drawable.ic_file_download_white_48dp)));
    }

}
