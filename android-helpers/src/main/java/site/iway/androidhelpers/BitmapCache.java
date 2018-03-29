package site.iway.androidhelpers;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

public class BitmapCache {

    static boolean IS_DEBUG_MODE;
    static int LOADER_COUNT;
    static int MAX_RAM_USAGE;
    static int MAX_RAM_USAGE_OF_SINGLE_BITMAP;
    static int LOADER_THREAD_PRIORITY;
    static int URL_CONNECT_TIMEOUT;
    static int URL_READ_TIMEOUT;
    static int URL_RETRY_COUNT;
    static String DOWNLOAD_DIRECTORY;

    static boolean mInitialized;
    static BitmapLoader[] mLoaders;

    static void throwIfAlreadyInitialized() {
        if (mInitialized) {
            throw new RuntimeException("Already initialized.");
        }
    }

    public static void setIsDebugMode(boolean debugMode) {
        throwIfAlreadyInitialized();
        IS_DEBUG_MODE = debugMode;
    }

    public static void setLoaderCount(int count) {
        throwIfAlreadyInitialized();
        LOADER_COUNT = count;
    }

    public static void setMaxRAMUsage(int usage) {
        throwIfAlreadyInitialized();
        MAX_RAM_USAGE = usage;
    }

    public static void setMaxRAMUsageForSingleBitmap(int usage) {
        throwIfAlreadyInitialized();
        MAX_RAM_USAGE_OF_SINGLE_BITMAP = usage;
    }

    public static void setLoaderThreadPriority(int priority) {
        throwIfAlreadyInitialized();
        LOADER_THREAD_PRIORITY = priority;
    }

    public static void setUrlConnectTimeout(int timeout) {
        throwIfAlreadyInitialized();
        URL_CONNECT_TIMEOUT = timeout;
    }

    public static void setUrlReadTimeout(int timeout) {
        throwIfAlreadyInitialized();
        URL_READ_TIMEOUT = timeout;
    }

    public static void setUrlRetryCount(int count) {
        throwIfAlreadyInitialized();
        URL_RETRY_COUNT = count;
    }

    public static void setDownloadDirectoryByContext(Context context, String directoryName) {
        throwIfAlreadyInitialized();
        File rootCacheDir = context.getCacheDir();
        File imageCacheDir = new File(rootCacheDir, directoryName);
        if (!imageCacheDir.exists()) {
            if (imageCacheDir.mkdirs()) {
                DOWNLOAD_DIRECTORY = imageCacheDir.getPath();
            } else {
                BitmapCacheLogger.logWarn("Failed to create download directory, BitmapLoaderUrl won't work.");
            }
        } else {
            DOWNLOAD_DIRECTORY = imageCacheDir.getPath();
        }
        if (!TextUtils.isEmpty(DOWNLOAD_DIRECTORY) && !DOWNLOAD_DIRECTORY.endsWith(File.separator)) {
            DOWNLOAD_DIRECTORY += File.separator;
        }
    }

    public static void initialize(Context context) {
        throwIfAlreadyInitialized();

        if (LOADER_COUNT <= 0 || LOADER_COUNT > 3) {
            BitmapCacheLogger.logWarn("LoaderCount is invalid, use 2 for default.");
            LOADER_COUNT = 2;
        }
        if (MAX_RAM_USAGE <= 0 || MAX_RAM_USAGE > DeviceHelper.getHeapGrowthLimit(context)) {
            BitmapCacheLogger.logWarn("MaxRAMUsage is invalid, use HeapGrowthLimit / 3 for default.");
            MAX_RAM_USAGE = DeviceHelper.getHeapGrowthLimit(context) / 3;
        }
        if (MAX_RAM_USAGE_OF_SINGLE_BITMAP <= 0 || MAX_RAM_USAGE_OF_SINGLE_BITMAP > 8 * 1024 * 1024) {
            BitmapCacheLogger.logWarn("MaxRAMUsageOfSingleBitmap is invalid, use 4MB for default.");
            MAX_RAM_USAGE_OF_SINGLE_BITMAP = 4 * 1024 * 1024;
        }
        if (LOADER_THREAD_PRIORITY < Thread.MIN_PRIORITY || LOADER_THREAD_PRIORITY > Thread.MAX_PRIORITY) {
            BitmapCacheLogger.logWarn("LoaderThreadPriority is invalid, use Thread.NORM_PRIORITY for default.");
            LOADER_THREAD_PRIORITY = Thread.NORM_PRIORITY;
        }
        if (URL_CONNECT_TIMEOUT <= 0 || URL_CONNECT_TIMEOUT > 20000) {
            BitmapCacheLogger.logWarn("UrlConnectTimeout is invalid, use 20000 for default.");
            URL_CONNECT_TIMEOUT = 20000;
        }
        if (URL_READ_TIMEOUT <= 0 || URL_READ_TIMEOUT > 20000) {
            BitmapCacheLogger.logWarn("UrlReadTimeout is invalid, use 20000 for default.");
            URL_READ_TIMEOUT = 20000;
        }
        if (URL_RETRY_COUNT <= 0 || URL_RETRY_COUNT > 5) {
            BitmapCacheLogger.logWarn("UrlRetryCount is invalid, use 1 for default.");
            URL_RETRY_COUNT = 1;
        }
        if (TextUtils.isEmpty(DOWNLOAD_DIRECTORY)) {
            BitmapCacheLogger.logWarn("DownloadDirectory not set or set failed, BitmapLoaderUrl won't work.");
        }

        mLoaders = new BitmapLoader[LOADER_COUNT];
        for (int i = 0; i < mLoaders.length; i++) {
            mLoaders[i] = new BitmapLoaderImpl(context);
            mLoaders[i].setPriority(LOADER_THREAD_PRIORITY);
            mLoaders[i].start();
        }
        mInitialized = true;
    }

    public static BitmapInfo get(BitmapSource source, BitmapInfoListener listener) {
        if (!mInitialized) {
            BitmapCacheLogger.logError("BitmapCache has not initialized, ignored.");
            return null;
        }
        if (source == null || !source.isValid()) {
            BitmapCacheLogger.logError("Invalid get request, source is empty or invalid, ignored.");
            return null;
        }
        if (listener == null) {
            BitmapCacheLogger.logError("Invalid get request, listener is empty, ignored.");
            return null;
        }
        BitmapInfo bitmapInfo = BitmapInfoManager.get(source);
        if (bitmapInfo == null) {
            bitmapInfo = new BitmapInfo(source);
            bitmapInfo.addListener(listener);
            BitmapInfoManager.add(bitmapInfo);
            BitmapCacheLogger.logVerbose("Added get request, hashCode is " + bitmapInfo.hashCode() + ".");
        } else {
            bitmapInfo.addListener(listener);
            BitmapCacheLogger.logVerbose("Existed get request, hashCode is " + bitmapInfo.hashCode() + ".");
        }
        return bitmapInfo;
    }

}
