package com.oomvelt.oomvelt.io

import raticator.RatState
import raticator.StateChangeCallback


class DroidBehaviourCallback : StateChangeCallback {

    override fun StateChanged(newState: RatState) {
        // TODO: make this do something on the UI thread now!
        print("Rat state change has been detected: " + newState.toString())
    }
}