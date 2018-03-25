package site.iway.androidhelpers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PhoneOverturnDetector implements SensorEventListener {

    public interface PhoneOverturnListener {
        public void onPhoneOverturn(PhoneOverturnDetector phoneOverturnDetector);
    }

    private Context mContext;
    private SensorManager mSensorManager;
    private int mTestCount = 0;
    private float mSourceZValue = 0;
    private boolean mIsSource = false;
    private PhoneOverturnListener mListener;

    public PhoneOverturnDetector(Context context) {
        mContext = context;
    }

    public void setPhoneOverturnListener(PhoneOverturnListener listener) {
        mListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mTestCount++;
        if (mTestCount < 30)
            return;
        if (!mIsSource) {
            float z = Math.abs(event.values[2]);
            if (z > 9.3f && z < 10.0f) {
                mIsSource = true;
                mSourceZValue = event.values[2];
            }
        } else {
            float span = event.values[2] - mSourceZValue;
            float absSpan = Math.abs(span);
            if (absSpan > 18.6f && absSpan < 20.0f && mListener != null) {
                mSourceZValue = event.values[2];
                mListener.onPhoneOverturn(this);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void start() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
    }

}
