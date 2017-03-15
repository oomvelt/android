package com.oomvelt.oomvelt

import android.app.Activity
import android.app.AlertDialog
import android.content.Context

object Util {
    var REQUEST_BT_ENABLE = 2
    var REQUEST_PERMISSION_LOCATION = 3

    fun showAlert(activity: Activity, title: String, message: String) {
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    fun preferenceSave(context: Activity, name: String, value: String) {
        val sharedPref = context.getSharedPreferences("com.oomvelt.oomvelt.PREFERENCES", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(name, value)
        editor.commit()
    }

    fun preferenceLoad(context: Activity, name: String): String? {
        val sharedPref = context.getSharedPreferences("com.oomvelt.oomvelt.PREFERENCES", Context.MODE_PRIVATE)
        return sharedPref.getString(name, null)
    }
}
