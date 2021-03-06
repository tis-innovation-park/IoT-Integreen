package com.cxem_car;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

class cBluetooth
{
	final static String TAG = "BL_4WD";

	private final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	private final static UUID UUID_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

	private final Activity mContext;
	private final String mAddress;
	private final Handler mHandler;
	private BluetoothLeService mBtLeService;
	private BluetoothGattCharacteristic mBtCharacteristic;

	private final static int BL_CONNECTION_PROBLEM = 1;
	final static int BL_RECEIVE_MESSAGE = 2;

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBtLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBtLeService.initialize()) {
				Log.d(TAG, "Bluetooth is not available.");
				Toast.makeText(mContext.getBaseContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
				mContext.finish();
				return;
			}
			// pass on to connect
			connect();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBtLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		StringBuilder recvData = new StringBuilder();

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.i(TAG, "Connected");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.i(TAG, "Disconnected");
				mBtCharacteristic = null;
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				Log.i(TAG, "Services Discovered");
				// Determine characteristic and enable notifications on it
				mBtCharacteristic = mBtLeService.getCharacteristic(UUID_SERVICE, UUID_CHARACTERISTIC);
				if (mBtCharacteristic == null) {
					Log.d(TAG, "In onReceive() and service characteristic not found");
					mHandler.sendEmptyMessage(BL_CONNECTION_PROBLEM);
					return;
				}
				mBtLeService.setCharacteristicNotification(mBtCharacteristic, true);
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				// Read from the InputStream. Since buffering differs on
				// Android and Arduino side we need to keep up our reading
				// until we receive the end-of-line markers (\r\n).
				recvData.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				if (recvData.length() > 0 && recvData.charAt(recvData.length()-1) == '\n') {
					Log.i(TAG, "Receive data: " + recvData);
					mHandler.obtainMessage(BL_RECEIVE_MESSAGE, -1, -1, recvData).sendToTarget();
					recvData = new StringBuilder(); // re-start from scratch
				}
			}
		}
	};

	static class DefaultHandlerCallback<T extends Activity> implements Handler.Callback {
		final WeakReference<T> obj;

		public DefaultHandlerCallback(T obj) {
			this.obj = new WeakReference<>(obj);
		}

		public boolean handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case cBluetooth.BL_CONNECTION_PROBLEM:
					Toast.makeText(obj.get().getBaseContext(), R.string.ble_connection_problem, Toast.LENGTH_SHORT).show();
					break;
			}
			return true;
		}
	}

	cBluetooth(Activity context, String address, Handler.Callback handlerCallback) {
		mContext = context;
		mAddress = address;
		mHandler = new Handler(handlerCallback);

		if(!BluetoothAdapter.checkBluetoothAddress(mAddress)){
			Log.d(TAG, "Incorrect MAC address");
			Toast.makeText(mContext.getBaseContext(), R.string.ble_incorrect_address, Toast.LENGTH_SHORT).show();
			mContext.finish();
			return;
		}

		Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
		mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	void connect() {
		Log.d(TAG, "...On Resume...");

		if (mBtLeService == null)
			return;

		mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		if (!mBtLeService.connect(mAddress)) {
			Log.d(TAG, "In onResume() and connect() failed");
			mHandler.sendEmptyMessage(BL_CONNECTION_PROBLEM);
		}
	}

	void close() {
		Log.d(TAG, "...On Destroy...");

		if (mBtLeService == null)
			return;

		mContext.unregisterReceiver(mGattUpdateReceiver);

		mContext.unbindService(mServiceConnection);
		mBtLeService = null;
	}

	void sendData(String message) {
		if (mBtLeService != null && mBtLeService.isDisconnected()) {
			// disconnected and no connection attempt in place, try to re-connect
			if (!mBtLeService.connect(mAddress)) {
				Log.d(TAG, "In sendData() and connect() failed");
				mHandler.sendEmptyMessage(BL_CONNECTION_PROBLEM);
			}
		}
		if (mBtCharacteristic == null) // a connection attempt is running, but has not finished yet
			return;

		Log.i(TAG, "Send data: " + message);

		mBtCharacteristic.setValue(message);
		mBtLeService.writeCharacteristic(mBtCharacteristic);
	}
}
