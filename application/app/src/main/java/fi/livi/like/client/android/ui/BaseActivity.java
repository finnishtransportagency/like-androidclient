package fi.livi.like.client.android.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.background.service.BackgroundService;
import fi.livi.like.client.android.background.service.ServiceBinder;
import fi.livi.like.client.android.background.googleplayservices.GooglePlayServiceLibraryChecker;


/**
 * BaseActivity has functionality for starting and stopping service. It also provides
 * helpers to check if Google Play Services has been installed on system.
 */
public class BaseActivity extends AppCompatActivity
    implements GooglePlayServiceLibraryChecker.ClientActivity,
        BackgroundServiceHandler.Callback {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(BaseActivity.class);

    protected boolean isBound = false;
    protected GooglePlayServiceLibraryChecker googlePlayServiceLibraryChecker;
    protected BackgroundServiceHandler backgroundServiceHandler;
    protected BackgroundService backgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backgroundServiceHandler = new BackgroundServiceHandler(this, this, connection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BackgroundService.isSystemShuttingDown) {
            stopAndUnbindBackgroundService();
        } else if (BackgroundService.isRunning) {
            unbindService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BackgroundService.isRunning) {
            bindService();
        }
    }

    // SERVICE HANDLING

    protected ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            log.info("onServiceConnected");
            onBackgroundServiceConnected(className, service);
        }
        public void onServiceDisconnected(ComponentName className) {
            log.info("onServiceDisconnected");
            backgroundService = null;
        }
    };

    protected void onBackgroundServiceConnected(ComponentName className, IBinder service) {
        log.info("onBackgroundServiceConnected");
        backgroundService = ((ServiceBinder) service).getService();
    }

    protected void bindService() {
        log.info("bind service");
        isBound = backgroundServiceHandler.bindService();
    }

    protected void startService() {
        log.info("start service");
        backgroundServiceHandler.startService();
    }

    protected void startAndBindBackgroundService() {
        isBound = backgroundServiceHandler.startAndBindBackgroundService();
    }

    protected void stopAndUnbindBackgroundService() {
        backgroundServiceHandler.stopAndUnbindBackgroundService();
    }

    protected void unbindService() {
        if (isBound && backgroundService != null) {
            backgroundServiceHandler.unbindService();
            isBound = false;
        }
        log.info("unbind service");
    }

    public Intent createServiceIntent() {
        // override when needed!
        return new Intent(BaseActivity.this, BackgroundService.class);
    }

    public BackgroundService getBackgroundService() {
        return backgroundService;
    }

    // GOOGLE PLAY SERVICES

    protected void prepareGooglePlayServices() {
        if (googlePlayServiceLibraryChecker == null) {
            googlePlayServiceLibraryChecker = new GooglePlayServiceLibraryChecker(this);
        }
        googlePlayServiceLibraryChecker.prepareGooglePlayServices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (googlePlayServiceLibraryChecker != null) {
            googlePlayServiceLibraryChecker.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onGooglePlayServicesPrepared() {
        // override when needed!
    }
}
