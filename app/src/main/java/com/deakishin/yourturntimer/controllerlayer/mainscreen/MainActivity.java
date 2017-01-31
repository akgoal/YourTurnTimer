package com.deakishin.yourturntimer.controllerlayer.mainscreen;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.controllerlayer.editscreen.EditActivity;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.TimerDependantFragment;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.controlsdisplay.ControlsFragment;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.controlsdisplay.ControlsIconsFragment;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay.TimerDisplayFragment;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay.tabledisplay.TimerTableFragment;
import com.deakishin.yourturntimer.controllerlayer.services.TimerService;
import com.deakishin.yourturntimer.controllerlayer.sound.SoundPlayer;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerState;

/**
 * Main Activity.
 * Hosts TimerDependantFragment and manages timers by communicating with service by binding to it.
 * Implements interface for getting notifications when timer dependant fragments are need to be
 * updated.
 * Implements interface for listening to switch button pressed events in TimerDisplayFragment.
 * Implements interface for listening to clicks on control buttons.
 */
public class MainActivity extends AppCompatActivity implements TimerDisplayFragment.OnSwitchPressedListener,
        ControlsFragment.ControlsListener, TimerDependantFragment.FragmentUpdateRequestListener {

    /* Fragment that displays timers and switch button. */
    private TimerDisplayFragment mTimerFragment;
    /* Fragment that displays controls buttons. */
    private ControlsFragment mControlsFragment;

    /* Listeners to service changes. */
    private TimerService.TimerUpdateListener mServiceListener;

    /* Last known state of mTimerManager. */
    private TimerManagerState mTimersState;

    /* Service for running timers in background. */
    private TimerService mTimerService;
    /* If bound to service. */
    private boolean mBound;

    /* Whether or not the screen is being kept turned on. */
    private boolean mScreenKeptOn = false;

    /* Sound player to play sounds. */
    private SoundPlayer mSoundPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();

        mTimerFragment = (TimerDisplayFragment) fm.findFragmentById(R.id.timerFragmentContainer);
        if (mTimerFragment == null) {
            mTimerFragment = new TimerTableFragment();
            fm.beginTransaction().add(R.id.timerFragmentContainer, mTimerFragment).commit();
        }

        mControlsFragment = (ControlsFragment) fm.findFragmentById(R.id.controlsFragmentContainer);
        if (mControlsFragment == null) {
            mControlsFragment = new ControlsIconsFragment();
            fm.beginTransaction().add(R.id.controlsFragmentContainer, mControlsFragment).commit();
        }

        mSoundPlayer = new SoundPlayer();
    }

    /* Binding to service. */
    @Override
    protected void onStart() {
        super.onStart();
        // Start TimerService and bind to it.
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            // Unregister listener.
            if (mTimerService != null) {
                mTimerService.removeListener(mServiceListener);
            }

            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update with fresh data in case it's changed.
        if (mTimerService != null && mServiceListener != null) {
            mTimerService.updateMe(mServiceListener);
        }
    }

    @Override
    public void onDestroy(){
        mSoundPlayer.stop();
        super.onDestroy();
    }

    /**
     * Defines callbacks for service binding, passed to bindService().
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to TimerService, cast the IBinder and get TimerService instance.
            TimerService.ServiceBinder binder = (TimerService.ServiceBinder) service;
            mTimerService = binder.getService();
            mBound = true;

            // Registering service listener.
            if (mTimerService != null) {
                mServiceListener = new TimerService.TimerUpdateListener() {
                    @Override
                    public void onTimerUpdated(TimerManagerState state, boolean timeUp) {
                        if (timeUp){
                            playTimeUpSound();
                        }
                        update(state);
                    }
                };
                mTimerService.addTimerUpdateListener(mServiceListener);
                mTimerService.updateMe(mServiceListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /* Performs updates with new timer manager state. */
    private void update(TimerManagerState state) {
        mTimersState = state;

        setKeepScreenOn(mTimersState.getStatus() == TimerManagerState.Status.RUNNING);

        if (mTimerFragment != null) {
            mTimerFragment.update(mTimersState);
        }
        if (mControlsFragment != null) {
            mControlsFragment.update(mTimersState);
        }
    }

    /* Plays sound indicating that the current timer's time has ended. */
    private void playTimeUpSound(){
        mSoundPlayer.play(this, R.raw.sound_time_up);
    }

    /* If (keepScreenOn) the screen needs to be kept turned on.
     * Otherwise it's not necessary. */
    private void setKeepScreenOn(boolean keepScreenOn) {
        if (mScreenKeptOn == keepScreenOn) {
            return;
        }

        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mScreenKeptOn = true;
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mScreenKeptOn = false;
        }
    }

    @Override
    public void updateFragment(TimerDependantFragment fragment) {
        fragment.update(mTimersState);
    }

    @Override
    public void onSwitchPressed() {
        if (mTimerService != null) {
            mTimerService.invokeSwitch();
        }
    }

    @Override
    public void onPlayClicked() {
        if (mTimerService != null) {
            mTimerService.invokePlay();
        }
    }

    @Override
    public void onStopClicked() {
        if (mTimerService != null) {
            mTimerService.invokeStop();
        }
    }

    @Override
    public void onEditClicked() {
        if (mTimersState == null)
            return;

        switch (mTimersState.getStatus()) {
            case RUNNING:
            case PAUSED:
                break;
            case STOPPED:
            case FINISHED:
                Intent i = new Intent(this, EditActivity.class);
                startActivity(i);
                break;
        }
    }
}
