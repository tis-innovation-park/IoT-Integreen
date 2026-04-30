package com.cxem_car;

import android.app.Activity;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class PrefsFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		// Load the preferences from an XML resource
		setPreferencesFromResource(R.xml.pref, s);
	}
}