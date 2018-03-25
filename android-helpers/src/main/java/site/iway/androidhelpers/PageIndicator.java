package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class PageIndicator extends LinearLayout {

    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttr(context, attrs);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttr(context, attrs);
    }

    public PageIndicator(Context context) {
        super(context);
        resolveAttr(context, null);
    }

    private int mCount;
    private int mResSelected;
    private int mResUnselected;
    private int mPoint;
    private int mIndicatorSpacing;

    private List<View> copyChildViews() {
        List<View> copiedChildViews = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            copiedChildViews.add(getChildAt(i));
        }
        return copiedChildViews;
    }

    private void update() {
        List<View> copiedChildViews = copyChildViews();
        removeAllViews();
        if (mCount <= 0) {
            return;
        }
        for (int i = 0; i < mCount; i++) {
            if (i < copiedChildViews.size()) {
                ImageView imageView = (ImageView) copiedChildViews.get(i);
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                params.weight = 1;
                addView(imageView, params);
            } else {
                Context context = getContext();
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                params.weight = 1;
                addView(imageView, params);
            }
            ImageView imageView = (ImageView) getChildAt(i);
            if (i == 0) {
                LayoutParams params = (LayoutParams) imageView.getLayoutParams();
                params.leftMargin = 0;
                params.rightMargin = 0;
            } else {
                LayoutParams params = (LayoutParams) imageView.getLayoutParams();
                params.leftMargin = mIndicatorSpacing;
                params.rightMargin = 0;
            }
            if (i == mPoint) {
                imageView.setImageResource(mResSelected);
            } else {
                imageView.setImageResource(mResUnselected);
            }
        }
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator);
        mCount = a.getInt(R.styleable.PageIndicator_pageCount, 5);
        mResSelected = a.getResourceId(R.styleable.PageIndicator_resSelected, R.drawable.page_indicator_s);
        mResUnselected = a.getResourceId(R.styleable.PageIndicator_resUnselected, R.drawable.page_indicator_n);
        mPoint = a.getInt(R.styleable.PageIndicator_pageIndex, 0);
        mIndicatorSpacing = a.getDimensionPixelSize(R.styleable.PageIndicator_indicatorSpacing, 0);
        update();
        a.recycle();
    }

    public void setPageCount(int count) {
        mCount = count;
        update();
    }

    public void setResSelected(int resSelected) {
        mResSelected = resSelected;
        update();
    }

    public void setResUnselected(int resUnselected) {
        mCount = resUnselected;
        update();
    }

    public void setPageIndex(int point) {
        mPoint = point;
        update();
    }

}
