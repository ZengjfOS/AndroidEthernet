package com.fsl.ethernet;

import android.app.Application;

public class EthernetApp extends Application {
	private EthernetEnabler mEthEnabler;

	public EthernetEnabler getmEthEnabler() {
		return mEthEnabler;
	}

	public void setmEthEnabler(EthernetEnabler mEthEnabler) {
		this.mEthEnabler = mEthEnabler;
	}

	
}
