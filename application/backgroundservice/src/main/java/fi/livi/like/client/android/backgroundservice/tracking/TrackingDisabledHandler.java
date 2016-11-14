package fi.livi.like.client.android.backgroundservice.tracking;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import org.slf4j.LoggerFactory;

import java.util.Date;

import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.broadcastreceivers.AlarmTriggerReceiver;

public class TrackingDisabledHandler implements AlarmTriggerReceiver.Listener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackingDisabledHandler.class);

    public final static String DISABLED_WAIT_TIME = TrackingStateMachine.BROADCAST_ACTION_ID + ".disabled.time";
    public final static String DISABLED_WAIT_STARTED = TrackingStateMachine.BROADCAST_ACTION_ID + ".disabled.time.started";
    public final static String DISABLED_WAIT_ENDS = TrackingStateMachine.BROADCAST_ACTION_ID + ".disabled.time.ends";

    private final LikeService likeService;
    private AlarmManager alarmManager;
    private AlarmTriggerReceiver alarmTriggerReceiver;

    private PendingIntent alarmIntent;
    private Intent currentWaitingIntent;

    public enum DisabledTime {
        ONE_HOUR,
        TWELVE_HOURS,
        TWENTY_FOUR_HOURS
    }

    TrackingDisabledHandler(LikeService likeService) {
        this.likeService = likeService;
    }

    @Override
    public void onAlarmTriggered() {
        log.info("onAlarmTriggered - disabled time over");
        reset();
        likeService.setTrackingEnabled();
    }

    void setDisabledTime(DisabledTime disabledTime) {

        reset();
        log.info("user tracking disabled waiter is set for " + disabledTime);
        currentWaitingIntent = createWaitingIntent(disabledTime);
        alarmIntent = createWakeUpIntent();

        prepareAlarmTriggerReceiver();
        setAlarmManager(disabledTime);
    }

    private void setAlarmManager(DisabledTime disabledTime) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager)likeService.getBackgroundService().getSystemService(Context.ALARM_SERVICE);
        }
        final long wakeupTime = SystemClock.elapsedRealtime() + getDisabledTimeInMilliseconds(disabledTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeupTime, alarmIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeupTime, alarmIntent);
        }
    }

    private void prepareAlarmTriggerReceiver() {
        if (alarmTriggerReceiver == null) {
            alarmTriggerReceiver = new AlarmTriggerReceiver(likeService.getBackgroundService(), this);
        }
        alarmTriggerReceiver.register(likeService.getBackgroundService());
    }

    void reset() {
        if (alarmManager == null || alarmIntent == null) {
            return;
        }
        log.info("user tracking disabled waiter is reset");

        alarmManager.cancel(alarmIntent);
        alarmTriggerReceiver.unregister(likeService.getBackgroundService());
        alarmIntent = null;
        currentWaitingIntent = null;
    }

    void broadcastStateLocal() {
        if (currentWaitingIntent != null) {
            LocalBroadcastManager.getInstance(likeService.getBackgroundService()).sendBroadcast(currentWaitingIntent);
        }
    }

    private Intent createWaitingIntent(DisabledTime disabledTime) {
        final Date starts = new Date();
        final int disabledTimeInMs = getDisabledTimeInMilliseconds(disabledTime);
        final Date ends = new Date(starts.getTime() + disabledTimeInMs);
        log.info("user tracking disabled, starts " + starts + " and ends " + ends);

        Intent intent = new Intent(TrackingStateMachine.BROADCAST_ACTION_ID);
        intent.putExtra(TrackingStateMachine.BROADCAST_KEY_ID, TrackingStateMachine.State.DISABLED.toString());
        intent.putExtra(DISABLED_WAIT_TIME, disabledTimeInMs);
        intent.putExtra(DISABLED_WAIT_STARTED, starts);
        intent.putExtra(DISABLED_WAIT_ENDS, ends);
        return intent;
    }

    private PendingIntent createWakeUpIntent() {
        Intent wakeUpIntent = new Intent(AlarmTriggerReceiver.BROADCAST_ACTION_ID);
        return PendingIntent.getBroadcast(likeService.getBackgroundService(), 0, wakeUpIntent, 0);
    }

    private int getDisabledTimeInMilliseconds(DisabledTime disabledTime) {

        int delay;
        switch (disabledTime) {
            case ONE_HOUR: {
                delay = 60 * 60 * 1000;
                break;
            }
            case TWELVE_HOURS: {
                delay = 12 * 60 * 60 * 1000;
                break;
            }
            case TWENTY_FOUR_HOURS: {
                delay = 24 * 60 * 60 * 1000;
                break;
            }
            default:
                delay = 0;
        }

        return delay;
    }
}
