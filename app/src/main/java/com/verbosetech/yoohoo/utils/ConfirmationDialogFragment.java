package com.verbosetech.yoohoo.utils;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.verbosetech.yoohoo.R;

/**
 * Created by a_man on 04-12-2017.
 */

public class ConfirmationDialogFragment extends DialogFragment {
    private View.OnClickListener yesClickListener, noClickListener;

    public ConfirmationDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_confirmation, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);

        TextView titleTextView = view.findViewById(R.id.title);
        TextView messageTextView = view.findViewById(R.id.message);
        TextView actionNo = view.findViewById(R.id.no);
        TextView actionYes = view.findViewById(R.id.yes);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String message = bundle.getString("message");
            String button_yes = bundle.getString("button_yes");
            String button_no = bundle.getString("button_no");

            titleTextView.setText(title);
            messageTextView.setText(message);
            if (!TextUtils.isEmpty(button_yes)) actionYes.setText(button_yes);
            if (!TextUtils.isEmpty(button_no)) actionNo.setText(button_no);
        }

        actionYes.setOnClickListener(view12 -> {
            dismiss();
            yesClickListener.onClick(view12);
        });
        actionNo.setOnClickListener(view1 -> {
            dismiss();
            noClickListener.onClick(view1);
        });

        return dialog;
    }

    public static ConfirmationDialogFragment newConfirmInstance(String title, String message, String actionTextPositive, String actionTextNegative, View.OnClickListener yesClickListener, View.OnClickListener noClickListener) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        bundle.putString("button_yes", actionTextPositive);
        bundle.putString("button_no", actionTextNegative);
        ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
        dialogFragment.setArguments(bundle);
        dialogFragment.yesClickListener = yesClickListener;
        dialogFragment.noClickListener = noClickListener;
        return dialogFragment;
    }
}
