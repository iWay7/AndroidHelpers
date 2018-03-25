package site.iway.androidhelpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageHelper {

    public static PackageInfo getPackageInfo(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            String name = context.getPackageName();
            return manager.getPackageInfo(name, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static int getPackageVersionCode(Context context) {
        try {
            return getPackageInfo(context).versionCode;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public static String getPackageVersionName(Context context) {
        try {
            return getPackageInfo(context).versionName;
        } catch (NullPointerException e) {
            return null;
        }
    }

}
