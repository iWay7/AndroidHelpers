package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ExtendedFrameLayout extends FrameLayout {

    public ExtendedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public ExtendedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedFrameLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private ViewSelector mViewSelector;

    public int[] getSelectIndices() {
        return mViewSelector.getSelectIndices();
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedFrameLayout);
        boolean enableTouchSelect = a.getBoolean(R.styleable.ExtendedFrameLayout_enableTouchSelect, false);
        boolean multipleSelectMode = a.getBoolean(R.styleable.ExtendedFrameLayout_multipleSelectMode, false);
        mViewSelector = new ViewSelector(this, enableTouchSelect, multipleSelectMode, false);
        a.recycle();
    }

    private boolean mSuperHandledTouchEvent;

    private boolean onSuperTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mSuperHandledTouchEvent = super.onTouchEvent(event);
        } else {
            if (mSuperHandledTouchEvent) {
                mSuperHandledTouchEvent = super.onTouchEvent(event);
            }
        }
        return mSuperHandledTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onSuperTouchEvent(event) || mViewSelector.onTouchEvent(event);
    }

}
