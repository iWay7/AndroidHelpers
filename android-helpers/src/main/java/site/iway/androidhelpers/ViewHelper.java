package site.iway.androidhelpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import site.iway.javahelpers.BooleanComparable;

public class ViewHelper {

    public static View findChildViewByComparable(ViewGroup viewGroup, BooleanComparable<View> comparable) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (comparable.compareTo(childView)) {
                return childView;
            }
        }
        return null;
    }

    public static View findChildViewById(ViewGroup viewGroup, int id) {
        ViewIdComparable viewIdComparable = new ViewIdComparable(id);
        return findChildViewByComparable(viewGroup, viewIdComparable);
    }

    public static View findChildViewByTag(ViewGroup viewGroup, Object tag) {
        ViewTagComparable viewTagComparable = new ViewTagComparable(tag);
        return findChildViewByComparable(viewGroup, viewTagComparable);
    }

    public static View findViewByComparable(ViewGroup viewGroup, BooleanComparable<View> comparable) {
        int viewCount = viewGroup.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View childView = viewGroup.getChildAt(i);
            if (childView instanceof ViewGroup) {
                View result = findViewByComparable((ViewGroup) childView, comparable);
                if (result != null) {
                    return result;
                }
            } else {
                if (comparable.compareTo(childView)) {
                    return childView;
                }
            }
        }
        return null;
    }

    public static View findViewById(ViewGroup viewGroup, int id) {
        ViewIdComparable viewIdComparable = new ViewIdComparable(id);
        return findViewByComparable(viewGroup, viewIdComparable);
    }

    public static View findViewByTag(ViewGroup viewGroup, Object tag) {
        ViewTagComparable viewTagComparable = new ViewTagComparable(tag);
        return findViewByComparable(viewGroup, viewTagComparable);
    }

    public static boolean isMotionEventInView(MotionEvent ev, View view) {
        int[] locationOnScreen = new int[2];
        view.getLocationOnScreen(locationOnScreen);
        int x = locationOnScreen[0];
        int y = locationOnScreen[1];
        boolean xInView = ev.getRawX() >= x && ev.getRawX() <= x + view.getWidth();
        boolean yInView = ev.getRawY() >= y && ev.getRawY() <= y + view.getHeight();
        return xInView && yInView;
    }

    public static boolean cancelMotionEventInView(MotionEvent ev, View view) {
        int action = ev.getAction();
        ev.setAction(MotionEvent.ACTION_CANCEL);
        boolean result = view.dispatchTouchEvent(ev);
        ev.setAction(action);
        return result;
    }

    public static boolean isChildView(ViewGroup viewGroup, View view) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (view == viewGroup.getChildAt(i)) {
                return true;
            }
        }
        return false;
    }

    public static void traversalViews(ViewGroup viewGroup, ViewProcessor viewProcessor) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            viewProcessor.process(child);
            if (child instanceof ViewGroup) {
                traversalViews((ViewGroup) child, viewProcessor);
            }
        }
    }

    public static void setOnClickListenerToAll(ViewGroup viewGroup, OnClickListener listener) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup)
                setOnClickListenerToAll((ViewGroup) child, listener);
            else
                child.setOnClickListener(listener);
        }
    }

    public static Bitmap getViewSnapshot(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static void moveView(View view, int horizontalPixels, int verticalPixels) {
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        lp.leftMargin += horizontalPixels;
        lp.rightMargin -= horizontalPixels;
        lp.topMargin += verticalPixels;
        lp.bottomMargin -= verticalPixels;
        view.setLayoutParams(lp);
    }

    private static void switchImageAnimatedInternal(final ImageView view, final Object obj, int timeout) {
        AlphaAnimation animation = new AlphaAnimation(1, 0);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(1);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // nothing
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (obj == null)
                    view.setImageBitmap(null);
                else if (obj instanceof Bitmap)
                    view.setImageBitmap((Bitmap) obj);
                else if (obj instanceof Integer)
                    view.setImageResource((Integer) obj);
                else if (obj instanceof Drawable)
                    view.setImageDrawable((Drawable) obj);
            }

        });
        animation.setDuration(timeout);
        view.startAnimation(animation);
    }

    public static void switchImageAnimated(ImageView view, Bitmap bmp, int timeout) {
        switchImageAnimatedInternal(view, bmp, timeout);
    }

    public static void switchImageAnimated(ImageView view, Integer resId, int timeout) {
        switchImageAnimatedInternal(view, resId, timeout);
    }

    public static void switchImageAnimated(ImageView view, Drawable drawable, int timeout) {
        switchImageAnimatedInternal(view, drawable, timeout);
    }

}
