package fi.livi.like.client.android.backgroundservice.service;

import android.content.Intent;

import org.slf4j.LoggerFactory;

/**
 * IntentSettingsHandler has the basic functionality handling the settings
 * via intent when service is launched.
 */
public class IntentSettingsHandler {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(IntentSettingsHandler.class);

    private BackgroundService backgroundService;

    public IntentSettingsHandler(BackgroundService backgroundService) {
        this.backgroundService = backgroundService;
    }

    public void handleSettings(Intent intent, int flags, int startId) {
        // flags and startId are onStartCommands parameters
        if (intent.hasExtra(BackgroundServiceSettings.SERVICESETTINGS_INTENT_ID)) {
            BackgroundServiceSettings settings = intent.getParcelableExtra(BackgroundServiceSettings.SERVICESETTINGS_INTENT_ID);
            log.info("handleSettings - " + settings);
            handleRestartPolicySetting(settings.getRestartPolicy());
            handleForegroundSettings(settings);
            handleWakeLockSetting(settings.getWakeLockMode());
        }
    }

    private void handleRestartPolicySetting(int restartPolicy) {
        if (restartPolicy != -1) {
            backgroundService.restartPolicy = restartPolicy;
        }
    }

    private void handleForegroundSettings(BackgroundServiceSettings settings) {
        if (settings.isStartForeground() && settings.getNotificationId() != -1 && settings.getNotification() != null) {
            backgroundService.startForeground(settings.getNotificationId(), settings.getNotification());
        }
    }

    private void handleWakeLockSetting(int wakeLockMode) {
        if (wakeLockMode != -1) {
            backgroundService.wakeLockHandler.acquireWakelock(wakeLockMode);
        }
    }
}
