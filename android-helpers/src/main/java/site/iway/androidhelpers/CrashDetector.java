package site.iway.androidhelpers;

import java.lang.Thread.UncaughtExceptionHandler;

public class CrashDetector {

    public interface OnCrashDetectedListener {
        void onCrashDetected(Thread thread, Throwable ex);
    }

    private static OnCrashDetectedListener sListener;
    private static UncaughtExceptionHandler sOldHandler;

    private static UncaughtExceptionHandler sNewHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            if (sListener != null) {
                sListener.onCrashDetected(thread, ex);
            }
            if (sOldHandler != null) {
                sOldHandler.uncaughtException(thread, ex);
            }
        }

    };

    public static void initialize(OnCrashDetectedListener listener) {
        sListener = listener;
        sOldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(sNewHandler);
    }

}
