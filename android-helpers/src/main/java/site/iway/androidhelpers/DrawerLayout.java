package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import site.iway.javahelpers.MathHelper;

public class DrawerLayout extends ViewGroup {

    private int mMotionDetectXRangeInPixel;
    private int mMotionDetectYRangeInPixel;

    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);

    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public DrawerLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private int mLeftViewIdFromAttr;
    private int mRightViewIdFromAttr;
    private int mCenterViewIdFromAttr;

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawerLayout);
        mLeftViewIdFromAttr = a.getResourceId(R.styleable.DrawerLayout_leftViewId, 0);
        mRightViewIdFromAttr = a.getResourceId(R.styleable.DrawerLayout_rightViewId, 0);
        mCenterViewIdFromAttr = a.getResourceId(R.styleable.DrawerLayout_centerViewId, 0);
        a.recycle();
        mMotionDetectXRangeInPixel = UnitHelper.dipToPxInt(context, 7.5f);
        mMotionDetectYRangeInPixel = UnitHelper.dipToPxInt(context, 7.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    private boolean canShowLeftView() {
        return mLeftView != null && !mCenterView.canScrollHorizontally(-1);
    }

    private boolean canShowRightView() {
        return mRightView != null && !mCenterView.canScrollHorizontally(1);
    }

    private View mLeftView;
    private View mRightView;
    private View mCenterView;

    public boolean isLeftViewPartVisible() {
        return mCenterViewOffset > 0;
    }

    public boolean isLeftViewWholeVisible() {
        return mCenterViewOffset >= mLeftView.getWidth();
    }

    public boolean isRightViewPartVisible() {
        return mCenterViewOffset < 0;
    }

    public boolean isRightViewWholeVisible() {
        return mCenterViewOffset <= -mRightView.getWidth();
    }

    public boolean isCenterViewPartVisible() {
        return mCenterViewOffset < 0 || mCenterViewOffset > 0;
    }

    public boolean isCenterViewWholeVisible() {
        return mCenterViewOffset == 0;
    }

    private void setViewVisibility(boolean leftView, boolean rightView) {
        if (mLeftView != null) {
            mLeftView.setVisibility(leftView ? View.VISIBLE : View.INVISIBLE);
        }
        if (mRightView != null) {
            mRightView.setVisibility(rightView ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private int mLeftViewOffset = 0;
    private int mRightViewOffset = 0;
    private int mCenterViewOffset = 0;

    public int getLeftViewOffset() {
        return mLeftViewOffset;
    }

    public int getRightViewOffset() {
        return mRightViewOffset;
    }

    public int getCenterViewOffset() {
        return mCenterViewOffset;
    }

    private void layoutLeftView() {
        int left = mLeftViewOffset;
        int right = left + mLeftView.getWidth();
        int top = mLeftView.getTop();
        int bottom = mLeftView.getBottom();
        mLeftView.layout(left, top, right, bottom);
    }

    private void layoutRightView() {
        int left = mRightViewOffset + getWidth() - mRightView.getWidth();
        int right = left + mRightView.getWidth();
        int top = mRightView.getTop();
        int bottom = mRightView.getBottom();
        mRightView.layout(left, top, right, bottom);
    }

    private void layoutCenterView() {
        int left = 0 + mCenterViewOffset;
        int right = left + mCenterView.getWidth();
        int top = mCenterView.getTop();
        int bottom = mCenterView.getBottom();
        mCenterView.layout(left, top, right, bottom);
    }

    protected float getHidingPercentOfSideViews() {
        return 50f;
    }

    protected void onViewsReLayout() {
        // nothing
    }

    private void moveViews(int centerViewOffset) {
        if (mCenterViewOffset == centerViewOffset)
            return;
        float hidingPercentOfSideViews = getHidingPercentOfSideViews();
        if (hidingPercentOfSideViews > 100f)
            hidingPercentOfSideViews = 100f;
        if (hidingPercentOfSideViews < 0f)
            hidingPercentOfSideViews = 0f;
        if (mLeftView != null && centerViewOffset > 0) {
            float mLeftViewOffsetF = -hidingPercentOfSideViews * (mLeftView.getWidth() - centerViewOffset) / 100f;
            mLeftViewOffset = MathHelper.pixel(mLeftViewOffsetF);
            layoutLeftView();
        }
        if (mRightView != null && centerViewOffset < 0) {
            float mRightViewOffsetF = hidingPercentOfSideViews * (mRightView.getWidth() + centerViewOffset) / 100f;
            mRightViewOffset = MathHelper.pixel(mRightViewOffsetF);
            layoutRightView();
        }
        if (mCenterView != null) {
            mCenterViewOffset = centerViewOffset;
            layoutCenterView();
        }
        onViewsReLayout();
    }

    private float mCurrentCenterViewOffset = 0;
    private float mTargetCenterViewOffset = 0;

    private UITimer mAnimator = new UITimer() {

        @Override
        public void doOnUIThread() {
            float change = (mTargetCenterViewOffset - mCurrentCenterViewOffset) * 0.15f;
            if (change == 0) {
                stop();
            } else if (Math.abs(mTargetCenterViewOffset - mCurrentCenterViewOffset) < 1) {
                moveViews((int) mTargetCenterViewOffset);
                stop();
            } else {
                mCurrentCenterViewOffset += change;
                moveViews((int) mCurrentCenterViewOffset);
            }
        }

    };

    private void animateCenterViewTo(int offset) {
        mCurrentCenterViewOffset = mCenterViewOffset;
        mTargetCenterViewOffset = offset;
        mAnimator.start(true);
    }

    public void showLeftView() {
        if (mLeftView == null) {
            return;
        }
        setViewVisibility(true, false);
        bringChildToFront(mLeftView);
        bringChildToFront(mCenterView);
        animateCenterViewTo(mLeftView.getWidth());
    }

    public void showRightView() {
        if (mRightView == null) {
            return;
        }
        setViewVisibility(false, true);
        bringChildToFront(mRightView);
        bringChildToFront(mCenterView);
        animateCenterViewTo(-mRightView.getWidth());
    }

    public void showCenterView() {
        if (mCenterView == null) {
            return;
        }
        animateCenterViewTo(0);
    }

    private boolean mShouldHandleSelfDefined = false;
    private boolean mShouldHandleSelf = false;

    private float mSavedTouchDownX;
    private float mSavedTouchDownY;
    private long mSavedTouchDownTime;
    private float mSavedCenterViewOffset;

    private static final int TO_SHOW_LEFT_VIEW = -1;
    private static final int TO_SHOW_NONE = 0;
    private static final int TO_SHOW_RIGHT_VIEW = 1;

    private int mToShowTarget;

    private void saveStates(MotionEvent ev) {
        mSavedTouchDownX = ev.getX();
        mSavedTouchDownY = ev.getY();
        mSavedTouchDownTime = System.currentTimeMillis();
        mSavedCenterViewOffset = mCenterViewOffset;
    }

    private boolean mShouldDispatchToLeftView;
    private boolean mShouldDispatchToRightView;
    private boolean mShouldDispatchToCenterView;

    private void dispatchMotionEventToViews(MotionEvent ev) {
        if (mShouldDispatchToLeftView && isLeftViewPartVisible()) {
            float offsetX = mLeftView.getLeft();
            float offsetY = mLeftView.getTop();
            ev.offsetLocation(-offsetX, -offsetY);
            mShouldDispatchToLeftView = mLeftView.dispatchTouchEvent(ev);
            ev.offsetLocation(offsetX, offsetY);
        }
        if (mShouldDispatchToRightView && isRightViewPartVisible()) {
            float offsetX = mRightView.getLeft();
            float offsetY = mRightView.getTop();
            ev.offsetLocation(-offsetX, -offsetY);
            mShouldDispatchToLeftView = mRightView.dispatchTouchEvent(ev);
            ev.offsetLocation(+offsetX, +offsetY);
        }
        if (mShouldDispatchToCenterView && isCenterViewWholeVisible()) {
            float offsetX = mCenterView.getLeft();
            float offsetY = mCenterView.getTop();
            ev.offsetLocation(-offsetX, -offsetY);
            mShouldDispatchToLeftView = mCenterView.dispatchTouchEvent(ev);
            ev.offsetLocation(+offsetX, +offsetY);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mAnimator.stop();
                mShouldHandleSelfDefined = false;
                mShouldHandleSelf = false;
                mToShowTarget = TO_SHOW_NONE;
                mShouldDispatchToLeftView = false;
                mShouldDispatchToRightView = false;
                mShouldDispatchToCenterView = false;
                if (mCenterViewOffset != 0) {
                    mShouldHandleSelfDefined = true;
                    if (ViewHelper.isMotionEventInView(ev, mCenterView)) {
                        mShouldHandleSelf = true;
                        if (mCenterViewOffset > 0) {
                            mToShowTarget = TO_SHOW_LEFT_VIEW;
                        } else {
                            mToShowTarget = TO_SHOW_RIGHT_VIEW;
                        }
                        saveStates(ev);
                    } else {
                        mShouldHandleSelf = false;
                        if (isLeftViewPartVisible()) {
                            mShouldDispatchToLeftView = mLeftView.dispatchTouchEvent(ev);
                            showLeftView();
                        }
                        if (isRightViewPartVisible()) {
                            mShouldDispatchToRightView = mRightView.dispatchTouchEvent(ev);
                            showRightView();
                        }
                    }
                } else {
                    saveStates(ev);
                    mShouldDispatchToCenterView = mCenterView.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mShouldHandleSelfDefined) {
                    if (ev.getPointerCount() > 1) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = false;
                    } else if (Math.abs(ev.getY() - mSavedTouchDownY) > mMotionDetectYRangeInPixel) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = false;
                    } else if (ev.getX() - mSavedTouchDownX > 0 && (mLeftView == null || !canShowLeftView())) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = false;
                    } else if (ev.getX() - mSavedTouchDownX > mMotionDetectXRangeInPixel) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = true;
                        saveStates(ev);
                        mToShowTarget = TO_SHOW_LEFT_VIEW;
                        setViewVisibility(true, false);
                        if (mShouldDispatchToCenterView)
                            ViewHelper.cancelMotionEventInView(ev, mCenterView);
                    } else if (ev.getX() - mSavedTouchDownX < 0 && (mRightView == null || !canShowRightView())) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = false;
                    } else if (ev.getX() - mSavedTouchDownX < -mMotionDetectXRangeInPixel) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = true;
                        saveStates(ev);
                        mToShowTarget = TO_SHOW_RIGHT_VIEW;
                        setViewVisibility(false, true);
                        if (mShouldDispatchToCenterView)
                            ViewHelper.cancelMotionEventInView(ev, mCenterView);
                    }
                }
                if (mShouldHandleSelfDefined) {
                    if (mShouldHandleSelf) {
                        switch (mToShowTarget) {
                            case TO_SHOW_LEFT_VIEW:
                                float targetOffset = ev.getX() - mSavedTouchDownX + mSavedCenterViewOffset;
                                if (targetOffset > mLeftView.getWidth()) {
                                    targetOffset = mLeftView.getWidth();
                                    saveStates(ev);
                                } else if (targetOffset < 0) {
                                    targetOffset = 0;
                                    saveStates(ev);
                                }
                                moveViews((int) targetOffset);
                                break;
                            case TO_SHOW_RIGHT_VIEW:
                                targetOffset = ev.getX() - mSavedTouchDownX + mSavedCenterViewOffset;
                                if (targetOffset > 0) {
                                    targetOffset = 0;
                                    saveStates(ev);
                                } else if (targetOffset < -mRightView.getWidth()) {
                                    targetOffset = -mRightView.getWidth();
                                    saveStates(ev);
                                }
                                moveViews((int) targetOffset);
                                break;
                        }
                    } else {
                        dispatchMotionEventToViews(ev);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mShouldHandleSelfDefined && mShouldHandleSelf) {
                    switch (mToShowTarget) {
                        case TO_SHOW_LEFT_VIEW:
                            if (System.currentTimeMillis() - mSavedTouchDownTime < 150) {
                                if (ev.getX() - mSavedTouchDownX > 0)
                                    showLeftView();
                                else if (ev.getX() - mSavedTouchDownX < 0)
                                    showCenterView();
                            } else {
                                float targetOffset = ev.getX() - mSavedTouchDownX + mSavedCenterViewOffset;
                                if (targetOffset > mLeftView.getWidth() / 2)
                                    showLeftView();
                                else
                                    showCenterView();
                            }
                            break;
                        case TO_SHOW_RIGHT_VIEW:
                            if (System.currentTimeMillis() - mSavedTouchDownTime < 150) {
                                if (ev.getX() - mSavedTouchDownX < 0)
                                    showRightView();
                                else if (ev.getX() - mSavedTouchDownX > 0)
                                    showCenterView();
                            } else {
                                float targetOffset = ev.getX() - mSavedTouchDownX + mSavedCenterViewOffset;
                                if (targetOffset > -mRightView.getWidth() / 2)
                                    showCenterView();
                                else
                                    showRightView();
                            }
                            break;
                    }
                } else if (mShouldHandleSelfDefined && !mShouldHandleSelf) {
                    dispatchMotionEventToViews(ev);
                } else {
                    if (mShouldDispatchToCenterView)
                        mCenterView.dispatchTouchEvent(ev);
                }
                break;
            default:
                if (mShouldHandleSelfDefined && !mShouldHandleSelf)
                    dispatchMotionEventToViews(ev);
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mLeftView = findViewById(mLeftViewIdFromAttr);
        mRightView = findViewById(mRightViewIdFromAttr);
        mCenterView = findViewById(mCenterViewIdFromAttr);
        if (mLeftView != null) {
            int left = mLeftViewOffset;
            int right = left + mLeftView.getMeasuredWidth();
            mLeftView.layout(left, 0, right, b - t);
        }
        if (mRightView != null) {
            int left = mRightViewOffset + r - l - mRightView.getMeasuredWidth();
            int right = left + mRightView.getMeasuredWidth();
            mRightView.layout(left, 0, right, b - t);
        }
        if (mCenterView != null) {
            int left = 0 + mCenterViewOffset;
            int right = left + mCenterView.getMeasuredWidth();
            mCenterView.layout(left, 0, right, b - t);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAnimator.start(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimator.stop();
    }

}
