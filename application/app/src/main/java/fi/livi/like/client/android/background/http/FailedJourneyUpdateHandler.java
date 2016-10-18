package fi.livi.like.client.android.background.http;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import fi.livi.like.client.android.Configuration;
import fi.livi.like.client.android.background.util.UpdateTimer;
import fi.livi.like.client.android.dependencies.backend.JourneyUpdate;

public class FailedJourneyUpdateHandler implements UpdateTimer.Update {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(FailedJourneyUpdateHandler.class);
    private final int failedMaxCount;
    private final int delayBeforeResend;

    private final HttpLoader httpLoader;
    private UpdateTimer lastFailedTimer;
    private List<JourneyUpdate> allFailedUpdates = new ArrayList<>();

    public FailedJourneyUpdateHandler(HttpLoader httpLoader, Configuration configuration) {
        this.httpLoader = httpLoader;
        this.failedMaxCount = configuration.getFailedUpdateJourneyMaxCount();
        this.delayBeforeResend = configuration.getFailedUpdateJourneyResendDelay();
    }

    synchronized public void addFailedUpdates(List<JourneyUpdate> failedUpdates) {
        allFailedUpdates.addAll(failedUpdates);
        log.info("added failed journey updates, total count now: " + allFailedUpdates.size());
        while (allFailedUpdates.size() > failedMaxCount) {
            allFailedUpdates.remove(0);
        }
        log.info("total count after limiting:" + allFailedUpdates.size());
        resetTimer();
    }

    public List<JourneyUpdate> getFailedUpdates() {
        return allFailedUpdates;
    }

    private void resetTimer() {
        stopLastFailedTimer();
        startLastFailedTimer();
    }

    private void startLastFailedTimer() {
        if (lastFailedTimer == null) {
            lastFailedTimer = new UpdateTimer(this);
            lastFailedTimer.startTimer(delayBeforeResend);
        }
    }

    private void stopLastFailedTimer() {
        if (lastFailedTimer != null) {
            lastFailedTimer.stopTimer();
            lastFailedTimer = null;
        }
    }

    @Override
    public void onTimerUpdate() {
        log.info("timer triggered, resend failed journey updates, amount:" + allFailedUpdates.size());
        stopLastFailedTimer();
        httpLoader.postJourneyUpdate(new ArrayList<>(allFailedUpdates));
        allFailedUpdates.clear();
    }
}
