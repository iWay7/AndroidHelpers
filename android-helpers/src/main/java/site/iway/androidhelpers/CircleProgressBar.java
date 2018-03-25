package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class CircleProgressBar extends View {

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public CircleProgressBar(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        mProgressBackColor = a.getColor(R.styleable.CircleProgressBar_progressBackColor, 0xffcccccc);
        mProgressFrontColor = a.getColor(R.styleable.CircleProgressBar_progressFrontColor, 0xff999999);
        Resources resources = a.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        mProgressWidth = a.getDimension(R.styleable.CircleProgressBar_progressWidth, displayMetrics.density * 20);
        mCircleDiameter = a.getDimension(R.styleable.CircleProgressBar_circleDiameter, displayMetrics.density * 200);
        mStartAngleOffset = a.getFloat(R.styleable.CircleProgressBar_startAngleOffset, 0);
        float progress = a.getFloat(R.styleable.CircleProgressBar_progress, 0);
        setProgress(progress, false);
        a.recycle();
    }

    private int mProgressBackColor;
    private int mProgressFrontColor;
    private float mProgressWidth;
    private float mCircleDiameter;
    private float mStartAngleOffset;

    public int getProgressBackColor() {
        return mProgressBackColor;
    }

    public void setProgressBackColor(int progressBackColor) {
        mProgressBackColor = progressBackColor;
    }

    public int getProgressFrontColor() {
        return mProgressFrontColor;
    }

    public void setProgressFrontColor(int progressFrontColor) {
        mProgressFrontColor = progressFrontColor;
    }

    public float getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(float progressWidth) {
        mProgressWidth = progressWidth;
    }

    public float getCircleDiameter() {
        return mCircleDiameter;
    }

    public void setCircleDiameter(float circleDiameter) {
        mCircleDiameter = circleDiameter;
    }

    public float getStartAngleOffset() {
        return mStartAngleOffset;
    }

    public void setStartAngleOffset(float startAngleOffset) {
        mStartAngleOffset = startAngleOffset;
    }

    private float mSourceProgress;
    private float mTargetProgress;

    public UITimer mAnimator = new UITimer() {

        @Override
        public void doOnUIThread() {
            if (mCircleRect == null) {
                return;
            }
            double change = 0.15d * (mTargetProgress - mSourceProgress);
            if (change == 0) {
                stop();
            } else if (Math.abs(Math.tan(change / 100f * Math.PI * 2) * mCircleDiameter / 2) < 0.1d) {
                mSourceProgress = mTargetProgress;
                stop();
            } else {
                mSourceProgress += change;
            }
            invalidate();
        }

    };

    public float getProgress() {
        return mTargetProgress;
    }

    public void setProgress(float progress, boolean animated) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > 100) {
            progress = 100;
        }
        mTargetProgress = progress;
        mSourceProgress = animated ? mSourceProgress : mTargetProgress;
        mAnimator.start(true);
    }

    protected Paint mDrawPaint;
    protected float mCenterX;
    protected float mCenterY;
    protected RectF mCircleRect;
    protected float mProgressFrontStartAngle;
    protected float mProgressFrontSweepAngle;
    protected float mProgressBackStartAngle;
    protected float mProgressBackSweepAngle;
    protected float mClearRadius;

    protected void computeValues() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        mCenterX = viewWidth / 2;
        mCenterY = viewHeight / 2;
        float radius = mCircleDiameter / 2;
        float left = mCenterX - radius;
        float right = mCenterX + radius;
        float top = mCenterY - radius;
        float bottom = mCenterY + radius;
        if (mCircleRect == null)
            mCircleRect = new RectF(left, top, right, bottom);
        else
            mCircleRect.set(left, top, right, bottom);
        mProgressFrontStartAngle = 0f - 90f + mStartAngleOffset;
        mProgressFrontSweepAngle = mSourceProgress / 100f * 360f;
        mProgressBackStartAngle = mProgressFrontStartAngle + mProgressFrontSweepAngle;
        mProgressBackSweepAngle = 360f - mProgressFrontSweepAngle;
        mClearRadius = (mCircleDiameter - mProgressWidth * 2) / 2;
    }

    protected void drawArcs(Canvas canvas) {
        if (mDrawPaint == null) {
            mDrawPaint = new Paint();
            mDrawPaint.setStyle(Paint.Style.STROKE);
            mDrawPaint.setStrokeWidth(mProgressWidth);
            mDrawPaint.setAntiAlias(true);
        }
        mDrawPaint.setColor(mProgressFrontColor);
        canvas.drawArc(mCircleRect, mProgressFrontStartAngle, mProgressFrontSweepAngle, false, mDrawPaint);
        mDrawPaint.setColor(mProgressBackColor);
        canvas.drawArc(mCircleRect, mProgressBackStartAngle, mProgressBackSweepAngle, false, mDrawPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        computeValues();
        drawArcs(canvas);
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
