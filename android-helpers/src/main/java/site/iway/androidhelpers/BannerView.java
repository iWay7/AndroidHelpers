package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.LinkedList;
import java.util.List;

import site.iway.javahelpers.ArrayHelper;
import site.iway.javahelpers.MathHelper;

public class BannerView extends ViewGroup {

    public interface BannerIndexChangedListener {
        public void onBannerIndexChanged(BannerView bannerView, int index);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public BannerView(Context context) {
        super(context);
        initialize(context, null);
    }

    private BannerIndexChangedListener mListener;

    public void setBannerIndexChangedListener(BannerIndexChangedListener l) {
        mListener = l;
    }

    private float mClickDetectRadius;

    public float getClickDetectRadius() {
        return mClickDetectRadius;
    }

    public void setClickDetectRadius(float clickDetectRadius) {
        mClickDetectRadius = clickDetectRadius;
    }

    private List<ViewGroup> mRequestDisallowInterceptTouchEventViewGroups;

    public void addRequestDisallowInterceptTouchEventViewGroup(ViewGroup viewGroup) {
        mRequestDisallowInterceptTouchEventViewGroups.add(viewGroup);
    }

    public void removeRequestDisallowInterceptTouchEventViewGroup(ViewGroup viewGroup) {
        mRequestDisallowInterceptTouchEventViewGroups.remove(viewGroup);
    }

    public void clearRequestDisallowInterceptTouchEventViewGroups() {
        mRequestDisallowInterceptTouchEventViewGroups.clear();
    }

    private boolean mWillPreloadNext;

    public boolean isWillPreloadNext() {
        return mWillPreloadNext;
    }

    public void setWillPreloadNext(boolean willPreloadNext) {
        mWillPreloadNext = willPreloadNext;
    }

    private Scroller mScroller;

    private BitmapView mBitmapViewCenter;
    private BitmapView mBitmapViewLeft;
    private BitmapView mBitmapViewRight;

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        setClickDetectRadius(a.getDimension(R.styleable.BannerView_clickDetectRadius, UnitHelper.dipToPxInt(context, 5)));
        setWillPreloadNext(a.getBoolean(R.styleable.BannerView_willPreloadNextPage, false));
        setAutoNextTime(a.getInt(R.styleable.BannerView_autoNextTime, 0));
        mRequestDisallowInterceptTouchEventViewGroups = new LinkedList<>();
        mScroller = new Scroller(context);
        int width = LayoutParams.MATCH_PARENT;
        int height = LayoutParams.MATCH_PARENT;
        mBitmapViewCenter = new BitmapView(context);
        mBitmapViewLeft = new BitmapView(context);
        mBitmapViewRight = new BitmapView(context);
        addView(mBitmapViewCenter, width, height);
        addView(mBitmapViewLeft, width, height);
        addView(mBitmapViewRight, width, height);
        a.recycle();
    }

    public void initializeBitmapViews(ViewProcessor viewProcessor) {
        viewProcessor.process(mBitmapViewCenter);
        viewProcessor.process(mBitmapViewLeft);
        viewProcessor.process(mBitmapViewRight);
    }

    private int mViewWidth;
    private int mViewHeight;

    private BitmapSource[] mBitmapSources;

    public void setBitmapSources(BitmapSource... bitmapSources) {
        mBitmapSources = bitmapSources;
        setScrollX(0);
        requestLayout();
    }

    private boolean isScrollable() {
        return mBitmapSources != null &&
                mBitmapSources.length > 1 &&
                mViewWidth > 0 &&
                mViewHeight > 0 &&
                mHasAttachedToWindow;
    }

    public int getCurrentIndex() {
        int scrollX = getScrollX();
        int index = scrollX / mViewWidth;
        int offset = scrollX % mViewWidth;
        int halfWidth = mViewWidth / 2;
        if (index == 0) {
            if (offset > halfWidth)
                index++;
            if (offset < -halfWidth)
                index--;
        } else if (index > 0) {
            if (offset > halfWidth)
                index++;
        } else if (index < 0) {
            if (offset < -halfWidth)
                index--;
        }
        return index;
    }

    public int getRealIndex() {
        if (ArrayHelper.isEmpty(mBitmapSources)) {
            return -1;
        } else if (mViewWidth == 0 || mViewHeight == 0) {
            return 0;
        } else {
            int currentIndex = getCurrentIndex();
            return getBitmapSourceIndex(currentIndex);
        }
    }

    private int mLastIndex;
    private int mCurrIndex;

    private void swapViewsPrev() {
        BitmapView lastLeft = mBitmapViewLeft;
        BitmapView lastCenter = mBitmapViewCenter;
        BitmapView lastRight = mBitmapViewRight;
        mBitmapViewLeft = lastRight;
        mBitmapViewCenter = lastLeft;
        mBitmapViewRight = lastCenter;
    }

    private void swapViewsNext() {
        BitmapView lastLeft = mBitmapViewLeft;
        BitmapView lastCenter = mBitmapViewCenter;
        BitmapView lastRight = mBitmapViewRight;
        mBitmapViewLeft = lastCenter;
        mBitmapViewCenter = lastRight;
        mBitmapViewRight = lastLeft;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mCurrIndex = getCurrentIndex();
        if (mCurrIndex < mLastIndex) {
            swapViewsPrev();
        }
        if (mCurrIndex > mLastIndex) {
            swapViewsNext();
        }
        if (mCurrIndex != mLastIndex) {
            requestLayout();
            if (mListener != null) {
                mListener.onBannerIndexChanged(this, mCurrIndex);
                if (mWillPreloadNext) {
                    BitmapSource bitmapSource = getBitmapSource(mCurrIndex + 1);
                    BitmapCache.get(bitmapSource, new BitmapInfoListener() {
                        @Override
                        public void onBitmapInfoChange(BitmapInfo bitmapInfo) {
                            // nothing
                        }
                    });
                }
            }
        }
        mLastIndex = mCurrIndex;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        boolean scrolling = mScroller.computeScrollOffset();
        if (scrolling) {
            int scrollX = mScroller.getCurrX();
            int scrollY = mScroller.getCurrY();
            scrollTo(scrollX, scrollY);
            postInvalidate();
        }
    }

