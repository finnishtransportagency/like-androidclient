package fi.livi.like.client.android.background.datacollectors.location;

import android.location.Location;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class LocationAverager {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LocationAverager.class);

    private final int AVERAGING_LOCATION_QUEUE_MAX_SIZE = 5;
    private Location averageLocation = null;
    private ArrayList<Location> previousLocations = new ArrayList<>();

    public void onLocationUpdate(Location currentLocation) {
        log.trace("onLocationUpdate - currentLocation: " + currentLocation);
        previousLocations.add(currentLocation);
        if (previousLocations.size() > AVERAGING_LOCATION_QUEUE_MAX_SIZE) {
            previousLocations.remove(0);
        }
        averageLocation = calculateAverageLocation();
        log.trace("onLocationUpdate - averageLocation: " + averageLocation);
    }

    public Location getAverageLocation() {
        return averageLocation;
    }

    public void reset() {
        averageLocation = null;
        previousLocations.clear();
    }

    private Location calculateAverageLocation() {

        if (previousLocations.isEmpty()) {
            return null;
        }

        Location avgLocation = new Location(previousLocations.get(previousLocations.size()-1));// takes the last inserted as a base

        // currently averages only longitude & latitude
        double longitudeSum = 0;
        double latitudeSum = 0;
        Location tempLocation;
        for (Location previousLocation : previousLocations) {
            tempLocation = previousLocation;
            longitudeSum = longitudeSum + tempLocation.getLongitude();
            latitudeSum = latitudeSum + tempLocation.getLatitude();
        }
        avgLocation.setLongitude(longitudeSum / previousLocations.size());
        avgLocation.setLatitude(latitudeSum / previousLocations.size());

        return avgLocation;
    }
}