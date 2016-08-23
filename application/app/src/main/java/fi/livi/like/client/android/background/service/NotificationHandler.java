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

import fi.livi.like.client.android.R;
import fi.livi.like.client.android.background.tracking.TrackingStateMachine;
import fi.livi.like.client.android.broadcastreceivers.StateChangeBroadcastReceiver;
import fi.livi.like.client.android.ui.LikeActivity;

public class NotificationHandler implements StateChangeBroadcastReceiver.Listener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(NotificationHandler.class);
    private static int APP_NOTIFICATION_ID = 100;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    private NotificationCompat.Builder notificationBuilder;
    private StateChangeBroadcastReceiver stateChangeBroadcastReceiver;

    public NotificationHandler(Context context, NotificationManagerCompat notificationManager) {
        log.info("creating notification handler");
        this.context = context;
        this.notificationManager = notificationManager;
        stateChangeBroadcastReceiver = new StateChangeBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                stateChangeBroadcastReceiver, new IntentFilter(TrackingStateMachine.BROADCAST_ACTION_ID));
    }

    public Intent createServiceIntent() {
        Intent intent = new Intent(context, BackgroundService.class);
        BackgroundServiceSettings serviceSettings = new BackgroundServiceSettings();
        serviceSettings.setRestartPolicy(Service.START_STICKY);
        serviceSettings.enableServiceOnForeground(APP_NOTIFICATION_ID, createServiceNotification());
        serviceSettings.setWakeLockMode(PowerManager.PARTIAL_WAKE_LOCK);
        intent.putExtra(BackgroundServiceSettings.SERVICESETTINGS_INTENT_ID, serviceSettings);
        return intent;
    }

    private Notification createServiceNotification() {
        if (notificationBuilder == null) {
            notificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
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

    public void updateNotificationText(String newText) {
        notificationBuilder.setContentText(newText);
        notificationManager.notify(APP_NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onTrackingStateChanged(TrackingStateMachine.State newState) {
        log.info("notification notified with state: " + newState);
        updateNotificationText(context.getString(
                TrackingStateMachine.getTrackingStringResourceId(newState)));

    }
}
