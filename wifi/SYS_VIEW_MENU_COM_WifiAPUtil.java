package jp.pioneer.ceam.view.menucommunication.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

public class SYS_VIEW_MENU_COM_WifiAPUtil {
	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private List<WifiRegisteredAP> m_registeredAP=new ArrayList<WifiRegisteredAP>();;
	private List<ScanResult> m_scannedAP=new ArrayList<ScanResult>();
	private List<WifiConfiguration> m_configList=new ArrayList<WifiConfiguration>();
	private HashMap<String , Integer> m_priorityList=new HashMap<String, Integer>();
	private int scannedAPPosition=0;
	private boolean isPriorityAdjusted=false;
	private  int  priority=0;
	private int netID = -1;
	private int m_screenType = 0;
	
	private boolean isWpsFlag = true;
	
	private boolean isRestore=false;
	
	private boolean isPasswordForward = false;
	
	//if machine acc off--on
	private boolean isMachineAccON = false;
	
	public class ScreenType{
		public static final int SAVEDVIEW = 1;
		public static final int SCANNEDVIEW = 2;
		public static final int PASSWORDENTRYVIEW = 3;
		public static final int NETWORADDVIEW = 4;
		public static final int WPSCONNETVIEW = 5;
	}
	
	private static SYS_VIEW_MENU_COM_WifiAPUtil m_instance = null;
	
	public SYS_VIEW_MENU_COM_WifiAPUtil() {
		super();
	}
	
	public static  SYS_VIEW_MENU_COM_WifiAPUtil getInstance() {
		if (null == m_instance) {
			SysLog.out(TAG, "Instance", "create singleton instance");
			synchronized (SYS_VIEW_MENU_COM_WifiAPUtil.class) {
				if (null == m_instance) {
					m_instance = new SYS_VIEW_MENU_COM_WifiAPUtil();
				}
			}
		}
		return m_instance;
	}

	public List<WifiRegisteredAP> getM_registeredAP() {
		return m_registeredAP;
	}

	public void setM_registeredAP(List<WifiRegisteredAP> m_registeredAP) {
		SysLog.out(TAG, "setM_registeredAP", "setM_registeredAP");
		this.m_registeredAP = m_registeredAP;
	}

	public List<ScanResult> getM_scannedAP() {
		return m_scannedAP;
	}

	public void setM_scannedAP(List<ScanResult> m_scannedAP) {
		this.m_scannedAP = m_scannedAP;
	}
	
	public List<WifiConfiguration> getM_configList() {
		return m_configList;
	}

	public void setM_configList(List<WifiConfiguration> m_configList) {
		SysLog.out(TAG, "setM_configList", "m_configList size="+m_configList.size());
		this.m_configList = m_configList;
	}
	
	public HashMap<String, Integer> getM_priorityList() {
		return m_priorityList;
	}

	public void setM_priorityList(HashMap<String, Integer> m_priorityList) {
		this.m_priorityList = m_priorityList;
	}

	public int getScannedAPPosition() {
		return scannedAPPosition;
	}

	public void setScannedAPPosition(int scannedAPPosition) {
		this.scannedAPPosition = scannedAPPosition;
	}

	public boolean isPriorityAdjusted() {
		return isPriorityAdjusted;
	}

	public void setPriorityAdjusted(boolean isPriorityAdjusted) {
		this.isPriorityAdjusted = isPriorityAdjusted;
	}
	
	public synchronized int getPriority() {
		if(100<priority){
			priority=0;
		}
		return priority;
	}

	public synchronized void setPriority(int priority) {
		this.priority = priority;
	}    
	
	public int getNetID() {
		return netID;
	}

	public void setNetID(int netID) {
		this.netID = netID;
	}
	
	public int getM_screenType() {
		return m_screenType;
	}

	public void setM_screenType(int m_screenType) {
		this.m_screenType = m_screenType;
	}

	public boolean isWpsFlag() {
		return isWpsFlag;
	}

	public void setWpsFlag(boolean isWpsFlag) {
		this.isWpsFlag = isWpsFlag;
	}

	public boolean isRestore() {
		return isRestore;
	}

	public void setRestore(boolean isRestore) {
		this.isRestore = isRestore;
	}

	public boolean isPasswordForward() {
		return isPasswordForward;
	}

	public void setPasswordForward(boolean isPasswordForward) {
		this.isPasswordForward = isPasswordForward;
	}

	public boolean isMachineAccON() {
		return isMachineAccON;
	}

	public void setMachineAccON(boolean isMachineAccON) {
		this.isMachineAccON = isMachineAccON;
	}

	
	
}
