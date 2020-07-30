package com.verbosetech.yoohoo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.verbosetech.yoohoo.R;
import com.verbosetech.yoohoo.models.AttachmentTypes;
import com.verbosetech.yoohoo.models.Message;
import com.verbosetech.yoohoo.models.User;
import com.verbosetech.yoohoo.utils.Helper;

import java.io.File;

/**
 * Created by mayank on 10/5/17.
 */

public class ImageViewerActivity extends AppCompatActivity {
    private static final String MESSAGE = ImageViewerActivity.class.getPackage().getName() + ".MESSAGE";
    private static final String URL = ImageViewerActivity.class.getPackage().getName() + ".URL";


    public static Intent newMessageInstance(Context context, Message message) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(MESSAGE, message);
        return intent;
    }

    public static Intent newUrlInstance(Context context, String url) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(URL, url);
        return intent;
    }

    PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        photoView = findViewById(R.id.photo_view);

        Message message = getIntent().getParcelableExtra(MESSAGE);
        String url = getIntent().getStringExtra(URL);
        if (url != null) {
            Glide.with(this).load(url).into(photoView);
        } else {
            User userMe = new Helper(this).getLoggedInUser();
            File file = Helper.getFile(this, message, userMe.getId());
            Glide.with(this).load(file).into(photoView);
        }
    }
}
