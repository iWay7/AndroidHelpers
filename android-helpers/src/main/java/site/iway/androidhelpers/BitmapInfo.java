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

    BitmapSource source = null;
    long priority = 0;
    Bitmap bitmap = null;
    Lock lock = new ReentrantLock();
    int progress = READY_TO_START;

    private final List<BitmapInfoListener> mListeners = new LinkedList<>();
    private final Object mListenersLock = new Object();

    void addListener(BitmapInfoListener listener) {
        synchronized (mListenersLock) {
            boolean existed = mListeners.contains(listener);
            if (!existed) {
                mListeners.add(listener);
            }
        }
    }

    void removeListener(BitmapInfoListener listener) {
        synchronized (mListenersLock) {
            mListeners.remove(listener);
        }
    }

    void clearListeners() {
        synchronized (mListenersLock) {
            mListeners.clear();
        }
    }

    List<BitmapInfoListener> copyListeners() {
        List<BitmapInfoListener> listenersCopy = new LinkedList<>();
        synchronized (mListenersLock) {
            listenersCopy.addAll(mListeners);
        }
        return listenersCopy;
    }

    void updateProgress(int newProgress) {
        if (progress != newProgress) {
            progress = newProgress;
            List<BitmapInfoListener> listeners = copyListeners();
            for (BitmapInfoListener listener : listeners) {
                try {
                    listener.onBitmapInfoChange(this);
                } catch (Exception e) {
                    BitmapCacheLogger.logError("BitmapInfo " + hashCode() + " has error occurred while notifying progress.");
                    BitmapCacheLogger.logError(e);
                }
            }
        }
    }

    int getRAMUsage() {
        if (bitmap == null || bitmap.isRecycled())
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    void releaseRAM() {
        if (bitmap == null || bitmap.isRecycled())
            return;
        bitmap.recycle();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public BitmapSource getSource() {
        return source;
    }

    public int getProgress() {
        return progress;
    }

    public int getDataProgress() {
        if (progress < 0)
            return 0;
        if (progress > 100)
            return 100;
        return progress;
    }

    public boolean isFinished() {
        return progress == GET_BITMAP || progress == GET_ERROR;
    }

    public Bitmap getBitmap() {
        return progress == GET_BITMAP ? bitmap : null;
    }

}
