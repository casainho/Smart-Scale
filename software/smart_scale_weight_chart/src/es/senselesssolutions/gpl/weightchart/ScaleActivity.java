/*
 * Copyright (C) 2011 Senseless Solutions 
 *
 * Licensed under the GNU General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * Weight Chart is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Modified Source code: http://code.google.com/p/weight-chart/
 * Original Source code: http://fredrik.jemla.eu/weightchart
 */

package es.senselesssolutions.gpl.weightchart;

import java.util.GregorianCalendar;
import java.util.Vector;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class ScaleActivity extends Activity {
	
	private static final String TAG = "ScaleActivity";
	
    // Message types sent from the BluetoothCommService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothCommService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the Comm services
    private BluetoothCommService mCommService = null;
    
    // Key names received from the BluetoothCommService Handler
    public static final String WEIGHT = "weight";
    public static final String WEIGHT_TO_SAVE = "weight to save";
    
    // Intent request codes
	public static final int RESULT_FIND_DEVICE = 0;
    
    private int mBTInitState;
    
    public Vector<Byte> mDataVector = new Vector<Byte>();
    public String dataString = "";
    
    TextView mScaleTextView;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scale);
		
		mScaleTextView = (TextView) findViewById(R.id.scale);
		
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Start activity find and return the device bluetooth address
        Intent intent = new Intent(this, FindDeviceActivity.class);
        startActivityForResult(intent, RESULT_FIND_DEVICE);
	}

	@Override
	protected void onDestroy() {
		super.onPause();
		
        // Stop the Bluetooth Comm services
        if (mCommService != null) mCommService.stop();
        
        // Disable bluetooth if it was disabled before
        if (mBTInitState == 0) {
            mBluetoothAdapter.disable();
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
    // The Handler that gets information back from the BluetoothCommService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothCommService.STATE_CONNECTED:
                    break;
                case BluetoothCommService.STATE_CONNECTING:
                    break;
                case BluetoothCommService.STATE_NONE:
					finish();
                    break;
                }
                break;
            case MESSAGE_WRITE:

                break;
            case MESSAGE_READ:
            	Bundle bundleObj = msg.getData();
            	
            	if (bundleObj.containsKey("weight")) {
            		
	    			int weight = bundleObj.getInt(WEIGHT);
	    			mScaleTextView.setGravity(Gravity.CENTER);
	    			mScaleTextView.setText(String.format("%1.1f", (((float) (weight)) / 10)) + " KG");
	    			bundleObj.remove("weight");
	    			
            	} else if (bundleObj.containsKey("weight to save")) {
            		
	    			int weight = bundleObj.getInt(WEIGHT_TO_SAVE); 
	    			mScaleTextView.setGravity(Gravity.CENTER);
	    			mScaleTextView.setText(String.format("%1.1f", (((float) (weight)) / 10)) + " KG");
	    			bundleObj.remove("weight");
	    			
	    			// COPY from original code EntryActivity.java
	    			long createdAt = System.currentTimeMillis() / 1000;
	    			Database database = new Database(ScaleActivity.this);
	    			
	    			database.exec(
	    					"INSERT INTO weight (weight, created_at) VALUES (?, ?)",
	    					new Object[] { weight, createdAt });
	    		
	    			database.close();
	    			Toast.makeText(ScaleActivity.this, "Weight saved",
	    					Toast.LENGTH_SHORT).show();
	    			finish();
            	}
	    		
            	break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to: "
                               + mConnectedDeviceName, Toast.LENGTH_LONG).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case RESULT_FIND_DEVICE:
            // When FindDeviceActivity returns with an address device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Read the device MAC address from Intent
                String address = data.getExtras()
                                     .getString(FindDeviceActivity.EXTRA_DEVICE_ADDRESS); 

                // Read BT initial state
                mBTInitState = data.getExtras()
                .getInt(FindDeviceActivity.EXTRA_BT_INITIAL_STATE);
                
                // Get the BluetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                
                // Initialize the BluetoothCommService to perform bluetooth connections
                mCommService = new BluetoothCommService(this, mHandler);

                // Attempt to connect to the device
                mCommService.connect(device);
            }
            // For some reason FindDeviceActivity didn't return a bluetooth device address
            else {
            	Toast.makeText(this, "not bluetooth device", Toast.LENGTH_SHORT).show();
            	finish();
            }
            break;
        }
    }
}