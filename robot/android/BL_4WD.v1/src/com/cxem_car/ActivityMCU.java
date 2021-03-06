package com.cxem_car;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class ActivityMCU  extends Activity{
	
	private cBluetooth bl = null;
	private Button btn_flash_Read, btn_flash_Write;
	private CheckBox cb_AutoOFF;
	private EditText edit_AutoOFF;
	
	private String address;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcu);
        
        btn_flash_Read = (Button) findViewById(R.id.flash_Read);
        btn_flash_Write = (Button) findViewById(R.id.flash_Write);
        cb_AutoOFF = (CheckBox) findViewById(R.id.cBox_AutoOFF);
        edit_AutoOFF = (EditText) findViewById(R.id.AutoOFF);
        
		loadPref();
		
	    bl = new cBluetooth(this, new Handler(myHandlerCallback));
	    bl.checkBTState();
	    
	    cb_AutoOFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	synchronized (ActivityMCU.this) {
	            	if (isChecked) edit_AutoOFF.setEnabled(true);
	            	else if (!isChecked) edit_AutoOFF.setEnabled(false);
            	}
            }
        });
        
        btn_flash_Read.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				synchronized (ActivityMCU.this) {
					bl.sendData(String.valueOf("Fr\t"));
				}
	    	}
	    });
        
        btn_flash_Write.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				synchronized (ActivityMCU.this) {
					float num1 = 0; // default is 0 [s] when no value has been set
					String str_to_send = "Fw";
					
					if(cb_AutoOFF.isChecked()) {
						str_to_send += "1";
						
						try {
					        num1 = Float.parseFloat(edit_AutoOFF.getText().toString());
					    } catch (NumberFormatException e) {
					    	String err_data_entry = getString(R.string.err_data_entry); 
					    	Toast.makeText(getBaseContext(), err_data_entry, Toast.LENGTH_SHORT).show();
					    	return;
					    }
			
						if (num1 < 1 || num1 >= 100) {
							String err_range = getString(R.string.mcu_error_range); 
							Toast.makeText(getBaseContext(), err_range, Toast.LENGTH_SHORT).show();
							return;
						}	
					} else {
						str_to_send += "0";
						
						try {
					        num1 = Float.parseFloat(edit_AutoOFF.getText().toString());
					    } catch (NumberFormatException e) {
					    	// use default 0 [s] when no value has been set
					    }
					}
		    						
					DecimalFormat myFormatter = new DecimalFormat("00.0");
				    String output = myFormatter.format(num1);
					
				    str_to_send += String.valueOf(output.charAt(0)) + String.valueOf(output.charAt(1)) + String.valueOf(output.charAt(3));
				    str_to_send += "\t";
				    		
				    Log.d(cBluetooth.TAG, "Send Flash Op:" + str_to_send);
				    bl.sendData(str_to_send);
					//Toast.makeText(getBaseContext(), str_to_send, Toast.LENGTH_SHORT).show();
		    	}
			}
	    });
    }
    
    private final Handler.Callback myHandlerCallback = new Handler.Callback() {
    	private WeakReference<ActivityMCU> obj = new WeakReference<ActivityMCU>(ActivityMCU.this);
    	
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
            case cBluetooth.RECEIVE_MESSAGE:										// if message has been received
            	byte[] readBuf = (byte[]) msg.obj;
            	String strIncom = new String(readBuf, 0, msg.arg1);
            	StringBuilder sb = new StringBuilder();
            	sb.append(strIncom);												// append string
            	
            	int FDataLineIndex = sb.indexOf("FData:");					// ������ � Flash ������� (������)
            	int FWOKLineIndex = sb.indexOf("FWOK");						// ������ � ���������� �� �������� ������ � Flash
            	int endOfLineIndex = sb.indexOf("\r\n");

            	if (FDataLineIndex >= 0 && endOfLineIndex > 0 && endOfLineIndex > FDataLineIndex) { 					// ���� ����������
            		String sbprint = sb.substring("FData:".length(), endOfLineIndex);	// ��������
            		synchronized (obj) {
	            		if (sbprint.substring(0, 1).equals("1")) { // we have a valid value set [s]
	            			obj.get().cb_AutoOFF.setChecked(true);
	            			obj.get().edit_AutoOFF.setEnabled(true);
	                		Float edit_data_AutoOFF = Float.parseFloat(sbprint.substring(1, 4))/10;  		
	                		obj.get().edit_AutoOFF.setText(String.valueOf(edit_data_AutoOFF));
	            		} else { // there is no valid value
	            			obj.get().cb_AutoOFF.setChecked(false);
	            			obj.get().edit_AutoOFF.setEnabled(false);
	            			obj.get().edit_AutoOFF.setText("");
	            		}
            		}
                }
            	else if (FWOKLineIndex >= 0 && endOfLineIndex > 0 && endOfLineIndex > FWOKLineIndex) {
            		String flash_success = obj.get().getString(R.string.flash_success);
            		Toast.makeText(obj.get().getBaseContext(), flash_success, Toast.LENGTH_SHORT).show();
            	}
            	else if(endOfLineIndex > 0) {
            		String error_get_data = obj.get().getString(R.string.error_get_data);
            		Toast.makeText(obj.get().getBaseContext(), error_get_data, Toast.LENGTH_SHORT).show();
            	}
            	break;
            }
        	return true;
        }
    };
	
    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	bl.BT_Connect(address);
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	bl.BT_onPause();
    }    
}