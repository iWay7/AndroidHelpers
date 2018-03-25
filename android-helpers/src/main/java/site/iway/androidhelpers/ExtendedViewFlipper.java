package site.iway.androidhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class ExtendedViewFlipper extends ViewFlipper {

    public ExtendedViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedViewFlipper(Context context) {
        super(context);
    }

    @Override
    public void setDisplayedChild(int whichChild) {
        int displayedChild = getDisplayedChild();
        if (displayedChild == whichChild)
            return;
        super.setDisplayedChild(whichChild);
    }

}
