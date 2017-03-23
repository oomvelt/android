package com.oomvelt.oomvelt.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import android.support.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import com.oomvelt.oomvelt.io.SocketReaderRunnable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import raticator.RatState;
import raticator.StateChangeCallback;

public class BluetoothService extends Service {
  public static final String TAG = "BluetoothService";
  public static final String INTENT_EVENT_NAME = "OOMVELT_BLUETOOTH_SERVICE";
  public static final String OPERATION = "operation";
  public static final String DEVICE = "device";
  private static final String FILE_DIRECTORY = "oomvelt";

  public static final int OPERATION_DISCOVER_DEVICES = 0;
  public static final int OPERATION_FILE_SAVE_START = 1;
  public static final int OPERATION_FILE_SAVE_STOP = 2;
  public static final int OPERATION_NEW_STATE = 3;

  private Service mService;
  private SocketReaderRunnable mSocketReader;
  private BluetoothSocket mBluetoothSocket;
  private BluetoothHelper mBluetoothHelper;
  private File mFileToWrite;
  private boolean mSaving = false;

  public BluetoothService() {
    mService = this;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public void onStateChange(RatState newState) {
    Intent intent = new Intent(INTENT_EVENT_NAME);
    intent.putExtra("state", newState.toString());
    intent.putExtra(OPERATION, OPERATION_NEW_STATE);
    LocalBroadcastManager.getInstance(mService).sendBroadcast(intent);
  }

  private void sendError(int operation, String error) {
    Intent intent = new Intent(INTENT_EVENT_NAME);
    intent.putExtra("error", true);
    intent.putExtra("errorMessage", error);
    intent.putExtra(OPERATION, operation);
    LocalBroadcastManager.getInstance(mService).sendBroadcast(intent);
  }

  private void operationDiscoverDevices() {
    BluetoothHelper.DiscoveryCallback discoveryCallback =
        new BluetoothHelper.DiscoveryCallback() {
          @Override public void discoveryEnded(@NotNull ArrayList<BluetoothDevice> devices) {
            Log.i(TAG, "Found " + devices.size() + " bluetooth devices.");

            for (BluetoothDevice device : devices) {
              Log.i(TAG, "Device" + device.getName() + " - " + device.getAddress() + ".");
            }

            Intent intent = new Intent(INTENT_EVENT_NAME);
            intent.putExtra("devices", devices);
            intent.putExtra(OPERATION, OPERATION_DISCOVER_DEVICES);
            LocalBroadcastManager.getInstance(mService).sendBroadcast(intent);
          }
        };

    mBluetoothHelper.discoverDevices(mService, discoveryCallback);
  }

  private void operationFileSaveStart(Intent intent) {
    BluetoothDevice device = intent.getExtras().getParcelable(DEVICE);
    mBluetoothSocket = mBluetoothHelper.connectToDevice(device);

    if (mBluetoothSocket == null) {
      sendError(OPERATION_FILE_SAVE_START, "Could not connect to Bluetooth device.");
    } else {
      String fileDirectory;

      if (Environment.getExternalStorageState().startsWith(Environment.MEDIA_MOUNTED)) {
        fileDirectory = Environment.getExternalStorageDirectory()
            + File.separator
            + Environment.DIRECTORY_DOWNLOADS;
      } else {
        fileDirectory = Environment.getDataDirectory() + File.separator + FILE_DIRECTORY;
      }

      File folder = new File(fileDirectory);

      if (!folder.exists()) {
        if (!folder.mkdirs()) {
          sendError(OPERATION_FILE_SAVE_START,
              "Could not create the directory " + folder.getAbsolutePath() + ".");
          return;
        }
      }

      mFileToWrite = new File(fileDirectory,
          FILE_DIRECTORY + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log");

      try {
        if (mSocketReader != null) {
          mSocketReader.stop();
        }

        mSocketReader = new SocketReaderRunnable(mBluetoothSocket, this, mFileToWrite);
        mSocketReader.start();

        Intent resultIntent = new Intent(INTENT_EVENT_NAME);
        resultIntent.putExtra("file", mFileToWrite.getAbsolutePath());
        resultIntent.putExtra(OPERATION, OPERATION_FILE_SAVE_START);
        LocalBroadcastManager.getInstance(mService).sendBroadcast(resultIntent);
        mSaving = true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void operationFileSaveStop() {
    mSaving = false;

    try {
      mSocketReader.stop();
      mBluetoothSocket.close();

      Intent resultIntent = new Intent(INTENT_EVENT_NAME);
      resultIntent.putExtra(OPERATION, OPERATION_FILE_SAVE_STOP);
      LocalBroadcastManager.getInstance(mService).sendBroadcast(resultIntent);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    int task = intent.getIntExtra(OPERATION, 0);
    mBluetoothHelper = new BluetoothHelper();

    if (task == OPERATION_DISCOVER_DEVICES) {
      operationDiscoverDevices();
    }

    if (task == OPERATION_FILE_SAVE_START) {
      operationFileSaveStart(intent);
    }

    if (task == OPERATION_FILE_SAVE_STOP && mSaving) {
      operationFileSaveStop();
    }

    return Service.START_STICKY;
  }
}
