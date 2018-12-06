package com.android.fresco.demo;

import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.fresco.helper.listener.IDownloadResult;
import com.facebook.fresco.helper.utils.ImageFileUtils;
import com.salton123.widget.longpic.BitmapFetcher;
import com.salton123.widget.longpic.listener.IFetchResult;

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
        String url = "http://ww3.sinaimg.cn/large/610dc034jw1f6m4aj83g9j20zk1hcww3.jpg";
        // longPicView.setImage(ImageSource.asset("longpic.png"));
        String filePath = ImageFileUtils.getImageDownloadPath(this, url);

        BitmapFetcher.downloadImage(this, Uri.parse(url), new IFetchResult() {
            @Override
            public void onResult(String result) {
                Log.e("aa","result="+result);
                longPicView.setImage(ImageSource.uri(result));
            }

            @Override
            public void onProgress(int progress) {
                Log.e("aa","progress="+progress);
            }
        });
    }
}
