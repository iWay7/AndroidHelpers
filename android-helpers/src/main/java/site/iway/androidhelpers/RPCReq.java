package site.iway.androidhelpers;

import java.net.HttpURLConnection;

public abstract class RPCReq {

    public String url;
    public long minDelayTime;
    boolean isCanceled;
    long beginTime;

    public final void start() {
        RPCEngine.dealWith(this);
    }

    public final void cancel() {
        isCanceled = true;
    }

    protected void onPrepare() throws Exception {
        // nothing
    }

    protected void onConnect(HttpURLConnection connection) throws Exception {
        // nothing
    }

    protected void onConnected(HttpURLConnection connection) throws Exception {
        // nothing
    }

    protected void onFinish() {
        // nothing
    }

    protected void onFinishUI() {
        // nothing
    }

    protected void onError(Exception e) {
        // nothing
    }

    protected void onErrorUI() {
        // nothing
    }

    protected void onFinally() {
        // nothing
    }

}
