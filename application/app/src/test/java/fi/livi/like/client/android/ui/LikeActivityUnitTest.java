package fi.livi.like.client.android.ui;

import android.content.ComponentName;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import fi.livi.like.client.android.BuildConfig;
import fi.livi.like.client.android.backgroundservice.LikeService;
import fi.livi.like.client.android.backgroundservice.googleplayservices.GooglePlayServiceLibraryChecker;
import fi.livi.like.client.android.backgroundservice.service.BackgroundService;
import fi.livi.like.client.android.backgroundservice.service.BackgroundServiceHandler;
import fi.livi.like.client.android.backgroundservice.service.ServiceBinder;
import fi.livi.like.client.android.ui.util.RuntimePermissionChecker;
import fi.livi.like.client.android.ui.util.SettingsChecker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class LikeActivityUnitTest {

    private ActivityController controller;
    private LikeService likeService;
    private BackgroundService backgroundService;
    private BackgroundServiceHandler backgroundServiceHandler;
    private RuntimePermissionChecker runtimePermissionChecker;
    private SettingsChecker settingsChecker;
    private GooglePlayServiceLibraryChecker googlePlayServiceLibraryChecker;

    @InjectMocks
    LikeActivity mocksInjectedActivity;

    @Before
    public void setup() {
        controller = Robolectric.buildActivity(LikeActivity.class);

        likeService = mock(LikeService.class);
        backgroundService = mock(BackgroundService.class);
        backgroundServiceHandler = mock(BackgroundServiceHandler.class);
        runtimePermissionChecker = mock(RuntimePermissionChecker.class);
        settingsChecker = mock(SettingsChecker.class);
        googlePlayServiceLibraryChecker = mock(GooglePlayServiceLibraryChecker.class);

        when(backgroundService.getLikeService()).thenReturn(likeService);
    }

    @After
    public void cleanup() {
        BackgroundService.isRunning = false;
    }

    @Test
    public void should_start_and_bind_to_service_when_resumed() {
        prepare_mock_resumed_state();

        verify(backgroundServiceHandler).startAndBindBackgroundService();
    }

    @Test
    public void should_bind_to_running_service_when_resumed() {
        BackgroundService.isRunning = true;
        prepare_mock_resumed_state();

        verify(backgroundServiceHandler, never()).startAndBindBackgroundService();
        verify(backgroundServiceHandler).bindService();
    }

    @Test
    public void should_prepare_googleplayservices_when_background_connection_ready() {
        prepare_mock_resumed_state();

        mocksInjectedActivity.onBackgroundServiceConnected(
                new ComponentName("dummy", backgroundService.getClass().getName()),
                new ServiceBinder(backgroundService));

        verify(googlePlayServiceLibraryChecker).prepareGooglePlayServices();
    }

    @Test
    public void should_check_permissions_after_googleplayservices_prepared() {
        prepare_mock_resumed_state();
        when(runtimePermissionChecker.checkAndHandleRuntimePermissions()).thenReturn(false);

        mocksInjectedActivity.onGooglePlayServicesPrepared();

        verify(runtimePermissionChecker).checkAndHandleRuntimePermissions();
    }

    @Test
    public void should_check_settings_when_runtime_permissions_ok() {
        prepare_mock_resumed_state();
        when(runtimePermissionChecker.checkAndHandleRuntimePermissions()).thenReturn(true);

        mocksInjectedActivity.onGooglePlayServicesPrepared();

        verify(settingsChecker).isSettingsEnabled();
    }

    @Test
    public void should_resume_service_when_settings_ok() {
        prepare_mock_resumed_state();
        when(settingsChecker.isSettingsEnabled()).thenReturn(true);

        mocksInjectedActivity.permissionsGranted();

        verify(likeService).resume();
    }

    // TEST UTILS

    public void prepare_mock_resumed_state() {
        controller.create().start();
        mocksInjectedActivity = (LikeActivity) controller.get();
        MockitoAnnotations.initMocks(this);
        controller.resume();
    }
}
