package fi.livi.like.client.android.ui.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormatter {
    public static final String DDMMYYYY_DATE_FORMAT = "dd.MM.yyyy";
    public static final String TIME_FORMAT_NO_SECONDS = "HH:mm";


    private TimeFormatter() {
        // static class, no need for public ctor
    }

    @SuppressLint("SimpleDateFormat")
    public static synchronized String formatToLocalDateTime(String format, Date datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(datetime);
    }
}
