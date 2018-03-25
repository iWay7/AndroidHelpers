package site.iway.androidhelpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class SMSObserver extends ContentObserver {

    public interface OnSMSListener {
        public void onSMSChanged(boolean selfChange);
    }

    private Context mContext;
    private ContentResolver mContentResolver;
    private Uri mUri;

    private SMSObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mUri = Uri.parse("content://sms/");
    }

    private OnSMSListener mOnSMSListener;

    public void setOnSMSChangeListener(OnSMSListener l) {
        mOnSMSListener = l;
    }

    @Override
    public void onChange(boolean selfChange) {
        if (mOnSMSListener != null) {
            mOnSMSListener.onSMSChanged(selfChange);
        }
    }

    public void start() {
        mContentResolver.registerContentObserver(mUri, true, this);
    }

    public void stop() {
        mContentResolver.unregisterContentObserver(this);
    }

}