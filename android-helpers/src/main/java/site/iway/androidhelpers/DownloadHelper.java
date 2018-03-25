package site.iway.androidhelpers;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import site.iway.javahelpers.AutoExpandLongArray;

@Deprecated
public class DownloadHelper {

    private static DownloadManager sDownloadManager;
    private static AutoExpandLongArray sDownloadIds;

    public static void initialize(Context context) {
        sDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        sDownloadIds = new AutoExpandLongArray(2);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (sDownloadIds.has(downloadId)) {
                    Object service = context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager downloadManager = (DownloadManager) service;
                    Query query = new Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(query);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int indexLocalUri = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                            int indexStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            String localUri = cursor.getString(indexLocalUri);
                            int status = cursor.getInt(indexStatus);
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                Intent it = new Intent();
                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                it.setAction(Intent.ACTION_VIEW);
                                it.setDataAndType(Uri.parse(localUri), "application/vnd.android.package-archive");
                                context.startActivity(it);
                            }
                        }
                        cursor.close();
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static boolean download(String url) {
        Uri uri = Uri.parse(url);
        String apkName = uri.getLastPathSegment();
        String directory = Environment.DIRECTORY_DOWNLOADS;
        File directoryFile = Environment.getExternalStoragePublicDirectory(directory);
        directoryFile.mkdirs();
        if (directoryFile.exists() && directoryFile.isDirectory()) {
            Request request = new Request(uri);
            request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(false);
            request.setTitle(apkName);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
            request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            sDownloadIds.add(sDownloadManager.enqueue(request));
            return true;
        }
        return false;
    }

}
