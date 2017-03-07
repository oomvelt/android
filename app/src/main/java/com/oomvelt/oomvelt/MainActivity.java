package com.oomvelt.oomvelt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.oomvelt.oomvelt.bluetooth.BTHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.bluetooth_choose) Button bluetoothChoose;
    @BindView(R.id.bluetooth_status) TextView bluetoothStatus;

    @BindString(R.string.error_bt_no_title) String errorBtNoTitle;
    @BindString(R.string.error_bt_no_message) String errorBtNoMessage;
    @BindString(R.string.error_bt_disabled_title) String errorBtDisabledTitle;
    @BindString(R.string.error_bt_disabled_message) String errorBtDisabledMessage;

    @BindString(R.string.main_bluetooth_devices_title) String bluetoothDevicesTitle;
    @BindString(R.string.main_bluetooth_devices_loading) String bluetoothDevicesLoading;

    @BindString(R.string.main_bluetooth_status_device) String mainBtStatusDevice;
    @BindString(R.string.main_bluetooth_status_none) String mainBtStatusNone;

    private static final String TAG = "MainActivity";
    private BTHelper btHelper;

    private MainActivity activity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private InputStream mInputStream;
    private MaterialDialog progressDialog;

    private final BTDeviceListAdapter.ItemCallback btDebviceCallback = new BTDeviceListAdapter.ItemCallback() {
        @Override
        public void onItemClicked(BluetoothDevice device) {
            Log.v(TAG, "Selected device");
            Log.v(TAG, device.getName());
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive");
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.v(TAG, "DEVICE DESCOVERY STARTED");
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.v(TAG, "DEVICE DESCOVERY ENDED");
                progressDialog.dismiss();

                BTDeviceListAdapter adapter = new BTDeviceListAdapter(devices);
                adapter.setCallback(btDebviceCallback);

                new MaterialDialog.Builder(activity)
                        .title(bluetoothDevicesTitle)
                        .adapter(adapter, null)
                        .show();
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!devices.contains(device)) {
                    devices.add(device);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btHelper = new BTHelper(this);

        activity = this;
        mBluetoothAdapter = BTHelper.Companion.getBluetoothDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.uiBluetoothDisabled();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION };

                requestPermissions(permissions, Util.REQUEST_PERMISSION_LOCATION);
            } else {
                this.checkIfBluetoothIsEnabled();
            }
        } else {
            this.checkIfBluetoothIsEnabled();
        }
    }

    @OnClick(R.id.bluetooth_choose)
    public void submit() {
        progressDialog = new MaterialDialog.Builder(this)
                .title(bluetoothDevicesTitle)
                .content(bluetoothDevicesLoading)
                .progress(true, 0)
                .show();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        devices.addAll(pairedDevices);

        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filterStart);
        registerReceiver(mReceiver, filterFound);
        registerReceiver(mReceiver, filterFinish);

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        boolean startedDiscovery = mBluetoothAdapter.startDiscovery();
        if (!startedDiscovery) {
            Log.v(TAG, "Unable to start discovery process.");
        } else {
            Log.v(TAG, "Started discovery process.");
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.v(TAG, "Callback");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && requestCode == Util.REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "We got permission");

                this.checkIfBluetoothIsEnabled();
            } else {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, Util.REQUEST_PERMISSION_LOCATION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.REQUEST_BT_DEVICE) {
            if (resultCode == RESULT_OK) {
                BluetoothDevice device = data.getExtras().getParcelable("BluetoothDevice");

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                BluetoothClass cls = device.getBluetoothClass();

                Log.v(TAG, "Selected BT device");
                Log.v(TAG, "deviceName=" + deviceName + "; deviceHWAddress=" + deviceHardwareAddress);
                Log.v(TAG, cls.toString());

                this.connectToDevice(device);
            } else {
                Util.showAlert(this, errorBtDisabledTitle, errorBtDisabledMessage);
            }
        }

        if (requestCode == Util.REQUEST_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                this.uiBluetoothEnabled();
            } else {
                Util.showAlert(this, errorBtDisabledTitle, errorBtDisabledMessage);
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            mBluetoothSocket.connect();
            mInputStream = mBluetoothSocket.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
            String line;

            line = br.readLine();
            Log.v(TAG, "Read line");
            Log.v(TAG, line);

            line = br.readLine();
            Log.v(TAG, "Read line");
            Log.v(TAG, line);

            line = br.readLine();
            Log.v(TAG, "Read line");
            Log.v(TAG, line);
        } catch (IOException e) {
            Util.showAlert(this, errorBtDisabledTitle, errorBtDisabledMessage);
            e.printStackTrace();
        }
    }

    private void checkIfBluetoothIsEnabled() {
        btHelper.checkIfEnabled(i -> this.uiBluetoothEnabled());

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Util.REQUEST_BT_ENABLE);
        } else {
            this.uiBluetoothEnabled();
        }
    }

    private void uiBluetoothDisabled() {
        bluetoothStatus.setText(mainBtStatusNone);
        bluetoothChoose.setEnabled(false);
    }

    private void uiBluetoothEnabled() {
        bluetoothChoose.setEnabled(true);
    }
}
