package site.iway.androidhelpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class WindowHelper {

    public static boolean makeTranslucent(Activity activity, boolean status, boolean navigation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            if (status) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            if (navigation) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean makeTranslucent(Activity activity) {
        return makeTranslucent(activity, true, true);
    }

    public static int getWindowRotationDegrees(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources resources = context.getResources();
        return resources.getDisplayMetrics();
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return displayMetrics.heightPixels;
    }

    public static boolean showSoftInput(View view) {
        Context context = view.getContext();
        Object service = context.getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodManager inputMethodManager = (InputMethodManager) service;
        return inputMethodManager.showSoftInput(view, 0);
    }

    public static boolean hideSoftInput(View view) {
        Context context = view.getContext();
        Object service = context.getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodManager inputMethodManager = (InputMethodManager) service;
        IBinder windowToken = view.getWindowToken();
        return inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }

}