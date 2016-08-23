package fi.livi.like.client.android.background.data;

import fi.livi.like.client.android.Configuration;
import fi.livi.like.client.android.dependencies.backend.User;

public class DataStorage {

    private User user;
    private Configuration configuration = new Configuration();

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
}
