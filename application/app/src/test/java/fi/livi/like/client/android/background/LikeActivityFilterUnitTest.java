package fi.livi.like.client.android.background;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;

import fi.livi.like.client.android.Configuration;
import fi.livi.like.client.android.background.data.DataStorage;
import fi.livi.like.client.android.background.datacollectors.activityrecognition.LikeActivityFilter;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LikeActivityFilterUnitTest {

    private LikeActivityFilter likeActivityFilter;

    private DataStorage dataStorage;
    private Configuration configuration;

    @Before
    public void setup() {
        dataStorage = mock(DataStorage.class);
        configuration = mock(Configuration.class);
        when(dataStorage.getConfiguration()).thenReturn(configuration);

        likeActivityFilter = new LikeActivityFilter(dataStorage);
    }

    @Test
    public void should_invalidate_activity_when_null_activity() {
        assertThat(likeActivityFilter.isValidLikeActivity(null), is(false));
    }

    @Test
    public void should_validate_activity_when_no_previous_activity() {
        LikeActivity likeActivity = mock(LikeActivity.class);

        assertThat(likeActivityFilter.isValidLikeActivity(likeActivity), is(true));
    }

    @Test
    public void should_validate_WALK_to_VEHICLE_when_no_location_available() {
        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.WALKING);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(true));
    }

    @Test
    public void should_invalidate_WALK_to_VEHICLE_when_speed_low() {
        when(configuration.getWalkToVehicleMinSpeed()).thenReturn(5);
        Location location = mock(Location.class);
        when(location.getSpeed()).thenReturn(1f);// < 5km/h
        when(dataStorage.getLastLocation()).thenReturn(location);

        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.WALKING);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(false));
    }

    @Test
    public void should_validate_WALK_to_VEHICLE_when_speed_fast_enough() {
        when(configuration.getWalkToVehicleMinSpeed()).thenReturn(5);
        Location location = mock(Location.class);
        when(location.getSpeed()).thenReturn(2f);// > 5km/h
        when(dataStorage.getLastLocation()).thenReturn(location);

        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.WALKING);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(true));
    }

    @Test
    public void should_invalidate_BICYCLE_to_VEHICLE_when_speed_low() {
        when(configuration.getBicycleVehicleSpeedLevel()).thenReturn(40);
        Location location = mock(Location.class);
        when(location.getSpeed()).thenReturn(11f);// < 40km/h
        when(dataStorage.getLastLocation()).thenReturn(location);

        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.ON_BICYCLE);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(false));
    }

    @Test
    public void should_validate_BICYCLE_to_VEHICLE_when_speed_fast_enough() {
        when(configuration.getBicycleVehicleSpeedLevel()).thenReturn(40);
        Location location = mock(Location.class);
        when(location.getSpeed()).thenReturn(11.5f);// > 40km/h
        when(dataStorage.getLastLocation()).thenReturn(location);

        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.ON_BICYCLE);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(true));
    }

    @Test
    public void should_invalidate_VEHICLE_to_BICYCLE_when_speed_too_high() {
        when(configuration.getBicycleVehicleSpeedLevel()).thenReturn(40);
        Location location = mock(Location.class);
        when(location.getSpeed()).thenReturn(11.5f);// > 40km/h
        when(dataStorage.getLastLocation()).thenReturn(location);

        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.ON_BICYCLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(false));
    }

    @Test
    public void should_validate_VEHICLE_to_BICYCLE_when_speed_low_enough() {
        when(configuration.getBicycleVehicleSpeedLevel()).thenReturn(40);
        Location location = mock(Location.class);
        when(location.getSpeed()).thenReturn(11f);// < 40km/h
        when(dataStorage.getLastLocation()).thenReturn(location);

        LikeActivity previousLikeActivity = mock(LikeActivity.class);
        when(previousLikeActivity.getType()).thenReturn(LikeActivity.Type.IN_VEHICLE);
        likeActivityFilter.isValidLikeActivity(previousLikeActivity);

        LikeActivity currentLikeActivity = mock(LikeActivity.class);
        when(currentLikeActivity.getType()).thenReturn(LikeActivity.Type.ON_BICYCLE);

        assertThat(likeActivityFilter.isValidLikeActivity(currentLikeActivity), is(true));
    }
}
