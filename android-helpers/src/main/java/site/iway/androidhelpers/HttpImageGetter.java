package site.iway.androidhelpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import site.iway.javahelpers.AutoExpandByteArray;
import site.iway.javahelpers.HttpDataGetter;

public class HttpImageGetter extends HttpDataGetter {

    public HttpImageGetter(String url) {
        super(url);
    }

    @Override
    public void onGetData(AutoExpandByteArray array) throws Exception {
        byte[] data = array.getRawArray();
        int length = array.size();
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, length);
        onGetImage(bmp);
    }

    public void onGetImage(Bitmap bitmap) throws Exception {
    }

}
