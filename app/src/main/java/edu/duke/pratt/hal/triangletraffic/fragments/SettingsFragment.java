package edu.duke.pratt.hal.triangletraffic.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

import edu.duke.pratt.hal.triangletraffic.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);

        // Load initial summary values.
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> keys = prefs.getAll().keySet();
        for ( String key : keys ) {
            updatePreferenceSummary(key);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceSummary(key);
    }

    private void updatePreferenceSummary(String key) {
        Preference preference = findPreference(key);

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            String value = prefs.getString(key, "");
            int index = listPreference.findIndexOfValue(value);
            preference.setSummary(listPreference.getEntries()[index]);
        }
    }
}
