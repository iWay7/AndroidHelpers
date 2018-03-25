package site.iway.androidhelpers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ExtendedImageView extends ImageView {

    public ExtendedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
    }

    public ExtendedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public ExtendedImageView(Context context) {
        super(context);
        resolveAttrs(context, null);
    }

    private int mDrawablePressAlpha;

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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedImageView);
        setTouchDownAnimation(a.getResourceId(R.styleable.ExtendedImageView_touchDownAnimation, 0));
        setTouchUpAnimation(a.getResourceId(R.styleable.ExtendedImageView_touchUpAnimation, 0));
        mDrawablePressAlpha = a.getInt(R.styleable.ExtendedImageView_drawablePressAlpha, -1);
        mFilterClickSpan = a.getInt(R.styleable.ExtendedImageView_filterClickSpan, 0);
        a.recycle();
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

    private boolean mLastPressed;

    @Override
    public void setPressed(boolean pressed) {
        if (mLastPressed != pressed) {
            Drawable drawable = getDrawable();
            if (pressed) {
                if (drawable != null && mDrawablePressAlpha >= 0 && mDrawablePressAlpha <= 255) {
                    drawable.setAlpha(mDrawablePressAlpha);
                }
                if (mTouchDownAnimation != null) {
                    startAnimation(mTouchDownAnimation);
                }
            } else {
                if (drawable != null && mDrawablePressAlpha >= 0 && mDrawablePressAlpha <= 255) {
                    drawable.setAlpha(255);
                }
                if (mTouchDownAnimation != null) {
                    startAnimation(mTouchUpAnimation);
                }
            }
        }
        super.setPressed(pressed);
        mLastPressed = pressed;
    }

}