package site.iway.androidhelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import site.iway.javahelpers.MathHelper;

public class ImageViewer extends View {

    public ImageViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewer(Context context) {
        super(context);
    }

    public static final int CENTER_NO_SCALE = 1;
    public static final int CENTER_FIT = 2;
    public static final int CENTER_CROP = 3;
    public static final int CENTER_INSIDE = 4;

    private Bitmap mBitmap;
    private float mImageWidth;
    private float mImageHeight;
    private float mImageLeft;
    private float mImageTop;
    private float mImageScale;
    private float mViewWidth;
    private float mViewHeight;
    private int mInitScaleType = CENTER_FIT;
    private float mInitImageScale;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;

        mImageWidth = mBitmap == null ? -1 : mBitmap.getWidth();
        mImageHeight = mBitmap == null ? -1 : mBitmap.getHeight();

        resetImageState();
    }

    public float getImageWidth() {
        if (mBitmap == null)
            return Float.NaN;
        return mImageWidth;
    }

    public float getImageHeight() {
        if (mBitmap == null)
            return Float.NaN;
        return mImageHeight;
    }

    public float getImageLeftPadding() {
        if (mBitmap == null)
            return Float.NaN;
        return mImageLeft;
    }

    public float getImageTopPadding() {
        if (mBitmap == null)
            return Float.NaN;
        return mImageTop;
    }

    public float getImageRightPadding() {
        if (mBitmap == null)
            return Float.NaN;
        return mViewWidth - (mImageLeft + mImageWidth * mImageScale);
    }

    public float getImageBottomPadding() {
        if (mBitmap == null)
            return Float.NaN;
        return mViewHeight - (mImageTop + mImageHeight * mImageScale);
    }

    public float getImageScale() {
        if (mBitmap == null)
            return Float.NaN;
        return mImageScale;
    }

    public float getViewWidth() {
        return mViewWidth;
    }

    public float getViewHeight() {
        return mViewHeight;
    }

    public int getInitScaleType() {
        return mInitScaleType;
    }

    public void setInitScaleType(int scaleType) {
        if (scaleType >= CENTER_NO_SCALE && scaleType <= CENTER_CROP) {
            if (mInitScaleType != scaleType) {
                mInitScaleType = scaleType;
                resetImageState();
            } else {
                mInitScaleType = scaleType;
            }
        }
    }

    public void resetImageState() {
        if (mBitmap == null || mViewWidth == 0 || mViewHeight == 0)
            return;

        if (mImageStateChecker != null)
            mImageStateChecker.pauseCheckState();

        switch (mInitScaleType) {
            case CENTER_NO_SCALE:
                mImageLeft = (mViewWidth - mImageWidth) / 2.0f;
                mImageTop = (mViewHeight - mImageHeight) / 2.0f;
                break;
            case CENTER_INSIDE:
                if (mImageWidth <= mViewWidth && mImageHeight <= mViewHeight) {
                    mImageLeft = (mViewWidth - mImageWidth) / 2.0f;
                    mImageTop = (mViewHeight - mImageHeight) / 2.0f;
                    break;
                }
            case CENTER_FIT:
                float targetScale = 1.0f;
                if (mViewWidth / mImageWidth * mImageHeight <= mViewHeight)
                    targetScale = mViewWidth / mImageWidth;
                if (mViewHeight / mImageHeight * mImageWidth <= mViewWidth)
                    targetScale = mViewHeight / mImageHeight;
                float scaledImageWidth = targetScale * mImageWidth;
                float scaledImageHeight = targetScale * mImageHeight;
                mImageScale = targetScale;
                mImageLeft = (mViewWidth - scaledImageWidth) / 2.0f;
                mImageTop = (mViewHeight - scaledImageHeight) / 2.0f;
                break;
            case CENTER_CROP:
                targetScale = 1.0f;
                if (mViewWidth / mImageWidth * mImageHeight >= mViewHeight)
                    targetScale = mViewWidth / mImageWidth;
                if (mViewHeight / mImageHeight * mImageWidth >= mViewWidth)
                    targetScale = mViewHeight / mImageHeight;
                scaledImageWidth = targetScale * mImageWidth;
                scaledImageHeight = targetScale * mImageHeight;
                mImageScale = targetScale;
                mImageLeft = (mViewWidth - scaledImageWidth) / 2.0f;
                mImageTop = (mViewHeight - scaledImageHeight) / 2.0f;
                break;
        }
        mInitImageScale = mImageScale;

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        resetImageState();
    }

    private int mImageAnimationStep = 5;

    public int getImageAnimationStep() {
        return mImageAnimationStep;
    }

    public void setImageAnimationStep(int millis) {
        mImageAnimationStep = millis;
    }

    private float mMinImageScale = 1.0f;

    public float getMinImageScale() {
        return mMinImageScale;
    }

    public void setMinImageScaleMin(float value) {
        mMinImageScale = value;
    }

    private float mMaxImageScale = 2.0f;

    public float getMaxImageScale() {
        return mMaxImageScale;
    }

    public void setMaxImageScale(float value) {
        mMaxImageScale = value;
    }

    private class ImageStateChecker extends Thread {

        private volatile boolean mShouldCheckImageState = false;
        private volatile boolean mShouldStopCheckState = false;

        public void run() {
            while (true) {
                if (mShouldStopCheckState) {
                    break;
                }
                if (mShouldCheckImageState) {
                    if (mBitmap == null) {
                        pauseCheckState();
                    } else {
                        float shouldImageLeftChange = (mTargetImageLeft - mImageLeft) * 0.05f;
                        float shouldImageTopChange = (mTargetImageTop - mImageTop) * 0.05f;
                        float shouldImageScaleChange = (mTargetImageScale - mImageScale) * 0.05f;

                        mImageLeft += shouldImageLeftChange;
                        mImageTop += shouldImageTopChange;
                        mImageScale += shouldImageScaleChange;

                        boolean shouldPauseCheckState = true;
                        if (Math.abs(mTargetImageLeft - mImageLeft) >= 1)
                            shouldPauseCheckState = false;
                        if (Math.abs(mTargetImageTop - mImageTop) >= 1)
                            shouldPauseCheckState = false;
                        if (Math.abs((mTargetImageScale - mImageScale) * mImageWidth) >= 1)
                            shouldPauseCheckState = false;
                        if (Math.abs((mTargetImageScale - mImageScale) * mImageHeight) >= 1)
                            shouldPauseCheckState = false;
                        if (shouldPauseCheckState) {
                            mImageLeft = mTargetImageLeft;
                            mImageTop = mTargetImageTop;
                            mImageScale = mTargetImageScale;
                            pauseCheckState();
                        }

                        if (getVisibility() == VISIBLE) {
                            postInvalidate();
                        }
                    }
                }
                try {
                    sleep(mImageAnimationStep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void beginCheckState() {
            if (!isAlive())
                start();
            mShouldCheckImageState = true;
        }

        public void pauseCheckState() {
            mShouldCheckImageState = false;
        }

        public void stopCheckState() {
            mShouldStopCheckState = true;
        }

    }

    private ImageStateChecker mImageStateChecker;

    private int mClickDetectStep = 300;

    public int getClickDetectStep() {
        return mClickDetectStep;
    }

    public void setClickDetectStep(int millis) {
        mClickDetectStep = millis;
    }

    private int mClickDetectRadius = 10;

    public int getClickDetectRadius() {
        return mClickDetectRadius;
    }

    public void setClickDetectRadius(int pixels) {
        mClickDetectRadius = pixels;
    }

    private OnClickListener mOnClickListener;

    public OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    private boolean mIsDetectingClick;

    private int mClickDownCount;
    private int mClickUpCount;

    private float mClickDownX;
    private float mClickDownY;

    private float mClickUpX;
    private float mClickUpY;

    private Runnable mClickDetectHandler = new Runnable() {

        @Override
        public void run() {
            float dx = mClickDownX - mClickUpX;
            float dy = mClickDownY - mClickUpY;
            if (MathHelper.distance(dx, dy) < mClickDetectRadius) {
                if (mClickDownCount == 1 && mClickUpCount == 1) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(ImageViewer.this);
                    }
                } else if (mClickDownCount == 2 && mClickUpCount == 2) {
                    onDoubleClickEvent();
                }
            }
            mIsDetectingClick = false;
        }

    };

    public void handleClickDetectDown(MotionEvent event) {
        mClickDownCount++;
        mClickDownX = event.getX();
        mClickDownY = event.getY();
    }

    public void handleClickDetectUp(MotionEvent event) {
        mClickUpCount++;
        mClickUpX = event.getX();
        mClickUpY = event.getY();
    }

    private void beginClickDetect() {
        if (!mIsDetectingClick) {
            mClickDownCount = 0;
            mClickUpCount = 0;
            mIsDetectingClick = true;
            getHandler().postDelayed(mClickDetectHandler, mClickDetectStep);
        }
    }

    private float mSavedImageLeft;
    private float mSavedImageTop;
    private float mSavedImageScale;

    private float mSavedTouchDownX;
    private float mSavedTouchDownY;
    private float mSavedTouchUpX;
    private float mSavedTouchUpY;
    private float mSavedXPivot;
    private float mSavedYPivot;
    private float mSavedFingersCenterX;
    private float mSavedFingersCenterY;
    private float mSavedFingerDistance;

    private float mTargetImageLeft;
    private float mTargetImageTop;
    private float mTargetImageScale;

    private void saveImageState() {
        mSavedImageLeft = mImageLeft;
        mSavedImageTop = mImageTop;
        mSavedImageScale = mImageScale;
    }

    private void loadTargetImageState() {
        mTargetImageLeft = mImageLeft;
        mTargetImageTop = mImageTop;
        mTargetImageScale = mImageScale;
    }

    private void checkTargetImageScale() {
        if (mTargetImageScale > mMaxImageScale * mInitImageScale)
            mTargetImageScale = mMaxImageScale * mInitImageScale;
        if (mTargetImageScale < mMinImageScale * mInitImageScale)
            mTargetImageScale = mMinImageScale * mInitImageScale;
    }

    private float mTargetImageWidth;
    private float mTargetImageHeight;

    private void computeTargetImageSize() {
        mTargetImageWidth = mImageWidth * mTargetImageScale;
        mTargetImageHeight = mImageHeight * mTargetImageScale;
    }

    private void setTargetImagePosition() {
        float xPovit = (mSavedTouchUpX - mImageLeft) / (mImageWidth * mImageScale);
        float yPovit = (mSavedTouchUpY - mImageTop) / (mImageHeight * mImageScale);
        mTargetImageLeft = mSavedTouchUpX - mTargetImageWidth * xPovit;
        mTargetImageTop = mSavedTouchUpY - mTargetImageHeight * yPovit;
    }

    private void checkTargetImagePosition() {
        if (mTargetImageWidth <= mViewWidth && mTargetImageHeight <= mViewHeight) {
            mTargetImageLeft = (mViewWidth - mTargetImageWidth) / 2.0f;
            mTargetImageTop = (mViewHeight - mTargetImageHeight) / 2.0f;
        } else if (mTargetImageWidth > mViewWidth && mTargetImageHeight <= mViewHeight) {
            if (mTargetImageLeft > 0)
                mTargetImageLeft = 0;
            else if (mTargetImageLeft + mTargetImageWidth < mViewWidth)
                mTargetImageLeft = mViewWidth - mTargetImageWidth;
            mTargetImageTop = (mViewHeight - mTargetImageHeight) / 2.0f;
        } else if (mTargetImageWidth <= mViewWidth && mTargetImageHeight > mViewHeight) {
            mTargetImageLeft = (mViewWidth - mTargetImageWidth) / 2.0f;
            if (mTargetImageTop > 0)
                mTargetImageTop = 0;
            else if (mTargetImageTop + mTargetImageHeight < mViewHeight)
                mTargetImageTop = mViewHeight - mTargetImageHeight;
        } else if (mTargetImageWidth > mViewWidth && mTargetImageHeight > mViewHeight) {
            if (mTargetImageLeft > 0)
                mTargetImageLeft = 0;
            else if (mTargetImageLeft + mTargetImageWidth < mViewWidth)
                mTargetImageLeft = mViewWidth - mTargetImageWidth;
            if (mTargetImageTop > 0)
                mTargetImageTop = 0;
            else if (mTargetImageTop + mTargetImageHeight < mViewHeight)
                mTargetImageTop = mViewHeight - mTargetImageHeight;
        }
    }

    private boolean mIsFirstTwoFingerTouch = true;
    private boolean mHasTwoFingerTouched = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSavedTouchDownX = event.getX();
                mSavedTouchDownY = event.getY();

                beginClickDetect();
                handleClickDetectDown(event);
                mImageStateChecker.pauseCheckState();

                saveImageState();

                mIsFirstTwoFingerTouch = true;
                mHasTwoFingerTouched = false;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (event.getPointerCount()) {
                    case 1:
                        if (!mHasTwoFingerTouched) {
                            mImageLeft = mTargetImageLeft = event.getX() - mSavedTouchDownX + mSavedImageLeft;
                            mImageTop = mTargetImageTop = event.getY() - mSavedTouchDownY + mSavedImageTop;
                            invalidate();
                        }
                        break;
                    case 2:
                        mSavedFingersCenterX = (event.getX(0) + event.getX(1)) / 2;
                        mSavedFingersCenterY = (event.getY(0) + event.getY(1)) / 2;

                        float x_0 = event.getX(0);
                        float y_0 = event.getY(0);
                        float x_1 = event.getX(1);
                        float y_1 = event.getY(1);
                        float fingerDistance = (float) MathHelper.distance(x_0, y_0, x_1, y_1);

                        if (mIsFirstTwoFingerTouch) {
                            mSavedImageScale = mImageScale;
                            mSavedXPivot = (mSavedFingersCenterX - mImageLeft) / (mImageWidth * mImageScale);
                            mSavedYPivot = (mSavedFingersCenterY - mImageTop) / (mImageHeight * mImageScale);
                            mSavedFingerDistance = fingerDistance;
                            mIsFirstTwoFingerTouch = false;
                            mHasTwoFingerTouched = true;
                        } else {
                            float multiple = fingerDistance / mSavedFingerDistance;
                            mImageScale = multiple * mSavedImageScale;
                            mImageScale = multiple * mSavedImageScale;
                            mImageLeft = mSavedFingersCenterX - mImageWidth * mImageScale * mSavedXPivot;
                            mImageTop = mSavedFingersCenterY - mImageHeight * mImageScale * mSavedYPivot;
                        }

                        postInvalidate();
                        break;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mHasTwoFingerTouched) {
                    mSavedTouchUpX = mSavedFingersCenterX;
                    mSavedTouchUpY = mSavedFingersCenterY;
                } else {
                    mSavedTouchUpX = event.getX();
                    mSavedTouchUpY = event.getY();
                }

                loadTargetImageState();
                checkTargetImageScale();
                computeTargetImageSize();
                setTargetImagePosition();
                checkTargetImagePosition();

                handleClickDetectUp(event);
                mImageStateChecker.beginCheckState();
                break;
        }
        return true;
    }

    private void onDoubleClickEvent() {
        loadTargetImageState();

        if (mImageScale > (mInitImageScale * (mMaxImageScale + mMinImageScale) / 2))
            mTargetImageScale = mMinImageScale * mInitImageScale;
        else
            mTargetImageScale = mMaxImageScale * mInitImageScale;

        computeTargetImageSize();
        setTargetImagePosition();
        checkTargetImagePosition();

        mImageStateChecker.beginCheckState();
    }

    private Matrix mMatrix = new Matrix();
    private float[] mMatrixValues = new float[9];
    private Paint mPaint = new Paint();

    public boolean getMultiSamplingEnabled() {
        return mPaint.isFilterBitmap();
    }

    public void setMultiSamplingEnabled(boolean enabled) {
        mPaint.setFilterBitmap(enabled);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mMatrix.getValues(mMatrixValues);
            mMatrixValues[Matrix.MTRANS_X] = mImageLeft;
            mMatrixValues[Matrix.MTRANS_Y] = mImageTop;
            mMatrixValues[Matrix.MSCALE_X] = mImageScale;
            mMatrixValues[Matrix.MSCALE_Y] = mImageScale;
            mMatrix.setValues(mMatrixValues);
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mImageStateChecker != null)
            mImageStateChecker.stopCheckState();
        mImageStateChecker = new ImageStateChecker();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mBitmap = null;
        mViewWidth = 0;
        mViewHeight = 0;

        if (mImageStateChecker != null)
            mImageStateChecker.stopCheckState();
        mImageStateChecker = null;
    }

}
