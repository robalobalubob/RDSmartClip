package com.example.rdsmartclipper;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

/**
 * SettingsActivity class
 * Handles preferences for the application
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Sets up preferences to be displayed
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the activity_settings layout
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_fragment, new SettingsFragment())
                    .commit();
        }
        // Set the toolbar as the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Handles the back button press
     *
     * @param item The menu item that was selected.
     * @return true if the action was handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button press
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * SettingsFragment class
     * Used to display the preferences
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }
}
