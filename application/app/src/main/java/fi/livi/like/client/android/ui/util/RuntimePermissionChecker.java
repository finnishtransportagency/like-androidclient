package fi.livi.like.client.android.ui.util;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fi.livi.like.client.android.R;

import static android.R.string.ok;

public class RuntimePermissionChecker {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RuntimePermissionChecker.class);

    private static final int REQUEST_MULTIPLE_PERMISSIONS = 1;
    private static final String[] neededDangerousPermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private final AppCompatActivity activity;
    private AlertDialog reasoningDialog;
    private Listener listener;

    public interface Listener {
        void permissionsGranted();
    }

    public RuntimePermissionChecker(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean checkAndHandleRuntimePermissions() {
        if (reasoningDialog != null) {
            reasoningDialog.dismiss();
            reasoningDialog = null;
        }

        List<String> deniedPermissions = getDeniedRuntimePermissions(activity);
        if (!deniedPermissions.isEmpty()) {
            final String deniedPermission = deniedPermissions.get(0);

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)) {
                showNotification(deniedPermission);// will request permission after showing reasons
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{deniedPermission}, REQUEST_MULTIPLE_PERMISSIONS);
            }
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MULTIPLE_PERMISSIONS: {
                log.info("onRequestPermissionsResult - permissions:" + Arrays.toString(permissions) + " grantResults:" + Arrays.toString(grantResults));
                if (areRuntimePermissionsGranted(activity) && listener != null) {
                    listener.permissionsGranted();
                }
            }
        }
    }

    private String mapPermissionToDescription(String deniedPermission) {
        switch (deniedPermission) {
            case Manifest.permission.READ_PHONE_STATE: {
                return activity.getResources().getString(R.string.permissions_phone);
            }
            case Manifest.permission.ACCESS_FINE_LOCATION: {
                return activity.getResources().getString(R.string.permissions_location);
            }
            case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                return activity.getResources().getString(R.string.permissions_file);
            }
            default:
                return null;
        }
    }

    private void showNotification(final String deniedPermission) {
        log.info("permission missing, notify user - permission:" + deniedPermission);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(mapPermissionToDescription(deniedPermission))
                .setCancelable(false)
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(activity, new String[]{deniedPermission}, REQUEST_MULTIPLE_PERMISSIONS);
                    }
                });
        reasoningDialog = builder.create();
        reasoningDialog.show();
    }

    // STATIC CHECKERS

    private static boolean areRuntimePermissionsGranted(Context context) {
        return getDeniedRuntimePermissions(context).isEmpty();
    }

    private static List<String> getDeniedRuntimePermissions(Context context) {

        List<String> deniedPermissions = new ArrayList<>();
        for (final String permission: neededDangerousPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                log.warn("### NO PERMISSION FOR " + permission);
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }
}
