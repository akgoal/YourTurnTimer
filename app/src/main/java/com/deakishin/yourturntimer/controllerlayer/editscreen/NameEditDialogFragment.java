package com.deakishin.yourturntimer.controllerlayer.editscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.controllerlayer.CustomDialogFragment;
import com.deakishin.yourturntimer.modellayer.timermanager.InitialTimer;

/**
 * Created by Dmitry Akishin on 26.01.2017.
 * <p>
 * Dialog fragment for editing timer's name.
 */

public class NameEditDialogFragment extends CustomDialogFragment {

    /* Static keys for returned values in an Intent object.
     * They are used to store initial arguments, too. */

    /**
     * Key for timer's position in returned result.
     */
    public static final String EXTRA_POSITION = "position";
    /**
     * Key for new time in returned result.
     */
    public static final String EXTRA_NAME = "name";

    /**
     * Returns instance that is set with info of the timer
     * that is being edited.
     *
     * @param timer    timer to change time of.
     * @param position position of timer starting from 0.
     * @return configured instance of NameEditDialogFragment.
     */
    public static NameEditDialogFragment getInstance(int position, InitialTimer timer) {
        NameEditDialogFragment fragment = new NameEditDialogFragment();

        // Save parameters to fragment arguments.
        if (timer != null) {
            Bundle args = new Bundle();
            args.putString(EXTRA_NAME, timer.getName());
            args.putInt(EXTRA_POSITION, position);
            fragment.setArguments(args);
        }

        return fragment;
    }

    /**
     * Empty constructor as required in Fragment docs.
     */
    public NameEditDialogFragment() {
        super();
    }

    /* Maximum length of an entered name. */
    private static final int MAX_LEN = 20;

    /* Position and name of timer that is being changed. */
    private int mPosition = -1;
    private String mName;

    /* Default name in case entered name is an empty String. */
    private String mDefaultName;

    /* Widgets. */
    private EditText mNameEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_name, null);

        // Get time params from arguments.
        Bundle args = getArguments();
        if (args != null) {
            mPosition = args.getInt(EXTRA_POSITION, -1);
            mName = args.getString(EXTRA_NAME);
        }

        mDefaultName = getActivity().getString(R.string.default_timer_name, mPosition + 1);

        mNameEditText = (EditText) v.findViewById(R.id.dialog_name_editText);
        mNameEditText.setText(mName);
        mNameEditText.setHint(mDefaultName);
        mNameEditText.setSelection(0, mName == null ? 0 : mName.length());
        mNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mName = mNameEditText.getText().toString();
                    sendResult(Activity.RESULT_OK);
                    return true;
                }
                return false;
            }
        });
        mNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN)});

        TextView numberTextView = (TextView) v.findViewById(R.id.dialog_name_number_textView);
        numberTextView.setText(getActivity().getString(R.string.edit_list_item_number, mPosition + 1));

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mName = mNameEditText.getText().toString();
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(R.string.cancel, null).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    /* Sends result to parent fragment. */
    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        if (mName.equals("")) {
            mName = mDefaultName;
        }
        Intent i = new Intent();
        i.putExtra(EXTRA_NAME, mName);
        i.putExtra(EXTRA_POSITION, mPosition);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
        this.dismiss();
    }
}
