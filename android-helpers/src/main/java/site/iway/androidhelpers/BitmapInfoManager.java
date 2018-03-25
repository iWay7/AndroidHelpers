package site.iway.androidhelpers;

import java.util.Arrays;
import java.util.Comparator;

class BitmapInfoManager {

    private static final Object mSynchronizer = new Object();
    private static final Object mBitmapWaiter = new Object();
    private static BitmapInfo[] mArray = new BitmapInfo[16];
    private static int mArraySize;
    private static boolean mShouldCheckLoad;

    static void add(BitmapInfo bitmapInfo) {
        synchronized (mSynchronizer) {
            int targetSize = mArraySize + 1;
            if (mArray.length < targetSize) {
                BitmapInfo[] newArray = new BitmapInfo[mArray.length * 2];
                System.arraycopy(mArray, 0, newArray, 0, mArray.length);
                mArray = newArray;
            }
            bitmapInfo.priority = System.nanoTime();
            mArray[mArraySize] = bitmapInfo;
            mArraySize = targetSize;
            mShouldCheckLoad = true;
        }
        synchronized (mBitmapWaiter) {
            mBitmapWaiter.notify();
        }
    }

    static BitmapInfo get(BitmapSource bitmapSource) {
        synchronized (mSynchronizer) {
            for (int i = 0; i < mArraySize; i++) {
                if (mArray[i].source.equals(bitmapSource)) {
                    mArray[i].priority = System.nanoTime();
                    return mArray[i];
                }
            }
            return null;
        }
    }

    static BitmapInfo getReadyItem(boolean setPreparing) {
        synchronized (mSynchronizer) {
            if (mShouldCheckLoad) {
                Arrays.sort(mArray, 0, mArraySize, mPriorityComparator);
                for (int i = 0; i < mArraySize; i++) {
                    BitmapInfo item = mArray[i];
                    if (item.progress == BitmapInfo.READY_TO_START) {
                        if (setPreparing) {
                            item.updateProgress(BitmapInfo.PREPARING);
                        }
                        return item;
                    }
                }
                mShouldCheckLoad = false;
            }
            return null;
        }
    }

    static void waitForBitmapInfoAdded() {
        synchronized (mBitmapWaiter) {
            try {
                mBitmapWaiter.wait();
            } catch (InterruptedException e) {
                // nothing
            }
        }
    }

    static Comparator<BitmapInfo> mPriorityComparator = new Comparator<BitmapInfo>() {
        @Override
        public int compare(BitmapInfo lhs, BitmapInfo rhs) {
            if (lhs.progress == BitmapInfo.GET_ERROR && rhs.progress != BitmapInfo.GET_ERROR)
                return 1;
            if (lhs.progress != BitmapInfo.GET_ERROR && rhs.progress == BitmapInfo.GET_ERROR)
                return -1;
            if (lhs.priority < rhs.priority)
                return 1;
            if (lhs.priority > rhs.priority)
                return -1;
            return 0;
        }
    };

    static void optimize(int ramToIncrease) {
        synchronized (mSynchronizer) {
            Arrays.sort(mArray, 0, mArraySize, mPriorityComparator);
            int ramUsageSum = ramToIncrease;
            for (int i = 0; i < mArraySize; i++) {
                boolean isError = mArray[i].progress == BitmapInfo.GET_ERROR;
                int itemRAMUsage = mArray[i].getRAMUsage();
                if (isError || ramUsageSum + itemRAMUsage > BitmapCache.MAX_RAM_USAGE) {
                    int targetSize = i;
                    do {
                        mArray[i].lock();
                        mArray[i].releaseRAM();
                        mArray[i].unlock();
                        mArray[i] = null;
                        i++;
                    } while (i < mArraySize);
                    mArraySize = targetSize;
                } else {
                    ramUsageSum += itemRAMUsage;
                }
            }
        }
    }

    static void optimize() {
        optimize(0);
    }

}
