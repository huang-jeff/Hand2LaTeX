package me.ofmc.capstone.capstone_s18;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by brandon on 3/5/18.
 */

public class SettingsFragment  extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Settings");
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
    }
}
