package com.cxem_car;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ActivityButtons extends Activity {
	
	private cBluetooth bl = null;
	private ToggleButton onOffButton;
	
	private Button btn_forward, btn_backward, btn_left, btn_right;
	
    private int motorLeft = 0;
    private int motorRight = 0;
    private String address;
    private int pwmBtnMotorLeft;
    private int pwmBtnMotorRight;
    private String commandLeft;		// ������ ������� ������ ���������
    private String commandRight;	// ������ ������� ������� ���������
    private String commandHorn;		// ������ ������� ��� ���. ������ (�������� ������)
    private boolean show_Debug;
	
    /**
     * Button handler for all directions
     */
	private void buttonHandler() {
		String cmdSend = "";
      	cmdSend = commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r";
       	bl.sendData(cmdSend);
        TextView mLeft = (TextView) findViewById(R.id.mLeft);
        TextView mRight = (TextView) findViewById(R.id.mRight);
        TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);
        if (show_Debug) {
	        mLeft.setText(String.valueOf("MotorL:" + motorLeft));
	        mRight.setText(String.valueOf("MotorR:" + motorRight));
	        textCmdSend.setText(String.valueOf("Send:" + cmdSend.toUpperCase(Locale.US)));
        } else {
        	mLeft.setText("");
        	mRight.setText("");
        	textCmdSend.setText("");
        }
	}
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buttons);
				
		loadPref();
		
	    bl = new cBluetooth(this, address, new cBluetooth.DefaultHandlerCallback<>(this));
		
		btn_forward = (Button) findViewById(R.id.forward);
		btn_backward = (Button) findViewById(R.id.backward);
		btn_left = (Button) findViewById(R.id.left);
		btn_right = (Button) findViewById(R.id.right);
		       
		btn_forward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	motorLeft = pwmBtnMotorLeft;
		        	motorRight = pwmBtnMotorRight;
		        	buttonHandler();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	buttonHandler();
		        	v.performClick(); // click event needs to be handled
		        } else {
		        	return false;
		        }
		        return true;
			}
		});
		
		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	motorLeft = -pwmBtnMotorLeft;
		        	motorRight = pwmBtnMotorRight;
		        	buttonHandler();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	buttonHandler();
		        	v.performClick(); // click event needs to be handled
		        } else {
		        	return false;
		        }
				return true;
		    }
		});
		
		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	motorLeft = pwmBtnMotorLeft;
		        	motorRight = -pwmBtnMotorRight;
		        	buttonHandler();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	buttonHandler();
		        	v.performClick(); // click event needs to be handled
		        } else {
		        	return false;
		        }
				return true;
		    }
		});
		
		btn_backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	motorLeft = -pwmBtnMotorLeft;
		        	motorRight = -pwmBtnMotorRight;
		        	buttonHandler();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	buttonHandler();
		        	v.performClick(); // click event needs to be handled
		        } else {
		        	return false;
		        }
				return true;
		    }
		});
		
		onOffButton = (ToggleButton) findViewById(R.id.OnOffButton);   
		onOffButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(onOffButton.isChecked()){
	    			bl.sendData(String.valueOf(commandHorn+"1\r"));
	    		}else{
	    			bl.sendData(String.valueOf(commandHorn+"0\r"));
	    		}
	    	}
	    });		
	}

	
    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
    	pwmBtnMotorLeft = Integer.parseInt(mySharedPreferences.getString("pref_pwmBtnMotorLeft", String.valueOf(pwmBtnMotorLeft)));
    	pwmBtnMotorRight = Integer.parseInt(mySharedPreferences.getString("pref_pwmBtnMotorRight", String.valueOf(pwmBtnMotorRight)));
    	show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
    	commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
    	commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
    	commandHorn = mySharedPreferences.getString("pref_commandHorn", commandHorn);
	}

    @Override
    protected void onResume() {
        super.onResume();
        bl.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bl.close();
    }
}
