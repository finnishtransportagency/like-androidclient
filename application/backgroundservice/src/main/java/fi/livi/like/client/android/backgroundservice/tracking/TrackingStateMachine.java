package fi.livi.like.client.android.backgroundservice.tracking;

import android.location.Location;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.backgroundservice.Configuration;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.DistanceCalculator;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationBearingAngleTracker;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationHandler;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeLocation;
import fi.livi.like.client.android.backgroundservice.googleplayservices.ActivityRecognitionListener;
import fi.livi.like.client.android.backgroundservice.util.Broadcaster;
import fi.livi.like.client.android.backgroundservice.util.UpdateTimer;

public class TrackingStateMachine implements ActivityRecognitionListener, LocationHandler.Listener, UpdateTimer.Update {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackingStateMachine.class);

    public final static String BROADCAST_ACTION_ID = "fi.livi.like.client.android.trackingstatemachine";
    public final static String BROADCAST_KEY_ID = "newState";
    private final static float JOURNEY_START_UPDATE_MIN_SPEED = 0;

    private final StateMachineClient stateMachineClient;
    private final TrackingDisabledHandler trackingDisabledHandler;
    private final Broadcaster broadcaster;
    private final DistanceCalculator distanceCalculator;
    private final Configuration configuration;
    private final LocationBearingAngleTracker bearingAngleTracker;
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
        TrackingStrings getTrackingStrings();
    }

    public TrackingStateMachine(StateMachineClient stateMachineClient, TrackingDisabledHandler trackingDisabledHandler,
                                Broadcaster broadcaster, DistanceCalculator distanceCalculator, LocationBearingAngleTracker bearingAngleTracker,
                                Configuration configuration) {
        this.stateMachineClient = stateMachineClient;
        this.trackingDisabledHandler = trackingDisabledHandler;
        this.broadcaster = broadcaster;
        this.distanceCalculator = distanceCalculator;
        this.bearingAngleTracker = bearingAngleTracker;
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
        if (trackingState != State.DISABLED) {
            trackingDisabledHandler.reset();
        }
        broadcastCurrentState();
    }

    public void setTrackingState(State newState, TrackingDisabledHandler.DisabledTime disabledTime) {
        if (newState != State.DISABLED) {
            throw new AssertionError("setTrackingState with DisabledTime can be used only with DISABLED-state!");
        }

        trackingDisabledHandler.setDisabledTime(disabledTime);
        setTrackingState(newState);
    }

    public void resumeTrackingStateMachine() {
        if (trackingState == State.INITIAL) {
            setTrackingState(State.WAITING_MOVEMENT);
        }
    }

    public void broadcastCurrentState() {
        if (trackingState != State.DISABLED) {
            broadcaster.broadcastLocal(
                    BROADCAST_ACTION_ID, BROADCAST_KEY_ID, trackingState.toString());
        } else {
            trackingDisabledHandler.broadcastStateLocal();
        }
    }

    @Override
    public void onActivityRecognitionUpdate(ActivityRecognitionResult result) {
        final int androidActivityType = result.getMostProbableActivity().getType();

        if (trackingState == State.WAITING_MOVEMENT && androidActivityType != DetectedActivity.STILL) {

            log.info("movement detected, start USER_MOVING-state");
            setTrackingState(State.USER_MOVING);
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
            log.trace("onLocationUpdated on USER_MOVING-state");
            final Location currentLocation = stateMachineClient.getLastLocation();
            if (currentLocation != null) {
                if (!(currentLocation.getSpeed() > JOURNEY_START_UPDATE_MIN_SPEED)) {
                    log.trace("location speed too low, staying on USER_MOVING-state!");
                    return;
                }

                final Journey previousJourney = stateMachineClient.getPreviousJourney();
                final boolean bearingAngleWithinLimits = bearingAngleTracker.isBearingAngleWithinLimits(currentLocation);
                if (previousJourney != null) {
                    LikeLocation likeLocation = previousJourney.getLastLikeLocation();
                    float distance = distanceCalculator.distanceBetween(
                            likeLocation.getLatitude(), likeLocation.getLongitude(),
                            currentLocation.getLatitude(), currentLocation.getLongitude());
                    log.debug("distance from last journey end point " + distance);
                    final boolean distanceOverTriggerLimit = distance > configuration.getDistanceToTriggerNewJourney();
                    if (distanceOverTriggerLimit) {
                        log.info("distance from last journey end less than " + configuration.getDistanceToTriggerNewJourney() + ", check bearing");
                        if (bearingAngleWithinLimits) {
                            log.info("bearing angle within the limits with previous location, start TRACKING-state");
                            setTrackingState(State.TRACKING_USER);
                        }
                    }
                } else {
                    log.info("no previous journey to compare locations, check bearing");
                    if (bearingAngleWithinLimits) {
                        log.info("bearing angle within the limits with previous location, start TRACKING-state");
                        setTrackingState(State.TRACKING_USER);
                    }
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

    public static String getShortTrackingString(
            TrackingStateMachine.State state, TrackingStrings trackingStrings) {

        if (trackingStrings == null) {
            return null;
        }

        switch (state) {
            case TRACKING_USER:
                return trackingStrings.getTrackingOn();
            case DISABLED:
                return trackingStrings.getTrackingDisabled();
            default:
                return trackingStrings.getTrackingOff();
        }
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