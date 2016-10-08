package jp.pioneer.ceam.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.pioneer.ceam.MediaManager.WifiManager.WifiClient;
import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

public class SysManagerWIFIAdmin {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;

	private static SysManagerWIFIAdmin m_instance = null;
	private WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	// scanedAPList
	private List<ScanResult> mWifiList;
	// savedAPList
	private List<WifiConfiguration> mWifiConfiguration;
	private static Context m_context;

	private WifiClient mWifiClient = WifiClient.getDefaultClient();

	public static final int TYPE_NO_PASSWD = 1;
	public static final int TYPE_WEP = 2;
	public static final int TYPE_WPA = 3;

	public static final int WIFI_CONNECTED = 0x01;
	public static final int WIFI_CONNECT_FAILED = 0x02;
	public static final int WIFI_CONNECTING = 0x03;

	private SysManagerWIFIAdmin(Context context) {
		m_context = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	public static SysManagerWIFIAdmin getInstance(Context context) {
		if (null == m_instance) {
			SysLog.out(TAG, "SysManagerWIFIAdmin getInstance", "create singleton instance");
			synchronized (SysManagerWIFIAdmin.class) {
				if (null == m_instance) {
					m_instance = new SysManagerWIFIAdmin(context);
				}
			}
		}
		return m_instance;
	}

	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiClient.setWifiEnabled(true);
		}
	}

	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiClient.setWifiEnabled(false);
		}
	}

	public int checkState() {
		return mWifiManager.getWifiState();
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public synchronized List<WifiConfiguration> getConfiguration() {
		// saved AP
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
		return mWifiConfiguration;
	}

	/**
	 * 获取当前已连接的热点的信息
	 * 
	 * @return
	 */
	public synchronized WifiInfo getConnectionInfo() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		// SysLog.out(TAG, "connect", "" + mWifiInfo.toString());
		return mWifiInfo;
	}

	// connect savedAP
	public boolean connectConfiguration(int netid) {
		if (netid < -1) {
			return false;
		}
		SysLog.out(TAG, "SysManagerWIFIAdmin enableNetwork", "start");
		boolean c = mWifiManager.enableNetwork(netid, true);
		return c;
	}

	public synchronized void saveConfiguration() {
		SysLog.out(TAG, "SysManagerWIFIAdmin saveConfiguration", "start");
		mWifiManager.saveConfiguration();
	}

	public synchronized void startScan() {
		SysLog.out(TAG, "SysManagerWIFIAdmin startScan", "start");
		mWifiManager.startScan();
	}

	public synchronized List<ScanResult> getWifiList() {
		// scaned AP
		mWifiList = mWifiManager.getScanResults();
		return mWifiList;
	}

	public synchronized String getMacAddress() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiInfo = mWifiManager.getConnectionInfo();
			return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
		}
		return null;
	}

	public synchronized String getBSSID() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiInfo = mWifiManager.getConnectionInfo();
			return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
		}
		return "";
	}

	public synchronized String getSSID() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiInfo = mWifiManager.getConnectionInfo();
			return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
		}
		return "";
	}

	public synchronized int getIPAddress() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiInfo = mWifiManager.getConnectionInfo();
			return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
		}
		return 0;
	}

	public synchronized int getNetworkId() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiInfo = mWifiManager.getConnectionInfo();
			return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
		}
		return 0;
	}

	public synchronized boolean deleteConfiguration(int netId) {
		SysLog.out(TAG, "SysManagerWIFIAdmin deleteConfiguration", "start");
		boolean b = mWifiManager.removeNetwork(netId);
		return b;
	}

	public synchronized int addNetwork(String ssid, String passwd, int type,
			String bssid) {
		SysLog.out(TAG, "SysManagerWIFIAdmin addNetwork", "start");
		int status = addNetwork(CreateWifiInfo(ssid, passwd, type, bssid));
		return status;
	}
	
	// add net and connect
	private int addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		SysLog.out(TAG, "SysManagerWIFIAdmin addNetwork(enableNetwork)",
				"start"+", netID = "+wcgID);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
		getConnectionInfo();
		return wcgID;
	}

	private WifiConfiguration CreateWifiInfo(String SSID, String Password,
			int Type, String BSSID) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID =  toHexString(SSID.getBytes()) ;
		SysLog.out(TAG, "SysManagerWIFIAdmin addNetwork", "SSID="+config.SSID);
		
		if (null == BSSID) {
		} else {
			config.BSSID = BSSID;
		}
		if ((null != SSID)&&(null != BSSID)) {
			WifiConfiguration tempConfig = this.IsExsits(SSID,BSSID);
			if (tempConfig != null) {
				mWifiManager.removeNetwork(tempConfig.networkId);
			}
		}

		if (TYPE_NO_PASSWD == Type) // WIFICIPHER_NOPASS
		{
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		} else if (TYPE_WEP == Type) { // WIFICIPHER_WEP
			config.hiddenSSID = true;
			int len = Password.length();
			if ((len == 10 || len == 26 || len == 58)
					&& Password.matches("[0-9A-Fa-f]*")) {
				config.wepKeys[0] = Password;
			} else {
				config.wepKeys[0] = '"' + Password + '"';
			}
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (TYPE_WPA == Type) { // WIFICIPHER_WPA
			if (Password.matches("[0-9A-Fa-f]{64}")) {
				config.preSharedKey = Password;
			} else {
				config.preSharedKey = '"' + Password + '"';
			}

			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		} else {

		}
		return config;
	}

	/**
	 * @param context
	 * @return
	 */
	public int isWifiContected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (wifiNetworkInfo.isConnected()) {
			return WIFI_CONNECTED;
		} else {
			return WIFI_CONNECT_FAILED;
		}
	}

	public WifiConfiguration IsExsits(String SSID,String BSSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (null != BSSID && (("\"" + SSID + "\"").equals(existingConfig.SSID))&&(BSSID.equals(existingConfig.BSSID))) {
				return existingConfig;
			}
		}
		return null;
	}

	public synchronized void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		if(netId == getNetworkId()){
			mWifiManager.disconnect();
			SysLog.out(TAG, "SysManagerWIFIAdmin disconnectWifi(netID)",
					"Ap is disconnect = "+netId);
		}
	}

	public synchronized void disconnectWifi() {
		mWifiManager.disconnect();
	}

	public synchronized void startWps(int setup, String pin) {
		mWifiClient.startWps(setup, pin);
	}

	public synchronized void cancelWps() {
		mWifiClient.cancelWps();
	}

	/**
	 * when AP is connected,set the priority
	 * 
	 * @param netId
	 * @param priorityVal
	 * @return boolean
	 */
	public synchronized boolean setAutoConnectPriority(int netId,
			int priorityVal) {
		return mWifiClient.setAutoConnectPriority(netId, priorityVal);
	}

	/**
	 * get the the priority of connectedAP
	 * 
	 * @param netid
	 * @return int
	 */
	public synchronized int getAutoConnectPriority(int netid) {
		return mWifiClient.getAutoConnectPriority(netid);
	}

	public synchronized boolean getAutoConnectStatus(int netId) {
		boolean status = mWifiClient.getAutoConnectStatus(netId);
		return status;
	}

	public synchronized boolean setAutoConnectStatus(int netId, boolean enabled) {
		boolean status = mWifiClient.setAutoConnectStatus(netId, enabled);
		return status;
	}

	/**
	 * get the oldestAP
	 * 
	 * @return the netID for oldestAP
	 */
	public synchronized int getOldestNetWorkId() {
		return mWifiClient.getOldestNetwork();
	}

	public int getLatestPriority(List<WifiConfiguration> list) {
		ArrayList<Integer> priorityList = new ArrayList<Integer>();
		if (null == list) {
			return 0;
		} else {
			if (0 == list.size()) {
				return 0;
			} else {
				for (WifiConfiguration ap : list) {
					int temp = getAutoConnectPriority(ap.networkId);
					priorityList.add(temp);
				}
				int latestPriority = Collections.min(priorityList);
				SysLog.out(TAG, "getLatestPriority", ""+latestPriority);
				priorityList.clear();
				return latestPriority;
			}
		}
	}

	/**
	 * signalLevel divide
	 * 
	 * @param level
	 * @param num
	 * @return signal level(0~4)
	 */
	public int calculateSignalLevel(int level, int num) {
		if (level <= -100) {
			return 0;
		} else if (level >= -55) {
			return num - 1;
		} else {
			float inputRange = ((-55) - (-100));
			float outputRange = num - 1;
			return (int) ((float) (level - (-100)) * outputRange / inputRange);
		}
	}

	/**
	 * switch IP Address
	 * 
	 * @param i
	 * @return String
	 */
	public String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	/**
	 * 
	 * @param bssid
	 * @return
	 */
	public String changeFormateBSSID(String bssid) {
		String[] strarray = bssid.split(":");
		String Bssid = "";
		for (int i = 0; i < strarray.length; i++) {
			String s = strarray[i].trim();
			if(null != s){
				if (i < strarray.length - 1) {
					Bssid = Bssid + changeStr(s) + "-";
				} else {
					Bssid = Bssid + changeStr(s);
				}
			}
		}
		return Bssid;
	}

	private static long lastClickTime;

	public static boolean isFastDoubleClick() {
		long time = SystemClock.uptimeMillis();
		if (time - lastClickTime < 400) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/**
	 * 
	 * @param bssid
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private String changeStr(String bssid) {
		bssid.toUpperCase();
		char[] ch = bssid.toCharArray();
		StringBuffer sb = new StringBuffer();
		int a = 'A' - 'a';
		for (int i = 0; i < ch.length; i++) {
			if ('a' <= ch[i] && ch[i] <= 'z') {
				ch[i] = (char) (ch[i] + a);
			}
			sb.append(ch[i]);
		}
		return sb.toString();

	}
	
	public String toHexString(byte[] bytes) {
		String hexSSID = "";
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			hexSSID = hexSSID + hex;
		}

		return hexSSID;
	}

	public int getTimestamp(int netId) {
		return mWifiClient.getTimestamp(netId);
	}

	public boolean dealAPExist(int netID, List<WifiConfiguration> m_configList) {
		if (-1 <= netID) {
			if (null != m_configList) {
				for (WifiConfiguration config : m_configList) {
					if (netID == config.networkId) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean dealAPExistByBssidSsid(String ssid, String bssid,
			List<WifiConfiguration> m_configList) {
		if ((null != bssid) && (null != ssid)) {
			if (null != m_configList) {
				for (WifiConfiguration config : m_configList) {
					if ((bssid.equals(config.BSSID))
							&& (ssid.equals(config.SSID))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void sendManuallyNetworkBroadcast(int netId, Context mContext) {
		Intent intent = new Intent();
		intent.setAction("jp.pioneer.ceam.liveinfo.Server.action.network_type_manual");
		intent.putExtra("netId", netId);
		if (null != mContext) {
			mContext.sendBroadcast(intent);
		}
	}

}
