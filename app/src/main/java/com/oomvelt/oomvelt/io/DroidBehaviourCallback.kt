package com.oomvelt.oomvelt.io
import org.jetbrains.anko.info
import org.jetbrains.anko.AnkoLogger

import com.oomvelt.oomvelt.bluetooth.BluetoothService
import raticator.RatState
import raticator.StateChangeCallback


class DroidBehaviourCallback : StateChangeCallback, AnkoLogger {
  private val mBluetoothService: BluetoothService

  constructor(service: BluetoothService): super() {
    mBluetoothService = service
  }

  override fun StateChanged(newState: RatState) {
    info("----------------------------------------------------------")
    info("Rat state change has been detected: " + newState.toString())
    info("----------------------------------------------------------")
    mBluetoothService.onStateChange(newState)
  }
}