package site.iway.androidhelpers;

public class BitmapSourceAsset extends BitmapSource {

    String assetPath;

    public BitmapSourceAsset(String path, BitmapFilter filter) {
        super(filter);
        assetPath = path;
    }

    public BitmapSourceAsset(String path) {
        super(null);
        assetPath = path;
    }

    public String getAssetPath() {
        return assetPath;
    }

    @Override
    public boolean isValid() {
        return assetPath != null && assetPath.length() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BitmapSourceAsset) {
            BitmapSourceAsset other = (BitmapSourceAsset) o;
            return filter == other.filter && compareValidString(assetPath, other.assetPath);
        }
        return false;
    }

}
