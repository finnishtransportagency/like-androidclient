package fi.livi.like.client.android.background.datacollectors.activityrecognition;

import android.location.Location;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.slf4j.LoggerFactory;

import java.util.List;

import fi.livi.like.client.android.background.data.DataStorage;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;

public class LikeActivityConverter {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LikeActivityConverter.class);

    private final static float WALKING_TO_VEHICLE_SPEED = 5.6f; // 20km/h
    private final DataStorage dataStorage;

    public LikeActivityConverter(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public LikeActivity convertToLikeActivity(ActivityRecognitionResult activityRecognitionResult) {

        if (activityRecognitionResult == null) {
            return null;
        }

        LikeActivity.Type likeType = null;
        DetectedActivity detectedActivity = activityRecognitionResult.getMostProbableActivity();
        switch (detectedActivity.getType()) {
            case DetectedActivity.IN_VEHICLE :
                likeType = LikeActivity.Type.IN_VEHICLE;
                break;
            case DetectedActivity.ON_BICYCLE :
                likeType = LikeActivity.Type.ON_BICYCLE;
                break;
            case DetectedActivity.RUNNING :
                likeType = LikeActivity.Type.RUNNING;
                break;
            case DetectedActivity.WALKING :
                likeType = LikeActivity.Type.WALKING;
                break;
            case DetectedActivity.ON_FOOT :
                detectedActivity = walkingOrRunning(activityRecognitionResult.getProbableActivities());
                if (detectedActivity == null) {
                    log.debug("ON_FOOT and not walking or running");
                } else if (detectedActivity.getType() == DetectedActivity.WALKING) {
                    likeType = LikeActivity.Type.WALKING;
                } else if (detectedActivity.getType() == DetectedActivity.RUNNING) {
                    likeType = LikeActivity.Type.RUNNING;
                }
                break;
            default:
                log.debug("no matching like activity found");
        }

        if (isValidLikeActivityType(likeType)) {
            likeType = adjustActivityBasedOnCurrentLocationData(likeType);
            return new LikeActivity(likeType, detectedActivity.getConfidence());
        } else {
            return null;
        }
    }

    private LikeActivity.Type adjustActivityBasedOnCurrentLocationData(LikeActivity.Type likeType) {
        if (LikeActivity.Type.WALKING == likeType) {
            final Location currentLocation = dataStorage.getLastLocation();
            if (currentLocation != null && currentLocation.getSpeed() > WALKING_TO_VEHICLE_SPEED) {
                likeType = LikeActivity.Type.IN_VEHICLE;
            }
        }

        return likeType;
    }

    private boolean isValidLikeActivityType(LikeActivity.Type likeType) {
        return LikeActivity.Type.IN_VEHICLE == likeType ||
                LikeActivity.Type.ON_BICYCLE == likeType ||
                LikeActivity.Type.RUNNING == likeType ||
                LikeActivity.Type.WALKING == likeType;
    }

    private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
        DetectedActivity detectedActivity = null;
        for (DetectedActivity activity : probableActivities) {
            if (activity.getType() == DetectedActivity.RUNNING || activity.getType() == DetectedActivity.WALKING) {
                detectedActivity = activity;
                break;
            }
        }
        return detectedActivity;
    }
}
