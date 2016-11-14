package fi.livi.like.client.android.backgroundservice.background;

import android.location.Location;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import fi.livi.like.client.android.backgroundservice.BuildConfig;
import fi.livi.like.client.android.backgroundservice.background.util.TestUtil;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationAverager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class LocationAveragerUnitTest {

    LocationAverager locationAverager;

    @Before
    public void setup() {
        locationAverager = new LocationAverager();
    }

    @Test
    public void should_return_null_as_average_location_when_empty_queue() {
        assertThat(locationAverager.getAverageLocation(), is(nullValue()));
    }

    @Test
    public void should_return_location_when_1_sample() {
        Location location = TestUtil.generateLocation(1, 1, 1, 1, 1);

        locationAverager.onLocationUpdate(location);

        assertThat(locationAverager.getAverageLocation().getLatitude(), is(location.getLatitude()));
        assertThat(locationAverager.getAverageLocation().getLongitude(), is(location.getLongitude()));
    }

    @Test
    public void should_calculate_average_location_from_2_samples() {
        Location location = TestUtil.generateLocation(1, 1, 1, 1, 1);
        Location location2 = TestUtil.generateLocation(3, 3, 1, 1, 1);

        locationAverager.onLocationUpdate(location);
        locationAverager.onLocationUpdate(location2);

        assertThat(locationAverager.getAverageLocation().getLatitude(), is(2.0));
        assertThat(locationAverager.getAverageLocation().getLongitude(), is(2.0));
    }

    @Test
    public void should_use_5_previous_locations_when_averaging() {
        Location location = TestUtil.generateLocation(1, 1, 1, 1, 1);
        Location location2 = TestUtil.generateLocation(3, 3, 1, 1, 1);

        locationAverager.onLocationUpdate(location);
        locationAverager.onLocationUpdate(location2);
        locationAverager.onLocationUpdate(location2);
        locationAverager.onLocationUpdate(location2);
        locationAverager.onLocationUpdate(location2);
        locationAverager.onLocationUpdate(location2);

        assertThat(locationAverager.getAverageLocation().getLatitude(), is(location2.getLatitude()));
        assertThat(locationAverager.getAverageLocation().getLongitude(), is(location2.getLongitude()));
    }

    @Test
    public void should_reset_average_location() {
        Location location = TestUtil.generateLocation(1, 1, 1, 1, 1);
        Location location2 = TestUtil.generateLocation(3, 3, 1, 1, 1);

        locationAverager.onLocationUpdate(location);
        locationAverager.onLocationUpdate(location2);
        locationAverager.reset();

        assertThat(locationAverager.getAverageLocation(), is(nullValue()));

        locationAverager.onLocationUpdate(location);
        assertThat(locationAverager.getAverageLocation().getLatitude(), is(location.getLatitude()));
        assertThat(locationAverager.getAverageLocation().getLongitude(), is(location.getLongitude()));
    }
}
