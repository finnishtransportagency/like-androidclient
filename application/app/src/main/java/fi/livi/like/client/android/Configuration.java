package fi.livi.like.client.android;

public class Configuration {
    // Network related
    private final int NETWORK_TIMEOUT = 15 * 1000;
    private final String HTTPS_CERT_FILE = ""; // add here the https-certificate of your system
    // URLs
    private final String BACKEND_SERVER_URL = ""; // add here the backend url of your system
    private final String BACKEND_GET_USER_URL = BACKEND_SERVER_URL + ""; // add here the backend url of your system for loading user
    private final String BACKEND_JOURNEY_UPDATE_URL = BACKEND_SERVER_URL + ""; // add here the backend url of your system for updating journey data
    // Intervals / delays
    private final int INTERNAL_INACTIVE_ACTIVITY_RECOG_INTERVAL = 30;
    private final int INTERNAL_ACTIVE_ACTIVITY_RECOG_INTERVAL = 10;
    private final int INTERNAL_GPS_INTERVAL = 1;
    private final int INTERNAL_INACTIVITY_DELAY = 60 * 5;
    private final int TRACKING_USER_INTERVAL = 5;
    // Distances
    private final float DISTANCE_TO_TRIGGER_NEW_JOURNEY = 50;

    public int getNetworkTimeout() {
        return NETWORK_TIMEOUT;
    }

    public String getHttpsCertFile() {
        return HTTPS_CERT_FILE;
    }

    public String getBackendGetUserUrl() {
        return BACKEND_GET_USER_URL;
    }

    public String getBackendJourneyUpdateUrl() {
        return BACKEND_JOURNEY_UPDATE_URL;
    }

    public int getInternalInactiveActivityRecogInterval() {
        return INTERNAL_INACTIVE_ACTIVITY_RECOG_INTERVAL;
    }

    public int getInternalActiveActivityRecogInterval() {
        return INTERNAL_ACTIVE_ACTIVITY_RECOG_INTERVAL;
    }

    public int getInternalGpsInterval() {
        return INTERNAL_GPS_INTERVAL;
    }

    public int getInternalInactivityDelay() {
        return INTERNAL_INACTIVITY_DELAY;
    }

    public int getTrackingUserInterval() {
        return TRACKING_USER_INTERVAL;
    }

    public float getDistanceToTriggerNewJourney() {
        return DISTANCE_TO_TRIGGER_NEW_JOURNEY;
    }
}
