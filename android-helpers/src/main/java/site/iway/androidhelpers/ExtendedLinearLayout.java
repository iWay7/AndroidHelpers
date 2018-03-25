package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class ExtendedLinearLayout extends LinearLayout {

    public ExtendedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public ExtendedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedLinearLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private ViewSelector mViewSelector;

    public int[] getSelectIndices() {
        return mViewSelector.getSelectIndices();
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedLinearLayout);
        boolean enableTouchSelect = a.getBoolean(R.styleable.ExtendedLinearLayout_enableTouchSelect, false);
        boolean multipleSelectMode = a.getBoolean(R.styleable.ExtendedLinearLayout_multipleSelectMode, false);
        boolean containSplitter = a.getBoolean(R.styleable.ExtendedLinearLayout_containSplitter, false);
        mViewSelector = new ViewSelector(this, enableTouchSelect, multipleSelectMode, containSplitter);
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
