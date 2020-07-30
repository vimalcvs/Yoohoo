package com.verbosetech.yoohoo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.adapters.MediaSummaryAdapter;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.utils.Helper;
import com.verbosetech.yoohoo.views.MyRecyclerView;

import java.util.ArrayList;

/**
 * Created by a_man on 6/30/2017.
 */

public class UserSubMediaFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private int position;
    private ArrayList<Message> messages;

    private MyRecyclerView recyclerView;

    public UserSubMediaFragment() {
        // Required empty public constructor
    }

    public static UserSubMediaFragment newInstance(int position) {
        UserSubMediaFragment fragment = new UserSubMediaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_sub_media, container, false);
        TextView emptyText = view.findViewById(R.id.emptyText);
        emptyText.setText(R.string.no_media);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(((ImageView) view.findViewById(R.id.emptyImage)));
        recyclerView.setEmptyTextView(((TextView) view.findViewById(R.id.emptyText)));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new MediaSummaryAdapter(getContext(), messages, true, new Helper(getContext()).getLoggedInUser().getId()));
        return view;
    }

    public void setAttachment(ArrayList<Message> attachments) {
        this.messages = attachments;
    }
}
