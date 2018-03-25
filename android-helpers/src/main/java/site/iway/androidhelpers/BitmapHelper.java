package site.iway.androidhelpers;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import site.iway.javahelpers.MathHelper;
import site.iway.javahelpers.Scale;

public class BitmapHelper {

    public static Bitmap addReflection(Bitmap source, float percentFromBottom, int reflectionHeight, int startAlpha, int endAlpha) {
        if (reflectionHeight < 0)
            throw new IllegalArgumentException("reflectionHeight > 0");
        if (percentFromBottom <= 0 || percentFromBottom > 1)
            throw new IllegalArgumentException("0 < percentFromBottom <= 1");
        if (startAlpha < 0 || startAlpha > 255)
            throw new IllegalArgumentException("0 <= startAlpha <= 255");
        if (endAlpha < 0 || endAlpha > 255)
            throw new IllegalArgumentException("0 <= endAlpha <= 255");
        Bitmap targetBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight() + reflectionHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(source, 0, 0, paint);
        Matrix matrix = new Matrix();
        float scale = (float) reflectionHeight / source.getHeight() / percentFromBottom;
        matrix.preTranslate(0, source.getHeight() + scale * source.getHeight());
        matrix.preScale(1, -scale);
        canvas.drawRect(0, source.getHeight(), source.getWidth(), source.getHeight() + reflectionHeight, paint);
        canvas.drawBitmap(source, matrix, paint);
        int yStart = source.getHeight();
        int yEnd = targetBitmap.getHeight();
        int startColor = 0x00ffffff | (startAlpha << 24);
        int entColor = 0x00ffffff | (endAlpha << 24);
        LinearGradient shader = new LinearGradient(0, yStart, 0, yEnd, startColor, entColor, TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, source.getHeight(), source.getWidth(), source.getHeight() + reflectionHeight, paint);
        return targetBitmap;
    }

    public static Bitmap addReflection(Bitmap source, float percentFromBottom, int reflectionHeight) {
        return addReflection(source, percentFromBottom, reflectionHeight, 255, 0);
    }

    public static Bitmap addReflection(Bitmap source) {
        return addReflection(source, 0.3f, source.getHeight() / 3);
    }

    public static Bitmap roundCorner(Bitmap source, float radiusX, float radiusY) {
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0)
            return null;
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setColor(Color.BLACK);
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rectF, radiusX, radiusY, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    public static Bitmap roundCorner(Bitmap source, float radius) {
        return roundCorner(source, radius, radius);
    }

