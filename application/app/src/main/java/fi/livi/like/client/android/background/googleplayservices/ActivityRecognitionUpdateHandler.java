package fi.livi.like.client.android.background.googleplayservices;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.background.datacollectors.activityrecognition.LikeActivityAverager;
import fi.livi.like.client.android.background.datacollectors.activityrecognition.LikeActivityConverter;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;

public class ActivityRecognitionUpdateHandler {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(ActivityRecognitionUpdateHandler.class);

    private static final String ACTIVITY_RECOGNITION_INTENT = "fi.livi.like.client.android.backgroundservice.ACTIVITY_RECOGNITION_UPDATE";

    private final BroadcastReceiver broadcastReceiver = new ActivityRecognitionBroadcastReceiver();
    private PendingIntent pendingIntent;
    private ActivityRecognitionListener listener;
    private ActivityRecognitionRequest request;
    private LikeActivityConverter likeActivityConverter;
    private LikeActivityAverager likeActivityAverager;
    private GoogleApiClient googleApiClient;
    private Context context;
    private boolean isListening = false;

    public ActivityRecognitionUpdateHandler(
            Context context,
            GoogleApiClient googleApiClient,
            ActivityRecognitionRequest request,
            ActivityRecognitionListener listener) {
        this.request = request;
        this.googleApiClient = googleApiClient;
        this.context = context;
        this.listener = listener;
        likeActivityConverter = new LikeActivityConverter();
        likeActivityAverager = new LikeActivityAverager();
    }

    public void startRequestingActivityRecognitionUpdates() {
        log.info("startRequestingActivityRecognitionUpdates");
        pendingIntent = createPendingIntent();
        context.registerReceiver(broadcastReceiver, new IntentFilter(ACTIVITY_RECOGNITION_INTENT));

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                request.getActivityUpdateIntervalInSecs() * 1000,
                pendingIntent);
        isListening = true;
    }

    public void stopRequestingActivityRecognitionUpdates() {
        log.info("stopRequestingActivityRecognitionUpdates");
        context.unregisterReceiver(broadcastReceiver);
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, pendingIntent);
        isListening = false;
    }

    public LikeActivity getAverageLikeActivity() {
        return likeActivityAverager.getAverageLikeActivity();
    }

    public void setListener(ActivityRecognitionListener listener) {
        this.listener = listener;
    }

    public boolean isListening() {
        return isListening;
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(ACTIVITY_RECOGNITION_INTENT);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult activityRecognitionResult = ActivityRecognitionResult.extractResult(intent);
                LikeActivity likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
                likeActivityAverager.onLikeActivityUpdate(likeActivity);

                if (listener != null) {
                    listener.onActivityRecognitionUpdate(activityRecognitionResult);
                }
            }
        }
    }
}
