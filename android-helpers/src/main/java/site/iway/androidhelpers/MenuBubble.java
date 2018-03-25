package site.iway.androidhelpers;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@Deprecated
public class MenuBubble extends MenuAlphaScale {

    public MenuBubble(Activity parent, int backgroundColor) {
        super(parent, backgroundColor);
    }

    @Override
    protected void processContentViewLayoutParams(LayoutParams layoutParams) {
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    }

    public void show(int x, int y) {
        View view = getContentView();
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        view.setLayoutParams(params);
        show();
    }

}
