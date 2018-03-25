package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class TypefaceHelper {

    private static Map<String, Typeface> sCachedTypefaces = new HashMap<>();
    private static Typeface sDefaultTypeface = null;

    public static void initialize(Context context, String defaultTypefaceAssetPath) {
        AssetManager assetManager = context.getAssets();
        sDefaultTypeface = Typeface.createFromAsset(assetManager, defaultTypefaceAssetPath);
    }

    public static Typeface get(Context context, String assetPath) {
        if (TextUtils.isEmpty(assetPath)) {
            return sDefaultTypeface;
        } else {
            Typeface typeface = sCachedTypefaces.get(assetPath);
            if (typeface == null) {
                try {
                    AssetManager assetManager = context.getAssets();
                    typeface = Typeface.createFromAsset(assetManager, assetPath);
                    sCachedTypefaces.put(assetPath, typeface);
                } catch (Exception e) {
                    return null;
                }
            }
            return typeface;
        }
    }

}
