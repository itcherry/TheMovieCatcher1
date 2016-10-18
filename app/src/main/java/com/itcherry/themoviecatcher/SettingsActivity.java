package com.itcherry.themoviecatcher;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import java.util.Map;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sorting_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_notification)));

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if(value instanceof Map)
            value = ((Map) value).get(preference.getKey());

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue((String) value);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }else if(preference instanceof SwitchPreference){
            ((SwitchPreference) preference).setChecked((Boolean) value);

        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary((String)value);
        }
        return true;
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getAll());
    }


}



