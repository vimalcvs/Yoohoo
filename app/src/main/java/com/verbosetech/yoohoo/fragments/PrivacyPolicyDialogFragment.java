package com.verbosetech.yoohoo.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.verbosetech.yoohoo.R;

/**
 * Created by a_man on 01-01-2018.
 */

public class PrivacyPolicyDialogFragment extends BaseFullDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_privacy, container);
        view.findViewById(R.id.back).setOnClickListener(view1 -> dismiss());
        return view;
    }
}
