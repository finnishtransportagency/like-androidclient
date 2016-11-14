package fi.livi.like.client.android.backgroundservice.http.tasks;

import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import fi.livi.like.client.android.backgroundservice.dependencies.backend.User;
import fi.livi.like.client.android.backgroundservice.http.HttpLoader;

public class UserLoadingTask implements Runnable {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(UserLoadingTask.class);

    private int failedCounter = 0;
    private User user;
    private HttpLoader httpLoader;

    public UserLoadingTask(HttpLoader httpLoader, User user) {
        this.httpLoader = httpLoader;
        this.user = user;
    }

    @Override
    public void run() {
        try {
            RestTemplate restTemplate = httpLoader.getRestTemplate();
            User loadedUser = restTemplate.postForObject(httpLoader.getDataStorage().getConfiguration().getBackendGetUserUrl(), user, User.class);
            httpLoader.getDataStorage().setUser(loadedUser);
            httpLoader.broadcastCompletion(HttpLoader.OperationType.USER_LOADING, HttpLoader.NOTIFY_COMPLETED_ID);
        } catch (Exception exception) {
            log.warn("UserLoadingTask failed - " + exception.getMessage());
            failedCounter++;
            if (failedCounter < 3) {
                log.info("retry user loading");
                run();
            } else {
                log.warn("cannot load user!!");
                httpLoader.broadcastCompletion(HttpLoader.OperationType.USER_LOADING, HttpLoader.NOTIFY_FAILED_ID);
            }
        }
    }
}