package com.deakishin.yourturntimer.modellayer.timermanager;

import java.util.List;

/**
 * Created by Dmitry Akishin on 21.01.2017.
 * <p>
 * Interface for a timer manager that manages multiple timers and switches between them.
 * Each initial timer info is encapsulated in a InitialTimer object along with its name.
 */

public interface TimerManager {
    /**
     * Starts manager starting first timer. If timer is started after being paused it will be resumed, otherwise
     * timers will be reset.
     */
    void start();

    /**
     * Switches to the next timer.
     *
     * @param autoResume if true then new timer needs to started immediately,
     *                   if false it needs to be paused.
     */
    void switchToNext(boolean autoResume);

    /**
     * Returns status of the manager.
     *
     * @return status - started, paused or stopped.
     */
    TimerManagerState.Status getStatus();

    /**
     * Pauses current timer.
     */
    void pause();

    /**
     * Stops manager's work and resets timers to their initial values.
     */
    void reset();

    /**
     * Returns state of the manager that includes information about all timers as well as
     * status of the manager.
     *
     * @return service state.
     */
    TimerManagerState getState();

    /**
     * Adds TimerListener that listens to time changes.
     *
     * @param listener listener to add.
     */
    void addTimerListener(TimerListener listener);

    /**
     * Removes TimerListener.
     *
     * @param listener listener to remove.
     */
    void removeTimerListener(TimerListener listener);

    // Following methods concern initial timers.

    /**
     * Adds new InitialTimer to timers in the given position.
     * Note! Modifying new timer after it has been added to manager won't
     * affect data in manager since a copy of the timer is added.
     *
     * @param newTimer timer to add.
     * @param position position in which new timer must be added;
     *                 must be greater than 0;
     *                 if it's greater than number of existing timers
     *                 than it's added to the end.
     */
    void addInitialTimer(int position, InitialTimer newTimer);

    /**
     * Adds new InitialTimer to timers in the end of the timers list.
     * Note! Modifying new timer after it has been added to manager won't
     * affect data in manager since a copy of the timer is added.
     *
     * @param newTimer timer to add.
     */
    void addInitialTimer(InitialTimer newTimer);

    /**
     * Returns list of initial timers in the order in which they are switched.
     * Note! Modifying the list doesn't affect data in the manager since only the copy of them
     * are returned.
     *
     * @return copy list of single timers.
     */
    List<InitialTimer> getInitialTimers();

    /**
     * Removes timer from the list of initial timers.
     *
     * @param position position of timer to remove; must be within range of the timers list.
     */
    void removeInitialTimer(int position);

    /**
     * Removes timers in given positions from the list of initial timers.
     *
     * @param positions list of position of timers to remove;
     *                  if a position is out of range it's ignored.
     */
    void removeInitialTimers(List<Integer> positions);

    /**
     * Edits timer in the given position with data in the given timer.
     *
     * @param position position of timer to remove; must be within range of the timers list.
     * @param timer    InitialTimer object with new data.
     */
    void editInitialTimer(int position, InitialTimer timer);

    /**
     * Moves timer to new position.
     *
     * @param oldPosition position to move timer from;
     *                    must be in range of timers list.
     * @param newPosition position to move timer to;
     *                    must be in range of timers list.
     */
    void moveInitialTimer(int oldPosition, int newPosition);

    /**
     * Checks if a new timer can be added. It might be
     * impossible if there are already maximum number of timers.
     *
     * @return true if it is possible to add new timer.
     */
    boolean canAddInitialTimer();

    /**
     * Changes order of timers to reverse order.
     */
    void reverseInitialTimersOrder();

    /**
     * Changes order of timers to a random order.
     */
    void shuffleInitialTimers();
}
