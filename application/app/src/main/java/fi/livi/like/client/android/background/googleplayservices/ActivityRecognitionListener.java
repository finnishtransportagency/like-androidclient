package fi.livi.like.client.android.background.googleplayservices;

import com.google.android.gms.location.ActivityRecognitionResult;

public interface ActivityRecognitionListener {
    void onActivityRecognitionUpdate(ActivityRecognitionResult result);
}
