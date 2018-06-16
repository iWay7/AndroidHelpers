package site.iway.androidhelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;

import site.iway.javahelpers.HttpFileDownloader;

public class BitmapCache {

    static boolean IS_DEBUG_MODE;
    static Context CONTEXT;
    static int LOADER_COUNT;
    static int LOADER_THREAD_PRIORITY;
    static int MAX_RAM_USAGE_OF_SINGLE_BITMAP;
    static int MAX_RAM_USAGE_OF_ALL_BITMAPS;
    static Class<? extends HttpFileDownloader> DOWNLOADER_CLASS;
    static File DOWNLOAD_DIRECTORY;

    private static LruCache<BitmapSource, Bitmap> sLruCache;
    private static LinkedBlockingDeque<BitmapRequest> sQueue;
    private static BitmapRequestHandler[] sHandlers;

    static boolean mInitialized;

    static void throwIfAlreadyInitialized() {
        if (mInitialized) {
            throw new RuntimeException("Already initialized.");
        }
    }

    public static void setIsDebugMode(boolean debugMode) {
        throwIfAlreadyInitialized();
        IS_DEBUG_MODE = debugMode;
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
                BitmapCacheLogger.logWarn("Failed to create download directory, downloader won't work.");
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
            BitmapCacheLogger.logError("Context is empty");
            throw new RuntimeException("Context must be set.");
        }
        if (LOADER_COUNT <= 0 || LOADER_COUNT > 3) {
            BitmapCacheLogger.logWarn("LoaderCount is invalid, use 2 for default.");
            LOADER_COUNT = 2;
        }
        if (LOADER_THREAD_PRIORITY < Thread.MIN_PRIORITY || LOADER_THREAD_PRIORITY > Thread.MAX_PRIORITY) {
            BitmapCacheLogger.logWarn("LoaderThreadPriority is invalid, use Thread.NORM_PRIORITY for default.");
            LOADER_THREAD_PRIORITY = Thread.NORM_PRIORITY;
        }
        if (MAX_RAM_USAGE_OF_SINGLE_BITMAP <= 0 || MAX_RAM_USAGE_OF_SINGLE_BITMAP > 8 * 1024 * 1024) {
            BitmapCacheLogger.logWarn("MaxRAMUsageOfSingleBitmap is invalid, use 4MB for default.");
            MAX_RAM_USAGE_OF_SINGLE_BITMAP = 4 * 1024 * 1024;
        }
        if (MAX_RAM_USAGE_OF_ALL_BITMAPS <= 0) {
            BitmapCacheLogger.logWarn("MaxRamUsageOfAllBitmaps is invalid, use 1/3 of HeapGrowthLimit for default.");
            MAX_RAM_USAGE_OF_ALL_BITMAPS = DeviceHelper.getHeapGrowthLimit(CONTEXT) / 5;
        }
        if (DOWNLOADER_CLASS == null) {
            BitmapCacheLogger.logWarn("DownloaderClass is not set, use HttpFileDownloader for default.");
            DOWNLOADER_CLASS = HttpFileDownloader.class;
        }
        if (DOWNLOAD_DIRECTORY == null) {
            BitmapCacheLogger.logWarn("DownloadDirectory not set or set failed, use cacheDir/BitmapCache for default.");
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
                    size += 1024;
                    if (!value.isRecycled()) {
                        size += value.getByteCount();
                    }
                }
                return size;
            }

            @Override
            protected void entryRemoved(boolean evicted, BitmapSource key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
        sQueue = new LinkedBlockingDeque<>();
        sHandlers = new BitmapRequestHandler[BitmapCache.LOADER_COUNT];
        for (int i = 0; i < BitmapCache.LOADER_COUNT; i++) {
            sHandlers[i] = new BitmapRequestHandler(sQueue);
            sHandlers[i].setPriority(BitmapCache.LOADER_THREAD_PRIORITY);
            sHandlers[i].start();
        }
        mInitialized = true;
    }

    static void put(BitmapSource bitmapSource, Bitmap bitmap) {
        sLruCache.put(bitmapSource, bitmap);
    }

    public static Bitmap get(BitmapSource bitmapSource) {
        return sLruCache.get(bitmapSource);
    }

    public static void requestNow(BitmapRequest bitmapRequest) {
        sQueue.addFirst(bitmapRequest);
    }

    private static final BitmapCallback EMPTY_CALLBACK = new BitmapCallback() {
        @Override
        public void onBitmapLoadProgressChange(BitmapRequest request) {
            // nothing
        }
    };

    public static void preRequest(BitmapSource bitmapSource) {
        BitmapRequest bitmapRequest = new BitmapRequest(bitmapSource, EMPTY_CALLBACK);
        sQueue.addFirst(bitmapRequest);
    }

}
