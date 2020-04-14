package com.cxem_car;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ActivityAbout extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		TextView tv = findViewById(R.id.textView_about);
		tv.setText(Html.fromHtml(getString(R.string.text_about)));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		
	}

}
