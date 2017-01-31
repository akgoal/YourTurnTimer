package com.deakishin.yourturntimer.modellayer.timermanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dmitry Akishin on 21.01.2017.
 * Singleton class implementing TimeManager using CountDownTimer for each timer.
 */

public class TimerManagerImpl implements TimerManager {

    /* Number of milliseconds between updates. */
    private static final long UPDATE_INTERVAL = 50;

    /* Maximum number of timers. */
    private static final int MAX_TIMERS = 100;

    /* List of timers, determining initial timers. */
    private List<InitialTimer> mTimers = new ArrayList<>();

    /* Time in milliseconds left for every timer. Length of the array must be the same as the size of the timers list. */
    private long[] mTimeLeft;

    /* Index of current timer in a list of players. */
    private int mCurrentIndex;

    /* Current timer that ticks. */
    private CountDownTimer mCurrentTimer;

    /* List of listeners that are notified with every change in timers.
     * Listeners are notified every UPDATE_INTERVAL milliseconds. */
    private List<TimerListener> mListeners = new ArrayList<>();

    /* Flags for the manager state.
     * mFinished - all timers are finished. */
    private boolean mPaused, mStarted, mFinished;

    /* Application context. */
    private Context mContext;

    /* Helper to store data in memory and load data from it. */
    private StorageHelper mStorageHelper;

    private static TimerManagerImpl sInstance;

    /**
     * Returns the sole instance of the class, creating it if there isn't one.
     *
     * @param context context of the application.
     * @return the instance of the class
     */
    public static TimerManagerImpl getInstance(Context context) {
        if (sInstance == null)
            sInstance = new TimerManagerImpl(context.getApplicationContext());
        return sInstance;
    }

    private TimerManagerImpl(Context context) {
        mStorageHelper = new StorageHelper(context);
        mStorageHelper.loadData();
    }

    @Override
    public void start() {
        stopCurrentTimer();

        if (mTimers == null || mTimers.isEmpty()) {
            stop();
            return;
        }

        mStarted = true;

        if (mPaused) {
            mPaused = false;
            mStorageHelper.wipeOutProgress();
        } else {
            // Reset to initial values.
            resetTimers();
        }
        switchTo(mCurrentIndex, true);
    }

    /* Switches to the timer with given index.
     * If there is no time left for that timer, switches to the next one.
     * Notifies listeners with new state and if there is no unfinished timer.
     * Manager must be started and not paused.
     * Param autoResume - if true timer needs to be started immediately after switching.
     * Otherwise timer pauses.*/
    private void switchTo(int index, boolean autoResume) {
        if (index < 0 || index >= mTimers.size()) {
            return;
        }

        if (!mStarted || mPaused) {
            return;
        }

        // Until an unfinished one is found switch to the next timer. If there is none then notify listeners.
        int oldIndex = mCurrentIndex;
        mCurrentIndex = index;
        while (mTimeLeft[mCurrentIndex] == 0) {
            mCurrentIndex = nextIndex(mCurrentIndex);
            if (mCurrentIndex == index) {
                stop();
                mFinished = true;
                mCurrentIndex = oldIndex;
                mStorageHelper.saveProgress();
                notifyListeners();
                return;
            }
        }

        if (autoResume) {
            mCurrentTimer = new NotifyingCountDownTimer(mTimeLeft[mCurrentIndex]);
            mCurrentTimer.start();
        } else {
            pause();
        }
        notifyListeners();
    }

    /* Stops current timer. */
    private void stopCurrentTimer() {
        if (mCurrentTimer != null) {
            mCurrentTimer.cancel();
            mCurrentTimer = null;
        }
    }

    @Override
    public void switchToNext(boolean resume) {
        stopCurrentTimer();

        // Return if the manager is not running
        if (!mStarted || mPaused) {
            notifyListeners();
            return;
        }

        switchTo(nextIndex(mCurrentIndex), resume);
    }

