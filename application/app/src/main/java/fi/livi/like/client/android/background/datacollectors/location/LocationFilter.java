package fi.livi.like.client.android.background.datacollectors.location;

import android.location.Location;

import org.slf4j.LoggerFactory;

public class LocationFilter {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LocationFilter.class);

    private final float MAX_LEVEL_OF_ACC = 6.0f;

    public boolean isValidLocation(Location previousLocation, Location currentLocation) {
        return previousLocation == null || isValidAcceleration(previousLocation, currentLocation);

    }

    private boolean isValidAcceleration(Location previousLocation, Location currentLocation) {
        float secsBetweenLocations = (currentLocation.getTime() - previousLocation.getTime()) / 1000;
        float acceleration = Math.abs((previousLocation.getSpeed() - currentLocation.getSpeed()) / secsBetweenLocations);
        if (acceleration <= MAX_LEVEL_OF_ACC) {
            return true;
        } else {
            log.info("invalid location update, too high acceleration -> " + acceleration);
            return false;
        }
    }
}
