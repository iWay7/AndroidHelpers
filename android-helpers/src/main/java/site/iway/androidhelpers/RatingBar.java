package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class RatingBar extends LinearLayout {

    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public RatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public RatingBar(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private int mCount;
    private int mResChecked;
    private int mResUnchecked;
    private int mPoint;
    private boolean mPointByTouch;

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
            if (i < mPoint) {
                imageView.setImageResource(mResChecked);
            } else {
                imageView.setImageResource(mResUnchecked);
            }
        }
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingBar);
        mCount = a.getInt(R.styleable.RatingBar_count, 5);
        mResChecked = a.getResourceId(R.styleable.RatingBar_resChecked, R.drawable.star_fill);
        mResUnchecked = a.getResourceId(R.styleable.RatingBar_resUnchecked, R.drawable.star_empty);
        mPoint = a.getInt(R.styleable.RatingBar_point, 0);
        mPointByTouch = a.getBoolean(R.styleable.RatingBar_pointByTouch, false);
        update();
        a.recycle();
    }

    public void setCount(int count) {
        mCount = count;
        update();
    }

    public void setResChecked(int resChecked) {
        mResChecked = resChecked;
        update();
    }

    public void setResUnchecked(int resUnchecked) {
        mCount = resUnchecked;
        update();
    }

    public void setPoint(int point) {
        mPoint = point;
        update();
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
        boolean result = onSuperTouchEvent(event) || mPointByTouch;
        if (mPointByTouch && event.getAction() == MotionEvent.ACTION_UP) {
            int childCount = getChildCount();
            for (int childIndex = childCount - 1; childIndex >= 0; childIndex--) {
                View child = getChildAt(childIndex);
                boolean hitView = ViewHelper.isMotionEventInView(event, child);
                if (hitView) {
                    setPoint(childIndex + 1);
                }
            }
        }
        return result;
    }

}
