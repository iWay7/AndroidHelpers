package site.iway.androidhelpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.List;

public class CameraPreview extends SurfaceView implements Callback, AutoFocusCallback, OnGestureListener {

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

    private boolean mFocusByTouch;
    private int mCameraRotate;
    private boolean mDetectQRCode;
    private int mCameraId;
    private int mVisibility;

    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private GestureDetector mGestureDetector;
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
        mFocusByTouch = a.getBoolean(R.styleable.CameraPreview_focusByTouch, false);
        mCameraRotate = a.getInt(R.styleable.CameraPreview_cameraRotate, 0);
        mDetectQRCode = a.getBoolean(R.styleable.CameraPreview_detectQRCode, false);
        mCameraId = a.getInt(R.styleable.CameraPreview_cameraId, 0);
        mVisibility = getVisibility();
        a.recycle();
        mHandler = new Handler();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mGestureDetector = new GestureDetector(context, this);
    }

    private Camera mCamera;
    private Camera.Parameters mParameters;
    private byte[] mPreviewData;

    private void configCamera() {
        if (mCameraRotate < 0) {
            Activity activity = (Activity) getContext();
            int windowOrientation = WindowHelper.getWindowRotationDegrees(activity);
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            switch (info.facing) {
                case CameraInfo.CAMERA_FACING_FRONT:
                    int displayOrientation = (info.orientation + windowOrientation) % 360;
                    displayOrientation = (360 - displayOrientation) % 360;
                    mCamera.setDisplayOrientation(displayOrientation);
                    break;
                case CameraInfo.CAMERA_FACING_BACK:
                    displayOrientation = (info.orientation - windowOrientation + 360) % 360;
                    mCamera.setDisplayOrientation(displayOrientation);
                    break;
                default:
                    mCamera.setDisplayOrientation(0);
                    break;
            }
        } else {
            mCamera.setDisplayOrientation(mCameraRotate);
        }
    }

    private void configCameraParameters(int width, int height) {
        List<Size> supportedPictureSizes = mParameters.getSupportedPictureSizes();
        List<Integer> supportedPreviewFormats = mParameters.getSupportedPreviewFormats();
        List<Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();
        List<Size> supportedJpegThumbnailSizes = mParameters.getSupportedJpegThumbnailSizes();
        List<int[]> supportedPreviewFpsRange = mParameters.getSupportedPreviewFpsRange();
        List<String> supportedAntibanding = mParameters.getSupportedAntibanding();
        List<String> supportedColorEffects = mParameters.getSupportedColorEffects();
        List<String> supportedFlashModes = mParameters.getSupportedFlashModes();
        List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
        List<Integer> supportedPictureFormats = mParameters.getSupportedPictureFormats();
        List<String> supportedSceneModes = mParameters.getSupportedSceneModes();
        List<String> supportedWhiteBalance = mParameters.getSupportedWhiteBalance();

        int surfaceArea = width * height;
        Size optimalPreviewSize = null;
        for (Size size : supportedPreviewSizes) {
            if (optimalPreviewSize == null) {
                optimalPreviewSize = size;
            } else {
                int sizeArea = size.width * size.height;
                int optimalPreviewSizeArea = optimalPreviewSize.width * optimalPreviewSize.height;
                int dSizeArea = sizeArea - surfaceArea;
                int dOptimalPreviewSize = optimalPreviewSizeArea - surfaceArea;
                if (Math.abs(dSizeArea) < Math.abs(dOptimalPreviewSize)) {
                    optimalPreviewSize = size;
                }
            }
        }
        mParameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
    }

    private void initializeCamera(int width, int height) {
        if (mCamera != null) {
            destroyCamera();
        }
        try {
            mCamera = Camera.open(mCameraId);
            configCamera();
            mParameters = mCamera.getParameters();
            configCameraParameters(width, height);

            mCamera.setParameters(mParameters);
            mCamera.setPreviewCallback(new PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (data != null && data.length > 0) {
                        if (mPreviewData == null) {
                            mPreviewData = new byte[data.length];
                            System.arraycopy(data, 0, mPreviewData, 0, data.length);
                        }
                    }
                }
            });
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            if (mOnPreviewErrorListener != null) {
                mOnPreviewErrorListener.onPreviewError(e);
            }
        }
    }

    private void destroyCamera() {
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

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
            initializeCamera(mSurfaceWidth, mSurfaceHeight);
        } else {
            destroyCamera();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceWidth = 0;
        mSurfaceHeight = 0;
        destroyCamera();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mVisibility != visibility) {
            if (visibility == View.VISIBLE) {
                if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
                    initializeCamera(mSurfaceWidth, mSurfaceHeight);
                }
            } else {
                destroyCamera();
            }
            mVisibility = visibility;
        }
    }

    private boolean mSuperHandledTouchEvent;

    private boolean handleSuperTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mSuperHandledTouchEvent = super.onTouchEvent(event);
        } else {
            if (mSuperHandledTouchEvent) {
                mSuperHandledTouchEvent = super.onTouchEvent(event);
            }
        }
        return mSuperHandledTouchEvent;
    }

    private boolean mAutoFocusing;

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        mAutoFocusing = false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // nothing
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mCamera != null && mFocusByTouch && mAutoFocusing == false) {
            mCamera.autoFocus(this);
            mAutoFocusing = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // nothing
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleSuperTouchEvent(event) || mGestureDetector.onTouchEvent(event);
    }

    private boolean mHasAttachedToWindow;
    private Thread mQRCodeDetectThread;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHasAttachedToWindow = true;
        if (mDetectQRCode) {
            mQRCodeDetectThread = new Thread() {
                public void run() {
                    while (mHasAttachedToWindow) {
                        if (mPreviewData != null) {
                            Size previewSize = mParameters.getPreviewSize();
                            int width = previewSize.width;
                            int height = previewSize.height;
                            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                                    mPreviewData,
                                    width, height,
                                    0, 0,
                                    width, height,
                                    false);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            QRCodeReader qrCodeReader = new QRCodeReader();
                            try {
                                final Result result = qrCodeReader.decode(bitmap);
                                final String text = result.getText();
                                if (mOnQRCodeDetectedListener != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mOnQRCodeDetectedListener.onQRCodeDetected(text);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                // nothing
                            }
                            mPreviewData = null;
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            mQRCodeDetectThread.setPriority(Thread.MIN_PRIORITY);
            mQRCodeDetectThread.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mQRCodeDetectThread != null) {
            mQRCodeDetectThread.interrupt();
            mQRCodeDetectThread = null;
        }
        mHasAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

}
