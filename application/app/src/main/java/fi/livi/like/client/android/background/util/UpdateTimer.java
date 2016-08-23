package fi.livi.like.client.android.background.util;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateTimer {
    private Timer mUpdateTimer = null;
    private TimerTask mUpdateTask = null;
    private Update mUpdated = null;

    public class UpdateTask extends TimerTask {
        @Override
        public void run() {
            mUpdated.onTimerUpdate();
        }
    }

    public interface Update {
        void onTimerUpdate();
    }

    public UpdateTimer(Update updated) {
        this.mUpdated = updated;
    }

    public void startTimer(int intervalInSecs) {
        if (mUpdateTask == null) {
            mUpdateTask = new UpdateTask();
        }
        if (mUpdateTimer == null) {
            mUpdateTimer = new Timer();
        }

        mUpdateTimer.schedule(mUpdateTask, intervalInSecs * 1000L, intervalInSecs * 1000L);
    }

    public void stopTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer.purge();
        }
        mUpdateTimer = null;
        mUpdateTask = null;
    }
}
