package site.iway.androidhelpers;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class TouchAllLayout extends FrameLayout {

    public TouchAllLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchAllLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchAllLayout(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
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

    private int mChildCount;
    private boolean[] mTouchResults;
    private Rect mHitRect = new Rect();

    private boolean onChildTouchEvent(int childIndex, MotionEvent event) {
        View child = getChildAt(childIndex);
        int action = event.getAction();
        float offsetX = getScrollX() - child.getLeft();
        float offsetY = getScrollY() - child.getTop();
        float x = event.getX() + offsetX;
        float y = event.getY() + offsetY;
        child.getHitRect(mHitRect);
        if (mHitRect.contains((int) x, (int) y)) {
            event.offsetLocation(offsetX, offsetY);
            if (action == MotionEvent.ACTION_DOWN) {
                mTouchResults[childIndex] = child.dispatchTouchEvent(event);
            } else {
                if (mTouchResults[childIndex]) {
                    mTouchResults[childIndex] = child.dispatchTouchEvent(event);
                }
            }
            event.offsetLocation(-offsetX, -offsetY);
        }
        return mTouchResults[childIndex];
    }

    private boolean onChildrenTouchEvents(MotionEvent event) {
        boolean hasChildHandled = false;
        for (int i = mChildCount - 1; i >= 0; i--) {
            hasChildHandled |= onChildTouchEvent(i, event);
        }
        return hasChildHandled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mChildCount = getChildCount();
            mTouchResults = new boolean[mChildCount];
            for (int i = 0; i < mChildCount; i++) {
                mTouchResults[i] = false;
            }
        }
        return onSuperTouchEvent(event) || onChildrenTouchEvents(event);
    }

}
