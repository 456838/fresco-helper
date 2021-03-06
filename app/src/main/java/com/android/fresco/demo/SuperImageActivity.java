package com.android.fresco.demo;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.imagepipeline.image.ImageInfo;
import com.salton123.widget.longpic.BitmapFetcher;
import com.salton123.widget.longpic.listener.IFetchResult;
import com.salton123.widget.photo.PhotoDraweeView;

import java.io.File;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/6 下午10:10
 * ModifyTime: 下午10:10
 * Description:
 */
public class SuperImageActivity extends AppCompatActivity {
    private SubsamplingScaleImageView longPicView;
    private PhotoDraweeView thumbnailView;
    private ProgressBar progressView;
    private FrameLayout rootView;
    String thumbnailUrl = "https://photo.zastatic.com/images/photo/27252/109007273/9396485883607195.png?imageMogr2/format/jpg/quality/85/thumbnail/120x120";
    final String url = "https://photo.zastatic.com/images/photo/27252/109007273/9396485883607195.png?imageMogr2/format/jpg/quality/85";
    // String url = "https://photo.zastatic.com/images/photo/27252/109007273/9396485883607195.png?imageMogr2/format/webp/quality/85";
    // String url = "http://ww3.sinaimg.cn/large/610dc034jw1f6m4aj83g9j20zk1hcww3.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_image);
        rootView = findViewById(R.id.rootView);
        progressView = findViewById(R.id.progressView);
        longPicView = findViewById(R.id.longPicView);
        longPicView.setMaxScale(10);
        longPicView.setMinScale(1);
        longPicView.setDebug(true);
        thumbnailView = findViewById(R.id.thumbnailView);
        thumbnailView.setLongPicScale(true);
        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
        builder.setUri(thumbnailUrl)
                .setOldController(thumbnailView.getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo == null) {
                            return;
                        }

                        Log.e("aa", "onFinalImageSet" + imageInfo.getWidth() + ",height=" + imageInfo.getHeight());
                        thumbnailView.update(imageInfo.getWidth(), imageInfo.getHeight());

                    }

                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        super.onIntermediateImageSet(id, imageInfo);
                        Log.e("aa", "onFinalImageSet" + imageInfo.getWidth() + ",height=" + imageInfo.getHeight());
                        thumbnailView.update(imageInfo.getWidth(), imageInfo.getHeight());

                    }
                });
        GenericDraweeHierarchy hierarchy = thumbnailView.getHierarchy();
        hierarchy.setProgressBarImage(new ProgressBarDrawable() {
            @Override
            protected boolean onLevelChange(int progress) {
                return super.onLevelChange(progress);
            }
        });
        thumbnailView.setController(builder.build());
        BitmapFetcher.downloadImage(SuperImageActivity.this, Uri.parse(url), new IFetchResult() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    Log.e("aa", "result=" + result + ",size=" + new File(result).length());
                    longPicView.setImage(ImageSource.uri(result));
                    longPicView.setVisibility(View.VISIBLE);
                    rootView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressView.setVisibility(View.GONE);
                            thumbnailView.setVisibility(View.GONE);
                            rootView.removeView(progressView);
                            rootView.removeView(thumbnailView);
                        }
                    },500);
                    longPicView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
                } else {
                    Toast.makeText(getApplicationContext(), "图片下载失败", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProgress(int progress) {
                Log.e("aa", "progress=" + progress);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Fresco.getImagePipeline().clearCaches();
    }

}
