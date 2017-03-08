package com.oomvelt.oomvelt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.afollestad.materialdialogs.MaterialDialog
import com.oomvelt.oomvelt.bluetooth.BTHelper
import com.oomvelt.oomvelt.bluetooth.BTDeviceListAdapter

import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    @BindView(R.id.bluetooth_choose) lateinit var bluetoothChoose: Button
    @BindView(R.id.bluetooth_connect) lateinit var bluetoothConnect: Button

    @BindView(R.id.bluetooth_status) lateinit var bluetoothStatus: TextView
    @BindView(R.id.bluetooth_log) lateinit var bluetoothLog: EditText

    @BindString(R.string.error_bt_no_title) lateinit var errorBtNoTitle: String
    @BindString(R.string.error_bt_no_message) lateinit var errorBtNoMessage: String
    @BindString(R.string.error_bt_disabled_title) lateinit var errorBtDisabledTitle: String
    @BindString(R.string.error_bt_disabled_message) lateinit var errorBtDisabledMessage: String
    @BindString(R.string.error_bt_connect_title) lateinit var errorBtConnectTitle: String
    @BindString(R.string.error_bt_connect_message) lateinit var errorBtConnectMessage: String

    @BindString(R.string.main_bluetooth_devices_title) lateinit var bluetoothDevicesTitle: String
    @BindString(R.string.main_bluetooth_devices_loading) lateinit var bluetoothDevicesLoading: String

    @BindString(R.string.main_bluetooth_status_device) lateinit var mainBtStatusDevice: String
    @BindString(R.string.main_bluetooth_status_none) lateinit var mainBtStatusNone: String

    private lateinit var btHelper: BTHelper

    // Bluetooth related
    private var btDevice : BluetoothDevice? = null

    // UI
    private var btProgressDialog : MaterialDialog? = null
    private var btListDialog: MaterialDialog? = null

    private val btDebviceCallback = BTDeviceListAdapter.ItemCallback { device -> bluetoothDeviceSelected(device) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        btHelper = BTHelper(this)
    }

    override fun onResume() {
        super.onResume()

        this.uiBluetoothDisabled()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

                requestPermissions(permissions, Util.REQUEST_PERMISSION_LOCATION)
            } else {
                this.checkIfBluetoothIsEnabled()
            }
        } else {
            this.checkIfBluetoothIsEnabled()
        }
    }

    @OnClick(R.id.bluetooth_connect)
    fun handlerBluetoothConnect() {
        var socket: BluetoothSocket? = btHelper.connectToDevice(btDevice)

        if (socket == null) {
            Util.showAlert(this, errorBtConnectTitle, String.format(errorBtConnectMessage, if (btDevice!!.name != null) btDevice!!.name else btDevice!!.address))
            return
        }

        bluetoothLog.setText("", TextView.BufferType.NORMAL)

        var iStream: InputStream = socket!!.inputStream
        val br = BufferedReader(InputStreamReader(iStream!!))
        var line: String = ""

        line += br.readLine()
        line += "\n";
        line += br.readLine()
        line += "\n";
        line += br.readLine()
        line += "\n";

        bluetoothLog.setText(line, TextView.BufferType.NORMAL)

        br.close();
        iStream.close();
        socket.close();
    }

    @OnClick(R.id.bluetooth_choose)
    fun handlerBluetoothChoose() {
        uiShowLoading(true)

        btHelper.discoverDevices {
            uiShowLoading(false)
            uiShowList()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.v(TAG, "Callback")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && requestCode == Util.REQUEST_PERMISSION_LOCATION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.checkIfBluetoothIsEnabled()
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Util.REQUEST_PERMISSION_LOCATION)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == Util.REQUEST_BT_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                this.uiBluetoothEnabled()
            } else {
                Util.showAlert(this, errorBtDisabledTitle, errorBtDisabledMessage)
            }
        }
    }

    private fun checkIfBluetoothIsEnabled() {
        btHelper.checkIfEnabled { uiBluetoothEnabled() }
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
            val device: BluetoothDevice? = btHelper.getDeviceByAddress(address)
            if (device != null) {
                bluetoothDeviceSelected(device)
            }
        }
    }

    private fun uiBluetoothDeviceSelected() {
        bluetoothStatus.text = String.format(mainBtStatusDevice, if (btDevice!!.name != null) btDevice!!.name else btDevice!!.address)
        bluetoothConnect.isEnabled = true
    }

    private fun uiShowLoading(status: Boolean) {
        if (btProgressDialog == null) {
            btProgressDialog = MaterialDialog.Builder(this)
                .title(bluetoothDevicesTitle)
                .content(bluetoothDevicesLoading)
                .progress(true, 0)
                .build()
        }

        if (status) {
            btProgressDialog!!.show()
        } else {
            btProgressDialog!!.dismiss()
        }
    }

    private fun uiShowList() {
        var adapter : BTDeviceListAdapter = BTDeviceListAdapter(btHelper.devices)
        adapter.setCallback(btDebviceCallback)

        btListDialog = MaterialDialog.Builder(this)
                .title(bluetoothDevicesTitle)
                .adapter(adapter, null)
                .show()
    }

    private fun bluetoothDeviceSelected(device: BluetoothDevice) {
        btDevice = device
        Util.preferenceSave(this, "deviceAddress", btDevice!!.address)

        if (btListDialog != null) btListDialog!!.dismiss()

        uiBluetoothDeviceSelected()
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
