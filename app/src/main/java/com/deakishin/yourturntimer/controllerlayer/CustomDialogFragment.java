package com.deakishin.yourturntimer.controllerlayer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import com.deakishin.yourturntimer.R;

/**
 * Custom DialogFragment with adjusted button attributes.
 * All DialogFragment should extend this class for design consistency.
 * Corresponding attributes must be set in resources.
 */
public class CustomDialogFragment extends DialogFragment {
    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            setButtonTextStyle(positiveButton);
        }
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            setButtonTextStyle(negativeButton);
        }
    }

    /* Sets button attributes. */
    private void setButtonTextStyle(Button button) {
        int colorResId = R.color.dialog_btn_text;
        button.setTextColor(ContextCompat.getColor(getActivity(), colorResId));
        button.setTextSize(getActivity().getResources().getInteger(R.integer.dialog_button_text_size));
        button.setBackgroundResource(R.drawable.dialog_button_background);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
    }

    /* Fix bug with dismissing dialog when rotating. */
    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
