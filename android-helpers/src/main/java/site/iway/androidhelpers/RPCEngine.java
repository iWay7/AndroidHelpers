package site.iway.androidhelpers;

import android.os.Handler;
import android.os.Looper;

import java.net.HttpURLConnection;
import java.util.concurrent.LinkedBlockingQueue;

public class RPCEngine {

    private LinkedBlockingQueue<RPCReq> mQueue;
    private RPCReqProcessor[] mProcessors;
    private Handler mHandler;

    public RPCEngine(int processorCount) {
        if (processorCount <= 0) {
            throw new RuntimeException("ProcessorCount must be larger than zero.");
        }
        mQueue = new LinkedBlockingQueue<>();
        mProcessors = new RPCReqProcessor[processorCount];
        for (int i = 0; i < processorCount; i++) {
            mProcessors[i] = new RPCReqProcessor();
            mProcessors[i].start();
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void addReq(RPCReq req) {
        if (req.independent) {
            new RPCReqProcessor(req).start();
        } else {
            mQueue.add(req);
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

    private class RPCReqProcessor extends Thread {

        private RPCReq mReq;

        public RPCReqProcessor(RPCReq req) {
            mReq = req;
        }

        public RPCReqProcessor() {
            // nothing
        }

        private void runReq(RPCReq req) {
            try {
                req.beginTime = System.currentTimeMillis();

                if (req.isCanceled)
                    return;
                req.onPrepare();

                do {
                    boolean isRetry = req.willRetry;
                    req.willRetry = false;

                    if (req.isCanceled)
                        return;
                    HttpURLConnection connection = req.onCreateConnection(isRetry);

                    if (req.willRetry)
                        continue;
                    if (req.isCanceled)
                        return;
                    req.onConnect(connection);

                    if (req.willRetry)
                        continue;
                    if (req.isCanceled)
                        return;
                    connection.connect();

                    if (req.willRetry)
                        continue;
                    if (req.isCanceled)
                        return;
                    req.onConnected(connection);
                } while (req.willRetry);

                if (req.isCanceled)
                    return;
                req.onFinish();

                if (req.isCanceled)
                    return;
                finishOnUIThread(req);
            } catch (Exception e) {
                try {
                    if (req.isCanceled)
                        return;
                    req.onError(e);

                    if (req.isCanceled)
                        return;
                    errorOnUIThread(req);
                } catch (Exception onErrorE) {
                    // nothing
                }
            } finally {
                try {
                    req.onFinally();
                } catch (Exception e) {
                    // nothing
                }
            }
        }

        @Override
        public void run() {
            if (mReq == null) {
                while (true) {
                    try {
                        RPCReq req = mQueue.take();
                        runReq(req);
                    } catch (Exception e) {
                        // nothing
                    }
                }
            } else {
                runReq(mReq);
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
