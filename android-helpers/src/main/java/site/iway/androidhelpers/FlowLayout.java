package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import site.iway.javahelpers.YAlign;

public class FlowLayout extends ViewGroup {

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public FlowLayout(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private ViewSelector mViewSelector;

    public int[] getSelectIndices() {
        return mViewSelector.getSelectIndices();
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        boolean enableTouchSelect = a.getBoolean(R.styleable.FlowLayout_enableTouchSelect, false);
        boolean multipleSelectMode = a.getBoolean(R.styleable.FlowLayout_multipleSelectMode, false);
        mViewSelector = new ViewSelector(this, enableTouchSelect, multipleSelectMode, false);
        int lineAlignment = a.getInt(R.styleable.FlowLayout_lineAlignment, 0);
        switch (lineAlignment) {
            case 0:
                mLineAlignment = YAlign.TopTop;
                break;
            case 1:
                mLineAlignment = YAlign.TopCenter;
                break;
            case 2:
                mLineAlignment = YAlign.TopBottom;
                break;
            case 3:
                mLineAlignment = YAlign.CenterTop;
                break;
            case 4:
                mLineAlignment = YAlign.CenterCenter;
                break;
            case 5:
                mLineAlignment = YAlign.CenterBottom;
                break;
            case 6:
                mLineAlignment = YAlign.BottomTop;
                break;
            case 7:
                mLineAlignment = YAlign.BottomCenter;
                break;
            case 8:
                mLineAlignment = YAlign.BottomBottom;
                break;
        }
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 0);
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0);
        a.recycle();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    private YAlign mLineAlignment;
    private int mHorizontalSpacing;
    private int mVerticalSpacing;

    public YAlign getLineAlignment() {
        return mLineAlignment;
    }

    public void setLineAlignment(YAlign alignment) {
        mLineAlignment = alignment;
    }

    public int getHorizontalSpacing() {
        return mHorizontalSpacing;
    }

    public void setHorizontalSpacing(int spacing) {
        if (spacing >= 0)
            mHorizontalSpacing = spacing;
    }

    public int getVerticalSpacing() {
        return mVerticalSpacing;
    }

    public void setVerticalSpacing(int spacing) {
        if (spacing >= 0)
            mVerticalSpacing = spacing;
    }

    private int computeWidth(List<Integer> widths) {
        int maxWidth = 0;
        for (Integer width : widths) {
            maxWidth = Math.max(maxWidth, width);
        }
        return maxWidth + getPaddingLeft() + getPaddingRight();
    }

    private int computeHeight(List<Integer> heights) {
        int heightSum = 0;
        for (Integer height : heights) {
            heightSum += height;
        }
        int lineCount = heights.size();
        if (lineCount > 1) {
            heightSum += (lineCount - 1) * mVerticalSpacing;
        }
        return heightSum + getPaddingTop() + getPaddingBottom();
    }

    private List<View> getVisibleChildViews() {
        List<View> visibleChildViews = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int childViewVisibility = childView.getVisibility();
            if (childViewVisibility != View.GONE) {
                visibleChildViews.add(childView);
            }
        }
        return visibleChildViews;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        List<Integer> widths = new ArrayList<>();
        List<Integer> heights = new ArrayList<>();

        int lineWidth = 0;
        int lineHeight = 0;

