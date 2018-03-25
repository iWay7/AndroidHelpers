package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import site.iway.javahelpers.MathHelper;

public class TabLayout extends LinearLayout {

    public static final int ITEM_NONE = -1;

    public interface OnItemSelectedListener {
        public boolean onItemSelected(TabLayout tabLayout, int item);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public TabLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout);
        mContainSplitter = a.getBoolean(R.styleable.TabLayout_containSplitter, false);
        mInvokeSelectOnTouchDown = a.getBoolean(R.styleable.TabLayout_invokeSelectOnTouchDown, false);
        mInvokeSelectOnTouchMove = a.getBoolean(R.styleable.TabLayout_invokeSelectOnTouchMove, false);
        mInvokeSelectOnTouchUp = a.getBoolean(R.styleable.TabLayout_invokeSelectOnTouchUp, false);
        mInvokeSelectOnTouchUpRange = a.getDimension(R.styleable.TabLayout_invokeSelectOnTouchUpRange, Float.POSITIVE_INFINITY);
        a.recycle();
    }

    private OnItemSelectedListener mOnItemSelectedListener;

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        mOnItemSelectedListener = l;
    }

    private boolean mContainSplitter;

    public boolean isContainSplitter() {
        return mContainSplitter;
    }

    public void setContainSplitter(boolean contains) {
        mContainSplitter = contains;
    }

    private int mSelectedItem = ITEM_NONE;

    public int getSelectedItem() {
        return mSelectedItem;
    }

    private void setSelectedView(int index) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.setSelected(i == index);
        }
    }

    public void setSelectedItem(int item, boolean invokeItemSelected) {
        if (item == mSelectedItem) {
            return;
        }

        int childCount = getChildCount();

        if (childCount == 0) {
            if (item == ITEM_NONE) {
                mSelectedItem = item;
            } else {
                throw new RuntimeException("The item " + item + " is not selectable");
            }
        } else {
            if (item == ITEM_NONE) {
                setSelectedView(-1);
                mSelectedItem = item;
            } else {
                int selectedViewIndex = mContainSplitter ? item * 2 : item * 1;
                if (selectedViewIndex >= childCount) {
                    throw new RuntimeException("The item " + item + " is not selectable");
                } else {
                    if (mOnItemSelectedListener == null) {
                        setSelectedView(selectedViewIndex);
                        mSelectedItem = item;
                    } else {
                        boolean result = mOnItemSelectedListener.onItemSelected(this, item);
                        if (result) {
                            setSelectedView(selectedViewIndex);
                            mSelectedItem = item;
                        }
                    }
                }
            }
        }
    }

    public void setSelectedItem(int item) {
        setSelectedItem(item, true);
    }

    private boolean mInvokeSelectOnTouchDown;
    private boolean mInvokeSelectOnTouchMove;
    private boolean mInvokeSelectOnTouchUp;
    private float mInvokeSelectOnTouchUpRange;

    public boolean isInvokeSelectOnTouchDown() {
        return mInvokeSelectOnTouchDown;
    }

    public void setInvokeSelectOnTouchDown(boolean value) {
        mInvokeSelectOnTouchDown = value;
    }

    public boolean isInvokeSelectOnTouchMove() {
        return mInvokeSelectOnTouchMove;
    }

    public void setInvokeSelectOnTouchMove(boolean value) {
        mInvokeSelectOnTouchMove = value;
    }

    public boolean isInvokeSelectOnTouchUp() {
        return mInvokeSelectOnTouchUp;
    }

    public void setInvokeSelectOnTouchUp(boolean value, float range) {
        mInvokeSelectOnTouchUp = value;
        mInvokeSelectOnTouchUpRange = range;
    }

    public void setInvokeSelectOnTouchUp(boolean value) {
        mInvokeSelectOnTouchUp = value;
        mInvokeSelectOnTouchUpRange = Float.POSITIVE_INFINITY;
    }

    private float mTouchDownX;
    private float mTouchDownY;

    private void invokeSelection(MotionEvent ev) {
        if (mContainSplitter) {
            for (int i = 0; i < getChildCount(); i += 2) {
                if (getChildAt(i).isEnabled() && ViewHelper.isMotionEventInView(ev, getChildAt(i))) {
                    setSelectedItem(i / 2);
                    break;
                }
            }
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i).isEnabled() && ViewHelper.isMotionEventInView(ev, getChildAt(i))) {
                    setSelectedItem(i);
                    break;
                }
            }
        }
    }

    private boolean mSuperHandledTouchEvent;

    private void handleSuperTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mSuperHandledTouchEvent = super.onTouchEvent(event);
        } else {
            if (mSuperHandledTouchEvent) {
                mSuperHandledTouchEvent = super.onTouchEvent(event);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        handleSuperTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = ev.getRawX();
                mTouchDownY = ev.getRawY();
                if (mInvokeSelectOnTouchDown) {
                    invokeSelection(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mInvokeSelectOnTouchMove) {
                    invokeSelection(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mInvokeSelectOnTouchUp) {
                    float dx = ev.getRawX() - mTouchDownX;
                    float dy = ev.getRawY() - mTouchDownY;
                    double dxy = MathHelper.distance(dx, dy);
                    if (dxy < mInvokeSelectOnTouchUpRange) {
                        invokeSelection(ev);
                    }
                }
                break;
        }
        return true;
    }

}
