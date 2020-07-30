package com.verbosetech.yoohoo.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.sinch.android.rtc.calling.Call;
import com.verbosetech.yoohoo.BuildConfig;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.adapters.MenuUsersRecyclerAdapter;
import com.verbosetech.yoohoo.adapters.ViewPagerAdapter;
import com.verbosetech.yoohoo.fragments.GroupCreateDialogFragment;
import com.verbosetech.yoohoo.fragments.MyCallsFragment;
import com.verbosetech.yoohoo.fragments.MyChatsFragment;
import com.verbosetech.yoohoo.fragments.OptionsFragment;
import com.verbosetech.yoohoo.fragments.ProfileEditDialogFragment;
import com.verbosetech.yoohoo.fragments.UserSelectDialogFragment;
import com.verbosetech.yoohoo.interfaces.ContextualModeInteractor;
import com.verbosetech.yoohoo.interfaces.ChatItemClickListener;
import com.verbosetech.yoohoo.interfaces.UserGroupSelectionDismissListener;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Contact;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.services.FetchMyUsersService;
import com.verbosetech.yoohoo.utils.ConfirmationDialogFragment;
import com.verbosetech.yoohoo.utils.Helper;
import com.verbosetech.yoohoo.views.SwipeControlViewPager;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends BaseActivity implements ChatItemClickListener, View.OnClickListener, ContextualModeInteractor, UserGroupSelectionDismissListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private final int CONTACTS_REQUEST_CODE = 321;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String OPTIONS_MORE = "optionsmore";
    private static String PROFILE_EDIT_TAG = "profileedittag";
    private static String GROUP_CREATE_TAG = "groupcreatedialog";
    private static String CONFIRM_TAG = "confirmtag";

    private ImageView usersImage, backImage, dialogUserImage;
    private RecyclerView menuRecyclerView;
    private SwipeRefreshLayout swipeMenuRecyclerView;
    private FlowingDrawer drawerLayout;
    private EditText searchContact;
    private TextView selectedCount;
    //private TextView invite;
    private RelativeLayout toolbarContainer, cabContainer;

    private TabLayout tabLayout;
    private SwipeControlViewPager viewPager;

    private FloatingActionButton floatingActionButton;
    private CoordinatorLayout coordinatorLayout;

    private MenuUsersRecyclerAdapter menuUsersRecyclerAdapter;
    private ArrayList<User> myUsers = new ArrayList<>();
    private ArrayList<Message> messageForwardList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;
    private DatabaseReference myInboxRef;
    private ViewPagerAdapter adapter;
    private BroadcastReceiver userReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Helper.BROADCAST_USER_ME)) {
                userUpdated();
            }
        }
    };
    private BroadcastReceiver myUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<User> myUsers = intent.getParcelableArrayListExtra("data");
            if (myUsers != null) {
                //myusers includes inviteAble users with separator tag
                helper.setMyUsers(myUsers);
                myUsersResult(myUsers);
            }
        }
    };
    private BroadcastReceiver myContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshMyContactsCache((HashMap<String, Contact>) intent.getSerializableExtra("data"));
            MyChatsFragment userChatsFragment = null, groupChatsFragment = null;
            if (adapter != null && adapter.getCount() > 1)
                userChatsFragment = ((MyChatsFragment) adapter.getItem(0));
            if (adapter != null && adapter.getCount() >= 2)
                groupChatsFragment = ((MyChatsFragment) adapter.getItem(1));
            if (userChatsFragment != null) userChatsFragment.resetChatNames(getSavedContacts());
            if (groupChatsFragment != null) groupChatsFragment.resetChatNames(getSavedContacts());
        }
    };
    private ChildEventListener chatChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (mContext != null) {
                Message newMessage = dataSnapshot.getValue(Message.class);
                if (newMessage != null && newMessage.getId() != null && newMessage.getChatId() != null) {

                    if (newMessage.getAttachmentType() == AttachmentTypes.NONE_NOTIFICATION) {
                        setNotificationMessageNames(newMessage);
                    }

                    Chat newChat = new Chat(newMessage, newMessage.getSenderId().equals(userMe.getId()));
                    if (!newChat.isGroup()) {
                        newChat.setChatName(getNameById(newChat.getUserId()));
//                            for (User user : myUsers) {
//                                if (user.getId().equals(newChat.getUserId())) {
//                                    newChat.setChatName(user.getNameToDisplay());
//                                    break;
//                                }
//                            }
                    }
                    if (adapter != null) {
                        MyChatsFragment fragment = (MyChatsFragment) adapter.getItem(newChat.isGroup() ? 1 : 0);
                        fragment.addMessage(newChat);
                    }
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (mContext != null) {
                Message updatedMessage = dataSnapshot.getValue(Message.class);
                if (updatedMessage != null && updatedMessage.getId() != null && updatedMessage.getChatId() != null) {
                    if (updatedMessage.getAttachmentType() == AttachmentTypes.NONE_NOTIFICATION) {
                        setNotificationMessageNames(updatedMessage);
                    }

                    Chat newChat = new Chat(updatedMessage, updatedMessage.getSenderId().equals(userMe.getId()));
                    if (!newChat.isGroup()) {
                        newChat.setChatName(getNameById(newChat.getUserId()));
//                            for (User user : myUsers) {
//                                if (user.getId().equals(newChat.getUserId())) {
//                                    newChat.setChatName(user.getNameToDisplay());
//                                    break;
//                                }
//                            }
                    }
                    if (adapter != null) {
                        MyChatsFragment fragment = (MyChatsFragment) adapter.getItem(newChat.isGroup() ? 1 : 0);
                        fragment.addMessage(newChat);
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.ENABLE_ADMOB) {
            MobileAds.initialize(getApplicationContext(), initializationStatus -> {
            });
        }
        initUi();
        setupMenu();

        setProfileImage();
        usersImage.setOnClickListener(this);
        backImage.setOnClickListener(this);
        //invite.setOnClickListener(this);
        findViewById(R.id.action_delete).setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.show();

        setupViewPager();
        //markOnline(true);
        updateFcmToken();
        //registerChatUpdates();

        boolean newUser = getIntent().getBooleanExtra("newUser", false);
        if (newUser && userMe != null) {
            Toast.makeText(mContext, R.string.setup_profile_msg, Toast.LENGTH_LONG).show();
            ProfileEditDialogFragment.newInstance(true).show(getSupportFragmentManager(), PROFILE_EDIT_TAG);
        }

        AdView mAdView = findViewById(R.id.adView);
        if (BuildConfig.ENABLE_ADMOB) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.registerReceiver(myContactsReceiver, new IntentFilter(Helper.BROADCAST_MY_CONTACTS));
        localBroadcastManager.registerReceiver(myUsersReceiver, new IntentFilter(Helper.BROADCAST_MY_USERS));
        localBroadcastManager.registerReceiver(userReceiver, new IntentFilter(Helper.BROADCAST_USER_ME));

        String chatChildToRefreshLastReads = helper.getChatChildToRefreshUnreadIndicatorFor();
        if (!TextUtils.isEmpty(chatChildToRefreshLastReads)) {
            if (!chatChildToRefreshLastReads.startsWith(Helper.GROUP_PREFIX)) {
                MyChatsFragment userChatsFragment = null;
                if (adapter != null && adapter.getCount() > 1)
                    userChatsFragment = ((MyChatsFragment) adapter.getItem(0));
                if (userChatsFragment != null)
                    userChatsFragment.refreshUnreadIndicatorFor(chatChildToRefreshLastReads, true);
            } else {
                MyChatsFragment groupChatsFragment = null;
                if (adapter != null && adapter.getCount() >= 2)
                    groupChatsFragment = ((MyChatsFragment) adapter.getItem(1));
                if (groupChatsFragment != null)
                    groupChatsFragment.refreshUnreadIndicatorFor(chatChildToRefreshLastReads, true);
            }
        }
        helper.clearRefreshUnreadIndicatorFor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.unregisterReceiver(myContactsReceiver);
        localBroadcastManager.unregisterReceiver(myUsersReceiver);
        localBroadcastManager.unregisterReceiver(userReceiver);
    }

    @Override
    protected void onDestroy() {
        //markOnline(false);
        if (myInboxRef != null && chatChildEventListener != null)
            myInboxRef.removeEventListener(chatChildEventListener);
        super.onDestroy();
    }

    private void registerChatUpdates() {
        if (myInboxRef == null) {
            myInboxRef = inboxRef.child(userMe.getId());
            myInboxRef.addChildEventListener(chatChildEventListener);
        }
    }

    private void initUi() {
        setContentView(R.layout.activity_main);
        usersImage = findViewById(R.id.users_image);
        menuRecyclerView = findViewById(R.id.menu_recycler_view);
        swipeMenuRecyclerView = findViewById(R.id.menu_recycler_view_swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        searchContact = findViewById(R.id.searchContact);
        //invite = findViewById(R.id.invite);
        toolbarContainer = findViewById(R.id.toolbarContainer);
        cabContainer = findViewById(R.id.cabContainer);
        selectedCount = findViewById(R.id.selectedCount);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        floatingActionButton = findViewById(R.id.addConversation);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        backImage = findViewById(R.id.back_button);
        drawerLayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        drawerLayout.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_CLOSED || newState == ElasticDrawer.STATE_CLOSING || newState == ElasticDrawer.STATE_DRAGGING_CLOSE) {
                    Log.i("onDrawerSlide", "Drawer STATE_CLOSED");
                    Helper.closeKeyboard(MainActivity.this, searchContact);
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                Log.i("onDrawerSlide", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
            }
        });
    }

    private void updateFcmToken() {
        OneSignal.addSubscriptionObserver(stateChanges -> {
            if (!stateChanges.getFrom().getSubscribed() && stateChanges.getTo().getSubscribed()) {
                usersRef.child(userMe.getId()).child("userPlayerId").setValue(stateChanges.getTo().getUserId());
                helper.setMyPlayerId(stateChanges.getTo().getUserId());
            }
        });
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        if (status != null && status.getSubscriptionStatus() != null && status.getSubscriptionStatus().getUserId() != null) {
            usersRef.child(userMe.getId()).child("userPlayerId").setValue(status.getSubscriptionStatus().getUserId());
            helper.setMyPlayerId(status.getSubscriptionStatus().getUserId());
        }
//        usersRef.child(userMe.getId()).child("userPlayerId").setValue(OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId());
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(MyChatsFragment.newInstance(false), getString(R.string.tab_title_chat));
        adapter.addFrag(MyChatsFragment.newInstance(true), getString(R.string.tab_title_group));
        adapter.addFrag(new MyCallsFragment(), getString(R.string.tab_title_call));
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (isContextualMode()) disableContextualMode();
            }

            @Override
            public void onPageSelected(int position) {
                if (isContextualMode()) disableContextualMode();
                if (position == 2) {
                    MyCallsFragment myCallsFrag = ((MyCallsFragment) adapter.getItem(2));
                    if (myCallsFrag != null) myCallsFrag.refreshList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        reduceMarginsInTabs(tabLayout, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, getResources().getDisplayMetrics()));
    }

    private void reduceMarginsInTabs(TabLayout tabLayout, int marginOffset) {
        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }

            tabLayout.requestLayout();
        }
    }

    private void setupMenu() {
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        menuUsersRecyclerAdapter = new MenuUsersRecyclerAdapter(mContext, myUsers);
        menuRecyclerView.setAdapter(menuUsersRecyclerAdapter);
        swipeMenuRecyclerView.setColorSchemeResources(R.color.colorAccent);
        swipeMenuRecyclerView.setOnRefreshListener(() -> refreshMyContacts());
        searchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                menuUsersRecyclerAdapter.getFilter().filter(editable.toString());
            }
        });

        ArrayList<User> myUsers = helper.getMyUsers();
        if (myUsers != null && !myUsers.isEmpty()) {
            myUsersResult(myUsers);
        }
        refreshMyContacts();
    }

    private void setProfileImage() {
        if (userMe != null)
            Glide.with(mContext).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(usersImage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CONTACTS_REQUEST_CODE:
                refreshMyContacts();
                break;
        }
    }

    private void refreshMyContacts() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if (!FetchMyUsersService.STARTED) {
                if (!swipeMenuRecyclerView.isRefreshing())
                    swipeMenuRecyclerView.setRefreshing(true);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            FetchMyUsersService.startMyUsersService(MainActivity.this, userMe.getId(), idToken);
                        }
                    });
                }
            }
        } else {
            FragmentManager manager = getSupportFragmentManager();
            ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(R.string.permission_contact_title),
                    getString(R.string.permission_contact_message), getString(R.string.okay), getString(R.string.no),
                    view -> {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
                    },
                    view -> {
                        finish();
                    });
            confirmationDialogFragment.show(manager, CONFIRM_TAG);
        }
    }

    @Override
    public void onBackPressed() {
        if (ElasticDrawer.STATE_CLOSED != drawerLayout.getDrawerState()) {
            drawerLayout.closeMenu(true);
        } else if (isContextualMode()) {
            disableContextualMode();
        } else if (viewPager.getCurrentItem() != 0) {
            viewPager.post(() -> viewPager.setCurrentItem(0));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CODE_CHAT_FORWARD):
                if (resultCode == Activity.RESULT_OK) {
                    //show forward dialog to choose users
                    messageForwardList.clear();
                    ArrayList<Message> temp = data.getParcelableArrayListExtra("FORWARD_LIST");
                    messageForwardList.addAll(temp);
                    userSelectDialogFragment = UserSelectDialogFragment.newUserSelectInstance(myUsers);
                    FragmentManager manager = getSupportFragmentManager();
                    Fragment frag = manager.findFragmentByTag(USER_SELECT_TAG);
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }
                    userSelectDialogFragment.show(manager, USER_SELECT_TAG);
                }
                break;
        }
    }
