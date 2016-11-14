package fi.livi.like.client.android.backgroundservice.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fi.livi.like.client.android.backgroundservice.user.UserManager;

public class UserManagerReceiver extends BroadcastReceiver {

    private final Listener listener;

    public interface Listener {
        void onPinCodeRequest();
    }

    public UserManagerReceiver(Listener listener) {
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String value = intent.getStringExtra(UserManager.REQUEST_PIN_CODE_KEY);
        if (UserManager.REQUEST_PIN_CODE_VALUE.equals(value) && listener != null) {
            listener.onPinCodeRequest();
        }
    }
}