    @Override
    public TimerManagerState.Status getStatus() {
        if (mStarted) {
            if (mPaused) {
                return TimerManagerState.Status.PAUSED;
            } else {
                return TimerManagerState.Status.RUNNING;
            }
        } else {
            if (mFinished) {
                return TimerManagerState.Status.FINISHED;
            } else {
                return TimerManagerState.Status.STOPPED;
            }
        }
    }

    /* Returns next index in the array; after the last index comes the first one. */
    private int nextIndex(int index) {
        return (index + 1) % mTimeLeft.length;
    }

    @Override
    public void pause() {
        stopCurrentTimer();
        if (mStarted) {
            mPaused = true;
            mStorageHelper.saveProgress();
        }
        notifyListeners();
    }

    /* Resets timers to their initial values. */
    private void resetTimers() {
        mTimeLeft = new long[mTimers.size()];
        for (int i = 0; i < mTimers.size(); i++) {
            mTimeLeft[i] = mTimers.get(i).getTime();
        }

        mCurrentIndex = 0;
    }

    @Override
    public void reset() {
        stop();

        mFinished = false;
        mStorageHelper.wipeOutProgress();

        resetTimers();

        notifyListeners();
    }

    /* Stops manager without resetting. */
    private void stop() {
        stopCurrentTimer();
        mPaused = false;
        mStarted = false;
    }

    @Override
    public TimerManagerState getState() {
        TimerManagerState state = new TimerManagerState();
        for (int i = 0; i < mTimers.size(); i++) {
            InitialTimer initTimer = mTimers.get(i);
            long timeLeft = (i < mTimeLeft.length) ? mTimeLeft[i] : 0;
            state.addTimer(new TimerManagerState.Timer(initTimer.getName(), initTimer.getTime(), timeLeft));
        }
        state.setCurrentIndex(mCurrentIndex);
        state.setStatus(getStatus());

        return state;
    }

