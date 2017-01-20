package com.aplex.ethernet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver{
	/**
	 * ����֪ʶ����Android����ʱ���ᷢ��һ��ϵͳ�㲥������ΪACTION_BOOT_COMPLETED��
	 * �����ַ���������ʾΪ android.intent.action.BOOT_COMPLETED��ֻҪ�ڳ����С���׽��
	 * �������Ϣ��������֮���ɡ���ס��Android���˵��Don''t call me, I''ll call you back��
	 * ����Ҫ���������ý��������Ϣ��׼������ʵ�ֵ��ֶξ���ʵ��һ��BroadcastReceiver��
	 */
	static final String action_boot="android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(action_boot)){
		/*
            Intent ootStartIntent=new Intent(context, MainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e("aplex receive test", "get Theme BroadcastReceiver");
            //����MainActivity.class��ȥ����
            context.startActivity(ootStartIntent);
            */
        //}
		
		new EthernetManager(context).resetInterface();
	}

}