//
//    @Override
//    void userAdded(User value) {
//        if (value.getId().equals(userMe.getId()))
//            return;
//        int existingPos = myUsers.indexOf(value);
//        if (existingPos != -1) {
//            myUsers.remove(existingPos);
//            menuUsersRecyclerAdapter.notifyItemRemoved(existingPos);
//        }
//        myUsers.add(0, value);
//        menuUsersRecyclerAdapter.notifyItemInserted(0);
//        refreshUsers(-1);
//    }
//
//    @Override
//    void groupAdded(Group group) {
//        if (!myGroups.contains(group)) {
//            myGroups.add(group);
//            sortMyGroupsByName();
//        }
//    }


    private void userUpdated() {
        userMe = helper.getLoggedInUser();
        setProfileImage();
        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag(OPTIONS_MORE);
        if (frag != null) {
            ((OptionsFragment) frag).setUserDetails(userMe);
        }
    }

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    public void onChatItemClick(Chat chat, int position, View userImage) {
        if (chat.isGroup() && chat.isLatest()) {
            ArrayList<Message> newGroupForwardList = new ArrayList<>();
            Message newMessage = new Message();
            newMessage.setBody(getString(R.string.invitation_group));
            newMessage.setAttachmentType(AttachmentTypes.NONE_NOTIFICATION);
            newMessage.setAttachment(null);
            newGroupForwardList.add(newMessage);
            openChat(ChatActivity.newIntent(mContext, newGroupForwardList, chat), userImage);
        } else {
            openChat(ChatActivity.newIntent(mContext, messageForwardList, chat), userImage);
        }
    }

    private void openChat(Intent intent, View userImage) {
        if (ElasticDrawer.STATE_CLOSED != drawerLayout.getDrawerState()) {
            drawerLayout.closeMenu(false);
        }
        if (userImage == null) {
            userImage = usersImage;
        }

        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, userImage, "userImage");
        startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD, activityOptionsCompat.toBundle());

