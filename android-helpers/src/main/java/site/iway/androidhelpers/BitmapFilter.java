package site.iway.androidhelpers;

import android.graphics.Bitmap;

public interface BitmapFilter {

    String id();
    Bitmap filter(Bitmap bitmap);

}
