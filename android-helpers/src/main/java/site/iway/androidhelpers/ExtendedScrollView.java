package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ExtendedScrollView extends ScrollView {

    public ExtendedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);
    }

    public ExtendedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedScrollView(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedScrollView);
        mFlingable = a.getBoolean(R.styleable.ExtendedScrollView_flingable, true);
        a.recycle();
    }

    private boolean mFlingable = true;

    public void setFlingable(boolean flingable) {
        mFlingable = flingable;
    }

    public boolean getFlingable() {
        return mFlingable;
    }

    @Override
    public void fling(int velocityY) {
        if (mFlingable) {
            super.fling(velocityY);
        }
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
