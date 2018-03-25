package site.iway.androidhelpers;

import android.app.Activity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@Deprecated
public class MenuSlideUp extends MenuAnimated {

    public MenuSlideUp(Activity parent, int backgroundColor) {
        super(parent, backgroundColor);
    }

    @Override
    protected void processContentViewLayoutParams(LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    }

    @Override
    protected void playShowAnimation(View contentView) {
        TranslateAnimation upAnimi = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 1,
                TranslateAnimation.RELATIVE_TO_SELF, 0);
        upAnimi.setInterpolator(new DecelerateInterpolator());
        upAnimi.setDuration(getAnimationTimeout());
        contentView.startAnimation(upAnimi);
    }

    @Override
    protected void playHideAnimation(View contentView) {
        TranslateAnimation downAnimi = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 1);
        downAnimi.setInterpolator(new AccelerateInterpolator());
        downAnimi.setDuration(getAnimationTimeout());
        contentView.startAnimation(downAnimi);
    }

}