        List<View> visibleChildViews = getVisibleChildViews();
        int visibleChildViewCount = visibleChildViews.size();
        for (int i = 0; i < visibleChildViewCount; i++) {
            View visibleChildView = visibleChildViews.get(i);
            measureChild(visibleChildView, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams mlp = (MarginLayoutParams) visibleChildView.getLayoutParams();

            int childWidth = visibleChildView.getMeasuredWidth() + mlp.leftMargin + mlp.rightMargin;
            int childHeight = visibleChildView.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;

            if (i == 0) {
                lineWidth = childWidth;
                lineHeight = childHeight;
            } else {
                if (lineWidth + mHorizontalSpacing + childWidth <= widthSize - paddingLeft - paddingRight) {
                    lineWidth += mHorizontalSpacing + childWidth;
                    lineHeight = Math.max(lineHeight, childHeight);
                } else {
                    widths.add(lineWidth);
                    heights.add(lineHeight);
                    lineWidth = childWidth;
                    lineHeight = childHeight;
                }
            }
            if (i == visibleChildViewCount - 1) {
                widths.add(lineWidth);
                heights.add(lineHeight);
            }
        }

        int computedWidth = computeWidth(widths);
        int computedHeight = computeHeight(heights);

        int measuredWidth = 0;
        int measuredHeight = 0;

        int minWidth = getSuggestedMinimumWidth();
        int minHeight = getSuggestedMinimumHeight();

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                measuredWidth = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                measuredWidth = Math.max(computedWidth, minWidth);
                measuredWidth = Math.min(widthSize, measuredWidth);
                break;
            case MeasureSpec.UNSPECIFIED:
                measuredWidth = Math.max(computedWidth, minWidth);
                break;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                measuredHeight = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                measuredHeight = Math.max(computedHeight, minHeight);
                measuredHeight = Math.min(heightSize, measuredHeight);
                break;
            case MeasureSpec.UNSPECIFIED:
                measuredHeight = Math.max(computedHeight, minHeight);
                break;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private void layoutLineViews(List<View> lineViews, int lineLeft, int lineTop, int lineWidth, int lineHeight) {
        for (View view : lineViews) {
            MarginLayoutParams viewMLP = (MarginLayoutParams) view.getLayoutParams();
            int viewW = view.getMeasuredWidth() + viewMLP.leftMargin + viewMLP.rightMargin;
            int viewH = view.getMeasuredHeight() + viewMLP.topMargin + viewMLP.bottomMargin;
            int viewL = lineLeft + viewMLP.leftMargin;
            int viewT = mLineAlignment.getY(lineTop, lineHeight, viewH) + viewMLP.topMargin;
            int viewR = viewL + view.getMeasuredWidth();
            int viewB = viewT + view.getMeasuredHeight();

            view.layout(viewL, viewT, viewR, viewB);

            lineLeft += viewW + mHorizontalSpacing;
        }
        lineViews.clear();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int lineLeft = 0;
        int lineTop = 0;
        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<>();

        List<View> visibleChildViews = getVisibleChildViews();
        int visibleChildViewCount = visibleChildViews.size();

        for (int i = 0; i < visibleChildViewCount; i++) {
            View visibleChildView = visibleChildViews.get(i);
            MarginLayoutParams mlp = (MarginLayoutParams) visibleChildView.getLayoutParams();

            int childWidth = visibleChildView.getMeasuredWidth() + mlp.leftMargin + mlp.rightMargin;
            int childHeight = visibleChildView.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;

            if (i == 0) {
                lineLeft = paddingLeft;
                lineTop = paddingTop;
                lineWidth = childWidth;
                lineHeight = childHeight;
                lineViews.add(visibleChildView);
            } else {
                if (lineWidth + mHorizontalSpacing + childWidth <= w - paddingLeft - paddingRight) {
                    lineWidth += mHorizontalSpacing + childWidth;
                    lineHeight = Math.max(lineHeight, childHeight);
                    lineViews.add(visibleChildView);
                } else {
                    layoutLineViews(lineViews, lineLeft, lineTop, lineWidth, lineHeight);

                    lineLeft = paddingLeft;
                    lineTop += lineHeight + mVerticalSpacing;
                    lineWidth = childWidth;
                    lineHeight = childHeight;
                    lineViews.add(visibleChildView);
                }
            }
            if (i == visibleChildViewCount - 1) {
                layoutLineViews(lineViews, lineLeft, lineTop, lineWidth, lineHeight);
            }
        }

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
