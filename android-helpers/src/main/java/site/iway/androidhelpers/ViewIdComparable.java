package site.iway.androidhelpers;

import android.view.View;

import site.iway.javahelpers.BooleanComparable;

public class ViewIdComparable implements BooleanComparable<View> {

    private int mId;

    public ViewIdComparable(int id) {
        mId = id;
    }

    @Override
    public boolean compareTo(View another) {
        return mId == another.getId();
    }

}
