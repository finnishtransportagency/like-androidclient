package fi.livi.like.client.android.background.service;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * DeviceInfo provides details from device.
 */
public class DeviceInfo {
    private String imei;
    private Context context;

    public DeviceInfo(Context context) {
        this.context = context;
    }

    /**
     * Get IMEI-code of device
     * @return String containing IMEI-code
     */
    public String getImei() {
        if (imei == null) {
            imei = (((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
        }
        return imei;
    }
}
