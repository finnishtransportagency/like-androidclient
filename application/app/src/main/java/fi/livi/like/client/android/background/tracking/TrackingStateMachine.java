package fi.livi.like.client.android.background.tracking;

import android.location.Location;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.Configuration;
import fi.livi.like.client.android.R;
import fi.livi.like.client.android.background.googleplayservices.ActivityRecognitionListener;
import fi.livi.like.client.android.background.datacollectors.location.DistanceCalculator;
import fi.livi.like.client.android.background.datacollectors.location.LocationHandler;
import fi.livi.like.client.android.background.util.Broadcaster;
import fi.livi.like.client.android.background.util.UpdateTimer;
import fi.livi.like.client.android.dependencies.backend.LikeLocation;

public class TrackingStateMachine implements ActivityRecognitionListener, LocationHandler.Listener, UpdateTimer.Update {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackingStateMachine.class);

    public final static String BROADCAST_ACTION_ID = "fi.livi.like.client.android.trackingstatemachine";
    public final static String BROADCAST_KEY_ID = "newState";

    private final StateMachineClient stateMachineClient;
    private final Broadcaster broadcaster;
    private final DistanceCalculator distanceCalculator;
    private final Configuration configuration;
    private UpdateTimer inactivityTimer;
    private State trackingState = TrackingStateMachine.State.INITIAL;

    public enum State {
        INITIAL,
        WAITING_MOVEMENT,
        USER_MOVING,
        TRACKING_USER,
        DISABLED
    }

    public interface StateMachineClient {
        void onStateChange(State oldState, State newState);
        Journey getPreviousJourney();
        Location getLastLocation();
    }

    public TrackingStateMachine(StateMachineClient stateMachineClient, Broadcaster broadcaster, DistanceCalculator distanceCalculator, Configuration configuration) {
        this.stateMachineClient = stateMachineClient;
        this.broadcaster = broadcaster;
        this.distanceCalculator = distanceCalculator;
        this.configuration = configuration;
    }

    public State getTrackingState() {
        return trackingState;
    }

    public void setTrackingState(State newState) {
        if (trackingState == newState) {
            return;
        }

        log.info("setTrackingState - " + trackingState + "->" + newState);

        stateMachineClient.onStateChange(trackingState, newState);
        trackingState = newState;
        broadcaster.broadcastLocal(
                BROADCAST_ACTION_ID, BROADCAST_KEY_ID, newState.toString());
    }

    public void resumeTrackingStateMachine() {
        if (trackingState == State.INITIAL) {
            setTrackingState(State.WAITING_MOVEMENT);
        }
    }

    public void broadcastCurrentState() {
        broadcaster.broadcastLocal(
                BROADCAST_ACTION_ID, BROADCAST_KEY_ID, trackingState.toString());
    }

    @Override
    public void onActivityRecognitionUpdate(ActivityRecognitionResult result) {
        final int androidActivityType = result.getMostProbableActivity().getType();

        if (trackingState == State.WAITING_MOVEMENT && androidActivityType != DetectedActivity.STILL) {

            if (stateMachineClient.getPreviousJourney() == null) {
                log.info("no previous journey to compare locations, start TRACKING-state");
                setTrackingState(State.TRACKING_USER);
            } else {
                log.info("previous journey available to compare locations, get current location");
                setTrackingState(State.USER_MOVING);
            }
        } else if (trackingState == State.TRACKING_USER || trackingState == State.USER_MOVING) {

            if (androidActivityType == DetectedActivity.STILL) {
                startInactivityTimer();
            } else {
                stopInactivityTimer();
            }
        }
    }

    @Override
    public void onLocationUpdated() {
        if (trackingState == State.USER_MOVING) {
            log.info("onLocationUpdated on USER_MOVING-state, try if we can utilise old journey");
            Journey previousJourney = stateMachineClient.getPreviousJourney();
            Location currentLocation = stateMachineClient.getLastLocation();
            if (previousJourney == null) {
                log.info("no previous journey available anymore to compare locations, start TRACKING-state");
                setTrackingState(State.TRACKING_USER);
            } else if (currentLocation != null) {
                LikeLocation likeLocation = previousJourney.getLastLikeLocation();
                float distance = distanceCalculator.distanceBetween(
                        likeLocation.getLatitude(), likeLocation.getLongitude(),
                        currentLocation.getLatitude(), currentLocation.getLongitude());
                log.info("distance from last journey end point " + distance);
                if (distance > configuration.getDistanceToTriggerNewJourney()) {
                    log.info("distance from last journey end over " + configuration.getDistanceToTriggerNewJourney() + ", start TRACKING-state");
                    setTrackingState(State.TRACKING_USER);
                }
            }
        }
    }

    @Override
    public void onTimerUpdate() {
        if (trackingState == State.TRACKING_USER || trackingState == State.USER_MOVING) {
            log.info("INACTIVITY TIMER triggered, resume WAITING-state");
            setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
            stopInactivityTimer();
        }
    }

    public static int getTrackingStringResourceId(TrackingStateMachine.State state) {
        int trackingTextResId;
        switch (state) {
            case TRACKING_USER:
                trackingTextResId = R.string.tracking_on;
                break;
            case DISABLED:
                trackingTextResId = R.string.tracking_disabled;
                break;
            default:
                trackingTextResId = R.string.tracking_off;
        }
        return trackingTextResId;
    }

    private void startInactivityTimer() {
        if (inactivityTimer == null) {
            log.info("starting INACTIVITY TIMER");
            inactivityTimer = new UpdateTimer(this);
            inactivityTimer.startTimer(configuration.getInternalInactivityDelay());
        }
    }

    private void stopInactivityTimer() {
        if (inactivityTimer != null) {
            log.info("stopping INACTIVITY TIMER");
            inactivityTimer.stopTimer();
            inactivityTimer = null;
        }
    }
}