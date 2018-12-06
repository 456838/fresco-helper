package com.salton123.widget.longpic;

import android.app.Application;
import android.os.Environment;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/6 下午10:41
 * ModifyTime: 下午10:41
 * Description:
 */
public class LongPicIncubator {

    public static String getSavePath() {
        return Environment.getExternalStorageDirectory() + File.separator + "longpic";
    }

    public void init(Application application) {
        Fresco.initialize(application);
    }

    public static class Builder {
        private Application mApplication;
        private String mImageSavePath;

        public Builder(Application application) {

        }

    }
}
