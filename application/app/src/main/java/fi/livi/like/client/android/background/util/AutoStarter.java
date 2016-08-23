package fi.livi.like.client.android.background.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.ui.LikeActivity;

/**
 * Note; Note that the system adds FLAG_EXCLUDE_STOPPED_PACKAGES to all broadcast intents.
 * It does this to prevent broadcasts from background services from inadvertently or unnecessarily
 * launching components of stoppped applications.
 *
 * Applications are in a stopped state when they are first installed but are not yet launched and
 * when they are manually stopped by the user (in Manage Applications).
 */
public class AutoStarter extends BroadcastReceiver {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(AutoStarter.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        log.info("Boot completed - starting LiVi LiKe client");
        Intent newIntent = new Intent(context, LikeActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }
}
