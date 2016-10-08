package jp.pioneer.ceam.view.menucommunication.wifi;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import android.content.Context;

public class SYS_VIEW_MENU_COM_EntryUtil {
	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	public static  SYS_VIEW_MENU_COM_EntryUtil m_instance = null;
	
	public static final int TYPE_SSID=1;
	public static final int TYPE_PWD=2;
	public static final int TYPE_DEFAULT=3;
	
	private String m_SSID="";
	private String m_pwd="";
	private String m_pwdAdd="";
	private String m_securityText="なし";
	
	private int m_securityTextDefault=0;
//	private int m_pwdDisplayFlag=0;
	private int m_entryflag=TYPE_DEFAULT;
	
	private float m_scroll_selectionForSave = 0;
	private float m_scroll_selectionForScan = 0;
	
	public SYS_VIEW_MENU_COM_EntryUtil() {
		super();
	}

	public static SYS_VIEW_MENU_COM_EntryUtil getInstance(){
		if (null == m_instance) {
			SysLog.out(TAG, "Instance", "create singleton instance");
			synchronized(SYS_VIEW_MENU_COM_EntryUtil.class){
				if (null == m_instance) {
					m_instance = new SYS_VIEW_MENU_COM_EntryUtil();
				}
			}
		}
		return m_instance;
	}
	
	public String getM_SSID() {
		return m_SSID;
	}

	public void setM_SSID(String m_SSID) {
		this.m_SSID = m_SSID;
	}

	public String getM_pwd() {
		return m_pwd;
	}

	public void setM_pwd(String m_pwd) {
		this.m_pwd = m_pwd;
	}
	
	public String getM_pwdAdd() {
		return m_pwdAdd;
	}

	public void setM_pwdAdd(String m_pwdAdd) {
		this.m_pwdAdd = m_pwdAdd;
	}

	public int getM_entryflag() {
		return m_entryflag;
	}

	public void setM_entryflag(int m_entryflag) {
		this.m_entryflag = m_entryflag;
	}

	public String getM_securityText() {
		return m_securityText;
	}

	public void setM_securityText(String m_securityText) {
		this.m_securityText = m_securityText;
	}
	
	public int getM_securityTextDefault() {
		return m_securityTextDefault;
	}

	public void setM_securityTextDefault(int m_securityTextDefault) {
		this.m_securityTextDefault = m_securityTextDefault;
	}

	public String getProtectedPwd(String pwd){
		String s="";
		for(int i=0;i<pwd.length();i++){
			s=s+"*";
		}
		return s;
	}

//	public int getM_pwdDisplayFlag() {
//		return m_pwdDisplayFlag;
//	}
//
//	public void setM_pwdDisplayFlag(int m_pwdDisplayFlag) {
//		this.m_pwdDisplayFlag = m_pwdDisplayFlag;
//	}

	public float getM_scroll_selectionForSave() {
		return m_scroll_selectionForSave;
	}

	public void setM_scroll_selectionForSave(float m_scroll_selectionForSave) {
		this.m_scroll_selectionForSave = m_scroll_selectionForSave;
	}

	public float getM_scroll_selectionForScan() {
		return m_scroll_selectionForScan;
	}

	public void setM_scroll_selectionForScan(float m_scroll_selectionForScan) {
		this.m_scroll_selectionForScan = m_scroll_selectionForScan;
	}
	
	
	
}
