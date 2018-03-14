package com.oracle.bits.parasjos.sdpdassignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.oracle.bits.parasjos.sdpdassignment.helper.SupportedFeatures;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PhoneDetailsActivity extends AppCompatActivity {

    public static final String activityKey  = "Phone Status";
    TextView phoneDetails;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_details);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_details);
        phoneDetails = findViewById(R.id.phone_details_text_id);
        checkPermissionElseRequest();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void checkPermissionElseRequest() {
        if (SupportedFeatures.hasAccess(this, Manifest.permission.READ_PHONE_STATE)) {
            fetchAndDisplayPhoneDetails();
        } else {
            SupportedFeatures.requestAccess(this,
                    Manifest.permission.READ_PHONE_STATE,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    SupportedFeatures.PERMISSION_READ_STATE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SupportedFeatures.PERMISSION_READ_STATE) {
            if (SupportedFeatures.checkPermissionGrantResult(grantResults)) {
                fetchAndDisplayPhoneDetails();
            } else {
                Toast.makeText(this, getString(R.string.phone_access_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchAndDisplayPhoneDetails() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        String info = new String();

        //Get Battery Percentage
        info+=getString(R.string.status_battery);
        info+="\n"+getString(R.string.status_battery_level)+" : "+batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)+ "%";

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) ? getString(R.string.status_battery_charging) : getString(R.string.status_battery_discharging);
        info+="\n"+getString(R.string.status_battery_status)+": "+isCharging;
        if(isCharging.equalsIgnoreCase(getString(R.string.status_battery_charging))){
            // How are we charging?
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            String usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB)? getString(R.string.status_battery_charging_USB):null;
            String acCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)? getString(R.string.status_battery_charging_AC):null;

            String chargeSource = usbCharge == null ? acCharge: usbCharge;
            info+="\n"+getString(R.string.status_battery_docking)+": "+chargeSource;
        }

        //Get Network information
        info+="\n\n"+getString(R.string.status_network)+": ";
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected){
            info+="\n"+getString(R.string.status_network)+": "+getString(R.string.status_network_connected)+".";
            info+="\n"+getString(R.string.status_network_connection)+": "+
                    (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                            ? getString(R.string.status_network_connection_wifi):getString(R.string.status_network_connection_cell_data));
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null && connectionInfo.getSSID() != null && !connectionInfo.getSSID().isEmpty()) {
                    info += " (" + connectionInfo.getSSID() + ")";
                }
            }
        }else{
            info+="\n"+getString(R.string.status_network)+": "+getString(R.string.status_network_disconnected)+".";
        }

        //Get Bluetooth Information
        info+="\n\n"+getString(R.string.status_Bluetooth)+": ";
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                info+="\n"+getString(R.string.status_Bluetooth_status)+": "+getString(R.string.status_Bluetooth_status_enabled)+".";
                info+="\n"+getString(R.string.status_Bluetooth_profile_headset)+" : "+(mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED
                        ? getString(R.string.status_Bluetooth_profile_connected): getString(R.string.status_Bluetooth_profile_disconnected));
                if(mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED ){
                    try {
                        List<BluetoothDevice> deviceList = bluetoothManager.getConnectedDevices(BluetoothHeadset.HEADSET);
                        if(deviceList!=null && !deviceList.isEmpty()){
                            info+="(";
                            for (BluetoothDevice device: deviceList) {
                                info+=" "+device.getName();
                            }
                            info+=")";
                        }
                    }catch (Exception e){

                    }

                }
                info+="\n"+getString(R.string.status_Bluetooth_profile_a2dp)+" : "+(mBluetoothAdapter.getProfileConnectionState(BluetoothA2dp.A2DP) == BluetoothA2dp.STATE_CONNECTED
                        ? getString(R.string.status_Bluetooth_profile_connected): getString(R.string.status_Bluetooth_profile_disconnected));
                if(mBluetoothAdapter.getProfileConnectionState(BluetoothA2dp.A2DP) == BluetoothA2dp.STATE_CONNECTED ){
                    try{
                    List<BluetoothDevice> deviceList = bluetoothManager.getConnectedDevices(BluetoothA2dp.A2DP);
                    if(deviceList!=null && !deviceList.isEmpty()){
                        info+="(";
                        for (BluetoothDevice device: deviceList) {
                            info+=" "+device.getName();
                        }
                        info+=")";
                    }
                    }catch (Exception e){

                    }
                }
                info+="\n"+getString(R.string.status_Bluetooth_profile_health)+" : "+(mBluetoothAdapter.getProfileConnectionState(BluetoothHealth.HEALTH) == BluetoothHealth.STATE_CONNECTED
                        ? getString(R.string.status_Bluetooth_profile_connected): getString(R.string.status_Bluetooth_profile_disconnected));
                if(mBluetoothAdapter.getProfileConnectionState(BluetoothHealth.HEALTH) == BluetoothHealth.STATE_CONNECTED){
                    try{
                    List<BluetoothDevice> deviceList = bluetoothManager.getConnectedDevices(BluetoothHealth.HEALTH);
                    if(deviceList!=null && !deviceList.isEmpty()){
                        info+="(";
                        for (BluetoothDevice device: deviceList) {
                            info+=" "+device.getName();
                        }
                        info+=")";
                    }
                    }catch (Exception e){

                    }
                }
            }else{
                info+="\n"+getString(R.string.status_Bluetooth_status)+": "+getString(R.string.status_Bluetooth_status_disabled)+".";
            }
        }else{
            info+="\n"+getString(R.string.status_Bluetooth_status)+": "+getString(R.string.status_Bluetooth_status_unsupported)+".";
        }
        phoneDetails.setText(info);
    }

    public void goToHome(MenuItem item) {
        finish();
    }

    public void goToHome(View view)  {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
