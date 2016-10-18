package fi.livi.like.client.android.background;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.R;
import fi.livi.like.client.android.background.data.DataStorage;
import fi.livi.like.client.android.background.http.HttpLoader;
import fi.livi.like.client.android.background.http.NetworkMonitor;
import fi.livi.like.client.android.background.service.BackgroundService;
import fi.livi.like.client.android.background.tracking.TrackingStateMachine;
import fi.livi.like.client.android.background.tracking.UserTracker;
import fi.livi.like.client.android.background.user.UserManager;
import fi.livi.like.client.android.background.util.Broadcaster;
import fi.livi.like.client.android.background.tracking.TrackingDisabledHandler;
import fi.livi.like.client.android.broadcastreceivers.UserLoadingCompletedReceiver;

public class LikeService implements UserLoadingCompletedReceiver.Listener, UserManager.Listener, NetworkMonitor.Listener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LikeService.class);

    private final BackgroundService backgroundService;

    private final DataStorage dataStorage;
    private final HttpLoader httpLoader;
    private final UserManager userManager;
    private final UserTracker userTracker;
    private NetworkMonitor networkMonitor;
    private UserLoadingCompletedReceiver userLoadingCompletedReceiver;

    public LikeService(BackgroundService backgroundService) {
        this.backgroundService = backgroundService;
        dataStorage = new DataStorage();
        httpLoader = new HttpLoader(this);
        userManager = new UserManager(this, Broadcaster.getInstance());
        userTracker = new UserTracker(this);
        userTracker.init();
    }

    public void resume() {
        log.info("resuming");
        userTracker.getTrackingStateMachine().broadcastCurrentState();
        if (userTracker.getTrackingStateMachine().getTrackingState() == TrackingStateMachine.State.DISABLED) {
            log.info("currently disabled, not resuming");
            return;
        }

        if (!NetworkMonitor.isNetworkAvailable(getBackgroundService())) {
            log.info("no network, wait until network available and resume then");
            networkMonitor = new NetworkMonitor(this);
            networkMonitor.registerNetworkStateListener(getBackgroundService());
            return;
        }

        prepareUser();
    }

    @Override
    public void onPinCodeStored() {
        prepareUser();
    }

    private void prepareUser() {
        if (userManager.isUserPrepared()) {
            log.info("user already prepared, continue by resuming tracking state machine");
            userTracker.getTrackingStateMachine().resumeTrackingStateMachine();
            return;
        }
        if (userManager.prepareUserPinCode()) {
            if (userLoadingCompletedReceiver == null) {
                userLoadingCompletedReceiver = new UserLoadingCompletedReceiver(this);
                LocalBroadcastManager.getInstance(backgroundService).registerReceiver(
                        userLoadingCompletedReceiver, new IntentFilter(HttpLoader.BROADCAST_ACTION_ID));
            }
            userManager.prepareUser();
        }
    }

    @Override
    public void onUserLoadingCompleted() {
        log.info("onUserLoadingCompleted - " + dataStorage.getUser().toString());
        if (userLoadingCompletedReceiver != null) {
            LocalBroadcastManager.getInstance(backgroundService).unregisterReceiver(userLoadingCompletedReceiver);
            userLoadingCompletedReceiver = null;
        }
        userTracker.getTrackingStateMachine().resumeTrackingStateMachine();
    }

    @Override
    public void onUserLoadingFailed() {
        final Context context = getBackgroundService().getApplicationContext();
        Toast.makeText(context, context.getText(R.string.user_loading_failed), Toast.LENGTH_LONG).show();
    }

    public void close() {
        userTracker.close();
    }

    public HttpLoader getHttpLoader() {
        return httpLoader;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public BackgroundService getBackgroundService() {
        return backgroundService;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public boolean isTrackingDisabled() {
        return userTracker.getTrackingStateMachine().getTrackingState() == TrackingStateMachine.State.DISABLED;
    }

    public void setTrackingDisabled(TrackingDisabledHandler.DisabledTime disabledTime) {
        userTracker.getTrackingStateMachine().setTrackingState(TrackingStateMachine.State.DISABLED, disabledTime);
    }

    public void setTrackingEnabled() {
        userTracker.getTrackingStateMachine().setTrackingState(TrackingStateMachine.State.INITIAL);
        resume();
    }

    @Override
    public void onNetworkAvailable() {
        log.info("network available - resume");
        networkMonitor = null;
        resume();
    }
}
