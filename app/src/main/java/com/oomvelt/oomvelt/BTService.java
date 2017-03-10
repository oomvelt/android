package com.oomvelt.oomvelt;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class BTService extends IntentService {

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public BTService() {
        super("BTService");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        try {
            Log.i("RatBTService", "Boom - intent processed");
            Thread.sleep(5000);
            Log.i("RatBTService", "Boom - intent processed Fini");
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }
}
