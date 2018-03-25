package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class SlideIndicator extends View {

    public SlideIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);
    }

    public SlideIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public SlideIndicator(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private Drawable mIndicatorDrawable;
    private Interpolator mInterpolator;

    public Drawable getIndicatorDrawable() {
        return mIndicatorDrawable;
    }

    public void setIndicatorDrawable(Drawable drawable) {
        mIndicatorDrawable = drawable;
    }

    public void setIndicatorDrawableResource(int resourceId) {
        mIndicatorDrawable = getContext().getResources().getDrawable(resourceId);
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideIndicator);
        mIndicatorDrawable = a.getDrawable(R.styleable.SlideIndicator_indicatorDrawable);
        int interpolator = a.getResourceId(R.styleable.SlideIndicator_interpolator, 0);
        if (interpolator == 0)
            mInterpolator = new LinearInterpolator();
        else
            mInterpolator = AnimationUtils.loadInterpolator(context, interpolator);
        a.recycle();
    }

    private int mIndicatorLeft = 0;
    private int mIndicatorRight = 0;

    public void setIndicatorTo(int left, int right, boolean animated) {
        mAnimator.stop();
        if (animated) {
            mAnimationStartTime = System.currentTimeMillis();
            mSourceIndicatorLeft = mIndicatorLeft;
            mSourceIndicatorRight = mIndicatorRight;
            mTargetIndicatorLeft = left;
            mTargetIndicatorRight = right;
            mAnimator.start(true);
        } else {
            mSourceIndicatorLeft = left;
            mSourceIndicatorRight = right;
            mTargetIndicatorLeft = left;
            mTargetIndicatorRight = right;
            mIndicatorLeft = left;
            mIndicatorRight = right;
            invalidate();
        }
    }

    private long mAnimationStartTime;
    private float mSourceIndicatorLeft;
    private float mSourceIndicatorRight;
    private float mTargetIndicatorLeft;
    private float mTargetIndicatorRight;

    private UITimer mAnimator = new UITimer() {

        private float mDuration = 300;

        @Override
        public void doOnUIThread() {
            long timeSpan = System.currentTimeMillis() - mAnimationStartTime;
            float fraction = mInterpolator.getInterpolation(timeSpan / mDuration);
            int leftOffset = (int) (fraction * (mTargetIndicatorLeft - mSourceIndicatorLeft));
            int rightOffset = (int) (fraction * (mTargetIndicatorRight - mSourceIndicatorRight));
            mIndicatorLeft = (int) (mSourceIndicatorLeft + leftOffset);
            mIndicatorRight = (int) (mSourceIndicatorRight + rightOffset);

            boolean shouldStop = false;
            float leftChange = Math.abs(mIndicatorLeft - mSourceIndicatorLeft);
            if (leftChange >= Math.abs(mTargetIndicatorLeft - mSourceIndicatorLeft))
                shouldStop = true;
            float rightChange = Math.abs(mIndicatorRight - mSourceIndicatorRight);
            if (rightChange >= Math.abs(mTargetIndicatorRight - mSourceIndicatorRight))
                shouldStop = true;
            if (shouldStop) {
                mIndicatorLeft = (int) mTargetIndicatorLeft;
                mIndicatorRight = (int) mTargetIndicatorRight;
                stop();
            }
            invalidate();
        }

    };

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIndicatorDrawable == null)
            return;
        mIndicatorDrawable.setBounds(mIndicatorLeft, 0, mIndicatorRight, getHeight());
        mIndicatorDrawable.draw(canvas);
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
