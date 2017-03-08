package com.oomvelt.oomvelt.bluetooth;

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.oomvelt.oomvelt.MainActivity
import com.oomvelt.oomvelt.Util
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class BTHelper(activity: Activity) {
    val adapter = getBluetoothAdapter()
    var activity = activity
    var devices = ArrayList<BluetoothDevice>()

    fun getBluetoothAdapter(): BluetoothAdapter {
        return BluetoothAdapter.getDefaultAdapter()
    }

    fun checkIfEnabled(callback: () -> Unit) {
        if (!adapter.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, Util.REQUEST_BT_ENABLE)
        } else {
            callback()
        }
    }

    fun discoverDevices(callback: () -> Unit) {
        val mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    callback()
                }

                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    if (!devices.contains(device)) {
                        devices.add(device)
                    }
                }
            }
        }

        val pairedDevices = adapter.bondedDevices
        devices.addAll(pairedDevices)

        val filterFound = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filterStart = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val filterFinish = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        activity.registerReceiver(mReceiver, filterStart)
        activity.registerReceiver(mReceiver, filterFound)
        activity.registerReceiver(mReceiver, filterFinish)

        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }

        val startedDiscovery = adapter.startDiscovery()
        if (!startedDiscovery) {
            callback()
        }
    }

    fun connectToDevice(device: BluetoothDevice?) : BluetoothSocket? {
        var socket: BluetoothSocket? = null
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        try {
            socket = device!!.createRfcommSocketToServiceRecord(uuid)
            socket!!.connect()

            return socket
        } catch (e: IOException) {
            return null
        }
    }

    fun getDeviceByAddress(address: String): BluetoothDevice? {
        return adapter.getRemoteDevice(address)
    }
}