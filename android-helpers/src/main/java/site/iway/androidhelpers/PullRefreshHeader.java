package site.iway.androidhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PullRefreshHeader extends FrameLayout {

    public PullRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInitViews(context);
    }

    public PullRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInitViews(context);
    }

    public PullRefreshHeader(Context context) {
        super(context);
        onInitViews(context);
    }

    private LinearLayout mMainView;
    private ImageView mImageView;
    private TextView mTextView;

    public void onInitViews(Context context) {
        inflate(context, R.layout.pull_refresh_header, this);
        mMainView = (LinearLayout) findViewById(R.id.mainView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText(R.string.pull_to_refresh);
    }

    public int getMainHeight() {
        return mMainView.getHeight();
    }

    private int mLastPullOffset;
    private boolean mRefreshing;
    private UITimer mRotatePlayer;

    public void updateContent(int pullOffset) {
        if (mRefreshing) {
            if (mRotatePlayer == null) {
                mRotatePlayer = new UITimer(100) {

                    @Override
                    public void doOnUIThread() {
                        float rotation = mImageView.getRotation();
                        rotation += 30;
                        mImageView.setRotation(rotation);
                    }

                };
                mRotatePlayer.start(false);
            }

            mTextView.setText(R.string.refreshing);
        } else {
            if (mRotatePlayer != null) {
                mRotatePlayer.stop();
                mRotatePlayer = null;
            }

            mImageView.setRotation(-pullOffset / 10 * 30);

            int mainHeight = getMainHeight();
            if (pullOffset > mainHeight) {
                mTextView.setText(R.string.release_can_refresh);
            } else {
                mTextView.setText(R.string.pull_to_refresh);
            }

        }
        mLastPullOffset = pullOffset;
    }

    public void setRefreshing(boolean refreshing) {
        mRefreshing = refreshing;
        updateContent(mLastPullOffset);
    }

}
