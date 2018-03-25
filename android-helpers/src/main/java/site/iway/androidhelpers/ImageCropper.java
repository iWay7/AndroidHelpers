package site.iway.androidhelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import site.iway.javahelpers.MathHelper;

public class ImageCropper extends View {

    public ImageCropper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageCropper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageCropper(Context context) {
        super(context);
    }

    private Bitmap mBitmap;
    private RectF mBitmapRect;
    private float mInitRectPercent = 0.5f;

    public float getInitCropRectPercent() {
        return mInitRectPercent;
    }

    public void setInitCropRectPercent(float percent) {
        mInitRectPercent = percent;
    }

    private void setBitmapAndCropRect() {
        if (mBitmap == null)
            return;
        float targetScale = 1.0f;
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth / mBitmap.getWidth() * mBitmap.getHeight() <= viewHeight)
            targetScale = viewWidth / mBitmap.getWidth();
        if (viewHeight / mBitmap.getHeight() * mBitmap.getWidth() <= viewWidth)
            targetScale = viewHeight / mBitmap.getHeight();
        float scaledImageWidth = targetScale * mBitmap.getWidth();
        float scaledImageHeight = targetScale * mBitmap.getHeight();
        float left = (viewWidth - scaledImageWidth) / 2;
        float top = (viewHeight - scaledImageHeight) / 2;
        mBitmapRect = new RectF(left, top, left + scaledImageWidth, top + scaledImageHeight);
        float cropWidth = scaledImageWidth * mInitRectPercent;
        float cropHeight = scaledImageHeight * mInitRectPercent;
        if (mCropRatio > 0) {
            if (cropHeight > cropWidth / mCropRatio)
                cropHeight = cropWidth / mCropRatio;
            if (cropWidth > cropHeight * mCropRatio)
                cropWidth = cropHeight * mCropRatio;
        }
        left = (viewWidth - cropWidth) / 2;
        top = (viewHeight - cropHeight) / 2;
        mCropRect = new RectF(left, top, left + cropWidth, top + cropHeight);
        invalidate();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        setBitmapAndCropRect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setBitmapAndCropRect();
    }

    private Drawable mCropDrawable;
    private RectF mCropRect;
    private float mCropBorderWidth;
    private float mMinCropRectSideLength = UnitHelper.dipToPx(getContext(), 20);
    private float mCropRatio = -1;
    private float mCropRatioBase;
    private float mCropRatioForX;
    private float mCropRatioForY;

    public Drawable getCropDrawable() {
        return mCropDrawable;
    }

    public void setCropDrawable(Drawable drawable) {
        mCropDrawable = drawable;
    }

    public void setCropDrawable(int resourceId) {
        mCropDrawable = getContext().getResources().getDrawable(resourceId);
    }

    public float getCropBorderWidth() {
        return mCropBorderWidth;
    }

    public void setCropBorderWidth(float width) {
        mCropBorderWidth = width;
    }

    public float getMinCropDrawableRectSideLength() {
        return mMinCropRectSideLength;
    }

    public void setMinCropDrawableRectSideLength(float minCropDrawableRectSideLength) {
        mMinCropRectSideLength = minCropDrawableRectSideLength;
    }

    public float getCropRatio() {
        return mCropRatio;
    }

    public void setCropRatio(float ratio) {
        mCropRatio = ratio;
        mCropRatioBase = (float) Math.sqrt(mCropRatio * mCropRatio + 1);
        mCropRatioForX = mCropRatio / mCropRatioBase;
        mCropRatioForY = 1 / mCropRatioBase;
        setBitmapAndCropRect();
    }

    private Drawable mCoverDrawable;

    public Drawable getCoverDrawable() {
        return mCoverDrawable;
    }

    public void setCoverDrawable(Drawable drawable) {
        mCoverDrawable = drawable;
        if (!(mCoverDrawable instanceof ColorDrawable) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        }
    }

    public void setCoverDrawable(int resourceId) {
        setCoverDrawable(getContext().getResources().getDrawable(resourceId));
    }

    private boolean mAdjustLeftEdge;
    private boolean mAdjustTopEdge;
    private boolean mAdjustRightEdge;
    private boolean mAdjustBottomEdge;
    private boolean mAdjustCenterPoint;

    private float mTouchDownX;
    private float mTouchDownY;

    private float mSavedCropLeft;
    private float mSavedCropTop;
    private float mSavedCropRight;
    private float mSavedCropBottom;

    private void saveTouchDownStates(MotionEvent event) {
        mTouchDownX = event.getX();
        mTouchDownY = event.getY();
        mSavedCropLeft = mCropRect.left;
        mSavedCropTop = mCropRect.top;
        mSavedCropRight = mCropRect.right;
        mSavedCropBottom = mCropRect.bottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBitmap == null || mCropDrawable == null)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                saveTouchDownStates(event);
                mAdjustLeftEdge = mTouchDownX <= mCropRect.left + mCropBorderWidth;
                mAdjustTopEdge = mTouchDownY <= mCropRect.top + mCropBorderWidth;
                mAdjustRightEdge = mTouchDownX >= mCropRect.right - mCropBorderWidth;
                mAdjustBottomEdge = mTouchDownY >= mCropRect.bottom - mCropBorderWidth;
                mAdjustCenterPoint = !(mAdjustLeftEdge || mAdjustTopEdge || mAdjustRightEdge || mAdjustBottomEdge);
                if (mCropRatio > 0) {
                    mAdjustLeftEdge = mTouchDownX < mCropRect.centerX();
                    mAdjustTopEdge = mTouchDownY < mCropRect.centerY();
                    mAdjustRightEdge = mTouchDownX > mCropRect.centerX();
                    mAdjustBottomEdge = mTouchDownY > mCropRect.centerY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mTouchDownX;
                float dy = event.getY() - mTouchDownY;
                if (mAdjustCenterPoint) {
                    float targetCropLeft = mSavedCropLeft + dx;
                    float targetCropTop = mSavedCropTop + dy;
                    float targetCropRight = mSavedCropRight + dx;
                    float targetCropBottom = mSavedCropBottom + dy;
                    float savedCropWidth = mSavedCropRight - mSavedCropLeft;
                    float savedCropHeight = mSavedCropBottom - mSavedCropTop;
                    if (targetCropLeft < mBitmapRect.left) {
                        targetCropLeft = mBitmapRect.left;
                        targetCropRight = (mBitmapRect.left + savedCropWidth);
                    }
                    if (targetCropTop < mBitmapRect.top) {
                        targetCropTop = mBitmapRect.top;
                        targetCropBottom = (mBitmapRect.top + savedCropHeight);
                    }
                    if (targetCropRight > mBitmapRect.right) {
                        targetCropRight = mBitmapRect.right;
                        targetCropLeft = (mBitmapRect.right - savedCropWidth);
                    }
                    if (targetCropBottom > mBitmapRect.bottom) {
                        targetCropBottom = mBitmapRect.bottom;
                        targetCropTop = (mBitmapRect.bottom - savedCropHeight);
                    }
                    mCropRect.left = targetCropLeft;
                    mCropRect.top = targetCropTop;
                    mCropRect.right = targetCropRight;
                    mCropRect.bottom = targetCropBottom;
                } else if (mCropRatio > 0) {
                    if (mAdjustLeftEdge && mAdjustTopEdge) {
                        float dst1 = (float) MathHelper.distance(mTouchDownX, mTouchDownY, getWidth(), getHeight());
                        float dst2 = (float) MathHelper.distance(event.getX(), event.getY(), getWidth(), getHeight());
                        float distanceChanged = dst2 - dst1;
                        float xChange = distanceChanged * mCropRatioForX;
                        float yChange = distanceChanged * mCropRatioForY;
                        float targetCropTop = mSavedCropTop - yChange;
                        float targetCropLeft = mSavedCropLeft - xChange;
                        boolean isTopTooBig = targetCropTop < mBitmapRect.top;
                        if (isTopTooBig) {
                            targetCropTop = mBitmapRect.top;
                            targetCropLeft = mSavedCropRight - (mSavedCropBottom - targetCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isLeftTooBig = targetCropLeft < mBitmapRect.left;
                        if (isLeftTooBig) {
                            targetCropLeft = mBitmapRect.left;
                            targetCropTop = mSavedCropBottom - (mSavedCropRight - targetCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isTopTooSmall = targetCropTop > mSavedCropBottom - mMinCropRectSideLength;
                        if (isTopTooSmall) {
                            targetCropTop = mSavedCropBottom - mMinCropRectSideLength;
                            targetCropLeft = mSavedCropRight - (mSavedCropBottom - targetCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isLeftTooSmall = targetCropLeft > mSavedCropRight - mMinCropRectSideLength;
                        if (isLeftTooSmall) {
                            targetCropLeft = mSavedCropRight - mMinCropRectSideLength;
                            targetCropTop = mSavedCropBottom - (mSavedCropRight - targetCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        mCropRect.top = targetCropTop;
                        mCropRect.left = targetCropLeft;
                    } else if (mAdjustLeftEdge && mAdjustBottomEdge) {
                        float dst1 = (float) MathHelper.distance(mTouchDownX, mTouchDownY, getWidth(), 0);
                        float dst2 = (float) MathHelper.distance(event.getX(), event.getY(), getWidth(), 0);
                        float distanceChanged = dst2 - dst1;
                        float xChange = distanceChanged * mCropRatioForX;
                        float yChange = distanceChanged * mCropRatioForY;
                        float targetCropBottom = mSavedCropBottom + yChange;
                        float targetCropLeft = mSavedCropLeft - xChange;
                        boolean isBottomTooBig = targetCropBottom > mBitmapRect.bottom;
                        if (isBottomTooBig) {
                            targetCropBottom = mBitmapRect.bottom;
                            targetCropLeft = mSavedCropRight - (targetCropBottom - mSavedCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isLeftTooBig = targetCropLeft < mBitmapRect.left;
                        if (isLeftTooBig) {
                            targetCropLeft = mBitmapRect.left;
                            targetCropBottom = mSavedCropTop + (mSavedCropRight - targetCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isBottomTooSmall = targetCropBottom < mSavedCropTop + mMinCropRectSideLength;
                        if (isBottomTooSmall) {
                            targetCropBottom = mSavedCropTop + mMinCropRectSideLength;
                            targetCropLeft = mSavedCropRight - (targetCropBottom - mSavedCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isLeftTooSmall = targetCropLeft > mSavedCropRight - mMinCropRectSideLength;
                        if (isLeftTooSmall) {
                            targetCropLeft = mSavedCropRight - mMinCropRectSideLength;
                            targetCropBottom = mSavedCropTop + (mSavedCropRight - targetCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        mCropRect.bottom = targetCropBottom;
                        mCropRect.left = targetCropLeft;
                    } else if (mAdjustRightEdge && mAdjustTopEdge) {
                        float dst1 = (float) MathHelper.distance(mTouchDownX, mTouchDownY, 0, getHeight());
                        float dst2 = (float) MathHelper.distance(event.getX(), event.getY(), 0, getHeight());
                        float distanceChanged = dst2 - dst1;
                        float xChange = distanceChanged * mCropRatioForX;
                        float yChange = distanceChanged * mCropRatioForY;
                        float targetCropTop = mSavedCropTop - yChange;
                        float targetCropRight = mSavedCropRight + xChange;
                        boolean isTopTooBig = targetCropTop < mBitmapRect.top;
                        if (isTopTooBig) {
                            targetCropTop = mBitmapRect.top;
                            targetCropRight = mSavedCropLeft + (mSavedCropBottom - targetCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isRightTooBig = targetCropRight > mBitmapRect.right;
                        if (isRightTooBig) {
                            targetCropRight = mBitmapRect.right;
                            targetCropTop = mSavedCropBottom - (targetCropRight - mSavedCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isTopTooSmall = targetCropTop > mSavedCropBottom - mMinCropRectSideLength;
                        if (isTopTooSmall) {
                            targetCropTop = mSavedCropBottom - mMinCropRectSideLength;
                            targetCropRight = mSavedCropLeft + (mSavedCropBottom - targetCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isRightTooSmall = targetCropRight < mSavedCropLeft + mMinCropRectSideLength;
                        if (isRightTooSmall) {
                            targetCropRight = mSavedCropLeft + mMinCropRectSideLength;
                            targetCropTop = mSavedCropBottom - (targetCropRight - mSavedCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        mCropRect.top = targetCropTop;
                        mCropRect.right = targetCropRight;
                    } else if (mAdjustRightEdge && mAdjustBottomEdge) {
                        float dst1 = (float) MathHelper.distance(mTouchDownX, mTouchDownY, 0, 0);
                        float dst2 = (float) MathHelper.distance(event.getX(), event.getY(), 0, 0);
                        float distanceChanged = dst2 - dst1;
                        float xChange = distanceChanged * mCropRatioForX;
                        float yChange = distanceChanged * mCropRatioForY;
                        float targetCropBottom = mSavedCropBottom + yChange;
                        float targetCropRight = mSavedCropRight + xChange;
                        boolean isBottomTooBig = targetCropBottom > mBitmapRect.bottom;
                        if (isBottomTooBig) {
                            targetCropBottom = mBitmapRect.bottom;
                            targetCropRight = mSavedCropLeft + (targetCropBottom - mSavedCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isRightTooBig = targetCropRight > mBitmapRect.right;
                        if (isRightTooBig) {
                            targetCropRight = mBitmapRect.right;
                            targetCropBottom = mSavedCropTop + (targetCropRight - mSavedCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isBottomTooSmall = targetCropBottom < mSavedCropTop + mMinCropRectSideLength;
                        if (isBottomTooSmall) {
                            targetCropBottom = mSavedCropTop + mMinCropRectSideLength;
                            targetCropRight = mSavedCropLeft + (targetCropBottom - mSavedCropTop) * mCropRatio;
                            saveTouchDownStates(event);
                        }
                        boolean isRightTooSmall = targetCropRight < mSavedCropLeft + mMinCropRectSideLength;
                        if (isRightTooSmall) {
                            targetCropRight = mSavedCropLeft + mMinCropRectSideLength;
                            targetCropBottom = mSavedCropTop + (targetCropRight - mSavedCropLeft) / mCropRatio;
                            saveTouchDownStates(event);
                        }
                        mCropRect.bottom = targetCropBottom;
                        mCropRect.right = targetCropRight;
                    }
                } else {
                    if (mAdjustLeftEdge) {
                        float targetCropLeft = mSavedCropLeft + dx;
                        if (targetCropLeft > mSavedCropRight - mMinCropRectSideLength) {
                            targetCropLeft = mSavedCropRight - mMinCropRectSideLength;
                            saveTouchDownStates(event);
                        }
                        if (targetCropLeft < mBitmapRect.left) {
                            targetCropLeft = mBitmapRect.left;
                            saveTouchDownStates(event);
                        }
                        mCropRect.left = targetCropLeft;
                    }
                    if (mAdjustTopEdge) {
                        float targetCropTop = mSavedCropTop + dy;
                        if (targetCropTop > mSavedCropBottom - mMinCropRectSideLength) {
                            targetCropTop = mSavedCropBottom - mMinCropRectSideLength;
                            saveTouchDownStates(event);
                        }
                        if (targetCropTop < mBitmapRect.top) {
                            targetCropTop = mBitmapRect.top;
                            saveTouchDownStates(event);
                        }
                        mCropRect.top = targetCropTop;
                    }
                    if (mAdjustRightEdge) {
                        float targetCropRight = mSavedCropRight + dx;
                        if (targetCropRight < mSavedCropLeft + mMinCropRectSideLength) {
                            targetCropRight = mSavedCropLeft + mMinCropRectSideLength;
                            saveTouchDownStates(event);
                        }
                        if (targetCropRight > mBitmapRect.right) {
                            targetCropRight = mBitmapRect.right;
                            saveTouchDownStates(event);
                        }
                        mCropRect.right = targetCropRight;
                    }
                    if (mAdjustBottomEdge) {
                        float targetCropBottom = mSavedCropBottom + dy;
                        if (targetCropBottom < mSavedCropTop + mMinCropRectSideLength) {
                            targetCropBottom = mSavedCropTop + mMinCropRectSideLength;
                            saveTouchDownStates(event);
                        }
                        if (targetCropBottom > mBitmapRect.bottom) {
                            targetCropBottom = mBitmapRect.bottom;
                            saveTouchDownStates(event);
                        }
                        mCropRect.bottom = targetCropBottom;
                    }
                }

                setCropDrawableRect(true);
                break;
        }
        return true;
    }

    private Paint mPaint = new Paint();

    public boolean getMultiSamplingEnabled() {
        return mPaint.isFilterBitmap();
    }

    public void setMultiSamplingEnabled(boolean enabled) {
        mPaint.setFilterBitmap(enabled);
    }

    private Rect mCropRectInt = new Rect();

    private void setCropDrawableRect(boolean invalidate) {
        mCropRectInt.left = (int) (mCropRect.left + 0.5f);
        mCropRectInt.top = (int) (mCropRect.top + 0.5f);
        mCropRectInt.right = (int) (mCropRect.right + 0.5f);
        mCropRectInt.bottom = (int) (mCropRect.bottom + 0.5f);
        mCropDrawable.setBounds(mCropRectInt);
        if (invalidate) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null || mCropDrawable == null)
            return;
        canvas.drawBitmap(mBitmap, null, mBitmapRect, mPaint);
        setCropDrawableRect(false);
        if (mCoverDrawable != null) {
            if (mCoverDrawable instanceof ColorDrawable) {
                mCoverDrawable.setBounds(0, 0, getWidth(), mCropRectInt.top);
                mCoverDrawable.draw(canvas);
                mCoverDrawable.setBounds(0, mCropRectInt.top, mCropRectInt.left, mCropRectInt.bottom);
                mCoverDrawable.draw(canvas);
                mCoverDrawable.setBounds(mCropRectInt.right, mCropRectInt.top, getWidth(), mCropRectInt.bottom);
                mCoverDrawable.draw(canvas);
                mCoverDrawable.setBounds(0, mCropRectInt.bottom, getWidth(), getHeight());
                mCoverDrawable.draw(canvas);
            } else {
                canvas.save();
                canvas.clipRect(mCropRectInt, Op.DIFFERENCE);
                mCoverDrawable.setBounds(0, 0, getWidth(), getHeight());
                mCoverDrawable.draw(canvas);
                canvas.restore();
            }
        }
        mCropDrawable.draw(canvas);
    }

    public RectF getCroppedRectF() {
        float x = (mCropRect.left - mBitmapRect.left) * mBitmap.getWidth() / mBitmapRect.width();
        float y = (mCropRect.top - mBitmapRect.top) * mBitmap.getHeight() / mBitmapRect.height();
        float w = mCropRect.width() * mBitmap.getWidth() / mBitmapRect.width();
        float h = mCropRect.height() * mBitmap.getHeight() / mBitmapRect.height();
        return new RectF(x, y, x + w, y + h);
    }

    public Rect getCroppedRect() {
        RectF rectf = getCroppedRectF();
        int l = (int) (rectf.left + 0.5f);
        int t = (int) (rectf.top + 0.5f);
        int r = (int) (rectf.right + 0.5f);
        int b = (int) (rectf.bottom + 0.5f);
        return new Rect(l, t, r, b);
    }

    public Bitmap getCroppedBitmap() {
        Rect rect = getCroppedRect();
        int left = rect.left;
        int top = rect.top;
        int width = rect.width();
        int height = rect.height();
        return Bitmap.createBitmap(mBitmap, left, top, width, height);
    }

}
