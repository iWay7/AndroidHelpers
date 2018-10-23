package site.iway.androidhelpers;

import android.os.Handler;
import android.os.Looper;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RPCEngine {

    private ExecutorService mExecutorService;
    private Handler mHandler;

    public RPCEngine(int processorCount) {
        if (processorCount <= 0) {
            throw new RuntimeException("ProcessorCount must be larger than zero.");
        }
        mExecutorService = Executors.newFixedThreadPool(processorCount);
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void addReq(RPCReq req) {
        ReqProcessor reqProcessor = new ReqProcessor(req);
        if (req.independent) {
            Thread thread = new Thread(reqProcessor);
            thread.start();
        } else {
            mExecutorService.execute(reqProcessor);
        }
    }

    private void postRunnable(Runnable runnable, long beginTime, long minDelayTime) {
        if (minDelayTime <= 0) {
            mHandler.post(runnable);
        } else {
            long delayTime = System.currentTimeMillis() - beginTime;
            if (delayTime >= minDelayTime) {
                mHandler.post(runnable);
            } else {
                mHandler.postDelayed(runnable, minDelayTime - delayTime);
            }
        }
    }

    private void finishOnUIThread(final RPCReq req) {
        postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!req.isCanceled) {
                    req.onFinishUI();
                }
            }
        }, req.beginTime, req.minDelayTime);
    }

    private void errorOnUIThread(final RPCReq req) {
        postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!req.isCanceled) {
                    req.onErrorUI();
                }
            }
        }, req.beginTime, req.minDelayTime);
    }

    private class ReqProcessor implements Runnable {

        private RPCReq mReq;

        public ReqProcessor(RPCReq req) {
            mReq = req;
        }

        @Override
        public void run() {
            try {
                mReq.beginTime = System.currentTimeMillis();

                if (mReq.isCanceled)
                    return;
                mReq.onPrepare();

                do {
                    boolean isRetry = mReq.willRetry;
                    mReq.willRetry = false;

                    if (mReq.isCanceled)
                        return;
                    HttpURLConnection connection = mReq.onCreateConnection(isRetry);

                    if (mReq.willRetry)
                        continue;
                    if (mReq.isCanceled)
                        return;
                    mReq.onConnect(connection);

                    if (mReq.willRetry)
                        continue;
                    if (mReq.isCanceled)
                        return;
                    connection.connect();

                    if (mReq.willRetry)
                        continue;
                    if (mReq.isCanceled)
                        return;
                    mReq.onConnected(connection);
                } while (mReq.willRetry);

                if (mReq.isCanceled)
                    return;
                mReq.onFinish();

                if (mReq.isCanceled)
                    return;
                finishOnUIThread(mReq);
            } catch (Exception e) {
                try {
                    if (mReq.isCanceled)
                        return;
                    mReq.onError(e);

                    if (mReq.isCanceled)
                        return;
                    errorOnUIThread(mReq);
                } catch (Exception onErrorE) {
                    // nothing
                }
            } finally {
                try {
                    mReq.onFinally();
                } catch (Exception e) {
                    // nothing
                }
            }
        }

    }

    private static RPCEngine sEngine;

    public static void initialize(int processorCount) {
        sEngine = new RPCEngine(processorCount);
    }

    static void dealWith(RPCReq req) {
        sEngine.addReq(req);
    }

}
