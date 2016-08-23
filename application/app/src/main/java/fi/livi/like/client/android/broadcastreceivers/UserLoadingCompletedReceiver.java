package fi.livi.like.client.android.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fi.livi.like.client.android.background.http.HttpLoader;

public class UserLoadingCompletedReceiver extends BroadcastReceiver {

    private final Listener listener;

    public interface Listener {
        void onUserLoadingCompleted();
        void onUserLoadingFailed();
    }

    public UserLoadingCompletedReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (HttpLoader.BROADCAST_ACTION_ID.equals(intent.getAction())) {
            String notifyTypeStringForUser = intent.getStringExtra(HttpLoader.OperationType.USER_LOADING.toString());
            if (HttpLoader.NOTIFY_COMPLETED_ID.equals(notifyTypeStringForUser)) {
                listener.onUserLoadingCompleted();
            } else if (HttpLoader.NOTIFY_FAILED_ID.equals(notifyTypeStringForUser)) {
                listener.onUserLoadingFailed();
            }
        }
    }
}