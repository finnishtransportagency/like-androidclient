package fi.livi.like.client.android.background.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import org.slf4j.LoggerFactory;

import java.util.Date;

import fi.livi.like.client.android.R;
import fi.livi.like.client.android.background.tracking.TrackingStateMachine;
import fi.livi.like.client.android.broadcastreceivers.StateChangeReceiver;
import fi.livi.like.client.android.ui.LikeActivity;

public class NotificationHandler implements StateChangeReceiver.Listener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(NotificationHandler.class);
    private static int APP_NOTIFICATION_ID = 100;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    private NotificationCompat.Builder notificationBuilder;
    private StateChangeReceiver stateChangeReceiver;

    public NotificationHandler(Context context, NotificationManagerCompat notificationManager) {
        log.info("creating notification handler");
        this.context = context;
        this.notificationManager = notificationManager;
        stateChangeReceiver = new StateChangeReceiver(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                stateChangeReceiver, new IntentFilter(TrackingStateMachine.BROADCAST_ACTION_ID));
    }

    public Intent createServiceIntent() {
        Intent intent = new Intent(context, BackgroundService.class);
        BackgroundServiceSettings serviceSettings = new BackgroundServiceSettings();
        serviceSettings.setRestartPolicy(Service.START_REDELIVER_INTENT);
        serviceSettings.enableServiceOnForeground(APP_NOTIFICATION_ID, createServiceNotification());
        serviceSettings.setWakeLockMode(PowerManager.PARTIAL_WAKE_LOCK);
        intent.putExtra(BackgroundServiceSettings.SERVICESETTINGS_INTENT_ID, serviceSettings);
        return intent;
    }

    private Notification createServiceNotification() {
        if (notificationBuilder == null) {
            notificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(getNotificationIconResId())
                            .setContentTitle(context.getString(R.string.app_name))
                            .setContentText(context.getString(R.string.tracking_off));
        }

        Intent resultIntent = new Intent(context, LikeActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LikeActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(APP_NOTIFICATION_ID, notification);
        return notification;
    }

    private int getNotificationIconResId() {
        final boolean useSilhouette = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useSilhouette ? R.drawable.ic_launcher_white_silhouette : R.mipmap.ic_launcher;
    }

    @Override
    public void onTrackingStateChanged(TrackingStateMachine.State newState, Date disabledTimeStarted, Date disabledTimeEnds, int disabledTimeInMs) {
        log.info("notification notified with state: " + newState);
        updateNotificationText(newState);
        notificationManager.notify(APP_NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationText(TrackingStateMachine.State newState) {
        notificationBuilder.setContentText(context.getString(TrackingStateMachine.getShortTrackingStringResourceId(newState)));
    }
}
