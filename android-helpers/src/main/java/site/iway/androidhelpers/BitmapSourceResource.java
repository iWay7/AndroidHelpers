package site.iway.androidhelpers;

public class BitmapSourceResource extends BitmapSource {

    int resourceId;

    public BitmapSourceResource(int resId, BitmapFilter filter) {
        super(filter);
        resourceId = resId;
    }

    public BitmapSourceResource(int resId) {
        super(null);
        resourceId = resId;
    }

    public int getResourceId() {
        return resourceId;
    }

    @Override
    public boolean isValid() {
        return resourceId >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BitmapSourceResource) {
            BitmapSourceResource other = (BitmapSourceResource) o;
            return resourceId == other.resourceId && compareFilters(filter, other.filter);
        }
        return false;
    }

}
