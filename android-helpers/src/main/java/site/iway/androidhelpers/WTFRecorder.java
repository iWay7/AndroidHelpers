package site.iway.androidhelpers;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;

import site.iway.javahelpers.CalendarHelper;

public class WTFRecorder {

    private static final String LOG_TAG = "WTFRecorder";

    private static Context sContext;
    private static String sDirectory;

    private static UncaughtExceptionHandler sOldHandler;

    private static UncaughtExceptionHandler sNewHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.wtf(LOG_TAG, ex);

            File rootFilesDir = sContext.getFilesDir();
            File logFilesDir = new File(rootFilesDir, sDirectory);
            if (!logFilesDir.exists() && !logFilesDir.mkdir()) {
                Log.e(LOG_TAG, "Record failed : Can not create directory " + sDirectory);
                return;
            }

            Calendar time = Calendar.getInstance();
            String timeString = CalendarHelper.format(time, "yyyy-MM-dd HHmmss");
            String fileName = timeString + ".txt";

            FileOutputStream fileOutputStream = null;
            try {
                File file = new File(logFilesDir, fileName);
                fileOutputStream = new FileOutputStream(file);
                PrintStream printStream = new PrintStream(fileOutputStream);
                ex.printStackTrace(printStream);
                printStream.close();
                Log.e(LOG_TAG, "Record succeeded : File saved at " + fileName);
            } catch (Exception e) {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (Exception exception) {
                    // nothing
                }
                Log.e(LOG_TAG, "Record failed : Can not write file " + fileName);
            } finally {
                if (sOldHandler != null) {
                    sOldHandler.uncaughtException(thread, ex);
                }
            }
        }

    };

    public static void initialize(Context context, String directory) {
        sContext = context;
        sDirectory = directory;
        sOldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(sNewHandler);
    }

}
