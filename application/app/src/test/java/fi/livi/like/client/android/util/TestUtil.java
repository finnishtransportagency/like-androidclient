package fi.livi.like.client.android.util;

import android.location.Location;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {

    public static Location generateLocation(double latitude, double longitude, long time, float speed, float bearing) {
        Location location = new Location("gps");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(time);
        location.setSpeed(speed);
        location.setBearing(bearing);

        return location;
    }

    public static Location generateMockedLocation(double latitude, double longitude, long time, float speed, float bearing) {
        Location location = mock(Location.class);
        when(location.getLatitude()).thenReturn(latitude);
        when(location.getLongitude()).thenReturn(longitude);
        when(location.getSpeed()).thenReturn(speed);
        when(location.getBearing()).thenReturn(bearing);
        when(location.getTime()).thenReturn(time);

        return location;
    }
}