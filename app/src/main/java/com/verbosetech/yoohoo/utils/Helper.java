package com.verbosetech.yoohoo.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.activities.ImageViewerActivity;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Chat;
import com.verbosetech.yoohoo.models.Contact;
import com.verbosetech.yoohoo.models.LogCall;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by a_man on 5/5/2017.
 */

public class Helper {
    public static boolean DISABLE_SPLASH_HANDLER = false;
    private static final String USER = "USER";
    private static final String USER_MUTE = "USER_MUTE";
    private static final String LOGS_CALLS = "CALL_LOGS";
    public static final String BROADCAST_MY_USERS = "com.opuslabs.yoohoo.MY_USERS";
    public static final String BROADCAST_MY_CONTACTS = "com.opuslabs.yoohoo.MY_CONTACTS";
    public static final String BROADCAST_USER_ME = "com.opuslabs.yoohoo.USER_ME";
    public static final String BROADCAST_LOGOUT = "com.opuslabs.yoohoo.services.LOGOUT";
    public static final String GROUP_CREATE = "group_create";
    public static final String GROUP_PREFIX = "group";
    public static final String USER_MY_CACHE = "usersmycache";
    public static final String CONTACTS_MY_CACHE = "contactsmycache";
    public static final String REF_CHAT = "chats";
    public static final String REF_GROUP = "groups";
    public static final String REF_INBOX = "inbox";
    public static final String REF_USERS = "users";
    public static String CURRENT_CHAT_ID;
    public static boolean CHAT_CAB = false;

    private SharedPreferenceHelper sharedPreferenceHelper;
    private Gson gson;
    private HashSet<String> muteUsersSet;
    private HashMap<String, User> myUsersNameInPhoneMap;

    public Helper(Context context) {
        sharedPreferenceHelper = new SharedPreferenceHelper(context);
        gson = new Gson();
    }

    public static String getDateTime(Long milliseconds) {
        return new SimpleDateFormat("dd MMM kk:mm", Locale.getDefault()).format(new Date(milliseconds));
    }

    public static String getTime(Long milliseconds) {
        return new SimpleDateFormat("kk:mm", Locale.getDefault()).format(new Date(milliseconds));
    }

