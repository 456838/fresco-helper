package com.android.fresco.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.salton123.widget.longpic.LongPhotoView;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/8 下午3:22
 * ModifyTime: 下午3:22
 * Description:
 */
public class LongPhotoViewActivity extends AppCompatActivity {
    private LongPhotoView longPhotoView;
    String thumbnailUrl = "https://photo.zastatic.com/images/photo/27252/109007273/9396485883607195.png?imageMogr2/format/jpg/quality/85/thumbnail/120x120";
    final String url = "https://photo.zastatic.com/images/photo/27252/109007273/9396485883607195.png?imageMogr2/format/jpg/quality/85";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_pic_view);
        longPhotoView = findViewById(R.id.longPhotoView);
        longPhotoView.setImage(thumbnailUrl, url);
    }
}
