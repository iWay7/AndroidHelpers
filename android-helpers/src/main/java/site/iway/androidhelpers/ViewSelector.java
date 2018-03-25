package site.iway.androidhelpers;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import site.iway.javahelpers.MathHelper;

public class ViewSelector {

    private ViewGroup mViewGroup;
    private Context mContext;
    private boolean mEnableTouchSelect;
    private boolean mMultipleSelect;
    private boolean mContainSplitter;

    public ViewSelector(ViewGroup viewGroup, boolean enableTouchSelect, boolean multipleSelectMode, boolean containSplitter) {
        mViewGroup = viewGroup;
        mContext = viewGroup.getContext();
        mEnableTouchSelect = enableTouchSelect;
        mMultipleSelect = multipleSelectMode;
        mContainSplitter = containSplitter;
    }

    public int[] getSelectIndices() {
        ArrayList<Integer> selectedIndices = new ArrayList<>();
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; i += mContainSplitter ? 2 : 1) {
            View child = mViewGroup.getChildAt(i);
            boolean selected = child.isSelected();
            if (selected) {
                selectedIndices.add(i);
            }
        }
        int size = selectedIndices.size();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = selectedIndices.get(i);
        }
        return array;
    }

    private float mTouchDownX;
    private float mTouchDownY;

    public boolean onTouchEvent(MotionEvent event) {
        if (mEnableTouchSelect) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchDownX = event.getRawX();
                    mTouchDownY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = event.getRawX() - mTouchDownX;
                    float dy = event.getRawY() - mTouchDownY;
                    double distance = MathHelper.distance(dx, dy);
                    double range = UnitHelper.dipToPx(mContext, 5);
                    if (distance < range) {
                        int childCount = mViewGroup.getChildCount();
                        for (int childIndex = childCount - 1; childIndex >= 0; childIndex--) {
                            View child = mViewGroup.getChildAt(childIndex);
                            boolean hitView = ViewHelper.isMotionEventInView(event, child);
                            boolean hittable = mContainSplitter ? childIndex % 2 == 0 : true;
                            if (hitView && hittable) {
                                boolean selected = !child.isSelected();
                                if (mMultipleSelect) {
                                    child.setSelected(selected);
                                } else {
                                    for (int i = 0; i < childCount; i += mContainSplitter ? 2 : 1) {
                                        if (i == childIndex)
                                            mViewGroup.getChildAt(i).setSelected(selected);
                                        else
                                            mViewGroup.getChildAt(i).setSelected(false);
                                    }
                                }
                                break;
                            }
                        }
                    }
                    break;
            }
            return true;
        }
        return false;
    }

}
