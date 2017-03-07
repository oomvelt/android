package com.oomvelt.oomvelt.bluetooth;

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import com.oomvelt.oomvelt.Util

class BTHelper(activity: Activity) {
    val adapter = getBluetoothAdapter()
    var activity = activity

    fun getBluetoothAdapter(): BluetoothAdapter {
        return BluetoothAdapter.getDefaultAdapter()
    }

    fun checkIfEnabled(callback: () -> Void) {
        if (!adapter.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, Util.REQUEST_BT_ENABLE)
        } else {
            callback()
        }
    }
}