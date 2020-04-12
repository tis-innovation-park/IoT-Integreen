package com.cxem_car;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

public class cBluetooth
{
	public final static String TAG = "BL_4WD";

	public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

	private final Context mContext;
	private final String mAddress;
	private final Handler mHandler;
	private BluetoothLeService mBtLeService;

	public final static int BL_NOT_AVAILABLE = 1;
	public final static int BL_INCORRECT_ADDRESS = 2;
	public final static int BL_REQUEST_ENABLE = 3;
	public final static int BL_SOCKET_FAILED = 4;
	public final static int RECEIVE_MESSAGE = 10;

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBtLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBtLeService.initialize()) {
				mHandler.sendEmptyMessage(BL_NOT_AVAILABLE);
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
				close();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				Log.i(TAG, "Services Discovered");
				// Enable Notifications
				BluetoothGattCharacteristic characteristic = mBtLeService.getService(UUID_SERVICE).getCharacteristic(UUID_CHARACTERISTIC);
				mBtLeService.setCharacteristicNotification(characteristic, true);
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				// Read from the InputStream. Since buffering differs on
				// Android and Arduino side we need to keep up our reading
				// until we receive the end-of-line markers (\r\n).
				recvData.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				if (recvData.length() > 0 && recvData.charAt(recvData.length()-1) == '\n') {
					Log.i(TAG, "Receive data: " + recvData);
					mHandler.obtainMessage(RECEIVE_MESSAGE, -1, -1, recvData).sendToTarget();
					recvData = new StringBuilder(); // re-start from scratch
				}
			}
		}
	};

	public static class DefaultHandlerCallback<T extends Activity> implements Handler.Callback {
		protected WeakReference<T> obj;

		public DefaultHandlerCallback(T obj) {
			this.obj = new WeakReference<T>(obj);
		}

		public boolean handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case cBluetooth.BL_NOT_AVAILABLE:
					Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
					Toast.makeText(obj.get().getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
					obj.get().finish();
					break;
				case cBluetooth.BL_INCORRECT_ADDRESS:
					Log.d(cBluetooth.TAG, "Incorrect MAC address");
					Toast.makeText(obj.get().getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
					break;
				case cBluetooth.BL_REQUEST_ENABLE:
					Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
					BluetoothAdapter.getDefaultAdapter();
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					obj.get().startActivityForResult(enableBtIntent, 1);
					break;
				case cBluetooth.BL_SOCKET_FAILED:
					Toast.makeText(obj.get().getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
					obj.get().finish();
					break;
			}
			return true;
		};
	};

	public cBluetooth(Context context, String address, Handler.Callback handlerCallback) {
		mContext = context;
		mAddress = address;
		mHandler = new Handler(handlerCallback);

		if(!BluetoothAdapter.checkBluetoothAddress(mAddress)){
			mHandler.sendEmptyMessage(BL_INCORRECT_ADDRESS);
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

	public void connect() {
		Log.d(TAG, "...On Resume...");

		if (mBtLeService == null)
			return;

		mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		if (!mBtLeService.connect(mAddress)) {
			Log.d(TAG, "In onResume() and socket create failed");
			mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
			return;
		}
	}

	public void close() {
		Log.d(TAG, "...On Destroy...");

		if (mBtLeService == null)
			return;

		mContext.unregisterReceiver(mGattUpdateReceiver);

		mContext.unbindService(mServiceConnection);
		mBtLeService = null;
	}

	public void sendData(String message) {
		BluetoothGattService s = mBtLeService.getService(UUID_SERVICE);
		if (s == null) {
			Log.d(TAG, "In sendData() and an exception occurred during read");
			mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
			return;
		}

		BluetoothGattCharacteristic c = s.getCharacteristic(UUID_CHARACTERISTIC);
		if (c == null) {
			Log.d(TAG, "In sendData() and an exception occurred during read");
			mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
			return;
		}

		Log.i(TAG, "Send data: " + message);

		c.setValue(message);
		mBtLeService.writeCharacteristic(c);
	}
}
