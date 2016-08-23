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
import android.widget.TextView;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.R;
import fi.livi.like.client.android.background.LikeService;
import fi.livi.like.client.android.background.http.HttpLoader;
import fi.livi.like.client.android.background.service.BackgroundService;
import fi.livi.like.client.android.background.service.NotificationHandler;
import fi.livi.like.client.android.background.tracking.TrackingStateMachine;
import fi.livi.like.client.android.background.user.UserManager;
import fi.livi.like.client.android.broadcastreceivers.StateChangeBroadcastReceiver;
import fi.livi.like.client.android.broadcastreceivers.UserLoadingCompletedReceiver;
import fi.livi.like.client.android.broadcastreceivers.UserManagerBroadcastReceiver;
import fi.livi.like.client.android.ui.components.NotifierDialog;
import fi.livi.like.client.android.ui.util.RuntimePermissionChecker;

public class LikeActivity extends BaseActivity
        implements RuntimePermissionChecker.Listener, NotifierDialog.Listener, PinCodeDialog.Listener,
        StateChangeBroadcastReceiver.Listener, UserManagerBroadcastReceiver.Listener, UserLoadingCompletedReceiver.Listener {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LikeActivity.class);
    private static final String ERROR_DIALOG_TAG = "errorDialog";
    private static final String USER_ID_DIALOG_TAG = "userIdDialog";
    private static final String USER_INSTRUCTIONS_DIALOG_TAG = "userInstructionsDialog";

    private RuntimePermissionChecker runtimePermissionChecker = new RuntimePermissionChecker(this);

    private UserManagerBroadcastReceiver userManagerBroadcastReceiver;
    private StateChangeBroadcastReceiver stateChangeBroadcastReceiver;
    private UserLoadingCompletedReceiver userLoadingCompletedReceiver;
    private NotificationHandler notificationHandler;
    private TextView trackingTextView;
    private Button enableDisableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.info("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        runtimePermissionChecker.setListener(this);
        trackingTextView = (TextView)findViewById(R.id.tracking_text_view);
        enableDisableButton = (Button)findViewById(R.id.enable_disable_tracking_button);
        notificationHandler = new NotificationHandler(getBaseContext(), NotificationManagerCompat.from(this));
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
        if (userManagerBroadcastReceiver == null) {
            userManagerBroadcastReceiver = new UserManagerBroadcastReceiver(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    userManagerBroadcastReceiver, new IntentFilter(UserManager.BROADCAST_ACTION_ID));
        }
        if (stateChangeBroadcastReceiver == null) {
            stateChangeBroadcastReceiver = new StateChangeBroadcastReceiver(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    stateChangeBroadcastReceiver, new IntentFilter(TrackingStateMachine.BROADCAST_ACTION_ID));
        }
    }

    private void unregisterActivityReceiver(BroadcastReceiver receiver) {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(backgroundService).unregisterReceiver(receiver);
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
    }

    public void onGooglePlayServicesPrepared() {
        if (runtimePermissionChecker.checkAndHandleRuntimePermissions()) {
            resumeOnBackgroundService();
        } // if not granted, requests from user -> permissionsGranted()
    }

    @Override
    public void permissionsGranted() {
        log.info("permissionsGranted");
        resumeOnBackgroundService();
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
        LikeService likeService = getBackgroundService().getLikeService();
        likeService.setDisabled(!likeService.isDisabled());
        enableDisableButton.setText(getText(
                likeService.isDisabled() ? R.string.enable_like_button : R.string.disable_like_button));
    }

    @Override
    public void onClickNotifierDialog(DialogInterface dialog, int which, String dialogTag) {
        // no dialogs with separate actions
    }

    @Override
    public void onTrackingStateChanged(TrackingStateMachine.State newState) {
        log.info("ui notified with state: " + newState);
        trackingTextView.setText(getString(TrackingStateMachine.getTrackingStringResourceId(newState)));
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
