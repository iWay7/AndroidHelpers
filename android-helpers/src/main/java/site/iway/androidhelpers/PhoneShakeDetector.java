package site.iway.androidhelpers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PhoneShakeDetector implements SensorEventListener {

    public interface PhoneShakeListener {
        public void onPhoneShake(PhoneShakeDetector phoneShakeDetector);
    }

    private Context mContext;
    private SensorManager mSensorManager;
    private PhoneShakeListener mListener;
    private int mShakeThreshold = 5000;
    private float x, y, z, last_x, last_y, last_z;
    private long lastUpdate;

    public PhoneShakeDetector(Context context) {
        mContext = context;
    }

    public void setPhoneShakeListener(PhoneShakeListener listener) {
        mListener = listener;
    }

    public void setShakeThreshold(int value) {
        mShakeThreshold = value;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
            if (speed > mShakeThreshold) {
                mListener.onPhoneShake(this);
            }
            last_x = x;
            last_y = y;
            last_z = z;
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
