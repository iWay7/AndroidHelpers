package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class LoadingView extends View {

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttrs(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public LoadingView(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private boolean mHasWindow;
    private Drawable mDrawable;
    private int mRotateDegrees;
    private int mRotateFrameSpan;
    private int mRotateFrameTime;
    private UITimer mTimer;

    private void update() {
        if (mTimer != null) {
            mTimer.stop();
        }
        if (mDrawable == null) {
            return;
        }
        if (mRotateFrameSpan % 360 == 0) {
            return;
        }
        if (mRotateFrameTime <= 0) {
            return;
        }
        mTimer = new UITimer(mRotateFrameTime) {
            @Override
            public void doOnUIThread() {
                int visibility = getVisibility();
                if (visibility == View.VISIBLE) {
                    mRotateDegrees += mRotateFrameSpan;
                    invalidate();
                }
            }

            @Override
            public void start(boolean startOnce) {
                mRotateDegrees = 0;
                super.start(startOnce);
            }
        };
        if (mHasWindow) {
            mTimer.start(false);
        }
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        Resources r = context.getResources();
        if (a.hasValue(R.styleable.LoadingView_loadingDrawable))
            mDrawable = a.getDrawable(R.styleable.LoadingView_loadingDrawable);
        else
            mDrawable = r.getDrawable(R.drawable.loading);
        mRotateFrameSpan = a.getInt(R.styleable.LoadingView_rotateFrameSpan, 30);
        mRotateFrameTime = a.getInt(R.styleable.LoadingView_rotateFrameTime, 100);
        a.recycle();
        update();
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
        mRotateDegrees = 0;
        update();
    }

    public int getRotateFrameSpan() {
        return mRotateFrameSpan;
    }

    public void setRotateFrameSpan(int rotateFrameSpan) {
        mRotateFrameSpan = rotateFrameSpan;
        update();
    }

    public int getRotateFrameTime() {
        return mRotateFrameTime;
    }

    public void setRotateFrameTime(int rotateFrameTime) {
        mRotateFrameTime = rotateFrameTime;
        update();
    }

    protected void onDraw(Canvas canvas) {
        if (mDrawable != null) {
            canvas.save();

            int clientLeft = getPaddingLeft();
            int clientTop = getPaddingTop();
            int clientRight = getWidth() - getPaddingRight();
            int clientBottom = getHeight() - getPaddingBottom();

            float centerX = (clientLeft + clientRight) * 0.5f;
            float centerY = (clientLeft + clientRight) * 0.5f;

            canvas.rotate(mRotateDegrees, centerX, centerY);

            mDrawable.setBounds(clientLeft, clientTop, clientRight, clientBottom);
            mDrawable.draw(canvas);

            canvas.restore();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHasWindow = true;
        if (mTimer != null) {
            mTimer.start(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mTimer != null) {
            mTimer.stop();
        }
        mHasWindow = false;
        super.onDetachedFromWindow();
    }

}
