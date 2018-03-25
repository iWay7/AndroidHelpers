package site.iway.androidhelpers;

import android.graphics.Bitmap;

import site.iway.javahelpers.Scale;

public class BitmapFilterClip implements BitmapFilter {

    Scale scale;
    int width;
    int height;
    float radius;

    public BitmapFilterClip(Scale scaleType, int width, int height, float radius) {
        this.scale = scaleType;
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    @Override
    public Bitmap filter(Bitmap bitmap) {
        return BitmapHelper.clip(bitmap, scale, width, height, radius);
    }

    @Override
    public String toString() {
        return "Clip," + scale.ordinal() + "," + width + "," + height + "," + radius;
    }
}
