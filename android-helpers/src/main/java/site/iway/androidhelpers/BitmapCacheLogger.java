package site.iway.androidhelpers;

import android.util.Log;

class BitmapCacheLogger {

    static final String LOG_TAG = "BitmapCache";

    static void logVerbose(String... messages) {
        if (BitmapCache.IS_DEBUG_MODE) {
            for (String message : messages) {
                if (message != null) {
                    Log.v(LOG_TAG, message);
                }
            }
        }
    }

    static void logWarn(String... messages) {
        if (BitmapCache.IS_DEBUG_MODE) {
            for (String message : messages) {
                if (message != null) {
                    Log.w(LOG_TAG, message);
                }
            }
        }
    }

    static void logError(String... messages) {
        if (BitmapCache.IS_DEBUG_MODE) {
            for (String message : messages) {
                if (message != null) {
                    Log.e(LOG_TAG, message);
                }
            }
        }
    }

    static void logError(Exception... exceptions) {
        if (BitmapCache.IS_DEBUG_MODE) {
            for (Exception exception : exceptions) {
                if (exception != null) {
                    String stackTraceString = Log.getStackTraceString(exception);
                    Log.e(LOG_TAG, stackTraceString);
                }
            }
        }
    }

}
