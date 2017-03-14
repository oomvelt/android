package com.oomvelt.oomvelt.bluetooth;

import android.app.Activity
import android.app.IntentService
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.oomvelt.oomvelt.MainActivity
import com.oomvelt.oomvelt.Util
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class BTHelper : AnkoLogger {
    val adapter = getBluetoothAdapter()

    interface DiscoveryCallback {
        fun discoveryEnded(devices: ArrayList<BluetoothDevice>)
    }

    fun getBluetoothAdapter(): BluetoothAdapter {
        return BluetoothAdapter.getDefaultAdapter()
    }

    fun checkIfEnabled(activity: Activity, callback: () -> Unit) {
        if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, Util.REQUEST_BT_ENABLE)
        } else {
            callback()
        }
    }

    fun discoverDevices(service: Service, callback: DiscoveryCallback) {
        info { "Discovery starting" }
        val filterFound = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val filterFinish = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        var devices = ArrayList<BluetoothDevice>()

        val mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, bIntent: Intent) {
                val action = bIntent.action

                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    info { "Finished bt discovery" }
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this)

                    callback.discoveryEnded(devices)
                }

                if (BluetoothDevice.ACTION_FOUND == action) {
                    info { "Device bt discovery" }
                    val device = bIntent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    if (!devices.contains(device)) {
                        devices.add(device)
                    }
                }
            }
        }

        info { "Adding paired devices" }
        val pairedDevices = adapter.bondedDevices
        devices.addAll(pairedDevices)

        service.registerReceiver(mReceiver, filterFound)
        service.registerReceiver(mReceiver, filterFinish)

        if (adapter.isDiscovering) {
            info { "Cancel bt discovery" }
            adapter.cancelDiscovery()
        }

        info { "Start bt discovery" }
        val startedDiscovery = adapter.startDiscovery()
        if (!startedDiscovery) {
            info { "Can't start bt discovery" }
            service.unregisterReceiver(mReceiver)

            callback.discoveryEnded(devices)
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