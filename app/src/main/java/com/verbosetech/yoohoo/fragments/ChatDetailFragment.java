package com.verbosetech.yoohoo.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.adapters.GroupNewParticipantsAdapter;
import com.verbosetech.yoohoo.adapters.MediaSummaryAdapter;
import com.verbosetech.yoohoo.interfaces.GroupModificationListener;
import com.verbosetech.yoohoo.interfaces.OnUserDetailFragmentInteraction;
import com.verbosetech.yoohoo.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Group;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.utils.ConfirmationDialogFragment;
import com.verbosetech.yoohoo.utils.Helper;

import java.util.ArrayList;

public class ChatDetailFragment extends Fragment implements GroupNewParticipantsAdapter.ParticipantClickListener {
    private static final int CALL_REQUEST_CODE = 911;
    private OnUserDetailFragmentInteraction mListener;

    private View mediaSummaryContainer, mediaSummaryViewAll, userDetailContainer, groupDetailContainer;
    private RecyclerView mediaSummary;
    private TextView userPhone, userStatus, removeMe;
    private ImageView userPhoneClick;
    private SwitchCompat muteNotificationToggle;
    private AppCompatEditText emotion;
    private Context mContext;

    private ArrayList<Message> attachments;

    private Chat chat;
    private User userMe;
    private Group group;
    private Helper helper;

    private GroupNewParticipantsAdapter selectedParticipantsAdapter;
    private TextView participantsCount, participantsAdd;
    private ProgressBar participantsProgress;
    private ArrayList<User> groupUsers, groupNewUsers;
    private String CONFIRM_TAG = "confirm";

    private DatabaseReference groupRef, userRef;
    private ArrayList<User> myUsers;
    private GroupModificationListener groupModificationListener;

    public ChatDetailFragment() {
        // Required empty public constructor
    }