//        if (Build.VERSION.SDK_INT > 21) {
//            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, userImage, "userImage");
//            startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD, options.toBundle());
//        } else {
//            startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD);
//            overridePendingTransition(0, 0);
//        }

        if (userSelectDialogFragment != null)
            userSelectDialogFragment.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                drawerLayout.openMenu(true);
                break;
            case R.id.addConversation:
                switch (viewPager.getCurrentItem()) {
                    case 0:
                    case 2:
                        drawerLayout.openMenu(true);
                        break;
                    case 1:
                        GroupCreateDialogFragment.newInstance(userMe, myUsers).show(getSupportFragmentManager(), GROUP_CREATE_TAG);
                        break;
                }
                break;
            case R.id.users_image:
                if (userMe != null)
                    OptionsFragment.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                break;
//            case R.id.invite:
//                try {
//                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                    shareIntent.setType("text/plain");
//                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.invitation_title), getString(R.string.app_name)));
//                    shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.invitation_message), getString(R.string.app_name), getPackageName()));
//                    startActivity(Intent.createChooser(shareIntent, getString(R.string.invitation_share_title)));
//                } catch (Exception ignored) {
//
//                }
//                break;
            case R.id.action_delete:
                FragmentManager manager = getSupportFragmentManager();
                Fragment frag = manager.findFragmentByTag(CONFIRM_TAG);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newConfirmInstance(getString(R.string.delete_chat_title),
                        getString(R.string.delete_chat_message), null, null,
                        view -> {
                            deleteChatsFromFirebase(((MyChatsFragment) adapter.getItem(0)).deleteAndGetSelectedChats());
                            deleteChatsFromFirebase(((MyChatsFragment) adapter.getItem(1)).deleteAndGetSelectedChats());
                            disableContextualMode();
                        },
                        view -> disableContextualMode());
                confirmationDialogFragment.show(manager, CONFIRM_TAG);
                break;
        }
    }

    private void deleteChatsFromFirebase(ArrayList<String> chatIdsToDelete) {
        for (String chatChild : chatIdsToDelete) {
            myInboxRef.child(chatChild.startsWith(Helper.GROUP_PREFIX) ? chatChild : Helper.getUserIdFromChatChild(userMe.getId(), chatChild)).setValue(null);
            ArrayList<Message> msgs = helper.getMessages(chatChild);
            ArrayList<String> deletedMessages = helper.getMessagesDeleted(chatChild);
            for (Message msg : msgs)
                deletedMessages.add(msg.getId());
            helper.setMessages(chatChild, new ArrayList<>());
            helper.setMessagesDeleted(chatChild, deletedMessages);
        }
    }

    @Override
    public void placeCall(boolean callIsVideo, User user) {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(user.getId()) : getSinchServiceInterface().callUser(user.getId());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(mContext, R.string.sinch_start_error, Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                startActivity(CallScreenActivity.newIntent(mContext, user, callId, "OUT"));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }

    @Override
    public void onUserGroupSelectDialogDismiss(ArrayList<User> users) {
        messageForwardList.clear();
//        if (helper.getSharedPreferenceHelper().getBooleanPreference(Helper.GROUP_CREATE, false)) {
//            helper.getSharedPreferenceHelper().setBooleanPreference(Helper.GROUP_CREATE, false);
//            GroupCreateDialogFragment.newInstance(this, userMe, myUsers).show(getSupportFragmentManager(), GROUP_CREATE_TAG);
//        }
    }

    @Override
    public void selectionDismissed() {
        //do nothing..
    }

    private void myUsersResult(ArrayList<User> myUsers) {
        this.myUsers.clear();
        this.myUsers.addAll(myUsers);
        //refreshUsers(-1);
        menuUsersRecyclerAdapter.notifyDataSetChanged();
        swipeMenuRecyclerView.setRefreshing(false);

        registerChatUpdates();
    }

    public void disableContextualMode() {
        cabContainer.setVisibility(View.GONE);
        toolbarContainer.setVisibility(View.VISIBLE);
        ((MyChatsFragment) adapter.getItem(0)).disableContextualMode();
        ((MyChatsFragment) adapter.getItem(1)).disableContextualMode();
        viewPager.setSwipeAble(true);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void enableContextualMode() {
        cabContainer.setVisibility(View.VISIBLE);
        toolbarContainer.setVisibility(View.GONE);
        viewPager.setSwipeAble(false);
        floatingActionButton.setVisibility(View.GONE);
    }

    @Override
    public boolean isContextualMode() {
        return cabContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void updateSelectedCount(int count) {
        if (count > 0) {
            selectedCount.setText(count + " " + getString(R.string.selected_count));
        } else {
            disableContextualMode();
        }
    }

}
