package com.cxem_car;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityServo extends Activity {
	private cBluetooth bl = null;
	
	private String address;			// MAC-����� ����������
	private boolean show_Debug;		// ����������� ���������� ����������
	private int pwmMax;	   			// ������������ �������� ���
	private String commandServo;
	
	private Button btn_up;
	private Button btn_down;
	
	private int motorServo; // servo motor angle [0..pwmMax]
	private String cmdSend; // command to send

	private Button btn_lift;

    /**
     * Button handler
     */
	private void buttonHandler() {
      	cmdSend = commandServo+motorServo+"\r";
        TextView mServo = (TextView) findViewById(R.id.mServo);
        TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);
        if (show_Debug) {
	        mServo.setText(String.valueOf("Motor:" + motorServo));
	        textCmdSend.setText(String.valueOf("Send:" + cmdSend.toUpperCase(Locale.US)));
        } else {
        	mServo.setText("");
        	textCmdSend.setText("");
        }
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servo);
		
		loadPref();
		
	    bl = new cBluetooth(this, new Handler(myHandlerCallback));
	    bl.checkBTState();

	    btn_up = (Button) findViewById(R.id.button_up);
	    btn_down = (Button) findViewById(R.id.button_down);
	    btn_lift = (Button) findViewById(R.id.button_lift);
	    
	    btn_up.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_MOVE) {
		        	if (motorServo < pwmMax) ++motorServo;
		        	buttonHandler();
		        	return true;
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		           	bl.sendData(cmdSend);
		        	v.performClick(); // click event needs to be handled
		        	return true;
		        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
		        	return true;
		        }
				return false;
			}
		});
	    
	    btn_down.setOnTouchListener(new OnTouchListener() {	
			public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_MOVE) {
	        		if (motorServo > 0) --motorServo;
	        		buttonHandler();
	        		return true;
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	bl.sendData(cmdSend);
		        	v.performClick(); // click event needs to be handled
		        	return true;
		        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
		        	return true;
		        }
				return false;
			}
		});	
	    
	    btn_lift.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// lifts the servo from right to left or left to right
				if (motorServo >= pwmMax/2) {
					motorServo = 0;
				} else {
					motorServo = pwmMax;
				}
				buttonHandler();
				bl.sendData(cmdSend);
			}
		});
	}
	
    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
    	pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax", String.valueOf(pwmMax)));
    	show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
    	commandServo = mySharedPreferences.getString("pref_commandServo", commandServo);
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	bl.BT_Connect(address);
    	// restore servo value
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	motorServo = mySharedPreferences.getInt("motorServo", 0);
	    buttonHandler();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	bl.BT_onPause();
    	// save servo value
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor editor = mySharedPreferences.edit();
    	editor.putInt("motorServo", motorServo);
    	editor.commit();
    }
    
	private final Handler.Callback myHandlerCallback =  new Handler.Callback() {
    	private WeakReference<ActivityServo> obj = new WeakReference<ActivityServo>(ActivityServo.this);
    	
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
}
