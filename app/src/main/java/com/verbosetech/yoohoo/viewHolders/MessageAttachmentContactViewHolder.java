package com.verbosetech.yoohoo.viewHolders;

import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.OnMessageItemClick;
import com.verbosetech.yoohoo.models.Message;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.chain.ChainingTextStringParser;

/**
 * Created by mayank on 11/5/17.
 */

public class MessageAttachmentContactViewHolder extends BaseMessageViewHolder {
    TextView text;
    LinearLayout ll;

    public MessageAttachmentContactViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        ll = itemView.findViewById(R.id.container);

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
        VCard vcard = null;
        if (!TextUtils.isEmpty(message.getAttachment().getData())) {
            try {
                ChainingTextStringParser ctsp = Ezvcard.parse(message.getAttachment().getData());
                vcard = ctsp.first();
            } catch (RuntimeException ex) {
            }
        }
        text.setText((vcard != null && vcard.getFormattedName() != null) ? vcard.getFormattedName().getValue() : context.getString(R.string.contact));
        text.setTextColor(ContextCompat.getColor(context, isMine() ? android.R.color.white : android.R.color.black));
        text.setCompoundDrawablesWithIntrinsicBounds(isMine() ? R.drawable.ic_person_white_24dp : R.drawable.ic_person_blue_24dp, 0, 0, 0);
    }
}
