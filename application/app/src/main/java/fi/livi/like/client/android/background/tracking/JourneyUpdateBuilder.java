package fi.livi.like.client.android.background.tracking;

import java.util.Date;

import fi.livi.like.client.android.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;
import fi.livi.like.client.android.dependencies.backend.LikeLocation;

public class JourneyUpdateBuilder {

    private String userId;
    private long journeyId;
    private long subJourneyId;
    private Date timestamp;
    private LikeLocation likeLocation;
    private LikeActivity likeActivity;

    public JourneyUpdateBuilder() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(long journeyId) {
        this.journeyId = journeyId;
    }

    public long getSubJourneyId() {
        return subJourneyId;
    }

    public void setSubJourneyId(long subJourneyId) {
        this.subJourneyId = subJourneyId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public LikeLocation getLikeLocation() {
        return likeLocation;
    }

    public void setLikeLocation(LikeLocation likeLocation) {
        this.likeLocation = likeLocation;
    }

    public LikeActivity getLikeActivity() {
        return likeActivity;
    }

    public void setLikeActivity(LikeActivity likeActivity) {
        this.likeActivity = likeActivity;
    }

    public JourneyUpdate build() {
        return new JourneyUpdate(userId, journeyId, subJourneyId, timestamp, likeLocation, likeActivity);
    }

    public boolean isJourneyUpdateValid() {
        return userId != null && !userId.isEmpty() &&
                journeyId != 0 &&
                subJourneyId != 0 &&
                timestamp != null &&
                likeLocation != null &&
                likeActivity != null;
    }
}
