package site.iway.androidhelpers;

import android.os.Handler;
import android.os.Looper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

import site.iway.javahelpers.StreamReader;

@Deprecated
public abstract class RPCInfoManager {

    private LinkedBlockingQueue<RPCInfo> mRPCInfosOrdered;
    private LinkedBlockingQueue<RPCInfo> mRPCInfos;
    private RPCInfoProcessor[] mProcessors;

    public RPCInfoManager(int processorCount) {
        if (processorCount <= 0) {
            throw new RuntimeException("processorCount must be larger than zero.");
        }
        mRPCInfosOrdered = new LinkedBlockingQueue<>();
        mRPCInfos = new LinkedBlockingQueue<>();
        mProcessors = new RPCInfoProcessor[processorCount];
        for (int i = 0; i < processorCount; i++) {
            mProcessors[i] = new RPCInfoProcessor(i == 0);
            mProcessors[i].start();
        }
    }

    public RPCInfoManager() {
        this(2);
    }

    public RPCInfo addRequest(RPCInfo rpcInfo) {
        if (rpcInfo.listener == null) {
            rpcInfo.listener = new RPCListener() {
                @Override
                public void onRequestOK(RPCInfo rpcInfo, Object response) {
                }

                @Override
                public void onRequestER(RPCInfo rpcInfo, Exception e) {
                }
            };
        }
        if (mProcessors.length > 1) {
            if (rpcInfo.ordered) {
                return mRPCInfosOrdered.add(rpcInfo) ? rpcInfo : null;
            } else {
                if (mRPCInfosOrdered.size() < mRPCInfos.size() / (mProcessors.length - 1))
                    return mRPCInfosOrdered.add(rpcInfo) ? rpcInfo : null;
                else
                    return mRPCInfos.add(rpcInfo) ? rpcInfo : null;
            }
        } else {
            return mRPCInfosOrdered.add(rpcInfo) ? rpcInfo : null;
        }
    }

    protected Handler mHandler;

    protected void checkHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

    protected void postRunnable(Runnable runnable, long beginTime, long minDelayTime) {
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

    protected boolean beforeOKOnUIThread(RPCInfo rpcInfo, Object response) {
        return true;
    }

    protected void afterOKOnUIThread(RPCInfo rpcInfo, Object response) {
        // nothing
    }

    protected void handleOKOnUIThread(final RPCInfo rpcInfo, final Object response) {
        checkHandler();
        postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!rpcInfo.isCanceled && beforeOKOnUIThread(rpcInfo, response)) {
                    RPCListener rpcListener = rpcInfo.getListener();
                    rpcListener.onRequestOK(rpcInfo, response);
                    afterOKOnUIThread(rpcInfo, response);
                }
            }
        }, rpcInfo.beginTime, rpcInfo.minDelayTime);
    }

    protected boolean beforeErrorOnUIThread(RPCInfo rpcInfo, Exception error) {
        return true;
    }

    protected void afterErrorOnUIThread(RPCInfo rpcInfo, Exception error) {
        // nothing
    }

    protected void handleErrorOnUIThread(final RPCInfo rpcInfo, final Exception error) {
        checkHandler();
        postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!rpcInfo.isCanceled && beforeErrorOnUIThread(rpcInfo, error)) {
                    RPCListener rpcListener = rpcInfo.getListener();
                    rpcListener.onRequestER(rpcInfo, error);
                    afterErrorOnUIThread(rpcInfo, error);
                }
            }
        }, rpcInfo.beginTime, rpcInfo.minDelayTime);
    }

    protected void onRPCInfoTaken(RPCInfo rpcInfo) throws Exception {
        // nothing
    }

    protected abstract void onUrlConnectionOpened(RPCInfo rpcInfo, HttpURLConnection connection) throws Exception;

    protected void onUrlConnectionEstablished(RPCInfo rpcInfo, HttpURLConnection connection) throws Exception {
        // nothing
    }

    protected abstract void sendData(RPCInfo rpcInfo, HttpURLConnection connection, OutputStream stream) throws Exception;

    protected abstract void onGetData(RPCInfo rpcInfo, byte[] data) throws Exception;

    protected abstract void onGetError(RPCInfo rpcInfo, Exception e);

    private class RPCInfoProcessor extends Thread {

        private boolean mOrdered;

        public RPCInfoProcessor(boolean ordered) {
            mOrdered = ordered;
        }

        @Override
        public void run() {
            while (true) {
                RPCInfo rpcInfo = null;
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    if (mOrdered)
                        rpcInfo = mRPCInfosOrdered.take();
                    else
                        rpcInfo = mRPCInfos.take();
                    rpcInfo.beginTime = System.currentTimeMillis();
                    if (rpcInfo.isCanceled)
                        continue;
                    onRPCInfoTaken(rpcInfo);

                    URL url = new URL(rpcInfo.url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(rpcInfo.getConnectTimeout());
                    connection.setReadTimeout(rpcInfo.getReadTimeout());
                    if (rpcInfo.isCanceled)
                        continue;
                    onUrlConnectionOpened(rpcInfo, connection);

                    connection.connect();
                    if (rpcInfo.isCanceled)
                        continue;
                    onUrlConnectionEstablished(rpcInfo, connection);

                    if (connection.getDoOutput()) {
                        outputStream = connection.getOutputStream();
                        if (rpcInfo.isCanceled)
                            continue;
                        sendData(rpcInfo, connection, outputStream);
                    }

                    if (connection.getDoInput()) {
                        inputStream = connection.getInputStream();
                        byte[] data = StreamReader.readAllBytes(inputStream);
                        if (rpcInfo.isCanceled)
                            continue;
                        onGetData(rpcInfo, data);
                    }
                } catch (Exception e) {
                    if (rpcInfo != null) {
                        if (rpcInfo.isCanceled)
                            continue;
                        onGetError(rpcInfo, e);
                    }
                } finally {
                    try {
                        outputStream.close();
                    } catch (Exception e) {
                        // nothing
                    }
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        // nothing
                    }
                }
            }
        }

    }

}
