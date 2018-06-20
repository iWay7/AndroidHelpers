package site.iway.androidhelpers;

public class BitmapSourceResource extends BitmapSource {

    public BitmapSourceResource(int resource, BitmapFilter filter) {
        super(TYPE_RESOURCE, String.valueOf(resource), filter);
    }

    public BitmapSourceResource(int resource) {
        this(resource, null);
    }

}
