package site.iway.androidhelpers;

import android.view.View;

import site.iway.javahelpers.BooleanComparable;

public class ViewTagComparable implements BooleanComparable<View> {

    private Object mTag;

    public ViewTagComparable(Object tag) {
        mTag = tag;
    }

    @Override
    public boolean compareTo(View another) {
        Object anotherTag = another.getTag();
        if (mTag == null) {
            return anotherTag == null;
        } else {
            return mTag.equals(anotherTag);
        }
    }

}
