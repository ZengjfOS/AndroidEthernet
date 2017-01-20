/*
 * Copyright (C) 2014 water.zhou2011@gmail.com.
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
package com.aplex.ethernet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.View;
import android.R.integer;
import android.R.string;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.aplex.ethernet.EthernetDevInfo;

import android.widget.LinearLayout;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
/**
 * Created by water.zhou on 13-8-5.
 */
public class EthernetConfigDialog extends AlertDialog implements
        DialogInterface.OnClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    private final String TAG = "EtherenetSettings";
    private static final boolean localLOGV = true;
    private Context mContext;
    private EthernetEnabler mEthEnabler;
    private View mView;
    private Spinner mDevList;
    private TextView mDevs;
    private RadioButton mConTypeDhcp;
    private RadioButton mConTypeManual;
    private EditText mIpaddr;
    private EditText mDns;
    private EditText mGateway;
    private EditText mNetmask;
    private LinearLayout ip_dns_setting;
    private static String Mode_dhcp = "dhcp";

    public EthernetConfigDialog(Context context, EthernetEnabler Enabler) {
        super(context);
        mContext = context;
        mEthEnabler = Enabler;
        buildDialogContent(context);
    }
    public int buildDialogContent(Context context) {
        this.setTitle(R.string.eth_config_title);
        this.setView(mView = getLayoutInflater().inflate(R.layout.eth_configure, null));
        mDevs = (TextView) mView.findViewById(R.id.eth_dev_list_text);
        mDevList = (Spinner) mView.findViewById(R.id.eth_dev_spinner);
        mConTypeDhcp = (RadioButton) mView.findViewById(R.id.dhcp_radio);
        mConTypeManual = (RadioButton) mView.findViewById(R.id.manual_radio);
        mIpaddr = (EditText)mView.findViewById(R.id.ipaddr_edit);
        mDns = (EditText)mView.findViewById(R.id.eth_dns_edit);
        mGateway = (EditText)mView.findViewById(R.id.gateway_edit);
        mNetmask = (EditText)mView.findViewById(R.id.netmask_edit);
        ip_dns_setting = (LinearLayout)mView.findViewById(R.id.ip_dns_setting);

        if (mEthEnabler.getManager().isConfigured()) {
            EthernetDevInfo info = mEthEnabler.getManager().getSavedConfig();
            if (mEthEnabler.getManager().getSharedPreMode().equals(Mode_dhcp)) {
                mConTypeDhcp.setChecked(true);
                mConTypeManual.setChecked(false);
                ip_dns_setting.setVisibility(View.GONE);
            } else {
                mConTypeDhcp.setChecked(false);
                mConTypeManual.setChecked(true);
                ip_dns_setting.setVisibility(View.VISIBLE);
                
                mIpaddr.setText(mEthEnabler.getManager().getSharedPreIpAddress(),TextView.BufferType.EDITABLE);
                mDns.setText(mEthEnabler.getManager().getSharedPreDnsAddress(),TextView.BufferType.EDITABLE);
                mGateway.setText(mEthEnabler.getManager().getSharedPreRoute());
                mNetmask.setText(mEthEnabler.getManager().getSharedPreNetmask());
            }
        } else {
            mConTypeDhcp.setChecked(true);
            mConTypeManual.setChecked(false);
            ip_dns_setting.setVisibility(View.GONE);
        }
        mConTypeManual.setOnClickListener(new RadioButton.OnClickListener() {
            public void onClick(View v) {
                ip_dns_setting.setVisibility(View.VISIBLE);
            }
        });
        mConTypeDhcp.setOnClickListener(new RadioButton.OnClickListener() {
            public void onClick(View v) {
                ip_dns_setting.setVisibility(View.GONE);
            }
        });

        this.setInverseBackgroundForced(true);
        this.setButton(BUTTON_POSITIVE, context.getText(R.string.menu_save), this);
        this.setButton(BUTTON_NEGATIVE, context.getText(R.string.menu_cancel), this);
        String[] Devs = mEthEnabler.getManager().getDeviceNameList();
        if (Devs != null) {
            if (localLOGV)
                Log.d(TAG, "found device: " + Devs[0]);
            updateDevNameList(Devs);
        }
        return 0;
    }
    
    /**
     * 判断是否为合法IP
     * 网上摘的，自己验证下，怎么用，我就不用说了吧？
     * @return true or false
     */
    public static boolean isIpv4(String ipAddress) {
        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    /**
     * 判断是否为合法IP
     * 网上摘的，自己验证下，怎么用，我就不用说了吧？
     * @return true or false
     */
    public static boolean isNetmask(String netmask) {
    	String mask="^(254|252|248|240|224|192|128|0)\\.0\\.0\\.0" +
    			"|255\\.(254|252|248|240|224|192|128|0)\\.0\\.0" +
    			"|255\\.255\\.(254|252|248|240|224|192|128|0)\\.0" +
    			"|255\\.255\\.255\\.(254|252|248|240|224|192|128|0)$"; 
        Pattern pattern = Pattern.compile(mask);
        Matcher matcher = pattern.matcher(netmask);
        return matcher.matches();
    }

    public void handle_saveconf() {

        EthernetDevInfo info = new EthernetDevInfo();
        info.setIfName(mDevList.getSelectedItem().toString());
        
        ArrayList<String> alertInfo = new ArrayList<String>();
        
        if (localLOGV)
            Log.d(TAG, "Config device for " + mDevList.getSelectedItem().toString());
        if (mConTypeManual.isChecked()) {
            // check the ipaddr
            String ipaddr = mIpaddr.getText().toString().trim();
            if ( !isIpv4(ipaddr) ) 
            	alertInfo.add("IP address");

            // check the netmask
            String netmask = mNetmask.getText().toString().trim();
            if ( (!isIpv4(netmask)) || (!isNetmask(netmask)) ) 
            	alertInfo.add("Netmask");

            // check the gateway
            String gateway = mGateway.getText().toString().trim();
            if ( (gateway.length() != 0) && !isIpv4(gateway) ) 
            	alertInfo.add("Gateway");

            // check the dns
            String dnsaddr = mDns.getText().toString().trim();
            if ( (dnsaddr.length() != 0) && !isIpv4(dnsaddr) ) 
            	alertInfo.add("DNS address");
            
            if (!alertInfo.isEmpty()) {
            	String information = "Please check the format:\n";
            	int i = 0;
            	for (; i < alertInfo.size()-1; i++) {
					information += "    " + (i+1) + ". ";
					information += alertInfo.get(i);
					information += ";\n";
				}
				information += "    " + (i+1) + ". ";
            	information += alertInfo.get(alertInfo.size() - 1);
				information += ".\n";
            	
                new AlertDialog.Builder(mContext).setTitle("Info:")
                    .setMessage(information).setPositiveButton("confirm", null).show(); 
                return;
			}

            info.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
            info.setIpAddress(ipaddr);
            info.setNetMask(netmask);

            // 由于gateway、DNS不是一定要设置的
            if ( gateway.length() == 0 ) 
				info.setRouteAddr(null);
            else
            	info.setRouteAddr(gateway);

            if ( dnsaddr.length() == 0 ) 
				info.setDnsAddr(null);
            else
            	info.setDnsAddr(dnsaddr);

        } else {
            info.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_DHCP);
            info.setIpAddress(null);
            info.setDnsAddr(null);
            info.setNetMask(null);
            info.setRouteAddr(null);
        }

        info.setProxyAddr(mEthEnabler.getManager().getSharedPreProxyAddress());
        info.setProxyPort(mEthEnabler.getManager().getSharedPreProxyPort());
        info.setProxyExclusionList(mEthEnabler.getManager().getSharedPreProxyExclusionList());
        mEthEnabler.getManager().updateDevInfo(info);
        mEthEnabler.setEthEnabled();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                handle_saveconf();
                break;
            case BUTTON_NEGATIVE:
                //Don't need to do anything
                break;
            default:
                Log.e(TAG,"Unknow button");
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onClick(View v) {

    }
    public void updateDevNameList(String[] DevList) {
        if (DevList != null) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    getContext(), android.R.layout.simple_spinner_item, DevList);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            mDevList.setAdapter(adapter);
        }

    }

}

