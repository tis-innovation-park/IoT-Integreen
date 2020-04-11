package com.cxem_car;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	Button btnActAccelerometer, btnActButtons, btnActServo, btnActTouch, btnActAbout;
	private BluetoothAdapter mBluetoothAdapter;

	private static final int REQUEST_ENABLE_BT = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    TextView textv = (TextView) findViewById(R.id.textView_appName);
	    textv.setShadowLayer(1, 3, 3, Color.GRAY);
	
	    btnActAccelerometer = (Button) findViewById(R.id.button_accel);
	    btnActAccelerometer.setOnClickListener(this);
	    
	    btnActButtons = (Button) findViewById(R.id.button_buttons);
	    btnActButtons.setOnClickListener(this);
	    
	    btnActServo = (Button) findViewById(R.id.button_servo);
	    btnActServo.setOnClickListener(this);
	    
	    btnActTouch = (Button) findViewById(R.id.button_touch);
	    btnActTouch.setOnClickListener(this);
	    
	    btnActAbout = (Button) findViewById(R.id.button_about);
	    btnActAbout.setOnClickListener(this);

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
		// fire an intent to display a dialog asking the user to grant permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
	    case R.id.button_accel:
	    	Intent intent_accel = new Intent(this, ActivityAccelerometer.class);
	    	startActivity(intent_accel);
	    	break;
	    case R.id.button_buttons:
	    	Intent intent_buttons = new Intent(this, ActivityButtons.class);
	    	startActivity(intent_buttons);
	    	break;  
	    case R.id.button_touch:
	    	Intent intent_touch = new Intent(this, ActivityTouch.class);
	    	startActivity(intent_touch);
	    	break;
	    case R.id.button_servo:
	    	Intent intent_mcu = new Intent(this, ActivityServo.class);
	    	startActivity(intent_mcu);
	    	break;
	    case R.id.button_about:
	    	Intent intent_about = new Intent(this, ActivityAbout.class);
	    	startActivity(intent_about);
	    	break; 	
	    default:
	    	break;
	    }
	}
  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  
		Intent intent = new Intent();
		if (item.getItemId() == R.id.menu_settings) {
			intent.setClass(MainActivity.this, SetPreferenceActivity.class);
		} else if (item.getItemId() == R.id.menu_mcu) {
			intent.setClass(MainActivity.this, ActivityMCU.class);
		}
		startActivityForResult(intent, 0); 
	  
		return true;
	}
}
