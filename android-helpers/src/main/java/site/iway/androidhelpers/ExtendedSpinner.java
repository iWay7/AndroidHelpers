package site.iway.androidhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class ExtendedSpinner extends Spinner {

    public ExtendedSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public ExtendedSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtendedSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedSpinner(Context context, int mode) {
        super(context, mode);
    }

    public ExtendedSpinner(Context context) {
        super(context);
    }

}
