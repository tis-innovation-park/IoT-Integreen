package com.cxem_car;

import java.lang.ref.WeakReference;
import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ActivityAccelerometer extends Activity implements SensorEventListener  {
	
	private SensorManager mSensorManager;
    private Sensor mAccel;
    private cBluetooth bl = null;
    private ToggleButton onOffButton;
	
	private int xAxis = 0;
    private int yAxis = 0;
    private int motorLeft = 0;
    private int motorRight = 0;
    private String address;			// MAC-����� ����������
    private boolean show_Debug;		// ����������� ���������� ����������
    private int xMax;		    	// ������ �� ��� X, ������������ �������� ��� ��� (0-10), ��� ������, ��� ������ ����� ��������� Android-����������
    private int yMax;		    	// ������ �� ��� Y, ������������ �������� ��� ��� (0-10)
    private int yThreshold;  		// ����������� �������� ��� (����� ���� �������� �� ��������� ���������)
    private int pwmMax;	   			// ������������ �������� ���
    private int xR;					// ����� ���������
    private String commandLeft;		// ������ ������� ������ ���������
    private String commandRight;	// ������ ������� ������� ���������
    private String commandHorn;		// ������ ������� ��� ���. ������ (�������� ������)
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        
        loadPref();
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);          
        
        bl = new cBluetooth(this, new Handler(myHandlerCallback));
        
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
    
    private final Handler.Callback myHandlerCallback = new Handler.Callback() {
    	private WeakReference<ActivityAccelerometer> obj = new WeakReference<ActivityAccelerometer>(ActivityAccelerometer.this);
    	
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
            case cBluetooth.BL_INITIALIZED:
                bl.connect(address);
                break;
            }
        	return true;
        };
    };
    
    public void onSensorChanged(SensorEvent e) {
    	String directionL = "";
    	String directionR = "";
    	String cmdSend;
    	
    	xAxis = Math.round(e.values[0]*pwmMax/xR);
        yAxis = Math.round(e.values[1]*pwmMax/yMax);
        
        if(xAxis > pwmMax) xAxis = pwmMax;
        else if(xAxis < -pwmMax) xAxis = -pwmMax;		// �����. �������� - ������ ������
        
        if(yAxis > pwmMax) yAxis = pwmMax;
        else if(yAxis < -pwmMax) yAxis = -pwmMax;		// �����. �������� - ������ ������
        else if(yAxis >= 0 && yAxis < yThreshold) yAxis = 0;
        else if(yAxis < 0 && yAxis > -yThreshold) yAxis = 0;
        
        if(xAxis > 0) {		// ���� �����, �� �������� ����� �����
        	motorRight = yAxis;
        	if(Math.abs(Math.round(e.values[0])) > xR){
        		motorLeft = Math.round((e.values[0]-xR)*pwmMax/(xMax-xR));
        		motorLeft = Math.round(-motorLeft * yAxis/pwmMax);
        		//if(motorLeft < -pwmMax) motorLeft = -pwmMax;
        	}
        	else motorLeft = yAxis - yAxis*xAxis/pwmMax;
        }
        else if(xAxis < 0) {		// ������ ������
        	motorLeft = yAxis;
        	if(Math.abs(Math.round(e.values[0])) > xR){
        		motorRight = Math.round((Math.abs(e.values[0])-xR)*pwmMax/(xMax-xR));
        		motorRight = Math.round(-motorRight * yAxis/pwmMax);
        		//if(motorRight > -pwmMax) motorRight = -pwmMax;
        	}
        	else motorRight = yAxis - yAxis*Math.abs(xAxis)/pwmMax;
        }
        else if(xAxis == 0) {
        	motorLeft = yAxis;
        	motorRight = yAxis;
        }
        
        if(motorLeft > 0) {			// ���� ������ �����
        	directionL = "-";
        }      
        if(motorRight > 0) {		// ���� ������ �����
        	directionR = "-";
        }
        motorLeft = Math.abs(motorLeft);
        motorRight = Math.abs(motorRight);
               
        if(motorLeft > pwmMax) motorLeft = pwmMax;
        if(motorRight > pwmMax) motorRight = pwmMax;
        
        cmdSend = String.valueOf(commandLeft+directionL+motorLeft+"\r"+commandRight+directionR+motorRight+"\r");
        bl.sendData(cmdSend);

        TextView textX = (TextView) findViewById(R.id.textViewX);
        TextView textY = (TextView) findViewById(R.id.textViewY);
        TextView mLeft = (TextView) findViewById(R.id.mLeft);
        TextView mRight = (TextView) findViewById(R.id.mRight);
        TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);
        
        if(show_Debug){
        	textX.setText(String.valueOf("X:" + String.format("%.1f",e.values[0]) + "; xPWM:"+xAxis));
	        textY.setText(String.valueOf("Y:" + String.format("%.1f",e.values[1]) + "; yPWM:"+yAxis));
	        mLeft.setText(String.valueOf("MotorL:" + directionL + "." + motorLeft));
	        mRight.setText(String.valueOf("MotorR:" + directionR + "." + motorRight));
	        textCmdSend.setText(String.valueOf("Send:" + cmdSend.toUpperCase(Locale.US)));
        }
        else{
        	textX.setText("");
        	textY.setText("");
        	mLeft.setText("");
        	mRight.setText("");
        	textCmdSend.setText("");
        }        
    }
   
    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
    	xMax = Integer.parseInt(mySharedPreferences.getString("pref_xMax", String.valueOf(xMax)));
    	xR = Integer.parseInt(mySharedPreferences.getString("pref_xR", String.valueOf(xR)));
    	yMax = Integer.parseInt(mySharedPreferences.getString("pref_yMax", String.valueOf(yMax)));
    	yThreshold = Integer.parseInt(mySharedPreferences.getString("pref_yThreshold", String.valueOf(yThreshold)));
    	pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax", String.valueOf(pwmMax)));
    	show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
    	commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
    	commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
    	commandHorn = mySharedPreferences.getString("pref_commandHorn", commandHorn);
	}

    @Override
    protected void onResume() {
    	super.onResume();
        bl.connect(address);

    	mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bl.close();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }
}
