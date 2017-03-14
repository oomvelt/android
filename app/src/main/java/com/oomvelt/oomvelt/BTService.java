package com.oomvelt.oomvelt;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.oomvelt.oomvelt.bluetooth.BTHelper;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BTService extends Service {
    public static final String TAG = "BTService";
    public static final String INTENT_EVENT_NAME = "OOMVELT_BLUETOOTH_SERVICE";
    public static final String OPERATION = "operation";
    public static final String DEVICE = "device";

    public static final int OPERATION_DISCOVER_DEVICES = 0;
    public static final int OPERATION_FILE_SAVE_START = 1;
    public static final int OPERATION_FILE_SAVE_STOP = 2;

    private Service service;
    private SocketReader socketReader;

    public BTService() {
        service = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendError(int operation, String error) {
        Intent intent = new Intent(INTENT_EVENT_NAME);
        intent.putExtra("error", true);
        intent.putExtra("errorMessage", error);
        intent.putExtra(OPERATION, operation);
        LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service starting");

        BTHelper btHelper = new BTHelper();
        int task = intent.getIntExtra(OPERATION, 0);

        if (task == OPERATION_DISCOVER_DEVICES) {
            BTHelper.DiscoveryCallback discoveryCallback = new BTHelper.DiscoveryCallback() {
                @Override
                public void discoveryEnded(@NotNull ArrayList<BluetoothDevice> devices) {
                    Log.i(TAG, "Found " + devices.size() + " bluetooth devices.");

                    for (BluetoothDevice device : devices) {
                        Log.i(TAG, "Device" + device.getName() + " - " + device.getAddress() + ".");
                    }

                    Intent intent = new Intent(INTENT_EVENT_NAME);
                    intent.putExtra("devices", devices);
                    intent.putExtra(OPERATION, OPERATION_DISCOVER_DEVICES);
                    LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
                }
            };

            btHelper.discoverDevices(service, discoveryCallback);
        }

        if (task == OPERATION_FILE_SAVE_START) {
            BluetoothDevice device = intent.getExtras().getParcelable(DEVICE);
            BluetoothSocket socket = btHelper.connectToDevice(device);

            if (socket == null) {
                sendError(task, "Could not connect to Bluetooth device.");
            } else {
                String fileDirectory;

                if (Environment.getExternalStorageState().startsWith(Environment.MEDIA_MOUNTED)) {
                    fileDirectory = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;
                } else {
                    fileDirectory = Environment.getDataDirectory() + File.separator + "oomvelt";
                }

                File folder = new File(fileDirectory);

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                File file = new File(fileDirectory, "oomvelt" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log");

                try {
                    if (socketReader != null) {
                        socketReader.stop();
                    }

                    socketReader = new SocketReader(socket, file);
                    socketReader.start();

                    Intent resultIntent = new Intent(INTENT_EVENT_NAME);
                    resultIntent.putExtra("file", file.getAbsolutePath());
                    resultIntent.putExtra(OPERATION, OPERATION_FILE_SAVE_START);
                    LocalBroadcastManager.getInstance(service).sendBroadcast(resultIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (task == OPERATION_FILE_SAVE_STOP && socketReader != null) {
            try {
                socketReader.stop();

                Intent resultIntent = new Intent(INTENT_EVENT_NAME);
                resultIntent.putExtra(OPERATION, OPERATION_FILE_SAVE_STOP);
                LocalBroadcastManager.getInstance(service).sendBroadcast(resultIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Service.START_STICKY;
    }
}
