package com.deakishin.yourturntimer.controllerlayer.editscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.controllerlayer.ConfirmationDialogFragment;
import com.deakishin.yourturntimer.modellayer.timermanager.TimeFormatter;
import com.deakishin.yourturntimer.modellayer.timermanager.InitialTimer;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManager;
import com.deakishin.yourturntimer.modellayer.timermanager.TimerManagerImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry Akishin on 25.01.2017.
 * <p>
 * Fragment for displaying and editing list of timers.
 * It works directly with the TimerManager instance.
 * Note! It is not checked if the manager is running or not.
 */

public class EditFragment extends Fragment {

    /* Identifiers for timer-editing dialogs. */
    private static final String DIALOG_TIME = "timer";
    private static final String DIALOG_NAME = "position_name";
    private static final String DIALOG_CONFIRM_DELETE = "confirmDelete";

    /* Request codes for getting results from child fragments. */
    private static final int REQUEST_TIME = 10;
    private static final int REQUEST_NAME = 11;
    private static final int REQUEST_CONFIRM_DELETE_SELECTED = 12;

    /* Timer manager to get and edit initial timers. */
    private TimerManager mTimerManager;

    /* List of displayed timers. */
    private List<InitialTimer> mTimers = new ArrayList<>();

    /* ListView for displaying list of timers. */
    private ListView mListView;

    /* Adapter for the listView that provides data for it. */
    private TimersListAdapter mListAdapter;

    /* List of positions of selected timers. */
    private List<Integer> mSelectedPositionsList = new ArrayList<>();

    /* Menu items. Items are visible in specific modes. */
    private MenuItem mOrderMenuItem, mAddMenuItem,
            mSelectAllMenuItem, mTimeMenuItem, mDeleteMenuItem,
            mShuffleMenuItem, mReverseOrderMenuItem;

    /* Flag for ordering mode in which order of timers is managed. */
    private boolean mOrderMode;

