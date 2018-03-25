package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import site.iway.javahelpers.TextRW;

public class AssetsHelper {

    public static String readTextFile(Context context, String name, Charset charset) {
        AssetManager manager = context.getAssets();
        try {
            InputStream stream = manager.open(name);
            String text = TextRW.readAllText(stream, charset);
            stream.close();
            return text;
        } catch (IOException e) {
            return null;
        }
    }

    public static String readTextFile(Context context, String name) {
        return readTextFile(context, name, Charset.defaultCharset());
    }

    public static Bitmap readImageFile(Context context, String name, Rect outPadding, Options opts) {
        AssetManager manager = null;
        InputStream stream = null;
        try {
            manager = context.getAssets();
            stream = manager.open(name);
            return BitmapFactory.decodeStream(stream, outPadding, opts);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                // nothing
            }
        }
    }

    public static Bitmap readImageFile(Context context, String name) {
        return readImageFile(context, name, null, null);
    }

}
