package jp.pioneer.ceam.view.menucommunication.wifi;

import android.net.wifi.WifiConfiguration;

public class WifiRegisteredAP {
	private WifiConfiguration mConfig;
	private int signalLevel;
	
	public WifiRegisteredAP() {
		super();
	}
	public WifiConfiguration getmConfig() {
		return mConfig;
	}
	public void setmConfig(WifiConfiguration mConfig) {
		this.mConfig = mConfig;
	}
	public int getSignalLevel() {
		return signalLevel;
	}
	public void setSignalLevel(int signalLevel) {
		this.signalLevel = signalLevel;
	}
	
}
