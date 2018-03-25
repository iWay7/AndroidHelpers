package site.iway.androidhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class ExtendedScrollViewHorizontal extends HorizontalScrollView {

    public ExtendedScrollViewHorizontal(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExtendedScrollViewHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedScrollViewHorizontal(Context context) {
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
