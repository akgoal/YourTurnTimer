package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.controlsdisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerState;

/**
 * Created by Dmitry Akishin on 23.01.2017.
 *
 * Fragment that displays controls as simple text buttons.
 */

public class ControlsTextFragment extends ControlsFragment {

    /* Widgets. */
    private Button mStartButton, mEditButton, mStopButton;

    /** Empty constructor as required in Fragment documentation. */
    public ControlsTextFragment(){ super(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_controls_text, parent, false);

        mStartButton = (Button) v.findViewById(R.id.controls_start_button);
        mStartButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                notifyActivityControlClicked(Controls.START);
            }
        });

        mStopButton = (Button) v.findViewById(R.id.controls_stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                notifyActivityControlClicked(Controls.STOP);
            }
        });

        mEditButton = (Button) v.findViewById(R.id.controls_edit_button);
        mEditButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                notifyActivityControlClicked(Controls.EDIT);
            }
        });

        return v;
    }

    @Override
    public void update(TimerManagerState timersState) {
        if (timersState==null)
            return;

        switch (timersState.getStatus()){
            case RUNNING:
                mStartButton.setText(R.string.pause);
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(true);
                mEditButton.setEnabled(false);
                break;
            case PAUSED:
                mStartButton.setText(R.string.resume);
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(true);
                mEditButton.setEnabled(false);
                break;
            case STOPPED:
                mStartButton.setText(R.string.start);
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                mEditButton.setEnabled(true);
                break;
            case FINISHED:
                mStartButton.setText(R.string.start);
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
                mEditButton.setEnabled(false);
                break;
        }
    }
}
