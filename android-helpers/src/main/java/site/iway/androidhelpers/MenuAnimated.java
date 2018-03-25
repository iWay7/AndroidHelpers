package site.iway.androidhelpers;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@Deprecated
public abstract class MenuAnimated {

    public interface OnBackPressedListener {
        public void onBackPressed(MenuAnimated menu);
    }

    public interface OnMenuBgClickListener {
        public void onMenuBgClick(MenuAnimated menu);
    }

    public interface OnMenuHiddenListener {
        public void onMenuHidden(MenuAnimated menu, boolean animated);
    }

    public interface OnMenuShownListener {
        public void onMenuShown(MenuAnimated menu, boolean animated);
    }

    private Activity mActivity;

    private RelativeLayout mRootView;
    private ImageView mBackgroundView;
    private View mContentView;

    private boolean mIsShown = false;
    private int mAnimationTimeout = 300;

    private boolean mWillHideOnBgClicked = true;
    private boolean mWillHideOnBackPressed = true;

    private OnMenuBgClickListener mOnMenuBgClickListener;
    private OnBackPressedListener mOnBackPressedListener;
    private OnMenuShownListener mOnMenuShownListener;
    private OnMenuHiddenListener mOnMenuHiddenListener;

    public MenuAnimated(Activity parent, int backgroundColor) {
        mActivity = parent;
        mRootView = new RelativeLayout(parent);
        mBackgroundView = new ImageView(parent);
        mBackgroundView.setScaleType(ScaleType.CENTER_CROP);
        mBackgroundView.setImageDrawable(new ColorDrawable(backgroundColor));
        mBackgroundView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuBgClickListener != null) {
                    mOnMenuBgClickListener.onMenuBgClick(MenuAnimated.this);
                }
                if (mWillHideOnBgClicked) {
                    hide();
                }
            }
        });
        int width = LayoutParams.MATCH_PARENT;
        int height = LayoutParams.MATCH_PARENT;
        LayoutParams layoutParams = new LayoutParams(width, height);
        mRootView.addView(mBackgroundView, layoutParams);
        mRootView.setVisibility(View.GONE);
        mRootView.setOnKeyListener(new OnKeyListener() {
            private boolean mHasKeyDowned;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mIsShown && keyCode == KeyEvent.KEYCODE_BACK) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            mHasKeyDowned = true;
                            return true;
                        case KeyEvent.ACTION_UP:
                            if (mHasKeyDowned) {
                                if (mOnBackPressedListener != null) {
                                    mOnBackPressedListener.onBackPressed(MenuAnimated.this);
                                }
                                if (mWillHideOnBackPressed) {
                                    hide();
                                }
                                mHasKeyDowned = false;
                            }
                            return true;
                        default:
                            return true;
                    }
                }
                return false;
            }
        });
        mRootView.setFocusable(true);
        mRootView.setFocusableInTouchMode(true);
        mActivity.addContentView(mRootView, layoutParams);
    }

    public Activity getActivity() {
        return mActivity;
    }

    public int getAnimationTimeout() {
        return mAnimationTimeout;
    }

    public void setAnimationTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must >= 0");
        }
        mAnimationTimeout = timeout;
    }

    public boolean getWillHideOnBgClicked() {
        return mWillHideOnBgClicked;
    }

    public void setWillHideOnBgClicked(boolean value) {
        mWillHideOnBgClicked = value;
    }

    public boolean getWillHideOnBackPressed() {
        return mWillHideOnBackPressed;
    }

    public void setWillHideOnBackPressed(boolean value) {
        mWillHideOnBackPressed = value;
        mRootView.setFocusable(value);
        mRootView.setFocusableInTouchMode(value);
    }

    public OnMenuBgClickListener getOnMenuBgClickListener() {
        return mOnMenuBgClickListener;
    }

    public void setOnMenuBgClickListener(OnMenuBgClickListener listener) {
        mOnMenuBgClickListener = listener;
    }

    public OnBackPressedListener getOnBackPressedListener() {
        return mOnBackPressedListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    public OnMenuShownListener getOnMenuShownListener() {
        return mOnMenuShownListener;
    }

    public void setOnMenuShownListener(OnMenuShownListener listener) {
        mOnMenuShownListener = listener;
    }

    public OnMenuHiddenListener getOnMenuHiddenListener() {
        return mOnMenuHiddenListener;
    }

    public void setOnMenuHiddenListener(OnMenuHiddenListener listener) {
        mOnMenuHiddenListener = listener;
    }

    public View getContentView() {
        return mContentView;
    }

    public View findViewById(int id) {
        return mContentView == null ? null : mContentView.findViewById(id);
    }

    public void setLayoutParams(LayoutParams layoutParams) {
        if (mContentView != null) {
            mContentView.setLayoutParams(layoutParams);
        }
    }

    protected abstract void processContentViewLayoutParams(LayoutParams layoutParams);

    private void setOnClickListenerToAll(ViewGroup viewGroup, OnClickListener listener) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup)
                setOnClickListenerToAll((ViewGroup) child, listener);
            else
                child.setOnClickListener(listener);
        }
    }

    public void setContentView(View view, OnClickListener listener) {
        if (mContentView != null) {
            mRootView.removeView(mContentView);
        }

        mContentView = view;

        if (listener != null) {
            if (mContentView instanceof ViewGroup)
                setOnClickListenerToAll((ViewGroup) mContentView, listener);
            else
                mContentView.setOnClickListener(listener);
        }

        LayoutParams params = (LayoutParams) mContentView.getLayoutParams();
        processContentViewLayoutParams(params);
        mRootView.addView(mContentView);
    }

    public void setContentView(int layoutId, OnClickListener listener) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View contentView = inflater.inflate(layoutId, mRootView, false);
        setContentView(contentView, listener);
    }

    public boolean isShown() {
        return mIsShown;
    }

    protected abstract void playShowAnimation(View contentView);

    public void show() {
        if (mIsShown) {
            return;
        }
        mIsShown = true;

        mRootView.setVisibility(View.VISIBLE);
        if (mWillHideOnBackPressed) {
            mRootView.requestFocus();
        }
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(mAnimationTimeout);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mOnMenuShownListener != null) {
                    mOnMenuShownListener.onMenuShown(MenuAnimated.this, true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing
            }

        });
        mBackgroundView.startAnimation(animation);

        playShowAnimation(mContentView);
    }

    public void showImediately() {
        if (mIsShown) {
            return;
        }
        mIsShown = true;

        mRootView.setVisibility(View.VISIBLE);
        if (mWillHideOnBackPressed) {
            mRootView.requestFocus();
        }
        if (mOnMenuShownListener != null) {
            mOnMenuShownListener.onMenuShown(this, false);
        }
    }

    public boolean isHidden() {
        return !mIsShown;
    }

    protected abstract void playHideAnimation(View contentView);

    public void hide() {
        if (!mIsShown) {
            return;
        }
        mIsShown = false;

        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setDuration(mAnimationTimeout);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRootView.setVisibility(View.GONE);
                if (mOnMenuHiddenListener != null) {
                    mOnMenuHiddenListener.onMenuHidden(MenuAnimated.this, true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing
            }

        });
        mBackgroundView.startAnimation(animation);

        playHideAnimation(mContentView);
    }

    public void hideImediately() {
        if (!mIsShown) {
            return;
        }
        mIsShown = false;

        mRootView.setVisibility(View.GONE);
        if (mOnMenuHiddenListener != null) {
            mOnMenuHiddenListener.onMenuHidden(this, false);
        }
    }

}
