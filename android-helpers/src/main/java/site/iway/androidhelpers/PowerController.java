package site.iway.androidhelpers;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@Deprecated
public class PowerController {

    private static WakeLock wakeLock = null;
    private static int instanceCount = 0;

    public static synchronized void keepAwake(Context context) {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            int param = PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
            wakeLock = pm.newWakeLock(param, context.getClass().getCanonicalName());
            wakeLock.acquire();
        }
        instanceCount++;
    }

    public static synchronized void allowSleep() {
        instanceCount--;
        if (instanceCount <= 0) {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
            instanceCount = 0;
        }
    }

}
