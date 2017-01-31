package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay;

import android.app.Activity;

import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.TimerDependantFragment;

/**
 * Created by Dmitry Akishin on 23.01.2017.
 * <p>
 * Abstract extension of TimerDependantFragment for fragments that
 * display timers and have switch button for switching between timers.
 * TimerDisplayFragment notifies its host activity when the switch button
 * is clicked via interface OnSwitchButtonClicked. Thus every activity that wants to
 * get such notifications must implement this interface.
 */

public abstract class TimerDisplayFragment extends TimerDependantFragment {

    /** Empty constructor as required in Fragment documentation. */
    public TimerDisplayFragment(){ super(); }

    /**
     * Interface that every host activity must implement if it wants to
     * listen to switching event.
     */
    public interface OnSwitchPressedListener {
        /**
         * The method is invoked when the switch is pressed.
         */
        void onSwitchPressed();
    }

    /**
     * Notifies host activity that switch is pressed.
     */
    protected void notifyActivitySwitchPressed() {
        Activity host = getActivity();
        if (host instanceof OnSwitchPressedListener) {
            ((OnSwitchPressedListener) host).onSwitchPressed();
        }
    }
}
