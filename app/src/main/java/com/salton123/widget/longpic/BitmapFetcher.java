package com.salton123.widget.longpic;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.fresco.helper.listener.IDownloadResult;
import com.facebook.fresco.helper.utils.ImageFileUtils;
import com.facebook.fresco.helper.utils.StreamTool;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.salton123.widget.longpic.listener.IFetchResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/6 下午10:40
 * ModifyTime: 下午10:40
 * Description:负责预下载图片
 */
public class BitmapFetcher {
    /**
     * 从网络下载图片
     * 1、根据提供的图片URL，获取图片数据流
     * 2、将得到的数据流写入指定路径的本地文件
     *
     * @param url            URL
     * @param loadFileResult LoadFileResult
     */
    public static void downloadImage(Context context, final String url, final IFetchResult loadFileResult) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Uri uri = Uri.parse(url);
        final String photoPath = getImageDownloadPath(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest imageRequest = builder.build();

        // 获取未解码的图片数据
        DataSource<CloseableReference<PooledByteBuffer>> dataSource = imagePipeline.fetchEncodedImage(imageRequest, context);
        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
            @Override
            public void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                if (!dataSource.isFinished() || loadFileResult == null) {
                    Log.e("aa", "");
                    return;
                }
                EncodedImage encodedImage = new EncodedImage(dataSource.getResult());
                String fileExtension = encodedImage.getImageFormat().getFileExtension();
                String fileName = encodedImage.getImageFormat().getName();
                int width = encodedImage.getWidth();
                int height = encodedImage.getHeight();
                Log.e("aa", "fileName=" + fileName + ",extension=" + fileExtension + ",width=" + width + ",height=" + height);
                CloseableReference<PooledByteBuffer> imageReference = dataSource.getResult();
                if (imageReference != null) {
                    final CloseableReference<PooledByteBuffer> closeableReference = imageReference.clone();
                    try {
                        PooledByteBuffer pooledByteBuffer = closeableReference.get();
                        InputStream inputStream = new PooledByteBufferInputStream(pooledByteBuffer);

                        byte[] data = StreamTool.read(inputStream);
                        StreamTool.write(photoPath, data);
                        loadFileResult.onResult(photoPath);
                    } catch (IOException e) {
                        loadFileResult.onResult(null);
                        e.printStackTrace();
                    } finally {
                        imageReference.close();
                        closeableReference.close();
                    }
                }
            }

            @Override
            public void onProgressUpdate(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                int progress = (int) (dataSource.getProgress() * 100);
                if (loadFileResult != null) {
                    loadFileResult.onProgress(progress);
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                if (loadFileResult != null) {
                    loadFileResult.onResult(null);
                }

                Throwable throwable = dataSource.getFailureCause();
                if (throwable != null) {
                    Log.e("ImageLoader", "onFailureImpl = " + throwable.toString());
                }
            }
        }, CallerThreadExecutor.getInstance());
    }

    public static String getImageDownloadPath(String url) {
        if (url.startsWith("/")) {
            return url;
        }

        String fileName = ImageFileUtils.getFileName(url);
        String imageRootDir = LongPicIncubator.getSavePath();
        File dir = new File(imageRootDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir + File.separator + fileName;
    }


    public static void downloadImage(Context context, final Uri uri, final IFetchResult loadFileResult) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest imageRequest = builder.build();
        //取缓存数据
        File localCache = getChacheFile(imageRequest);
        if (localCache != null && localCache.exists()) {
            loadFileResult.onResult(localCache.getAbsolutePath());
        } else {
            // 获取未解码的图片数据
            DataSource<CloseableReference<PooledByteBuffer>> dataSource = imagePipeline.fetchEncodedImage(imageRequest, context);
            dataSource.subscribe(new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
                @Override
                public void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                    if (!dataSource.isFinished() || loadFileResult == null) {
                        Log.e("aa", "");
                        return;
                    }
                    EncodedImage encodedImage = new EncodedImage(dataSource.getResult());
                    String fileExtension = encodedImage.getImageFormat().getFileExtension();
                    String fileName = encodedImage.getImageFormat().getName();
                    int width = encodedImage.getWidth();
                    int height = encodedImage.getHeight();
                    Log.e("aa", "fileName=" + fileName + ",extension=" + fileExtension + ",width=" + width + ",height=" + height);
                    CloseableReference<PooledByteBuffer> imageReference = dataSource.getResult();
                    if (imageReference != null) {
                        final CloseableReference<PooledByteBuffer> closeableReference = imageReference.clone();
                        try {
                            PooledByteBuffer pooledByteBuffer = closeableReference.get();
                            InputStream inputStream = new PooledByteBufferInputStream(pooledByteBuffer);

                            byte[] data = StreamTool.read(inputStream);
                            // StreamTool.write(photoPath, data);
                            loadFileResult.onResult(fileName);
                        } catch (IOException e) {
                            loadFileResult.onResult(null);
                            e.printStackTrace();
                        } finally {
                            imageReference.close();
                            closeableReference.close();
                        }
                    }
                }

                @Override
                public void onProgressUpdate(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                    int progress = (int) (dataSource.getProgress() * 100);
                    if (loadFileResult != null) {
                        loadFileResult.onProgress(progress);
                    }
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    if (loadFileResult != null) {
                        loadFileResult.onResult(null);
                    }

                    Throwable throwable = dataSource.getFailureCause();
                    if (throwable != null) {
                        Log.e("ImageLoader", "onFailureImpl = " + throwable.toString());
                    }
                }
            }, CallerThreadExecutor.getInstance());
        }
    }

    private static File getChacheFile(ImageRequest imageRequest) {
        FileCache mainFileCache = ImagePipelineFactory
                .getInstance()
                .getMainFileCache();
        final CacheKey cacheKey = DefaultCacheKeyFactory
                .getInstance()
                .getEncodedCacheKey(imageRequest, false); // we don't need context, but avoid null
        File cacheFile = imageRequest.getSourceFile();
        // http://crashes.to/s/ee10638fb31
        if (mainFileCache.hasKey(cacheKey) && mainFileCache.getResource(cacheKey) != null) {
            cacheFile = ((FileBinaryResource) mainFileCache.getResource(cacheKey)).getFile();
        }
        return cacheFile;
    }
}
