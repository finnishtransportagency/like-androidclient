package fi.livi.like.client.android.backgroundservice.tracking;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.backgroundservice.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeActivity;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeLocation;

public class Journey {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(Journey.class);

    private long journeyId = 0;
    private long lastSubJourneyJourneyId = 0;
    private LikeActivity lastLikeActivity;
    private LikeLocation lastLikeLocation;
    private JourneyUpdate lastJourneyUpdate;

    public Journey() {
    }

    public long getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(long journeyId) {
        this.journeyId = journeyId;
    }

    public long getLastSubJourneyJourneyId() {
        return lastSubJourneyJourneyId;
    }

    public void setLastSubJourneyJourneyId(long lastSubJourneyJourneyId) {
        this.lastSubJourneyJourneyId = lastSubJourneyJourneyId;
    }

    public LikeActivity getLastLikeActivity() {
        return lastLikeActivity;
    }

    public void setLastLikeActivity(LikeActivity lastLikeActivity) {
        this.lastLikeActivity = lastLikeActivity;
    }

    public LikeLocation getLastLikeLocation() {
        return lastLikeLocation;
    }

    public void setLastLikeLocation(LikeLocation lastLikeLocation) {
        this.lastLikeLocation = lastLikeLocation;
    }

    public JourneyUpdate getLastJourneyUpdate() {
        return lastJourneyUpdate;
    }

    public void setLastJourneyUpdate(JourneyUpdate lastJourneyUpdate) {
        this.lastJourneyUpdate = lastJourneyUpdate;
    }
}
