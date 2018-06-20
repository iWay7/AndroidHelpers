package site.iway.androidhelpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import site.iway.javahelpers.MathHelper;
import site.iway.javahelpers.Scale;
import site.iway.javahelpers.XAlign;
import site.iway.javahelpers.YAlign;

public class CameraPreview extends FrameLayout implements Callback {

    public interface OnQRCodeDetectedListener {
        public void onQRCodeDetected(String qrCode);
    }

    public interface OnPreviewErrorListener {
        public void onPreviewError(Exception e);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttr(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        resolveAttr(context, attrs);
    }

    public CameraPreview(Context context) {
        super(context);
        resolveAttr(context, null);
    }

    private int mCameraId;
    private boolean mCameraIdChanged;
    private boolean mFocusByClick;
    private boolean mDetectQRCode;

    private Activity mActivity;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceRunning;
    private OnQRCodeDetectedListener mOnQRCodeDetectedListener;
    private OnPreviewErrorListener mOnPreviewErrorListener;

    public void setOnQRCodeDetectedListener(OnQRCodeDetectedListener l) {
        mOnQRCodeDetectedListener = l;
    }

    public void setOnPreviewErrorListener(OnPreviewErrorListener l) {
        mOnPreviewErrorListener = l;
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraPreview);
        mCameraId = a.getInt(R.styleable.CameraPreview_cameraId, 0);
        mCameraIdChanged = true;
        mFocusByClick = a.getBoolean(R.styleable.CameraPreview_focusByClick, false);
        mDetectQRCode = a.getBoolean(R.styleable.CameraPreview_detectQRCode, false);
        a.recycle();
        mActivity = (Activity) context;
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceRunning = false;
        setOnClickListener(mRequestAutoFocusListener);
    }

    public void setCameraId(int cameraId) {
        if (mCameraId != cameraId) {
            mCameraId = cameraId;
            mCameraIdChanged = true;
            if (mSurfaceRunning) {
                tryInitializeCamera();
            }
        }
    }

    public void setFocusByClick(boolean focusByClick) {
        mFocusByClick = focusByClick;
    }

    public void setDetectQRCode(boolean detectQRCode) {
        mDetectQRCode = detectQRCode;
    }

    private Camera mCamera;
    private Parameters mParameters;
    private ReentrantLock mPreviewDataLock = new ReentrantLock();
    private byte[] mPreviewData;
    private int mPreviewDataWidth;
    private int mPreviewDataHeight;

