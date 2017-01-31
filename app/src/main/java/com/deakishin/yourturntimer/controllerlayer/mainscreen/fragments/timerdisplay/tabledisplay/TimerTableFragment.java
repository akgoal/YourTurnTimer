package com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay.tabledisplay;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.modellayer.timermanager.TimeFormatter;
import com.deakishin.yourturntimer.controllerlayer.mainscreen.fragments.timerdisplay.TimerDisplayFragment;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerState;

import java.util.List;

/**
 * Created by Dmitry Akishin on 21.01.2017.
 * <p>
 * Extension of TimerDisplayFragment. Timers are displayed as a table with a switching button above it.
 */

public class TimerTableFragment extends TimerDisplayFragment {

    /* Widgets. */
    private ButtonWithBorderProgress mSwitchButton;
    private ListView mTimersListView;

    /* Adapter for timers list. */
    private TimerListAdapter mListAdapter;

    /* List of displayed timers. */
    private List<TimerManagerState.Timer> mTimers;
    /* Index of current timer. */
    private int mCurrIndex;
    /* Status of timer service. */
    private TimerManagerState.Status mServiceStatus;

    /**
     * Empty constructor as required in Fragment documentation.
     */
    public TimerTableFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new TimerListAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timer, parent, false);

        mTimersListView = (ListView) v.findViewById(R.id.timer_listView);
        mTimersListView.setAdapter(mListAdapter);

        mSwitchButton = (ButtonWithBorderProgress) v.findViewById(R.id.timer_switch_button);
        mSwitchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                notifyActivitySwitchPressed();
            }
        });

        return v;
    }

    @Override
    public void update(TimerManagerState timersState) {
        if (timersState == null) {
            return;
        }

        mTimers = timersState.getTimers();
        int oldIndex = mCurrIndex;
        mCurrIndex = timersState.getCurrentIndex();
        mServiceStatus = timersState.getStatus();

        update(oldIndex != mCurrIndex);
    }

    /* Updates widgets according to timers data.
     * indexChanged - current index has changed. */
    private void update(boolean indexChanged) {
        TimerManagerState.Timer currTimer = mTimers.get(mCurrIndex);
        mSwitchButton.setText(getActivity().getString(
                R.string.timer_button, mCurrIndex + 1, currTimer.getName(),
                TimeFormatter.format(currTimer.getCurrentTime())));
        mSwitchButton.setEnabled(mServiceStatus == TimerManagerState.Status.RUNNING);
        mSwitchButton.setProgress(calculateTimerProgress(currTimer),
                currTimer.getCurrentTime());
        mListAdapter.notifyDataSetChanged();
        if (indexChanged) {
            mTimersListView.smoothScrollToPosition(mCurrIndex);
        }
    }

    /* Calculates progress of a given timer as a
     float number between 0 and 1. */
    private float calculateTimerProgress(TimerManagerState.Timer timer) {
        if (timer == null) {
            return 0;
        }
        return timer.getCurrentTime() / (float) timer.getInitialTime();
    }

    /* Class for adapter of timers list. */
    private class TimerListAdapter extends BaseAdapter {

        TimerListAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return mTimers == null ? 0 : mTimers.size();
        }

        @Override
        public Object getItem(int position) {
            return mTimers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.fragment_timer_list_item, parent, false);
            }

            TimerManagerState.Timer timer = (TimerManagerState.Timer) getItem(position);

            View border = convertView.findViewById(R.id.timer_list_item_border);

            int textColorResId;
            if (mCurrIndex == position) {
                textColorResId = R.color.timer_list_item_text_color_selected;
                border.setVisibility(View.VISIBLE);
            } else {
                border.setVisibility(View.GONE);
                if (timer.isFinished()) {
                    textColorResId = R.color.timer_list_item_text_color_disabled;
                } else {
                    textColorResId = R.color.timer_list_item_text_color_normal;
                }
            }
            int textColor = ContextCompat.getColor(getActivity(), textColorResId);

            String name = getActivity().getString(R.string.timer_list_item_name_with_number, position + 1,
                    timer.getName());
            TextView nameTextView = (TextView) convertView.findViewById(R.id.timer_list_item_name);
            nameTextView.setText(name);
            nameTextView.setTextColor(textColor);

            TextView timeTextView = (TextView) convertView.findViewById(R.id.timer_list_item_time);
            timeTextView.setText(TimeFormatter.format(timer.getCurrentTime()));
            timeTextView.setTextColor(textColor);

            ProgressView progressView = (ProgressView) convertView.findViewById(R.id.timer_list_item_progressView);
            progressView.setProgress(calculateTimerProgress(timer));

            return convertView;
        }
    }
}
