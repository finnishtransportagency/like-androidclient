package fi.livi.like.client.android.backgroundservice.datacollectors.location;

import android.location.Location;

import org.slf4j.LoggerFactory;

public class LocationBearingAngleTracker {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LocationBearingAngleTracker.class);

    public final static float JOURNEY_START_MAX_BEARING_ANGLE_CHANGE = 90f;

    private Location previousLocation = null;
    private float previousBearingAngle = 0f;
    private boolean previousBearingSet = false;
    private float maxAngleChange;

    public LocationBearingAngleTracker(float maxAngleChange) {
        this.maxAngleChange = maxAngleChange;
    }

    public boolean isBearingAngleWithinLimits(Location currentLocation) {
        if (previousLocation == null) {
            previousLocation = currentLocation;
            if (currentLocation.hasBearing()) {
                setPreviousBearing(currentLocation.getBearing());
            }
            return false;
        }

        final float currentBearingAngle = previousLocation.bearingTo(currentLocation);
        log.debug("isBearingAngleWithinLimits - currentBearingAngle: {}", currentBearingAngle);

        if (!previousBearingSet) {
            setPreviousBearing(currentBearingAngle);
            return false;
        }

        log.debug("isBearingAngleWithinLimits - previousBearingAngle: {}", previousBearingAngle);
        final float changeOfBearingAngle = 180 - Math.abs(Math.abs(previousBearingAngle - currentBearingAngle) - 180);
        log.debug("isBearingAngleWithinLimits - changeOfBearingAngle: {}", changeOfBearingAngle);
        final boolean isWithinBearingAngleLimits = changeOfBearingAngle <= maxAngleChange;

        previousLocation = currentLocation;
        setPreviousBearing(currentBearingAngle);

        return isWithinBearingAngleLimits;
    }

    private void setPreviousBearing(float bearingAngle) {
        previousBearingAngle = bearingAngle;
        previousBearingSet = true;
    }
}