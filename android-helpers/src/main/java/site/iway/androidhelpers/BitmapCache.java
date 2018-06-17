package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.util.Log;
import android.util.LruCache;

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

public class BitmapCache {

    private static final String LOG_TAG = "BitmapCache";

    private static Context CONTEXT;
    private static int LOADER_COUNT;
    private static int LOADER_THREAD_PRIORITY;
    private static int MAX_RAM_USAGE_OF_SINGLE_BITMAP;
    private static int MAX_RAM_USAGE_OF_ALL_BITMAPS;
    private static Class<? extends HttpFileDownloader> DOWNLOADER_CLASS;
    private static File DOWNLOAD_DIRECTORY;

    private static LruCache<BitmapSource, Bitmap> sLruCache;
    private static LinkedBlockingDeque<BitmapRequest> sDeque;
    private static BitmapLoader[] sLoaders;

    private static class BitmapLoader extends Thread {

        private static final Map<String, CountDownLatch> DOWNLOADING_URLS = new HashMap<>();

        private BitmapRequest mWorkingRequest;
        private Rect mDecodeRect;
        private Options mDecodeOptions;

        private InputStream getInputStream(BitmapSource source) throws Exception {
            switch (source.type) {
                case BitmapSource.TYPE_URL:
                    String cacheFileName = StringHelper.md5(source.content);
                    File cacheFile = new File(DOWNLOAD_DIRECTORY, cacheFileName);
                    return new FileInputStream(cacheFile);
                case BitmapSource.TYPE_ASSET:
                    AssetManager assetManager = CONTEXT.getAssets();
                    return assetManager.open(source.content);
                case BitmapSource.TYPE_FILE:
                    return new FileInputStream(source.content);
                case BitmapSource.TYPE_RESOURCE:
                    Resources resources = CONTEXT.getResources();
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
                while (width * height * 4 > MAX_RAM_USAGE_OF_SINGLE_BITMAP) {
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
                    mWorkingRequest = sDeque.takeFirst();
                    if (mWorkingRequest.isCanceled()) {
                        continue;
                    }
                    BitmapSource source = mWorkingRequest.getSource();
                    if (source.type == BitmapSource.TYPE_URL) {
                        if (DOWNLOAD_DIRECTORY == null) {
                            throw new RuntimeException("Download directory not set.");
                        }
                        String url = source.content;
                        String cacheFileName = StringHelper.md5(url);
                        File cacheFile = new File(DOWNLOAD_DIRECTORY, cacheFileName);
                        String cacheFilePath = cacheFile.getAbsolutePath();
                        CountDownLatch countDownLatch = null;
                        HttpFileDownloader httpFileDownloader = null;
                        synchronized (DOWNLOADING_URLS) {
                            if (DOWNLOADING_URLS.containsKey(url)) {
                                countDownLatch = DOWNLOADING_URLS.get(url);
                            } else {
                                if (!cacheFile.exists()) {
                                    DOWNLOADING_URLS.put(url, new CountDownLatch(1));
                                    Constructor<? extends HttpFileDownloader> constructor =
                                            DOWNLOADER_CLASS.getConstructor(String.class, String.class);
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
                    sLruCache.put(source, bitmap);
                    mWorkingRequest.updateProgress(BitmapRequest.GET_BITMAP);
                    Log.v(LOG_TAG, "BitmapRequest with source " + source.id + " get succeed.");
                } catch (Exception e) {
                    if (mWorkingRequest != null) {
                        BitmapSource source = mWorkingRequest.getSource();
                        mWorkingRequest.updateProgress(BitmapRequest.GET_ERROR);
                        Log.e(LOG_TAG, "BitmapRequest with source " + source.id + " get failed.");
                        Log.e(LOG_TAG, Log.getStackTraceString(e));
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

    static boolean mInitialized;

    static void throwIfAlreadyInitialized() {
        if (mInitialized) {
            throw new RuntimeException("Already initialized.");
        }
    }

    public static void setContext(Context context) {
        CONTEXT = context;
    }

    public static void setLoaderCount(int count) {
        throwIfAlreadyInitialized();
        LOADER_COUNT = count;
    }

    public static void setLoaderThreadPriority(int priority) {
        throwIfAlreadyInitialized();
        LOADER_THREAD_PRIORITY = priority;
    }

    public static void setMaxRAMUsageForSingleBitmap(int usage) {
        throwIfAlreadyInitialized();
        MAX_RAM_USAGE_OF_SINGLE_BITMAP = usage;
    }

    public static void setMaxRamUsageOfAllBitmaps(int maxRamUsageOfAllBitmaps) {
        MAX_RAM_USAGE_OF_ALL_BITMAPS = maxRamUsageOfAllBitmaps;
    }

    public static void setDownloaderClass(Class<? extends HttpFileDownloader> downloaderClass) {
        DOWNLOADER_CLASS = downloaderClass;
    }

    public static void setDownloadDirectory(File downloadDirectory) {
        throwIfAlreadyInitialized();
        if (downloadDirectory.isDirectory() && downloadDirectory.exists()) {
            DOWNLOAD_DIRECTORY = downloadDirectory;
        } else {
            if (downloadDirectory.mkdirs()) {
                DOWNLOAD_DIRECTORY = downloadDirectory;
            } else {
                Log.w(LOG_TAG, "Failed to create download directory, downloader won't work.");
            }
        }
    }

    public static void setDownloadDirectory(String directoryName) {
        File cacheDir = CONTEXT.getCacheDir();
        File imageCacheDir = new File(cacheDir, directoryName);
        setDownloadDirectory(imageCacheDir);
    }

    public static void initialize() {
        throwIfAlreadyInitialized();
        if (CONTEXT == null) {
            Log.w(LOG_TAG, "Context is empty");
            throw new RuntimeException("Context must be set.");
        }
        if (LOADER_COUNT <= 0 || LOADER_COUNT > 3) {
            Log.w(LOG_TAG, "LoaderCount is invalid, use 2 for default.");
            LOADER_COUNT = 2;
        }
        if (LOADER_THREAD_PRIORITY < Thread.MIN_PRIORITY || LOADER_THREAD_PRIORITY > Thread.MAX_PRIORITY) {
            Log.w(LOG_TAG, "LoaderThreadPriority is invalid, use Thread.NORM_PRIORITY for default.");
            LOADER_THREAD_PRIORITY = Thread.NORM_PRIORITY;
        }
        if (MAX_RAM_USAGE_OF_SINGLE_BITMAP <= 0 || MAX_RAM_USAGE_OF_SINGLE_BITMAP > 8 * 1024 * 1024) {
            Log.w(LOG_TAG, "MaxRAMUsageOfSingleBitmap is invalid, use 4MB for default.");
            MAX_RAM_USAGE_OF_SINGLE_BITMAP = 4 * 1024 * 1024;
        }
        if (MAX_RAM_USAGE_OF_ALL_BITMAPS <= 0) {
            Log.w(LOG_TAG, "MaxRamUsageOfAllBitmaps is invalid, use 1/3 of HeapGrowthLimit for default.");
            MAX_RAM_USAGE_OF_ALL_BITMAPS = DeviceHelper.getHeapGrowthLimit(CONTEXT) / 5;
        }
        if (DOWNLOADER_CLASS == null) {
            Log.w(LOG_TAG, "DownloaderClass is not set, use HttpFileDownloader for default.");
            DOWNLOADER_CLASS = HttpFileDownloader.class;
        }
        if (DOWNLOAD_DIRECTORY == null) {
            Log.w(LOG_TAG, "DownloadDirectory not set or set failed, use cacheDir/BitmapCache for default.");
            setDownloadDirectory("BitmapCache");
        }
        sLruCache = new LruCache<BitmapSource, Bitmap>(MAX_RAM_USAGE_OF_ALL_BITMAPS) {
            @Override
            protected int sizeOf(BitmapSource key, Bitmap value) {
                int size = 0;
                if (key != null) {
                    size += 1024;
                }
                if (value != null) {
                    size += 1024 + value.getByteCount();
                }
                return size;
            }
        };
        sDeque = new LinkedBlockingDeque<>();
        sLoaders = new BitmapLoader[LOADER_COUNT];
        for (int i = 0; i < LOADER_COUNT; i++) {
            sLoaders[i] = new BitmapLoader();
            sLoaders[i].setPriority(LOADER_THREAD_PRIORITY);
            sLoaders[i].start();
        }
        mInitialized = true;
    }

    public static Bitmap get(BitmapSource bitmapSource) {
        return sLruCache.get(bitmapSource);
    }

    public static void requestNow(BitmapRequest bitmapRequest) {
        sDeque.addFirst(bitmapRequest);
    }

    private static final BitmapCallback EMPTY_CALLBACK = new BitmapCallback() {
        @Override
        public void onBitmapLoadProgressChange(BitmapRequest request) {
            // nothing
        }
    };

    public static void preRequest(BitmapSource bitmapSource) {
        requestNow(new BitmapRequest(bitmapSource, EMPTY_CALLBACK));
    }

}
