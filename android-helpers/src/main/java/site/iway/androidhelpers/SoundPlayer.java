package site.iway.androidhelpers;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

@Deprecated
public final class SoundPlayer {

    private static MediaPlayer mMediaPlayer = null;
    private static int mInstanceCount = 0;

    public static synchronized void play(Context context, String soundUri) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, Uri.parse(soundUri));
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }
        mInstanceCount++;
    }

    public static synchronized void play(Context context, int resId) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, resId);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }
        mInstanceCount++;
    }

    public static synchronized void stop() {
        mInstanceCount--;
        if (mInstanceCount <= 0) {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mInstanceCount = 0;
        }
    }

}
