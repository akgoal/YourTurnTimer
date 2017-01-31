package com.deakishin.yourturntimer.controllerlayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.deakishin.yourturntimer.R;

/**
 * Created by Dmitry Akishin on 27.01.2017.
 * <p>
 * Dialog Fragment for confirmation dialogs.
 */
public class ConfirmationDialogFragment extends CustomDialogFragment {

    /* Keys for fragment arguments. */
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_POSITIVE_BUTTON_TEXT = "positiveBtnText";
    private static final String EXTRA_REQUEST_CODE = "reqCode";
    private static final String EXTRA_HOST_IS_ACTIVITY = "hostIsActivity";

    /* Message to display. */
    private String mMessage;
    /* Request code. */
    private int mReqCode;
    /* Text displayed on positive button. */
    private String mPositiveButtonText;

    /* True if fragment was showed by activity,
     * false if host if another fragment. */
    private boolean mHostIsActivity = true;

    /* Result is sent to whoever requested it. */
    private boolean mResultSent = false;

    /**
     * Interface fro listener to confirmation result.
     * Every activity that want to show dialog must implement it.
     */
    public interface ConfirmationListener {
        /**
         * Callback for confirmation result.
         *
         * @param requestCode request code that was used to start dialog.
         * @param resultCode  Activity.RESULT_OK if confirmed, otherwise Activity.RESULT_CANCELED
         */
        void onConfirmed(int requestCode, int resultCode);
    }

    /**
     * Configures and returns instance of ConfirmationDialogFragment.
     *
     * @param message         message to display.
     * @param positiveBtnText text that shoul be display on the positive button.
     * @param requestCode     request code that will be used to send result back.
     * @param hostIsActivity  is dialog showed by activity.
     * @return configured dialog fragment.
     */
    public static ConfirmationDialogFragment getInstance(String message, String positiveBtnText, int requestCode,
                                                         boolean hostIsActivity) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_MESSAGE, message);
        args.putString(EXTRA_POSITIVE_BUTTON_TEXT, positiveBtnText);
        args.putInt(EXTRA_REQUEST_CODE, requestCode);
        args.putBoolean(EXTRA_HOST_IS_ACTIVITY, hostIsActivity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_message, null);

        Bundle args = getArguments();
        if (args.containsKey(EXTRA_MESSAGE))
            mMessage = args.getString(EXTRA_MESSAGE);
        if (args.containsKey(EXTRA_POSITIVE_BUTTON_TEXT))
            mPositiveButtonText = args.getString(EXTRA_POSITIVE_BUTTON_TEXT);
        if (mPositiveButtonText == null || mPositiveButtonText.equals(""))
            mPositiveButtonText = getActivity().getString(R.string.ok);
        if (args.containsKey(EXTRA_REQUEST_CODE))
            mReqCode = args.getInt(EXTRA_REQUEST_CODE);
        if (args.containsKey(EXTRA_HOST_IS_ACTIVITY))
            mHostIsActivity = args.getBoolean(EXTRA_HOST_IS_ACTIVITY);

        TextView messageTextView = (TextView) v.findViewById(R.id.message_textView);
        if (mMessage != null)
            messageTextView.setText(mMessage);

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sendResult(Activity.RESULT_OK);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!mResultSent)
            sendResult(Activity.RESULT_CANCELED);
        super.onDismiss(dialog);
    }

    /* Отправка результата родителю. */
    private void sendResult(int resultCode) {
        mResultSent = true;
        if (mHostIsActivity) {
            // Check if activity implement callback interface.
            if (getActivity() instanceof ConfirmationListener) {
                ((ConfirmationListener) getActivity()).onConfirmed(mReqCode, resultCode);
            }
        } else {
            if (getTargetFragment() == null)
                return;

            getTargetFragment().onActivityResult(mReqCode, resultCode, null);
        }
    }
}
