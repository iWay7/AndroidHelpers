package site.iway.androidhelpers;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BitmapInfo {

    public static final int READY_TO_START = -2;
    public static final int PREPARING = -1;
    public static final int GETTING_DATA = 0;
    public static final int DATA_FINISH = 100;
    public static final int FILTERING = 101;
    public static final int GET_BITMAP = 102;
    public static final int GET_ERROR = 103;

    private final BitmapSource mSource;
    private List<BitmapInfoListener> mListeners;
    private long mGetTime;
    private int mProgress;
    private Bitmap mBitmap;
    private Lock mBitmapLock;

    public BitmapInfo(BitmapSource bitmapSource) {
        mSource = bitmapSource;
        mListeners = new LinkedList<>();
        mGetTime = 0;
        mProgress = READY_TO_START;
        mBitmapLock = new ReentrantLock();
    }

    void addListener(BitmapInfoListener listener) {
        synchronized (this) {
            boolean existed = mListeners.contains(listener);
            if (!existed) {
                if (mProgress == GET_BITMAP || mProgress == GET_ERROR) {
                    listener.onBitmapInfoChange(this);
                } else {
                    mListeners.add(listener);
                }
            }
        }
    }

    void removeListener(BitmapInfoListener listener) {
        synchronized (this) {
            mListeners.remove(listener);
        }
    }

    long getGetTime() {
        synchronized (this) {
            return mGetTime;
        }
    }

    void updateGetTime() {
        synchronized (this) {
            mGetTime = System.nanoTime();
        }
    }

    void updateProgress(int newProgress) {
        synchronized (this) {
            if (mProgress != newProgress) {
                mProgress = newProgress;
                for (BitmapInfoListener listener : mListeners) {
                    try {
                        listener.onBitmapInfoChange(this);
                    } catch (Exception e) {
                        BitmapCacheLogger.logError("BitmapInfo " + hashCode() + " has error occurred while notifying mProgress.");
                        BitmapCacheLogger.logError(e);
                    }
                }
            }
            if (mProgress == GET_BITMAP || mProgress == GET_ERROR) {
                mListeners.clear();
            }
        }
    }

    public BitmapSource getSource() {
        synchronized (this) {
            return mSource;
        }
    }

    public int getProgress() {
        synchronized (this) {
            return mProgress;
        }
    }

    public int getDataProgress() {
        synchronized (this) {
            if (mProgress < 0)
                return 0;
            if (mProgress > 100)
                return 100;
            return mProgress;
        }
    }

    public void lockBitmap() {
        mBitmapLock.lock();
    }

    public void unlockBitmap() {
        mBitmapLock.unlock();
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    int getRAMUsage() {
        if (mBitmap == null || mBitmap.isRecycled())
            return 0;
        return mBitmap.getRowBytes() * mBitmap.getHeight();
    }

    void releaseRAM() {
        if (mBitmap == null || mBitmap.isRecycled())
            return;
        mBitmap.recycle();
    }
}
