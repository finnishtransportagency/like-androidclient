package fi.livi.like.client.android.background.googleplayservices;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.slf4j.LoggerFactory;

public class GooglePlayServiceLibraryChecker {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(GooglePlayServiceLibraryChecker.class);

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 4200;
    private final Activity activity;

    public interface ClientActivity {
        void onGooglePlayServicesPrepared();
    }

    public static GooglePlayServiceLibraryChecker create(Activity activity) {
        return new GooglePlayServiceLibraryChecker(activity);
    }

    public GooglePlayServiceLibraryChecker(Activity activity) {
        this.activity = activity;
    }

    /**
     * prepareGooglePlayServices() confirms android device has correct version of Google Play Services
     * installed. If it's not, it will launch installation dialogs to help user installing it.
     *
     * It will call onGooglePlayServicesPrepared() when installation is done.
     */
    public void prepareGooglePlayServices() {
        GoogleApiAvailability availabilityApi = GoogleApiAvailability.getInstance();
        int code = availabilityApi.isGooglePlayServicesAvailable(activity);
        if (code == ConnectionResult.SUCCESS) {
            log.info("no problems with google play services");
            ((ClientActivity)activity).onGooglePlayServicesPrepared();
        } else if (availabilityApi.isUserResolvableError(code) &&
                availabilityApi.showErrorDialogFragment(activity, code, REQUEST_GOOGLE_PLAY_SERVICES)) {
            log.warn("resolving problems with google play services");
        } else {
            log.warn("problems with google play services");
            String str = availabilityApi.getErrorString(code);
            Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * For most of documentation, see Activity.onActivityResult documentation.
     * This returns also boolean if result hase been handled.
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_GOOGLE_PLAY_SERVICES == requestCode) {
            //log.info("SUCCESS:" + ConnectionResult.SUCCESS);//0
            //log.info("RESULT_OK:" + Activity.RESULT_OK);//-1
            log.info("resultCode:" + resultCode);
            // resultCode-parameter seems to always be not_ok, even when returning after services installation.
            // Recheck situation instead directly from service itself.
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS) {
                log.info("no problems with google play services");
                ((ClientActivity)activity).onGooglePlayServicesPrepared();
            } else {
                log.warn("problems still with google play services - result code:" + resultCode);
            }
            return true;
        }
        return false;
    }
}
