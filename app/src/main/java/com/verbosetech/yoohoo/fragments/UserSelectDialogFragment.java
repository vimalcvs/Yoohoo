package com.verbosetech.yoohoo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.adapters.MenuUsersRecyclerAdapter;
import com.verbosetech.yoohoo.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.yoohoo.models.User;

import java.util.ArrayList;

/**
 * Created by a_man on 01-12-2017.
 */

public class UserSelectDialogFragment extends BaseFullDialogFragment {
    private TextView heading;
    private EditText query;
    private RecyclerView usersRecycler;
    private ArrayList<User> myUsers;

    public UserSelectDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_select, container);
        heading = view.findViewById(R.id.heading);
        query = view.findViewById(R.id.searchQuery);
        usersRecycler = view.findViewById(R.id.usersRecycler);
        view.findViewById(R.id.close).setOnClickListener(view1 -> dismiss());

        if (getActivity() instanceof UserGroupSelectionDismissListener) {
            dismissListener = (UserGroupSelectionDismissListener) getActivity();
        } else {
            throw new RuntimeException(getActivity().toString() + " must implement UserGroupSelectionDismissListener");
        }

        myUsers = new ArrayList<>();
        myUsers.addAll(getArguments().getParcelableArrayList("users"));

        usersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecycler.setAdapter(new MenuUsersRecyclerAdapter(getActivity(), myUsers));
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (usersRecycler.getAdapter() instanceof MenuUsersRecyclerAdapter) {
                    ((MenuUsersRecyclerAdapter) usersRecycler.getAdapter()).getFilter().filter(editable.toString());
                }
            }
        });

        return view;
    }

    public void refreshUsers(int pos) {
        if (pos == -1) {
            query.setText("");
            usersRecycler.getAdapter().notifyDataSetChanged();
        } else {
            usersRecycler.getAdapter().notifyItemChanged(pos);
        }
    }

    public static UserSelectDialogFragment newUserSelectInstance(ArrayList<User> myUsers) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("users", myUsers);

        UserSelectDialogFragment dialogFragment = new UserSelectDialogFragment();
        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }
}
