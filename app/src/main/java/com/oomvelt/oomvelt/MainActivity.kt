package com.oomvelt.oomvelt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.afollestad.materialdialogs.MaterialDialog
import com.oomvelt.oomvelt.bluetooth.BluetoothHelper
import com.oomvelt.oomvelt.ui.BluetoothDeviceAdapter

import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.oomvelt.oomvelt.bluetooth.BluetoothService
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop

class MainActivity : AppCompatActivity() {
  @BindView(R.id.bluetooth_choose) lateinit var bluetoothChoose: Button
  @BindView(R.id.bluetooth_connect) lateinit var bluetoothConnect: Button

  @BindView(R.id.bluetooth_status) lateinit var bluetoothStatus: TextView

  @BindView(R.id.activity_main) lateinit var mainView: CoordinatorLayout

  @BindString(R.string.error_bt_no_title) lateinit var errorBtNoTitle: String
  @BindString(R.string.error_bt_no_message) lateinit var errorBtNoMessage: String
  @BindString(R.string.error_bt_disabled_title) lateinit var errorBtDisabledTitle: String
  @BindString(R.string.error_bt_disabled_message) lateinit var errorBtDisabledMessage: String
  @BindString(R.string.error_bt_connect_title) lateinit var errorBtConnectTitle: String
  @BindString(R.string.error_bt_connect_message) lateinit var errorBtConnectMessage: String

  @BindString(R.string.main_bluetooth_read_start) lateinit var buttonReadStart: String
  @BindString(R.string.main_bluetooth_read_end) lateinit var buttonReadStop: String
  @BindString(R.string.main_bluetooth_read_status) lateinit var readStatus: String

  @BindString(R.string.main_bluetooth_devices_title) lateinit var bluetoothDevicesTitle: String
  @BindString(R.string.main_bluetooth_devices_loading) lateinit var bluetoothDevicesLoading: String

  @BindString(R.string.main_bluetooth_status_device) lateinit var mainBtStatusDevice: String
  @BindString(R.string.main_bluetooth_status_none) lateinit var mainBtStatusNone: String

  private lateinit var mBluetoothHelper: BluetoothHelper
  private lateinit var mActivity: Activity

  // Bluetooth related
  private var mBluetoothDevice: BluetoothDevice? = null

  // UI
  private var mBluetoothProgressDialog: MaterialDialog? = null
  private var mBluetoothListDialog: MaterialDialog? = null
  private var mWriteStatusSnackbar: Snackbar? = null

  // Bluetooth service receiver
  private val mBluetoothReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val operation = intent.getIntExtra(BluetoothService.OPERATION, 0)

      if (BluetoothService.OPERATION_FILE_SAVE_START == operation) {
        if (mWriteStatusSnackbar != null) {
          mWriteStatusSnackbar!!.dismiss()
        }

        val file: String = intent.getStringExtra("file")

        mWriteStatusSnackbar = Snackbar.make(mainView, String.format(readStatus, file),
            Snackbar.LENGTH_INDEFINITE)
        mWriteStatusSnackbar!!.show()
      }

      if (BluetoothService.OPERATION_FILE_SAVE_STOP == operation) {
        if (mWriteStatusSnackbar != null) {
          mWriteStatusSnackbar!!.dismiss()
        }
      }

