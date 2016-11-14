package fi.livi.like.client.android.backgroundservice.datacollectors.activityrecognition;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fi.livi.like.client.android.backgroundservice.data.DataStorage;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeActivity;

public class LikeActivityAverager {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LikeActivityAverager.class);

    private static final int AVERAGING_LIKE_ACTIVITY_ARRAY_SIZE = 5;
    private static final int HIGH_CONFIDENCE_LEVEL = 75;
    private final DataStorage dataStorage;
    private List<LikeActivity> lastLikeActivities = new ArrayList<>();
    private LikeActivity averageLikeActivity;

    public LikeActivityAverager(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        final LikeActivity lastLikeActivity = dataStorage.getLastAverageLikeActivity();
        if (lastLikeActivity != null) {
            averageLikeActivity = lastLikeActivity;
            lastLikeActivities.add(lastLikeActivity);
        }
    }

    public LikeActivity getAverageLikeActivity() {
        return averageLikeActivity;
    }

    public void reset() {
        averageLikeActivity = null;
        lastLikeActivities.clear();
    }

    public synchronized void onLikeActivityUpdate(LikeActivity likeActivity) {
        if (likeActivity == null) {
            return;
        }

        log.info("onLikeActivityUpdate - current LikeActivity: " + likeActivity);
        handleHighConfidenceActivities(likeActivity);

        lastLikeActivities.add(likeActivity);
        while (lastLikeActivities.size() > AVERAGING_LIKE_ACTIVITY_ARRAY_SIZE) {
            lastLikeActivities.remove(0);
        }

        // add all activity types to map using it as a counter
        // Map<ActivityType, count>
        Map<LikeActivity.Type, Integer> typeMap = new HashMap<>();
        for (LikeActivity lastLikeActivity : lastLikeActivities) {
            LikeActivity.Type activityType = lastLikeActivity.getType();
            Integer counterValue = typeMap.get(activityType);
            counterValue = counterValue == null ? 0 : counterValue;
            counterValue++;
            typeMap.put(activityType, counterValue);
        }

        SortedSet<Map.Entry<LikeActivity.Type, Integer>> sortedLastActivities = entriesSortedByValues(typeMap);
        log.info("last likeactivities, sorted based on type counts:");
        for (Map.Entry<LikeActivity.Type, Integer> entry  : sortedLastActivities) {
            log.info("- " + entry.getKey() + " : " + entry.getValue());
        }

        averageLikeActivity = findAverageLikeActivity(sortedLastActivities);
        dataStorage.setLastAverageLikeActivity(averageLikeActivity);
        log.info("onLikeActivityUpdate - averageLikeActivity: " + averageLikeActivity);
    }

    private void handleHighConfidenceActivities(LikeActivity likeActivity) {
        if (likeActivity.getConfidence() > HIGH_CONFIDENCE_LEVEL &&
                averageLikeActivity != null && averageLikeActivity.getType() != likeActivity.getType()) {

            final int neededAmount = AVERAGING_LIKE_ACTIVITY_ARRAY_SIZE / 2;
            log.info("LikeActivity confidence high enough, add " + neededAmount + " same objects to give more weight on averaging");
            for(int i=0; i<neededAmount; i++) {
                lastLikeActivities.add(likeActivity);
            }
        }
    }

    private <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    private LikeActivity findAverageLikeActivity(SortedSet<Map.Entry<LikeActivity.Type, Integer>> sortedLastLikeActivities) {

        switch (sortedLastLikeActivities.size()) {
            case 0:
                return null;
            case 1:
                return findLastMatchingActivityFromList(sortedLastLikeActivities.first().getKey());
            default:
                return compareLastTwoResultsAndGetLastOne(sortedLastLikeActivities);
        }
    }

    private LikeActivity compareLastTwoResultsAndGetLastOne(SortedSet<Map.Entry<LikeActivity.Type, Integer>> sortedLastLikeActivities) {
        Iterator<Map.Entry<LikeActivity.Type, Integer>> it = sortedLastLikeActivities.iterator();
        final Map.Entry<LikeActivity.Type, Integer> first = it.next();
        final Map.Entry<LikeActivity.Type, Integer> second = it.next();

        if (first.getValue() > second.getValue()) {
            return findLastMatchingActivityFromList(first.getKey());
        } else {
            final int firstIndex = getIndexOfTypeInList(first.getKey());
            final int secondIndex = getIndexOfTypeInList(second.getKey());
            return lastLikeActivities.get(firstIndex > secondIndex ? firstIndex : secondIndex);
        }
    }

    private LikeActivity findLastMatchingActivityFromList(LikeActivity.Type type) {
        return lastLikeActivities.get(getIndexOfTypeInList(type));
    }

    private int getIndexOfTypeInList(LikeActivity.Type type) {
        int i;
        for (i = lastLikeActivities.size() - 1; i >= 0; i--) {
            if (lastLikeActivities.get(i).getType() == type) {
                break;
            }
        }
        return i;
    }
}
