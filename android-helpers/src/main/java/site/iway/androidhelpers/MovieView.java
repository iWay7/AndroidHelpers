package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;

import site.iway.javahelpers.Scale;

public class MovieView extends View {

    public MovieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public MovieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public MovieView(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private Scale mScale;
    private boolean mRepeated;
    private int mRefreshTimeSpan;

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MovieView);
        int scaleType = a.getInt(R.styleable.MovieView_scaleType, 0);
        switch (scaleType) {
            case 0:
                mScale = Scale.None;
                break;
            case 1:
                mScale = Scale.Center;
                break;
            case 2:
                mScale = Scale.CenterFit;
                break;
            case 3:
                mScale = Scale.CenterCrop;
                break;
            case 4:
                mScale = Scale.CenterInside;
                break;
        }
        mRepeated = a.getBoolean(R.styleable.MovieView_repeated, false);
        mRefreshTimeSpan = a.getInt(R.styleable.MovieView_refreshTimeSpan, 16);
        a.recycle();

        mUITimer = new UITimer(mRefreshTimeSpan) {
            @Override
            public void doOnUIThread() {
                long now = System.nanoTime() / 1000000;
                int time = (int) (now - mStartTime);
                if (mRepeated) {
                    time = time % mMovie.duration();
                } else if (time > mMovie.duration()) {
                    time = mMovie.duration();
                }
                mMovie.setTime(time);
                mMovie.draw(mTempCanvas, 0, 0);
                invalidate();
            }
        };
    }

    private boolean mLoaded;
    private Movie mMovie;
    private Bitmap mTempImage;
    private Canvas mTempCanvas;
    private Paint mPaint;

    private long mStartTime;
    private UITimer mUITimer;

    private void start() {
        if (mMovie != null) {
            mStartTime = System.nanoTime() / 1000000;
            mUITimer.start(true);
        }
    }

    private void stop() {
        mUITimer.stop();
    }

    public void load(InputStream inputStream) {
        mMovie = Movie.decodeStream(inputStream);
        if (mMovie != null) {
            int width = mMovie.width();
            int height = mMovie.height();
            Config config = Config.ARGB_8888;
            mTempImage = Bitmap.createBitmap(width, height, config);
            mTempCanvas = new Canvas(mTempImage);
            mPaint = new Paint();
            mPaint.setFilterBitmap(true);
            mLoaded = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLoaded) {
            CanvasHelper.drawBitmap(canvas, null, mTempImage, null, mScale, mPaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

}
