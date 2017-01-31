package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerState;

/**
 * Created by Dmitry Akishin on 22.01.2017.
 * <p>
 * Abstract  class for a fragment that is dependant on information about timers.
 * It can be updated to display new timers data.
 * To let the host activity know where it's need to be updated, the fragment sends
 * request via interface FragmentUpdateRequestListener. Thus every activity that wants
 * to update the fragment from the start must implement this method.
 */

public abstract class TimerDependantFragment extends Fragment {

    /** Empty constructor as required in Fragment documentation. */
    public TimerDependantFragment() {
        super();
    }

    /**
     * Interface for getting notifications when fragment is need to get data to be updated.
     */
    public interface FragmentUpdateRequestListener {
        /**
         * Reaction to an instance of TimerDependantFragment getting request to be updated.
         *
         * @param fragment an instance of TimerDependantFragment that wants to be updated.
         */
        void updateFragment(TimerDependantFragment fragment);
    }

    /**
     * Abstract method that updates fragment with new timers' data.
     *
     * @param timersState state of the timer service to update views according to fresh data.
     */
    public abstract void update(TimerManagerState timersState);

    @Override
    public void onResume() {
        super.onResume();

        Activity host = getActivity();
        if (host instanceof FragmentUpdateRequestListener) {
            ((FragmentUpdateRequestListener) host).updateFragment(this);
        }
    }
}
