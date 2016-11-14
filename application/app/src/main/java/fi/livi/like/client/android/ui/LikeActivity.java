package fi.livi.like.client.android.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.slf4j.LoggerFactory;

import java.util.Date;

import fi.livi.like.client.android.R;
import fi.livi.like.client.android.backgroundservice.broadcastreceivers.StateChangeReceiver;
import fi.livi.like.client.android.backgroundservice.broadcastreceivers.UserLoadingCompletedReceiver;
import fi.livi.like.client.android.backgroundservice.broadcastreceivers.UserManagerReceiver;
import fi.livi.like.client.android.backgroundservice.http.HttpLoader;
import fi.livi.like.client.android.backgroundservice.service.BackgroundService;
import fi.livi.like.client.android.backgroundservice.service.BaseActivity;
import fi.livi.like.client.android.ui.util.NotificationHandler;
import fi.livi.like.client.android.backgroundservice.tracking.TrackingDisabledHandler;
import fi.livi.like.client.android.backgroundservice.tracking.TrackingStateMachine;
import fi.livi.like.client.android.backgroundservice.tracking.TrackingStrings;
import fi.livi.like.client.android.backgroundservice.user.UserManager;
import fi.livi.like.client.android.ui.components.NotifierDialog;
import fi.livi.like.client.android.ui.util.PinCodeDialog;
import fi.livi.like.client.android.ui.util.RuntimePermissionChecker;
import fi.livi.like.client.android.ui.util.SettingsChecker;
import fi.livi.like.client.android.ui.util.TimeFormatter;

