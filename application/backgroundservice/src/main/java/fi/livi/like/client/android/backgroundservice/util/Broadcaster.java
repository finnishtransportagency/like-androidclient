package fi.livi.like.client.android.backgroundservice.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class Broadcaster {

    private static Broadcaster instance;
    private static Context context;

    protected Broadcaster(Context context) {
        Broadcaster.context = context;
    }

    public synchronized static void prepareSingleton(Context context) {
        if (instance == null) {
            instance = new Broadcaster(context);
        }
    }

    public synchronized static Broadcaster getInstance() {
        return instance;
    }

    public synchronized void broadcastLocal(String broadcastActionId, String key, String value) {
        Intent intent = new Intent(broadcastActionId);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