    private void scrollToIndex(int index) {
        int width = getWidth();
        int startX = getScrollX();
        int startY = getScrollY();
        int dx = width * index - startX;
        int dy = 0;
        int duration = 500;

        mScroller.startScroll(startX, startY, dx, dy, duration);

        scrollTo(startX, startY);
        postInvalidate();
    }

    public void previous() {
        if (isScrollable()) {
            int index = getCurrentIndex();
            scrollToIndex(index - 1);
        }
    }

    public void next() {
        if (isScrollable()) {
            int index = getCurrentIndex();
            scrollToIndex(index + 1);
        }
    }

    private boolean mHasAttachedToWindow;
    private UITimer mTimer;

    public void setAutoNextTime(int millis) {
        if (mTimer != null) {
            mTimer.stop();
            mTimer = null;
        }
        if (millis > 0) {
            mTimer = new UITimer(millis) {
                @Override
                public void doOnUIThread() {
                    next();
                }
            };
            if (mHasAttachedToWindow) {
                mTimer.start(false);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
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

    private float mTouchDownX;
    private float mTouchDownY;
    private float mTouchMoveX;
    private float mTouchMoveY;
    private float mTouchDownXOffset;
    private float mTouchDownScrollX;

    private boolean isClick() {
        return MathHelper.distance(mTouchDownX, mTouchDownY, mTouchMoveX, mTouchMoveY) < mClickDetectRadius;
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        OnClickListener wrappedOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClick()) {
                    l.onClick(v);
                }
            }
        };
        super.setOnClickListener(wrappedOnClickListener);
    }

    @Override
    public void setOnLongClickListener(final OnLongClickListener l) {
        OnLongClickListener wrappedOnLongClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (MathHelper.distance(mTouchDownX, mTouchDownY, mTouchMoveX, mTouchMoveY) < mClickDetectRadius) {
                    if (isClick()) {
                        return l.onLongClick(v);
                    }
                }
                return false;
            }
        };
        super.setOnLongClickListener(wrappedOnLongClickListener);

    }

    private void doScroll(MotionEvent event) {
        float targetScrollX = mTouchDownScrollX - mTouchDownXOffset;
        int targetScrollXRounded = MathHelper.pixel(targetScrollX);
        setScrollX(targetScrollXRounded);
    }

    private VelocityTracker mVelocityTracker;

    private void adjustScroll(MotionEvent event) {
        mVelocityTracker.computeCurrentVelocity(1);
        float xVelocity = mVelocityTracker.getXVelocity();
        int index = getCurrentIndex();
        if (xVelocity < -3)
            index++;
        if (xVelocity > 3)
            index--;
        scrollToIndex(index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleSuperTouchEvent(event);
        if (isScrollable()) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    for (ViewGroup viewGroup : mRequestDisallowInterceptTouchEventViewGroups) {
                        viewGroup.requestDisallowInterceptTouchEvent(true);
                    }

                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                    mTouchDownScrollX = getScrollX();

                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(event);

                    boolean isScrolling = !mScroller.isFinished();
                    if (isScrolling) {
                        mScroller.abortAnimation();
                    }

                    if (mTimer != null) {
                        mTimer.stop();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchMoveX = event.getX();
                    mTouchMoveY = event.getY();

                    mVelocityTracker.addMovement(event);

                    mTouchDownXOffset = event.getX() - mTouchDownX;

                    doScroll(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mVelocityTracker.addMovement(event);

                    adjustScroll(event);

                    if (mTimer != null) {
                        mTimer.start(false);
                    }

                    mVelocityTracker.recycle();

                    for (ViewGroup viewGroup : mRequestDisallowInterceptTouchEventViewGroups) {
                        viewGroup.requestDisallowInterceptTouchEvent(false);
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    private int getBitmapSourceIndex(int index) {
        if (ArrayHelper.isEmpty(mBitmapSources))
            return -1;
        int bitmapSourceIndex = index % mBitmapSources.length;
        if (bitmapSourceIndex < 0)
            bitmapSourceIndex += mBitmapSources.length;
        return bitmapSourceIndex;
    }

    private BitmapSource getBitmapSource(int index) {
        if (ArrayHelper.isEmpty(mBitmapSources))
            return null;
        int bitmapSourceIndex = getBitmapSourceIndex(index);
        return mBitmapSources[bitmapSourceIndex];
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mViewWidth = r - l;
        mViewHeight = b - t;

        int index = getCurrentIndex();
        int base = index * mViewWidth;

        mBitmapViewLeft.layout(base - mViewWidth, 0, base - mViewWidth + mViewWidth, 0 + mViewHeight);
        mBitmapViewLeft.loadFromSource(getBitmapSource(index - 1));
        mBitmapViewCenter.layout(base, 0, base + mViewWidth, 0 + mViewHeight);
        mBitmapViewCenter.loadFromSource(getBitmapSource(index));
        mBitmapViewRight.layout(base + mViewWidth, 0, base + mViewWidth + mViewWidth, 0 + mViewHeight);
        mBitmapViewRight.loadFromSource(getBitmapSource(index + 1));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHasAttachedToWindow = true;
        if (mTimer != null) {
            mTimer.start(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mTimer != null) {
            mTimer.stop();
        }
        mHasAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

}
