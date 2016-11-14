package fi.livi.like.client.android.backgroundservice.tracking;

public class TrackingStrings {
    private final String trackingDisabled;
    private final String trackingOff;
    private final String trackingOn;

    public TrackingStrings(String trackingDisabled, String trackingOff, String trackingOn) {
        this.trackingDisabled = trackingDisabled;
        this.trackingOff = trackingOff;
        this.trackingOn = trackingOn;
    }

    public String getTrackingDisabled() {
        return trackingDisabled;
    }

    public String getTrackingOff() {
        return trackingOff;
    }

    public String getTrackingOn() {
        return trackingOn;
    }
}
