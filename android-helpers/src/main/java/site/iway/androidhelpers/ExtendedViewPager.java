package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.reflect.Field;

public class ExtendedViewPager extends ViewPager {

    public ExtendedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedViewPager(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedViewPager);
        int customizeScrollerDuration = a.getInt(R.styleable.ExtendedViewPager_customizeScrollDuration, 0);
        customizeScrollerDuration(customizeScrollerDuration);
        int autoSwitchTime = a.getInt(R.styleable.ExtendedViewPager_autoSwitchTime, 0);
        setAutoSwitchTime(autoSwitchTime);
        mUserTouchable = a.getBoolean(R.styleable.ExtendedViewPager_userTouchable, true);
        a.recycle();
    }

    public void customizeScrollerDuration(int customizedScrollerDuration) {
        try {
            if (customizedScrollerDuration > 0) {
                Context context = getContext();
                ExtendedScroller extendedScroller = new ExtendedScroller(context);
                extendedScroller.customizeDuration(customizedScrollerDuration);
                Field localField = ViewPager.class.getDeclaredField("mScroller");
                localField.setAccessible(true);
                localField.set(this, extendedScroller);
            }
        } catch (IllegalAccessException localIllegalAccessException) {
            // nothing
        } catch (IllegalArgumentException localIllegalArgumentException) {
            // nothing
        } catch (NoSuchFieldException localNoSuchFieldException) {
            // nothing
        }
    }

    private boolean mAttachedToWindow;
    private UITimer mAutoSwitchTimer;

    public void setAutoSwitchTime(int millis) {
        if (millis > 0) {
            mAutoSwitchTimer = new UITimer(millis) {
                @Override
                public void doOnUIThread() {
                    PagerAdapter adapter = getAdapter();
                    if (adapter == null || adapter.getCount() < 2) {
                        return;
                    }
                    int index = getCurrentItem();
                    int count = adapter.getCount();
                    index += 1;
                    if (index >= count)
                        index = 0;
                    setCurrentItem(index);
                }

            };
            if (mAttachedToWindow) {
                mAutoSwitchTimer.start(false);
            }
        } else {
            if (mAutoSwitchTimer != null) {
                mAutoSwitchTimer.stop();
                mAutoSwitchTimer = null;
            }
        }
    }

    private boolean mUserTouchable = true;

    public void setUserTouchable(boolean userTouchable) {
        mUserTouchable = userTouchable;
    }

    private boolean mSuperHandledTouchEvent;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mUserTouchable)
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        if (mAutoSwitchTimer != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mAutoSwitchTimer.stop();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mAutoSwitchTimer.start(false);
                    break;
            }
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        if (mAutoSwitchTimer != null) {
            mAutoSwitchTimer.start(false);
            mAutoSwitchTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAutoSwitchTimer != null) {
            mAutoSwitchTimer.stop();
            mAutoSwitchTimer = null;
        }
        mAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

}
