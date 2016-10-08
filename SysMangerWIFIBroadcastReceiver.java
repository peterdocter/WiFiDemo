package jp.pioneer.ceam.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.pioneer.ceam.MediaManager.WifiManager.WifiClient;
import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.manage.common.SysManagerCommonIF;
import jp.pioneer.ceam.manage.message.MessageControlIF;
import jp.pioneer.ceam.manage.viewcontrol.SysViewControlIF;
import jp.pioneer.ceam.uicommonlib.constant.MessageConstant.MsgErrorType;
import jp.pioneer.ceam.uicommonlib.constant.SysViewConstant.SysViewKind;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_WifiAPUtil;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_WifiAPUtil.ScreenType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;

public class SysMangerWIFIBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private static final int DISCONNECTING = 0;
	private static final int DISCONNECTED = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;
	
	private static final int MESSAGETIMEOUT=4;
	private Context m_context=null;
	private  boolean isResult=true;
	
	private static int num=0;
	
	private int netID=0;
	
	private ArrayList<WiFIStatusListener> m_wifiStatusListeners = new ArrayList<SysMangerWIFIBroadcastReceiver.WiFIStatusListener>();
	
	private ArrayList<WIFIConnectListener> m_wifiConnectListeners = new ArrayList<SysMangerWIFIBroadcastReceiver.WIFIConnectListener>();

	private ArrayList<WpsConnectListener> m_WpsConnectListeners=new ArrayList<WpsConnectListener>();
	
	private ArrayList<WpsCancelListener> m_WpsCancelListeners=new ArrayList<WpsCancelListener>();
	
	private static SysMangerWIFIBroadcastReceiver instance = new SysMangerWIFIBroadcastReceiver();
	
	private SysManagerWIFIAdmin m_wifiAdmin=null;
	
	private Handler m_handler = new Handler() {

		public void handleMessage(Message msg) {
			if (MESSAGETIMEOUT == msg.what) {
				netID=SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getNetID();
				SysLog.out(TAG, "SysMangerWIFIBroadcastReceiver Handler", "TimeOut"
							+  ",netID="+ netID);
				dealConnectTimeOut(netID);
			}
		}
	};
	
	public static SysMangerWIFIBroadcastReceiver getInstance() {
		return instance;
	}
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		m_context=arg0;
		m_wifiAdmin=SysManagerWIFIAdmin.getInstance(m_context);
		
		/**get the status of wifi*/
		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(arg1.getAction())) {
			int wifiState = arg1.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			SysLog.out(TAG, "onReceive", "SYS_VIEW_ID_INVALID");
			switch (wifiState) {
			case WifiManager.WIFI_STATE_DISABLED:
				SysLog.out(TAG, "onReceive", "wifi disabled");
				for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
					if (null != m_wifiStatusListener) {
						m_wifiStatusListener.macChange();
						m_wifiStatusListener.buttonStatusChange();
					}
				}
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				SysLog.out(TAG, "onReceive", "wifi disabling");
				for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
					if (null != m_wifiStatusListener) {
						m_wifiStatusListener.buttonStatusChange();
					}
				}
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				SysLog.out(TAG, "onReceive", "wifi enabled");
				for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
					if (null != m_wifiStatusListener) {
						m_wifiStatusListener.macChange();
						m_wifiStatusListener.buttonStatusChange();
					}
				}
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				SysLog.out(TAG, "onReceive", "wifi enabling");
				for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
					if (null != m_wifiStatusListener) {
						m_wifiStatusListener.buttonStatusChange();
					}
				}
				break;
			default:
				break;
			}
		}

		/**get the notification of pwd error*/
		if(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(arg1.getAction())){
			int State = arg1.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
			if(State==WifiManager.ERROR_AUTHENTICATING){
				SysLog.out(TAG,"onReceive" , "wificonnect failure(pwd error)");
				for(WIFIConnectListener listener : m_wifiConnectListeners){
					if(null != listener){
						listener.isConnectSuccess(false);
					}
				}
				netID=SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getNetID();
				SysManagerCommonIF.instance().removeWifiConnectTimeOut();
				dealConnectFailure(netID);
			}
		}
		
		/**get the notification of wpsconnect*/
		if (WifiClient.ACTION_WPS_PROGRESS_CHANGED.equals(arg1.getAction())) {
			int status = arg1.getIntExtra(
					WifiClient.EXTRA_WPS_PROGRESS_STATE, -1);
			SysLog.out(TAG, "onReceive", "ACTION_WPS_PROGRESS_CHANGED  num :"+(++num)+"status:"+status);
			switch (status) {
			case WifiClient.WPS_PROGRESS_START_COMPLETION:
				SysLog.out(TAG, "onReceive", "Wps Completion");
				m_handler.sendEmptyMessageDelayed(MESSAGETIMEOUT, 32000);
				for (WpsConnectListener listener : m_WpsConnectListeners) {
					if (null != listener) {
						listener.onCompletion();
					}
				}
				break;
			case WifiClient.WPS_PROGRESS_START_FAILURE:
				SysLog.out(TAG, "onReceive", "Wps Start Failure");
				dealWpsStartOrCancelFailure();
				
				for (WpsConnectListener listener : m_WpsConnectListeners) {
					if (null != listener) {
						listener.onFailure(0);
					}
				}
				break;
			case WifiClient.WPS_PROGRESS_START_SUCCESS:
				SysLog.out(TAG, "onReceive", "Wps Start Success");
				for (WpsConnectListener listener : m_WpsConnectListeners) {
					if (null != listener) {
						listener.onStartSuccess(arg1.getStringExtra(WifiClient.EXTRA_WPS_PIN));
					}
				}
				break;
			case WifiClient.WPS_PROGRESS_CANCEL_FAILURE:
				SysLog.out(TAG, "onReceive", "Wps Cancel Failure");
				if(SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isWpsFlag()){
					SysManagerIF.instance().closeViewByKind(
							SysViewKind.KIND_WPSMESSAGE);
				}else{
					MessageControlIF.getIntance().closeMessageForSysView(
							"WIFI_16_T1");
				}
				for (WpsCancelListener listener : m_WpsCancelListeners) {
					if (null != listener) {
						listener.onFailure(0);
					}
				}
				break;
			case WifiClient.WPS_PROGRESS_CANCEL_SUCCESS:
				SysLog.out(TAG, "onReceive", "Wps Cancel Success");
				dealWpsStartOrCancelFailure();
				for (WpsCancelListener listener : m_WpsCancelListeners) {
					if (null != listener) {
						listener.onSuccess();
					}
				}
				break;

			default:
				break;
			}
		}

		/**get the notification of  AP connect*/
		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(arg1.getAction())) {  
			Parcelable parcelableExtra = arg1  
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);  
            if (null != parcelableExtra) {  
                NetworkInfo wifi = (NetworkInfo) parcelableExtra;  
                
			if (wifi != null) {
				if (NetworkInfo.State.CONNECTED == wifi.getState()) {
					SysLog.out(TAG, "onReceive", "wifi connected");
					isResult=true;
					SysManagerCommonIF.instance().removeWifiConnectTimeOut();
					
					if(SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isMachineAccON()){          //correct openmessage for wificonnected when machine acc off --on (16-143-1-00294)
						boolean flag = openWIFIConnectedOrFailureMessage("WIFI_17_T1",m_wifiAdmin.getSSID());
						SysLog.out(TAG, "onReceive", "isMachineAccON true,ismessageOpen="+flag);
						SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setMachineAccON(false);
					}else{
						SysLog.out(TAG, "onReceive", "isMachineAccON false");
					}
					
					if(ScreenType.WPSCONNETVIEW == SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_screenType()){
						netID=m_wifiAdmin.getNetworkId();
					}else{
						netID=SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getNetID();
					}
					List<WifiConfiguration> m_configList=SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_configList();
					if((-1<netID)&&(null != m_configList)){
						dealConnectSuccess(netID, m_configList);
					}else{
						SysLog.out(TAG, "onReceive", "netID error or m_configList == null");
					}
					
					for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
						if (null != m_wifiStatusListener) {
							m_wifiStatusListener.ipChange();
							m_wifiStatusListener.isConnected(CONNECTED);
						}
					}
					
				} else if (NetworkInfo.State.DISCONNECTING == wifi.getState()) {
					SysLog.out(TAG, "onReceive", "wifi disconnecting");
					isResult=false;
					for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
						if (null != m_wifiStatusListener) {
							m_wifiStatusListener.isConnected(DISCONNECTING);
						}
					}
					
				} else if (NetworkInfo.State.DISCONNECTED == wifi.getState()) {
					SysLog.out(TAG, "onReceive", "wifi disconnected");
					isResult=true;
					for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
						m_wifiStatusListener.ipChange();
						m_wifiStatusListener.isConnected(DISCONNECTED);
					}
				} else if (NetworkInfo.State.CONNECTING == wifi.getState()) {
					String ssid = m_wifiAdmin.getSSID();
					String bssid = m_wifiAdmin.getBSSID();
					SysLog.out(TAG, "onReceive", "wifi connecting SSID="+ssid+", BSSID= "+bssid);
					isResult = false;
					
					int connecting_networkID = m_wifiAdmin.getNetworkId();
					SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setNetID(connecting_networkID);  
					for (WiFIStatusListener m_wifiStatusListener : m_wifiStatusListeners) {
						if (null != m_wifiStatusListener) {
							m_wifiStatusListener.isConnected(CONNECTING);
						}
					}
				}
			}
           }
		}

	}

	public interface WiFIStatusListener {
		public void ipChange();
		public void macChange();
		public void buttonStatusChange();
		public void isConnected(int satus);
	}

	public interface WIFIConnectListener{
		public void isConnectSuccess(boolean isSuccess);
	}
	
	public interface WpsConnectListener{
		public void onCompletion();
		public void onFailure(int arg0);
		public void onStartSuccess(String arg0);
	}
	
	public interface WpsCancelListener{
		public void onSuccess();
		public void onFailure(int arg0);
	}

	public void addWiFIStatusListener(WiFIStatusListener listener) {
		this.m_wifiStatusListeners.add(listener);
	}
	public void removeWiFIStatusListener(WiFIStatusListener listener) {
		this.m_wifiStatusListeners.remove(listener);
	}
	
	public void addWIFIConnectListener(WIFIConnectListener listener) {
		this.m_wifiConnectListeners.add(listener);
	}
	public void removeWIFIConnectListener(WIFIConnectListener listener) {
		this.m_wifiConnectListeners.remove(listener);
	}

	public void addWpsConnectListener(WpsConnectListener listener){
		this.m_WpsConnectListeners.add(listener);
	}
	public  void removeWpsConnectListener(WpsConnectListener listener){
		this.m_WpsConnectListeners.remove(listener);
	}
	
	public void addWpsCancelListener(WpsCancelListener listener){
		this.m_WpsCancelListeners.add(listener);
	}
	public void removeWpsCancelListener(WpsCancelListener listener){
		this.m_WpsCancelListeners.remove(listener);
	}

	public boolean isResult() {
		return isResult;
	}
	
	private  void dealConnectTimeOut(int netID){
		MessageControlIF.getIntance().closeMessageForSysView(
				"WIFI_05_T1");
		m_handler.removeMessages(MESSAGETIMEOUT);
		SysManagerWIFIAdmin.getInstance(m_context).disconnectWifi(netID);
		int screenType = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_screenType();
		if(ScreenType.WPSCONNETVIEW != screenType){
			if(ScreenType.SAVEDVIEW != screenType){
				int timestamp = SysManagerWIFIAdmin.getInstance(m_context).getTimestamp(netID);
				if(0 >= timestamp){
					m_wifiAdmin.deleteConfiguration(netID);
					m_wifiAdmin.saveConfiguration();
				}
				
				SysLog.out(TAG, "dealConnectTimeOut", "ScreenType = "+screenType);
				setInitScreenType();
				
			}else{
				SysLog.out(TAG, "dealConnectTimeOut", "isSavedView");
			}
			openWIFIConnectedOrFailureMessage("WIFI_18_T1",null);
		}else{
			SysLog.out(TAG, "dealConnectTimeOut", "isWpsConnect");
			setInitScreenType();
			m_wifiAdmin.cancelWps();
			SysManagerIF.instance()
					.closeViewByKind(SysViewKind.KIND_WPSMESSAGE);
			openWIFIConnectedOrFailureMessage("WIFI_18_T1",null);
		}
	}
	
	
	private void dealConnectFailure(int netID){
		MessageControlIF.getIntance().closeMessageForSysView(
				"WIFI_05_T1");
		int screenType = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_screenType();
		if(ScreenType.WPSCONNETVIEW != screenType){
			if(ScreenType.SAVEDVIEW != screenType){
				int timestamp = SysManagerWIFIAdmin.getInstance(m_context).getTimestamp(netID);
				if(0 >= timestamp){
					m_wifiAdmin.deleteConfiguration(netID);
					m_wifiAdmin.saveConfiguration();
				}
				
				SysLog.out(TAG, "dealConnectFailure", "ScreenType = "+screenType);
				setInitScreenType();
				
			}else{
				SysLog.out(TAG, "dealConnectFailure", "isSavedView");
			}
			openWIFIConnectedOrFailureMessage("WIFI_18_T1",null);
			
		}
	}
	
	private void dealWpsStartOrCancelFailure(){
		if(SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isWpsFlag()){
			SysManagerIF.instance().closeViewByKind(
					SysViewKind.KIND_WPSMESSAGE);
		}else{
			MessageControlIF.getIntance().closeMessageForSysView(
					"WIFI_16_T1");
		}
		setInitScreenType();
		openWIFIConnectedOrFailureMessage("WIFI_18_T1",null);
		
	}
	
	@SuppressWarnings("unused")
	private void dealConnectSuccess(int netID,List<WifiConfiguration> m_configList){
		SysLog.out(TAG, "dealConnectSuccess", "netID="+netID+", m_configListSize="+m_configList.size());
		m_handler.removeMessages(MESSAGETIMEOUT);
		String bssid=m_wifiAdmin.getBSSID();
		String ssid=m_wifiAdmin.getSSID();
		switch (SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_screenType()) {
		case ScreenType.NETWORADDVIEW:
			SysLog.out(TAG, "dealConnectSuccess isNetWorkAddView", "true");
			WifiConfiguration config = new WifiConfiguration();
			config.networkId = netID;
			config.BSSID = bssid;
			if(!m_wifiAdmin.dealAPExistByBssidSsid(ssid,bssid, m_configList)){
				SysLog.out(TAG, "dealAPExistByBssid", "false");
				int priority = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
						.getPriority();
				int p = 100 - (priority++);
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
						.setPriority(priority);
				m_wifiAdmin.getWifiManager().updateNetwork(config);
				m_wifiAdmin.setAutoConnectPriority(netID, p);
				m_wifiAdmin.saveConfiguration();
			}
			
			if(hasConnectingOrWpsMessageOpened()){
				closeConnectingMessage();
				SysViewControlIF.instance().forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
			}
			
			break;
		case ScreenType.WPSCONNETVIEW:
			SysLog.out(TAG, "dealConnectSuccess isWpsConnect", "true");
			WifiConfiguration config_wps = new WifiConfiguration();
			config_wps.networkId = m_wifiAdmin.getNetworkId();
			config_wps.BSSID = m_wifiAdmin.getBSSID();
			if(!m_wifiAdmin.dealAPExistByBssidSsid(ssid,bssid, m_configList)){
				SysLog.out(TAG, "dealAPExistByBssid", "false");
				m_wifiAdmin.getWifiManager().updateNetwork(config_wps);
				int priority = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
						.getPriority();
				int p = 100 - (priority++);
				m_wifiAdmin.setAutoConnectPriority(m_wifiAdmin.getNetworkId(),
						p);
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
						.setPriority(priority);
				m_wifiAdmin.setAutoConnectStatus(m_wifiAdmin.getNetworkId(), true);
				m_wifiAdmin.saveConfiguration();
			}
			
			if(hasConnectingOrWpsMessageOpened()){
				closeConnectingMessage();
				SysViewControlIF.instance().forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
			}
			
			break;
		case ScreenType.PASSWORDENTRYVIEW:
			SysLog.out(TAG, "dealConnectSuccess isPassWordEntryView", "true");
		case ScreenType.SCANNEDVIEW:
			SysLog.out(TAG, "dealConnectSuccess isScannedView", "true");
			if(!m_wifiAdmin.dealAPExistByBssidSsid(ssid,bssid, m_configList)){
				SysLog.out(TAG, "dealAPExistByBssid", "false");
				int priority = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
						.getPriority();
				int p = 100 - (priority++);
				m_wifiAdmin.setAutoConnectPriority(netID, p);
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
						.setPriority(priority);
				m_wifiAdmin.saveConfiguration();
			}
			
			if(hasConnectingOrWpsMessageOpened()){
				closeConnectingMessage();
				SysViewControlIF.instance().forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
			}
			
			break;
		case ScreenType.SAVEDVIEW:
			SysLog.out(TAG, "dealConnectSuccess isSavedView", "true");
			if(hasConnectingOrWpsMessageOpened()){
				closeConnectingMessage();
			}
			
			break;

		default:
			SysLog.out(TAG, "dealConnectSuccess WIFI Menu", "true");
			if(hasConnectingOrWpsMessageOpened()){
				closeConnectingMessage();
			}
			
			break;
		}
		
		String temp = String.format(
				m_context.getResources().getString(
						com.android.internal.R.string.WIFI_17_T1),
				ssid);
		openWIFIConnectedOrFailureMessage("WIFI_17_T1",ssid);
		
	}

	
	private void setInitScreenType(){
		SysLog.out(TAG, "setInitScreenType", "setSavedView true");
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_screenType(ScreenType.SAVEDVIEW);
	}
	
	private boolean openWIFIConnectedOrFailureMessage(String messageContent,String ssid) {
		if (null == MessageControlIF.getIntance()) {
			return false;
		}
		String temp = null;
		if (!MessageControlIF.getIntance().isMessageOpened(messageContent)) {
			if(messageContent.equals("WIFI_17_T1")){
				temp = String.format(
						m_context.getResources().getString(
								com.android.internal.R.string.WIFI_17_T1),ssid);
				SysLog.out(TAG, "onRecieve openWIFIConnectedOrFailureMessage","ssid="+ssid);
			}else if(messageContent.equals("WIFI_18_T1")){
				temp = String.format(m_context.getResources().getString(
						com.android.internal.R.string.WIFI_18_T1));
			}else{
				return false;
			}
			MessageControlIF.getIntance().openMessageForSysView(messageContent,
					temp, MsgErrorType.ERROR_HAVE, null);
			SysLog.out(TAG, "onRecieve openWIFIConnectedOrFailureMessage", messageContent
					+ " :OPEN MESSAGE SUCCESS");
			return true;
		} else {
			SysLog.out(TAG, "onRecieve openWIFIConnectedOrFailureMessage", messageContent
					+ ":isOpened");
			return false;
		}
	}
	
	private boolean hasConnectingOrWpsMessageOpened() {
		if (MessageControlIF.getIntance().isMessageOpened("WIFI_05_T1")
				|| MessageControlIF.getIntance().isMessageOpened("WIFI_16_T1")
				|| SysManagerIF.instance().isActiveOfKind(
						SysViewKind.KIND_WPSMESSAGE)) {
			SysLog.out(TAG, "onRecieve hasConnectingOrWpsMessageOpened","true");
			return true;
		} else {
			return false;
		}
	}
	
	private void closeConnectingMessage(){
		MessageControlIF.getIntance().closeMessageForSysView(
				"WIFI_05_T1");
		SysManagerIF.instance().closeViewByKind(SysViewKind.KIND_WPSMESSAGE);
		MessageControlIF.getIntance().closeMessageForSysView("WIFI_16_T1");
	}
}
