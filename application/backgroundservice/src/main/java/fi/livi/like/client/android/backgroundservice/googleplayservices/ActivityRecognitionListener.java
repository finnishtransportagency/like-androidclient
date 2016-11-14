package fi.livi.like.client.android.backgroundservice.googleplayservices;

import com.google.android.gms.location.ActivityRecognitionResult;

public interface ActivityRecognitionListener {
    void onActivityRecognitionUpdate(ActivityRecognitionResult result);
}
