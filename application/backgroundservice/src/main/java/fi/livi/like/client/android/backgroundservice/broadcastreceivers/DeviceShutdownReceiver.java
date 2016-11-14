package fi.livi.like.client.android.backgroundservice.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.slf4j.LoggerFactory;

public class DeviceShutdownReceiver extends BroadcastReceiver {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(DeviceShutdownReceiver.class);

    private final static String HTC_ACTION_SHUTDOWN = "android.intent.action.QUICKBOOT_POWEROFF";
    private final Listener listener;

    public interface Listener {
        void onDeviceShutdown();
    }

    public DeviceShutdownReceiver(Listener listener) {
        this.listener = listener;
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(HTC_ACTION_SHUTDOWN);
        context.registerReceiver(this, intentFilter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        log.info("Device shutting down!");
        listener.onDeviceShutdown();
    }
}
