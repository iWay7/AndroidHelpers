package site.iway.androidhelpers;

import android.app.Activity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@Deprecated
public class MenuAlphaScale extends MenuAnimated {

    public MenuAlphaScale(Activity parent, int backgroundColor) {
        super(parent, backgroundColor);
    }

    @Override
    protected void processContentViewLayoutParams(LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    }

    private float mScaleStart = 0.5f;

    public float getScaleStart() {
        return mScaleStart;
    }

    public void setScaleStart(float start) {
        mScaleStart = start;
    }

    private float mScalePivotX = 0.5f;
    private float mScalePivotY = 0.5f;

    public float getScalePivotX() {
        return mScalePivotX;
    }

    public void setScalePivotX(float scalePivotX) {
        mScalePivotX = scalePivotX;
    }

    public float getScalePivotY() {
        return mScalePivotY;
    }

    public void setScalePivotY(float scalePivotY) {
        mScalePivotY = scalePivotY;
    }

    @Override
    protected void playShowAnimation(View contentView) {
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(getAnimationTimeout());
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                mScaleStart, 1,
                mScaleStart, 1,
                ScaleAnimation.RELATIVE_TO_SELF, mScalePivotX,
                ScaleAnimation.RELATIVE_TO_SELF, mScalePivotY);
        scaleAnimation.setDuration(getAnimationTimeout());
        set.addAnimation(alphaAnimation);
        set.addAnimation(scaleAnimation);
        set.setInterpolator(new DecelerateInterpolator());
        contentView.startAnimation(set);
    }

    @Override
    protected void playHideAnimation(View contentView) {
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(getAnimationTimeout());
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1, mScaleStart,
                1, mScaleStart,
                ScaleAnimation.RELATIVE_TO_SELF, mScalePivotX,
                ScaleAnimation.RELATIVE_TO_SELF, mScalePivotY);
        scaleAnimation.setDuration(getAnimationTimeout());
        set.addAnimation(alphaAnimation);
        set.addAnimation(scaleAnimation);
        set.setInterpolator(new AccelerateInterpolator());
        contentView.startAnimation(set);
    }

}
