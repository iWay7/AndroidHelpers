package site.iway.androidhelpers;

import android.widget.BaseAdapter;

@Deprecated
public abstract class StringTypeAdapter extends BaseAdapter {

    private int mStringTypeCount;
    private String[] mStringTypes;

    public abstract int getViewStringTypeCount();

    @Override
    public int getViewTypeCount() {
        int count = getViewStringTypeCount();
        mStringTypes = new String[count];
        mStringTypeCount = 0;
        return count;
    }

    public abstract String getViewStringType(int position);

    @Override
    public int getItemViewType(int position) {
        String type = getViewStringType(position);
        for (int i = 0; i < mStringTypeCount; i++) {
            if (mStringTypes[i].compareTo(type) == 0) {
                return i;
            }
        }
        mStringTypes[mStringTypeCount] = type;
        mStringTypeCount++;
        return mStringTypeCount - 1;
    }

}
