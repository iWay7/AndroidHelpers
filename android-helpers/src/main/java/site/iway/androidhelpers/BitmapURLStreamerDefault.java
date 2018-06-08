package site.iway.androidhelpers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapURLStreamerDefault implements BitmapURLStreamer {

    private HttpURLConnection mConnection;
    private InputStream mInputStream;
    private int mDataLength;

    public void initialize(String urlPath) throws Exception {
        mConnection = (HttpURLConnection) new URL(urlPath).openConnection();
        mConnection.setConnectTimeout(20 * 1000);
        mConnection.setReadTimeout(20 * 1000);
        mConnection.connect();
        mInputStream = mConnection.getInputStream();
        mDataLength = mConnection.getContentLength();
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public int getDataLength() {
        return mDataLength;
    }

    public void release() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (Exception e) {
                // nothing
            }
            mInputStream = null;
        }
        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }
    }

}