    public static ChatDetailFragment newInstance(Chat chat) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("chat", chat);
        ChatDetailFragment fragment = new ChatDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ValueEventListener groupValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Group inGroup = dataSnapshot.getValue(Group.class);
            if (mContext != null && inGroup != null && inGroup.getUserIds() != null) {
                group = inGroup;
                setParticipants();
                boolean isMember = group.getUserIds().contains(userMe.getId());
                if (!isMember && removeMe != null && participantsAdd != null) {
                    participantsAdd.setOnClickListener(null);
                    participantsAdd.setVisibility(View.GONE);
                    removeMe.setOnClickListener(null);
                    removeMe.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        groupRef.removeEventListener(groupValueEventListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        groupRef = firebaseDatabase.getReference(Helper.REF_GROUP);
        userRef = firebaseDatabase.getReference(Helper.REF_USERS);
        helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_detail, container, false);
        userDetailContainer = view.findViewById(R.id.userDetailContainer);
        groupDetailContainer = view.findViewById(R.id.groupDetailContainer);

        mediaSummaryContainer = view.findViewById(R.id.mediaSummaryContainer);
        mediaSummaryViewAll = view.findViewById(R.id.mediaSummaryAll);
        mediaSummary = view.findViewById(R.id.mediaSummary);
        emotion = view.findViewById(R.id.emotion);
        removeMe = view.findViewById(R.id.removeMe);

        if (getActivity() instanceof GroupModificationListener) {
            groupModificationListener = (GroupModificationListener) getActivity();
        } else {
            throw new RuntimeException(getActivity().toString() + " must implement GroupModificationListener");
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            chat = bundle.getParcelable("chat");
        }

        if (!chat.isGroup()) {
            userDetailContainer.setVisibility(View.VISIBLE);
            groupDetailContainer.setVisibility(View.GONE);

            userPhone = view.findViewById(R.id.userPhone);
            userPhoneClick = view.findViewById(R.id.userPhoneClick);
            userStatus = view.findViewById(R.id.userStatus);
            muteNotificationToggle = view.findViewById(R.id.muteNotificationSwitch);
        } else {
            userDetailContainer.setVisibility(View.GONE);
            groupDetailContainer.setVisibility(View.VISIBLE);

            emotion.setText(chat.getChatStatus());
            participantsCount = view.findViewById(R.id.participantsCount);
            participantsAdd = view.findViewById(R.id.participantsAdd);
            participantsProgress = view.findViewById(R.id.participantsProgress);
            RecyclerView participantsRecycler = view.findViewById(R.id.participants);
            participantsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            participantsRecycler.setNestedScrollingEnabled(false);
            groupUsers = new ArrayList<>();
            selectedParticipantsAdapter = new GroupNewParticipantsAdapter(this, groupUsers, userMe);
            participantsRecycler.setAdapter(selectedParticipantsAdapter);
            participantsAdd.setOnClickListener(view1 -> {
                if (group != null) {
                    ArrayList<User> myUsersLeftToAdd = new ArrayList<>();

                    for (User usr : myUsers) {
                        if (usr.getId().equals("-1") && usr.getName().equals("-1"))
                            break;
                        usr.setSelected(false);
                        myUsersLeftToAdd.add(usr);
                    }

                    myUsersLeftToAdd.removeAll(groupUsers);
                    if (myUsersLeftToAdd.isEmpty()) {
                        Toast.makeText(getContext(), R.string.no_members, Toast.LENGTH_SHORT).show();
                    } else {
                        groupNewUsers = new ArrayList<>();
                        GroupMembersSelectDialogFragment.newInstance(new UserGroupSelectionDismissListener() {
                            @Override
                            public void onUserGroupSelectDialogDismiss(ArrayList<User> users) {
                                if (users != null) groupNewUsers.addAll(users);
                                if (!groupNewUsers.isEmpty()) {
                                    showAddMemberConfirmationDialog();
                                }
                            }

                            @Override
                            public void selectionDismissed() {

                            }
                        }, groupNewUsers, myUsersLeftToAdd).show(getChildFragmentManager(), "selectgroupmembers");
                    }
                } else {
                    Toast.makeText(mContext, R.string.just_moment, Toast.LENGTH_SHORT).show();
                }
            });
            removeMe.setOnClickListener(v -> {
                if (groupUsers != null) {
                    int myIndex = groupUsers.indexOf(userMe);
                    if (myIndex != -1) {
                        onParticipantClick(myIndex, groupUsers.get(myIndex));
                    }
                }
            });
        }

        if (mListener != null)
            mListener.getAttachments();
        if (chat.isGroup()) {
            groupRef.child(chat.getChatChild()).addValueEventListener(groupValueEventListener);
            myUsers = helper.getMyUsers();
//            myUsers = new ArrayList<>();
//            for (User usr : helper.getMyUsers()) {
//                if (usr.getId().equals("-1") && usr.getName().equals("-1"))
//                    break;
//                myUsers.add(usr);
//            }
        } else {
            setData();
//            muteNotificationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    helper.setUserMute(user.getId(), b);
//                }
//            });
            userPhoneClick.setOnClickListener(view1 -> callPhone(true, chat.getUserId()));
        }
        mediaSummaryViewAll.setOnClickListener(v -> {
            if (mListener != null)
                mListener.switchToMediaFragment();
        });

        return view;
    }

    public String getGroupStatus() {
        return emotion != null ? emotion.getText().toString().trim() : null;
    }

    private void showAddMemberConfirmationDialog() {
        ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(R.string.add_member_title),
                getString(R.string.add_member_message), null, null,
                view -> {
                    for (User userToAdd : groupNewUsers)
                        group.getUserIds().add(userToAdd.getId());
                    groupRef.child(group.getId()).child("userIds").setValue(group.getUserIds()).addOnSuccessListener(aVoid -> {
                        if (groupModificationListener != null) {
                            groupModificationListener.groupModified(getString(R.string.added) + " " + (groupNewUsers.size() == 1 ? groupNewUsers.get(0).getId() : (groupNewUsers.size() + " " + getString(R.string.member_count))), group.getUserIds());
                        }
                        if (mListener != null) {
                            Toast.makeText(getContext(), R.string.member_added, Toast.LENGTH_SHORT).show();
                            groupNewUsers.clear();
                        }
                    });
                    groupUsers.addAll(groupNewUsers);
                    selectedParticipantsAdapter.notifyDataSetChanged();
                    participantsCount.setText(getString(R.string.participants_count) + " (" + selectedParticipantsAdapter.getItemCount() + ")");
                },
                view -> {
                });
        confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
    }

