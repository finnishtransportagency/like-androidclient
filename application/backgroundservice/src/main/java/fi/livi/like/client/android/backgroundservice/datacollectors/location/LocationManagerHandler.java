package fi.livi.like.client.android.backgroundservice.datacollectors.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import org.slf4j.LoggerFactory;

/**
 * LocationManagerHandler handles the separate location managers and location requests
 */
public class LocationManagerHandler {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LocationManagerHandler.class);

    private Context context;
    private LocationListener locationListener;

    private final int locationUpdateIntervalSecs;
    private LocationManager locationManager;
    private boolean isLocationManagerActive;

    public LocationManagerHandler(Context context, LocationListener locationListener, int locationUpdateIntervalSecs) {
        this.context = context;
        this.locationListener = locationListener;
        this.locationUpdateIntervalSecs = locationUpdateIntervalSecs;
    }

    public void requestLocationUpdates() {
        if (isLocationManagerActive) {
            log.warn("requestLocationUpdates -  location manager already running!");
            return;
        }

        log.info("requestLocationUpdates - requesting location gps-only listener");
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            log.warn("### requestLocationUpdates - no permission!");
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, locationUpdateIntervalSecs * 1000, 0, locationListener);
        isLocationManagerActive = true;
    }

    public void stopLocationUpdates() {
        if (isLocationManagerActive && locationManager != null) {
            log.debug("stopLocationUpdates");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                log.warn("### stopLocationUpdates - no permission!");
                return;
            }
            locationManager.removeUpdates(locationListener);
            isLocationManagerActive = false;
        }
    }
}
