package fi.livi.like.client.android.backgroundservice.background;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import fi.livi.like.client.android.backgroundservice.background.util.TestUtil;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationFilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LocationFilterUnitTest {

    LocationFilter locationFilter;

    @Before
    public void setup() {
        locationFilter = new LocationFilter();
    }

    @Test
    public void should_validate_location_when_no_previous_location() {
        Location currentLocation = TestUtil.generateMockedLocation(1F, 1F, new Date(11000).getTime(), 7F, 1F);

        assertThat(locationFilter.isValidLocation(null, currentLocation), is(true));
    }

    @Test
    public void should_validate_location_when_location_under_threshold() {
        Location previousLocation = TestUtil.generateMockedLocation(1F, 1F, new Date(10000).getTime(), 1F, 1F);
        Location currentLocation = TestUtil.generateMockedLocation(1F, 1F, new Date(11000).getTime(), 5F, 1F);

        assertThat(locationFilter.isValidLocation(previousLocation, currentLocation), is(true));
    }

    @Test
    public void should_invalidate_location_with_too_high_acceleration() {
        Location previousLocation = TestUtil.generateMockedLocation(1F, 1F, new Date(10000).getTime(), 1F, 1F);
        Location currentLocation = TestUtil.generateMockedLocation(1F, 1F, new Date(11000).getTime(), 7.1F, 1F);

        assertThat(locationFilter.isValidLocation(previousLocation, currentLocation), is(false));
    }
}
