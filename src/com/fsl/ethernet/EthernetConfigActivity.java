package com.fsl.ethernet;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.View;
import android.view.View.OnClickListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;
import com.fsl.ethernet.EthernetDevInfo;
import android.widget.LinearLayout;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EthernetConfigActivity extends Activity implements AdapterView.OnItemSelectedListener, OnClickListener {

    private final String TAG = "EtherenetSettings";
    private static final boolean localLOGV = true;
    private EthernetEnabler mEthEnabler;
    private Spinner mDevList;
    private TextView mDevs;
    private RadioButton mConTypeDhcp;
    private RadioButton mConTypeManual;
    private EditText mIpaddr;
    private EditText mDns;
    private EditText mMask;
    private EditText mGateway;
    private LinearLayout ip_dns_setting;
    // private static String Mode_dhcp = "dhcp";
    private static String Mode_dhcp = EthernetDevInfo.ETHERNET_CONN_MODE_DHCP;
    private Handler configHandler = new Handler();
    
    private Button mconfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ethernet_config);
		
		mEthEnabler = ((EthernetApp)getApplication()).getmEthEnabler();

        mDevs = (TextView) this.findViewById(R.id.eth_dev_list_text);
        mDevList = (Spinner) this.findViewById(R.id.eth_dev_spinner);
        mConTypeDhcp = (RadioButton) this.findViewById(R.id.dhcp_radio);
        mConTypeManual = (RadioButton) this.findViewById(R.id.manual_radio);
        mIpaddr = (EditText)this.findViewById(R.id.ipaddr_edit);
        mDns = (EditText)this.findViewById(R.id.eth_dns_edit);
        mMask = (EditText)this.findViewById(R.id.eth_netmask_edit);
        mGateway = (EditText)this.findViewById(R.id.eth_gateway_edit);
        mconfirm = (Button)this.findViewById(R.id.confirm_setings);
        
        ip_dns_setting = (LinearLayout)this.findViewById(R.id.ip_dns_setting);

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
                mMask.setText(mEthEnabler.getManager().getSharedPreNetMask(),TextView.BufferType.EDITABLE);
                mGateway.setText(mEthEnabler.getManager().getSharedPreGateway(),TextView.BufferType.EDITABLE);
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

        String[] Devs = mEthEnabler.getManager().getDeviceNameList();
        if (Devs != null) {
            if (localLOGV)
                Log.d(TAG, "found device: " + Devs[0]);
            updateDevNameList(Devs);
        }
        
        mconfirm.setOnClickListener(this);
        
        
	}

    public static boolean isIpv4(String ipAddress) {
        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public static boolean isNetmask(String netmask) {
    	String mask="^(254|252|248|240|224|192|128|0)\\.0\\.0\\.0" +
    			"|255\\.(254|252|248|240|224|192|128|0)\\.0\\.0" +
    			"|255\\.255\\.(254|252|248|240|224|192|128|0)\\.0" +
    			"|255\\.255\\.255\\.(254|252|248|240|224|192|128|0)$"; 
        Pattern pattern = Pattern.compile(mask);
        Matcher matcher = pattern.matcher(netmask);
        return matcher.matches();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ethernet_config, menu);
		return true;
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
            String netmask = mMask.getText().toString().trim();
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
            	
                new AlertDialog.Builder(this).setTitle("Info:").setMessage(information).setPositiveButton("confirm", null).show(); 

                return;
			}

            info.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
            info.setIpAddress(ipaddr);
            info.setNetMask(netmask);

            // 由于gateway、DNS不是一定要设置的
            if ( gateway.length() == 0 ) 
            	info.setGateway(null);
            else
            	info.setGateway(gateway);

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
        configHandler.post(new ConfigHandler(info));
    }

    class ConfigHandler implements Runnable {
        EthernetDevInfo info;

        public ConfigHandler(EthernetDevInfo info) {
            this.info = info;
        }

        public void run() {
            mEthEnabler.getManager().updateDevInfo(info);
            mEthEnabler.setEthEnabled();

            finish();
        }

    }

    public void updateDevNameList(String[] DevList) {
        if (DevList != null) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, android.R.layout.simple_spinner_item, DevList);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            mDevList.setAdapter(adapter);
        }

    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		handle_saveconf	();
	}

}
