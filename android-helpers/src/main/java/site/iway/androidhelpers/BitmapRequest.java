package site.iway.androidhelpers;

public class BitmapRequest {

    public static final int READY_TO_START = 0;
    public static final int PREPARING = 1;
    public static final int DECODING = 2;
    public static final int FILTERING = 3;
    public static final int GET_BITMAP = 4;
    public static final int GET_ERROR = 5;

    private final BitmapSource mSource;
    private final BitmapCallback mCallback;
    private volatile int mProgress;
    private volatile boolean mCanceled;

    public BitmapRequest(BitmapSource source, BitmapCallback callback) {
        if (source == null)
            throw new NullPointerException("Param source can not be null.");
        if (callback == null)
            throw new NullPointerException("Param callback can not be null.");
        mSource = source;
        mCallback = callback;
        mProgress = READY_TO_START;
    }

    public BitmapSource getSource() {
        return mSource;
    }

    public BitmapCallback getCallback() {
        return mCallback;
    }

    public int getProgress() {
        return mProgress;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void cancel() {
        mCanceled = true;
    }

    void updateProgress(int progress) {
        if (mProgress != progress) {
            mProgress = progress;
            mCallback.onBitmapLoadProgressChange(this);
        }
    }

}
