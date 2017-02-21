package com.deakishin.yourturntimer.controllerlayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.modellayer.TimeFormatter;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.MainActivity;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerListener;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManager;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerImpl;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry Akishin on 24.01.2017.
 * <p>
 * Bound service for running TimerManager instance in
 * background and communicating with clients via binding.
 * Service provides access to its methods through Binder implementation.
 */

public class TimerService extends Service {

    /* Binder given to clients. */
    private final IBinder mBinder = new ServiceBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ServiceBinder extends Binder {
        public TimerService getService() {
            // Return this instance of TimerService so clients can call public methods
            return TimerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* List of mListeners that listen to timer updates. */
    private List<TimerUpdateListener> mListeners = new ArrayList<>();

    /* Own listener to timer. */
    private TimerListener mTimerListener;

    private TimerManager mTimerManager;

    /* State of timer manager. */
    private TimerManagerState mTimerState;

    /* Service is initialised and configured. */
    private boolean mInitialised = false;

    /* Notification Manager to show notifications. */
    private NotificationManager mNotificationManager;

    // Unique Id for Notification to start and cancel it.
    private static final int NOTIFICATION_ID = R.string.timer_service_notification_ticker;

    // Builder for notification.
    NotificationCompat.Builder mNotificationBuilder;

    // Notification is showing.
    private boolean mNotificationShowing;

    /**
     * Interface for mListeners to timers data being updated.
     */
    public interface TimerUpdateListener {
        /**
         * Invoked when fresh timers' data is available.
         *
         * @param state  new state of timer manager.
         * @param timeUp current timer's time has just ended.
         */
        void onTimerUpdated(TimerManagerState state, boolean timeUp);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return START_STICKY;
    }

    /* Initialises and configures service. */
    private void init() {
        if (!mInitialised) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Construct notification builder.
            // The PendingIntent to launch activity if the user selects the notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            // Set the info for the views that show in the notification panel.
            String ticker = getString(R.string.timer_service_notification_ticker, getString(R.string.app_name));
            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setTicker(ticker)
                    .setSmallIcon(R.drawable.ic_status_bar)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .setOngoing(true);

            mTimerManager = TimerManagerImpl.getInstance(this);
            mTimerState = mTimerManager.getState();
            /* Start listening to timer changes. */
            mTimerListener = new TimerListener() {
                @Override
                public void onChanged(TimerManagerState state) {
                    mTimerState = state;

                    boolean currTimerIsFinished = false;
                    if (mTimerState.getStatus() == TimerManagerState.Status.RUNNING
                            && mTimerState.getCurrentTimer().isFinished()) {
                        currTimerIsFinished = true;
                        mTimerManager.switchToNext(false);
                    }

                    updateListeners(mTimerState, currTimerIsFinished);

                    updateNotification();

                    stopIfUseless();
                }
            };
            mTimerManager.addTimerListener(mTimerListener);

            updateNotification();

            mInitialised = true;
        }
    }

    /* Updates mListeners with new timer manager state. */
    private void updateListeners(TimerManagerState state, boolean timeUp) {
        for (TimerUpdateListener listener : mListeners) {
            listener.onTimerUpdated(state, timeUp);
        }
    }

    /* Returns true if there are mListeners. */
    private boolean hasListeners() {
        return !mListeners.isEmpty();
    }

    /* Stops if there are no listeners and timer manager is not running. */
    private void stopIfUseless() {
        if (!hasListeners() && mTimerState.getStatus() != TimerManagerState.Status.RUNNING) {
            stopSelf();
        }
    }

    /* Updates notification.
     * Notification is shown when there are no listeners and
     * timer manager is still running. */
    private void updateNotification() {
        if (mNotificationBuilder == null || mTimerState == null || mTimerState.getCurrentTimer() == null) {
            cancelNotification();
            return;
        }

        if (!hasListeners()) {
            switch (mTimerState.getStatus()) {
                case STOPPED:
                    cancelNotification();
                    break;
                case FINISHED:
                    if (!mNotificationShowing) break;
                case PAUSED:
                    if (!mNotificationShowing) break;
                case RUNNING:
                    String title = getString(R.string.timer_service_notification_title, mTimerState.getCurrentIndex() + 1,
                            mTimerState.getCurrentTimer().getName());
                    String text = TimeFormatter.format(mTimerState.getCurrentTimer().getCurrentTime());
                    Notification notification = mNotificationBuilder
                            .setContentText(text)
                            .setContentTitle(title)
                            .build();

                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                    mNotificationShowing = true;
                    break;
            }
        } else {
            cancelNotification();
        }
    }

    /* Cancels notification. */
    private void cancelNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
        mNotificationShowing = false;
    }

    @Override
    public void onDestroy() {
        /* Stop listening to timer changes. */
        mTimerManager.removeTimerListener(mTimerListener);

        super.onDestroy();
    }


    /**
     * Reacts to play being invoked.
     */
    public void invokePlay() {
        switch (mTimerState.getStatus()) {
            case RUNNING:
                mTimerManager.pause();
                break;
            case PAUSED:
            case FINISHED:
            case STOPPED:
                mTimerManager.start();
                break;
        }
    }

    /**
     * Reacts to stop being invoked.
     */
    public void invokeStop() {
        switch (mTimerState.getStatus()) {
            case RUNNING:
            case PAUSED:
            case FINISHED:
                mTimerManager.reset();
                break;
            case STOPPED:
                break;
        }
    }

    /**
     * Reacts to switch being invoked.
     */
    public void invokeSwitch() {
        switch (mTimerState.getStatus()) {
            case RUNNING:
                mTimerManager.switchToNext(true);
                break;
            case PAUSED:
            case STOPPED:
                //mTimerManager.start();
                break;
            case FINISHED:
                break;
        }
    }

    /**
     * Updates a timer update listener.
     */
    public void updateMe(TimerUpdateListener listener) {
        if (listener != null) {
            listener.onTimerUpdated(mTimerState, false);
        }
    }

    /**
     * Adds new listener that listens to timer updates.
     *
     * @param listener new listener
     */
    public void addTimerUpdateListener(TimerUpdateListener listener) {
        if (listener != null) {
            mListeners.add(listener);
        }
        updateNotification();
    }

    /**
     * Removes timer updates listener.
     *
     * @param listener listener ot remove.
     */
    public void removeListener(TimerUpdateListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
        updateNotification();
        stopIfUseless();
    }
}
