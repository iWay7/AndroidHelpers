package site.iway.androidhelpers;

import android.os.Handler;
import android.os.Looper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class RPCEngine {

    private LinkedBlockingQueue<RPCReq> mRPCReqs;
    private RPCReqProcessor[] mProcessors;
    private Handler mHandler;

    private RPCEngine(int processorCount) {
        if (processorCount <= 0) {
            throw new RuntimeException("ProcessorCount must be larger than zero.");
        }
        mRPCReqs = new LinkedBlockingQueue<>();
        mProcessors = new RPCReqProcessor[processorCount];
        for (int i = 0; i < processorCount; i++) {
            mProcessors[i] = new RPCReqProcessor();
            mProcessors[i].start();
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void addReq(RPCReq req) {
        mRPCReqs.add(req);
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

        @Override
        public void run() {
            while (true) {
                RPCReq req = null;
                try {
                    req = mRPCReqs.take();
                    req.beginTime = System.currentTimeMillis();
                    if (req.isCanceled)
                        continue;
                    req.onPrepare();
                    URL url = new URL(req.url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    if (req.isCanceled)
                        continue;
                    req.onConnect(connection);
                    connection.connect();
                    if (req.isCanceled)
                        continue;
                    req.onConnected(connection);
                    if (req.isCanceled)
                        continue;
                    req.onFinish();
                    if (req.isCanceled)
                        continue;
                    finishOnUIThread(req);
                } catch (Exception e) {
                    try {
                        if (req != null) {
                            if (req.isCanceled)
                                continue;
                            req.onError(e);
                            if (req.isCanceled)
                                continue;
                            errorOnUIThread(req);
                        }
                    } catch (Exception onErrorE) {
                        // nothing
                    }
                } finally {
                    try {
                        if (req != null) {
                            req.onFinally();
                        }
                    } catch (Exception onFinallyE) {
                        // nothing
                    }
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
