package fi.livi.like.client.android.backgroundservice.http.tasks;

import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import fi.livi.like.client.android.backgroundservice.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.backgroundservice.http.HttpLoader;

public class JourneyUpdatePostTask implements Runnable {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(JourneyUpdatePostTask.class);

    private HttpLoader httpLoader;
    private List<JourneyUpdate> journeyUpdates;

    public JourneyUpdatePostTask(HttpLoader httpLoader, List<JourneyUpdate> journeyUpdates) {
        this.httpLoader = httpLoader;
        this.journeyUpdates = journeyUpdates;
    }

    @Override
    public void run() {
        try {
            RestTemplate restTemplate = httpLoader.getRestTemplate();
            restTemplate.postForObject(httpLoader.getDataStorage().getConfiguration().getBackendJourneyUpdateUrl(), journeyUpdates, JourneyUpdate[].class);
            httpLoader.broadcastCompletion(HttpLoader.OperationType.JOURNEY_UPDATE, HttpLoader.NOTIFY_COMPLETED_ID);
        } catch (Exception exception) {
            log.warn("JourneyUpdatePostTask failed with " + journeyUpdates.size() + " update(s) - " + exception.getMessage());
            httpLoader.broadcastCompletion(HttpLoader.OperationType.JOURNEY_UPDATE, HttpLoader.NOTIFY_FAILED_ID);
            httpLoader.getFailedJourneyUpdateHandler().addFailedUpdates(journeyUpdates);
        }
    }
}