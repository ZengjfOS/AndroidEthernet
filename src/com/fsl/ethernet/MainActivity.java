/*
 * Copyright (C) 2013-2015 Freescale Semiconductor, Inc.
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

package com.fsl.ethernet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.content.BroadcastReceiver;
import com.fsl.ethernet.EthernetDevInfo;
import android.view.View.OnClickListener;
import android.text.method.ScrollingMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.SystemProperties;

public class MainActivity extends Activity {
    private EthernetEnabler mEthEnabler;
    private EthernetConfigDialog mEthConfigDialog;
    private Button mBtnConfig;
    private Button mBtnCheck;
    private EthernetDevInfo  mSaveConfig;
    private ConnectivityManager  mConnMgr;
    private String TAG = "EthernetMainActivity";
    private static String Mode_dhcp = "dhcp";
    private boolean shareprefences_flag = false;
    private boolean first_run = true;
    public static final String FIRST_RUN = "ethernet";
    private Button mBtnAdvanced;
    private EthernetAdvDialog mEthAdvancedDialog;
    // set the broadcast receiver
    private final BroadcastReceiver mEthernetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	// filter the broadcast
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                NetworkInfo info =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    Log.i(TAG,"getState()="+info.getState() + "getType()=" +
                            info.getType());
                    if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                        if (info.getState() == State.DISCONNECTED)
                            mConnMgr.setGlobalProxy(null);
                            SystemProperties.set("rw.HTTP_PROXY", "");
                        if (info.getState() == State.CONNECTED)
                            mEthEnabler.getManager().initProxy();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set display type
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // defaut page to display
        setContentView(R.layout.ethernet_configure);
        // get ethernet config shared preferences
        SharedPreferences sp = getSharedPreferences("ethernet",
                Context.MODE_WORLD_WRITEABLE);

        // create ethernet manager and reset the interface
        mEthEnabler = new EthernetEnabler(this);

        // add button listener
        addListenerOnBtnConfig();
        addListenerOnBtnCheck();
        // addListenerOnBtnAdvanced();

        // register broadcast receiver
        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mEthernetReceiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void addListenerOnBtnConfig() {
        mBtnConfig = (Button) findViewById(R.id.btnConfig);

        mBtnConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEthConfigDialog = new EthernetConfigDialog(MainActivity.this, mEthEnabler);
                mEthEnabler.setConfigDialog(mEthConfigDialog);
                mEthConfigDialog.show();
            }
        });
    }

    public void addListenerOnBtnCheck() {
        mBtnConfig = (Button) findViewById(R.id.btnCheck);

        mBtnConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) findViewById(R.id.tvConfig);
                text.setMovementMethod(ScrollingMovementMethod.getInstance());
                mSaveConfig = mEthEnabler.getManager().getSavedConfig();
                if (mSaveConfig != null) {
                    String config_detail = 
                    		"\nEthernet Devices : " + mEthEnabler.getManager().getDeviceNameList()[0] + "\n"
                    		+ "Connection Type : " + mEthEnabler.getManager().getSharedPreMode() + "\n"
                            + "IP Address      : " + mEthEnabler.getManager().getSharedPreIpAddress() + "\n"
                    		+ "Netmask         : " + mEthEnabler.getManager().getSharedPreNetMask() + "\n"
                    		+ "Gateway         : " + mEthEnabler.getManager().getSharedPreGateway() + "\n"
                            + "DNS Address     : " + mEthEnabler.getManager().getSharedPreDnsAddress() + "\n";
                            //+ "Proxy Address : " + mEthEnabler.getManager().getSharedPreProxyAddress() + "\n"
                            //+ "Proxy Port    : " + mEthEnabler.getManager().getSharedPreProxyPort() + "\n";
                    BufferedReader reader = null;
                    try {
						reader = new BufferedReader(new FileReader("/sys/class/net/eth0/address"));
						String ethernetMac = reader.readLine();
						if (ethernetMac != null && ethernetMac.trim().length() != 0)
							config_detail += "MAC Address     : " + ethernetMac;
					} catch (Exception e) {
						Log.e(TAG, "open /sys/class/net/eth0/address failed:" + e);
					} finally {
						try {
							if (reader != null)
								reader.close();
						} catch (IOException e) {
							// TODO: handle exception
							Log.e(TAG, "close /sys/class/net/eth0 address failed:" + e);
						}
					}
                  
                    // text.setText(config_detail);
                    new AlertDialog.Builder(MainActivity.this).setTitle("Info:").setMessage(config_detail).setPositiveButton("confirm", null).show(); 
                }
            }
        });
    }

    /*
    public void addListenerOnBtnAdvanced() {
        mBtnAdvanced = (Button) findViewById(R.id.btnAdvanced);

        mBtnAdvanced.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveConfig = mEthEnabler.getManager().getSavedConfig();
                if (mSaveConfig != null) {
                    mEthAdvancedDialog = new EthernetAdvDialog(MainActivity.this,mEthEnabler);
                    mEthEnabler.setmEthAdvancedDialog(mEthAdvancedDialog);
                    mEthAdvancedDialog.show();
                }
            }
        });
    }
    */

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onStop() will force clear global proxy set by ethernet");
        unregisterReceiver(mEthernetReceiver);
    }
}
