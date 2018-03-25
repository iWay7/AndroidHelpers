package site.iway.androidhelpers;

import android.view.View;
import android.view.View.OnClickListener;

public abstract class OnTimedClickListener implements OnClickListener {

    private long mLastClickTime;
    private int mTimeSpan;

    public OnTimedClickListener(int timeSpanInMillis) {
        mTimeSpan = timeSpanInMillis;
    }

    @Override
    public void onClick(View v) {
        if (System.currentTimeMillis() - mLastClickTime > mTimeSpan) {
            mLastClickTime = System.currentTimeMillis();
            onTimedClick(v);
        }
    }

    public abstract void onTimedClick(View v);

}
