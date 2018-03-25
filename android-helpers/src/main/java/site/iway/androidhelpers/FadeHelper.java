package site.iway.androidhelpers;

import android.view.View;
import android.view.animation.AlphaAnimation;

public class FadeHelper {

    public static void fadeToVisible(View view, int startOffset, int duration) {
        int visibility = view.getVisibility();
        if (visibility != View.VISIBLE) {
            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setDuration(duration);
            anim.setStartOffset(startOffset);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(anim);
        }
    }

    public static void fadeToInvisible(View view, int startOffset, int duration) {
        int visibility = view.getVisibility();
        if (visibility != View.INVISIBLE) {
            AlphaAnimation anim = new AlphaAnimation(1, 0);
            anim.setStartOffset(startOffset);
            anim.setDuration(duration);
            view.setVisibility(View.INVISIBLE);
            view.startAnimation(anim);
        }
    }

}
