package fi.livi.like.client.android.backgroundservice.background;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import fi.livi.like.client.android.backgroundservice.Configuration;
import fi.livi.like.client.android.backgroundservice.data.DataStorage;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationHandler;
import fi.livi.like.client.android.backgroundservice.googleplayservices.ActivityRecognitionListener;
import fi.livi.like.client.android.backgroundservice.googleplayservices.ActivityRecognitionRequest;
import fi.livi.like.client.android.backgroundservice.googleplayservices.GooglePlayServicesApiClient;
import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.service.BackgroundService;
import fi.livi.like.client.android.backgroundservice.tracking.JourneyManager;
import fi.livi.like.client.android.backgroundservice.tracking.TrackingStateMachine;
import fi.livi.like.client.android.backgroundservice.tracking.UserTracker;
import fi.livi.like.client.android.backgroundservice.util.UpdateTimer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserTrackerUnitTest {

    @InjectMocks UserTracker userTracker;

    @Mock
    LikeService mockedLikeService;
    @Mock
    LocationHandler mockedLocationHandler;
    @Mock
    GooglePlayServicesApiClient mockedGooglePlayServicesApiClient;
    @Mock
    UpdateTimer mockedUpdateTimer;
    @Mock
    JourneyManager mockedJourneyManager;

    private DataStorage mockedDataStorage;
    private Configuration configuration = new Configuration();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        // not calling init() on tests to keep mocks alive
        BackgroundService mockedBackgroundService = mock(BackgroundService.class);
        mockedDataStorage = mock(DataStorage.class);

        when(mockedLikeService.getBackgroundService()).thenReturn(mockedBackgroundService);
        when(mockedLikeService.getDataStorage()).thenReturn(mockedDataStorage);
        when(mockedDataStorage.getConfiguration()).thenReturn(configuration);
        when(mockedBackgroundService.getGooglePlayServicesApiClient()).thenReturn(mockedGooglePlayServicesApiClient);
    }

    @Test
    public void should_start_listening_activity_recognizer_changes_when_waiting_movement() {
        verify(mockedGooglePlayServicesApiClient, never()).startActivityRecognitionUpdates(any(DataStorage.class), any(ActivityRecognitionRequest.class), any(ActivityRecognitionListener.class));

        userTracker.onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);

        ArgumentCaptor<ActivityRecognitionRequest> requestCaptor = ArgumentCaptor.forClass(ActivityRecognitionRequest.class);
        verify(mockedGooglePlayServicesApiClient, times(1)).startActivityRecognitionUpdates(any(DataStorage.class), requestCaptor.capture(), any(ActivityRecognitionListener.class));
        assertThat(requestCaptor.getValue().getActivityUpdateIntervalInSecs(), is(new Configuration().getInternalInactiveActivityRecogInterval()));
        verify(mockedDataStorage).setLastAverageLikeActivity(null);
    }

    @Test
    public void should_request_location_updates_when_user_moving() {
        verify(mockedGooglePlayServicesApiClient, never()).startActivityRecognitionUpdates(any(DataStorage.class), any(ActivityRecognitionRequest.class), any(ActivityRecognitionListener.class));
        userTracker.onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);
        reset(mockedLocationHandler);

        userTracker.onStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.USER_MOVING);

        verify(mockedLocationHandler).requestLocationUpdates();
    }

    @Test
    public void should_request_location_updates_and_adjust_activity_recognizer_interval_when_starting_to_track_user() {
        verify(mockedGooglePlayServicesApiClient, never()).startActivityRecognitionUpdates(any(DataStorage.class), any(ActivityRecognitionRequest.class), any(ActivityRecognitionListener.class));
        userTracker.onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);

        userTracker.onStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.TRACKING_USER);

        ArgumentCaptor<ActivityRecognitionRequest> requestCaptor = ArgumentCaptor.forClass(ActivityRecognitionRequest.class);
        verify(mockedGooglePlayServicesApiClient, times(2)).startActivityRecognitionUpdates(any(DataStorage.class), requestCaptor.capture(), any(ActivityRecognitionListener.class));
        assertThat(requestCaptor.getAllValues().get(1).getActivityUpdateIntervalInSecs(), is(new Configuration().getInternalActiveActivityRecogInterval()));
        verify(mockedLocationHandler).requestLocationUpdates();
        verify(mockedUpdateTimer).startTimer(new Configuration().getTrackingUserInterval());
    }

    @Test
    public void should_stop_location_updates_and_adjust_activity_recognizer_interval_when_resuming_from_tracking_to_waiting_movement() {
        verify(mockedGooglePlayServicesApiClient, never()).startActivityRecognitionUpdates(any(DataStorage.class), any(ActivityRecognitionRequest.class), any(ActivityRecognitionListener.class));
        userTracker.onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);
        userTracker.onStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.TRACKING_USER);
        reset(mockedLocationHandler);

        userTracker.onStateChange(TrackingStateMachine.State.TRACKING_USER, TrackingStateMachine.State.WAITING_MOVEMENT);

        ArgumentCaptor<ActivityRecognitionRequest> requestCaptor = ArgumentCaptor.forClass(ActivityRecognitionRequest.class);
        verify(mockedGooglePlayServicesApiClient, times(3)).startActivityRecognitionUpdates(any(DataStorage.class), requestCaptor.capture(), any(ActivityRecognitionListener.class));
        assertThat(requestCaptor.getAllValues().get(2).getActivityUpdateIntervalInSecs(), is(new Configuration().getInternalInactiveActivityRecogInterval()));
        verify(mockedLocationHandler).stopLocationUpdates();
        verify(mockedUpdateTimer).stopTimer();
        verify(mockedJourneyManager).endJourney();
        verify(mockedDataStorage, atLeast(1)).setLastAverageLikeActivity(null);
    }

    @Test
    public void should_stop_location_updates_and_adjust_activity_recognizer_interval_when_resuming_from_user_moving_to_waiting_movement() {
        verify(mockedGooglePlayServicesApiClient, never()).startActivityRecognitionUpdates(any(DataStorage.class), any(ActivityRecognitionRequest.class), any(ActivityRecognitionListener.class));
        userTracker.onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);
        userTracker.onStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.USER_MOVING);
        reset(mockedLocationHandler);
        reset(mockedGooglePlayServicesApiClient);

        userTracker.onStateChange(TrackingStateMachine.State.USER_MOVING, TrackingStateMachine.State.WAITING_MOVEMENT);

        ArgumentCaptor<ActivityRecognitionRequest> requestCaptor = ArgumentCaptor.forClass(ActivityRecognitionRequest.class);
        verify(mockedGooglePlayServicesApiClient, times(1)).startActivityRecognitionUpdates(any(DataStorage.class), requestCaptor.capture(), any(ActivityRecognitionListener.class));
        assertThat(requestCaptor.getAllValues().get(0).getActivityUpdateIntervalInSecs(), is(new Configuration().getInternalInactiveActivityRecogInterval()));
        verify(mockedLocationHandler).stopLocationUpdates();
        verify(mockedDataStorage, atLeast(1)).setLastAverageLikeActivity(null);
    }

    @Test
    public void should_stop_location_updates_and_activity_recognition_when_disabled() {
        userTracker.onStateChange(TrackingStateMachine.State.INITIAL, TrackingStateMachine.State.WAITING_MOVEMENT);
        reset(mockedLocationHandler);
        reset(mockedGooglePlayServicesApiClient);

        userTracker.onStateChange(TrackingStateMachine.State.WAITING_MOVEMENT, TrackingStateMachine.State.DISABLED);

        verify(mockedUpdateTimer).stopTimer();
        verify(mockedLocationHandler).stopLocationUpdates();
        verify(mockedGooglePlayServicesApiClient).stopActivityRecognitionUpdates();
        verify(mockedJourneyManager).endJourney();
    }

    @Test
    public void should_stop_location_updates_and_activity_recognition_when_closed() {
        userTracker.close();

        verify(mockedUpdateTimer).stopTimer();
        verify(mockedLocationHandler).stopLocationUpdates();
        verify(mockedGooglePlayServicesApiClient).close();
        verify(mockedJourneyManager).endJourney();
    }
}