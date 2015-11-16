package com.android.akshay.wakeup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";
    public static final String THRESHOLD = "THRESHOLD";
    private static final int REQUEST_CODE_ENABLE_ADMIN = 99;

    private Intent mPersistentService;
    private NumberPicker numberPicker;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPersistentService = new Intent(this, PersistentService.class);

        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);

        sharedPreferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THRESHOLD, sharedPreferences.getInt(THRESHOLD, 2));
        editor.commit();

        numberPicker.setValue(sharedPreferences.getInt(THRESHOLD, 2));

        if (!isMyServiceRunning(PersistentService.class)) {
            startService(mPersistentService);
//            Toast.makeText(this, "Started service with threshold = 2", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "Service is already running", Toast.LENGTH_SHORT).show();
        }

        DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdminSample = new ComponentName(this, AdminActivity.class);
        if (!mDPM.isAdminActive(mDeviceAdminSample)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, this.getString(R.string.add_admin_extra_app_text));
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        }
    }

    public void onStartClicked(View v) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THRESHOLD, numberPicker.getValue());
        editor.commit();
        startService(mPersistentService);
//        Toast.makeText(this, "Started service with threshold = " + numberPicker.getValue(), Toast.LENGTH_SHORT).show();
    }

    public void onStopClicked(View v) {
        stopService(mPersistentService);
//        Toast.makeText(this, "Stopped service", Toast.LENGTH_SHORT).show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Device Administration permission was denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
