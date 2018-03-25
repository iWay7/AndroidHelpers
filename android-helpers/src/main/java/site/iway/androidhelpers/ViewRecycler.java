package site.iway.androidhelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

public class ViewRecycler {

    private LayoutInflater mLayoutInflater;
    private LinkedList<View> mViews;
    private int mLayoutResId;

    public ViewRecycler(Context context, int layoutResId) {
        String serviceName = Context.LAYOUT_INFLATER_SERVICE;
        Object service = context.getSystemService(serviceName);
        mLayoutInflater = (LayoutInflater) service;
        mViews = new LinkedList<>();
        mLayoutResId = layoutResId;
    }

    public void setChildCount(ViewGroup viewGroup, int childCount) {
        while (viewGroup.getChildCount() > childCount) {
            View childView = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
            viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
            mViews.add(childView);
        }
        while (viewGroup.getChildCount() < childCount) {
            View childView;
            if (mViews.isEmpty())
                childView = mLayoutInflater.inflate(mLayoutResId, viewGroup, false);
            else
                childView = mViews.removeFirst();
            viewGroup.addView(childView);
        }
    }

}
