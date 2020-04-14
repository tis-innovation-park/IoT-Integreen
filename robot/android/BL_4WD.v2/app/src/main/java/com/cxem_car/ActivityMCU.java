package com.cxem_car;

import java.text.DecimalFormat;
import java.text.ParseException;

import android.app.Activity;
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
        
        btn_flash_Read = findViewById(R.id.flash_Read);
        btn_flash_Write = findViewById(R.id.flash_Write);
        cb_AutoOFF = findViewById(R.id.cBox_AutoOFF);
        edit_AutoOFF = findViewById(R.id.AutoOFF);
        
		loadPref();

	    bl = new cBluetooth(this, address, myHandlerCallback);

        cb_AutoOFF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                synchronized (ActivityMCU.this) {
                    if (isChecked) edit_AutoOFF.setEnabled(true);
                    else edit_AutoOFF.setEnabled(false);
                }
            }
        });
        
        btn_flash_Read.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				synchronized (ActivityMCU.this) {
					bl.sendData("Fr\t");
				}
	    	}
	    });
        
        btn_flash_Write.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				synchronized (ActivityMCU.this) {
					String str_to_send = "Fw";

					if(cb_AutoOFF.isChecked()) {
						float num1 = 0; // default is 0 [s] when no value has been set
						str_to_send += "1";

						DecimalFormat myFormatter = new DecimalFormat("00.0");
						try {
							num1 = myFormatter.parse(edit_AutoOFF.getText().toString()).floatValue();
						} catch (ParseException e) {
							Toast.makeText(getBaseContext(), R.string.mcu_error_range, Toast.LENGTH_SHORT).show();
							return;
						}

						if (num1 < 01.0 || num1 >= 99.9) {
							Toast.makeText(getBaseContext(), R.string.mcu_error_range, Toast.LENGTH_SHORT).show();
							return;
						}

						String output = myFormatter.format(num1);
						Log.d(ActivityMCU.class.getSimpleName(), "Watchdog value [s]: " + output);
						str_to_send += String.format("%c%c%c\t", output.charAt(0), output.charAt(1), output.charAt(3));
					} else {
						str_to_send += "0000\t";
					}

					Log.d(cBluetooth.TAG, "Send Flash Op:" + str_to_send);
					bl.sendData(str_to_send);
				}
			}
	    });
    }

	private final Handler.Callback myHandlerCallback = new cBluetooth.DefaultHandlerCallback<ActivityMCU>(this) {
		@Override
		public boolean handleMessage(android.os.Message msg) {
			if (msg.what == cBluetooth.BL_RECEIVE_MESSAGE) {
				StringBuilder sb = (StringBuilder) msg.obj;

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
					Toast.makeText(obj.get().getBaseContext(), R.string.mcu_flash_success, Toast.LENGTH_SHORT).show();
				}
				else if(endOfLineIndex > 0) {
					Toast.makeText(obj.get().getBaseContext(), R.string.mcu_error_get_data, Toast.LENGTH_SHORT).show();
				}

				return true;
			}
			// else
			return super.handleMessage(msg);
		}
	};

    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
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
