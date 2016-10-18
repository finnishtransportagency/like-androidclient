package fi.livi.like.client.android.background.datacollectors.activityrecognition;

import android.location.Location;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.background.data.DataStorage;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;

public class LikeActivityFilter {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LikeActivityFilter.class);

    private final DataStorage dataStorage;
    private LikeActivity previousLikeActivity;

    public LikeActivityFilter(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public boolean isValidLikeActivity(LikeActivity likeActivity) {
        if (likeActivity == null) {
            return false;
        }

        if (previousLikeActivity != null && previousLikeActivity.getType() != likeActivity.getType()) {
            return isValidActivityChange(likeActivity);
        }

        previousLikeActivity = likeActivity;
        return true;
    }

    private boolean isValidActivityChange(LikeActivity currentLikeActivity) {
        if (dataStorage == null) {
            log.warn("datastorage is null, cannot validate activity!");
            return false;
        }

        final Location location = dataStorage.getLastLocation();
        if (location != null) {
            final double speed = location.getSpeed() * 3.6;
            // WALKING -> IN_VEHICLE
            if (previousLikeActivity.getType() == LikeActivity.Type.WALKING &&
                currentLikeActivity.getType() == LikeActivity.Type.IN_VEHICLE) {
                if (speed > dataStorage.getConfiguration().getWalkToVehicleMinSpeed()) {
                    return true;
                } else {
                    log.debug("too low speed for WALK->IN_VEHICLE activity change - speed:" + speed);
                    return false;
                }
            }
            // ON_BICYCLE -> IN_VEHICLE
            if (previousLikeActivity.getType() == LikeActivity.Type.ON_BICYCLE &&
                    currentLikeActivity.getType() == LikeActivity.Type.IN_VEHICLE) {
                if (speed >= dataStorage.getConfiguration().getBicycleVehicleSpeedLevel()) {
                    return true;
                } else {
                    log.debug("too low speed for ON_BICYCLE->IN_VEHICLE activity change - speed:" + speed);
                    return false;
                }
            }
            // IN_VEHICLE -> ON_BICYCLE
            if (previousLikeActivity.getType() == LikeActivity.Type.IN_VEHICLE &&
                    currentLikeActivity.getType() == LikeActivity.Type.ON_BICYCLE) {
                if (speed < dataStorage.getConfiguration().getBicycleVehicleSpeedLevel()) {
                    return true;
                } else {
                    log.debug("too high speed for IN_VEHICLE->ON_BICYCLE activity change - speed:" + speed);
                    return false;
                }
            }
        }
        return true;
    }
}
