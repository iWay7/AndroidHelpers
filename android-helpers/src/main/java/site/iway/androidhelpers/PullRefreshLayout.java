package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import site.iway.javahelpers.MathHelper;

public class PullRefreshLayout extends ViewGroup {

    public interface OnRefreshListener {
        public void onRefresh(PullRefreshLayout pullRefreshLayout);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public PullRefreshLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private int mHeaderViewIdFromAttr;
    private int mContentViewIdFromAttr;

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullRefreshLayout);
        mHeaderViewIdFromAttr = a.getResourceId(R.styleable.PullRefreshLayout_headerViewId, 0);
        mContentViewIdFromAttr = a.getResourceId(R.styleable.PullRefreshLayout_contentViewId, 0);
        a.recycle();
    }

    private View mHeaderView;
    private View mContentView;

    public void setHeaderView(View view) {
        if (ViewHelper.isChildView(this, view)) {
            mHeaderView = view;
        }
    }

    public void setHeaderViewId(int id) {
        mHeaderView = ViewHelper.findChildViewById(this, id);
    }

    public void setContentView(View view) {
        if (ViewHelper.isChildView(this, view)) {
            mContentView = view;
        }
    }

    public void setContentViewId(int id) {
        mContentView = ViewHelper.findChildViewById(this, id);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        int childViewId = child.getId();
        if (childViewId == mHeaderViewIdFromAttr)
            mHeaderView = child;
        if (childViewId == mContentViewIdFromAttr)
            mContentView = child;
    }

    private OnRefreshListener mOnRefreshListener;

    public void setOnRefreshListener(OnRefreshListener l) {
        mOnRefreshListener = l;
    }

    private boolean mIsRefreshing;

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        PullRefreshHeader pullRefreshHeader = (PullRefreshHeader) mHeaderView;
        int pullRefreshHeaderMainHeight = pullRefreshHeader.getMainHeight();
        if (refreshing) {
            pullRefreshHeader.setRefreshing(true);
            if (mOnRefreshListener != null && !mIsRefreshing) {
                mIsRefreshing = true;
                mOnRefreshListener.onRefresh(this);
            } else {
                mIsRefreshing = true;
            }
            animatePullOffsetTo(pullRefreshHeaderMainHeight);
        } else {
            pullRefreshHeader.setRefreshing(false);
            mIsRefreshing = false;
            animatePullOffsetTo(0);
        }
    }

    private void setPullOffset(int offset) {
        if (offset < 0)
            offset = 0;
        setScrollY(-offset);
    }

    private int getPullOffset() {
        return -getScrollY();
    }

    private float mCurrentPullOffset = 0;
    private float mTargetPullOffset = 0;
    private boolean mAnimatingToTarget = false;

    private UITimer mAnimator = new UITimer() {

        @Override
        public void doOnUIThread() {
            float change = (mTargetPullOffset - mCurrentPullOffset) * 0.15f;
            if (Math.abs(change) < 1) {
                mCurrentPullOffset = mTargetPullOffset;
                setPullOffset((int) mCurrentPullOffset);
                stop();
                mAnimatingToTarget = false;
            } else {
                mCurrentPullOffset += change;
                setPullOffset((int) mCurrentPullOffset);
            }
        }

    };

    private void animatePullOffsetTo(int pullOffset) {
        mCurrentPullOffset = getPullOffset();
        mTargetPullOffset = pullOffset;
        mAnimatingToTarget = true;
        mAnimator.start(false);
    }

    private float mSavedTouchEventXAxis;
    private float mSavedTouchEventYAxis;

    private float mDragStartYAxis;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsRefreshing || mAnimatingToTarget) {
            return true;
        } else {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mSavedTouchEventXAxis = ev.getX();
                    mSavedTouchEventYAxis = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Context context = getContext();
                    float touchEventXAxis = ev.getX();
                    float touchEventYAxis = ev.getY();
                    float detectRange = UnitHelper.dipToPx(context, 10);
                    boolean isPullHorizontal = Math.abs(touchEventXAxis - mSavedTouchEventXAxis) > detectRange;
                    boolean isPullVertical = touchEventYAxis - mSavedTouchEventYAxis > detectRange;
                    boolean canContentViewPull = mContentView.canScrollVertically(-1);
                    if (isPullHorizontal == false && isPullVertical && canContentViewPull == false) {
                        mDragStartYAxis = ev.getY();
                        return true;
                    }
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
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
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsRefreshing || mAnimatingToTarget) {
            return true;
        }
        handleSuperTouchEvent(event);
        PullRefreshHeader pullRefreshHeader = (PullRefreshHeader) mHeaderView;
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragStartYAxis = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float offset = event.getY() - mDragStartYAxis;
                int pullOffset = MathHelper.pixel(offset * 0.5);
                setPullOffset(pullOffset);
                pullRefreshHeader.updateContent(pullOffset);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int pullRefreshHeaderMainHeight = pullRefreshHeader.getMainHeight();
                int currentPullOffset = getPullOffset();
                setRefreshing(currentPullOffset > pullRefreshHeaderMainHeight);
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        RuntimeException runtimeException = new RuntimeException("HeaderView or ContentView not set !");
        if (mHeaderView == null || mContentView == null) {
            if (mHeaderViewIdFromAttr > 0 && mContentViewIdFromAttr > 0) {
                setHeaderViewId(mHeaderViewIdFromAttr);
                setContentViewId(mContentViewIdFromAttr);
                if (mHeaderView == null || mContentView == null) {
                    throw runtimeException;
                }
            } else {
                throw runtimeException;
            }
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child == mHeaderView) {
                int left = 0;
                int top = -child.getMeasuredHeight();
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);
            } else {
                int left = 0;
                int top = 0;
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);
            }
        }
    }

}
