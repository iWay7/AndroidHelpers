package site.iway.androidhelpers;

import android.graphics.Bitmap;

import site.iway.javahelpers.Scale;

public class BitmapFilterClip implements BitmapFilter {

    final Scale scale;
    final int width;
    final int height;
    final float radius;

    public BitmapFilterClip(Scale scaleType, int width, int height, float radius) {
        this.scale = scaleType;
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    @Override
    public String id() {
        return "Clip," + scale.ordinal() + "," + width + "," + height + "," + radius;
    }

    @Override
    public Bitmap filter(Bitmap bitmap) {
        return BitmapHelper.clip(bitmap, scale, width, height, radius);
    }

}
