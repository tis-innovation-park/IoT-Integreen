package com.cxem_car;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SetPreferenceActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
	}
}