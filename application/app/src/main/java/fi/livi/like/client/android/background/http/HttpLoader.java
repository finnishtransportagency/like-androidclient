package fi.livi.like.client.android.background.http;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fi.livi.like.client.android.background.LikeService;
import fi.livi.like.client.android.background.data.DataStorage;
import fi.livi.like.client.android.background.http.tasks.JourneyUpdatePostTask;
import fi.livi.like.client.android.background.http.tasks.UserLoadingTask;
import fi.livi.like.client.android.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.dependencies.backend.User;

public class HttpLoader {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(HttpLoader.class);

    public final static String BROADCAST_ACTION_ID = "fi.livi.like.client.android.httploader";
    public final static String NOTIFY_COMPLETED_ID = BROADCAST_ACTION_ID + ".notify.completed";
    public final static String NOTIFY_FAILED_ID = BROADCAST_ACTION_ID + ".notify.failed";

    private final LikeService likeService;

    public enum OperationType {
        USER_LOADING,
        JOURNEY_UPDATE
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final RestTemplateProvider restTemplateProvider;
    private final FailedJourneyUpdateHandler failedJourneyUpdateHandler;
    private ScheduledFuture userLoaderHandle;

    public HttpLoader(LikeService likeService) {
        this.likeService = likeService;
        restTemplateProvider = new RestTemplateProvider(likeService);
        failedJourneyUpdateHandler = new FailedJourneyUpdateHandler(this, likeService.getDataStorage().getConfiguration());
    }

    public RestTemplate getRestTemplate() {
        return restTemplateProvider.getRestTemplate();
    }

    synchronized public void broadcastCompletion(OperationType operationType, String success) {
        Intent intent = new Intent(BROADCAST_ACTION_ID);
        intent.putExtra(operationType.toString(), success);
        LocalBroadcastManager.getInstance(likeService.getBackgroundService()).sendBroadcast(intent);
    }

    public DataStorage getDataStorage() {
        return likeService.getDataStorage();
    }

    public FailedJourneyUpdateHandler getFailedJourneyUpdateHandler() {
        return failedJourneyUpdateHandler;
    }

    // operations

    public void loadUser(User user) {
        stopOperation(userLoaderHandle);
        userLoaderHandle = scheduler.schedule(
                new UserLoadingTask(this, user), 0, TimeUnit.SECONDS);
    }

    public ScheduledFuture postJourneyUpdate(List<JourneyUpdate> journeyUpdates) {
        return scheduler.schedule(new JourneyUpdatePostTask(this, journeyUpdates), 0, TimeUnit.SECONDS);
    }

    private void stopOperation(ScheduledFuture task) {
        if (task != null) {
            task.cancel(true);
        }
    }
}