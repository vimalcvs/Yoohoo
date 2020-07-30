package com.verbosetech.yoohoo.viewHolders;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.OnMessageItemClick;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.utils.GeneralUtils;
import com.verbosetech.yoohoo.utils.Helper;

import static com.verbosetech.yoohoo.adapters.MessageAdapter.OTHER;

/**
 * Created by mayank on 11/5/17.
 */

public class BaseMessageViewHolder extends RecyclerView.ViewHolder {
    protected static int lastPosition;
    public static boolean animate;
    protected static View newMessageView;
    private int attachmentType;
    protected Context context;

    private static int _48dpInPx = -1;
    private Message message;
    private OnMessageItemClick itemClickListener;

    TextView time, senderName;
    CardView cardView;
    LinearLayout container;

    public BaseMessageViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView);
        if (itemClickListener != null)
            this.itemClickListener = itemClickListener;
        context = itemView.getContext();
        time = itemView.findViewById(R.id.time);
        senderName = itemView.findViewById(R.id.senderName);
        cardView = itemView.findViewById(R.id.card_view);
        container = itemView.findViewById(R.id.container);
        if (_48dpInPx == -1) _48dpInPx = GeneralUtils.dpToPx(itemView.getContext(), 48);
    }

    public BaseMessageViewHolder(View itemView, int attachmentType, OnMessageItemClick itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
        this.attachmentType = attachmentType;
    }

    public BaseMessageViewHolder(View itemView, View newMessage, OnMessageItemClick itemClickListener) {
        this(itemView, itemClickListener);
        this.itemClickListener = itemClickListener;
        if (newMessageView == null) newMessageView = newMessage;
    }

    protected boolean isMine() {
        return (getItemViewType() & OTHER) != OTHER;
    }

    public void setData(Message message, int position) {
        this.message = message;
        if (attachmentType == AttachmentTypes.NONE_TYPING)
            return;
        time.setText(message.getTimeDiff());
        if (!message.getChatId().startsWith(Helper.GROUP_PREFIX) || message.getAttachmentType() == AttachmentTypes.NONE_NOTIFICATION) {
            senderName.setVisibility(View.GONE);
        } else {
            senderName.setText(message.getSenderName());
            senderName.setVisibility(View.VISIBLE);
        }

        senderName.setTextColor(ContextCompat.getColor(context, isMine() ? R.color.card_msg_sender_color : R.color.card_msg_color));
        senderName.setGravity(isMine() ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams senderParams = (LinearLayout.LayoutParams) senderName.getLayoutParams();
        senderParams.gravity = isMine() ? Gravity.END : Gravity.START;
        senderName.setLayoutParams(senderParams);
        time.setTextColor(ContextCompat.getColor(context, isMine() ? R.color.card_msg_time_color_me : R.color.card_msg_time_color));
        time.setCompoundDrawablesWithIntrinsicBounds(0, 0, isMine() ? (message.isSent() ? (message.isDelivered() ? R.drawable.ic_done_all_blue : R.drawable.ic_done_blue) : R.drawable.ic_waiting_blue) : 0, 0);
        LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) time.getLayoutParams();
        timeParams.gravity = isMine() ? Gravity.END : Gravity.START;
        time.setLayoutParams(timeParams);

        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.getAttachmentType() == AttachmentTypes.NONE_NOTIFICATION ? R.color.card_notification_color_background : isMine() ? R.color.card_msg_color_background : android.R.color.white));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cardView.getLayoutParams();
        if (message.getAttachmentType() == AttachmentTypes.NONE_NOTIFICATION) {
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        } else if (isMine()) {
            layoutParams.gravity = Gravity.END;
            layoutParams.leftMargin = _48dpInPx;
        } else {
            layoutParams.gravity = Gravity.START;
            layoutParams.rightMargin = _48dpInPx;
            //itemView.startAnimation(AnimationUtils.makeInAnimation(itemView.getContext(), true));
        }
        cardView.setLayoutParams(layoutParams);
        container.setBackgroundResource(message.isSelected() ? (isMine() ? R.drawable.gradient_selected_right : R.drawable.gradient_selected_left) : 0);
    }

    void onItemClick(boolean b) {
        if (itemClickListener != null && message != null) {
            if (b)
                itemClickListener.OnMessageClick(message, getAdapterPosition());
            else
                itemClickListener.OnMessageLongClick(message, getAdapterPosition());
        }
    }

}
