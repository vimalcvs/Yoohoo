package com.verbosetech.yoohoo.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.adapters.GroupNewParticipantsAdapter;
import com.verbosetech.yoohoo.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.yoohoo.models.User;

import java.util.ArrayList;

/**
 * Created by a_man on 31-12-2017.
 */

public class GroupMembersSelectDialogFragment extends BaseFullDialogFragment implements GroupNewParticipantsAdapter.ParticipantClickListener {
    private ArrayList<User> selectedUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_select_members, container);
        //RecyclerView participants = view.findViewById(R.id.participants);
        RecyclerView myUsersRecycler = view.findViewById(R.id.myUsers);

        selectedUsers = new ArrayList<>();
        ArrayList<User> myUsers = new ArrayList<>();
        Bundle arguments = getArguments();
        if (arguments != null) {
            ArrayList<User> users_selected = arguments.getParcelableArrayList("users_selected");
            if (users_selected != null) selectedUsers.addAll(users_selected);
            ArrayList<User> users = arguments.getParcelableArrayList("users");
            if (users != null) myUsers.addAll(users);
        }

        //participants.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        myUsersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        GroupNewParticipantsAdapter selectedParticipantsAdapter = new GroupNewParticipantsAdapter(this, selectedUsers);
        //participants.setAdapter(selectedParticipantsAdapter);
        myUsersRecycler.setAdapter(new GroupNewParticipantsAdapter(this, myUsers, selectedParticipantsAdapter));

        view.findViewById(R.id.back).setOnClickListener(view12 -> dismiss());
        view.findViewById(R.id.done).setOnClickListener(view1 -> dismiss());

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dismissListener != null) dismissListener.onUserGroupSelectDialogDismiss(selectedUsers);
        super.onDismiss(dialog);
    }

    public static GroupMembersSelectDialogFragment newInstance(UserGroupSelectionDismissListener dismissListener, ArrayList<User> selectedUsers, ArrayList<User> myUsers) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("users_selected", selectedUsers);
        bundle.putParcelableArrayList("users", myUsers);

        GroupMembersSelectDialogFragment fragment = new GroupMembersSelectDialogFragment();
        fragment.setArguments(bundle);

        fragment.dismissListener = dismissListener;
        return fragment;
    }

    @Override
    public void onParticipantClick(int pos, User participant) {

    }
}
