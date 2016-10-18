package fi.livi.like.client.android.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import fi.livi.like.client.android.background.tracking.TrackingDisabledHandler;
import fi.livi.like.client.android.background.tracking.TrackingStateMachine;

public class StateChangeReceiver extends BroadcastReceiver {

    private final Listener listener;

    public interface Listener {
        void onTrackingStateChanged(TrackingStateMachine.State newState, Date disabledTimeStarted, Date disabledTimeEnds, int disabledTimeInMs);
}

    public StateChangeReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String newState = intent.getStringExtra(TrackingStateMachine.BROADCAST_KEY_ID);
        if (newState != null && listener != null) {
            final int disabledTimeInMs = intent.getIntExtra(TrackingDisabledHandler.DISABLED_WAIT_TIME, -1);
            final Date disabledTimeStarted = (Date) intent.getSerializableExtra(TrackingDisabledHandler.DISABLED_WAIT_STARTED);
            final Date disabledTimeEnds = (Date) intent.getSerializableExtra(TrackingDisabledHandler.DISABLED_WAIT_ENDS);
            listener.onTrackingStateChanged(TrackingStateMachine.State.valueOf(newState), disabledTimeStarted, disabledTimeEnds, disabledTimeInMs);
        }
    }
}
