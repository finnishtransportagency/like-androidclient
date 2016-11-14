package fi.livi.like.client.android.backgroundservice.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import org.slf4j.LoggerFactory;

/**
 * BackgroundServiceHandler
 */
public class BackgroundServiceHandler {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(BackgroundServiceHandler.class);

    private Callback callbackActivity;
    protected Activity activity;
    protected ServiceConnection connection;

    public interface Callback {
        Intent createServiceIntent();
    }

    public BackgroundServiceHandler() {
        // When using empty constructor, you need to set activity and connection via setters!
    }

    public BackgroundServiceHandler(Activity activity, Callback callback, ServiceConnection connection) {
        this.activity = activity;
        this.callbackActivity = callback;
        this.connection = connection;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setConnection(ServiceConnection connection) {
        this.connection = connection;
    }

    public void setCallback(Callback callback) {
        this.callbackActivity = callback;
    }

    public boolean bindService() {
        log.info("bind service");
        return activity.bindService(getServiceIntentFromActivity(), connection, Context.BIND_AUTO_CREATE);
    }

    public void startService() {
        log.info("start service");
        activity.startService(getServiceIntentFromActivity());
    }

    public boolean startAndBindBackgroundService() {
        boolean isBound = bindService();// needs to be in this order to keep service running on bg
        startService();
        return isBound;
    }

    public void stopAndUnbindBackgroundService() {
        activity.stopService(getServiceIntentFromActivity());// needs to be in this order; stop->unbind
        unbindService();
    }

    public void unbindService() {
        activity.unbindService(connection);
        log.info("unbind service");
    }

    protected Intent getServiceIntentFromActivity() {
        return callbackActivity.createServiceIntent();
    }
}
