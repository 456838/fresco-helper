package com.android.fresco.demo;

import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.fresco.helper.listener.IDownloadResult;
import com.facebook.fresco.helper.utils.ImageFileUtils;
import com.salton123.widget.longpic.BitmapFetcher;
import com.salton123.widget.longpic.listener.IFetchResult;

import java.io.File;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/6 下午10:10
 * ModifyTime: 下午10:10
 * Description:
 */
public class SuperImageActivity extends AppCompatActivity {
    private SubsamplingScaleImageView longPicView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_image);
        longPicView = findViewById(R.id.longPicView);
        String url = "http://ww1.sinaimg.cn/mw690/005Fj2RDgw1f9mvl4pivvj30c82ougw3.jpg";
        // String url = "https://photo.zastatic.com/images/photo/27252/109007273/9396485883607195.png?imageMogr2/format/webp/quality/85";
        // String url = "http://ww3.sinaimg.cn/large/610dc034jw1f6m4aj83g9j20zk1hcww3.jpg";
        // longPicView.setImage(ImageSource.asset("longpic.png"));

        BitmapFetcher.downloadImage(this, Uri.parse(url), new IFetchResult() {
            @Override
            public void onResult(String result) {
                if (!TextUtils.isEmpty(result)) {
                    Log.e("aa", "result=" + result + ",size=" + new File(result).length());
                    longPicView.setImage(ImageSource.uri(result));
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
