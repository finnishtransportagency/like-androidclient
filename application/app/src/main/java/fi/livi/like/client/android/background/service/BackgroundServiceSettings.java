package fi.livi.like.client.android.background.service;

import android.app.Notification;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.LocationRequest;

import fi.livi.like.client.android.background.googleplayservices.ActivityRecognitionRequest;

/**
 * BackgroundServiceSettings describes the starting parameters which can be given via Intent
 * when launching the BackgroundService.
 */
public class BackgroundServiceSettings implements Parcelable {

    public static final String SERVICESETTINGS_INTENT_ID = "fi.livi.like.client.android.backgroundservice.settings.intent.id";

    /**
     * Service life restart policy tells how to proceed if service is killed by system
     * START_NOT_STICKY - will not be restarted before next call to startService()
     * START_STICKY - will be restarted later by system (Intent can be null)
     * START_REDELIVER_INTENT - will be restarted later and the last delivered Intent re-delivered to it again
     */
    private int restartPolicy = -1;

    /**
     *  Run service on foreground
     *  - Run service in a foreground state, where the system considers it to be something the user is actively aware of
     *  and thus not a candidate for killing when low on memory. When enabled, all three needs to be provided.
     */
    private boolean startForeground = false;
    private int notificationId = -1;
    private Notification notification;

    /**
     *  Wakelock keeps certain parts of hardware alive, for example with PARTIAL_WAKE_LOCK the CPU is not let to go to sleep.
     *  - With wakelock-modes application needs to have "android.permission.WAKE_LOCK"
     */
    private int wakeLockMode = -1;

    /**
     * Location listener
     * Set LocationRequest to enable location listener via Intent.
     * - With some settings application needs to have "android.permission.ACCESS_FINE_LOCATION"
     */
    private LocationRequest locationRequest;

    /**
     * ActivityRecognition listener
     * Set ActivityRequest to enable activity listener via Intent.
     * - Application needs to have "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
     */
    private ActivityRecognitionRequest activityRecognitionRequest;

    // CONSTRUCTORS

    public BackgroundServiceSettings() {
    }

    public BackgroundServiceSettings(Parcel in) {
        readFromParcel(in);
    }

    // SETTERS/GETTERS

    public int getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(int restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public void enableServiceOnForeground(int notificationId, Notification notification) {
        startForeground = true;
        this.notificationId = notificationId;
        this.notification = notification;
    }

    public boolean isStartForeground() {
        return startForeground;
    }

    public void setStartForeground(boolean startForeground) {
        this.startForeground = startForeground;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public int getWakeLockMode() {
        return wakeLockMode;
    }

    public void setWakeLockMode(int wakeLockMode) {
        this.wakeLockMode = wakeLockMode;
    }

    public LocationRequest getLocationRequest() {
        return locationRequest;
    }

    public void setLocationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
    }

    public ActivityRecognitionRequest getActivityRecognitionRequest() {
        return activityRecognitionRequest;
    }

    public void setActivityRecognitionRequest(ActivityRecognitionRequest activityRecognitionRequest) {
        this.activityRecognitionRequest = activityRecognitionRequest;
    }

    @Override
    public String toString() {
        return "BackgroundServiceSettings{" +
                "restartPolicy=" + restartPolicy +
                ", startForeground=" + startForeground +
                ", notificationId=" + notificationId +
                ", notification=" + notification +
                ", wakeLockMode=" + wakeLockMode +
                ", locationRequest=" + locationRequest +
                ", activityRecognitionRequest=" + activityRecognitionRequest +
                '}';
    }

    // PARCEABLE

    protected void readFromParcel(Parcel in) {
        restartPolicy = in.readInt();
        startForeground = in.readByte() != 0;
        notificationId = in.readInt();
        notification = in.readParcelable(Notification.class.getClassLoader());
        wakeLockMode = in.readInt();
        locationRequest = in.readParcelable(LocationRequest.class.getClassLoader());
        activityRecognitionRequest = in.readParcelable(ActivityRecognitionRequest.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(restartPolicy);
        dest.writeByte((byte)(startForeground ? 1 : 0));
        dest.writeInt(notificationId);
        dest.writeParcelable(notification, flags);
        dest.writeInt(wakeLockMode);
        dest.writeParcelable(locationRequest, flags);
        dest.writeParcelable(activityRecognitionRequest, flags);
    }

    public static final Creator<BackgroundServiceSettings> CREATOR = new Creator<BackgroundServiceSettings>() {
        @Override
        public BackgroundServiceSettings createFromParcel(Parcel in) {
            return new BackgroundServiceSettings(in);
        }

        @Override
        public BackgroundServiceSettings[] newArray(int size) {
            return new BackgroundServiceSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