    public static boolean isImage(Context context, String url) {
        return getMimeType(context, url).startsWith("image");
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getMimeType(Context context, String url) {
        String mimeType;
        Uri uri = Uri.parse(url);
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getChatChild(String userId, String myId) {
        //example: userId="9" and myId="5" -->> chat child = "5-9"
        String[] temp = {userId, myId};
        Arrays.sort(temp);
        return temp[0] + "-" + temp[1];
    }

    public static CharSequence timeDiff(Long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        return DateUtils.getRelativeTimeSpanString(new Date(milliseconds).getTime(), calendar.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    public static boolean areUserIdsDifferent(ArrayList<String> userIdOld, ArrayList<String> userIdsNew) {
        if (userIdOld == null || userIdsNew == null) return true;
        if (userIdOld.size() != userIdsNew.size()) return true;
        boolean diff = false;
        for (int i = 0; i < userIdOld.size(); i++) {
            if (!userIdOld.get(i).equals(userIdsNew.get(i))) {
                diff = true;
                break;
            }
        }
        return diff;
    }

    public static String getUserIdFromChatChild(String myId, String chatChild) {
        String[] idsSplit = chatChild.split("-");
        return (idsSplit.length == 2) ? idsSplit[0].equals(myId) ? idsSplit[1] : idsSplit[0] : chatChild;
    }

    public static void presentToast(Context context, String string, boolean lengthLong) {
        Toast toast = Toast.makeText(context, string, lengthLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static String getFileBase(Context context) {
        return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + context.getString(R.string.app_name);
    }

    public static File getFile(Context context, Message message, boolean isMine) {
        return new File(getFileBase(context) + "/" + AttachmentTypes.getTypeName(message.getAttachmentType()) + (isMine ? "/.sent/" : "")
                , message.getAttachment().getName());
    }

    public static File getFile(Context context, Message message, String meId) {
        return new File(getFileBase(context) + "/" + AttachmentTypes.getTypeName(message.getAttachmentType()) + (message.getSenderId().equals(meId) ? "/.sent/" : "")
                , message.getAttachment().getName());
    }

    public ArrayList<User> getMyUsers() {
        String savedUserPref = sharedPreferenceHelper.getStringPreference(USER_MY_CACHE);
        if (savedUserPref != null) {
            return gson.fromJson(savedUserPref, new TypeToken<ArrayList<User>>() {
            }.getType());
        } else {
            return new ArrayList<User>();
        }
    }

    public HashMap<String, Contact> getMyContacts() {
        String savedContactsPref = sharedPreferenceHelper.getStringPreference(CONTACTS_MY_CACHE);
        if (savedContactsPref != null) {
            return gson.fromJson(savedContactsPref, new TypeToken<HashMap<String, Contact>>() {
            }.getType());
        } else {
            return new HashMap<String, Contact>();
        }
    }

    public void setMyContacts(HashMap<String, Contact> myContacts) {
        sharedPreferenceHelper.setStringPreference(CONTACTS_MY_CACHE, gson.toJson(myContacts, new TypeToken<HashMap<String, Contact>>() {
        }.getType()));
    }

    public void setMyUsers(ArrayList<User> myUsers) {
        sharedPreferenceHelper.setStringPreference(USER_MY_CACHE, gson.toJson(myUsers, new TypeToken<ArrayList<User>>() {
        }.getType()));
    }

    public User getLoggedInUser() {
        String savedUserPref = sharedPreferenceHelper.getStringPreference(USER);
        if (savedUserPref != null)
            return gson.fromJson(savedUserPref, new TypeToken<User>() {
            }.getType());
        return null;
    }

    public void setLoggedInUser(User user) {
        sharedPreferenceHelper.setStringPreference(USER, gson.toJson(user, new TypeToken<User>() {
        }.getType()));
    }

    public void logout() {
        sharedPreferenceHelper.clearPreference(USER);
        sharedPreferenceHelper.clearPreference("chats_" + true);
        sharedPreferenceHelper.clearPreference("chats_" + false);
    }

    public boolean isLoggedIn() {
        return sharedPreferenceHelper.getStringPreference(USER) != null;
    }

    public void setUserMute(String userId, boolean mute) {
        if (muteUsersSet == null) {
            String muteUsersPref = sharedPreferenceHelper.getStringPreference(USER_MUTE);
            if (muteUsersPref != null) {
                muteUsersSet = gson.fromJson(muteUsersPref, new TypeToken<HashSet<String>>() {
                }.getType());
            } else {
                muteUsersSet = new HashSet<>();
            }
        }

        if (mute)
            muteUsersSet.add(userId);
        else
            muteUsersSet.remove(userId);

        sharedPreferenceHelper.setStringPreference(USER_MUTE, gson.toJson(muteUsersSet, new TypeToken<HashSet<String>>() {
        }.getType()));
    }

    public boolean isUserMute(String userId) {
        String muteUsersPref = sharedPreferenceHelper.getStringPreference(USER_MUTE);
        if (muteUsersPref != null) {
            HashSet<String> muteUsersSet = gson.fromJson(muteUsersPref, new TypeToken<HashSet<String>>() {
            }.getType());
            return muteUsersSet.contains(userId);
        } else {
            return false;
        }
    }

    public void setMessagesDeleted(String chatChild, ArrayList<String> dataList) {
        sharedPreferenceHelper.setStringPreference((chatChild + "_deleted"), gson.toJson(dataList, new TypeToken<ArrayList<String>>() {
        }.getType()));
    }

    public void setLastRead(String chatChild, String id) {
        sharedPreferenceHelper.setStringPreference((chatChild + "_lastread"), id);
        sharedPreferenceHelper.setStringPreference("refreshunreadindicator", chatChild);
    }

    public String getChatChildToRefreshUnreadIndicatorFor() {
        return sharedPreferenceHelper.getStringPreference("refreshunreadindicator");
    }

    public void clearRefreshUnreadIndicatorFor() {
        sharedPreferenceHelper.clearPreference("refreshunreadindicator");
    }

    public String getLastRead(String chatChild) {
        return sharedPreferenceHelper.getStringPreference((chatChild + "_lastread"));
    }

    public void setMessages(String chatChild, ArrayList<Message> dataList) {
        sharedPreferenceHelper.setStringPreference(chatChild, gson.toJson(dataList, new TypeToken<ArrayList<Message>>() {
        }.getType()));
    }

    public ArrayList<String> getMessagesDeleted(String chatChild) {
        ArrayList<String> toReturn;
        String savedInPrefs = sharedPreferenceHelper.getStringPreference(chatChild + "_deleted");
        if (savedInPrefs != null) {
            toReturn = gson.fromJson(savedInPrefs, new TypeToken<ArrayList<String>>() {
            }.getType());
        } else {
            toReturn = new ArrayList<>();
        }
        return toReturn;
    }

    public ArrayList<Message> getMessages(String chatChild) {
        ArrayList<Message> toReturn;
        String savedInPrefs = sharedPreferenceHelper.getStringPreference(chatChild);
        if (savedInPrefs != null) {
            toReturn = gson.fromJson(savedInPrefs, new TypeToken<ArrayList<Message>>() {
            }.getType());
        } else {
            toReturn = new ArrayList<>();
        }
        return toReturn;
    }

    public ArrayList<LogCall> getCallLogs() {
        ArrayList<LogCall> toReturn;
        String savedInPrefs = sharedPreferenceHelper.getStringPreference(LOGS_CALLS);
        if (savedInPrefs != null) {
            toReturn = gson.fromJson(savedInPrefs, new TypeToken<ArrayList<LogCall>>() {
            }.getType());
        } else {
            toReturn = new ArrayList<>();
        }
        return toReturn;
    }

    public void saveCallLog(LogCall logCall) {
        ArrayList<LogCall> previousLogs = getCallLogs();
        previousLogs.add(logCall);
        sharedPreferenceHelper.setStringPreference(LOGS_CALLS, gson.toJson(previousLogs, new TypeToken<ArrayList<LogCall>>() {
        }.getType()));
    }

    public void saveChats(String key, ArrayList<Chat> chats) {
        sharedPreferenceHelper.setStringPreference(key, gson.toJson(chats, new TypeToken<ArrayList<Chat>>() {
        }.getType()));
    }

    public void setMyPlayerId(String userId) {
        sharedPreferenceHelper.setStringPreference("myplayerid", userId);
    }

    public String getMyPlayerId() {
        return sharedPreferenceHelper.getStringPreference("myplayerid");
    }

    public ArrayList<Chat> getChats(String key) {
        ArrayList<Chat> toReturn;
        String savedInPrefs = sharedPreferenceHelper.getStringPreference(key);
        if (savedInPrefs != null) {
            toReturn = gson.fromJson(savedInPrefs, new TypeToken<ArrayList<Chat>>() {
            }.getType());
        } else {
            toReturn = new ArrayList<>();
        }
        return toReturn;
    }

    public static void loadUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
// create an intent builder
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
// Begin customizing
// set toolbar colors
        intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        intentBuilder.addDefaultShareMenuItem();
        intentBuilder.enableUrlBarHiding();
// build custom tabs intent
        CustomTabsIntent customTabsIntent = intentBuilder.build();
// launch the url
        customTabsIntent.launchUrl(context, uri);
    }

    public static String getEndTrim(String phoneNumber) {
        return phoneNumber != null && phoneNumber.length() >= 8 ? phoneNumber.substring(phoneNumber.length() - 7) : phoneNumber;
    }

    public SharedPreferenceHelper getSharedPreferenceHelper() {
        return sharedPreferenceHelper;
    }

    public static void openShareIntent(Context context, @Nullable View itemview, String shareText) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (itemview != null) {
                try {
                    Uri imageUri = getImageUri(context, itemview, "postBitmap.jpeg");
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (IOException e) {
                    intent.setType("text/plain");
                    e.printStackTrace();
                }
            } else {
                intent.setType("text/plain");
            }
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            context.startActivity(Intent.createChooser(intent, "Share Via:"));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, R.string.error_detail, Toast.LENGTH_SHORT).show();
        }
    }

    private static Uri getImageUri(Context context, View view, String fileName) throws IOException {
        Bitmap bitmap = loadBitmapFromView(view);
        File pictureFile = new File(context.getExternalCacheDir(), fileName);
        FileOutputStream fos = new FileOutputStream(pictureFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        fos.close();
        return Uri.parse("file://" + pictureFile.getAbsolutePath());
    }

    private static Bitmap loadBitmapFromView(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            v.setDrawingCacheEnabled(true);
            return v.getDrawingCache();
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    public static void openPlayStore(Context context) {
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void openSupportMail(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", context.getString(R.string.support_email), null));
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static void closeKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String timeFormater(float time) {
        Long secs = (long) (time / 1000);
        Long mins = (long) ((time / 1000) / 60);
        Long hrs = (long) (((time / 1000) / 60) / 60); /* Convert the seconds to String * and format to ensure it has * a leading zero when required */
        secs = secs % 60;
        String seconds = String.valueOf(secs);
        if (secs == 0) {
            seconds = "00";
        }
        if (secs < 10 && secs > 0) {
            seconds = "0" + seconds;
        } /* Convert the minutes to String and format the String */
        mins = mins % 60;
        String minutes = String.valueOf(mins);
        if (mins == 0) {
            minutes = "00";
        }
        if (mins < 10 && mins > 0) {
            minutes = "0" + minutes;
        } /* Convert the hours to String and format the String */
        String hours = String.valueOf(hrs);
        if (hrs == 0) {
            hours = "00";
        }
        if (hrs < 10 && hrs > 0) {
            hours = "0" + hours;
        }

        return hours + ":" + minutes + ":" + seconds;
//        String milliseconds = String.valueOf((long) time);
//        if (milliseconds.length() == 2) {
//            milliseconds = "0" + milliseconds;
//        }
//        if (milliseconds.length() <= 1) {
//            milliseconds = "00";
//        }
//        milliseconds = milliseconds.substring(milliseconds.length() - 3, milliseconds.length() - 2); /* Setting the timer text to the elapsed time */
    }
}
