package com.verbosetech.yoohoo.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.onesignal.OneSignal;
import com.verbosetech.yoohoo.BuildConfig;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.services.SinchService;
import com.verbosetech.yoohoo.utils.ConfirmationDialogFragment;
import com.verbosetech.yoohoo.utils.Helper;

/**
 * Created by a_man on 01-01-2018.
 */

public class OptionsFragment extends BaseFullDialogFragment {
    private static String CONFIRM_TAG = "confirmtag";
    private static String PRIVACY_TAG = "privacytag";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView userImage;
    private TextView userName, userStatus, actionBuy;
    private Helper helper;
    private SinchService.SinchServiceInterface sinchServiceInterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container);
        userImage = view.findViewById(R.id.userImage);
        userName = view.findViewById(R.id.userName);
        userStatus = view.findViewById(R.id.userStatus);
        actionBuy = view.findViewById(R.id.actionBuy);

        helper = new Helper(getContext());
        setUserDetails(helper.getLoggedInUser());

        view.findViewById(R.id.userDetailContainer).setOnClickListener(view19 -> new ProfileEditDialogFragment().show(getChildFragmentManager(), PROFILE_EDIT_TAG));
        view.findViewById(R.id.back).setOnClickListener(view18 -> {
            Helper.closeKeyboard(getContext(), view18);
            dismiss();
        });
        view.findViewById(R.id.share).setOnClickListener(view17 -> Helper.openShareIntent(getContext(), null, (getString(R.string.hey_there) + " " + getString(R.string.app_name) + "\n" + getString(R.string.download_now) + ": " + ("https://play.google.com/store/apps/details?id=" + getContext().getPackageName()))));
        view.findViewById(R.id.rate).setOnClickListener(view16 -> Helper.openPlayStore(getContext()));
        view.findViewById(R.id.contact).setOnClickListener(view15 -> Helper.openSupportMail(getContext()));
        view.findViewById(R.id.privacy).setOnClickListener(view14 -> new PrivacyPolicyDialogFragment().show(getChildFragmentManager(), PRIVACY_TAG));
        view.findViewById(R.id.logout).setOnClickListener(view13 -> {
            ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(R.string.logout_title),
                    getString(R.string.logout_message), null, null,
                    view12 -> {
                        FirebaseAuth.getInstance().signOut();
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Helper.BROADCAST_LOGOUT));
                        sinchServiceInterface.stopClient();
                        helper.logout();
                        getActivity().finish();
                    },
                    view1 -> {
                    });
            confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
        });
        if (BuildConfig.IS_DEMO) {
            actionBuy.setVisibility(View.VISIBLE);
            actionBuy.setOnClickListener(actionView -> {
                if (!TextUtils.isEmpty(BuildConfig.DEMO_ACTION_LINK)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEMO_ACTION_LINK)));
                }
            });
        }
        AdView mAdView = view.findViewById(R.id.adView);
        if (BuildConfig.ENABLE_ADMOB) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }
        return view;
    }

    public void setUserDetails(User user) {
        userName.setText(user.getName());
        userStatus.setText(user.getStatus());
        Glide.with(this).load(user.getImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(userImage);
    }

    public static OptionsFragment newInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        OptionsFragment fragment = new OptionsFragment();
        fragment.sinchServiceInterface = sinchServiceInterface;
        return fragment;
    }
}
