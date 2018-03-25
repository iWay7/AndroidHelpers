package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import site.iway.javahelpers.ArrayHelper;
import site.iway.javahelpers.MathHelper;

public class GestureLockIndicator extends View {

    public GestureLockIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public GestureLockIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public GestureLockIndicator(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private static final int ROW_COUNT = 3;
    private static final int COL_COUNT = 3;

    private Drawable mNormalDrawable;
    private Drawable mSelectedDrawable;

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

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GestureLockIndicator);
        mNormalDrawable = a.getDrawable(R.styleable.GestureLockIndicator_normalDrawable);
        mSelectedDrawable = a.getDrawable(R.styleable.GestureLockIndicator_selectedDrawable);
        a.recycle();
    }

    private int[] mRoute;

    public int[] getRoute() {
        return mRoute;
    }

    public void setRoute(int[] route) {
        mRoute = route;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNormalDrawable == null)
            return;
        if (mSelectedDrawable == null)
            return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COL_COUNT; col++) {
                float drawableCenterX = viewWidth / COL_COUNT * (col + 0.5f);
                float drawableCenterY = viewHeight / ROW_COUNT * (row + 0.5f);
                int drawablePoint = row * COL_COUNT + col;
                if (mRoute != null && ArrayHelper.contains(mRoute, drawablePoint)) {
                    float drawableWidth = mSelectedDrawable.getIntrinsicWidth();
                    float drawableHeight = mSelectedDrawable.getIntrinsicHeight();
                    int drawableLeft = MathHelper.pixel(drawableCenterX - drawableWidth / 2);
                    int drawableRight = MathHelper.pixel(drawableCenterX + drawableWidth / 2);
                    int drawableTop = MathHelper.pixel(drawableCenterY - drawableHeight / 2);
                    int drawableBottom = MathHelper.pixel(drawableCenterY + drawableHeight / 2);
                    mSelectedDrawable.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
                    mSelectedDrawable.draw(canvas);
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
