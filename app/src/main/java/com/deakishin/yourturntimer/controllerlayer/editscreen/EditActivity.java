package com.deakishin.yourturntimer.controllerlayer.editscreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.deakishin.yourturntimer.R;
import com.deakishin.yourturntimer.controllerlayer.SingleFragmentActivity;

/**
 * Created by Dmitry Akishin on 25.01.2017.
 * <p>
 * Activity for displaying and editing list of initial timers.
 * All it does is being a host to its only fragment.
 */

public class EditActivity extends AppCompatActivity {

    /* Hosted fragment. */
    private EditFragment mEditFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        mEditFragment = (EditFragment) fm.findFragmentById(R.id.fragmentContainer);
        if (mEditFragment == null) {
            mEditFragment = new EditFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, mEditFragment).commit();
        }

        if (NavUtils.getParentActivityName(this) != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Ask fragment if it wants to process event.
                // If not then return to parent activity.
                if (!mEditFragment.onHomeMenuItemSelected()) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
