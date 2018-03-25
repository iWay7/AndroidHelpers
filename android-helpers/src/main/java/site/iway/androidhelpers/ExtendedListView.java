package site.iway.androidhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ExtendedListView extends ListView {

    public ExtendedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtendedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedListView(Context context) {
        super(context);
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    public void setOnScrollChangedListener(OnScrollChangedListener l) {
        mOnScrollChangedListener = l;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

}
