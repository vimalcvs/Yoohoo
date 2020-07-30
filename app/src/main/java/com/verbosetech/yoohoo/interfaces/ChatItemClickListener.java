package com.verbosetech.yoohoo.interfaces;

import android.view.View;

import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.User;

/**
 * Created by a_man on 5/10/2017.
 */

public interface ChatItemClickListener {
    void onChatItemClick(Chat chat, int position, View userImage);

    void placeCall(boolean callIsVideo, User user);
}
