package site.iway.androidhelpers;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class ExtendedScroller extends Scroller {

    public ExtendedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    public ExtendedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public ExtendedScroller(Context context) {
        super(context);
    }

    private int mCustomizedDuration = -1;

    public void customizeDuration(int customizedDuration) {
        mCustomizedDuration = customizedDuration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if (mCustomizedDuration > 0)
            super.startScroll(startX, startY, dx, dy, mCustomizedDuration);
        else
            super.startScroll(startX, startY, dx, dy, duration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        if (mCustomizedDuration > 0)
            super.startScroll(startX, startY, dx, dy, mCustomizedDuration);
        else
            super.startScroll(startX, startY, dx, dy);
    }

}