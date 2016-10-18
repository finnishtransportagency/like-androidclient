package fi.livi.like.client.android.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;

import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import fi.livi.like.client.android.R;

import static android.R.string.ok;

public class SettingsChecker {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SettingsChecker.class);

    private final Activity activity;

    public SettingsChecker(Activity activity) {
        this.activity = activity;
    }

    public boolean isSettingsEnabled() {
        return isGpsEnabled() && isMobileDataEnabled();
    }

    private boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showNotification(R.string.gps_not_enabled, new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return false;
        }
        return true;
    }

    private boolean isMobileDataEnabled() {
        // hackish..
        boolean mobileDataEnabled = false;
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataEnabled = (Boolean)method.invoke(cm);
            if (!mobileDataEnabled) {
                Intent settingsIntent = new Intent(Intent.ACTION_MAIN);
                settingsIntent.setComponent(new ComponentName("com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity"));
                showNotification(R.string.mobile_data_not_enabled, settingsIntent);
                return false;
            }
        } catch (Exception e) {
            log.warn("cannot check if mobile data is enabled or not - " + e.getMessage());
        }
        return mobileDataEnabled;
    }

    private void showNotification(int messageResId, final Intent settingsIntent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getResources().getString(messageResId))
                .setCancelable(false)
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        activity.startActivity(settingsIntent);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}

