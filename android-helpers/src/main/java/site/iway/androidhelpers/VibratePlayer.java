package site.iway.androidhelpers;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

@Deprecated
public final class VibratePlayer {

    private static Vibrator mVibrator = null;
    private static int mInstanceCount = 0;

    public static synchronized void play(Context context, int vibrateSpan, int sleepSpan) {
        if (mVibrator == null) {
            mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            long[] pattern = {vibrateSpan, sleepSpan};
            mVibrator.vibrate(pattern, 0);
        }
        mInstanceCount++;
    }

    public static synchronized void play(Context context) {
        play(context, 1000, 500);
    }

    public static synchronized void stop() {
        mInstanceCount--;
        if (mInstanceCount <= 0) {
            if (mVibrator != null) {
                mVibrator.cancel();
                mVibrator = null;
            }
            mInstanceCount = 0;
        }
    }

}
