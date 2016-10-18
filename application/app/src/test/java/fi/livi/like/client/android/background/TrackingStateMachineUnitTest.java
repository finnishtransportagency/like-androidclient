package fi.livi.like.client.android.background;

import android.location.Location;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.junit.Before;
import org.junit.Test;

import fi.livi.like.client.android.Configuration;
import fi.livi.like.client.android.background.datacollectors.location.DistanceCalculator;
import fi.livi.like.client.android.background.tracking.Journey;
import fi.livi.like.client.android.background.tracking.TrackingDisabledHandler;
import fi.livi.like.client.android.background.tracking.TrackingStateMachine;
import fi.livi.like.client.android.background.util.Broadcaster;
import fi.livi.like.client.android.dependencies.backend.LikeLocation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrackingStateMachineUnitTest {

    private TrackingStateMachine trackingStateMachine;
    private TrackingDisabledHandler trackingDisabledHandler;
    private Broadcaster broadcaster;
    private DistanceCalculator distanceCalculator;
    private Configuration configuration;
    private TrackingStateMachine.StateMachineClient stateMachineClient;

    @Before
    public void setup() {
        stateMachineClient = mock(TrackingStateMachine.StateMachineClient.class);
        trackingDisabledHandler = mock(TrackingDisabledHandler.class);
        broadcaster = mock(Broadcaster.class);
        distanceCalculator = mock(DistanceCalculator.class);
        configuration = mock(Configuration.class);

        trackingStateMachine = new TrackingStateMachine(
                stateMachineClient, trackingDisabledHandler, broadcaster, distanceCalculator, configuration);

        when(configuration.getInternalInactivityDelay()).thenReturn(1);
        when(configuration.getDistanceToTriggerNewJourney()).thenReturn(10f);
    }

    @Test
    public void should_notify_listener_when_state_is_changed() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);

        verifyStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.WAITING_MOVEMENT));
    }

    @Test
    public void should_not_notify_listener_when_state_is_not_changed() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.INITIAL);

        verify(stateMachineClient, never()).onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.INITIAL));
    }

    // WAITING -> MOVING/TRACKING (no previous journey)

    @Test
    public void should_change_state_from_waiting_without_previous_journey_to_tracking_when_activity_other_than_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        reset(stateMachineClient); // reset setting to WAITING_STATE

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.TILTING));

        verifyStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.USER_MOVING);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    @Test
    public void should_not_change_state_from_waiting_without_previous_journey_to_tracking_when_activity_is_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        reset(stateMachineClient); // reset setting to WAITING_STATE

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.STILL));

        verify(stateMachineClient, never()).onStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.TRACKING_USER);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.WAITING_MOVEMENT));
    }

    // WAITING -> MOVING/TRACKING (previous journey available)

    @Test
    public void should_change_state_from_waiting_with_previous_journey_to_user_moving_when_activity_other_than_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        reset(stateMachineClient); // reset setting to WAITING_STATE
        Journey mockedJourney = mock(Journey.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.TILTING));

        verifyStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.USER_MOVING);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    @Test
    public void should_not_change_state_from_user_moving_with_previous_journey_receiving_location_with_journey_and_null_location() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        Journey mockedJourney = mock(Journey.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        reset(stateMachineClient); // reset setting to USER_MOVING
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);

        trackingStateMachine.onLocationUpdated();

        verify(stateMachineClient, never()).onStateChange(any(TrackingStateMachine.State.class), any(TrackingStateMachine.State.class));
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    @Test
    public void should_not_change_state_from_user_moving_with_previous_journey_to_tracking_user_when_receiving_location_too_close() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        Journey mockedJourney = mock(Journey.class);
        Location mockedLocation = mock(Location.class);
        LikeLocation mockedLikeLocation = mock(LikeLocation.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        reset(stateMachineClient); // reset setting to USER_MOVING
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        when(stateMachineClient.getLastLocation()).thenReturn(mockedLocation);
        when(distanceCalculator.distanceBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(9f);
        when(mockedJourney.getLastLikeLocation()).thenReturn(mockedLikeLocation);

        trackingStateMachine.onLocationUpdated();

        verify(stateMachineClient, never()).onStateChange(any(TrackingStateMachine.State.class), any(TrackingStateMachine.State.class));
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    @Test
    public void should_not_change_state_from_user_moving_with_previous_journey_to_tracking_user_when_receiving_location_far_enough_but_speed_not_enough() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        Journey mockedJourney = mock(Journey.class);
        Location mockedLocation = mock(Location.class);
        when(mockedLocation.getSpeed()).thenReturn(0f);
        LikeLocation mockedLikeLocation = mock(LikeLocation.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        reset(stateMachineClient); // reset setting to USER_MOVING
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        when(stateMachineClient.getLastLocation()).thenReturn(mockedLocation);
        when(distanceCalculator.distanceBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(10.01f);
        when(mockedJourney.getLastLikeLocation()).thenReturn(mockedLikeLocation);

        trackingStateMachine.onLocationUpdated();

        verify(stateMachineClient, never()).onStateChange(any(TrackingStateMachine.State.class), any(TrackingStateMachine.State.class));
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    @Test
    public void should_change_state_from_user_moving_with_previous_journey_to_tracking_user_when_receiving_location_far_enough() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        Journey mockedJourney = mock(Journey.class);
        Location mockedLocation = mock(Location.class);
        when(mockedLocation.getSpeed()).thenReturn(0.001f);
        LikeLocation mockedLikeLocation = mock(LikeLocation.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        reset(stateMachineClient); // reset setting to USER_MOVING
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        when(stateMachineClient.getLastLocation()).thenReturn(mockedLocation);
        when(distanceCalculator.distanceBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(10.01f);
        when(mockedJourney.getLastLikeLocation()).thenReturn(mockedLikeLocation);

        trackingStateMachine.onLocationUpdated();

        verifyStateChange(TrackingStateMachine.State.USER_MOVING, TrackingStateMachine.State.TRACKING_USER);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.TRACKING_USER));
    }

    @Test
    public void should_change_state_from_waiting_with_previous_journey_to_tracking_user_when_receiving_location_with_null_journey() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        Location mockedLocation = mock(Location.class);
        when(mockedLocation.getSpeed()).thenReturn(0.1f);
        Journey mockedJourney = mock(Journey.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        reset(stateMachineClient); // reset setting to USER_MOVING
        when(stateMachineClient.getLastLocation()).thenReturn(mockedLocation);

        when(stateMachineClient.getPreviousJourney()).thenReturn(null);
        trackingStateMachine.onLocationUpdated();

        verifyStateChange(TrackingStateMachine.State.USER_MOVING, TrackingStateMachine.State.TRACKING_USER);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.TRACKING_USER));
    }

    @Test
    public void should_not_change_state_from_waiting_with_previous_journey_to_tracking_user_when_receiving_location_with_null_journey_but_speed_too_low() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.WAITING_MOVEMENT);
        Location mockedLocation = mock(Location.class);
        when(mockedLocation.getSpeed()).thenReturn(0f);
        Journey mockedJourney = mock(Journey.class);
        when(stateMachineClient.getPreviousJourney()).thenReturn(mockedJourney);
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        reset(stateMachineClient); // reset setting to USER_MOVING
        when(stateMachineClient.getLastLocation()).thenReturn(mockedLocation);

        when(stateMachineClient.getPreviousJourney()).thenReturn(null);
        trackingStateMachine.onLocationUpdated();

        verify(stateMachineClient, never()).onStateChange(any(TrackingStateMachine.State.class), any(TrackingStateMachine.State.class));
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    // TRACKING -> WAITING

    @Test
    public void should_change_state_from_tracking_to_waiting_when_activity_is_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.TRACKING_USER);
        reset(stateMachineClient); // reset setting to TRACKING_USER

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.STILL));

        verify(stateMachineClient, timeout(3000).times(1)).onStateChange(TrackingStateMachine.State.TRACKING_USER, TrackingStateMachine.State.WAITING_MOVEMENT);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.WAITING_MOVEMENT));
    }

    @Test
    public void should_change_state_from_user_moving_to_waiting_when_activity_is_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.USER_MOVING);
        reset(stateMachineClient); // reset setting to USER_MOVING

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.STILL));

        verify(stateMachineClient, timeout(3000).times(1)).onStateChange(TrackingStateMachine.State.USER_MOVING, TrackingStateMachine.State.WAITING_MOVEMENT);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.WAITING_MOVEMENT));
    }

    @Test
    public void should_not_change_state_from_tracking_to_waiting_when_activity_is_something_else_than_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.TRACKING_USER);
        reset(stateMachineClient); // reset setting to TRACKING_USER

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.TILTING));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.IN_VEHICLE));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_BICYCLE));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.ON_FOOT));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.RUNNING));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.UNKNOWN));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.WALKING));

        verify(stateMachineClient, never()).onStateChange(TrackingStateMachine.State.TRACKING_USER, TrackingStateMachine.State.WAITING_MOVEMENT);
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.TRACKING_USER));
    }

    // TRACKING / MOVING

    @Test
    public void should_cancel_inactivity_timer_from_tracking_user_when_activity_other_than_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.TRACKING_USER);
        reset(stateMachineClient); // reset setting to TRACKING_USER

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.STILL));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.UNKNOWN));

        sleepTight(2000);
        verify(stateMachineClient, never()).onStateChange(any(TrackingStateMachine.State.class), any(TrackingStateMachine.State.class));
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.TRACKING_USER));
    }

    @Test
    public void should_cancel_inactivity_timer_from_user_moving_when_activity_other_than_STILL() {
        trackingStateMachine.setTrackingState(TrackingStateMachine.State.USER_MOVING);
        reset(stateMachineClient); // reset setting to USER_MOVING

        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.STILL));
        trackingStateMachine.onActivityRecognitionUpdate(getActivityRecognitionResult(DetectedActivity.UNKNOWN));

        sleepTight(2000);
        verify(stateMachineClient, never()).onStateChange(any(TrackingStateMachine.State.class), any(TrackingStateMachine.State.class));
        assertThat(trackingStateMachine.getTrackingState(), is(TrackingStateMachine.State.USER_MOVING));
    }

    private void verifyStateChange(TrackingStateMachine.State oldState, TrackingStateMachine.State newState) {
        verify(stateMachineClient).onStateChange(oldState, newState);
        verify(broadcaster).broadcastLocal(
                TrackingStateMachine.BROADCAST_ACTION_ID, TrackingStateMachine.BROADCAST_KEY_ID, newState.toString());
    }
    // UTIL

    private ActivityRecognitionResult getActivityRecognitionResult(int activity) {
        ActivityRecognitionResult mockedResult = mock(ActivityRecognitionResult.class);
        DetectedActivity mockedActivity = mock(DetectedActivity.class);
        when(mockedActivity.getType()).thenReturn(activity);
        when(mockedResult.getMostProbableActivity()).thenReturn(mockedActivity);

        return mockedResult;
    }

    private void sleepTight(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
