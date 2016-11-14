package fi.livi.like.client.android.backgroundservice.tracking;

import android.location.Location;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.datacollectors.location.LocationHandler;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeActivity;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeLocation;
import fi.livi.like.client.android.backgroundservice.googleplayservices.GooglePlayServicesApiClient;

public class JourneyManager {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(JourneyManager.class);

    private final LikeService likeService;
    private final LocationHandler locationHandler;
    private final GooglePlayServicesApiClient googlePlayServicesApiClient;

    private Journey currentJourney;
    private Journey previousJourney;

    public JourneyManager(LikeService likeService, LocationHandler locationHandler, GooglePlayServicesApiClient googlePlayServicesApiClient) {
        this.likeService = likeService;
        this.locationHandler = locationHandler;
        this.googlePlayServicesApiClient = googlePlayServicesApiClient;
    }

    public boolean updateJourney() {
        if (currentJourney == null) {
            currentJourney = new Journey();
            currentJourney.setJourneyId(new Date().getTime());
        }

        JourneyUpdateBuilder journeyUpdateBuilder = new JourneyUpdateBuilder();
        journeyUpdateBuilder.setUserId(likeService.getDataStorage().getUser().getId());
        journeyUpdateBuilder.setJourneyId(currentJourney.getJourneyId());
        journeyUpdateBuilder.setTimestamp(new Date());

        updateLikeActivity();
        journeyUpdateBuilder.setSubJourneyId(currentJourney.getLastSubJourneyJourneyId());
        journeyUpdateBuilder.setLikeActivity(currentJourney.getLastLikeActivity());
        if (journeyUpdateBuilder.getLikeActivity() == null) {
            log.info("- no activity available, journey not updated!");
            return false;
        }

        updateLikeLocation();
        journeyUpdateBuilder.setLikeLocation(currentJourney.getLastLikeLocation());
        if (journeyUpdateBuilder.getLikeLocation() == null) {
            log.info("- no location, journey not updated!");
            return false;
        }

        if (!journeyUpdateBuilder.isJourneyUpdateValid()) {
            log.info("- not valid journey update, journey not updated!");
            return false;
        }

        sendJourneyUpdate(journeyUpdateBuilder);

        return true;
    }

    public void endJourney() {
        if (currentJourney != null && currentJourney.getLastJourneyUpdate() != null) {
            previousJourney = currentJourney;
        }
        currentJourney = null;
    }

    public Journey getCurrentJourney() {
        return currentJourney;
    }

    public Journey getPreviousJourney() {
        return previousJourney;
    }

    public boolean isJourneyStarted() {
        return !(currentJourney == null || currentJourney.getLastJourneyUpdate() == null);
    }

    private void sendJourneyUpdate(JourneyUpdateBuilder journeyUpdateBuilder) {
        JourneyUpdate journeyUpdate = journeyUpdateBuilder.build();
        List<JourneyUpdate> journeyUpdates = new ArrayList<>();
        currentJourney.setLastJourneyUpdate(journeyUpdate);
        journeyUpdates.add(journeyUpdate);

        likeService.getHttpLoader().postJourneyUpdate(journeyUpdates);
    }

    private void updateLikeLocation() {

        Location location = locationHandler.getAverageLocation();
        if (location == null) {
            return;
        }

        LikeLocation likeLocation = new LikeLocation(
                location.getLatitude(), location.getLongitude(), (int)location.getBearing(), (int)location.getSpeed());
        currentJourney.setLastLikeLocation(likeLocation);
    }

    private void updateLikeActivity() {

        LikeActivity likeActivity = googlePlayServicesApiClient.getActivityRecognitionUpdateHandler().getAverageLikeActivity();

        if (likeActivity != null) {
            if (currentJourney.getLastLikeActivity() == null ||
                    currentJourney.getLastLikeActivity().getType() != likeActivity.getType()) {
                currentJourney.setLastSubJourneyJourneyId(new Date().getTime());
            }
            currentJourney.setLastLikeActivity(likeActivity);
        }
    }
}
