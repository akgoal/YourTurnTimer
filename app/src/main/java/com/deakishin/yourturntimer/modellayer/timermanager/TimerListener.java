package com.deakishin.yourturntimer.modellayer.timermanager;

/**
 * Created by Dmitry Akishin on 21.01.2017.
 * <p>
 * Interface for listening to events of changes in timers.
 * TimerListener is notified by TimerManager with every tick of time.
 */

public interface TimerListener {
    /**
     * Method is invoked with every change in timers.
     *
     * @param state current state of timers.
     */
    void onChanged(TimerManagerState state);
}
