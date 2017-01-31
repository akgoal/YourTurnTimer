package com.deakishin.yourturntimer.controllerlayer.editscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.controllerlayer.CustomDialogFragment;
import com.deakishin.yourturntimer.modellayer.timermanager.InitialTimer;
import com.deakishin.yourturntimer.modellayer.timermanager.TimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry Akishin on 26.01.2017.
 * <p>
 * Dialog Fragment for picking time in format hh:mm:ss.
 */

public class TimePickerDialogFragment extends CustomDialogFragment {

    /* Static keys for returned values in an Intent object.
     * They are used to store initial arguments, too. */
    /**
     * Key for timer's new time in returned result.
     */
    public static final String EXTRA_TIME = "time";
    /**
     * Key for timer's position (or positions) in returned result.
     */
    public static final String EXTRA_POSITION = "position";

    /* Keys for name and position of edited timer
     * to save in arguments. */
    private static final String KEY_NAME = "name";

    /**
     * Key for boolean flag in returned result. Value for this key
     * determines whether picker was shown for multiple timers or not.
     */
    public static final String EXTRA_MULTIPLE = "multiple";

    /*
     * Delimiter between positions in a String representation of an array of them.
     */
    private static final String POSITIONS_DELIMITER = ";";

    /**
     * Returns instance that is set with timer's initial time.
     *
     * @param timer    timer to change time of.
     * @param position position of timer starting from 0.
     * @return configured instance of TimePickerFragment.
     */
    public static TimePickerDialogFragment getInstance(int position, InitialTimer timer) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();

        // Save parameters to fragment arguments.
        if (timer != null) {
            Bundle args = new Bundle();
            args.putLong(EXTRA_TIME, timer.getTime());
            args.putInt(EXTRA_POSITION, position);
            args.putString(KEY_NAME, timer.getName());
            args.putBoolean(EXTRA_MULTIPLE, false);
            fragment.setArguments(args);
        }

