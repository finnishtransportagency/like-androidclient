package fi.livi.like.client.android.background.tracking;


import android.location.Location;

import com.google.android.gms.location.ActivityRecognitionResult;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.background.LikeService;
import fi.livi.like.client.android.background.googleplayservices.ActivityRecognitionListener;
import fi.livi.like.client.android.background.googleplayservices.ActivityRecognitionRequest;
import fi.livi.like.client.android.background.googleplayservices.GooglePlayServicesApiClient;
import fi.livi.like.client.android.background.datacollectors.location.DistanceCalculator;
import fi.livi.like.client.android.background.datacollectors.location.LocationHandler;
import fi.livi.like.client.android.background.util.Broadcaster;
import fi.livi.like.client.android.background.util.UpdateTimer;

public class UserTracker implements UpdateTimer.Update, LocationHandler.Listener, ActivityRecognitionListener, TrackingStateMachine.StateMachineClient {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(UserTracker.class);

    private LikeService likeService;
    private UpdateTimer updateTimer;
    private GooglePlayServicesApiClient googlePlayServicesApiClient;
    private LocationHandler locationHandler;
    private TrackingStateMachine trackingStateMachine;

    private JourneyManager journeyManager;

    public UserTracker(LikeService likeService) {
        this.likeService = likeService;
    }

    public void init() {
        locationHandler = new LocationHandler(likeService, this);
        updateTimer = new UpdateTimer(this);
        googlePlayServicesApiClient = likeService.getBackgroundService().getGooglePlayServicesApiClient();
        trackingStateMachine = new TrackingStateMachine(
                this,
                new TrackingDisabledHandler(likeService),
                Broadcaster.getInstance(),
                new DistanceCalculator(),
                likeService.getDataStorage().getConfiguration());
        journeyManager = new JourneyManager(likeService, locationHandler, googlePlayServicesApiClient);
    }

    private void waitUserMovement() {
        log.info("waitUserMovement");
        likeService.getDataStorage().setLastAverageLikeActivity(null);
        googlePlayServicesApiClient.startActivityRecognitionUpdates(
                likeService.getDataStorage(),
                new ActivityRecognitionRequest(likeService.getDataStorage().getConfiguration().getInternalInactiveActivityRecogInterval()),
                this);
    }

    private void userMoving() {
        log.info("userMoving");
        locationHandler.requestLocationUpdates();
    }

    private void stopUserMoving() {
        log.info("stopUserMoving");
        locationHandler.stopLocationUpdates();
    }

    private void startTrackingUser() {
        log.info("startTrackingUser");
        updateTimer.startTimer(likeService.getDataStorage().getConfiguration().getTrackingUserInterval());

        locationHandler.requestLocationUpdates();
        googlePlayServicesApiClient.startActivityRecognitionUpdates(
                likeService.getDataStorage(),
                new ActivityRecognitionRequest(likeService.getDataStorage().getConfiguration().getInternalActiveActivityRecogInterval()),
                this);
    }

    private void stopTrackingUser() {
        log.info("stopTrackingUser");
        updateTimer.stopTimer();

        locationHandler.stopLocationUpdates();
        journeyManager.endJourney();
    }

    private void stopAllLikeTracking() {
        log.info("stopAllLikeTracking");
        updateTimer.stopTimer();
        locationHandler.stopLocationUpdates();
        googlePlayServicesApiClient.stopActivityRecognitionUpdates();
        journeyManager.endJourney();
    }

    public void close() {
        log.info("close");
        stopTrackingUser();
        googlePlayServicesApiClient.close();
    }

    @Override
    public void onStateChange(TrackingStateMachine.State oldState, TrackingStateMachine.State newState) {

        switch (oldState) {
            case TRACKING_USER :
                stopTrackingUser();
                break;
            case USER_MOVING :
                stopUserMoving();
                break;
            default:
        }

        switch (newState) {
            case INITIAL:
                break;
            case WAITING_MOVEMENT :
                waitUserMovement();
                break;
            case USER_MOVING :
                userMoving();
                break;
            case TRACKING_USER :
                startTrackingUser();
                break;
            case DISABLED :
                stopAllLikeTracking();
                break;
            default :
                log.warn("invalid tracking state");
        }
    }

    @Override
    public void onLocationUpdated() {
        log.trace("onLocationUpdated - " + locationHandler.getAverageLocation().toString());
        trackingStateMachine.onLocationUpdated();
    }

    @Override
    public void onActivityRecognitionUpdate(ActivityRecognitionResult result) {
        log.info("onActivityRecognitionUpdate - " + result.getMostProbableActivity().toString());
        trackingStateMachine.onActivityRecognitionUpdate(result);
    }

    @Override
    public Journey getPreviousJourney() {
        return journeyManager.getPreviousJourney();
    }

    @Override
    public Location getLastLocation() {
        return locationHandler.getAverageLocation();
    }

    public void onTimerUpdate() {
        log.debug("onTimerUpdate - updating journey");
        journeyManager.updateJourney();
    }

    public TrackingStateMachine getTrackingStateMachine() {
        return trackingStateMachine;
    }
}