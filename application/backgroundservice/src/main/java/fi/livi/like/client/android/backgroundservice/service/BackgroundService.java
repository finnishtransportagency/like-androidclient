package fi.livi.like.client.android.backgroundservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.broadcastreceivers.DeviceShutdownReceiver;
import fi.livi.like.client.android.backgroundservice.googleplayservices.GooglePlayServicesApiClient;
import fi.livi.like.client.android.backgroundservice.util.Broadcaster;
import fi.livi.like.client.android.backgroundservice.util.UncaughtExceptionLogger;

/**
 * BackgroundService provides access to Google Play Services API and certain details of device. These
 * can be accessed through getGooglePlayServicesApiClient() and getDeviceInfo(). See documentation of
 * classes itself to understand their functionality better.
 *
 * BackgroundService provides also means to keep service alive while application is paused and get
 * notification on notification bar for relaunching/notifying purposes. See BackgroundServiceSettings
 * documentation to see the parameters what can be used for this.
 *
 * See also documentation on methods below.
 */
public class BackgroundService extends Service implements DeviceShutdownReceiver.Listener {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(BackgroundService.class);

    public static boolean isRunning = false;
    public static boolean isSystemShuttingDown = false;

    protected IBinder binder;
    protected GooglePlayServicesApiClient googlePlayServicesApiClient;
    protected DeviceInfo deviceInfo;
    protected int restartPolicy = START_STICKY;// will be restarted later by system (Intent can be null)
    protected WakeLockHandler wakeLockHandler = new WakeLockHandler(this);
    protected IntentSettingsHandler intentSettingsHandler;
    protected UncaughtExceptionLogger exceptionLogger;
    protected DeviceShutdownReceiver deviceShutdownReceiver;
    private LikeService likeService;

    @Override
    public void onCreate() {
        log.info("onCreate");
        super.onCreate();
        exceptionLogger = new UncaughtExceptionLogger(Thread.currentThread());
        createLocalBinder();
        deviceInfo = new DeviceInfo(getBaseContext());
        googlePlayServicesApiClient = new GooglePlayServicesApiClient(getBaseContext());
        createOptional();

        likeService = new LikeService(this);
    }

    /**
     * Overridable method for creation of local Binder
     */
    protected void createLocalBinder() {
        binder = new ServiceBinder(this);
    }

    /**
     * Overridable method for creation of optional members in case needed
     */
    protected void createOptional() {
        intentSettingsHandler = new IntentSettingsHandler(this);
        Broadcaster.prepareSingleton(getBaseContext());
        deviceShutdownReceiver = new DeviceShutdownReceiver(this);
        deviceShutdownReceiver.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("onStartCommand");
        log.info("Received start id " + startId + ": " + intent);

        onStart(intent, flags, startId);
        BackgroundService.setIsRunning(true);
        return restartPolicy;
    }

    /**
     * Overridable method to handle onStartCommand and incoming intents. See documentation of
     * onStartCommand to see details of the parameters.
     * Note; restart policy is handled through restartPolicy-member
     * @param intent Intent
     * @param flags int
     * @param startId int
     */
    protected void onStart(Intent intent, int flags, int startId) {
        if (intent != null) {
            intentSettingsHandler.handleSettings(intent, flags, startId);
        }
    }

    @Override
    public void onDestroy() {
        log.info("onDestroy");
        super.onDestroy();
        googlePlayServicesApiClient.close();
        likeService.close();

        wakeLockHandler.releaseWakeLock();

        stopForeground(true);
        BackgroundService.setIsRunning(false);
    }

    static synchronized void setIsRunning(boolean isRunning) {
        BackgroundService.isRunning = isRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {
        log.info("onBind");
        return binder;
    }

    /**
     * Get access to device details with DeviceInfo.
     * See DeviceInfo for more details.
     * @return DeviceInfo
     */
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * Get access to services provided by Google Play Services.
     * See GooglePlayServicesApiClient for more details.
     * @return GooglePlayServicesApiClient
     */
    public GooglePlayServicesApiClient getGooglePlayServicesApiClient() {
        return googlePlayServicesApiClient;
    }

    public LikeService getLikeService() {
        return likeService;
    }

    @Override
    public void onDeviceShutdown() {
        deviceShutdownReceiver.unregister(this);
        isSystemShuttingDown = true;
        stopSelf();
    }
}