        return fragment;
    }

    /**
     * Returns instance that is set with initial time.
     * Should be used when modifying multiple timers.
     *
     * @param positions list of the positions of the timers
     *                  that are being changed; these positions
     *                  are returned when time is picked.
     * @param time      initial time that should be set to the picker.
     * @return configured instance of TimePickerDialogFragment.
     */
    public static TimePickerDialogFragment getInstance(List<Integer> positions, long time) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();

        Bundle args = new Bundle();
        args.putLong(EXTRA_TIME, time);
        args.putString(EXTRA_POSITION, positionsToString(positions));
        args.putBoolean(EXTRA_MULTIPLE, true);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Converts String to list of positions the same way
     * as they are converted to String in returned result.
     *
     * @param line String containing positions.
     * @return list of decoded position.
     */
    public static List<Integer> parsePositions(String line) {
        if (line == null) {
            return null;
        }

        String[] posStrArr = line.split(POSITIONS_DELIMITER);
        List<Integer> res = new ArrayList<>();
        for (String posStr : posStrArr) {
            res.add(Integer.parseInt(posStr));
        }
        return res;
    }

    /* Converts list of positions to String. */
    public static String positionsToString(List<Integer> positions) {
        StringBuilder sb = new StringBuilder();
        if (positions != null) {
            for (int pos : positions) {
                sb.append(pos).append(POSITIONS_DELIMITER);
            }
        }
        return sb.toString();
    }

    /**
     * Empty constructor as required in Fragment docs.
     */
    public TimePickerDialogFragment() {
        super();
    }

    /* Displayed time values. */
    private int mHours, mMinutes, mSeconds;

    /* Position of timer that is being changed. */
    private int mPosition = -1;
    /* List of position of timers that are being changed. */
    private List<Integer> mPositionsList;
    /* If multiple timers are being changed. */
    private boolean mMultiple;

    /* Widgets. */
    private NumberPicker mHoursPicker, mMinutesPicker, mSecondsPicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

        long time = 0;

        String title = null;

        // Get time params from arguments.
        Bundle args = getArguments();
        if (args != null) {
            time = args.getLong(EXTRA_TIME, 0);

            mMultiple = args.getBoolean(EXTRA_MULTIPLE, false);
            if (mMultiple) {
                mPositionsList = parsePositions(args.getString(EXTRA_POSITION));
                if (mPositionsList != null) {
                    title = getActivity().getString(R.string.time_picker_dialog_title_multiple, mPositionsList.size());
                }
            } else {
                mPosition = args.getInt(EXTRA_POSITION, -1);
                String name = args.getString(KEY_NAME);
                if (name != null) {
                    title = getActivity().getString(R.string.time_picker_dialog_title_single, mPosition + 1, name);
                }
            }
        }
        if (title == null) {
            title = "Timer not found.";
        }

        TextView titleTextView = (TextView) v.findViewById(R.id.dialog_time_picker_title_textView);
        titleTextView.setText(title);

        long[] timeValues = TimeFormatter.getTimeValues(time);
        mHours = (int) timeValues[0];
        mMinutes = (int) timeValues[1];
        mSeconds = (int) timeValues[2];

        mHoursPicker = (NumberPicker) v.findViewById(R.id.dialog_time_picker_hours_numberPicker);
        constructRevertPicker(mHoursPicker, 0, InitialTimer.MAX_HOURS, false);
        setPickedNumber(mHoursPicker, mHours <= InitialTimer.MAX_HOURS ? mHours : InitialTimer.MAX_HOURS);
        mHoursPicker.setWrapSelectorWheel(false);

        mMinutesPicker = (NumberPicker) v.findViewById(R.id.dialog_time_picker_minutes_numberPicker);
        constructRevertPicker(mMinutesPicker, 0, 59, true);
        setPickedNumber(mMinutesPicker, mMinutes);

        mSecondsPicker = (NumberPicker) v.findViewById(R.id.dialog_time_picker_seconds_numberPicker);
        constructRevertPicker(mSecondsPicker, 0, 59, true);
        setPickedNumber(mSecondsPicker, mSeconds);

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHours = getPickedNumber(mHoursPicker);
                        mMinutes = getPickedNumber(mMinutesPicker);
                        mSeconds = getPickedNumber(mSecondsPicker);

                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(R.string.cancel, null).create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    /* Constructs displayed values for picker
     * in descending order and sets them to the picker.
     * if twoDigits then each number is represented as two-digit number. */
    private void constructRevertPicker(NumberPicker picker, final int minValue,
                                       final int maxValue, final boolean twoDigits) {
        if (maxValue < minValue)
            return;

        picker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                int num = minValue + maxValue - value;
                return (twoDigits ? String.format("%02d", num) : Integer.toString(num));
            }
        });
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);

        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(picker);
    }

    /* Sets custom color for number picker divider.
     * Uses Reflection. */
    private void setDividerColor(NumberPicker picker) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(
                            ContextCompat.getColor(getActivity(), R.color.number_picker_divider));
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | IllegalAccessException | Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /* Returns selected number from number picker that has its
     * values in descending order. */
    private int getPickedNumber(NumberPicker picker) {
        return picker.getMaxValue() - picker.getValue();
    }

    /* Sets selected number in number picker that has its
     * values in descending order. */
    private void setPickedNumber(NumberPicker picker, int number) {
        picker.setValue(picker.getMaxValue() - number);
    }

    /* Sends result to parent fragment. */
    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_MULTIPLE, mMultiple);
        if (mMultiple) {
            i.putExtra(EXTRA_POSITION, positionsToString(mPositionsList));
        } else {
            i.putExtra(EXTRA_POSITION, mPosition);
        }
        long time = TimeFormatter.getTimeMilliSec(mHours, mMinutes, mSeconds);
        i.putExtra(EXTRA_TIME, time);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
        this.dismiss();
    }
}
