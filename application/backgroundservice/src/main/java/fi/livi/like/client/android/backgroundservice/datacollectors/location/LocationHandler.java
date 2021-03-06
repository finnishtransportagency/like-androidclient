package fi.livi.like.client.android.backgroundservice.datacollectors.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.data.DataStorage;

/**
 * Handle location updates
 */
public class LocationHandler implements LocationListener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LocationHandler.class);

    private final Listener listener;
    private final DataStorage dataStorage;
    private final LocationManagerHandler locationManagerHandler;
    private LocationFilter locationFilter;
    private LocationAverager locationAverager;

    private Location previousLocation;

    public interface Listener {
        void onLocationUpdated();
    }

    public LocationHandler(LikeService likeService, Listener listener) {
        locationFilter = new LocationFilter();
        locationAverager = new LocationAverager();
        this.listener = listener;
        this.dataStorage = likeService.getDataStorage();
        this.locationManagerHandler =
                new LocationManagerHandler(likeService.getBackgroundService(), this, likeService.getDataStorage().getConfiguration().getInternalGpsInterval());
    }

    public void requestLocationUpdates() {
        locationManagerHandler.requestLocationUpdates();
    }

    public void stopLocationUpdates() {
        locationAverager.reset();
        locationManagerHandler.stopLocationUpdates();
        dataStorage.setLastLocation(null);
    }

    public Location getAverageLocation() {
        synchronized (this) {
            return locationAverager.getAverageLocation();
        }
    }

    @Override
    public void onLocationChanged(Location currentLocation) {
        synchronized (this) {
            if (locationFilter.isValidLocation(previousLocation, currentLocation)) {
                locationAverager.onLocationUpdate(currentLocation);
                previousLocation = currentLocation;
                dataStorage.setLastLocation(currentLocation);
                if (listener != null) {
                    listener.onLocationUpdated();
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log.debug("onStatusChanged - provider:" + provider + " status:" + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        log.debug("onProviderEnabled - provider:" + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        log.debug("onProviderDisabled - provider:" + provider);
    }
}
