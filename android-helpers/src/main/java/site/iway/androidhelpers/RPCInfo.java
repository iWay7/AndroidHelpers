package site.iway.androidhelpers;

import android.util.SparseArray;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

@Deprecated
public class RPCInfo {

    String url;
    int connectTimeout = 20 * 1000;
    int readTimeout = 20 * 1000;
    Object request;
    byte[] data;
    Object tag;
    SparseArray<Object> tags;
    Class<?> responseClass;
    RPCListener listener;
    boolean isCanceled;
    long beginTime;
    long minDelayTime;
    boolean ordered;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataString(Charset charset) {
        if (data == null)
            return "";
        return new String(data, charset);
    }

    public int getDataLength() {
        if (data == null)
            return 0;
        return data.length;
    }

    public String getDataLengthString() {
        if (data == null)
            return "0";
        return String.valueOf(data.length);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void writeDataToStream(OutputStream stream) throws IOException {
        if (data != null) {
            stream.write(data);
            stream.flush();
        }
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag(int key) {
        if (tags != null) {
            return tags.get(key);
        }
        return null;
    }

    public void setTag(int key, Object tag) {
        if (tags == null) {
            tags = new SparseArray<>();
        }
        this.tags.put(key, tag);
    }

    public Class<?> getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class<?> responseClass) {
        this.responseClass = responseClass;
    }

    public RPCListener getListener() {
        return listener;
    }

    public void setListener(RPCListener listener) {
        this.listener = listener;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void requestCancel() {
        isCanceled = true;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getMinDelayTime() {
        return minDelayTime;
    }

    public void setMinDelayTime(long minDelayTime) {
        this.minDelayTime = minDelayTime;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }
}