    public static Bitmap blur(Bitmap source, int radius) {
        if (source == null)
            return null;

        int width = source.getWidth();
        int height = source.getHeight();

        if (width == 0)
            return null;
        if (height == 0)
            return null;

        int[] pixelsFromSource = new int[width * height];
        source.getPixels(pixelsFromSource, 0, width, 0, 0, width, height);

        int wm = width - 1;
        int hm = height - 1;
        int wh = width * height;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rSum, gSum, bSum, x, y, i, p, yp, yi, yw;
        int vMin[] = new int[Math.max(width, height)];
        int divSum = (div + 1) >> 1;
        divSum *= divSum;
        int dv[] = new int[256 * divSum];
        for (i = 0; i < 256 * divSum; i++) {
            dv[i] = (i / divSum);
        }
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackPointer;
        int stackStart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int rOutSum, gOutSum, bOutSum;
        int rInSum, gInSum, bInSum;
        for (y = 0; y < height; y++) {
            rInSum = gInSum = bInSum = rOutSum = gOutSum = bOutSum = rSum = gSum = bSum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pixelsFromSource[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rSum += sir[0] * rbs;
                gSum += sir[1] * rbs;
                bSum += sir[2] * rbs;
                if (i > 0) {
                    rInSum += sir[0];
                    gInSum += sir[1];
                    bInSum += sir[2];
                } else {
                    rOutSum += sir[0];
                    gOutSum += sir[1];
                    bOutSum += sir[2];
                }
            }
            stackPointer = radius;
            for (x = 0; x < width; x++) {
                r[yi] = dv[rSum];
                g[yi] = dv[gSum];
                b[yi] = dv[bSum];
                rSum -= rOutSum;
                gSum -= gOutSum;
                bSum -= bOutSum;
                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];
                rOutSum -= sir[0];
                gOutSum -= sir[1];
                bOutSum -= sir[2];
                if (y == 0) {
                    vMin[x] = Math.min(x + radius + 1, wm);
                }
                p = pixelsFromSource[yw + vMin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rInSum += sir[0];
                gInSum += sir[1];
                bInSum += sir[2];
                rSum += rInSum;
                gSum += gInSum;
                bSum += bInSum;
                stackPointer = (stackPointer + 1) % div;
                sir = stack[(stackPointer) % div];
                rOutSum += sir[0];
                gOutSum += sir[1];
                bOutSum += sir[2];
                rInSum -= sir[0];
                gInSum -= sir[1];
                bInSum -= sir[2];
                yi++;
            }
            yw += width;
        }
        for (x = 0; x < width; x++) {
            rInSum = gInSum = bInSum = rOutSum = gOutSum = bOutSum = rSum = gSum = bSum = 0;
            yp = -radius * width;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rSum += r[yi] * rbs;
                gSum += g[yi] * rbs;
                bSum += b[yi] * rbs;
                if (i > 0) {
                    rInSum += sir[0];
                    gInSum += sir[1];
                    bInSum += sir[2];
                } else {
                    rOutSum += sir[0];
                    gOutSum += sir[1];
                    bOutSum += sir[2];
                }
                if (i < hm) {
                    yp += width;
                }
            }
            yi = x;
            stackPointer = radius;
            for (y = 0; y < height; y++) {
                pixelsFromSource[yi] = (0xff000000 & pixelsFromSource[yi]) | (dv[rSum] << 16) | (dv[gSum] << 8) | dv[bSum];
                rSum -= rOutSum;
                gSum -= gOutSum;
                bSum -= bOutSum;
                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];
                rOutSum -= sir[0];
                gOutSum -= sir[1];
                bOutSum -= sir[2];
                if (x == 0) {
                    vMin[y] = Math.min(y + r1, hm) * width;
                }
                p = x + vMin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rInSum += sir[0];
                gInSum += sir[1];
                bInSum += sir[2];
                rSum += rInSum;
                gSum += gInSum;
                bSum += bInSum;
                stackPointer = (stackPointer + 1) % div;
                sir = stack[stackPointer];
                rOutSum += sir[0];
                gOutSum += sir[1];
                bOutSum += sir[2];
                rInSum -= sir[0];
                gInSum -= sir[1];
                bInSum -= sir[2];
                yi += width;
            }
        }

        return Bitmap.createBitmap(pixelsFromSource, width, height, Config.ARGB_8888);
    }

    public static Bitmap emboss(Bitmap source) {
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0)
            return null;

        int width = source.getWidth();
        int height = source.getHeight();

        int pixR;
        int pixG;
        int pixB;

        int pixColor;

        int newR;
        int newG;
        int newB;

        int[] pixelsFromSource = new int[width * height];
        source.getPixels(pixelsFromSource, 0, width, 0, 0, width, height);

        int pos;
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                pos = i * width + k;
                pixColor = pixelsFromSource[pos];

                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                pixColor = pixelsFromSource[pos + 1];
                newR = Color.red(pixColor) - pixR + 127;
                newG = Color.green(pixColor) - pixG + 127;
                newB = Color.blue(pixColor) - pixB + 127;

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixelsFromSource[pos] = Color.argb(255, newR, newG, newB);
            }
        }

        return Bitmap.createBitmap(pixelsFromSource, width, height, Config.ARGB_8888);
    }

    public static Bitmap greyScale(Bitmap source) {
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0)
            return null;
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = source.getPixel(i, j);
                int a = (pixel & 0xFF000000) >> 24;
                int r = (pixel & 0x00FF0000) >> 16;
                int g = (pixel & 0x0000FF00) >> 8;
                int b = pixel & 0x000000FF;
                int gray = (r * 76 + g * 151 + b * 28) >> 8;
                int color = Color.argb(a, gray, gray, gray);
                target.setPixel(i, j, color);
            }
        }
        return target;
    }

    public static Bitmap mirror(Bitmap source, boolean horizontal, boolean vertical) {
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0)
            return null;
        int width = source.getWidth();
        int height = source.getHeight();
        Matrix m = new Matrix();
        float sx = horizontal ? -1 : 1;
        float sy = vertical ? -1 : 1;
        float w = width;
        float h = height;
        float px = w / 2;
        float py = h / 2;
        m.setScale(sx, sy, px, py);
        return Bitmap.createBitmap(source, 0, 0, width, height, m, false);
    }

    public static Bitmap rotate(Bitmap source, float degrees) {
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0)
            return null;
        int width = source.getWidth();
        int height = source.getHeight();
        Matrix m = new Matrix();
        float w = width;
        float h = height;
        float px = w / 2;
        float py = h / 2;
        m.setRotate(degrees, px, py);
        return Bitmap.createBitmap(source, 0, 0, width, height, m, true);
    }

    public static Bitmap rotateByExifInfo(Bitmap source, String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            String tagName = ExifInterface.TAG_ORIENTATION;
            int defaultValue = ExifInterface.ORIENTATION_NORMAL;
            int orientation = exifInterface.getAttributeInt(tagName, defaultValue);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return source;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotate(source, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotate(source, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotate(source, 270f);
                default:
                    return source;
            }
        } catch (Exception e) {
            return source;
        }
    }

    public static Bitmap scale(Bitmap source, float scaleX, float scaleY) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int targetWidth = MathHelper.pixel(sourceWidth * scaleX);
        int targetHeight = MathHelper.pixel(sourceHeight * scaleY);
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }

    public static Bitmap scale(Bitmap source, float scale) {
        return scale(source, scale, scale);
    }

    public static Bitmap scale(Bitmap source, Scale scale, int width, int height) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        float scaleValue = scale.getScale(width, height, sourceWidth, sourceHeight);
        return scale(source, scaleValue);
    }

    public static Bitmap clip(Bitmap source, Scale scale, int width, int height, float radius) {
        if (source == null || width < 0 || height < 0)
            return null;
        Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rectF = new RectF(0, 0, width, height);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        if (radius > 0) {
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, radius, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        }
        CanvasHelper.drawBitmap(canvas, rectF, source, null, scale, paint);
        return target;
    }

    public static Options decodeJustBounds(String path) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    public static Bitmap decodeRestrictDataSize(String path, int maxDataSize) {
        Options options = decodeJustBounds(path);
        int width = options.outWidth;
        int height = options.outHeight;
        int scale = 1;
        while (width * height * 4 > maxDataSize) {
            scale *= 2;
            width /= 2;
            height /= 2;
        }
        options.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, options);
    }

    public static byte[] convertToBytes(Bitmap bitmap, CompressFormat format, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, stream);
        byte[] data = stream.toByteArray();
        try {
            stream.close();
        } catch (Exception e) {
            // nothing
        }
        return data;
    }

    public static byte[] compressJPEG(Bitmap bitmap, int quality) {
        return convertToBytes(bitmap, CompressFormat.JPEG, quality);
    }

    public static byte[] compressJPEG(Bitmap bitmap, int dataLength, int testStep) {
        for (int quality = 100; quality > 0; quality -= testStep) {
            byte[] data = compressJPEG(bitmap, quality);
            if (data.length < dataLength) {
                return data;
            }
        }
        return null;
    }

    public static boolean saveToFile(Bitmap bitmap, File file) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 100, outputStream);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                // nothing
            }
        }
    }

    public static boolean saveToFile(Bitmap bitmap, String path) {
        File file = new File(path);
        return saveToFile(bitmap, file);
    }

}
