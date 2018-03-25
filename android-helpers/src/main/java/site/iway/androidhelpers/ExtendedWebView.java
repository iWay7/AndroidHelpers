package site.iway.androidhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ExtendedWebView extends WebView {

    public ExtendedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedWebView(Context context) {
        super(context);
    }

}
