/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.senselesssolutions.gpl.weightchart;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FindDeviceActivity extends Activity {
	
	private static final String TAG = "FindDeviceActivity";
	
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static String EXTRA_BT_INITIAL_STATE = "bt_initial_state";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private int mBTInitState;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // If the adapter is null, then Bluetooth is not supported
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        /* If bluetooth is no enable, send Intent to start activity for user permission
         * and enable of BT
         */
        if (!mBtAdapter.isEnabled()) {   
        	
        	mBTInitState = 0;
        	
        	// Enable bluetooth adapter
        	if (!mBtAdapter.enable())
        	{
        		// There was some problem and bluetooth adapter can't be turned ON
                finish();
        	}
        	
            Toast.makeText(getApplicationContext(), "Enabling Bluetooth adapter...", Toast.LENGTH_LONG).show();
        	
            // Register for broadcasts when bluetooth adapter state change
            filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.registerReceiver(mReceiver, filter);
            
        } else {
        	mBTInitState = 1;
        	
        	/* Bluetooth is enable and so start the device search */
        	searchDevice();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    // The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            	if (extras.get(BluetoothAdapter.EXTRA_STATE).equals(BluetoothAdapter.STATE_ON))
    			{
                    // Bluetooth is now enabled
                	searchDevice();
    			}
         
            // When discovery finds a device
        	} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because we already checked that
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	if (device.getName().equals(
                    		getResources().getText(R.string.bluetooth_device_name).toString())) {
                		
                		returnWithDeviceAddress(device.getAddress());
                	}
                }
            // When discovery is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	
            	/* 
            	 * Discovery were finish and none device were found!
            	 * Returning to calling activity with RESULT_CANCELED on Intent.
            	 */
            	
                // Set result and finish this Activity
                setResult(Activity.RESULT_CANCELED, null, null);

           	 	//Toast.makeText(FindDeviceActivity.this, "device not found!!!",
                  //   Toast.LENGTH_LONG).show();
           	 	Log.d(TAG, "device not found");
                
                finish();
            }
        }
    };
    
    void searchDevice() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {            	
                if (device.getName().equals(
                		getResources().getText(R.string.bluetooth_device_name).toString())) {

                	returnWithDeviceAddress(device.getAddress());
                }
                else {
                	/*
                	 * Device were not found on the paired devices list.
                	 * Doing discovery.
                	 */
                	
                    // If we're already discovering, stop it
                    if (mBtAdapter.isDiscovering()) {
                        mBtAdapter.cancelDiscovery();
                    }
                	
                    // Request discover from BluetoothAdapter
                    mBtAdapter.startDiscovery();                	
                }
            }
        } else {
        	
        	/*
        	 * Device is not paired yet.
        	 * Doing discovery.
        	 */
        	
            // If we're already discovering, stop it
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
        	
            // Request discover from BluetoothAdapter
            mBtAdapter.startDiscovery();
        }
    }

    void returnWithDeviceAddress(String address) {
		/*
		 * Found the device on the paired devices.
		 * Finish and send before the Intent with BT device MAC address
		 * to the caller activity.
		 */
		
	    // Create the result Intent and include the BT device MAC address
	    Intent intent = new Intent();
	    intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
	    intent.putExtra(EXTRA_BT_INITIAL_STATE, mBTInitState);
	
	    // Set result and finish this Activity
	    setResult(Activity.RESULT_OK, intent);
	    
		Log.d(TAG, "Device address: " + address);
 
	    finish();
    }
}
