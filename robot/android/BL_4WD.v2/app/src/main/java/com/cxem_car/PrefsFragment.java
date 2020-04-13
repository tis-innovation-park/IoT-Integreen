package com.cxem_car;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class PrefsFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref);
	}
}