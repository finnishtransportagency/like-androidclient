package fi.livi.like.client.android.backgroundservice.googleplayservices;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivityRecognitionRequest implements Parcelable {

    private int activityUpdateIntervalInSecs;

    public ActivityRecognitionRequest(int activityUpdateIntervalInSecs) {
        this.activityUpdateIntervalInSecs = activityUpdateIntervalInSecs;
    }

    protected ActivityRecognitionRequest(Parcel in) {
        readFromParcel(in);
    }

    public int getActivityUpdateIntervalInSecs() {
        return activityUpdateIntervalInSecs;
    }

    public void setActivityUpdateIntervalInSecs(int activityUpdateIntervalInSecs) {
        this.activityUpdateIntervalInSecs = activityUpdateIntervalInSecs;
    }

    // PARCEABLE

    protected void readFromParcel(Parcel in) {
        activityUpdateIntervalInSecs = in.readInt();
    }

    public static final Creator<ActivityRecognitionRequest> CREATOR = new Creator<ActivityRecognitionRequest>() {
        @Override
        public ActivityRecognitionRequest createFromParcel(Parcel in) {
            return new ActivityRecognitionRequest(in);
        }

        @Override
        public ActivityRecognitionRequest[] newArray(int size) {
            return new ActivityRecognitionRequest[size];
        }
    };

    public void setActivityUpdateInterval(int activityUpdateInterval) {
        this.activityUpdateIntervalInSecs = activityUpdateInterval;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(activityUpdateIntervalInSecs);
    }
}
