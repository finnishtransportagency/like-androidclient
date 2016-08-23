package fi.livi.like.client.android.background.service;

import android.content.Context;
import android.os.PowerManager;

/**
 * WakeLockHandler provides means to keep certain parts (CPU, display) alive.
 */
public class WakeLockHandler {

    private PowerManager.WakeLock wakeLock;
    private Context context;

    public WakeLockHandler(Context context) {
        this.context = context;
    }

    public void acquireWakelock(int wakeLockMode) {
        releaseWakeLock();
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(wakeLockMode, "BackgroundService WakeLock");
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    public void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
