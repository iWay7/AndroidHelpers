package site.iway.androidhelpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

@Deprecated
public class SettingsHelper {

    private static void startActivity(Context context, String packageName, String className) {
        ComponentName componentName = new ComponentName(packageName, className);
        Intent intent = new Intent();
        intent.setComponent(componentName);
        context.startActivity(intent);
    }

    public static void goSettings(Context context) {
        String packageName = "com.android.settings";
        String className = "com.android.settings.Settings";
        startActivity(context, className, packageName);
    }

    public static void goManageApplications(Context context) {
        String packageName = "com.android.settings";
        String className = "com.android.settings.ManageApplications";
        startActivity(context, className, packageName);
    }

    public static void goManageWirelessSettings(Context context) {
        String packageName = "com.android.settings";
        String className = "com.android.settings.WirelessSettings";
        startActivity(context, className, packageName);
    }

}
