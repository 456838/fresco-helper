package com.salton123.widget.longpic;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.fresco.demo.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.imagepipeline.image.ImageInfo;
import com.salton123.widget.longpic.listener.IFetchResult;
import com.salton123.widget.photo.PhotoDraweeView;

import java.io.File;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/8 下午2:50
 * ModifyTime: 下午2:50
 * Description:解决超大图和超长图无法显示以及内存泄漏等问题
 */
public class LongPhotoView extends FrameLayout {
    private SubsamplingScaleImageView longPicView;
    private PhotoDraweeView thumbnailView;
    private ProgressBar progressView;

    public LongPhotoView(@NonNull Context context) {
        super(context);
        initView();
    }

    public LongPhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LongPhotoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.stub_long_photo_view, this);
        progressView = findViewById(R.id.progressView);
        longPicView = findViewById(R.id.longPicView);
        longPicView.setMaxScale(10);
        // longPicView.setDebug(true);
        thumbnailView = findViewById(R.id.thumbnailView);
        thumbnailView.setLongPicScale(true);
    }

    public void setImage(Uri thumbnailUri, Uri photoUri) {
        if (photoUri == null) {
            throw new IllegalArgumentException("photoUri can not be null");
        } else {
            if (thumbnailUri != null) {
                initThumbnailView(thumbnailUri);
            }
            initPhotoView(photoUri);
        }

    }

    public void setImage(String thumbnailUrl, String photoUrl) {
        if (TextUtils.isEmpty(photoUrl)) {
            throw new IllegalArgumentException("photoUrl can not be null");
        } else {
            if (thumbnailUrl != null) {
                initThumbnailView(Uri.parse(thumbnailUrl));
            }
            initPhotoView(Uri.parse(photoUrl));
        }
    }

    private void initThumbnailView(Uri thumbnailUri) {
        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
        builder.setUri(thumbnailUri)
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
    }

    private void initPhotoView(Uri photoUri) {
        BitmapFetcher.downloadImage(getContext(), photoUri, new IFetchResult() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    Log.e("aa", "result=" + result + ",size=" + new File(result).length());
                    longPicView.setImage(ImageSource.uri(result));
                    longPicView.setVisibility(View.VISIBLE);
                    longPicView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
                    postDelayed(clearMission, 500);
                } else {
                    Toast.makeText(getContext(), "图片下载失败", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProgress(int progress) {
                Log.e("aa", "progress=" + progress);
            }
        });
    }

    private Runnable clearMission = new Runnable() {
        @Override
        public void run() {
            progressView.setVisibility(View.GONE);
            thumbnailView.setVisibility(View.GONE);
            removeView(progressView);
            removeView(thumbnailView);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(clearMission);
    }
}
