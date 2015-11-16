package com.android.akshay.wakeup;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

/**
 * Created by akshayk on 23/10/15.
 */

public class PersistentService extends Service implements SensorEventListener {

    public static final String TAG = "PersistentService";

    private SensorManager mSensorManager;
    private Sensor mProximity;

    private PowerManager.WakeLock wakeLock;

    private PowerManager powerManager;
    private int count = 0;
    private int threshold;
    private long time = 0;
    private boolean isScreenOn = false;

    private ScreenOnReceiver screenOnReceiver;
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminSample;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO This wont work after the app has been killed. Put this data in persistent memory and retrieve from there.
        if (intent != null) {
            threshold = intent.getExtras().getInt("data") * 1000;
        } else {
            threshold = 2 * 1000;
        }
        Log.d(TAG, "Setting threshold to " + threshold);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_UI);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, TAG);

        screenOnReceiver = new ScreenOnReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnReceiver, intentFilter);

        isScreenOn = isScreenOn(this);

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this, AdminActivity.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        unregisterReceiver(screenOnReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int distance = (int) event.values[0];
        Log.d(TAG, "Changed " + distance);
        if (distance == 0) {
            time = System.currentTimeMillis();
        } else {
            long now = System.currentTimeMillis();
            if (((now - time) >= (threshold - 250)) && ((now - time) <= (threshold + 1000))) {
                if (!isScreenOn) {
                    Log.d(TAG, "Wake up");
//                    Intent intent = new Intent(this, TransientActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                    wakeLock.acquire(1);
                } else {
                    if (mDPM.isAdminActive(mDeviceAdminSample)) {
                        Log.d(TAG, "Lock screen");
                        mDPM.lockNow();
                    }
                }
            } else {
                Log.d(TAG, "Time elapsed = " + (now - time) + " Screen On = " + isScreenOn);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            //noinspection deprecation
            return powerManager.isScreenOn();
        }
    }

    private class ScreenOnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                isScreenOn = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isScreenOn = false;
            }
        }

    }
}
