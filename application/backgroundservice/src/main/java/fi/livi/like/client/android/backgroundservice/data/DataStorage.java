package fi.livi.like.client.android.backgroundservice.data;

import android.location.Location;

import fi.livi.like.client.android.backgroundservice.Configuration;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeActivity;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.User;

public class DataStorage {

    private User user;
    private Configuration configuration = new Configuration();
    private Location lastLocation;// raw, not average
    private LikeActivity lastAverageLikeActivity;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void setLastAverageLikeActivity(LikeActivity lastAverageLikeActivity) {
        this.lastAverageLikeActivity = lastAverageLikeActivity;
    }

    public LikeActivity getLastAverageLikeActivity() {
        return lastAverageLikeActivity;
    }
}
