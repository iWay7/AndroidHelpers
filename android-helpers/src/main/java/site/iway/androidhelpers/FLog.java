package site.iway.androidhelpers;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import site.iway.javahelpers.CalendarHelper;
import site.iway.javahelpers.StringHelper;

public class FLog {

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int NONE = 8;

    private static File sLogDirectory;
    private static int sLogLevel;
    private static String sDefaultTag;
    private static List<String> sCachedRecords;
    private static Object sSynchronizer;
    private static boolean sPaused;
    private static FileOutputStream sOutputStream;
    private static PrintStream sPrintStream;

    public static void initialize(String logDirectory, int logLevel, String defaultTag) {
        sLogDirectory = new File(logDirectory);
        if (sLogDirectory.exists()) {
            if (!sLogDirectory.isDirectory()) {
                throw new RuntimeException("The log directory is not a directory.");
            }
        } else {
            if (!sLogDirectory.mkdirs()) {
                throw new RuntimeException("Create log directory failed.");
            }
        }
        sLogLevel = logLevel;
        sDefaultTag = defaultTag;
        if (StringHelper.nullOrEmpty(sDefaultTag)) {
            sDefaultTag = "FLog";
        }
        sCachedRecords = new ArrayList<>();
        sSynchronizer = new Object();
        sPaused = true;
        resumeFileLog();
    }

    private static void logFile(String level, String tag, String message) {
        synchronized (sSynchronizer) {
            message = StringHelper.trim(message, true, true, ' ', '\n');
            if (StringHelper.nullOrEmpty(message)) {
                return;
            }
            Calendar now = CalendarHelper.now();
            String nowString = CalendarHelper.format(now, "yyyy-MM-dd HH:mm:ss.SSS");
            message = nowString + " " + level + "/" + tag + " " + message;
            if (sPaused) {
                sCachedRecords.add(message);
            } else {
                sPrintStream.print(message);
                sPrintStream.println();
                sPrintStream.flush();
            }
        }
    }

    private static void createStreams() {
        try {
            Calendar now = CalendarHelper.now();
            String fileName = CalendarHelper.format(now, "yyyyMMddHHmmssSSS");
            File logFile = new File(sLogDirectory, fileName);
            sOutputStream = new FileOutputStream(logFile, true);
            sPrintStream = new PrintStream(sOutputStream);
        } catch (Exception e) {
            // nothing
        }
    }

    private static void closeStreams() {
        sPrintStream.close();
    }

    private static void flushRecords() {
        for (String record : sCachedRecords) {
            sPrintStream.print(record);
            sPrintStream.println();
        }
        sPrintStream.flush();
        sCachedRecords.clear();
    }

    public static void pauseFileLog() {
        synchronized (sSynchronizer) {
            if (sPaused) {
                return;
            }
            closeStreams();
            sPaused = true;
        }
    }

    public static void resumeFileLog() {
        synchronized (sSynchronizer) {
            if (!sPaused) {
                return;
            }
            createStreams();
            flushRecords();
            sPaused = false;
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE >= sLogLevel) {
            logFile("V", tag, msg);
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        v(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void v(String msg) {
        v(sDefaultTag, msg);
    }

    public static void v(String msg, Throwable tr) {
        v(sDefaultTag, msg, tr);
    }

    public static void d(String tag, String msg) {
        if (DEBUG >= sLogLevel) {
            logFile("D", tag, msg);
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        d(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void d(String msg) {
        d(sDefaultTag, msg);
    }

    public static void d(String msg, Throwable tr) {
        d(sDefaultTag, msg, tr);
    }

    public static void i(String tag, String msg) {
        if (INFO >= sLogLevel) {
            logFile("I", tag, msg);
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        i(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void i(String msg) {
        i(sDefaultTag, msg);
    }

    public static void i(String msg, Throwable tr) {
        i(sDefaultTag, msg, tr);
    }

    public static void w(String tag, String msg) {
        if (WARN >= sLogLevel) {
            logFile("W", tag, msg);
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        w(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void w(String msg) {
        w(sDefaultTag, msg);
    }

    public static void w(String msg, Throwable tr) {
        w(sDefaultTag, msg, tr);
    }

    public static void e(String tag, String msg) {
        if (ERROR >= sLogLevel) {
            logFile("E", tag, msg);
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        e(tag, msg + '\n' + getStackTraceString(tr));
    }

    public static void e(String msg) {
        e(sDefaultTag, msg);
    }

    public static void e(String msg, Throwable tr) {
        e(sDefaultTag, msg, tr);
    }

    public static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }

}
