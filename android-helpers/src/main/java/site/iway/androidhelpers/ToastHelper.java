package site.iway.androidhelpers;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

@Deprecated
public class ToastHelper {

    private static Context sContext;
    private static Toast sLastToast;

    public static void initialize(Context context) {
        sContext = context;
    }

    public static void cancelLastToast() {
        if (sLastToast != null) {
            sLastToast.cancel();
        }
    }

    public static Toast show(String text) {
        cancelLastToast();
        sLastToast = Toast.makeText(sContext, text, Toast.LENGTH_SHORT);
        sLastToast.show();
        return sLastToast;
    }

    public static Toast show(int resId) {
        cancelLastToast();
        sLastToast = Toast.makeText(sContext, resId, Toast.LENGTH_SHORT);
        sLastToast.show();
        return sLastToast;
    }

    public static Toast showLong(String text) {
        cancelLastToast();
        sLastToast = Toast.makeText(sContext, text, Toast.LENGTH_LONG);
        sLastToast.show();
        return sLastToast;
    }

    public static Toast showLong(int resId) {
        cancelLastToast();
        sLastToast = Toast.makeText(sContext, resId, Toast.LENGTH_LONG);
        sLastToast.show();
        return sLastToast;
    }

    public static Toast showCenter(String text) {
        cancelLastToast();
        sLastToast = Toast.makeText(sContext, text, Toast.LENGTH_SHORT);
        sLastToast.setGravity(Gravity.CENTER, 0, 0);
        sLastToast.show();
        return sLastToast;
    }

    public static Toast showCenter(int resId) {
        cancelLastToast();
        String string = sContext.getString(resId);
        return showCenter(string);
    }

    public static Toast showCenterLong(String text) {
        cancelLastToast();
        sLastToast = Toast.makeText(sContext, text, Toast.LENGTH_LONG);
        sLastToast.setGravity(Gravity.CENTER, 0, 0);
        sLastToast.show();
        return sLastToast;
    }

    public static Toast showCenterLong(int resId) {
        cancelLastToast();
        String string = sContext.getString(resId);
        return showCenterLong(string);
    }

}
