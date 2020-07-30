package com.verbosetech.yoohoo.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.fragments.ChatDetailFragment;
import com.verbosetech.yoohoo.fragments.UserMediaFragment;
import com.verbosetech.yoohoo.interfaces.GroupModificationListener;
import com.verbosetech.yoohoo.interfaces.OnUserDetailFragmentInteraction;
import com.verbosetech.yoohoo.models.Attachment;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Group;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.services.SendMessageService;
import com.verbosetech.yoohoo.utils.FirebaseUploader;
import com.verbosetech.yoohoo.utils.GeneralUtils;
import com.verbosetech.yoohoo.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends BaseActivity implements OnUserDetailFragmentInteraction, ImagePickerCallback, GroupModificationListener {
    private Handler handler;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;
    private View userDetailsSummaryContainer, pickImage;
    private ImageView userImage;
    private EditText userName;
    private ArrayList<Message> mediaMessages;
    private ProgressDialog progressDialog;

    private static final String TAG_DETAIL = "TAG_DETAIL", TAG_MEDIA = "TAG_MEDIA";
    private static String EXTRA_DATA_CHAT = "extradatachat";
    private static String EXTRA_DATA_MESSAGES = "extradatamessages";
    private static String EXTRA_DATA_USER = "extradatauser";
    private static String EXTRA_DATA_GROUP = "extradatagroup";
    private static final int REQUEST_CODE_PICKER = 4321;
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 999;
    private ChatDetailFragment fragmentUserDetail;
    //private View done;

    private String pickerPath;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;

    private Chat chat;
    private User user;
    private Group group;
    private ArrayList<Message> messages;

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        userDetailsSummaryContainer = findViewById(R.id.userDetailsSummaryContainer);
        pickImage = findViewById(R.id.pickImage);
        setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_36dp);
        userImage = findViewById(R.id.expandedImage);
        userName = findViewById(R.id.user_name);
        //done = findViewById(R.id.done);

        Intent intent = getIntent();
        chat = intent.getParcelableExtra(EXTRA_DATA_CHAT);
        user = intent.getParcelableExtra(EXTRA_DATA_USER);
        group = intent.getParcelableExtra(EXTRA_DATA_GROUP);
        messages = intent.getParcelableArrayListExtra(EXTRA_DATA_MESSAGES);
        handler = new Handler();

        userImage.setOnClickListener(view -> {
            if (chat != null && !TextUtils.isEmpty(chat.getChatImage()))
                startActivity(ImageViewerActivity.newUrlInstance(mContext, chat.getChatImage()));
        });
        pickImage.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChatDetailActivity.this);
            alertDialog.setMessage(R.string.get_image_title);
            alertDialog.setPositiveButton(R.string.get_image_camera, (dialogInterface, i) -> {
                dialogInterface.dismiss();
                openImageClick();
            });
            alertDialog.setNegativeButton(R.string.get_image_gallery, (dialogInterface, i) -> {
                dialogInterface.dismiss();
                openImagePick();
            });
            alertDialog.create().show();
        });

        setupViews();
        getMediaInfo();
        loadFragment(TAG_DETAIL);
    }

    @Override
    protected void onPause() {
        if (group != null && groupRef != null && chat != null && chat.isGroup()) {
            updateGroupNameAndStatus();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onDestroy();
    }

    void openImageClick() {
        if (permissionsAvailable(permissionsCamera)) {
            cameraPicker = new CameraImagePicker(this);
            cameraPicker.shouldGenerateMetadata(true);
            cameraPicker.shouldGenerateThumbnails(true);
            cameraPicker.setImagePickerCallback(this);
            pickerPath = cameraPicker.pickImage();
        } else {
            ActivityCompat.requestPermissions(this, permissionsCamera, 47);
        }
    }

    public void openImagePick() {
        if (permissionsAvailable(permissionsStorage)) {
            imagePicker = new ImagePicker(this);
            imagePicker.shouldGenerateMetadata(true);
            imagePicker.shouldGenerateThumbnails(true);
            imagePicker.setImagePickerCallback(this);
            imagePicker.pickImage();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 36);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 36:
                if (permissionsAvailable(permissions))
                    openImagePick();
                break;
            case 47:
                if (permissionsAvailable(permissions))
                    openImageClick();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                        imagePicker.setImagePickerCallback(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.setImagePickerCallback(this);
                        cameraPicker.reinitialize(pickerPath);
                    }
                    cameraPicker.submit(data);
                    break;
            }
        }
    }

    private void userImageUploadTask(final File fileToUpload, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.uploading));
            progressDialog.setMessage(getString(R.string.just_moment));
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing())
            progressDialog.show();
        Toast.makeText(mContext, R.string.uploading, Toast.LENGTH_SHORT).show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name))
                .child("ProfileImage")
                .child(chat.getChatChild());

        FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
            @Override
            public void onUploadFail(String message) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(ChatDetailActivity.this, R.string.err_upload_img, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                chat.setChatImage(downloadUrl);
                groupRef.child(chat.getChatChild()).child("image").setValue(downloadUrl).addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChatDetailActivity.this, getString(R.string.updated), Toast.LENGTH_SHORT).show();
                    notifyGroupChange(userMe.getId() + " " + getString(R.string.changed_group_image), group.getUserIds());
                });
            }

            @Override
            public void onUploadProgress(int progress) {

            }

            @Override
            public void onUploadCancelled() {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }, storageReference);
        firebaseUploader.setReplace(true);
        firebaseUploader.uploadImage(this, fileToUpload);
    }

    private void notifyGroupChange(String body, ArrayList<String> newUserIds) {
        Message newMessage = new Message();
        newMessage.setBody(body);
        newMessage.setAttachmentType(AttachmentTypes.NONE_NOTIFICATION);
        newMessage.setAttachment(null);
        newMessage.setChatId(chat.getChatChild());
        newMessage.setDateTimeStamp(String.valueOf(System.currentTimeMillis()));
        newMessage.setSenderId(userMe.getId());
        newMessage.setSenderName(userMe.getName());
        newMessage.setSenderStatus(userMe.getStatus());
        newMessage.setSenderImage(userMe.getImage());
        newMessage.setSent(true);
        newMessage.setDelivered(false);
        newMessage.setRecipientId(user != null ? user.getId() : chat.getUserId());
        //message.setRecipientGroupIds(group != null ? new ArrayList<MyString>(group.getUserIds()) : null);
        newMessage.setRecipientName(user != null ? user.getName() : chat.getChatName());
        newMessage.setRecipientImage(user != null ? user.getImage() : chat.getChatImage());
        newMessage.setRecipientStatus(user != null ? user.getStatus() : chat.getChatStatus());

        Intent intent = new Intent(this, SendMessageService.class);
        intent.putExtra("message", newMessage);
        if (newUserIds != null) group.setUserIds(new ArrayList<>(newUserIds));
        intent.putExtra("group", group);
        intent.putExtra("chat", chat);
        startService(intent);

        onBackPressed();
    }

    private void updateGroupNameAndStatus() {
        String updatedName = userName.getText().toString().trim();
        String updatedStatus = null;
        ChatDetailFragment chatDetailFragment = (ChatDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_DETAIL);
        if (chatDetailFragment != null) {
            updatedStatus = chatDetailFragment.getGroupStatus();
        }
        if (updatedName == null || updatedStatus == null) return;
        boolean somethingUpdated = false;
        if (!TextUtils.isEmpty(updatedName.trim()) && !group.getName().equals(updatedName.trim())) {
            groupRef.child(chat.getChatChild()).child("name").setValue(updatedName.trim());
            notifyGroupChange(userMe.getId() + " " + getString(R.string.changed_group_name), group.getUserIds());
            somethingUpdated = true;
        }

        if (!TextUtils.isEmpty(updatedStatus.trim()) && !group.getStatus().equals(updatedStatus.trim())) {
            groupRef.child(chat.getChatChild()).child("status").setValue(updatedStatus.trim());
            notifyGroupChange(userMe.getId() + " " + getString(R.string.changed_group_status), group.getUserIds());
            somethingUpdated = true;
        }

        if (somethingUpdated) {
            Toast.makeText(ChatDetailActivity.this, R.string.updated, Toast.LENGTH_SHORT).show();
        }
//        if (TextUtils.isEmpty(updatedName)) {
//            Toast.makeText(this, R.string.validation_req_username, Toast.LENGTH_SHORT).show();
//        } else if (TextUtils.isEmpty(updatedStatus)) {
//            Toast.makeText(this, R.string.validation_req_status, Toast.LENGTH_SHORT).show();
//        } else {
//            groupRef.child(chat.getChatChild()).child("name").setValue(updatedName);
//            groupRef.child(chat.getChatChild()).child("status").setValue(updatedStatus);
//            Toast.makeText(ChatDetailActivity.this, R.string.updated, Toast.LENGTH_SHORT).show();
//        }
    }

    private void getMediaInfo() {
        String myId = userMe.getId();
        mediaMessages = new ArrayList<>();
        if (messages != null) {
            for (Message m : messages) {
                if (m.getAttachmentType() == AttachmentTypes.AUDIO
                        ||
                        m.getAttachmentType() == AttachmentTypes.IMAGE
                        ||
                        m.getAttachmentType() == AttachmentTypes.VIDEO
                        ||
                        m.getAttachmentType() == AttachmentTypes.DOCUMENT) {
                    if (m.getAttachmentType() != AttachmentTypes.IMAGE && !Helper.getFile(this, m, myId).exists()) {
                        continue;
                    }
                    mediaMessages.add(m);
                }
            }
        }
    }

    private void setupViews() {
        appBarLayout.post(() -> setAppBarOffset(GeneralUtils.getDisplayMetrics().widthPixels / 2));

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setTitle(" ");
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    userDetailsSummaryContainer.setVisibility(View.INVISIBLE);
                    collapsingToolbarLayout.setTitle(chat.getChatName());
                    isShow = true;
                } else if (isShow) {
                    userDetailsSummaryContainer.setVisibility(View.VISIBLE);
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        setUserData();
    }

    private void setUserData() {
//        if (user != null) {
//            userName.setCompoundDrawablesWithIntrinsicBounds(user.isOnline() ? R.drawable.ring_green : 0, 0, 0, 0);
//        }
        userName.setText(chat.getChatName());
        Glide.with(this).load(chat.getChatImage()).apply(new RequestOptions().placeholder(R.drawable.yoohoo_placeholder)).into(userImage);

        if (chat.isGroup()) {
            userName.setEnabled(true);
            //done.setVisibility(View.VISIBLE);
            pickImage.setVisibility(View.VISIBLE);
        } else {
            userName.setEnabled(false);
            //done.setVisibility(View.GONE);
            pickImage.setVisibility(View.GONE);
        }
    }

    private void loadFragment(final String fragmentTag) {
        if (getSupportFragmentManager().findFragmentByTag(fragmentTag) != null)
            return;

        handler.post(() -> {
            Fragment fragment = null;
            switch (fragmentTag) {
                case TAG_DETAIL:
                    fragment = ChatDetailFragment.newInstance(chat);
                    break;
                case TAG_MEDIA:
                    fragment = new UserMediaFragment();
                    break;
            }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frameLayout, fragment, fragmentTag);
            if (fragmentTag.equals(TAG_MEDIA)) {
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        if (getSupportFragmentManager().findFragmentByTag(TAG_DETAIL) == null)
//            loadFragment(TAG_DETAIL);
//        else
//            super.onBackPressed();
//    }

    private void setAppBarOffset(int offsetPx) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, offsetPx, new int[]{0, 0});
    }

    @Override
    public void groupModified(String message, ArrayList<String> newUserIds) {
        notifyGroupChange(userMe.getId() + " " + message, newUserIds);
    }

    @Override
    public String fetchNameById(String userId) {
        return getNameById(userId);
    }

    @Override
    public void getAttachments() {
        ChatDetailFragment fragment = ((ChatDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_DETAIL));
        if (fragment != null) {
            fragment.setupMediaSummary(mediaMessages);
        }
    }

    @Override
    public ArrayList<Message> getAttachments(int tabPos) {
        if (getSupportFragmentManager().findFragmentByTag(TAG_MEDIA) != null) {
            ArrayList<Message> toReturn = new ArrayList<>();
            switch (tabPos) {
                case 0:
                    for (Message msg : mediaMessages)
                        if (msg.getAttachmentType() == AttachmentTypes.IMAGE || msg.getAttachmentType() == AttachmentTypes.VIDEO)
                            toReturn.add(msg);
                    break;
                case 1:
                    for (Message msg : mediaMessages)
                        if (msg.getAttachmentType() == AttachmentTypes.AUDIO)
                            toReturn.add(msg);
                    break;
                case 2:
                    for (Message msg : mediaMessages)
                        if (msg.getAttachmentType() == AttachmentTypes.DOCUMENT)
                            toReturn.add(msg);
                    break;
            }
            return toReturn;
        } else
            return null;
    }

    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        if (list != null && !list.isEmpty())
            userImageUploadTask(new File(Uri.parse(list.get(0).getOriginalPath()).getPath()), AttachmentTypes.IMAGE, null);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // After Activity recreate, you need to re-intialize these
        // two values to be able to re-intialize CameraImagePicker
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void switchToMediaFragment() {
        appBarLayout.setExpanded(false, true);
        loadFragment(TAG_MEDIA);
    }

    public static Intent newIntent(Context context, Chat chat, ArrayList<Message> dataList, User user, Group group) {
        Intent intent = new Intent(context, ChatDetailActivity.class);
        intent.putExtra(EXTRA_DATA_CHAT, chat);
        intent.putExtra(EXTRA_DATA_MESSAGES, dataList);
        intent.putExtra(EXTRA_DATA_USER, user);
        intent.putExtra(EXTRA_DATA_GROUP, group);
        return intent;
    }

}