    @Override
    public void addTimerListener(TimerListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeTimerListener(TimerListener listener) {
        mListeners.remove(listener);
    }

    /* Reacts to initial timers being changed. */
    private void onInitialTimersChanged() {
        reset();
        mStorageHelper.saveInitialTimers();
        mStorageHelper.loadData();
    }

    @Override
    public void addInitialTimer(int position, InitialTimer newTimer) {
        if (position < 0 || newTimer == null) {
            return;
        }
        if (position >= mTimers.size()) {
            mTimers.add(newTimer.copy());
        } else {

            mTimers.add(position, newTimer.copy());
        }
        onInitialTimersChanged();
    }

    @Override
    public void addInitialTimer(InitialTimer newTimer) {
        if (newTimer != null && canAddInitialTimer()) {
            mTimers.add(newTimer.copy());
            onInitialTimersChanged();
        }
    }

    @Override
    public List<InitialTimer> getInitialTimers() {
        List<InitialTimer> res = new ArrayList<>();
        for (InitialTimer timer : mTimers) {
            res.add(timer.copy());
        }
        return res;
    }

    @Override
    public void removeInitialTimer(int position) {
        if (position >= 0 && position < mTimers.size()) {
            mTimers.remove(position);
            onInitialTimersChanged();
        }
    }

    @Override
    public void removeInitialTimers(List<Integer> positions) {
        // Sort list in descending order.
        List<Integer> orderedList = new ArrayList<Integer>();
        for (Integer pos : positions) {
            boolean added = false;
            for (int i = 0; i < orderedList.size(); i++) {
                if (pos > orderedList.get(i)) {
                    orderedList.add(i, pos);
                    added = true;
                    break;
                }
            }
            if (!added) {
                orderedList.add(pos);
            }
        }

        // Now we can safely remove positions.
        for (int pos : orderedList) {
            if (pos >= 0 && pos < mTimers.size()) {
                mTimers.remove(pos);
            }
        }

        // Add default timer if no timers left
        if (mTimers.isEmpty()) {
            if (mStorageHelper != null) {
                mTimers.add(mStorageHelper.DEFAULT_TIMERS[0].copy());
            }
        }

        onInitialTimersChanged();
    }

    @Override
    public void editInitialTimer(int position, InitialTimer timer) {
        if (position >= 0 && position < mTimers.size() && timer != null) {
            mTimers.get(position).setData(timer);
            onInitialTimersChanged();
        }
    }

    @Override
    public void moveInitialTimer(int oldPosition, int newPosition) {
        if (oldPosition >= 0 && oldPosition < mTimers.size() &&
                newPosition >= 0 && newPosition < mTimers.size()) {
            InitialTimer timer = mTimers.get(oldPosition);
            mTimers.remove(oldPosition);
            mTimers.add(newPosition, timer);
            onInitialTimersChanged();
        }
    }

    @Override
    public boolean canAddInitialTimer() {
        return (mTimers != null && mTimers.size() < MAX_TIMERS);
    }

    @Override
    public void reverseInitialTimersOrder() {
        // Go from the end to the beginning of the list
        // and move elements to the end.
        // This way order of items becomes reverse.
        for (int i = mTimers.size() - 1; i >= 0; i--) {
            mTimers.add(mTimers.get(i));
            mTimers.remove(i);
        }
        onInitialTimersChanged();
    }

    @Override
    public void shuffleInitialTimers() {
        if (mTimers.size() < 2) {
            // If there are less then 2 timers,
            // it doens't make sense to do shuffleInitialTimers.
            return;
        }
        if (mTimers.size() == 2) {
            // If there are two timers
            // just swap them by reversing order.
            reverseInitialTimersOrder();
            return;
        }
        // Move random timer to the end.
        // Then continue randomizing other timers
        // that are not moved yet.
        Random rand = new Random();
        boolean sameOrder = true;
        for (int i = mTimers.size(); i > 0; i--) {
            int pos = rand.nextInt(i);
            sameOrder = sameOrder && pos == i - 1;
            mTimers.add(mTimers.get(pos));
            mTimers.remove(pos);
        }

        // If new order is the same as the old one,
        // Just move first timer to the end.
        if (sameOrder){
            mTimers.add(mTimers.get(0));
            mTimers.remove(0);
        }

        onInitialTimersChanged();
    }

    /* Notifies listeners with current state. */
    private void notifyListeners() {
        TimerManagerState state = getState();
        for (TimerListener listener : mListeners) {
            listener.onChanged(state);
        }
    }

    /**
     * Extension of CountDownTimer that notifies listeners with its progress.
     */
    private class NotifyingCountDownTimer extends CountDownTimer {


        /**
         * @param millisInFuture The number of millis in the future from the call
         *                       to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                       is called.
         */
        NotifyingCountDownTimer(long millisInFuture) {
            super(millisInFuture, UPDATE_INTERVAL);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mTimeLeft[mCurrentIndex] = millisUntilFinished;
            notifyListeners();
        }

        @Override
        public void onFinish() {
            mTimeLeft[mCurrentIndex] = 0;
            notifyListeners();
        }
    }

    /**
     * Helper class to save and load manager's data.
     * Designed as an inner class to get exclusive access
     * to manager's internals.
     */
    private class StorageHelper {

        /* Keys for storing data in SharedPreferences. */
        private static final String PREF_TIMERS = "timers";
        private static final String PREF_TIMELEFT = "timeLeft";
        private static final String PREF_CURRENT_INDEX = "currIndex";

        /* Delimiter between timeLeft values for writing and reading them. */
        private static final String TIMELEFT_DELIMITER = ";";

        /* Data is saved to and loaded from application's shared preferences. */
        private SharedPreferences mPrefs;

        /**
         * Default initial timers.
         */
        public final InitialTimer[] DEFAULT_TIMERS = {
                new InitialTimer("Timer 1", 30000),
                new InitialTimer("Timer 2", 30000)
        };

        /**
         * Constructs object by taking app context as parameter.
         *
         * @param context application context.
         */
        StorageHelper(Context context) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }

        /**
         * Saves list of initial timers.
         */
        void saveInitialTimers() {
            if (mTimers == null) {
                return;
            }

            try {
                JSONArray array = new JSONArray();
                for (InitialTimer timer : mTimers) {
                    array.put(timer.toJSON());
                }
                mPrefs.edit().putString(PREF_TIMERS, array.toString()).commit();
            } catch (JSONException e) {
            }
        }

        /**
         * Saves current progress, consisting of
         * array of time left for each timer.
         * If manager is not running than data gets wiped out.
         */
        void saveProgress() {
            if (mTimeLeft == null) {
                wipeOutProgress();
                return;
            }

            if (!mStarted && !mFinished) {
                wipeOutProgress();
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (long time : mTimeLeft) {
                sb.append(Long.toString(time)).append(TIMELEFT_DELIMITER);
            }
            mPrefs.edit().putString(PREF_TIMELEFT, sb.toString())
                    .putInt(PREF_CURRENT_INDEX, mCurrentIndex).commit();
        }

        /* Wipes out progress data. */
        void wipeOutProgress() {
            mPrefs.edit().remove(PREF_TIMELEFT).remove(PREF_CURRENT_INDEX).commit();
        }

        /* Loads default timers. */
        private void loadDefaultTimers() {
            mTimers = new ArrayList<>();
            for (InitialTimer timer : DEFAULT_TIMERS) {
                mTimers.add(timer.copy());
            }
        }

        /**
         * Loads data from memory and sets values in manager's fields.
         * First, timers are loaded. If failed, timers are set
         * to default values and all saved data gets wiped out.
         * If timers are loaded successfully, then flags and timeLeft values are loaded.
         * TimeLeft values are how much time is left for each timer.
         * If they don't exist contradict timers then manager resets and saved timeLeft data
         * gets wiped out.
         */
        void loadData() {
            mTimers = null;

            String timersString = mPrefs.getString(PREF_TIMERS, null);
            if (timersString != null) {
                JSONArray array = null;
                try {
                    array = (JSONArray) new JSONTokener(timersString).nextValue();
                    if (array != null) {
                        mTimers = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            InitialTimer timer = new InitialTimer(array.getJSONObject(i));
                            mTimers.add(timer);
                        }
                    }
                } catch (JSONException e) {
                    mTimers = null;
                }
            }

            if (mTimers == null || mTimers.isEmpty()) {
                mPrefs.edit().remove(PREF_TIMERS).commit();
                wipeOutProgress();
                loadDefaultTimers();
                reset();
                return;
            }

            mTimeLeft = null;
            String timeLeftString = mPrefs.getString(PREF_TIMELEFT, null);
            if (timeLeftString != null) {
                String[] timeLeftStringArray = timeLeftString.split(TIMELEFT_DELIMITER);
                mTimeLeft = new long[timeLeftStringArray.length];
                try {
                    for (int i = 0; i < timeLeftStringArray.length; i++) {
                        String s = timeLeftStringArray[i];
                        mTimeLeft[i] = Long.valueOf(s);
                    }
                } catch (NumberFormatException e) {
                    mTimeLeft = null;
                }
            }

            mCurrentIndex = mPrefs.getInt(PREF_CURRENT_INDEX, -1);
            if (mTimeLeft == null || mTimeLeft.length != mTimers.size()
                    || mCurrentIndex < 0 || mCurrentIndex >= mTimers.size()) {
                wipeOutProgress();
                reset();
            } else {
                mFinished = true;
                for (long time : mTimeLeft) {
                    mFinished = (time == 0);
                    if (!mFinished) {
                        break;
                    }
                }
                mStarted = mPaused = !mFinished;
            }
        }
    }
}
