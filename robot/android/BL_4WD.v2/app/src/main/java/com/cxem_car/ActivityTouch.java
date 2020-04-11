package com.cxem_car;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityTouch extends Activity {
	
    private cBluetooth bl = null;
	
	private final static int BIG_CIRCLE_SIZE = 120;
	private final static int FINGER_CIRCLE_SIZE = 20;
	
    private int motorLeft = 0;
    private int motorRight = 0;
    
    private String address;			// MAC-����� ����������
    private boolean show_Debug;		// ����������� ���������� ����������
    private int xRperc;				// ����� ���������
    private int pwmMax;	   			// ������������ �������� ���
    private String commandLeft;		// ������ ������� ������ ���������
    private String commandRight;	// ������ ������� ������� ���������
    private String commandHorn;		// ������ ������� ��� ���. ������ (�������� ������)
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));
                
        loadPref();
        
        bl = new cBluetooth(this, new Handler(myHandlerCallback));
        
        final ToggleButton onOffButton = new ToggleButton(this);
        addContentView(onOffButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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
	
    private final Handler.Callback myHandlerCallback =  new Handler.Callback() {
    	private WeakReference<ActivityTouch> obj = new WeakReference<ActivityTouch>(ActivityTouch.this);
    	
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
	
	class MyView extends View {

		Paint fingerPaint, borderPaint, textPaint;

        int dispWidth;
        int dispHeight;
        
        float x;
        float y;
        
        float xcirc;
        float ycirc;
        
    	String directionL = "";
    	String directionR = "";
    	String cmdSend;
    	String temptxtMotor;
    	
        // ���������� ��� ��������������
        boolean drag = false;
        float dragX = 0;
        float dragY = 0;
        

        public MyView(Context context) {
        	super(context);
        	fingerPaint = new Paint();
        	fingerPaint.setAntiAlias(true);
        	fingerPaint.setColor(Color.RED);
                
        	borderPaint = new Paint();
        	borderPaint.setColor(Color.BLUE);
        	borderPaint.setAntiAlias(true);
        	borderPaint.setStyle(Style.STROKE);
        	borderPaint.setStrokeWidth(3);
        	
	        textPaint = new Paint(); 
	        textPaint.setColor(Color.WHITE); 
	        textPaint.setStyle(Style.FILL); 
	        textPaint.setColor(Color.BLACK); 
	        textPaint.setTextSize(14); 
        }


        protected void onDraw(Canvas canvas) {
        	dispWidth = (this.getRight()-this.getLeft())/2;
        	dispHeight = (this.getBottom()-this.getTop())/2;
        	if(!drag){
        		x = dispWidth;
        		y = dispHeight;
        		fingerPaint.setColor(Color.RED);
        	}

            canvas.drawCircle(x, y, FINGER_CIRCLE_SIZE, fingerPaint);              
            canvas.drawCircle(dispWidth, dispHeight, BIG_CIRCLE_SIZE, borderPaint);
            
            if(show_Debug){
	            canvas.drawText(String.valueOf("X:"+xcirc), 10, 80, textPaint);
	            canvas.drawText(String.valueOf("Y:"+(-ycirc)), 10, 95, textPaint);
	            canvas.drawText(String.valueOf("Motor:"+temptxtMotor), 10, 115, textPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
        	
        	// ���������� Touch-�������
        	float evX = event.getX();
        	float evY = event.getY();
                          
        	xcirc = evX - dispWidth;
        	ycirc = evY - dispHeight;
        	//Log.d("4WD", String.valueOf("X:"+this.getRight()+" Y:"+dispHeight));
            	   
        	float radius = (float) Math.sqrt(Math.pow(Math.abs(xcirc),2)+Math.pow(Math.abs(ycirc),2));

        	switch (event.getAction()) {

        	case MotionEvent.ACTION_DOWN:        
        		if(radius >= 0 && radius <= BIG_CIRCLE_SIZE){
        			x = evX;
        			y = evY;
        			fingerPaint.setColor(Color.GREEN);
        			temptxtMotor = CalcMotor(xcirc,ycirc);
        			invalidate();
        			drag = true;
        		}
        		break;

        	case MotionEvent.ACTION_MOVE:
        		// ���� ����� �������������� �������
        		if (drag && radius >= 0 && radius <= BIG_CIRCLE_SIZE) {
        			x = evX;
        			y = evY;
        			fingerPaint.setColor(Color.GREEN);
        			temptxtMotor = CalcMotor(xcirc,ycirc);
        			invalidate();
        		}
        		break;

        	// ������� ���������
        	case MotionEvent.ACTION_UP:
        		// ��������� ����� ��������������
        		xcirc = 0;
        		ycirc = 0; 
        		drag = false;
        		temptxtMotor = CalcMotor(xcirc,ycirc);
        		invalidate();
        		performClick(); // click event needs to be handled
        		break;
        	
        	default:
        		return false;
        	}
        	return true;
        }

		@Override
		public boolean performClick() {
			return super.performClick();
		}
	}
	
	private String CalcMotor(float calc_x, float calc_y){
    	String directionL = "";
    	String directionR = "";

    	/*
    	 * This operation consists in a Cartesian to Polar coordinate transformation.
    	 * First we calculate phi and radius, but need to subtract PI/4 to have
    	 * the  maximum of both motors on PI/2 and not PI/4 - that's x=0,
    	 * y=BIG_CIRCLE_SIZE.
    	 * The radius (distance) allows us to determine the intensity (velocity)
    	 * of both the motors (0..pwmMax).
    	 * Arrived here we go back to Cartesian coordinates to determine the
    	 * final two motor PWM (intensity) values and invert them (left -> right
    	 * motor to turn left, down -> forward).
    	 */
    	double phi = Math.atan2(calc_y, calc_x);
    	phi -= Math.PI / 4;
    	double radius = Math.sqrt(Math.pow(calc_x, 2) + Math.pow(calc_y, 2));
    	double pwm = radius / BIG_CIRCLE_SIZE * pwmMax;
    	motorLeft = -Math.round((float)(Math.sin(phi) * pwm));
    	motorRight = -Math.round((float)(Math.cos(phi) * pwm));
    	
        String cmdSend = String.valueOf(commandLeft+directionL+motorLeft+"\r"+commandRight+directionR+motorRight+"\r");	
        bl.sendData(cmdSend);
        
		return cmdSend;
	}
		
	@Override
    protected void onResume() {
        super.onResume();
        bl.connect(address);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bl.close();
    }

    private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// ������ ��� ��������� ��������� ��������
    	xRperc = Integer.parseInt(mySharedPreferences.getString("pref_xRperc", String.valueOf(xRperc)));
    	pwmMax = Integer.parseInt(mySharedPreferences.getString("pref_pwmMax", String.valueOf(pwmMax)));
    	show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
    	commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
    	commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
    	commandHorn = mySharedPreferences.getString("pref_commandHorn", commandHorn);
	}
}
