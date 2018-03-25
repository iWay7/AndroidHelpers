package site.iway.androidhelpers;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import site.iway.javahelpers.BooleanComparable;

public abstract class PhotoAlbum extends ViewPager {

    public PhotoAlbum(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoAlbum(Context context) {
        super(context);
    }

    protected abstract boolean isImageViewerInPosition(ImageViewer mImageViewer, int position);

    private BooleanComparable<View> mViewComparable = new BooleanComparable<View>() {
        @Override
        public boolean compareTo(View another) {
            if (!(another instanceof ImageViewer))
                return false;
            else if (isImageViewerInPosition((ImageViewer) another, getCurrentItem()))
                return true;
            return false;
        }
    };

    private BooleanComparable<View> mViewComparableLeft = new BooleanComparable<View>() {
        @Override
        public boolean compareTo(View another) {
            if (!(another instanceof ImageViewer))
                return false;
            else if (isImageViewerInPosition((ImageViewer) another, getCurrentItem() - 1))
                return true;
            return false;
        }
    };

    private BooleanComparable<View> mViewComparableRight = new BooleanComparable<View>() {
        @Override
        public boolean compareTo(View another) {
            if (!(another instanceof ImageViewer))
                return false;
            else if (isImageViewerInPosition((ImageViewer) another, getCurrentItem() + 1))
                return true;
            return false;
        }
    };

    private ImageViewer mImageViewer;
    private float mTouchDownX;
    private boolean mShouldHandleSelfDefined;
    private boolean mShouldHandleSelf;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private void resetLeftOrRightImageViewer() {
        ImageViewer imageViewer;

        imageViewer = (ImageViewer) ViewHelper.findViewByComparable(this, mViewComparableLeft);
        if (imageViewer != null) {
            imageViewer.resetImageState();
        }
        imageViewer = (ImageViewer) ViewHelper.findViewByComparable(this, mViewComparableRight);
        if (imageViewer != null) {
            imageViewer.resetImageState();
        }
    }

    private View findChildView(View view) {
        if (view.getParent() == this) {
            return view;
        }
        return findChildView((View) view.getParent());
    }

    private int findImageViewerLeft(ImageViewer imageViewer) {
        View view = findChildView(imageViewer);
        return view.getLeft();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mImageViewer = (ImageViewer) ViewHelper.findViewByComparable(this, mViewComparable);
                mTouchDownX = ev.getX();
                if (mImageViewer == null) {
                    mShouldHandleSelfDefined = true;
                    mShouldHandleSelf = true;
                } else if (getScrollX() - findImageViewerLeft(mImageViewer) != 0) {
                    mShouldHandleSelfDefined = true;
                    mShouldHandleSelf = true;
                } else {
                    mShouldHandleSelfDefined = false;
                    mShouldHandleSelf = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mShouldHandleSelfDefined) {
                    if (ev.getPointerCount() > 1) {
                        mShouldHandleSelfDefined = true;
                        mShouldHandleSelf = false;
                    } else {
                        float xChange = ev.getX() - mTouchDownX;
                        if (Math.abs(xChange) >= 5) {
                            if (xChange > 0 && mImageViewer.getBitmap() != null && mImageViewer.getImageLeftPadding() < -0.5) {
                                mShouldHandleSelfDefined = true;
                                mShouldHandleSelf = false;
                            }
                            if (xChange < 0 && mImageViewer.getBitmap() != null && mImageViewer.getImageRightPadding() < -0.5) {
                                mShouldHandleSelfDefined = true;
                                mShouldHandleSelf = false;
                            }

                            if (!mShouldHandleSelfDefined) {
                                mShouldHandleSelfDefined = true;
                                mShouldHandleSelf = true;
                                resetLeftOrRightImageViewer();
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mShouldHandleSelfDefined) {
                    mShouldHandleSelfDefined = true;
                    mShouldHandleSelf = true;
                }
                break;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mImageViewer != null)
                    mImageViewer.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mShouldHandleSelfDefined && !mShouldHandleSelf)
                    mImageViewer.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
                if (mImageViewer != null) {
                    mImageViewer.onTouchEvent(ev);
                    mImageViewer = null;
                }
                break;
        }

        switch (ev.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                super.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEventCompat.ACTION_POINTER_DOWN:
            case MotionEventCompat.ACTION_POINTER_UP:
                if (mShouldHandleSelfDefined && mShouldHandleSelf)
                    super.onTouchEvent(ev);
                break;
        }

        return true;
    }

}
