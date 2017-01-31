package com.deakishin.yourturntimer.modellayer.timermanager;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry Akishin on 22.01.2017.
 * State of TimerManager. Used for updating listeners on current progress.
 * TimerManagerState implements Parcelable interface for sending it within
 * application and save to shared preferences.
 */
public class TimerManagerState implements Parcelable {

    /**
     * Status of the service.
     * Can be one of following:
     * RUNNING - service is running changing current timer,
     * PAUSED - service is paused,
     * STOPPED - service is not running,
     * FINISHED - service is not running and all timers are finished.
     */
    public enum Status {
        RUNNING, PAUSED, STOPPED, FINISHED
    }

    /* List of timers with their current time. */
    private List<Timer> mTimers = new ArrayList<>();

    /* Index of the current timer. */
    private int mCurrentIndex;

    /* Status of the manager. */
    private Status mStatus;

    public TimerManagerState() {
    }

    protected TimerManagerState(Parcel in) {
        mCurrentIndex = in.readInt();
    }

    public static final Creator<TimerManagerState> CREATOR = new Creator<TimerManagerState>() {
        @Override
        public TimerManagerState createFromParcel(Parcel in) {
            return new TimerManagerState(in);
        }

        @Override
        public TimerManagerState[] newArray(int size) {
            return new TimerManagerState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCurrentIndex);
    }

    public List<Timer> getTimers() {
        return mTimers;
    }

    /**
     * Return current timers.
     *
     * @return current timer or null if current index is out of range.
     */
    public Timer getCurrentTimer() {
        return mCurrentIndex < mTimers.size() ? mTimers.get(mCurrentIndex) : null;
    }

    public void setTimers(List<Timer> timers) {
        mTimers = timers;
    }

    /**
     * Adds timer to the list of timers.
     *
     * @param timer timer to add.
     */
    public void addTimer(Timer timer) {
        mTimers.add(timer);
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    /**
     * Individual timer with its own name, initial time and current time.
     */
    public static class Timer {
        /* Name. */
        private String mName;
        /* Initial time. */
        private long mInitialTime;
        /* Current time. */
        private long mCurrentTime;

        public Timer(String name, long initialTime, long currentTime) {
            mName = name;
            mInitialTime = initialTime;
            mCurrentTime = currentTime;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public long getInitialTime() {
            return mInitialTime;
        }

        public void setInitialTime(long initialTime) {
            mInitialTime = initialTime;
        }

        public long getCurrentTime() {
            return mCurrentTime;
        }

        /**
         * Returns true if there is no time left for this timer.
         *
         * @return true if no time left.
         */
        public boolean isFinished() {
            return mCurrentTime <= 0;
        }

        public void setCurrentTime(long currentTime) {
            mCurrentTime = currentTime;
        }
    }
}
