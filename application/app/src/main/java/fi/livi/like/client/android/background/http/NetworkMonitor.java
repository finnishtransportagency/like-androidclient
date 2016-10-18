package fi.livi.like.client.android.background.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkMonitor {

    private static final String NETWORK_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private final Listener listener;
    private NetworkStateReceiver networkStateReceiver;

    class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                    && NetworkMonitor.isNetworkAvailable(context) && listener != null) {
                unregisterNetworkStateListener(context);
                listener.onNetworkAvailable();
            }
        }
    }

    public interface Listener {
        void onNetworkAvailable();
    }

    public NetworkMonitor(Listener listener) {
        this.listener = listener;
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void registerNetworkStateListener(Context context) {
        if (networkStateReceiver == null) {
            networkStateReceiver = new NetworkStateReceiver();
            context.registerReceiver(networkStateReceiver, new IntentFilter(NETWORK_CONNECTIVITY_CHANGE));
        }
    }

    public void unregisterNetworkStateListener(Context context) {
        if (networkStateReceiver != null) {
            context.unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
    }
}
