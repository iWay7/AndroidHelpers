package site.iway.androidhelpers;

public class BitmapSourceURL extends BitmapSource {

    public BitmapSourceURL(String url, BitmapFilter filter) {
        super(TYPE_URL, url, filter);
    }

    public BitmapSourceURL(String url) {
        this(url, null);
    }

}
