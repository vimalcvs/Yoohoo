package com.verbosetech.yoohoo.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.ChatItemClickListener;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.LogCall;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.utils.Helper;

import java.util.ArrayList;
import java.util.Locale;

public class LogCallAdapter extends RecyclerView.Adapter<LogCallAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<LogCall> dataList;
    private ChatItemClickListener itemClickListener;

    public LogCallAdapter(Context context, ArrayList<LogCall> dataList) {
        this.context = context;
        this.dataList = dataList;

        if (context instanceof ChatItemClickListener) {
            this.itemClickListener = (ChatItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChatItemClickListener");
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_item_log_call, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImage, callTypeIcon;
        private TextView duration, userName, message;

        MyViewHolder(View itemView) {
            super(itemView);
            callTypeIcon = itemView.findViewById(R.id.callTypeIcon);
            userImage = itemView.findViewById(R.id.user_image);
            //time = itemView.findViewById(R.id.time);
            duration = itemView.findViewById(R.id.duration);
            userName = itemView.findViewById(R.id.userName);
            message = itemView.findViewById(R.id.message);

            userImage.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != -1) {
                    itemClickListener.onChatItemClick(new Chat(new User(dataList.get(pos).getUserId(), dataList.get(pos).getUserName(), dataList.get(pos).getStatus(), dataList.get(pos).getUserImage()), dataList.get(pos).getMyId()), pos, userImage);
                }
            });
            callTypeIcon.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != -1) {
                    itemClickListener.placeCall(dataList.get(pos).isVideo(), new User(dataList.get(pos).getUserId(), dataList.get(pos).getUserName(), dataList.get(pos).getStatus(), dataList.get(pos).getUserImage()));
                }
            });
        }

        public void setData(LogCall logCall) {
            Glide.with(context).load(logCall.getUserImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(userImage);
            userName.setText(logCall.getUserName());
//            time.setText(formatTimespan(logCall.getTimeDuration()));
//            time.setCompoundDrawablesWithIntrinsicBounds(logCall.isVideo() ? R.drawable.ic_videocam_dark_gray : R.drawable.ic_phone_dark_gray, 0, 0, 0);
            //time.setCompoundDrawablesWithIntrinsicBounds(logCall.isVideo() ? R.drawable.ic_videocam_dark_gray : R.drawable.ic_phone_dark_gray, 0, logCall.getStatus().equals("CANCELED") ? R.drawable.ic_call_missed : logCall.getStatus().equals("DENIED") || logCall.getStatus().equals("IN") ? R.drawable.ic_call_received : logCall.getStatus().equals("OUT") ? R.drawable.ic_call_made : 0, 0);
            callTypeIcon.setImageDrawable(ContextCompat.getDrawable(context, logCall.isVideo() ? R.drawable.ic_videocam_dark_primary_24dp : R.drawable.ic_phone_dark_primary_24dp));
            message.setText(formatTimespan(logCall.getTimeDuration()) + ", " + context.getString(logCall.getStatus().equals("CANCELED") ? R.string.missed : logCall.getStatus().equals("DENIED") || logCall.getStatus().equals("IN") ? R.string.incoming : logCall.getStatus().equals("OUT") ? R.string.outgoing : R.string.outgoing));
            duration.setText(Helper.getDateTime(logCall.getTimeUpdated()));
        }

        private String formatTimespan(int totalSeconds) {
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }
}
