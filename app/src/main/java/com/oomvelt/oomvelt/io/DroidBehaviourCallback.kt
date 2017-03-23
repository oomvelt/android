package com.oomvelt.oomvelt.io
import org.jetbrains.anko.info
import org.jetbrains.anko.AnkoLogger

import raticator.RatState
import raticator.StateChangeCallback


class DroidBehaviourCallback : StateChangeCallback, AnkoLogger {

    override fun StateChanged(newState: RatState) {
        // TODO: make this do something on the UI thread now!
        info("----------------------------------------------------------")
        info("Rat state change has been detected: " + newState.toString())
        info("----------------------------------------------------------")
    }
}