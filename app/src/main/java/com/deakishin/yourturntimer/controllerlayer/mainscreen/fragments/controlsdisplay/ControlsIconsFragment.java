package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.controlsdisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerState;

/**
 * Created by Dmitry Akishin on 23.01.2017.
 * <p>
 * Fragment that displays controls as icons.
 */

public class ControlsIconsFragment extends ControlsFragment {

    /* Widgets. */
    private ImageButton mStartButton, mEditButton, mStopButton;

    /* Transparency for disabled icons. */
    private float mDisabledIconsTransparency = 1f;

    /**
     * Empty constructor as required in Fragment documentation.
     */
    public ControlsIconsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int transp = getActivity().getResources().getInteger(R.integer.disabled_icon_transparency);
        if (transp >= 0 && transp <= 255) {
            mDisabledIconsTransparency = transp / (float) 255;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_controls_icons, parent, false);

        mStartButton = (ImageButton) v.findViewById(R.id.fragment_controls_icons_start_imageButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                notifyActivityControlClicked(Controls.START);
            }
        });

        mStopButton = (ImageButton) v.findViewById(R.id.fragment_controls_icons_stop_imageButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                notifyActivityControlClicked(Controls.STOP);
            }
        });

        mEditButton = (ImageButton) v.findViewById(R.id.fragment_controls_icons_edit_imageButton);
        mEditButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                notifyActivityControlClicked(Controls.EDIT);
            }
        });

        return v;
    }

    /* Sets enabled/disabled state for ImageButton. */
    private void setEnabled(ImageButton imageButton, boolean enabled) {
        imageButton.setAlpha(enabled ? 1f : mDisabledIconsTransparency);
        imageButton.setEnabled(enabled);
    }

    @Override
    public void update(TimerManagerState timersState) {
        if (timersState == null)
            return;

        switch (timersState.getStatus()) {
            case RUNNING:
                mStartButton.setImageResource(R.drawable.ic_pause);
                setEnabled(mStartButton, true);
                setEnabled(mStopButton, true);
                setEnabled(mEditButton, false);
                break;
            case PAUSED:
                mStartButton.setImageResource(R.drawable.ic_resume);
                setEnabled(mStartButton, true);
                setEnabled(mStopButton, true);
                setEnabled(mEditButton, false);
                break;
            case STOPPED:
                mStartButton.setImageResource(R.drawable.ic_play);
                setEnabled(mStartButton, true);
                setEnabled(mStopButton, false);
                setEnabled(mEditButton, true);
                break;
            case FINISHED:
                mStartButton.setImageResource(R.drawable.ic_play);
                setEnabled(mStartButton, false);
                setEnabled(mStopButton, true);
                setEnabled(mEditButton, false);
                break;
        }
    }
}
