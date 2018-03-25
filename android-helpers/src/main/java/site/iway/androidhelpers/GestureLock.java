package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import site.iway.javahelpers.ArrayHelper;
import site.iway.javahelpers.AutoExpandIntArray;
import site.iway.javahelpers.MathHelper;

public class GestureLock extends View {

    public interface GestureLockListener {

        public void onGestureLockBegin();

        public void onGesturePointAdded(int[] route);

        public boolean onGestureLockFinish(int[] route);

    }

    public GestureLock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public GestureLock(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public GestureLock(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private static final int ROW_COUNT = 3;
    private static final int COL_COUNT = 3;

    private GestureLockListener mListener;
    private Drawable mNormalDrawable;
    private Drawable mSelectedDrawable;
    private Drawable mErrorDrawable;
    private boolean mIsShowingError;
    private int mRouteColorSelected;
    private int mRouteColorError;
    private float mRouteWidth;
    private float mRouteShrink;
    private float mTouchDetectRadius;

    public GestureLockListener getListener() {
        return mListener;
    }

    public void setListener(GestureLockListener listener) {
        mListener = listener;
    }

    public Drawable getNormalDrawable() {
        return mNormalDrawable;
    }

    public void setNormalDrawable(Drawable normalDrawable) {
        mNormalDrawable = normalDrawable;
    }

    public void setNormalDrawableResource(int resource) {
        mNormalDrawable = getResources().getDrawable(resource);
    }

    public Drawable getSelectedDrawable() {
        return mSelectedDrawable;
    }

    public void setSelectedDrawable(Drawable selectedDrawable) {
        mSelectedDrawable = selectedDrawable;
    }

    public void setSelectedDrawableResource(int resource) {
        mSelectedDrawable = getResources().getDrawable(resource);
    }

    public Drawable getErrorDrawable() {
        return mErrorDrawable;
    }

    public void setErrorDrawable(Drawable errorDrawable) {
        mErrorDrawable = errorDrawable;
    }

    public void setErrorDrawableResource(int resource) {
        mErrorDrawable = getResources().getDrawable(resource);
    }

    public int getRouteColorSelected() {
        return mRouteColorSelected;
    }

    public void setRouteColorSelected(int routeColorSelected) {
        mRouteColorSelected = routeColorSelected;
    }

    public int getRouteColorError() {
        return mRouteColorError;
    }

    public void setRouteColorError(int routeColorError) {
        mRouteColorError = routeColorError;
    }

    public float getRouteWidth() {
        return mRouteWidth;
    }

    public void setRouteWidth(float routeWidth) {
        mRouteWidth = routeWidth;
    }

    public float getRouteShrink() {
        return mRouteShrink;
    }

    public void setRouteShrink(float routeShrink) {
        mRouteShrink = routeShrink;
    }

    public float getTouchDetectRadius() {
        return mTouchDetectRadius;
    }

    public void setTouchDetectRadius(float touchDetectRadius) {
        mTouchDetectRadius = touchDetectRadius;
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GestureLock);
        mNormalDrawable = a.getDrawable(R.styleable.GestureLock_normalDrawable);
        mSelectedDrawable = a.getDrawable(R.styleable.GestureLock_selectedDrawable);
        mErrorDrawable = a.getDrawable(R.styleable.GestureLock_errorDrawable);
        mRouteColorSelected = a.getColor(R.styleable.GestureLock_routeColorSelected, 0x00000000);
        mRouteColorError = a.getColor(R.styleable.GestureLock_routeColorError, 0x00000000);
        mRouteWidth = a.getDimension(R.styleable.GestureLock_routeWidth, 0);
        mRouteShrink = a.getDimension(R.styleable.GestureLock_routeShrink, 0);
        mTouchDetectRadius = a.getDimension(R.styleable.GestureLock_touchDetectRadius, 0);
        a.recycle();
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

    private AutoExpandIntArray mRoute;
    private float mTouchingX;
    private float mTouchingY;

    private void addRoutePoint(int row, int col) {
        int drawablePoint = row * COL_COUNT + col;
        if (!mRoute.has(drawablePoint)) {
            mRoute.add(drawablePoint);
            if (mListener != null) {
                int[] route = mRoute.getOutArray();
                mListener.onGesturePointAdded(route);
            }
        }
    }

    private Handler mHandler;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = onSuperTouchEvent(event);

        if (mIsShowingError)
            return result;
        if (mNormalDrawable == null)
            return result;
        if (mErrorDrawable == null)
            return result;

        int action = event.getAction();
        mTouchingX = event.getX();
        mTouchingY = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mRoute == null) {
                    mRoute = new AutoExpandIntArray(4);
                }
                mRoute.clear();
                if (mListener != null) {
                    mListener.onGestureLockBegin();
                }
            case MotionEvent.ACTION_MOVE:
                float viewWidth = getWidth();
                float viewHeight = getHeight();
                for (int row = 0; row < ROW_COUNT; row++) {
                    for (int col = 0; col < COL_COUNT; col++) {
                        float drawableCenterX = viewWidth / COL_COUNT * (col + 0.5f);
                        float drawableCenterY = viewHeight / ROW_COUNT * (row + 0.5f);
                        if (mTouchDetectRadius <= 0) {
                            float drawableWidth = mNormalDrawable.getIntrinsicWidth();
                            float drawableHeight = mNormalDrawable.getIntrinsicHeight();
                            float drawableLeft = drawableCenterX - drawableWidth / 2;
                            float drawableRight = drawableCenterX + drawableWidth / 2;
                            float drawableTop = drawableCenterY - drawableHeight / 2;
                            float drawableBottom = drawableCenterY + drawableHeight / 2;
                            boolean xOK = mTouchingX > drawableLeft && mTouchingX < drawableRight;
                            boolean yOK = mTouchingY > drawableTop && mTouchingY < drawableBottom;
                            if (xOK && yOK) {
                                addRoutePoint(row, col);
                            }
                        } else {
                            float dx = mTouchingX - drawableCenterX;
                            float dy = mTouchingY - drawableCenterY;
                            if (MathHelper.distance(dx, dy) < mTouchDetectRadius) {
                                addRoutePoint(row, col);
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    int[] route = mRoute.getOutArray();
                    boolean isGestureRight = mListener.onGestureLockFinish(route);
                    if (isGestureRight) {
                        mRoute.clear();
                    } else {
                        if (mErrorDrawable != null) {
                            if (mHandler == null) {
                                mHandler = new Handler();
                            }
                            mIsShowingError = true;
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mIsShowingError = false;
                                    mRoute.clear();
                                    invalidate();
                                }
                            }, 1000);
                        } else {
                            mRoute.clear();
                        }
                    }
                } else {
                    mRoute.clear();
                }
                break;
        }