    /**
     * Empty constructor as required in Fragment documentation.
     */
    public EditFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mTimerManager = TimerManagerImpl.getInstance(getActivity());
        updateData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit, parent, false);

        mListView = (ListView) v.findViewById(R.id.edit_timers_listView);
        mListAdapter = new TimersListAdapter();
        mListView.setAdapter(mListAdapter);

        return v;
    }

    /* Updates displayed data from TimeManager. */
    private void updateData() {
        if (mTimerManager != null) {
            mTimers = mTimerManager.getInitialTimers();
        }
    }

    /* Updates data and views. */
    private void update() {
        updateData();
        mListAdapter.notifyDataSetChanged();

        if (mAddMenuItem != null) {
            mAddMenuItem.setEnabled(mTimerManager.canAddInitialTimer());
        }
    }

    /* Updates views according to current mode. */
    private void onModeChanged() {
        boolean selectionMode = !mSelectedPositionsList.isEmpty();
        if (mOrderMode) {
            mOrderMenuItem.setVisible(false);
            mAddMenuItem.setVisible(false);
            mTimeMenuItem.setVisible(false);
            mDeleteMenuItem.setVisible(false);
            mSelectAllMenuItem.setVisible(false);
            mReverseOrderMenuItem.setVisible(true);
            mShuffleMenuItem.setVisible(true);
        } else {
            mShuffleMenuItem.setVisible(false);
            mReverseOrderMenuItem.setVisible(false);
            mOrderMenuItem.setVisible(!selectionMode);
            mAddMenuItem.setVisible(!selectionMode);
            mTimeMenuItem.setVisible(selectionMode);
            mDeleteMenuItem.setVisible(selectionMode);
            mSelectAllMenuItem.setVisible(selectionMode);
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        int iconResId = mOrderMode ? R.drawable.ic_menu_ok
                : selectionMode ? R.drawable.ic_menu_close
                : 0;
        String title = mOrderMode ? "" : selectionMode ? ("" + mSelectedPositionsList.size()) :
                getActivity().getString(R.string.edit_screen_title);
        int colorResId = mOrderMode || selectionMode ?
                R.color.toolbar_accent :
                R.color.toolbar;
        actionBar.setHomeAsUpIndicator(iconResId);
        actionBar.setTitle(title);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), colorResId)));

        // Change status bar color if available.
        if (Build.VERSION.SDK_INT >= 21) {
            int statusBarColorResId = mOrderMode || selectionMode ?
                    R.color.statusbar_accent :
                    R.color.status_bar;
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(getActivity(), statusBarColorResId));
        }

        mListAdapter.notifyDataSetChanged();
    }

    /* Clears list of selected positions. */
    private void clearSelection() {
        mSelectedPositionsList.clear();
        onModeChanged();
    }

    /* Shows dialog for editing time for the timer in given position.
     * if (selectedPosition) then shows dialog for editing timers
     * in selected positions. */
    private void showTimePickerDialog(int position, boolean selectedPosition) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.findFragmentByTag(DIALOG_TIME) != null) {
            return;
        }

        TimePickerDialogFragment dialog;
        if (selectedPosition) {
            if (mSelectedPositionsList.isEmpty()) {
                return;
            } else {
                if (mSelectedPositionsList.size() == 1) {
                    int pos = mSelectedPositionsList.get(0);
                    if (pos < mTimers.size()) {
                        dialog = TimePickerDialogFragment.getInstance(pos, mTimers.get(pos));
                    } else {
                        return;
                    }
                } else {
                    dialog = TimePickerDialogFragment.getInstance(mSelectedPositionsList,
                            mTimers.get(mSelectedPositionsList.get(0)).getTime());
                }
            }
        } else {
            dialog = TimePickerDialogFragment.getInstance(position, mTimers.get(position));
        }
        dialog.setTargetFragment(EditFragment.this, REQUEST_TIME);
        dialog.show(fm, DIALOG_TIME);
    }

    /* Show dialog for editing time of selected timers. */
    private void showTimePickerDialogForSelectedPosition() {
        showTimePickerDialog(0, true);
    }

    /* Shows dialog for editing name for the timer in given position. */
    private void showNameEditDialog(int position) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.findFragmentByTag(DIALOG_NAME) != null) {
            return;
        }
        NameEditDialogFragment dialog = NameEditDialogFragment
                .getInstance(position, mTimers.get(position));
        dialog.setTargetFragment(EditFragment.this, REQUEST_NAME);
        dialog.show(fm, DIALOG_NAME);
    }

    /* Moves timer in given position up or down in list of timers depending on boolean argument. */
    private void moveTimer(int position, boolean up) {
        int newPosition = up ? position - 1 : position + 1;
        if (position >= 0 && position < mTimers.size() && newPosition >= 0 &&
                newPosition < mTimers.size()) {
            mTimerManager.moveInitialTimer(position, newPosition);
            update();
            scrollTo(newPosition);
        }
    }

    /* Shows dialog to confirm deleting selected positions. */
    private void showDeleteDialog() {
        if (!mSelectedPositionsList.isEmpty()) {
            FragmentManager fm = getActivity().getSupportFragmentManager();

            ConfirmationDialogFragment dialog = ConfirmationDialogFragment
                    .getInstance(getActivity().getString(R.string.confirm_delete_selected_message),
                            getActivity().getString(R.string.confirm_delete_selected_yes),
                            REQUEST_CONFIRM_DELETE_SELECTED, false);
            dialog.setTargetFragment(EditFragment.this, REQUEST_CONFIRM_DELETE_SELECTED);
            dialog.show(fm, DIALOG_CONFIRM_DELETE);
        }
    }

    /* Scrolls listView to desired position. */
    private void scrollTo(int position) {
        mListView.smoothScrollToPosition(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TIME) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            long time = data.getLongExtra(TimePickerDialogFragment.EXTRA_TIME, -1);
            boolean multiple = data.getBooleanExtra(TimePickerDialogFragment.EXTRA_MULTIPLE, false);
            if (multiple) {
                clearSelection();
                List<Integer> posList = TimePickerDialogFragment.parsePositions(
                        data.getStringExtra(TimePickerDialogFragment.EXTRA_POSITION));
                if (posList != null) {
                    for (Integer pos : posList) {
                        onTimePicked(pos, time);
                    }
                    update();
                }
            } else {
                int pos = data.getIntExtra(TimePickerDialogFragment.EXTRA_POSITION, -1);
                onTimePicked(pos, time);
                update();
            }
            return;
        }
        if (requestCode == REQUEST_NAME) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            int pos = data.getIntExtra(NameEditDialogFragment.EXTRA_POSITION, -1);
            String name = data.getStringExtra(NameEditDialogFragment.EXTRA_NAME);

            if (pos >= 0 && pos < mTimers.size() && name != null) {
                InitialTimer timer = mTimers.get(pos);
                timer.setName(name);
                mTimerManager.editInitialTimer(pos, timer);
                update();
            }
        }
        if (requestCode == REQUEST_CONFIRM_DELETE_SELECTED) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            mTimerManager.removeInitialTimers(mSelectedPositionsList);
            update();
            clearSelection();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Reacts to time being picked for the timer in the given position.
     * Sends request to TimerManager but does not update views. */
    private void onTimePicked(int position, long time) {
        if (position >= 0 && position < mTimers.size() && time > 0) {
            InitialTimer timer = mTimers.get(position);
            timer.setTime(time);
            mTimerManager.editInitialTimer(position, timer);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_edit, menu);

        mAddMenuItem = menu.findItem(R.id.edit_menu_item_add);
        mOrderMenuItem = menu.findItem(R.id.edit_menu_item_order);

        mTimeMenuItem = menu.findItem(R.id.edit_menu_item_time);
        mDeleteMenuItem = menu.findItem(R.id.edit_menu_item_delete);
        mSelectAllMenuItem = menu.findItem(R.id.edit_menu_item_select_all);

        mReverseOrderMenuItem = menu.findItem(R.id.edit_menu_item_order_reverse);
        mShuffleMenuItem = menu.findItem(R.id.edit_menu_item_shuffle);

        onModeChanged();
    }

    /**
     * Processes selection of home button.
     *
     * @return false if it fragment is not interested
     * in processing it.
     */
    public boolean onHomeMenuItemSelected() {
        if (mSelectedPositionsList.isEmpty() && !mOrderMode) {
            return false;
        }
        mOrderMode = false;
        clearSelection();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu_item_order:
                mOrderMode = true;
                onModeChanged();
                return true;
            case R.id.edit_menu_item_add:
                if (mTimerManager.canAddInitialTimer()) {
                    long time;
                    int num;
                    if (mTimers == null || mTimers.isEmpty()) {
                        num = 1;
                        time = 30000;
                    } else {
                        num = mTimers.size() + 1;
                        time = mTimers.get(mTimers.size() - 1).getTime();
                    }
                    InitialTimer timer = new InitialTimer(
                            getActivity().getString(R.string.default_timer_name, num), time);
                    mTimerManager.addInitialTimer(timer);

                    update();
                    scrollTo(num);
                }
                return true;
            case R.id.edit_menu_item_time:
                showTimePickerDialogForSelectedPosition();
                return true;
            case R.id.edit_menu_item_delete:
                showDeleteDialog();
                return true;
            case R.id.edit_menu_item_select_all:
                for (int pos = 0; pos < mTimers.size(); pos++) {
                    if (!mSelectedPositionsList.contains(pos)) {
                        mSelectedPositionsList.add(pos);
                    }
                }
                onModeChanged();
                return true;
            case R.id.edit_menu_item_order_reverse:
                mTimerManager.reverseInitialTimersOrder();
                update();
                return true;
            case R.id.edit_menu_item_shuffle:
                mTimerManager.shuffleInitialTimers();
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Adapter for displaying timers list. */
    private class TimersListAdapter extends BaseAdapter {

        public TimersListAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return mTimers.size();
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
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.fragment_edit_list_item, parent, false);
            }

            InitialTimer timer = (InitialTimer) getItem(position);

            TextView numberTextView = (TextView) convertView.findViewById(R.id.edit_list_item_number_textView);
            numberTextView.setText(getActivity().getString(R.string.edit_list_item_number, position + 1));

            TextView nameTextView = (TextView) convertView.findViewById(R.id.edit_list_item_name_textView);
            nameTextView.setText(timer.getName());

            TextView timeTextView = (TextView) convertView.findViewById(R.id.edit_list_item_time_textView);
            timeTextView.setText(TimeFormatter.format(timer.getTime(), false));

            View timePanel = convertView.findViewById(R.id.edit_list_item_time_panel);
            timePanel.setOnClickListener(new OnItemClickListener(position, OnItemClickListener.TIME_LISTENER));

            View posNamePanel = convertView.findViewById(R.id.edit_list_item_position_name_panel);
            posNamePanel.setOnClickListener(new OnItemClickListener(position, OnItemClickListener.NAME_LISTENER));

            AppCompatCheckBox checkBox = (AppCompatCheckBox) convertView.findViewById(R.id.edit_list_item_checkBox);
            if (mSelectedPositionsList.contains(position)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            checkBox.setOnClickListener(new OnItemClickListener(position, checkBox));
            checkBox.setVisibility(mOrderMode ? View.GONE : View.VISIBLE);
            setCheckBoxColor(checkBox,
                    mSelectedPositionsList.isEmpty() ? R.color.edit_timer_checkbox_color_unchecked_normal :
                            R.color.edit_timer_checkbox_color_unchecked_accent,
                    R.color.edit_timer_checkbox_color_checked);
            checkBox.setBackgroundResource(mSelectedPositionsList.isEmpty() ?
                    R.drawable.edit_timer_button_inactive_background :
                    R.drawable.edit_timer_button_background);

            ImageButton moveUpImageButton = (ImageButton) convertView.findViewById(
                    R.id.edit_list_item_move_up_imageButton);
            ImageButton moveDownImageButton = (ImageButton) convertView.findViewById(
                    R.id.edit_list_item_move_down_imageButton);
            moveUpImageButton.setVisibility(mOrderMode ?
                    (position > 0 ? View.VISIBLE : View.INVISIBLE) : View.GONE);
            moveDownImageButton.setVisibility(mOrderMode ?
                    (position < getCount() - 1 ? View.VISIBLE : View.INVISIBLE) : View.GONE);
            moveUpImageButton.setOnClickListener(new OnItemClickListener(position, OnItemClickListener.MOVE_UP));
            moveDownImageButton.setOnClickListener(new OnItemClickListener(position, OnItemClickListener.MOVE_DOWN));

            return convertView;
        }

        /* Changes colors of checkbox.
         * Params: checkbox and color resources ids. */
        private void setCheckBoxColor(AppCompatCheckBox checkBox, int uncheckedColorResId, int checkedColorResId) {
            int uncheckedColor = ContextCompat.getColor(getActivity(), uncheckedColorResId);
            int checkedColor = ContextCompat.getColor(getActivity(), checkedColorResId);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, // unchecked color
                            new int[]{android.R.attr.state_checked}  // checked color
                    },
                    new int[]{
                            uncheckedColor,
                            checkedColor
                    }
            );
            checkBox.setSupportButtonTintList(colorStateList);
        }

        /**
         * Listener to clicks on list item.
         */
        private class OnItemClickListener implements View.OnClickListener {

            /**
             * Type of listener that reacts to time being clicked on.
             */
            static final int TIME_LISTENER = 0;
            /**
             * Type of listener that reacts to name being clicked on.
             */
            static final int NAME_LISTENER = 1;

            /* Type of listener that reacts to checkBox being clicked on. */
            private static final int CHECK_LISTENER = 2;

            /**
             * Type of listener that reacts to button to move up being clicked on.
             */
            static final int MOVE_UP = 3;

            /**
             * Type of listener that reacts to button to move down being clicked on.
             */
            static final int MOVE_DOWN = 4;

            private int mPosition;
            private int mType;
            private CheckBox mCheckBox;

            /**
             * Constructs specific type of listener to clicks on item.
             *
             * @param position position of listened item.
             * @type type code of listener.
             */
            OnItemClickListener(int position, int type) {
                mPosition = position;
                mType = type;
            }

            /**
             * Constructs specific type of listener to clicks on item.
             * Listens to clicks on checkBox.
             *
             * @param position position of listened item.
             * @param checkBox item's checkBox.
             */
            OnItemClickListener(int position, CheckBox checkBox) {
                this(position, CHECK_LISTENER);
                mCheckBox = checkBox;
            }

            @Override
            public void onClick(View v) {
                switch (mType) {
                    case TIME_LISTENER:
                        showTimePickerDialog(mPosition, false);
                        break;
                    case NAME_LISTENER:
                        showNameEditDialog(mPosition);
                        break;
                    case CHECK_LISTENER:
                        if (mCheckBox != null) {
                            if (mCheckBox.isChecked()) {
                                mSelectedPositionsList.add(mPosition);
                            } else {
                                for (int i = 0; i < mSelectedPositionsList.size(); i++) {
                                    if (mSelectedPositionsList.get(i).equals(mPosition)) {
                                        mSelectedPositionsList.remove(i);
                                        break;
                                    }
                                }
                            }
                            onModeChanged();
                        }
                        break;
                    case MOVE_UP:
                        moveTimer(mPosition, true);
                        break;
                    case MOVE_DOWN:
                        moveTimer(mPosition, false);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
