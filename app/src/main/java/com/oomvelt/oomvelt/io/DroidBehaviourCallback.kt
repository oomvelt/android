package com.oomvelt.oomvelt.io

import android.util.Log
import com.oomvelt.oomvelt.bluetooth.BluetoothService
import raticator.RatState
import raticator.StateChangeCallback


class DroidBehaviourCallback : StateChangeCallback {
  private val mBluetoothService: BluetoothService

  constructor(service: BluetoothService): super() {
    mBluetoothService = service
  }

  override fun StateChanged(newState: RatState) {
    Log.i("BEHAVIOUR", "Rat state change has been detected: " + newState.toString())
    mBluetoothService.onStateChange(newState)
  }
}