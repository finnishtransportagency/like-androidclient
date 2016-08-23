package fi.livi.like.client.android.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fi.livi.like.client.android.background.tracking.TrackingStateMachine;

public class StateChangeBroadcastReceiver extends BroadcastReceiver {

    private final Listener listener;

    public interface Listener {
        void onTrackingStateChanged(TrackingStateMachine.State newState);
    }

    public StateChangeBroadcastReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String newState = intent.getStringExtra(TrackingStateMachine.BROADCAST_KEY_ID);
        if (newState != null && listener != null) {
            listener.onTrackingStateChanged(TrackingStateMachine.State.valueOf(newState));
        }
    }
}
