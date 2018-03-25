package site.iway.androidhelpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

public class LocationGetter implements LocationListener {

    public interface LocationGetterListener {
        public void onLocationGet(Location location);
    }

    private Context mContext;
    private LocationManager mLocationManager;
    private LocationGetterListener mOnLocationGetListener;

    public LocationGetter(Context context) {
        mContext = context;
        Object service = mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager = (LocationManager) service;
    }

    public void setOnLocationGetListener(LocationGetterListener l) {
        mOnLocationGetListener = l;
    }

    private boolean checkPermission() {
        int pid = android.os.Process.myPid();
        int uid = android.os.Process.myUid();
        int p1 = mContext.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, pid, uid);
        int p2 = mContext.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, pid, uid);
        return p1 == PackageManager.PERMISSION_GRANTED && p2 == PackageManager.PERMISSION_GRANTED;
    }

    public boolean startGPS() {
        String provider = LocationManager.GPS_PROVIDER;
        Looper looper = Looper.getMainLooper();
        if (checkPermission()) {
            mLocationManager.requestSingleUpdate(provider, this, looper);
            return true;
        } else {
            return false;
        }
    }

    public boolean startGPS(long minTime, float minDistance) {
        String provider = LocationManager.GPS_PROVIDER;
        Looper looper = Looper.getMainLooper();
        if (checkPermission()) {
            mLocationManager.requestLocationUpdates(provider, minTime, minDistance, this, looper);
            return true;
        } else {
            return false;
        }
    }

    public boolean startNetwork() {
        String provider = LocationManager.NETWORK_PROVIDER;
        Looper looper = Looper.getMainLooper();
        if (checkPermission()) {
            mLocationManager.requestSingleUpdate(provider, this, looper);
            return true;
        } else {
            return false;
        }
    }

    public boolean startNetwork(long minTime, float minDistance) {
        String provider = LocationManager.NETWORK_PROVIDER;
        Looper looper = Looper.getMainLooper();
        if (checkPermission()) {
            mLocationManager.requestLocationUpdates(provider, minTime, minDistance, this, looper);
            return true;
        } else {
            return false;
        }
    }

    public boolean startPassive() {
        String provider = LocationManager.PASSIVE_PROVIDER;
        Looper looper = Looper.getMainLooper();
        if (checkPermission()) {
            mLocationManager.requestSingleUpdate(provider, this, looper);
            return true;
        } else {
            return false;
        }
    }

    public boolean startPassive(long minTime, float minDistance) {
        String provider = LocationManager.PASSIVE_PROVIDER;
        Looper looper = Looper.getMainLooper();
        if (checkPermission()) {
            mLocationManager.requestLocationUpdates(provider, minTime, minDistance, this, looper);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mOnLocationGetListener.onLocationGet(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public boolean stop() {
        if (checkPermission()) {
            mLocationManager.removeUpdates(this);
            return true;
        } else {
            return false;
        }
    }

}
