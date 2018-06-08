package site.iway.androidhelpers;

import java.io.InputStream;

public interface BitmapURLStreamer {

    public void initialize(String urlPath) throws Exception;

    public InputStream getInputStream();

    public int getDataLength();

    public void release();

}
