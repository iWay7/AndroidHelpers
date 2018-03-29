package site.iway.androidhelpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;

abstract class BitmapLoader extends Thread {

    protected abstract void prepareResources(BitmapInfo bitmapInfo) throws Exception;

    protected abstract int getDataLength();

    protected abstract InputStream getInputStream();

    protected Rect getDecodeOutPadding() {
        return null;
    }

    protected Options getDecodeOptions() {
        return null;
    }

    protected int getTargetDataSize() {
        return 0;
    }

    protected boolean isDataSkipEnabled() {
        return true;
    }

    protected void onDataRead(byte[] buffer, int offset, int length) throws IOException {
    }

    protected abstract void releaseResources(boolean errorOccurred);

    private BitmapInfo mWorkingItem;

    public void run() {
        while (true) {
            mWorkingItem = BitmapInfoManager.getReadyItem(true);

            if (mWorkingItem == null) {
                BitmapInfoManager.waitForBitmapInfoAdded();
                continue;
            }

            try {
                prepareResources(mWorkingItem);

                InputStream inputStreamWrapper = new InputStream() {

                    private InputStream mInputStream = getInputStream();
                    private int mReadCount = 0;
                    private int mTotalCount = getDataLength();

                    private void updateProgress() {
                        if (mTotalCount > 0) {
                            int newProgress = mReadCount * 100 / mTotalCount;
                            mWorkingItem.updateProgress(newProgress);
                        }
                    }

                    @Override
                    public int available() throws IOException {
                        return mInputStream.available();
                    }

                    @Override
                    public void close() throws IOException {
                        mInputStream.close();
                    }

                    @Override
                    public void mark(int readLimit) {
                        mInputStream.mark(readLimit);
                    }

                    @Override
                    public boolean markSupported() {
                        return mInputStream.markSupported();
                    }

                    @Override
                    public synchronized void reset() throws IOException {
                        mInputStream.reset();
                    }

                    @Override
                    public long skip(long byteCount) throws IOException {
                        if (isDataSkipEnabled()) {
                            return mInputStream.skip(byteCount);
                        } else {
                            return 0;
                        }
                    }

                    @Override
                    public int read() throws IOException {
                        int n = mInputStream.read();
                        if (n >= 0) {
                            byte[] buf = {(byte) n};
                            onDataRead(buf, 0, 1);
                            mReadCount += 1;
                            updateProgress();
                        }
                        return n;
                    }

                    @Override
                    public int read(byte[] buffer) throws IOException {
                        int currentReadCount = mInputStream.read(buffer);
                        if (currentReadCount > 0) {
                            onDataRead(buffer, 0, currentReadCount);
                            mReadCount += currentReadCount;
                            updateProgress();
                        }
                        return currentReadCount;
                    }

                    @Override
                    public int read(byte[] buffer, int offset, int length) throws IOException {
                        int currentReadCount = mInputStream.read(buffer, offset, length);
                        if (currentReadCount > 0) {
                            onDataRead(buffer, offset, currentReadCount);
                            mReadCount += currentReadCount;
                            updateProgress();
                        }
                        return currentReadCount;
                    }

                };
                Rect outPadding = getDecodeOutPadding();
                Options options = getDecodeOptions();
                BitmapInfoManager.optimize(getTargetDataSize());
                mWorkingItem.updateProgress(BitmapInfo.GETTING_DATA);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStreamWrapper, outPadding, options);
                if (bitmap == null) {
                    throw new RuntimeException("Decode failed.");
                }
                mWorkingItem.updateProgress(BitmapInfo.DATA_FINISH);
                BitmapSource source = mWorkingItem.getSource();
                if (source.filter != null) {
                    mWorkingItem.updateProgress(BitmapInfo.FILTERING);
                    BitmapFilter filter = source.filter;
                    bitmap = filter.filter(bitmap);
                    if (bitmap == null) {
                        throw new RuntimeException("Filter failed.");
                    }
                }
                mWorkingItem.setBitmap(bitmap);
                mWorkingItem.updateProgress(BitmapInfo.GET_BITMAP);
                BitmapInfoManager.optimize();

                BitmapCacheLogger.logVerbose("BitmapInfo " + mWorkingItem.hashCode() + " get succeed.");

                releaseResources(false);
            } catch (Exception e) {
                mWorkingItem.setBitmap(null);
                mWorkingItem.updateProgress(BitmapInfo.GET_ERROR);
                BitmapInfoManager.optimize();

                BitmapCacheLogger.logError("BitmapInfo " + mWorkingItem.hashCode() + " get failed.");
                BitmapCacheLogger.logError(e);

                releaseResources(true);
            } finally {
                mWorkingItem = null;
            }
        }
    }
}
