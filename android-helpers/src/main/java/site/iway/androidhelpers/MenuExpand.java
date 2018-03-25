package site.iway.androidhelpers;

import android.app.Activity;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@Deprecated
public class MenuExpand extends MenuAnimated {

    public MenuExpand(Activity parent, int backgroundColor) {
        super(parent, backgroundColor);
    }

    @Override
    protected void processContentViewLayoutParams(LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    }

    @Override
    protected void playShowAnimation(View contentView) {
        ScaleAnimation expendAnimi = new ScaleAnimation(
                0, 1, 0, 1,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        expendAnimi.setDuration(getAnimationTimeout());
        contentView.startAnimation(expendAnimi);
    }

    @Override
    protected void playHideAnimation(View contentView) {
        ScaleAnimation reduceAnimi = new ScaleAnimation(
                1, 0, 1, 0,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        reduceAnimi.setDuration(getAnimationTimeout());
        contentView.startAnimation(reduceAnimi);
    }

}
