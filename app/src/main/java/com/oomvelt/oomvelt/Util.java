package com.oomvelt.oomvelt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Util {
    public static int REQUEST_BT_ENABLE = 2;
    public static int REQUEST_PERMISSION_LOCATION = 3;
    public static int REQUEST_BT_DEVICE = 4;

    public static void showAlert(Activity activity, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
