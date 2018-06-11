package site.iway.androidhelpers;

import java.net.HttpURLConnection;
import java.net.URL;

public abstract class RPCReq {

    public String url;
    public long minDelayTime;
    public boolean independent;
    volatile boolean isCanceled;
    volatile long beginTime;
    volatile boolean willRetry;

    public final void start() {
        RPCEngine.dealWith(this);
    }

    public final void retry() {
        willRetry = true;
    }

    public final void cancel() {
        isCanceled = true;
    }

    protected void onPrepare() throws Exception {
        // nothing
    }

    protected HttpURLConnection onCreateConnection(boolean isRetry) throws Exception {
        return (HttpURLConnection) new URL(url).openConnection();
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
