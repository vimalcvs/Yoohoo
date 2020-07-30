package com.verbosetech.yoohoo.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.interfaces.ChatItemClickListener;
import com.verbosetech.yoohoo.interfaces.ContextualModeInteractor;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.utils.Helper;

import java.util.ArrayList;

/**
 * Created by a_man on 5/10/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Chat> dataList;
    private ChatItemClickListener itemClickListener;
    private ContextualModeInteractor contextualModeInteractor;
    private ArrayList<String> lastReadIds;
    private Helper helper;
    private int selectedCount = 0;

    public ChatAdapter(Context context, ArrayList<Chat> dataList) {
        this.context = context;
        this.dataList = dataList;
        this.helper = new Helper(context);
        this.lastReadIds = new ArrayList<>();

        if (context instanceof ChatItemClickListener) {
            this.itemClickListener = (ChatItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChatItemClickListener");
        }

        if (context instanceof ContextualModeInteractor) {
            this.contextualModeInteractor = (ContextualModeInteractor) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ContextualModeInteractor");
        }
        loadLastReadIds(null, false);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void loadLastReadIds(String chatChild, boolean force) {
        if (lastReadIds == null) lastReadIds = new ArrayList<>();
        if (TextUtils.isEmpty(chatChild)) {
            if (!lastReadIds.isEmpty()) lastReadIds.clear();
        }

        if (helper != null) {
            for (Chat chat : dataList) {
                if (TextUtils.isEmpty(chatChild)) {
                    if (force) {
                        lastReadIds.add(0, chat.getLastMessageId());
                    } else {
                        String lasReadMsgId = helper.getLastRead(chat.getChatChild());
                        if (!TextUtils.isEmpty(lasReadMsgId))
                            lastReadIds.add(lasReadMsgId);
                    }
                } else {
                    if (chat.getChatChild().equals(chatChild)) {
                        if (force) {
                            lastReadIds.add(0, chat.getLastMessageId());
                        } else {
                            String lasReadMsgId = helper.getLastRead(chat.getChatChild());
                            if (!TextUtils.isEmpty(lasReadMsgId))
                                lastReadIds.add(lasReadMsgId);
                        }
                        break;
                    }
                }
            }
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView status, name, lastMessage, time;
        private ImageView image;
        private LinearLayout user_details_container;

        MyViewHolder(View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.emotion);
            name = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            lastMessage = itemView.findViewById(R.id.message);
            image = itemView.findViewById(R.id.user_image);
            user_details_container = itemView.findViewById(R.id.user_details_container);

            itemView.setOnClickListener(v -> {
                if (contextualModeInteractor.isContextualMode()) {
                    toggleSelection(dataList.get(getAdapterPosition()), getAdapterPosition());
                } else {
                    int pos = getAdapterPosition();
                    if (pos != -1) {
                        Chat chat = dataList.get(pos);
                        itemClickListener.onChatItemClick(chat, pos, image);
                    }
                }
            });
            itemView.setOnLongClickListener(view -> {
                contextualModeInteractor.enableContextualMode();
                toggleSelection(dataList.get(getAdapterPosition()), getAdapterPosition());
                return true;
            });
        }

        private void setData(Chat chat) {
            Glide.with(context).load(chat.getChatImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(image);

            name.setText(chat.getChatName());
            //name.setCompoundDrawablesWithIntrinsicBounds(0, 0, !chat.isRead() ? R.drawable.ring_blue : 0, 0);
            status.setText(chat.getChatStatus());

            time.setText(chat.getTimeDiff());
            lastMessage.setText(chat.getLastMessage());
            lastMessage.setCompoundDrawablesWithIntrinsicBounds((lastReadIds != null && lastReadIds.contains(chat.getLastMessageId())) ? 0 : R.drawable.circle_unread, 0, 0, 0);
            //lastMessage.setTextColor(ContextCompat.getColor(context, !chat.isRead() ? R.color.textColorPrimary : R.color.textColorSecondary));

            user_details_container.setBackgroundColor(ContextCompat.getColor(context, (chat.isSelected() ? R.color.bg_gray : R.color.colorIcon)));
        }
    }

    private void toggleSelection(Chat chat, int position) {
        chat.setSelected(!chat.isSelected());
        notifyItemChanged(position);

        if (chat.isSelected())
            selectedCount++;
        else
            selectedCount--;

        contextualModeInteractor.updateSelectedCount(selectedCount);
    }

    public void disableContextualMode() {
        selectedCount = 0;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).isSelected()) {
                dataList.get(i).setSelected(false);
                notifyItemChanged(i);
            }
        }
    }

}
