package site.iway.androidhelpers;

public class BitmapSourceURL extends BitmapSource {

    String urlPath;

    public BitmapSourceURL(String path, BitmapFilter filter) {
        super(filter);
        urlPath = path;
    }

    public BitmapSourceURL(String path) {
        super(null);
        urlPath = path;
    }

    public String getURLPath() {
        return urlPath;
    }

    @Override
    public boolean isValid() {
        return urlPath != null && urlPath.length() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BitmapSourceURL) {
            BitmapSourceURL other = (BitmapSourceURL) o;
            return compareValidString(urlPath, other.urlPath) && compareFilters(filter, other.filter);
        }
        return false;
    }

}
