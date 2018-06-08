package site.iway.androidhelpers;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import site.iway.javahelpers.SecurityHelper;
import site.iway.javahelpers.StringHelper;

class BitmapLoaderImpl extends BitmapLoader {

    private Context mContext;

    private File mFile;
    private InputStream mInputStream;
    private Options mOptions;
    private int mTargetDataSize;
    private File mCacheFile;
    private OutputStream mCacheStream;
    private int mDataLength;
    private BitmapURLStreamer mURLStreamer;

    BitmapLoaderImpl(Context context) {
        mContext = context;
    }

    private void prepareResourcesForURL(String urlPath) throws Exception {
        byte[] urlPathData = urlPath.getBytes();
        byte[] urlPathMD5 = SecurityHelper.md5(urlPathData);
        String cacheFileName = StringHelper.hex(urlPathMD5);
        String cacheFilePath = BitmapCache.DOWNLOAD_DIRECTORY + cacheFileName;
        mCacheFile = new File(cacheFilePath);
        if (mCacheFile.exists() && mCacheFile.length() > 0) {
            prepareResourcesForFile(cacheFilePath);
        } else {
            mURLStreamer = BitmapCache.URL_STREAMER_CLASS.newInstance();
            mURLStreamer.initialize(urlPath);
            mInputStream = mURLStreamer.getInputStream();
            mDataLength = mURLStreamer.getDataLength();
            mCacheStream = new FileOutputStream(mCacheFile);
        }
    }

    private void prepareResourcesForFile(String filePath) throws Exception {
        mOptions = new Options();
        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, mOptions);
        if (mOptions.outWidth <= 0 || mOptions.outHeight <= 0) {
            throw new RuntimeException("Try decode size of " + filePath + " failed.");
        } else {
            int width = mOptions.outWidth;
            int height = mOptions.outHeight;
            int scale = 1;
            while (width * height * 4 > BitmapCache.MAX_RAM_USAGE_OF_SINGLE_BITMAP) {
                scale *= 2;
                width /= 2;
                height /= 2;
            }
            mOptions.inSampleSize = scale;
            mOptions.inJustDecodeBounds = false;
            mTargetDataSize = width * height * 4;
        }
        mFile = new File(filePath);
        mDataLength = (int) mFile.length();
        mInputStream = new FileInputStream(mFile);
    }

    private void prepareResourcesForAsset(String assetPath) throws Exception {
        mInputStream = mContext.getAssets().open(assetPath);
    }

    private void prepareResourcesForResource(int resourceId) throws Exception {
        mInputStream = mContext.getResources().openRawResource(resourceId);
    }

    @Override
    protected void prepareResources(BitmapInfo bitmapInfo) throws Exception {
        BitmapSource bitmapSource = bitmapInfo.getSource();
        if (bitmapSource instanceof BitmapSourceURL) {
            BitmapSourceURL urlSource = (BitmapSourceURL) bitmapSource;
            prepareResourcesForURL(urlSource.urlPath);
        }
        if (bitmapSource instanceof BitmapSourceFile) {
            BitmapSourceFile fileSource = (BitmapSourceFile) bitmapSource;
            prepareResourcesForFile(fileSource.filePath);
        }
        if (bitmapSource instanceof BitmapSourceAsset) {
            BitmapSourceAsset assetSource = (BitmapSourceAsset) bitmapSource;
            prepareResourcesForAsset(assetSource.assetPath);
        }
        if (bitmapSource instanceof BitmapSourceResource) {
            BitmapSourceResource resourceSource = (BitmapSourceResource) bitmapSource;
            prepareResourcesForResource(resourceSource.resourceId);
        }
    }

    @Override
    protected int getDataLength() {
        return mDataLength;
    }

    @Override
    protected InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    protected Options getDecodeOptions() {
        return mOptions;
    }

    @Override
    protected int getTargetDataSize() {
        return mTargetDataSize;
    }

    @Override
    protected boolean isDataSkipEnabled() {
        return mCacheStream == null;
    }

    @Override
    protected void onDataRead(byte[] buffer, int offset, int length) throws IOException {
        if (mCacheStream != null) {
            mCacheStream.write(buffer, offset, length);
        }
    }

    @Override
    protected void releaseResources(boolean errorOccurred) {
        if (mCacheStream != null) {
            try {
                mCacheStream.close();
            } catch (Exception e) {
                // nothing
            }
            mCacheStream = null;
        }
        mFile = null;
        if (mCacheFile != null) {
            if (errorOccurred) {
                if (!mCacheFile.delete()) {
                    mCacheFile.deleteOnExit();
                }
            }
            mCacheFile = null;
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (Exception e) {
                // nothing
            }
            mInputStream = null;
        }
        if (mURLStreamer != null) {
            mURLStreamer.release();
            mURLStreamer = null;
        }
        mOptions = null;
        mTargetDataSize = 0;
        mDataLength = 0;
    }

}
