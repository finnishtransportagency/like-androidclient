package fi.livi.like.client.android.backgroundservice.background;

import android.location.Location;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import fi.livi.like.client.android.backgroundservice.BuildConfig;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationBearingAngleTracker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class LocationBearingAngleTrackerUnitTest {

    @Test
    public void should_return_false_until_calculation_can_be_done_and_bearing_not_set() {
        LocationBearingAngleTracker bearingAngleTracker = new LocationBearingAngleTracker(360f);
        // needs 2 locations to get bearing to compare to (if bearing is not set)
        boolean result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(0, 0));
        assertThat(result, is(false));
        result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(1, 1));
        assertThat(result, is(false));
        result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(2, 2));
        assertThat(result, is(true));
    }

    @Test
    public void should_return_false_until_calculation_can_be_done_and_bearing_is_set() {
        LocationBearingAngleTracker bearingAngleTracker = new LocationBearingAngleTracker(360f);
        // needs 1 locations to get bearing to compare to (if bearing is set)
        boolean result = bearingAngleTracker.isBearingAngleWithinLimits(newLocationWithBearing(0, 0, 10f));
        assertThat(result, is(false));
        result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(1, 2));
        assertThat(result, is(true));
    }

    @Test
    public void should_return_true_when_location_bearing_within_limit() {
        LocationBearingAngleTracker bearingAngleTracker = new LocationBearingAngleTracker(1f);

        bearingAngleTracker.isBearingAngleWithinLimits(newLocationWithBearing(0, 0, 45f));
        boolean result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(1, 1));// ~0.2 degrees

        assertThat(result, is(true));
    }

    @Test
    public void should_return_true_when_location_bearing_below_limit_initial_bearing_near_360() {
        LocationBearingAngleTracker bearingAngleTracker = new LocationBearingAngleTracker(60f);

        bearingAngleTracker.isBearingAngleWithinLimits(newLocationWithBearing(0, 0, 350f));
        boolean result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(1, 1));// ~55.2 degrees

        assertThat(result, is(true));
    }

    @Test
    public void should_return_true_when_location_bearing_below_limit_location_bearing_near_360() {
        LocationBearingAngleTracker bearingAngleTracker = new LocationBearingAngleTracker(90f);

        bearingAngleTracker.isBearingAngleWithinLimits(newLocationWithBearing(0, 0, 0f));
        boolean result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(0, -1));// ~90 degrees

        assertThat(result, is(true));
    }

    @Test
    public void should_compare_always_to_last_value() {
        LocationBearingAngleTracker bearingAngleTracker = new LocationBearingAngleTracker(15f);

        bearingAngleTracker.isBearingAngleWithinLimits(newLocationWithBearing(0, 0, 0f));
        boolean result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(-1, 0));// ~180 degrees
        assertThat(result, is(false));
        result = bearingAngleTracker.isBearingAngleWithinLimits(newLocation(-2, 0.2));// ~11.4 degrees
        assertThat(result, is(true));
    }

    //

    private Location newLocation(double latitude, double longitude) {
        Location location = new Location("GPS");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    private Location newLocationWithBearing(double latitude, double longitude, float bearing) {
        Location location = new Location("GPS");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setBearing(bearing);
        return location;
    }
}
