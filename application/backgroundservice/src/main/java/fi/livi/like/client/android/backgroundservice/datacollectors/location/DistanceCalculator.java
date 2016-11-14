package fi.livi.like.client.android.backgroundservice.datacollectors.location;

import android.location.Location;

public class DistanceCalculator {

    public float distanceBetween(double startLatitude, double startLongitude,
                         double endLatitude, double endLongitude) {
        float[] results = new float[3];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }
}
