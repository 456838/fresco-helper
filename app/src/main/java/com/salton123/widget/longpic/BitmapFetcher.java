package com.salton123.widget.longpic;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.fresco.helper.utils.ImageFileUtils;
import com.facebook.fresco.helper.utils.StreamTool;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.salton123.widget.longpic.listener.IFetchResult;
import com.salton123.widget.longpic.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: newSalton@outlook.com
 * Date: 2018/12/6 下午10:40
 * ModifyTime: 下午10:40
 * Description:负责预下载图片
 */
public class BitmapFetcher {

    public static void downloadImage(Context context, final Uri uri, final IFetchResult loadFileResult) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        final ImageRequest imageRequest = builder.build();
        //取缓存数据
        File localCache = getCachedFile(imageRequest);
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
                    // EncodedImage encodedImage = new EncodedImage(dataSource.getResult());
                    // String fileExtension = encodedImage.getImageFormat().getFileExtension();
                    // String fileName = encodedImage.getImageFormat().getName();
                    // int width = encodedImage.getWidth();
                    // int height = encodedImage.getHeight();
                    // Log.e("aa", "fileName=" + fileName + ",extension=" + fileExtension + ",width=" + width + ",height=" + height);
                    final File cachedFile = getCachedFile(imageRequest);
                    if (cachedFile != null && cachedFile.canRead()) {
                        loadFileResult.onResult(cachedFile.getAbsolutePath());
                    } else {
                        CloseableReference<PooledByteBuffer> imageReference = dataSource.getResult();
                        if (imageReference != null) {
                            InputStream inputStream = null;
                            FileOutputStream outputStream = null;
                            try {
                                PooledByteBuffer pooledByteBuffer = imageReference.get();
                                inputStream = new PooledByteBufferInputStream(pooledByteBuffer);
                                final String photoPath = getImageDownloadPath(ImageFileUtils.getFileName(uri.getEncodedPath()));
                                outputStream = new FileOutputStream(photoPath);
                                IOUtils.copy(inputStream, outputStream);
                                loadFileResult.onResult(photoPath);
                            } catch (Exception e) {
                                loadFileResult.onResult("");
                                e.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(inputStream);
                                IOUtils.closeQuietly(outputStream);
                                imageReference.close();
                            }
                        } else {
                            loadFileResult.onResult("");
                        }
                    }
                    dataSource.close();
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
                        loadFileResult.onResult("");
                    }

                    Throwable throwable = dataSource.getFailureCause();
                    if (throwable != null) {
                        Log.e("ImageLoader", "onFailureImpl = " + throwable.toString());
                    }
                }
            }, UiThreadImmediateExecutorService.getInstance());
        }
    }

    private static File getCachedFile(ImageRequest imageRequest) {
        FileCache mainFileCache = ImagePipelineFactory
                .getInstance()
                .getMainFileCache();
        final CacheKey cacheKey = DefaultCacheKeyFactory
                .getInstance()
                .getEncodedCacheKey(imageRequest, false); // we don't need context, but avoid null
        // http://crashes.to/s/ee10638fb31
        if (mainFileCache.hasKey(cacheKey) && mainFileCache.getResource(cacheKey) != null) {
            return ((FileBinaryResource) mainFileCache.getResource(cacheKey)).getFile();
        } else {
            return null;
        }
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

}