    private void setParticipants() {
        groupUsers.clear();
        participantsProgress.setVisibility(View.VISIBLE);
        for (String memberId : group.getUserIds()) {
            if (memberId.equals(userMe.getId())) {
                groupUsers.add(userMe);
            } else {
                User groupUser = new User(memberId, groupModificationListener != null ? groupModificationListener.fetchNameById(memberId) : memberId, getString(R.string.hey_there) + " " + getString(R.string.app_name), "");
                int pos = myUsers.indexOf(groupUser);
                groupUsers.add(pos != -1 ? myUsers.get(pos) : groupUser);
            }
        }
        if (group.getUserIds().size() == groupUsers.size()) {
            participantsProgress.setVisibility(View.GONE);
            selectedParticipantsAdapter.notifyDataSetChanged();
            participantsCount.setText(getString(R.string.participants_count) + " (" + selectedParticipantsAdapter.getItemCount() + ")");
        } else {
            Toast.makeText(getContext(), R.string.group_participants_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callPhone(boolean dial, String phoneNumber) {
        if (dial) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                    Toast.makeText(getContext(), R.string.dial_permission, Toast.LENGTH_LONG).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST_CODE);
                }
            } else {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
            }
        } else {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CALL_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    callPhone(true, chat.getUserId());
                else
                    callPhone(false, chat.getUserId());
                break;
        }
    }

    private void setData() {
        userStatus.setText(chat.getChatStatus());
        userPhone.setText(chat.getUserId());
        //muteNotificationToggle.setChecked(helper.isUserMute(user.getId()));
    }

    public void setupMediaSummary(ArrayList<Message> attachments) {
        if (attachments.size() > 0) {
            this.attachments = attachments;
            mediaSummaryContainer.setVisibility(View.VISIBLE);
            mediaSummary.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            mediaSummary.setAdapter(new MediaSummaryAdapter(getContext(), attachments, false, userMe.getId()));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserDetailFragmentInteraction) {
            mListener = (OnUserDetailFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserDetailFragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onParticipantClick(final int pos, final User participant) {
        if (group != null) {
            ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(participant.getId().equals(userMe.getId()) ? R.string.leave_group : R.string.remove_user_title),
                    getString(participant.getId().equals(userMe.getId()) ? R.string.remove_myself_message : R.string.remove_user_message) + (participant.getId().equals(userMe.getId()) ? "" : (" " + participant.getName())),
                    null, null,
                    view -> {
                        removeParticipant(participant.getId());
//                            groupUsers.remove(pos);
//                            selectedParticipantsAdapter.notifyItemRemoved(pos);
                    },
                    view -> {
                    });
            confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
        } else {
            Toast.makeText(mContext, R.string.just_moment, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeParticipant(String id) {
        ArrayList<String> userIds = new ArrayList<>();
        for (String userId : group.getUserIds())
            if (!userId.equals(id))
                userIds.add(userId);
        //group.setUserIds(userIds);

        groupRef.child(group.getId()).child("userIds").setValue(userIds).addOnSuccessListener(aVoid -> {
            if (groupModificationListener != null) {
                groupModificationListener.groupModified(id.equals(userMe.getId()) ? getString(R.string.left_group) : (getString(R.string.removed) + " " + id), userIds);
            }
            if (mListener != null && !id.equals(userMe.getId()))
                Toast.makeText(getContext(), R.string.member_removed, Toast.LENGTH_SHORT).show();
        });
    }

}
