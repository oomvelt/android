package com.oomvelt.oomvelt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BTListActivity extends AppCompatActivity {
//    private static final String TAG = "BTListActivity";
//
//    @BindView(R.id.btlist_container) CoordinatorLayout container;
//    @BindView(R.id.btlist_devices) ListView btListDevices;
//
//    private BTListActivity activity;
//    private Snackbar alert;
//    private BluetoothAdapter mBluetoothAdapter;
//    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
//    private BTDeviceListAdapter adapter;
//
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            Log.v(TAG, "onReceive");
//            String action = intent.getAction();
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                Log.v(TAG, "DEVICE DESCOVERY STARTED");
//            }
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                Log.v(TAG, "DEVICE DESCOVERY ENDED");
//                alert.dismiss();
//
//                activity.updateList();
//            }
//
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                if (!devices.contains(device)) {
//                    devices.add(device);
//                }
//            }
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_btlist);
//
//        ButterKnife.bind(this);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        activity = this;
//
//        alert = Snackbar.make(container, "Scanning for Bluetooth devices.", Snackbar.LENGTH_INDEFINITE);
//        alert.show();
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//
//        Log.v(TAG, "pairedDevices=" + pairedDevices.size());
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress();
//
//                devices.add(device);
//
//                Log.v(TAG, "deviceName=" + deviceName + "; deviceHWAddress=" + deviceHardwareAddress);
//            }
//        }
//
//        this.discoverBluetoothDevices();
//    }
//
//    private void updateList() {
//        Log.v(TAG, "Found BT devices");
//        for (BluetoothDevice device : devices) {
//            String deviceName = device.getName();
//            String deviceHardwareAddress = device.getAddress();
//            BluetoothClass cls = device.getBluetoothClass();
//
//            Log.v(TAG, "Found BT device");
//            Log.v(TAG, "deviceName=" + deviceName + "; deviceHWAddress=" + deviceHardwareAddress);
//            Log.v(TAG, cls.toString());
//        }
//
//        adapter = new BTDeviceListAdapter(this, devices);
//        btListDevices.setAdapter(adapter);
//        btListDevices.setClickable(true);
//        btListDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent result = new Intent();
//                BluetoothDevice device = devices.get(position);
//                result.putExtra("BluetoothDevice", device);
//
//                setResult(RESULT_OK, result);
//
//                finish();
//            }
//        });
//    }
//
//    private void discoverBluetoothDevices() {
//        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//
//        registerReceiver(mReceiver, filterStart);
//        registerReceiver(mReceiver, filterFound);
//        registerReceiver(mReceiver, filterFinish);
//
//        if (mBluetoothAdapter.isDiscovering()) {
//            mBluetoothAdapter.cancelDiscovery();
//        }
//
//        boolean startedDiscovery = mBluetoothAdapter.startDiscovery();
//        if (!startedDiscovery) {
//            Log.v(TAG, "Unable to start discovery process.");
//        } else {
//            Log.v(TAG, "Started discovery process.");
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        if (mBluetoothAdapter.isDiscovering()) {
//            mBluetoothAdapter.cancelDiscovery();
//        }
//
//        unregisterReceiver(mReceiver);
//    }
}
