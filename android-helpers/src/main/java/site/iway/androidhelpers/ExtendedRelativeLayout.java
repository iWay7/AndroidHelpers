package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class ExtendedRelativeLayout extends RelativeLayout {

    public ExtendedRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public ExtendedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedRelativeLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private ViewSelector mViewSelector;

    public int[] getSelectIndices() {
        return mViewSelector.getSelectIndices();
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedRelativeLayout);
        boolean enableTouchSelect = a.getBoolean(R.styleable.ExtendedRelativeLayout_enableTouchSelect, false);
        boolean multipleSelectMode = a.getBoolean(R.styleable.ExtendedRelativeLayout_multipleSelectMode, false);
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
