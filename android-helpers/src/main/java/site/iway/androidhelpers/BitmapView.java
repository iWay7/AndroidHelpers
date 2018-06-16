package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.HashMap;
import java.util.Map;

import site.iway.javahelpers.Scale;

public class BitmapView extends View implements BitmapCallback {

    private static Map<String, BitmapFilter> mCachedBitmapFilters = new HashMap<>();

    private static BitmapFilter createBitmapFilter(Scale s, int w, int h, float r) {
        if (mCachedBitmapFilters == null) {
            mCachedBitmapFilters = new HashMap<>();
        }
        String key = s.ordinal() + "," + w + "," + h + "," + r;
        BitmapFilter value = mCachedBitmapFilters.get(key);
        if (value == null) {
            value = new BitmapFilterClip(s, w, h, r);
            mCachedBitmapFilters.put(key, value);
        }
        return value;
    }

    public BitmapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resolveAttr(context, attrs);
    }

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttr(context, attrs);
    }

    public BitmapView(Context context) {
        super(context);
        resolveAttr(context, null);
    }

    private Drawable mBackDrawable;
    private Drawable mForeDrawable;
    private Drawable mEmptyDrawable;
    private Drawable mErrorDrawable;

    public Drawable getBackDrawable() {
        return mBackDrawable;
    }

    public void setBackDrawable(Drawable backDrawable) {
        mBackDrawable = backDrawable;
        invalidate();
    }

    public void setBackDrawableResource(int resId) {
        Drawable drawable = getContext().getResources().getDrawable(resId);
        setBackDrawable(drawable);
    }

    public Drawable getForeDrawable() {
        return mForeDrawable;
    }

    public void setForeDrawable(Drawable foreDrawable) {
        mForeDrawable = foreDrawable;
        invalidate();
    }

    public void setForeDrawableResource(int resId) {
        Drawable drawable = getContext().getResources().getDrawable(resId);
        setForeDrawable(drawable);
    }

    public Drawable getEmptyDrawable() {
        return mEmptyDrawable;
    }

    public void setEmptyDrawable(Drawable drawable) {
        mEmptyDrawable = drawable;
        invalidate();
    }

    public void setEmptyDrawableResource(int resId) {
        Drawable drawable = getContext().getResources().getDrawable(resId);
        setEmptyDrawable(drawable);
    }

    public Drawable getErrorDrawable() {
        return mErrorDrawable;
    }

    public void setErrorDrawable(Drawable drawable) {
        mErrorDrawable = drawable;
        invalidate();
    }

    public void setErrorDrawableResource(int resId) {
        Drawable drawable = getContext().getResources().getDrawable(resId);
        setErrorDrawable(drawable);
    }

    private Scale mScale;
    private boolean mUseDefaultFilter;
    private float mRoundCornerRadius;

    public Scale getScale() {
        return mScale;
    }

    public void setScale(Scale scale) {
        mScale = scale;
        invalidate();
    }

    public boolean isUseDefaultFilter() {
        return mUseDefaultFilter;
    }

    public void setUseDefaultFilter(boolean useDefaultFilter) {
        mUseDefaultFilter = useDefaultFilter;
        invalidate();
    }

    public float getRoundCornerRadius() {
        return mRoundCornerRadius;
    }

    public void setRoundCornerRadius(float roundCornerRadius) {
        mRoundCornerRadius = roundCornerRadius;
        invalidate();
    }

    private Animation mFinishAnimation;

    public Animation getFinishAnimation() {
        return mFinishAnimation;
    }

    public void setFinishAnimation(Animation animation) {
        mFinishAnimation = animation;
    }

    public void setFinishAnimation(int animationResId) {
        if (animationResId != 0) {
            Context context = getContext();
            mFinishAnimation = AnimationUtils.loadAnimation(context, animationResId);
        } else {
            mFinishAnimation = null;
        }
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BitmapView);
        mBackDrawable = a.getDrawable(R.styleable.BitmapView_backDrawable);
        mForeDrawable = a.getDrawable(R.styleable.BitmapView_foreDrawable);
        mEmptyDrawable = a.getDrawable(R.styleable.BitmapView_emptyDrawable);
        mErrorDrawable = a.getDrawable(R.styleable.BitmapView_errorDrawable);
        int scaleType = a.getInt(R.styleable.BitmapView_scaleType, 0);
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
        mUseDefaultFilter = a.getBoolean(R.styleable.BitmapView_useDefaultFilter, false);
        mRoundCornerRadius = a.getDimension(R.styleable.BitmapView_roundCornerRadius, 0);
        setFinishAnimation(a.getResourceId(R.styleable.BitmapView_finishAnimation, 0));
        a.recycle();
    }

    private boolean mHasAttachedToWindow;
    private BitmapSource mBitmapSource;
    private BitmapRequest mBitmapRequest;

    public void loadFromSource(int type, String content, BitmapFilter filter) {
        try {
            mBitmapSource = new BitmapSource(type, content, filter);
        } catch (Exception e) {
            mBitmapSource = null;
        }
        mBitmapRequest = null;
        clearAnimation();
        invalidate();
    }

    public void loadFromSource(BitmapSource source) {
        mBitmapSource = source;
        mBitmapRequest = null;
        clearAnimation();
        invalidate();
    }

    public void loadFromAssetSource(String asset, BitmapFilter filter) {
        loadFromSource(BitmapSource.TYPE_ASSET, asset, filter);
    }

    public void loadFromAssetSource(String asset) {
        loadFromAssetSource(asset, null);
    }

    public void loadFromFileSource(String file, BitmapFilter filter) {
        loadFromSource(BitmapSource.TYPE_FILE, file, filter);
    }

    public void loadFromFileSource(String file) {
        loadFromFileSource(file, null);
    }

    public void loadFromResourceSource(int resourceId, BitmapFilter filter) {
        loadFromSource(BitmapSource.TYPE_RESOURCE, String.valueOf(resourceId), filter);
    }

    public void loadFromResourceSource(int resourceId) {
        loadFromResourceSource(resourceId, null);
    }

    public void loadFromURLSource(String url, BitmapFilter filter) {
        loadFromSource(BitmapSource.TYPE_URL, url, filter);
    }

    public void loadFromURLSource(String url) {
        loadFromURLSource(url, null);
    }

    @Override
    public void onBitmapLoadProgressChange(BitmapRequest request) {
        if (mHasAttachedToWindow && request == mBitmapRequest) {
            int progress = request.getProgress();
            if (progress == BitmapRequest.GET_BITMAP || progress == BitmapRequest.GET_ERROR) {
                if (mFinishAnimation != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            startAnimation(mFinishAnimation);
                        }
                    });
                }
                postInvalidate();
            }
        }
    }

    private Rect mClientRect = new Rect();
    private RectF mClientRectF = new RectF();

    private void setClientRect() {
        int clientLeft = getPaddingLeft();
        int clientTop = getPaddingTop();
        int clientRight = getWidth() - getPaddingRight();
        int clientBottom = getHeight() - getPaddingBottom();
        mClientRect.set(clientLeft, clientTop, clientRight, clientBottom);
        mClientRectF.set(clientLeft, clientTop, clientRight, clientBottom);
    }

    private Paint mPaint;

    private void drawDrawable(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(mClientRect);
            drawable.draw(canvas);
        }
    }

    private boolean checkBitmapSource() {
        if (mUseDefaultFilter) {
            int w = mClientRect.width();
            int h = mClientRect.height();
            BitmapFilter filter = createBitmapFilter(mScale, w, h, mRoundCornerRadius);
            if (mBitmapSource.filter != filter) {
                int type = mBitmapSource.type;
                String content = mBitmapSource.content;
                mBitmapSource = new BitmapSource(type, content, filter);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setClientRect();
        drawDrawable(canvas, mBackDrawable);
        if (mBitmapSource == null) {
            drawDrawable(canvas, mEmptyDrawable);
        } else {
            boolean bitmapSourceChanged = checkBitmapSource();
            Bitmap cachedBitmap = BitmapCache.get(mBitmapSource);
            if (cachedBitmap == null) {
                if (mBitmapRequest == null || bitmapSourceChanged) {
                    mBitmapRequest = new BitmapRequest(mBitmapSource, this);
                    BitmapCache.requestNow(mBitmapRequest);
                    drawDrawable(canvas, mEmptyDrawable);
                } else {
                    switch (mBitmapRequest.getProgress()) {
                        case BitmapRequest.GET_BITMAP:
                            mBitmapRequest = new BitmapRequest(mBitmapSource, this);
                            BitmapCache.requestNow(mBitmapRequest);
                            drawDrawable(canvas, mEmptyDrawable);
                            break;
                        case BitmapRequest.GET_ERROR:
                            drawDrawable(canvas, mErrorDrawable);
                            break;
                        default:
                            drawDrawable(canvas, mEmptyDrawable);
                            break;
                    }
                }
            } else {
                if (mPaint == null) {
                    mPaint = new Paint();
                    mPaint.setAntiAlias(true);
                    mPaint.setFilterBitmap(true);
                }
                CanvasHelper.drawBitmap(canvas, mClientRectF, cachedBitmap, null, mScale, mPaint);
            }
        }
        drawDrawable(canvas, mForeDrawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHasAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mBitmapRequest != null) {
            mBitmapRequest.cancel();
            mBitmapRequest = null;
        }
        mHasAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

}
