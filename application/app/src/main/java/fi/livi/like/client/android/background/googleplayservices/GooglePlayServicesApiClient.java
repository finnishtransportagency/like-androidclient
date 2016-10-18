package fi.livi.like.client.android.background.googleplayservices;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.background.data.DataStorage;

public class GooglePlayServicesApiClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(GooglePlayServicesApiClient.class);

    private ActivityRecognitionUpdateHandler activityUpdateHandler;
    private final GoogleApiClient googleApiClient;
    private final Context context;

    public GooglePlayServicesApiClient(Context context) {
        this.context = context;
        this.googleApiClient = buildGoogleApiClient();
    }

    public void close() {
        stopListenerUpdates();
        googleApiClient.disconnect();
    }

    private void connect() {
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    private void restartActivityRecognitionUpdates() {
        if (activityUpdateHandler != null && !activityUpdateHandler.isListening()) {
            activityUpdateHandler.startRequestingActivityRecognitionUpdates();
        }
    }

    public void stopActivityRecognitionUpdates() {
        if (activityUpdateHandler != null && activityUpdateHandler.isListening()) {
            activityUpdateHandler.stopRequestingActivityRecognitionUpdates();
        }
    }

    public void restartListenerUpdates() {
        restartActivityRecognitionUpdates();
    }

    public void stopListenerUpdates() {
        stopActivityRecognitionUpdates();
    }

    @Override
    public void onConnected(Bundle bundle) {
        restartListenerUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log.warn("onConnectionFailed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        log.warn("onConnectionSuspended");
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }

    public void startActivityRecognitionUpdates(DataStorage dataStorage, ActivityRecognitionRequest request, ActivityRecognitionListener listener) {
        stopActivityRecognitionUpdates();
        activityUpdateHandler = new ActivityRecognitionUpdateHandler(context, googleApiClient, dataStorage, request, listener);
        if (googleApiClient.isConnected()) {
            activityUpdateHandler.startRequestingActivityRecognitionUpdates();
        } else {
            connect();
        }
    }

    public ActivityRecognitionUpdateHandler getActivityRecognitionUpdateHandler() {
        return activityUpdateHandler;
    }
}