public class LikeActivity extends BaseActivity
        implements RuntimePermissionChecker.Listener, NotifierDialog.Listener, PinCodeDialog.Listener,
        StateChangeReceiver.Listener, UserManagerReceiver.Listener, UserLoadingCompletedReceiver.Listener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LikeActivity.class);
    private static final String ERROR_DIALOG_TAG = "errorDialog";
    private static final String USER_ID_DIALOG_TAG = "userIdDialog";
    private static final String USER_INSTRUCTIONS_DIALOG_TAG = "userInstructionsDialog";

    private RuntimePermissionChecker runtimePermissionChecker = new RuntimePermissionChecker(this);

    private UserManagerReceiver userManagerReceiver;
    private StateChangeReceiver stateChangeReceiver;
    private UserLoadingCompletedReceiver userLoadingCompletedReceiver;
    private NotificationHandler notificationHandler;
    private TextView trackingTextView;
    private LinearLayout disableTrackingViewGroup;
    private Button cancelDisabledTrackingButton;
    private SettingsChecker settingsChecker;
    private TrackingStrings trackingStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.info("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        runtimePermissionChecker.setListener(this);
        trackingTextView = (TextView)findViewById(R.id.livi_tracking_text_view);
        disableTrackingViewGroup = (LinearLayout) findViewById(R.id.livi_disable_tracking_group);
        cancelDisabledTrackingButton = (Button) findViewById(R.id.livi_cancel_disabled_tracking_button);
        notificationHandler = new NotificationHandler(
                getBaseContext(), NotificationManagerCompat.from(this), LikeActivity.class);
        registerBroadcastReceivers();
    }

    @Override
    protected void onResume() {
        if (!BackgroundService.isRunning) {
            startAndBindBackgroundService();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        log.info("onDestroy");
        super.onDestroy();
    }

    private void registerBroadcastReceivers() {
        if (userManagerReceiver == null) {
            userManagerReceiver = new UserManagerReceiver(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    userManagerReceiver, new IntentFilter(UserManager.BROADCAST_ACTION_ID));
        }
        if (stateChangeReceiver == null) {
            stateChangeReceiver = new StateChangeReceiver(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    stateChangeReceiver, new IntentFilter(TrackingStateMachine.BROADCAST_ACTION_ID));
        }
    }

    private void unregisterActivityReceiver(BroadcastReceiver receiver) {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(backgroundService).unregisterReceiver(receiver);
            // receiver is 'nulled' when unregistered
            //noinspection UnusedAssignment
            receiver = null;
        }
    }

    @Override
    public Intent createServiceIntent() {
        return notificationHandler.createServiceIntent();
    }

    @Override
    protected void onBackgroundServiceConnected(ComponentName className, IBinder service) {
        super.onBackgroundServiceConnected(className, service);
        prepareGooglePlayServices();//after prepared -> onGooglePlayServicesPrepared()
        trackingStrings = getBackgroundService().getLikeService().getTrackingStrings();
        notificationHandler.setTrackingStrings(trackingStrings);
        notificationHandler.updateNotificationTextToLastKnownState();
    }

    public void onGooglePlayServicesPrepared() {
        if (runtimePermissionChecker.checkAndHandleRuntimePermissions()) {
            checkSettingsEnabled();
        } // if not granted, requests from user -> permissionsGranted()
    }

    @Override
    public void permissionsGranted() {
        log.info("permissionsGranted");
        checkSettingsEnabled();
    }

    private void checkSettingsEnabled() {
        if (settingsChecker == null) {
            settingsChecker = new SettingsChecker(this);
        }

        if (settingsChecker.isSettingsEnabled()) {
            resumeOnBackgroundService();
        } else {
            log.warn("related settings not enabled, notify user!");
        }

        settingsChecker = null;
    }

    private void resumeOnBackgroundService() {
        getBackgroundService().getLikeService().resume();
    }

    public void showUserIdDialog(View view) {
        NotifierDialog.newInstance(
                getString(R.string.user_id_dialog_title),
                getBackgroundService().getDeviceInfo().getImei(),
                true, this).show(getSupportFragmentManager(), USER_ID_DIALOG_TAG);
    }

    public void disableLikeTracking(View view) {
        log.info("ui - disabling tracking");
        getBackgroundService().getLikeService().setTrackingDisabled(
                TrackingDisabledHandler.DisabledTime.valueOf(view.getTag().toString()));
    }

    public void cancelDisabledLikeTracking(View view) {
        log.info("ui - cancel disabled tracking");
        getBackgroundService().getLikeService().setTrackingEnabled();
    }

    @Override
    public void onClickNotifierDialog(DialogInterface dialog, int which, String dialogTag) {
        // no dialogs with separate actions
    }

    @Override
    public void onTrackingStateChanged(TrackingStateMachine.State newState, Date disabledTimeStarted, Date disabledTimeEnds, int disabledTimeInMs) {
        log.info("ui notified with state: " + newState);
        if (newState == TrackingStateMachine.State.DISABLED) {
            disableTrackingViewGroup.setVisibility(View.INVISIBLE);
            cancelDisabledTrackingButton.setVisibility(View.VISIBLE);
            trackingTextView.setText(
                    String.format(getString(R.string.tracking_disabled_in_detail),
                            TimeFormatter.formatToLocalDateTime(TimeFormatter.DDMMYYYY_DATE_FORMAT, disabledTimeStarted),
                            TimeFormatter.formatToLocalDateTime(TimeFormatter.TIME_FORMAT_NO_SECONDS, disabledTimeEnds)));
        } else {
            trackingTextView.setText(TrackingStateMachine.getShortTrackingString(newState, trackingStrings));
            disableTrackingViewGroup.setVisibility(View.VISIBLE);
            cancelDisabledTrackingButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPinCodeRequest() {
        log.info("ui notified with missing pincode");
        dismissPinCodeDialog();
        PinCodeDialog.newInstance().show(getSupportFragmentManager(), PinCodeDialog.PIN_CODE_DIALOG_TAG);
    }

    @Override
    public void onPinCodeEntered(String pinCode) {
        log.info("onPinCodeEntered - " + pinCode);
        backgroundService.getLikeService().getUserManager().storePinCode(pinCode);
        if (userLoadingCompletedReceiver == null) {
            userLoadingCompletedReceiver = new UserLoadingCompletedReceiver(this);
            LocalBroadcastManager.getInstance(backgroundService).registerReceiver(
                    userLoadingCompletedReceiver, new IntentFilter(HttpLoader.BROADCAST_ACTION_ID));
        }
    }

    @Override
    public void onUserLoadingCompleted() {
        log.info("onUserLoadingCompleted on ui");
        unregisterActivityReceiver(userLoadingCompletedReceiver);
        dismissPinCodeDialog();
        showInstructions();
    }

    @Override
    public void onUserLoadingFailed() {
        NotifierDialog.newInstance(
                getString(R.string.error_title),
                getString(R.string.user_loading_failed),
                true, this).show(getSupportFragmentManager(), ERROR_DIALOG_TAG);
        PinCodeDialog pinCodeDialog = (PinCodeDialog) getSupportFragmentManager().findFragmentByTag(PinCodeDialog.PIN_CODE_DIALOG_TAG);
        if (pinCodeDialog != null) {
            pinCodeDialog.setSaveButtonVisible(true);
        }
    }

    private void dismissPinCodeDialog() {
        DialogFragment pinCodeDialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(PinCodeDialog.PIN_CODE_DIALOG_TAG);
        if (pinCodeDialog != null) {
            pinCodeDialog.dismiss();
        }
    }

    private void showInstructions() {
        NotifierDialog.newInstance(
                getString(R.string.user_instructions_dialog_title),
                getString(R.string.user_instructions_dialog_content),
                true, this).show(getSupportFragmentManager(), USER_INSTRUCTIONS_DIALOG_TAG);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        runtimePermissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
