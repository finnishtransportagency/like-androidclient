package fi.livi.like.client.android.background.util;

import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UncaughtExceptionLogger {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(UncaughtExceptionLogger.class);
    private Thread.UncaughtExceptionHandler mDefaultHandler = null;

    public UncaughtExceptionLogger(Thread thread) {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    log.error(getExceptionText(ex));
                    mDefaultHandler.uncaughtException(thread, ex);
                }
            }
        );
    }

    public static String getExceptionText(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return "\n\n== EXCEPTION ==\n" + throwable.getMessage() + "\n" + throwable.getLocalizedMessage() + "\n" + stringWriter.toString();
    }
}
