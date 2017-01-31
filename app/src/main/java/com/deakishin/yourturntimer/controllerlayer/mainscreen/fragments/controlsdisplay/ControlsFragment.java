package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.controlsdisplay;

import android.app.Activity;

import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.TimerDependantFragment;

/**
 * Created by Dmitry Akishin on 22.01.2017.
 * <p>
 * Abstract extension of TimerDependantFragment for a fragment that displays and manages control panel.
 * It communicates with its host activity via interface ControlsListener.
 */

public abstract class ControlsFragment extends TimerDependantFragment {

    /** Empty constructor as required in Fragment documentation. */
    public ControlsFragment() {
        super();
    }

    /**
     * Interface for listener to control button clicked events.
     */
    public interface ControlsListener {
        /**
         * Reaction to click on play button.
         */
        void onPlayClicked();

        /**
         * Reaction to click on stop button.
         */
        void onStopClicked();

        /**
         * Reaction to click on edit button.
         */
        void onEditClicked();
    }

    /**
     * Controls.
     */
    protected enum Controls {
        START, STOP, EDIT
    }

    /**
     * Notifies host activity that a control button is clicked.
     *
     * @param controls controls button that was clicked.
     */
    protected void notifyActivityControlClicked(Controls controls) {
        Activity host = getActivity();
        if (host instanceof ControlsListener) {
            switch (controls) {
                case START:
                    ((ControlsListener) host).onPlayClicked();
                    break;
                case STOP:
                    ((ControlsListener) host).onStopClicked();
                    break;
                case EDIT:
                    ((ControlsListener) host).onEditClicked();
                    break;
            }
        }
    }
}
