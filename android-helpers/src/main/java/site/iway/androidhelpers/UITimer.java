package site.iway.androidhelpers;

import android.os.Handler;

public abstract class UITimer {

    private Thread mThread;
    private Handler mHandler;
    private long mPostSpan;
    private boolean mRunning;

    public UITimer(long span) {
        mThread = Thread.currentThread();
        mHandler = new Handler();
        mPostSpan = span;
        mRunning = false;
    }

    public UITimer() {
        this(10);
    }

    private long mTargetRunTime;

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (currentTime < mTargetRunTime) {
                long delay = mPostSpan + (mTargetRunTime - currentTime);
                mHandler.postDelayed(mRunnable, delay);
                mTargetRunTime = System.currentTimeMillis() + delay;
            } else if (currentTime > mTargetRunTime) {
                long delay = mPostSpan + (mTargetRunTime - currentTime);
                if (delay < 1)
                    delay = 1;
                mHandler.postDelayed(mRunnable, delay);
                mTargetRunTime = System.currentTimeMillis() + delay;
            } else {
                mHandler.postDelayed(mRunnable, mPostSpan);
                mTargetRunTime = System.currentTimeMillis() + mPostSpan;
            }
            doOnUIThread();
        }

    };

    public abstract void doOnUIThread();

    private void checkThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != mThread) {
            throw new RuntimeException("Call from different thread.");
        }
    }

    public void start(boolean startOnce) {
        checkThread();
        if (mRunning) {
            mHandler.removeCallbacks(mRunnable);
        } else {
            mRunning = true;
        }
        if (startOnce) {
            mHandler.postDelayed(mRunnable, 0);
            mTargetRunTime = System.currentTimeMillis();
        } else {
            mHandler.postDelayed(mRunnable, mPostSpan);
            mTargetRunTime = System.currentTimeMillis() + mPostSpan;
        }
    }

    public void stop() {
        checkThread();
        if (mRunning) {
            mHandler.removeCallbacks(mRunnable);
            mRunning = false;
        }
    }

}
