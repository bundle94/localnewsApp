package com.example.localnewsapp.utils;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.localnewsapp.R;

public class MySettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}
