package site.iway.androidhelpers;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceHelper {

    public static void callPhone(Context context, String phoneNumber) {
        Uri uri = Uri.parse("tel:" + phoneNumber);
        Intent phoneIntent = new Intent(Intent.ACTION_CALL, uri);
        context.startActivity(phoneIntent);
    }

    public static void sendSms(Context context, String content, String... receivers) {
        StringBuilder stringBuilder = new StringBuilder();
        String splitter = ";";
        for (String receiver : receivers) {
            stringBuilder.append(splitter);
            stringBuilder.append(receiver);
        }
        String receiver = stringBuilder.substring(splitter.length());
        Uri uri = Uri.parse("smsto:" + receiver);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", content);
        context.startActivity(intent);
    }

    public static String getSDCardPath() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        return externalStorageDirectory.getPath();
    }

    public static boolean isSDCardInstalled() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }

    public static long getSDCardFreeSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            long blockSize = sf.getBlockSize();
            long freeBlocks = sf.getAvailableBlocks();
            return freeBlocks * blockSize;
        } else {
            long blockSize = sf.getBlockSizeLong();
            long freeBlocks = sf.getAvailableBlocksLong();
            return freeBlocks * blockSize;
        }
    }

    public static long getSDCardTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            long blockSize = sf.getBlockSize();
            long allBlocks = sf.getBlockCount();
            return allBlocks * blockSize;
        } else {
            long blockSize = sf.getBlockSizeLong();
            long allBlocks = sf.getBlockCountLong();
            return allBlocks * blockSize;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        Object service = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) service;
        if (connectivityManager != null) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWiFiConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null && info.getIpAddress() != 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFlightModeOn(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        int result = Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, 0);
        return result == 1;
    }

    public static void turnOnFlightMode(Context context) {
        if (!isFlightModeOn(context)) {
            ContentResolver contentResolver = context.getContentResolver();
            Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            context.sendBroadcast(intent);
        }
    }

    public static void turnOffFlightMode(Context context) {
        if (isFlightModeOn(context)) {
            ContentResolver contentResolver = context.getContentResolver();
            Settings.System.putInt(contentResolver, Settings.System.AIRPLANE_MODE_ON, 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            context.sendBroadcast(intent);
        }
    }

    public static int getHeapGrowthLimit(Context context) {
        Object service = context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager activityManager = (ActivityManager) service;
        int memoryClass = activityManager.getMemoryClass();
        return memoryClass * 1024 * 1024;
    }

    public static int getHeapSize(Context context) {
        Object service = context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager activityManager = (ActivityManager) service;
        int largeMemoryClass = activityManager.getLargeMemoryClass();
        return largeMemoryClass * 1024 * 1024;
    }

    public static int getCPUCoreCount() {
        File cpuDirectory = new File("/sys/devices/system/cpu/");
        File[] cpuFiles = cpuDirectory.listFiles();
        if (cpuFiles == null) {
            return 0;
        }
        int cpuCoreCount = 0;
        Pattern pattern = Pattern.compile("cpu[0-9]*");
        for (File cpuFile : cpuFiles) {
            String name = cpuFile.getName();
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                cpuCoreCount++;
            }
        }
        return cpuCoreCount;
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null) {
                return info.getMacAddress();
            }
        }
        return null;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static String getSubscriberId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSubscriberId();
    }

    public static boolean isSimulator(Context context) {
        return "000000000000000".equals(getDeviceId(context));
    }

    public static boolean isSensorExisted(Context context, int sensorType) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensorManager != null && sensorManager.getDefaultSensor(sensorType) != null;
    }

}
