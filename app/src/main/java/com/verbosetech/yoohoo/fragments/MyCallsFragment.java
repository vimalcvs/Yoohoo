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
import com.verbosetech.yoohoo.adapters.LogCallAdapter;
import com.verbosetech.yoohoo.models.Contact;
import com.verbosetech.yoohoo.models.LogCall;
import com.verbosetech.yoohoo.utils.Helper;
import com.verbosetech.yoohoo.views.MyRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MyCallsFragment extends Fragment {
    private LogCallAdapter chatAdapter;
    private ArrayList<LogCall> logCallDataList = new ArrayList<>();
    private HashMap<String, Contact> savedContacts;
    private Context mContext;
    private Helper helper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        helper = new Helper(mContext);
        View view = inflater.inflate(R.layout.fragment_main_recycler, container, false);
        MyRecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(((ImageView) view.findViewById(R.id.emptyImage)));
        recyclerView.setEmptyTextView(((TextView) view.findViewById(R.id.emptyText)));
        recyclerView.setEmptyText(getString(R.string.empty_log_call_list));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new LogCallAdapter(getActivity(), presetNames());
        recyclerView.setAdapter(chatAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        if (mContext != null) {
            logCallDataList.clear();
            logCallDataList.addAll(presetNames());
            chatAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<LogCall> presetNames() {
        ArrayList<LogCall> toReturn = helper.getCallLogs();
        for (LogCall logCall : toReturn)
            logCall.setUserName(getNameById(logCall.getUserId()));
        Collections.sort(toReturn, (one, two) -> {
            Long oneTime = one.getTimeUpdated();
            Long twoTime = two.getTimeUpdated();
            return twoTime.compareTo(oneTime);
        });
        return toReturn;
    }

    private String getNameById(String senderId) {
        if (savedContacts == null) savedContacts = helper.getMyContacts();
        String senderIDEndTrim = Helper.getEndTrim(senderId);
        if (savedContacts.containsKey(senderIDEndTrim))
            return savedContacts.get(senderIDEndTrim).getName();
        else
            return senderId;
    }

}
