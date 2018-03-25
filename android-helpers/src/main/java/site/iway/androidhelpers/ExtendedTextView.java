package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class ExtendedTextView extends TextView {

    public ExtendedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public ExtendedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedTextView(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private boolean mDrawRoundRect;
    private int mRoundRectColor;
    private float mRoundRectRadius;
    private boolean mDrawRoundRectIncludePadding;

    public boolean isDrawRoundRect() {
        return mDrawRoundRect;
    }

    public void setDrawRoundRect(boolean drawRoundRect) {
        mDrawRoundRect = drawRoundRect;
        invalidate();
    }

    public int getRoundRectColor() {
        return mRoundRectColor;
    }

    public void setRoundRectColor(int roundRectColor) {
        mRoundRectColor = roundRectColor;
        invalidate();
    }

    public float getRoundRectRadius() {
        return mRoundRectRadius;
    }

    public void setRoundRectRadius(float roundRectRadius) {
        mRoundRectRadius = roundRectRadius;
        invalidate();
    }

    public boolean isDrawRoundRectIncludePadding() {
        return mDrawRoundRectIncludePadding;
    }

    public void setDrawRoundRectIncludePadding(boolean drawRoundRectIncludePadding) {
        mDrawRoundRectIncludePadding = drawRoundRectIncludePadding;
    }

    private Drawable mLineDrawable;
    private int mLineDrawableHeight;
    private int mLineDrawableOffset;

    public Drawable getLineDrawable() {
        return mLineDrawable;
    }

    public void setLineDrawable(Drawable drawable) {
        mLineDrawable = drawable;
        invalidate();
    }

    public void setLineDrawable(int resourceId) {
        Drawable drawable = getContext().getResources().getDrawable(resourceId);
        setLineDrawable(drawable);
    }

    public int getLineDrawableHeight() {
        return mLineDrawableHeight;
    }

    public void setLineDrawableHeight(int lineDrawableHeight) {
        mLineDrawableHeight = lineDrawableHeight;
        invalidate();
    }

    public int getLineDrawableOffset() {
        return mLineDrawableOffset;
    }

    public void setLineDrawableOffset(int lineDrawableOffset) {
        mLineDrawableOffset = lineDrawableOffset;
        invalidate();
    }

    private Animation mTouchDownAnimation;
    private Animation mTouchUpAnimation;

    public void setTouchDownAnimation(Animation animation) {
        mTouchDownAnimation = animation;
    }

    public void setTouchDownAnimation(int resourceId) {
        if (resourceId != 0) {
            Context context = getContext();
            Animation animation = AnimationUtils.loadAnimation(context, resourceId);
            setTouchDownAnimation(animation);
        }
    }

    public void setTouchUpAnimation(Animation animation) {
        mTouchUpAnimation = animation;
    }

    public void setTouchUpAnimation(int resourceId) {
        if (resourceId != 0) {
            Context context = getContext();
            Animation animation = AnimationUtils.loadAnimation(context, resourceId);
            setTouchUpAnimation(animation);
        }
    }

    private int mTextPressAlpha;
    private int mDrawablePressAlpha;

    public void setTextPressAlpha(int textPressAlpha) {
        mTextPressAlpha = textPressAlpha;
    }

    public void setDrawablePressAlpha(int drawablePressAlpha) {
        mDrawablePressAlpha = drawablePressAlpha;
    }

    private int mFilterClickSpan;

    @Override
    public void setOnClickListener(final OnClickListener l) {
        if (mFilterClickSpan > 0) {
            OnClickListener wrapper = new OnClickListener() {
                private long mLastClickTime;

                @Override
                public void onClick(View v) {
                    long now = System.currentTimeMillis();
                    if (now - mLastClickTime > mFilterClickSpan) {
                        mLastClickTime = System.currentTimeMillis();
                        l.onClick(v);
                    }
                }
            };
            super.setOnClickListener(wrapper);
        } else {
            super.setOnClickListener(l);
        }
    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedTextView);
        String typeFaceAssetPath = a.getString(R.styleable.ExtendedTextView_typeFaceAssetPath);
        Typeface typeface = TypefaceHelper.get(context, typeFaceAssetPath);
        if (typeface != null) {
            Typeface oldTypeface = getTypeface();
            if (oldTypeface != null) {
                int oldTypefaceStyle = oldTypeface.getStyle();
                setTypeface(typeface, oldTypefaceStyle);
            } else {
                setTypeface(typeface);
            }
        }
        mDrawRoundRect = a.getBoolean(R.styleable.ExtendedTextView_drawRoundRect, false);
        mRoundRectColor = a.getColor(R.styleable.ExtendedTextView_roundRectColor, 0x00000000);
        mRoundRectRadius = a.getDimension(R.styleable.ExtendedTextView_roundRectRadius, 0);
        mDrawRoundRectIncludePadding = a.getBoolean(R.styleable.ExtendedTextView_drawRoundRectIncludePadding, false);
        mLineDrawable = a.getDrawable(R.styleable.ExtendedTextView_lineDrawable);
        mLineDrawableHeight = a.getDimensionPixelSize(R.styleable.ExtendedTextView_lineDrawableHeight, 1);
        mLineDrawableOffset = a.getDimensionPixelSize(R.styleable.ExtendedTextView_lineDrawableOffset, 0);
        setTouchDownAnimation(a.getResourceId(R.styleable.ExtendedTextView_touchDownAnimation, 0));
        setTouchUpAnimation(a.getResourceId(R.styleable.ExtendedTextView_touchUpAnimation, 0));
        mTextPressAlpha = a.getInt(R.styleable.ExtendedTextView_textPressAlpha, -1);
        mDrawablePressAlpha = a.getInt(R.styleable.ExtendedTextView_drawablePressAlpha, -1);
        mFilterClickSpan = a.getInt(R.styleable.ExtendedTextView_filterClickSpan, 0);
        a.recycle();
    }

    private boolean mLastPressed;

    @Override
    public void setPressed(boolean pressed) {
        if (mLastPressed != pressed) {
            int textColor = getCurrentTextColor();
            int red = Color.red(textColor);
            int green = Color.green(textColor);
            int blue = Color.blue(textColor);
            int textColorHint = getCurrentHintTextColor();
            int redHint = Color.red(textColorHint);
            int greenHint = Color.green(textColorHint);
            int blueHint = Color.blue(textColorHint);
            Drawable[] drawables = getCompoundDrawables();
            if (pressed) {
                if (mTextPressAlpha >= 0 && mTextPressAlpha <= 255) {
                    setTextColor(Color.argb(mTextPressAlpha, red, green, blue));
                    setHintTextColor(Color.argb(mTextPressAlpha, redHint, greenHint, blueHint));
                }
                for (Drawable drawable : drawables) {
                    if (drawable != null && mDrawablePressAlpha >= 0 && mDrawablePressAlpha <= 255) {
                        drawable.setAlpha(mDrawablePressAlpha);
                    }
                }
                if (mTouchDownAnimation != null) {
                    startAnimation(mTouchDownAnimation);
                }
            } else {
                if (mTextPressAlpha >= 0 && mTextPressAlpha <= 255) {
                    setTextColor(Color.argb(255, red, green, blue));
                    setHintTextColor(Color.argb(255, redHint, greenHint, blueHint));
                }
                for (Drawable drawable : drawables) {
                    if (drawable != null && mDrawablePressAlpha >= 0 && mDrawablePressAlpha <= 255) {
                        drawable.setAlpha(255);
                    }
                }
                if (mTouchDownAnimation != null) {
                    startAnimation(mTouchUpAnimation);
                }
            }
        }
        super.setPressed(pressed);
        mLastPressed = pressed;
    }

    private Paint mRoundRectPaint;
    private RectF mRoundRect;

    private Rect mRect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawRoundRect) {
            if (mRoundRect == null) {
                mRoundRect = new RectF();
            }
            if (mRoundRectPaint == null) {
                mRoundRectPaint = new Paint();
                mRoundRectPaint.setAntiAlias(true);
                mRoundRectPaint.setDither(true);
            }
            if (mRoundRectColor != 0x00000000) {
                if (mDrawRoundRectIncludePadding) {
                    mRoundRect.left = getPaddingLeft();
                    mRoundRect.top = getPaddingTop();
                    mRoundRect.right = getWidth() - getPaddingRight();
                    mRoundRect.bottom = getHeight() - getPaddingBottom();
                } else {
                    mRoundRect.left = 0;
                    mRoundRect.top = 0;
                    mRoundRect.right = getWidth();
                    mRoundRect.bottom = getHeight();
                }
                mRoundRectPaint.setStyle(Style.FILL);
                mRoundRectPaint.setColor(mRoundRectColor);
                canvas.drawRoundRect(mRoundRect, mRoundRectRadius, mRoundRectRadius, mRoundRectPaint);
            }
        }
        super.onDraw(canvas);
        if (mLineDrawable != null) {
            int lineCount = getLineCount();
            for (int i = 0; i < lineCount; i++) {
                getLineBounds(i, mRect);
                float top = mRect.bottom - mLineDrawableHeight + mLineDrawableOffset;
                float bottom = mRect.bottom + mLineDrawableOffset;
                mLineDrawable.setBounds(mRect.left, (int) top, mRect.right, (int) bottom);
                mLineDrawable.draw(canvas);
            }
        }
    }

    private View mClearView;

    public void setClearView(View view) {
        mClearView = view;
        mClearView.setVisibility(TextUtils.isEmpty(getText()) ? View.GONE : View.VISIBLE);
        mClearView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setText(null);
            }
        });
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mClearView != null) {
            mClearView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        }
    }

}
