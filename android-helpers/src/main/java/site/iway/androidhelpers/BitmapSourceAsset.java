package site.iway.androidhelpers;

public class BitmapSourceAsset extends BitmapSource {

    public BitmapSourceAsset(String asset, BitmapFilter filter) {
        super(TYPE_ASSET, asset, filter);
    }

    public BitmapSourceAsset(String asset) {
        this(asset, null);
    }

}
