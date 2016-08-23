package fi.livi.like.client.android.ui.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionChecker {

    private static final int REQUEST_PERMISSIONS = 0;
    private static final String[] neededDangerousPermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };


    private final AppCompatActivity activity;
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
        if (!areRuntimePermissionsGranted(activity)) {
            ActivityCompat.requestPermissions(activity, neededDangerousPermissions, REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkAndHandleRuntimePermissions() && listener != null) {
                            listener.permissionsGranted();
                        }
                    }
                });
            }
        }
    }

    // STATIC CHECKERS

    public static boolean areRuntimePermissionsGranted(Context context) {
        return getDeniedRuntimePermissions(context).isEmpty();
    }

    public static List<String> getDeniedRuntimePermissions(Context context) {

        List<String> deniedPermissions = new ArrayList<>();
        for (final String permission: neededDangerousPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                LoggerFactory.getLogger(RuntimePermissionChecker.class).warn("### NO PERMISSION FOR " + permission);
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }
}