        invalidate();

        return true;
    }

    private Paint mPaint;
    private int[] mEmptyRoute;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNormalDrawable == null)
            return;
        if (mSelectedDrawable == null)
            return;
        if (mErrorDrawable == null)
            return;

        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setFilterBitmap(true);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(mRouteWidth);
        }
        mPaint.setColor(mIsShowingError ? mRouteColorError : mRouteColorSelected);

        if (mEmptyRoute == null) {
            mEmptyRoute = new int[0];
        }

        int[] route = mRoute == null ? mEmptyRoute : mRoute.getOutArray();

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        for (int i = 0; i < route.length; i++) {
            float startX = viewWidth * (route[i] % COL_COUNT + 0.5f) / COL_COUNT;
            float startY = viewHeight * (route[i] / ROW_COUNT + 0.5f) / ROW_COUNT;
            if (i + 1 == route.length) {
                float stopX = mTouchingX;
                float stopY = mTouchingY;
                if (mRouteShrink > 0) {
                    float dx = stopX - startX;
                    float dy = stopY - startY;
                    float distance = (float) MathHelper.distance(dx, dy);
                    if (distance > mRouteShrink) {
                        startX = startX + (mRouteShrink / distance) * dx;
                        startY = startY + (mRouteShrink / distance) * dy;
                        canvas.drawLine(startX, startY, stopX, stopY, mPaint);
                    }
                } else {
                    canvas.drawLine(startX, startY, stopX, stopY, mPaint);
                }
            } else {
                float stopX = viewWidth * (route[i + 1] % COL_COUNT + 0.5f) / COL_COUNT;
                float stopY = viewHeight * (route[i + 1] / ROW_COUNT + 0.5f) / ROW_COUNT;
                if (mRouteShrink > 0) {
                    float dx = stopX - startX;
                    float dy = stopY - startY;
                    float distance = (float) MathHelper.distance(dx, dy);
                    if (distance > mRouteShrink * 2) {
                        startX = startX + (mRouteShrink / distance) * dx;
                        startY = startY + (mRouteShrink / distance) * dy;
                        stopX = stopX - (mRouteShrink / distance) * dx;
                        stopY = stopY - (mRouteShrink / distance) * dy;
                        canvas.drawLine(startX, startY, stopX, stopY, mPaint);
                    }
                } else {
                    canvas.drawLine(startX, startY, stopX, stopY, mPaint);
                }
            }
        }

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COL_COUNT; col++) {
                float drawableCenterX = viewWidth / COL_COUNT * (col + 0.5f);
                float drawableCenterY = viewHeight / ROW_COUNT * (row + 0.5f);
                int drawablePoint = row * COL_COUNT + col;
                if (ArrayHelper.contains(route, drawablePoint)) {
                    Drawable drawable = mIsShowingError ? mErrorDrawable : mSelectedDrawable;
                    float drawableWidth = drawable.getIntrinsicWidth();
                    float drawableHeight = drawable.getIntrinsicHeight();
                    int drawableLeft = MathHelper.pixel(drawableCenterX - drawableWidth / 2);
                    int drawableRight = MathHelper.pixel(drawableCenterX + drawableWidth / 2);
                    int drawableTop = MathHelper.pixel(drawableCenterY - drawableHeight / 2);
                    int drawableBottom = MathHelper.pixel(drawableCenterY + drawableHeight / 2);
                    drawable.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
                    drawable.draw(canvas);
                } else {
                    float drawableWidth = mNormalDrawable.getIntrinsicWidth();
                    float drawableHeight = mNormalDrawable.getIntrinsicHeight();
                    int drawableLeft = MathHelper.pixel(drawableCenterX - drawableWidth / 2);
                    int drawableRight = MathHelper.pixel(drawableCenterX + drawableWidth / 2);
                    int drawableTop = MathHelper.pixel(drawableCenterY - drawableHeight / 2);
                    int drawableBottom = MathHelper.pixel(drawableCenterY + drawableHeight / 2);
                    mNormalDrawable.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
                    mNormalDrawable.draw(canvas);
                }
            }
        }

    }

}
