package com.aplex.ethernet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver{
	/**
	 * 背景知识：当Android启动时，会发出一个系统广播，内容为ACTION_BOOT_COMPLETED，
	 * 它的字符串常量表示为 android.intent.action.BOOT_COMPLETED。只要在程序中“捕捉”
	 * 到这个消息，再启动之即可。记住，Android框架说：Don''t call me, I''ll call you back。
	 * 我们要做的是做好接收这个消息的准备，而实现的手段就是实现一个BroadcastReceiver。
	 */
	static final String action_boot="android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(action_boot)){
		/*
            Intent ootStartIntent=new Intent(context, MainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.e("aplex receive test", "get Theme BroadcastReceiver");
            //跳到MainActivity.class中去运行
            context.startActivity(ootStartIntent);
            */
        //}
		
		new EthernetManager(context).resetInterface();
	}

}
