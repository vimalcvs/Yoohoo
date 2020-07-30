package com.verbosetech.yoohoo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.adapters.ChatAdapter;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Contact;
import com.verbosetech.yoohoo.utils.Helper;
import com.verbosetech.yoohoo.views.MyRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by a_man on 30-12-2017.
 */

public class MyChatsFragment extends Fragment {
    private MyRecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ArrayList<Chat> chatDataList = new ArrayList<>();
    private Context mContext;
    private Helper helper;
    private boolean groupChats = false;

    public static MyChatsFragment newInstance(boolean isGroup) {
        MyChatsFragment toReturn = new MyChatsFragment();
        toReturn.groupChats = isGroup;
        return toReturn;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        helper = new Helper(mContext);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        helper.saveChats(("chats_" + groupChats), chatDataList);
        mContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_recycler, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(((ImageView) view.findViewById(R.id.emptyImage)));
        TextView emptyTextView = view.findViewById(R.id.emptyText);
        emptyTextView.setText(getString(groupChats ? R.string.empty_group_chat_list : R.string.empty_text_chat_list));
        recyclerView.setEmptyTextView(emptyTextView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<Chat> savedChats = helper.getChats("chats_" + groupChats);
        //Collections.reverse(savedChats);
        chatDataList.addAll(savedChats);
        chatAdapter = new ChatAdapter(getActivity(), chatDataList);
        recyclerView.setAdapter(chatAdapter);
    }

    public void refreshUnreadIndicatorFor(String chatChild, boolean force) {
        if (mContext != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
            chatAdapter.loadLastReadIds(chatChild, force);
            chatAdapter.notifyDataSetChanged();
        }
    }

    public void addMessage(Chat chat) {
        if (mContext != null && chatAdapter != null) {
            int pos = chatDataList.indexOf(chat);
            if (pos == -1) {
                chatDataList.add(chat);
            } else {
                chatDataList.set(pos, chat);
            }
            Collections.sort(chatDataList, (one, two) -> {
                Long oneTime = Long.valueOf(one.getDateTimeStamp());
                Long twoTime = Long.valueOf(two.getDateTimeStamp());
                return twoTime.compareTo(oneTime);
            });
            refreshUnreadIndicatorFor(chat.getChatChild(), false);
        }
    }

    public void resetChatNames(HashMap<String, Contact> savedContacts) {
        if (mContext != null && chatAdapter != null) {
            for (Chat chat : chatDataList) {
                if (!chat.isGroup()) chat.setChatName(getNameById(chat.getUserId(), savedContacts));
            }
        }
    }

    private String getNameById(String senderId, HashMap<String, Contact> savedContacts) {
        String senderIDEndTrim = Helper.getEndTrim(senderId);
        if (savedContacts.containsKey(senderIDEndTrim))
            return savedContacts.get(senderIDEndTrim).getName();
        else
            return senderId;
    }

    public ArrayList<String> deleteAndGetSelectedChats() {
        ArrayList<String> chatIdsToDelete = new ArrayList<>();
        for (Chat chat : chatDataList)
            if (chat.isSelected()) chatIdsToDelete.add(chat.getChatChild());
        for (String chatIdToDelete : chatIdsToDelete) {
            int pos = -1;
            for (int i = 0; i < chatDataList.size(); i++) {
                if (chatDataList.get(i).getChatChild().equals(chatIdToDelete)) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1) {
                chatDataList.remove(pos);
                chatAdapter.notifyItemRemoved(pos);
            }
        }
        return chatIdsToDelete;
    }

    public void disableContextualMode() {
        if (chatAdapter != null)
            chatAdapter.disableContextualMode();
    }
}