    private void tryInitializeCamera() {
        destroyCameraIfExisted();
        try {
            if (mCameraIdChanged) {
                mCamera = Camera.open(mCameraId);
                mParameters = mCamera.getParameters();
                mCameraIdChanged = false;
            }
            Size suggestedPreviewSize = null;
            boolean surfaceViewChanged = false;
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            int clientWidth = viewWidth - paddingLeft - paddingRight;
            int clientHeight = viewHeight - paddingTop - paddingBottom;
            int windowOrientation = WindowHelper.getWindowRotationDegrees(mActivity);
            if (clientWidth > 0 && clientHeight > 0) {
                List<Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();
                Collections.sort(supportedPreviewSizes, new Comparator<Size>() {
                    @Override
                    public int compare(Size o1, Size o2) {
                        return o1.width * o1.height - o2.width * o2.height;
                    }
                });
                for (Size size : supportedPreviewSizes) {
                    int previewWidth = size.width;
                    int previewHeight = size.height;
                    if (windowOrientation % 180 == 0) {
                        previewWidth = size.height;
                        previewHeight = size.width;
                    }
                    if (previewWidth >= clientWidth && previewHeight >= clientHeight) {
                        suggestedPreviewSize = size;
                        break;
                    }
                }
                if (suggestedPreviewSize == null) {
                    suggestedPreviewSize = supportedPreviewSizes.get(supportedPreviewSizes.size() - 1);
                }
                mParameters.setPreviewSize(suggestedPreviewSize.width, suggestedPreviewSize.height);
                int previewWidth = suggestedPreviewSize.width;
                int previewHeight = suggestedPreviewSize.height;
                if (windowOrientation % 180 == 0) {
                    previewWidth = suggestedPreviewSize.height;
                    previewHeight = suggestedPreviewSize.width;
                }
                float surfaceScale = Scale.Center.getScale(clientWidth, clientHeight, previewWidth, previewHeight);
                int targetWidth = MathHelper.pixel(surfaceScale * previewWidth);
                int targetHeight = MathHelper.pixel(surfaceScale * previewHeight);
                float targetLeft = MathHelper.pixel(XAlign.CenterCenter.getX(paddingLeft, clientWidth, targetWidth * 1.0f));
                float targetTop = MathHelper.pixel(YAlign.CenterCenter.getY(paddingTop, clientHeight, targetHeight * 1.0f));
                LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
                float translationX = mSurfaceView.getTranslationX();
                float translationY = mSurfaceView.getTranslationY();
                if (layoutParams.width != targetWidth || layoutParams.height != targetHeight ||
                        translationX != targetLeft || translationY != targetTop) {
                    layoutParams.width = targetWidth;
                    layoutParams.height = targetHeight;
                    mSurfaceView.setTranslationX(targetLeft);
                    mSurfaceView.setTranslationY(targetTop);
                    mSurfaceView.setLayoutParams(layoutParams);
                    surfaceViewChanged = true;
                }
            }
            if (surfaceViewChanged) {
                destroyCameraIfExisted();
            } else {
                if (mCamera == null) {
                    mCamera = Camera.open(mCameraId);
                }
                CameraInfo info = new CameraInfo();
                Camera.getCameraInfo(mCameraId, info);
                int cameraFacing = info.facing;
                int cameraOrientation = info.orientation;
                int displayOrientation = 0;
                switch (cameraFacing) {
                    case CameraInfo.CAMERA_FACING_FRONT:
                        displayOrientation = (cameraOrientation + windowOrientation) % 360;
                        displayOrientation = (360 - displayOrientation) % 360;
                        break;
                    case CameraInfo.CAMERA_FACING_BACK:
                        displayOrientation = (cameraOrientation - windowOrientation + 360) % 360;
                        break;
                }
                mCamera.setDisplayOrientation(displayOrientation);
                mCamera.setParameters(mParameters);
                mCamera.setPreviewCallback(new PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (mPreviewDataLock.tryLock()) {
                            if (mPreviewData == null) {
                                mPreviewData = data;
                                Size previewSize = mParameters.getPreviewSize();
                                mPreviewDataWidth = previewSize.width;
                                mPreviewDataHeight = previewSize.height;
                            }
                            mPreviewDataLock.unlock();
                        }
                    }
                });
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            destroyCameraIfExisted();
            if (mOnPreviewErrorListener != null) {
                mOnPreviewErrorListener.onPreviewError(e);
            }
        }
    }

    private void destroyCameraIfExisted() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // nothing
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceRunning = true;
        tryInitializeCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroyCameraIfExisted();
        mSurfaceRunning = false;
    }

    private boolean mAutoFocusing;

    public void autoFocus() {
        if (mCamera != null && !mAutoFocusing) {
            mAutoFocusing = true;
            mCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mAutoFocusing = false;
                }
            });
        }
    }

    private OnClickListener mRequestAutoFocusListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFocusByClick) {
                autoFocus();
            }
        }
    };

    private class RotationAndQRCodeDetector extends Thread {

        private int mLastWindowRotationDegrees;

        private boolean mIsCanceled;

        @Override
        public void run() {
            while (!mIsCanceled) {
                int currentWindowRotationDegrees = WindowHelper.getWindowRotationDegrees(mActivity);
                if (currentWindowRotationDegrees != mLastWindowRotationDegrees) {
                    if ((mLastWindowRotationDegrees == 90 && currentWindowRotationDegrees == 270) ||
                            (mLastWindowRotationDegrees == 270 && currentWindowRotationDegrees == 90)) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                if (mSurfaceRunning) {
                                    tryInitializeCamera();
                                }
                            }
                        });
                    }
                    mLastWindowRotationDegrees = currentWindowRotationDegrees;
                }
                if (mDetectQRCode) {
                    byte[] savedPreviewData = null;
                    int savedPreviewDataWidth = 0;
                    int savedPreviewDataHeight = 0;
                    if (mPreviewDataLock.tryLock()) {
                        savedPreviewData = mPreviewData;
                        savedPreviewDataWidth = mPreviewDataWidth;
                        savedPreviewDataHeight = mPreviewDataHeight;
                        mPreviewData = null;
                        mPreviewDataWidth = 0;
                        mPreviewDataHeight = 0;
                        mPreviewDataLock.unlock();
                    }
                    if (savedPreviewData != null && savedPreviewData.length > 0 &&
                            savedPreviewDataWidth > 0 && savedPreviewDataHeight > 0) {
                        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                                savedPreviewData,
                                savedPreviewDataWidth, savedPreviewDataHeight,
                                0, 0, savedPreviewDataWidth, savedPreviewDataHeight,
                                false);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        QRCodeReader qrCodeReader = new QRCodeReader();
                        try {
                            final Result result = qrCodeReader.decode(bitmap);
                            final String text = result.getText();
                            if (mOnQRCodeDetectedListener != null) {
                                post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOnQRCodeDetectedListener.onQRCodeDetected(text);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                    }
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // nothing
                }
            }
        }

        public void cancel() {
            mIsCanceled = true;
        }
    }

    private RotationAndQRCodeDetector mRotationAndQRCodeDetector;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mRotationAndQRCodeDetector != null) {
            mRotationAndQRCodeDetector.cancel();
        }
        mRotationAndQRCodeDetector = new RotationAndQRCodeDetector();
        mRotationAndQRCodeDetector.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mRotationAndQRCodeDetector != null) {
            mRotationAndQRCodeDetector.cancel();
        }
        super.onDetachedFromWindow();
    }


}