      if (BluetoothService.OPERATION_DISCOVER_DEVICES == operation) {
        mBluetoothProgressDialog!!.dismiss()

        val devices: ArrayList<BluetoothDevice> = intent.extras.getParcelableArrayList("devices")
        val adapter: BluetoothDeviceAdapter = BluetoothDeviceAdapter(devices)
        adapter.setCallback(btDeviceCallback)

        mBluetoothListDialog = MaterialDialog.Builder(mActivity)
            .title(bluetoothDevicesTitle)
            .adapter(adapter, null)
            .show()
      }
    }
  }

  private val btDeviceCallback = BluetoothDeviceAdapter.ItemCallback { device ->
    mBluetoothListDialog!!.dismiss(); bluetoothDeviceSelected(device)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)

    mActivity = this
    mBluetoothHelper = BluetoothHelper()
  }

  override fun onResume() {
    super.onResume()

    LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, IntentFilter(
        BluetoothService.INTENT_EVENT_NAME))

    this.uiBluetoothDisabled()

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      if (checkSelfPermission(
          Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermissions(permissions, Util.REQUEST_PERMISSION_LOCATION)
      } else {
        this.checkIfBluetoothIsEnabled()
      }
    } else {
      this.checkIfBluetoothIsEnabled()
    }
  }

  override fun onPause() {
    super.onPause()

    LocalBroadcastManager.getInstance(this).unregisterReceiver(mBluetoothReceiver)

    stopService(intentFor<BluetoothService>())
  }

  @OnClick(R.id.bluetooth_connect)
  fun handlerBluetoothConnect() {
    // Need a better way to handle this
    if (bluetoothConnect.text == buttonReadStart) {
      startService(intentFor<BluetoothService>(
          BluetoothService.OPERATION to BluetoothService.OPERATION_FILE_SAVE_START,
          BluetoothService.DEVICE to mBluetoothDevice).singleTop())
      bluetoothConnect.text = buttonReadStop
    } else {
      startService(intentFor<BluetoothService>(
          BluetoothService.OPERATION to BluetoothService.OPERATION_FILE_SAVE_STOP).singleTop())
      bluetoothConnect.text = buttonReadStart
    }
  }

  @OnClick(R.id.bluetooth_choose)
  fun handlerBluetoothChoose() {
    uiShowLoading(true)

    startService(intentFor<BluetoothService>(
        BluetoothService.OPERATION to BluetoothService.OPERATION_DISCOVER_DEVICES).singleTop())
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    Log.v(TAG, "Callback")
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && requestCode == Util.REQUEST_PERMISSION_LOCATION) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        this.checkIfBluetoothIsEnabled()
      } else {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Util.REQUEST_PERMISSION_LOCATION)
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == Util.REQUEST_BT_ENABLE) {
      if (resultCode == Activity.RESULT_OK) {
        this.uiBluetoothEnabled()
      } else {
        Util.showAlert(this, errorBtDisabledTitle, errorBtDisabledMessage)
      }
    }
  }

  private fun checkIfBluetoothIsEnabled() {
    mBluetoothHelper.checkIfEnabled(this) { uiBluetoothEnabled() }
  }

  private fun uiBluetoothDisabled() {
    bluetoothStatus.text = mainBtStatusNone
    bluetoothChoose.isEnabled = false
    bluetoothConnect.visibility = View.INVISIBLE
  }

  private fun uiBluetoothEnabled() {
    bluetoothChoose.isEnabled = true
    bluetoothConnect.visibility = View.VISIBLE
    bluetoothConnect.isEnabled = false

    val address: String? = Util.preferenceLoad(this, "deviceAddress")
    if (address != null) {
      val device: BluetoothDevice? = mBluetoothHelper.getDeviceByAddress(address)
      if (device != null) {
        bluetoothDeviceSelected(device)
      }
    }
  }

  private fun uiBluetoothDeviceSelected() {
    bluetoothStatus.text = String.format(mainBtStatusDevice,
        if (mBluetoothDevice!!.name != null) mBluetoothDevice!!.name else mBluetoothDevice!!.address)
    bluetoothConnect.isEnabled = true
  }

  private fun uiShowLoading(status: Boolean) {
    if (mBluetoothProgressDialog == null) {
      mBluetoothProgressDialog = MaterialDialog.Builder(this)
          .title(bluetoothDevicesTitle)
          .content(bluetoothDevicesLoading)
          .progress(true, 0)
          .build()
    }

    if (status) {
      mBluetoothProgressDialog!!.show()
    } else {
      mBluetoothProgressDialog!!.dismiss()
    }
  }

  private fun bluetoothDeviceSelected(device: BluetoothDevice) {
    mBluetoothDevice = device
    Util.preferenceSave(this, "deviceAddress", mBluetoothDevice!!.address)

    if (mBluetoothListDialog != null) mBluetoothListDialog!!.dismiss()

    uiBluetoothDeviceSelected()
  }

  companion object {
    private val TAG = "MainActivity"
  }
}
