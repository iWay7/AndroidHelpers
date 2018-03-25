package site.iway.androidhelpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by iWay on 2015/12/2.
 */
public class SMSTraverser {

    public interface OnSMSReadListener {
        public void onSMSRead(String address, String content);
    }

    private Context mContext;
    private ContentResolver mContentResolver;
    private Uri mUri;
    private String[] mQueryContent;

    public SMSTraverser(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mUri = Uri.parse("content://sms/inbox");
        mQueryContent = new String[]{"_id", "address", "body"};
    }

    private OnSMSReadListener mOnSMSReadListener;

    public void setOnSMSReadListener(OnSMSReadListener l) {
        mOnSMSReadListener = l;
    }

    public void doTraverse() {
        Cursor cursor = mContentResolver.query(mUri, mQueryContent, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                if (mOnSMSReadListener != null) {
                    mOnSMSReadListener.onSMSRead(address, body);
                }
            }
        }
    }

}