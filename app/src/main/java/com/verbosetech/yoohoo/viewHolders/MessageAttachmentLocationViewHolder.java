package com.verbosetech.yoohoo.viewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.OnMessageItemClick;
import com.verbosetech.yoohoo.models.Message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mayank on 11/5/17.
 */

public class MessageAttachmentLocationViewHolder extends BaseMessageViewHolder {
    TextView text;
    ImageView locationImage;
    LinearLayout ll;

    String staticMap = "https://maps.googleapis.com/maps/api/staticmap?center=%s,%s&zoom=16&size=512x512&format=jpg";
    String latitude, longitude;

    public MessageAttachmentLocationViewHolder(View itemView, OnMessageItemClick itemClickListener) {
        super(itemView, itemClickListener);
        text = itemView.findViewById(R.id.text);
        locationImage = itemView.findViewById(R.id.locationImage);
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
        try {
            JSONObject placeData = new JSONObject(message.getAttachment().getData());
            text.setText(placeData.getString("address"));
            latitude = placeData.getString("latitude");
            longitude = placeData.getString("longitude");
            Glide.with(context).load(String.format(staticMap, latitude, longitude)).into(locationImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        cardView.setCardBackgroundColor(ContextCompat.getColor(context, message.isSelected() ? R.color.colorPrimary : R.color.colorBgLight));
//        ll.setBackgroundColor(message.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : isMine() ? Color.WHITE : ContextCompat.getColor(context, R.color.colorBgLight));
    }
}
