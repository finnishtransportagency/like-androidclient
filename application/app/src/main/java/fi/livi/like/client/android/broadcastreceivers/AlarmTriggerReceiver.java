package fi.livi.like.client.android.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AlarmTriggerReceiver extends BroadcastReceiver {

    public final static String BROADCAST_ACTION_ID = "fi.livi.like.client.android.alarm";

    private final Listener listener;

    public interface Listener {
        void onAlarmTriggered();
    }

    public AlarmTriggerReceiver(Context context, Listener listener) {
        this.listener = listener;
    }

    public void register(Context context) {
        context.registerReceiver(this, new IntentFilter(BROADCAST_ACTION_ID));
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onAlarmTriggered();
    }
}
