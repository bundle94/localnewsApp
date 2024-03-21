package com.example.localnewsapp.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.localnewsapp.MainActivity;
import com.example.localnewsapp.R;
import com.example.localnewsapp.utils.Helper;
import com.example.localnewsapp.utils.MySettingsFragment;


public class SettingsFragment extends PreferenceFragmentCompat {

    Helper helper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        getActivity().setTitle("Settings");
        helper = new Helper(getActivity());

        // Find the logout preference
        Preference logoutPreference = findPreference("logout");
        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                // Perform logout operation here
                helper.displayClosingAlertBox(true);
                return true;
            });
        }

        SwitchPreferenceCompat nightModePref = findPreference("night_mode");
        if (nightModePref != null) {
            nightModePref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isNightModeOn = (boolean) newValue;
                if (isNightModeOn) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save a flag or identifier for your SettingsFragment
        Log.d("SETTINGS FRAGMENT", "I am in the settings fragment");
        outState.putBoolean("isSettingsFragmentShown", true);
    }
}