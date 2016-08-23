package fi.livi.like.client.android.background.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.slf4j.LoggerFactory;

import fi.livi.like.client.android.background.LikeService;
import fi.livi.like.client.android.background.util.Broadcaster;
import fi.livi.like.client.android.dependencies.backend.User;

public class UserManager {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(UserManager.class);

    public final static String BROADCAST_ACTION_ID = "fi.livi.like.client.android.usermanager";
    public final static String REQUEST_PIN_CODE_KEY = BROADCAST_ACTION_ID + ".pincode";
    public final static String REQUEST_PIN_CODE_VALUE = "request";

    private static final String PREFERENCE_FILE_KEY = "fi.livi.like.client.android.PREFERENCE_FILE";
    private static final String PIN_KEY = "pin_code";

    private final LikeService likeService;
    private final Listener listener;
    private final Broadcaster broadcaster;

    public interface Listener {
        void onPinCodeStored();
    }

    public UserManager(LikeService likeService, Broadcaster broadcaster) {
        this.likeService = likeService;
        this.listener = likeService;
        this.broadcaster = broadcaster;
    }

    public boolean isUserPrepared() {
        final User user = getUser();
        return (user != null && !user.getId().isEmpty());
    }

    public User getUser() {
        return likeService.getDataStorage().getUser();
    }

    public boolean prepareUserPinCode() {
        log.info("prepareUserPinCode()");
        String pinCode = getDecodedPinCode();
        if (pinCode == null) {
            log.info("pin code missing, request from user");
            broadcaster.broadcastLocal(BROADCAST_ACTION_ID, REQUEST_PIN_CODE_KEY, REQUEST_PIN_CODE_VALUE);
            return false;
        }
        return true;
    }

    public boolean prepareUser() {
        log.info("prepareUser()");
        if (!prepareUserPinCode()) {
            return false;
        }
        User user = new User(null, likeService.getBackgroundService().getDeviceInfo().getImei(), getDecodedPinCode());
        log.info(user.toString());
        likeService.getHttpLoader().loadUser(user);
        return true;
    }

    public void storePinCode(String pinCode) {
        SharedPreferences.Editor editor = likeService.getBackgroundService().getSharedPreferences(
                PREFERENCE_FILE_KEY, Context.MODE_PRIVATE).edit();

        editor.putString(PIN_KEY, Base64.encodeToString(pinCode.getBytes(), Base64.DEFAULT));// UTF-8 on android
        editor.commit();
        listener.onPinCodeStored();
    }

    private String getDecodedPinCode() {
        SharedPreferences sharedPref = likeService.getBackgroundService().getSharedPreferences(
                PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        String base64PinCode = sharedPref.getString(PIN_KEY, null);
        if (base64PinCode == null) {
            return null;
        }

        return new String(Base64.decode(base64PinCode, Base64.DEFAULT));
    }
}
