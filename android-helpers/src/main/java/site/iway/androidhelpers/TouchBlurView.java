package site.iway.androidhelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TouchBlurView extends View {

    public TouchBlurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchBlurView(Context context) {
        super(context);
    }

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaintBlurred;

    public void setBitmap(Bitmap bitmap, int blurRadius, int strokeWidth) {
        mBitmap = bitmap.copy(Config.ARGB_8888, true);
        mCanvas = new Canvas(mBitmap);
        mPaintBlurred = new Paint();
        Bitmap bitmapBlurred = BitmapHelper.blur(mBitmap, blurRadius);
        TileMode tileMode = TileMode.REPEAT;
        BitmapShader bitmapShader = new BitmapShader(bitmapBlurred, tileMode, tileMode);
        mPaintBlurred.setShader(bitmapShader);
        mPaintBlurred.setAntiAlias(true);
        mPaintBlurred.setDither(true);
        mPaintBlurred.setStyle(Paint.Style.STROKE);
        mPaintBlurred.setFilterBitmap(true);
        mPaintBlurred.setStrokeJoin(Paint.Join.ROUND);
        mPaintBlurred.setStrokeCap(Paint.Cap.ROUND);
        mPaintBlurred.setStrokeWidth(strokeWidth);
        invalidate();
    }

    private Path mPath;

    private float mLastTouchX;
    private float mLastTouchY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentTouchX = event.getX();
        float currentTouchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mPath == null) {
                    mPath = new Path();
                }
                mPath.reset();
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(currentTouchX - mLastTouchX);
                float dy = Math.abs(currentTouchY - mLastTouchY);
                if (dx >= 4 || dy >= 4) {
                    float x1 = mLastTouchX;
                    float y1 = mLastTouchY;
                    float x2 = (currentTouchX + mLastTouchX) / 2;
                    float y2 = (currentTouchY + mLastTouchY) / 2;
                    mPath.quadTo(x1, y1, x2, y2);
                }
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(currentTouchX, currentTouchY);
                break;
        }

        mCanvas.drawPath(mPath, mPaintBlurred);

        mLastTouchX = currentTouchX;
        mLastTouchY = currentTouchY;

        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

}
