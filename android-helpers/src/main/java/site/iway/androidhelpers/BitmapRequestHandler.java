package site.iway.androidhelpers;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import site.iway.javahelpers.HttpFileDownloader;
import site.iway.javahelpers.StringHelper;

class BitmapRequestHandler extends Thread {

    private static final Map<String, CountDownLatch> DOWNLOADING_URLS = new HashMap<>();

    private LinkedBlockingDeque<BitmapRequest> mQueue;

    public BitmapRequestHandler(LinkedBlockingDeque<BitmapRequest> queue) {
        mQueue = queue;
    }

    private BitmapRequest mWorkingRequest;
    private Rect mDecodeRect;
    private Options mDecodeOptions;

    private InputStream getInputStream(BitmapSource source) throws Exception {
        switch (source.type) {
            case BitmapSource.TYPE_URL:
                String cacheFileName = StringHelper.md5(source.content);
                File cacheFile = new File(BitmapCache.DOWNLOAD_DIRECTORY, cacheFileName);
                return new FileInputStream(cacheFile);
            case BitmapSource.TYPE_ASSET:
                AssetManager assetManager = BitmapCache.CONTEXT.getAssets();
                return assetManager.open(source.content);
            case BitmapSource.TYPE_FILE:
                return new FileInputStream(source.content);
            case BitmapSource.TYPE_RESOURCE:
                Resources resources = BitmapCache.CONTEXT.getResources();
                int resourceId = Integer.parseInt(source.content);
                return resources.openRawResource(resourceId);
            default:
                return null;
        }
    }

    private void prepareDecode(BitmapSource source) throws Exception {
        mDecodeOptions = new Options();
        mDecodeOptions.inJustDecodeBounds = true;
        InputStream inputStream = getInputStream(source);
        try {
            BitmapFactory.decodeStream(inputStream, mDecodeRect, mDecodeOptions);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // nothing
                }
            }
        }
        if (mDecodeOptions.outWidth <= 0 || mDecodeOptions.outHeight <= 0) {
            throw new RuntimeException("Try decode size failed.");
        } else {
            int width = mDecodeOptions.outWidth;
            int height = mDecodeOptions.outHeight;
            int scale = 1;
            while (width * height * 4 > BitmapCache.MAX_RAM_USAGE_OF_SINGLE_BITMAP) {
                scale *= 2;
                width /= 2;
                height /= 2;
            }
            mDecodeOptions.inSampleSize = scale;
            mDecodeOptions.inJustDecodeBounds = false;
        }
    }

    private Bitmap doDecode(BitmapSource source) throws Exception {
        InputStream inputStream = getInputStream(source);
        try {
            return BitmapFactory.decodeStream(inputStream, mDecodeRect, mDecodeOptions);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // nothing
                }
            }
        }
    }

    public void run() {
        while (true) {
            try {
                mWorkingRequest = mQueue.takeFirst();
                if (mWorkingRequest.isCanceled()) {
                    continue;
                }
                BitmapSource source = mWorkingRequest.getSource();
                if (source.type == BitmapSource.TYPE_URL) {
                    if (BitmapCache.DOWNLOAD_DIRECTORY == null) {
                        throw new RuntimeException("Download directory not set.");
                    }
                    String url = source.content;
                    String cacheFileName = StringHelper.md5(url);
                    File cacheFile = new File(BitmapCache.DOWNLOAD_DIRECTORY, cacheFileName);
                    String cacheFilePath = cacheFile.getAbsolutePath();
                    CountDownLatch countDownLatch = null;
                    HttpFileDownloader httpFileDownloader = null;
                    synchronized (DOWNLOADING_URLS) {
                        if (DOWNLOADING_URLS.containsKey(url)) {
                            countDownLatch = DOWNLOADING_URLS.get(url);
                        } else {
                            if (!cacheFile.exists()) {
                                DOWNLOADING_URLS.put(url, new CountDownLatch(1));
                                Class<? extends HttpFileDownloader> downloaderClass = BitmapCache.DOWNLOADER_CLASS;
                                Constructor<? extends HttpFileDownloader> constructor = downloaderClass.getConstructor(String.class, String.class);
                                httpFileDownloader = constructor.newInstance(url, cacheFilePath);
                            }
                        }
                    }
                    if (countDownLatch != null) {
                        countDownLatch.await();
                    }
                    if (httpFileDownloader != null) {
                        httpFileDownloader.start();
                        httpFileDownloader.join();
                        synchronized (DOWNLOADING_URLS) {
                            DOWNLOADING_URLS.get(url).countDown();
                            DOWNLOADING_URLS.remove(url);
                        }
                    }
                    if (!cacheFile.exists()) {
                        throw new IOException("Download failed.");
                    }
                }
                mWorkingRequest.updateProgress(BitmapRequest.PREPARING);
                prepareDecode(source);
                mWorkingRequest.updateProgress(BitmapRequest.DECODING);
                Bitmap bitmap = doDecode(source);
                if (bitmap == null) {
                    throw new RuntimeException("Decode failed.");
                }
                if (source.filter != null) {
                    mWorkingRequest.updateProgress(BitmapRequest.FILTERING);
                    bitmap = source.filter.filter(bitmap);
                    if (bitmap == null) {
                        throw new RuntimeException("Filter failed.");
                    }
                }
                BitmapCache.put(source, bitmap);
                mWorkingRequest.updateProgress(BitmapRequest.GET_BITMAP);
                BitmapCacheLogger.logVerbose("BitmapRequest with source " + source.id + " get succeed.");
            } catch (Exception e) {
                if (mWorkingRequest != null) {
                    BitmapSource source = mWorkingRequest.getSource();
                    mWorkingRequest.updateProgress(BitmapRequest.GET_ERROR);
                    BitmapCacheLogger.logError("BitmapRequest with source " + source.id + " get failed.");
                    BitmapCacheLogger.logError(e);
                }
            } finally {
                if (mWorkingRequest != null) {
                    BitmapSource source = mWorkingRequest.getSource();
                    if (source.type == BitmapSource.TYPE_URL) {
                        String url = source.content;
                        synchronized (DOWNLOADING_URLS) {
                            if (DOWNLOADING_URLS.containsKey(url)) {
                                DOWNLOADING_URLS.get(url).countDown();
                                DOWNLOADING_URLS.remove(url);
                            }
                        }
                    }
                    mWorkingRequest = null;
                    mDecodeRect = null;
                    mDecodeOptions = null;
                }
            }
        }
    }
}
