package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class ViewSwapper extends FrameLayout {

    public ViewSwapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttr(context, attrs);
    }

    public ViewSwapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttr(context, attrs);
    }

    public ViewSwapper(Context context) {
        super(context);
        resolveAttr(context, null);
    }

    private boolean mAttachedToWindow;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        setDisplayedChild(mDisplayedChild, false);
    }

    private Animation mInAnimation;
    private Animation mOutAnimation;
    private int mDisplayedChild;

    public Animation getInAnimation() {
        return mInAnimation;
    }

    public void setInAnimation(Animation animation) {
        mInAnimation = animation;
    }

    public void setInAnimation(int resourceId) {
        if (resourceId != 0) {
            Context context = getContext();
            Animation animation = AnimationUtils.loadAnimation(context, resourceId);
            setInAnimation(animation);
        }
    }

    public Animation getOutAnimation() {
        return mOutAnimation;
    }

    public void setOutAnimation(Animation animation) {
        mOutAnimation = animation;
    }

    public void setOutAnimation(int resourceId) {
        if (resourceId != 0) {
            Context context = getContext();
            Animation animation = AnimationUtils.loadAnimation(context, resourceId);
            setOutAnimation(animation);
        }
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewSwapper);
        mDisplayedChild = a.getInt(R.styleable.ViewSwapper_displayedChild, 0);
        setInAnimation(a.getResourceId(R.styleable.ViewSwapper_inAnimation, 0));
        setOutAnimation(a.getResourceId(R.styleable.ViewSwapper_outAnimation, 0));
        a.recycle();
        setMeasureAllChildren(true);
    }

    public int getDisplayedChild() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int visibility = child.getVisibility();
            if (visibility == View.VISIBLE) {
                return i;
            }
        }
        return -1;
    }

    public void setDisplayedChild(int index, boolean animated) {
        mDisplayedChild = index;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.clearAnimation();
            int visibility = child.getVisibility();
            if (i == index) {
                if (visibility == View.VISIBLE) {
                    // nothing
                } else {
                    child.setVisibility(View.VISIBLE);
                    if (mAttachedToWindow && mInAnimation != null && animated) {
                        child.startAnimation(mInAnimation);
                    }
                }
            } else {
                if (visibility == View.VISIBLE) {
                    child.setVisibility(View.GONE);
                    if (mAttachedToWindow && mOutAnimation != null && animated) {
                        child.startAnimation(mOutAnimation);
                    }
                } else {
                    // nothing
                }
            }
        }
    }

    public void setDisplayedChild(int index) {
        setDisplayedChild(index, true);
    }

    protected boolean mUserInteractEnabled = true;

    public void disableUserInteract() {
        mUserInteractEnabled = false;
    }

    public void enableUserInteract() {
        mUserInteractEnabled = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mUserInteractEnabled) {
            return super.dispatchTouchEvent(ev);
        } else {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    return false;
                default:
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    boolean superResult = super.dispatchTouchEvent(ev);
                    ev.setAction(action);
                    return false;
            }
        }
    }

}
