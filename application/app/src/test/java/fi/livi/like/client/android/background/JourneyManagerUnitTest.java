package fi.livi.like.client.android.background;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import fi.livi.like.client.android.util.TestUtil;
import fi.livi.like.client.android.background.data.DataStorage;
import fi.livi.like.client.android.background.googleplayservices.ActivityRecognitionUpdateHandler;
import fi.livi.like.client.android.background.googleplayservices.GooglePlayServicesApiClient;
import fi.livi.like.client.android.background.http.HttpLoader;
import fi.livi.like.client.android.background.datacollectors.location.LocationHandler;
import fi.livi.like.client.android.background.tracking.JourneyManager;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;
import fi.livi.like.client.android.dependencies.backend.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JourneyManagerUnitTest {

    JourneyManager journeyManager;

    LikeService likeService = mock(LikeService.class);
    HttpLoader httpLoader = mock(HttpLoader.class);
    DataStorage dataStorage = mock(DataStorage.class);
    User user = mock(User.class);
    LocationHandler locationHandler = mock(LocationHandler.class);
    GooglePlayServicesApiClient googlePlayServicesApiClient = mock(GooglePlayServicesApiClient.class);
    ActivityRecognitionUpdateHandler activityRecognitionUpdateHandler = mock(ActivityRecognitionUpdateHandler.class);

    @Before
    public void setup() {
        journeyManager = new JourneyManager(likeService, locationHandler, googlePlayServicesApiClient);

        when(googlePlayServicesApiClient.getActivityRecognitionUpdateHandler()).thenReturn(activityRecognitionUpdateHandler);
        when(likeService.getHttpLoader()).thenReturn(httpLoader);
        when(likeService.getDataStorage()).thenReturn(dataStorage);
        when(dataStorage.getUser()).thenReturn(user);
        when(user.getId()).thenReturn("1234");
    }

    @Test
    public void should_return_null_journey_when_none_avail() {
        assertThat(journeyManager.getCurrentJourney(), is(nullValue()));
        assertThat(journeyManager.isJourneyStarted(), is(false));
        assertThat(journeyManager.getPreviousJourney(), is(nullValue()));
    }

    @Test
    public void should_return_false_when_activity_result_available_but_no_location() {
        setLikeActivityAverageResult(LikeActivity.Type.WALKING, 99);

        boolean result = journeyManager.updateJourney();

        assertThat(result, is(false));
        assertThat(journeyManager.isJourneyStarted(), is(false));
    }

    @Test
    public void should_return_false_when_location_available_but_no_activity_result() {
        Location androidLocation = mock(Location.class);
        when(locationHandler.getAverageLocation()).thenReturn(androidLocation);

        boolean result = journeyManager.updateJourney();

        assertThat(result, is(false));
        assertThat(journeyManager.isJourneyStarted(), is(false));
    }

    @Test
    public void should_update_current_journey_with_like_activity_when_available() {
        setLikeActivityAverageResult(LikeActivity.Type.ON_BICYCLE, 22);

        boolean result = journeyManager.updateJourney();

        assertThat(result, is(false));
        assertThat(journeyManager.getCurrentJourney().getLastLikeActivity().getType(), is(LikeActivity.Type.ON_BICYCLE));
        assertThat(journeyManager.getCurrentJourney().getLastLikeActivity().getConfidence(), is(22));
        assertThat(journeyManager.isJourneyStarted(), is(false));
    }

    @Test
    public void should_start_journey_when_both_location_and_activity_result_available() {
        setLikeActivityAverageResult(LikeActivity.Type.ON_BICYCLE, 22);
        setAndroidLocation(1d, 2d, 3L, 4f, 5f);

        boolean result = journeyManager.updateJourney();

        assertThat(result, is(true));
        assertThat(journeyManager.isJourneyStarted(), is(true));
    }

    @Test
    public void should_update_location_and_activity_to_journey() {
        setLikeActivityAverageResult(LikeActivity.Type.RUNNING, 88);
        setAndroidLocation(1d, 2d, 3L, 4f, 5f);

        boolean result = journeyManager.updateJourney();

        assertThat(result, is(true));
        assertThat(journeyManager.isJourneyStarted(), is(true));
        assertThat(journeyManager.getCurrentJourney().getLastLikeActivity().getType(), is(LikeActivity.Type.RUNNING));
        assertThat(journeyManager.getCurrentJourney().getLastLikeActivity().getConfidence(), is(88));
        assertThat(journeyManager.getCurrentJourney().getLastLikeLocation().getLatitude(), is(1d));
        assertThat(journeyManager.getCurrentJourney().getLastLikeLocation().getLongitude(), is(2d));
        assertThat(isRecentTime(journeyManager.getCurrentJourney().getJourneyId()), is(true));
        assertThat(isRecentTime(journeyManager.getCurrentJourney().getLastSubJourneyJourneyId()), is(true));
        assertThat(journeyManager.getCurrentJourney().getLastLikeLocation().getSpeed(), is(4));
        assertThat(journeyManager.getCurrentJourney().getLastLikeLocation().getHeading(), is(5));
    }

    @Test
    public void should_move_journey_to_previous_when_ending() {
        setLikeActivityAverageResult(LikeActivity.Type.RUNNING, 88);
        setAndroidLocation(1d, 2d, 3L, 4f, 5f);

        journeyManager.updateJourney();
        journeyManager.endJourney();

        assertThat(journeyManager.isJourneyStarted(), is(false));
        assertThat(journeyManager.getCurrentJourney(), is(nullValue()));
        assertThat(journeyManager.getPreviousJourney().getLastLikeActivity().getType(), is(LikeActivity.Type.RUNNING));
        assertThat(journeyManager.getPreviousJourney().getLastLikeActivity().getConfidence(), is(88));
        assertThat(journeyManager.getPreviousJourney().getLastLikeLocation().getLatitude(), is(1d));
        assertThat(journeyManager.getPreviousJourney().getLastLikeLocation().getLongitude(), is(2d));
        assertThat(isRecentTime(journeyManager.getPreviousJourney().getJourneyId()), is(true));
        assertThat(isRecentTime(journeyManager.getPreviousJourney().getLastSubJourneyJourneyId()), is(true));
        assertThat(journeyManager.getPreviousJourney().getLastLikeLocation().getSpeed(), is(4));
        assertThat(journeyManager.getPreviousJourney().getLastLikeLocation().getHeading(), is(5));
    }

    @Test
    public void should_keep_valid_previous_journey_if_current_is_not_started() {
        setLikeActivityAverageResult(LikeActivity.Type.RUNNING, 88);
        setAndroidLocation(1d, 2d, 3L, 4f, 5f);

        journeyManager.updateJourney();
        journeyManager.endJourney();
        journeyManager.endJourney();

        assertThat(journeyManager.isJourneyStarted(), is(false));
        assertThat(journeyManager.getCurrentJourney(), is(nullValue()));
        assertThat(journeyManager.getPreviousJourney(), is(notNullValue()));
    }

    @Test
    public void should_update_subjourneyid_when_activity_changes() {
        setLikeActivityAverageResult(LikeActivity.Type.RUNNING, 88);
        setAndroidLocation(1d, 2d, 3L, 4f, 5f);
        journeyManager.updateJourney();

        long firstSubJourneyId = journeyManager.getCurrentJourney().getLastSubJourneyJourneyId();

        setLikeActivityAverageResult(LikeActivity.Type.IN_VEHICLE, 22);
        journeyManager.updateJourney();

        assertThat(journeyManager.getCurrentJourney().getLastSubJourneyJourneyId(), is(not(firstSubJourneyId)));
        assertThat(journeyManager.getCurrentJourney().getJourneyId(),
                is(not(journeyManager.getCurrentJourney().getLastSubJourneyJourneyId())));
    }

    // UTIL

    private void setLikeActivityAverageResult(LikeActivity.Type type, int confidence) {
        LikeActivity likeActivity = new LikeActivity(type, confidence);
        when(activityRecognitionUpdateHandler.getAverageLikeActivity()).thenReturn(likeActivity);
    }

    private void setAndroidLocation(double latitude, double longitude, long time, float speed, float bearing) {
        Location mockedLocation = TestUtil.generateMockedLocation(latitude, longitude, time, speed, bearing);
        when(locationHandler.getAverageLocation()).thenReturn(mockedLocation);
    }

    private boolean isRecentTime(long time) {
        long secondsAgo = new Date().getTime() - 2000;
        return time > secondsAgo;
    }
}