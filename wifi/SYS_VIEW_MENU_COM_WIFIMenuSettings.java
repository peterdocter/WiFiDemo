package jp.pioneer.ceam.view.menucommunication.wifi;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import jp.pioneer.ceam.DataManager.DataManager;
import jp.pioneer.ceam.DataManager.DataManagerKeyDef;
import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.Interface.common.IListItemButtonGroupListener4UI;
import jp.pioneer.ceam.ctl.Interface.common.IMessageActionListener;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ImageViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewAdapterBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase.TypeFaceId;
import jp.pioneer.ceam.ctl.common.CTL_CommonUtil;
import jp.pioneer.ceam.ctl.common.CTL_Constant.TitleBar_Layout_Pattern;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_600_Button;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_600_Button.CTL_14AJ_L_600_Button_ID;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_600_Text_Button.CTL_14AJ_L_600_Text_Button_Textid;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_DoubleList;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_14AJ_L_004_1_Titlebar.CTL_14AJ_L_004_1_Titlebar_ButtonId;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver.WiFIStatusListener;
import jp.pioneer.ceam.manage.message.MessageControlIF;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.uicommonlib.TextTheme.TextListConstant.TextStyleID;
import jp.pioneer.ceam.uicommonlib.constant.MessageConstant.MsgErrorType;
import jp.pioneer.ceam.view.commonviewctl.Common_View_CTL_SideBar;
import jp.pioneer.ceam.view.menusystem.restore.SettingRestoreStatus;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SYS_VIEW_MENU_COM_WIFIMenuSettings extends SysView {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;

	// SysViewCommonInfoMenuSettings
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;

	// warning titel text size
	private static final float FLOAT_SIZE_WARNING_TITLE = 26;
	private static final int MENU_LIST_PAGE_COUNT = 3;
	private static final int WIFI_POWER = 1;
	private static final int WIFI_REGISTEREDAP = 2;
	private static final int WIFI_SIMPLESETTING = 3;
	private static final int MESSAGEOK = 0;
	private static final int MESSAGENG = 1;
	private static final int MACLEGALLENGTH=6;
	
	private static final int OPENWIFI=1;
	private static final int CLOSEWIFI=0;
	
	private SysManagerWIFIAdmin m_wifiAdmin;
	private Context m_Context;
	private DataManager m_dataManger;
	private byte[] macAddress = new byte[17];
	@SuppressLint("UseValueOf")
	private static final Integer DATASIZE = new Integer(17);

	private CTL_Control_ViewGroupBase m_viewDsp = null;
	private CTL_Control_ViewGroupBase m_view = null;
	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_16AVHN_L_004_Titlebar m_titleBar = null;
	private Common_View_CTL_SideBar m_sideBar = null;
	private CTL_Control_TextViewBase m_macAddressText = null;
	private CTL_Control_TextViewBase m_ipAddressText = null;
	private CTL_Control_TextViewBase m_macAddressData = null;
	private CTL_Control_TextViewBase m_ipAddressData = null;


	private CTL_14AJ_L_600_DoubleList m_listView = null;
	private ListViewAdapter m_mAdapter = null;
	private int[] m_wifiListItem = null;
	private AttributeSet m_attrs = null;
	private int m_wifiStatus = 0;

	private CTL_Control_ImageViewBase m_mac_Line = null;
	private CTL_Control_ImageViewBase m_ip_Line = null;
	private CTL_Control_ImageViewBase m_Line = null;
	
	private WiFIStatusListener m_wifiStatusListener = new WiFIStatusListener() {

		@Override
		public void macChange() {
			m_dataManger.GetDataInfo(
					DataManagerKeyDef.SETTING_WIFI_MAC_ADDRESS, macAddress, DATASIZE);
			String macAddressData = getString(macAddress, "utf-8");
			if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin.checkState()) {
				if (judgeMACLegality(getString(macAddress, "utf-8"))) {
					m_macAddressData.setText(macAddressData);
				} else {
					m_macAddressData.setText(m_wifiAdmin.getMacAddress());
					SysLog.out(TAG,
							"COM_WIFIMenu  WiFIStatusListener getMacAddress",
							macAddressData);
					m_dataManger.SetDataInfo(
							DataManagerKeyDef.SETTING_WIFI_MAC_ADDRESS,
							getBytes(m_wifiAdmin.getMacAddress(), "utf-8"), 17);
					SysLog.out(TAG,
							"COM_WIFIMenu  WiFIStatusListener(ON) macAddress",
							macAddressData);
				}
			} else {
				if (judgeMACLegality(getString(macAddress, "utf-8"))) {
					m_macAddressData.setText(macAddressData);
				} else {
					m_macAddressData.setText("");
				}
				SysLog.out(TAG,
						"COM_WIFIMenu  WiFIStatusListener(OFF) macAddress",
						macAddressData);
			}
		}

		@Override
		public void ipChange() {
			if (m_wifiAdmin.WIFI_CONNECTED == m_wifiAdmin
					.isWifiContected(m_Context)) {
				m_ipAddressData.setText(m_wifiAdmin.intToIp(m_wifiAdmin
						.getIPAddress()));
			} else {
				m_ipAddressData
						.setText(com.android.internal.R.string.WIFI_00_T5);
			}

		}

		@Override
		public void buttonStatusChange() {
			if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin.checkState()) {
				m_wifiStatus = com.android.internal.R.string.WIFI_00_S1_01;
				m_listView.getMainList().notifyDataSetChanged();
			} else if (WifiManager.WIFI_STATE_DISABLED == m_wifiAdmin
					.checkState()) {
				m_wifiStatus = com.android.internal.R.string.WIFI_00_S1_02;
				m_listView.getMainList().notifyDataSetChanged();
			} else if (WifiManager.WIFI_STATE_ENABLING == m_wifiAdmin
					.checkState()) {
				m_wifiStatus = com.android.internal.R.string.WIFI_00_S1_02;
				m_listView.getMainList().notifyDataSetChanged();
			} else if (WifiManager.WIFI_STATE_DISABLING == m_wifiAdmin
					.checkState()) {
				m_wifiStatus = com.android.internal.R.string.WIFI_00_S1_01;
				m_listView.getMainList().notifyDataSetChanged();
			} else {

			}

		}

		@Override
		public void isConnected(int satus) {
			// TODO Auto-generated method stub

		}
	};
	

	public SYS_VIEW_MENU_COM_WIFIMenuSettings(Context context) {
		super(context);
		this.m_Context = context;
	}
	
	private Handler m_handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OPENWIFI:
				m_wifiAdmin.openWifi();
				break;
			case CLOSEWIFI:
				m_wifiAdmin.closeWifi();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_WIFIMenu onCreate", "onCreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.wifi_topmenu, null);
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);
		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		m_dataManger = DataManager.getInstance();

		obtainCtrlViews();
		/**when priority is less than 50，reset the priority of registeredList*/
		resetPriority();
		/**when the machine is power on，get the latest priority form conf*/
		setPriorityFromConf();
		
		return m_view_main;
	}

	private void obtainCtrlViews() {
		SysLog.out(TAG, "COM_WIFIMenu obtainCtrlViews", "obtainCtrlViews");
		if (null != m_view_main) {
			m_sideBar = (Common_View_CTL_SideBar) m_view_main
					.findViewById(R.id.menu_wifi_side);
			m_listView = (CTL_14AJ_L_600_DoubleList) m_view_main
					.findViewById(R.id.menu_wifi_toplist);
			m_viewDsp = (CTL_Control_ViewGroupBase) m_view_main
					.findViewById(R.id.dspbase);
			m_view = (CTL_Control_ViewGroupBase) m_view_main
					.findViewById(R.id.menu_view);

			m_macAddressText = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.mac_address);
			m_macAddressData = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.mac_addressdata);
			m_ipAddressText = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.ip_address);
			m_ipAddressData = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.ip_addressdata);

			m_mac_Line = (CTL_Control_ImageViewBase) m_view_main
					.findViewById(R.id.mac_line);
			m_ip_Line = (CTL_Control_ImageViewBase) m_view_main
					.findViewById(R.id.ip_line);
			m_Line = (CTL_Control_ImageViewBase) m_view_main
					.findViewById(R.id.line);
		}

		m_wifiListItem = getWifiListItem();

		if (null != m_view_main && null != m_listView) {
			XmlPullParser parser = m_view_main.getResources().getXml(
					R.layout.wifi_topmenu);
			m_attrs = Xml.asAttributeSet(parser);
			m_mAdapter = new ListViewAdapter(m_view_main.getContext(), m_attrs);
			m_listView.getMainList().setAdapter(m_mAdapter);
			m_listView.getMainList().setPageChildrenCount(MENU_LIST_PAGE_COUNT);
		}

		if (null != m_macAddressText) {
			m_macAddressText.setTextSize(FLOAT_SIZE_WARNING_TITLE);
			m_macAddressText.setTextFont(TypeFaceId.HG);
			m_macAddressText.setTextStyle(TextStyleID.TEXT_010);
			m_macAddressText.setText(com.android.internal.R.string.WIFI_00_T2);
		}
		if (null != m_ipAddressText) {
			m_ipAddressText.setTextSize(FLOAT_SIZE_WARNING_TITLE);
			m_ipAddressText.setTextFont(TypeFaceId.HG);
			m_ipAddressText.setTextStyle(TextStyleID.TEXT_010);
			m_ipAddressText.setText(com.android.internal.R.string.WIFI_00_T3);
		}

		if (null != m_macAddressData) {
			m_macAddressData.setTextSize(FLOAT_SIZE_WARNING_TITLE);
			m_macAddressData.setTextFont(TypeFaceId.HG);
			m_macAddressData.setTextStyle(TextStyleID.TEXT_011);
			m_dataManger.GetDataInfo(
					DataManagerKeyDef.SETTING_WIFI_MAC_ADDRESS, macAddress, DATASIZE);
			if (null != m_wifiAdmin) {
				String macAddressData = getString(macAddress, "utf-8");
				if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin.checkState()) {
					if (judgeMACLegality(macAddressData)) {
						m_macAddressData
								.setText(getString(macAddress, "utf-8"));
					} else {
						m_macAddressData.setText(m_wifiAdmin.getMacAddress());
						SysLog.out(TAG,
								"COM_WIFIMenu  obtainCtrlViews getMacAddress",
								macAddressData);
						m_dataManger.SetDataInfo(
								DataManagerKeyDef.SETTING_WIFI_MAC_ADDRESS,
								getBytes(m_wifiAdmin.getMacAddress(), "utf-8"),
								17);
						SysLog.out(
								TAG,
								"COM_WIFIMenu   macAddress",
								"Mac:"
										+ getBytes(m_wifiAdmin.getMacAddress(),
												"utf-8"));
					}
				} else {
					if (judgeMACLegality(macAddressData)) {
						m_macAddressData
								.setText(macAddressData);
					} else {
						m_macAddressData.setText("");
					}
					SysLog.out(TAG, "COM_WIFIMenu macAddress",
							macAddressData);
				}
			}
		}

		if (null != m_ipAddressData) {
			m_ipAddressData.setTextSize(FLOAT_SIZE_WARNING_TITLE);
			m_ipAddressData.setTextFont(TypeFaceId.HG);
			m_ipAddressData.setTextStyle(TextStyleID.TEXT_011);
		}

		if (null != m_mac_Line) {
			m_mac_Line
					.setBackgroundResource(com.android.internal.R.drawable.p2077_listline);
		}
		if (null != m_ip_Line) {
			m_ip_Line
					.setBackgroundResource(com.android.internal.R.drawable.p2077_listline);
		}
		if (null != m_Line) {
			m_Line.setBackgroundResource(com.android.internal.R.drawable.p2077_listline);
		}

		if (null != m_listView) {
			m_titleBar = m_listView.getTitle();
		}
		if (null != m_titleBar) {
			m_titleBar
					.setLayoutPatternID(TitleBar_Layout_Pattern.BACK_TEXT_ESC);
			m_titleBar.setTitleText(com.android.internal.R.string.WIFI_00_T1);
		}
		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);

	}

	private int[] getWifiListItem() {
		return new int[] { WIFI_POWER, WIFI_REGISTEREDAP, WIFI_SIMPLESETTING };
	}
	@Override
	protected void onShow() {
		SysLog.out(TAG, "COM_WIFIMenu onShow", "onShow");
		if ((WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin.checkState())
				|| (WifiManager.WIFI_STATE_DISABLING == m_wifiAdmin
						.checkState())) {
			m_wifiStatus = com.android.internal.R.string.WIFI_00_S1_01;
			m_listView.getMainList().notifyDataSetChanged();
		}
		if ((WifiManager.WIFI_STATE_DISABLED == m_wifiAdmin.checkState())
				|| (WifiManager.WIFI_STATE_ENABLING == m_wifiAdmin.checkState())) {
			m_wifiStatus = com.android.internal.R.string.WIFI_00_S1_02;
			m_listView.getMainList().notifyDataSetChanged();
		}
		
		List<WifiConfiguration> m_configList = new ArrayList<WifiConfiguration>();
		List<WifiConfiguration> aplist = new ArrayList<WifiConfiguration>();
		m_configList = m_wifiAdmin.getConfiguration();
		if (null != m_configList) {
			for (WifiConfiguration config : m_configList) {
				if (null != config) {
					int netid = config.networkId;
					int timeStamp = m_wifiAdmin.getTimestamp(netid);
					if (0 >= timeStamp || null == config.BSSID) {
						m_wifiAdmin.deleteConfiguration(netid);
						m_wifiAdmin.saveConfiguration();
					} else {
						if (null != aplist) {
							aplist.add(config);
						}
					}
				}
			}
		}
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
				.setM_configList(aplist);
		
		CTL_Control_ListViewBase list = m_listView.getMainList();
		jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_RelativeLayout.LayoutParams listLayoutParams = (jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_RelativeLayout.LayoutParams) list
				.getLayoutParams();
		CTL_Control_ViewGroupBase.LayoutParams lp_dsp=new CTL_Control_ViewGroupBase.LayoutParams(662,128);
		CTL_Control_ViewGroupBase.LayoutParams lp_textLeftUp=new CTL_Control_ViewGroupBase.LayoutParams(155,32);
		CTL_Control_ViewGroupBase.LayoutParams lp_textRightUp=new CTL_Control_ViewGroupBase.LayoutParams(229,32);
		CTL_Control_ViewGroupBase.LayoutParams lp_lineUp=new CTL_Control_ViewGroupBase.LayoutParams(1,23);
		CTL_Control_ViewGroupBase.LayoutParams lp_textLeftDown=new CTL_Control_ViewGroupBase.LayoutParams(155,32);
		CTL_Control_ViewGroupBase.LayoutParams lp_textRightDown=new CTL_Control_ViewGroupBase.LayoutParams(229,32);
		CTL_Control_ViewGroupBase.LayoutParams lp_lineDown=new CTL_Control_ViewGroupBase.LayoutParams(1,23);
		
		m_sideBar.setVisibility(View.VISIBLE);
		m_titleBar.setVisibility(View.VISIBLE);
		m_titleBar.setTitleText(com.android.internal.R.string.WIFI_00_T1);
		listLayoutParams.width = 701;
		listLayoutParams.leftMargin = 119 - CTL_CommonUtil.getXOffSet();
		lp_dsp.width = 668;
		lp_dsp.leftMargin = 116;
		lp_dsp.topMargin = 336;
		lp_textLeftUp.leftMargin = 139;
		lp_textLeftUp.topMargin = 22;
		lp_textRightUp.leftMargin = 307;
		lp_textRightUp.topMargin = 22;
		lp_lineUp.leftMargin = 294;
		lp_lineUp.topMargin = 29;
		lp_textLeftDown.leftMargin = 139;
		lp_textLeftDown.topMargin = 66;
		lp_textRightDown.leftMargin = 307;
		lp_textRightDown.topMargin = 66;
		lp_lineDown.leftMargin = 294;
		lp_lineDown.topMargin = 73;

		m_viewDsp.setLayoutParams(lp_dsp);
		list.setLayoutParams(listLayoutParams);
		m_macAddressText.setLayoutParams(lp_textLeftUp);
		m_macAddressData.setLayoutParams(lp_textRightUp);
		m_mac_Line.setLayoutParams(lp_lineUp);
		
		m_ipAddressText.setLayoutParams(lp_textLeftDown);
		m_ipAddressData.setLayoutParams(lp_textRightDown);
		m_ip_Line.setLayoutParams(lp_lineDown);
		
		
		if (m_wifiAdmin.WIFI_CONNECTED == m_wifiAdmin
				.isWifiContected(m_Context)) {
			m_ipAddressData.setText(m_wifiAdmin.intToIp(m_wifiAdmin
					.getIPAddress()));
		} else {
			m_ipAddressData.setText(com.android.internal.R.string.WIFI_00_T5);
		}

		SysLog.out(TAG, "COM_WIFIMenu onShow",
				m_wifiAdmin.intToIp(m_wifiAdmin.getIPAddress()));

		addViewCtrlListener();
		SysMangerWIFIBroadcastReceiver.getInstance().addWiFIStatusListener(
				m_wifiStatusListener);
	}

	private void addViewCtrlListener() {
		if (null != m_titleBar && null != TitleBarListener) {
			m_titleBar.setTitlebarListener(TitleBarListener,
					IButtonActionListener.BUTTON_EVENT.NORMAL, null);
		}
		if (null != m_sideBar) {
			m_sideBar
					.registerTab(Common_View_CTL_SideBar.SIDE_BAR_COM_SELECTED);
		}
		if (null != m_listView && null != listItemListener) {
			m_listView.setButtonListener(listItemListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.pioneer.ceam.manage.viewcontrol.SysView#onShowFinished()
	 */
	@Override
	protected void onShowFinished() {
		SysLog.out(TAG, "COM_WIFIMenu onShowFinished", "onShowFinished");
	}
	
	@Override
	protected void onHide() {
		SysLog.out(TAG, "COM_WIFIMenu onHide",
				"setVisibility VISIBLE ");
		boolean flag=SettingRestoreStatus.getIntance().getRestoreStatus(SettingRestoreStatus.Item.WIFISETTINGS);
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setRestore(flag);
		SysLog.out(TAG, "COM_WIFIMenu onHide ","isRestore="+flag);
		 if(flag){
			 SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_scroll_selectionForSave(0);
			 SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_scroll_selectionForScan(0);
			 
			 /**deal with by FW?*/
//			 byte[] byOutputData = new byte[4];
//			 byOutputData = intToBytes(0);
//			 int ret = m_dataManger.SetDataInfo(
//						DataManagerKeyDef.DATA_MGR_SETTING_WIFI_KEYDISPLAYSTATUS, 
//						byOutputData, 4);
//			 SysLog.out(TAG, "COM_WIFIMenu onHide ","ret="+ret);
		 }
		
		if (null != m_sideBar) {
			m_sideBar.unRegisterTab();
		}
		if(null!=m_listView){
			m_listView.removeButtonListener();
		}
		if(null!=m_commonInfoSetting){
			m_commonInfoSetting.setForwardViewChanged(false);
		}
		
		if (null != m_titleBar) {
			m_titleBar.removeListener();
		}

		SysMangerWIFIBroadcastReceiver.getInstance().removeWiFIStatusListener(
				m_wifiStatusListener);

	}

	@Override
	protected void onHideFinished() {
		super.onHideFinished();
	}
	
	private class ListViewAdapter extends
			CTL_Control_ListViewAdapterBase<CTL_14AJ_L_600_Button> {
		public ListViewAdapter(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #getCount(java.lang.String)
		 */
		@Override
		protected int getCount(String key) {
			if (null != m_wifiListItem) {
				return m_wifiListItem.length;
			} else {
				return 0;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #fillView(java.lang.String, int,
		 * android.view.View)
		 */
		@Override
		public boolean fillView(String key, int position,
				CTL_14AJ_L_600_Button item) {
			SysLog.out(TAG, "COM_WIFIMenu fillView",
					"fillView ");
			item.setImageV(true);
//			item.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.CONTENT, 24, 51, 382, 31);
//			item.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS, 24, 521, 150, 31);
			
			item.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.CONTENT, 23, 51, 382, 30);
			item.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS, 23, 522, 148, 30);
			item.setOnProhibition();
			if (null != m_commonInfoSetting && null != m_wifiListItem) {
				if (position < m_wifiListItem.length) {
					switch (m_wifiListItem[position]) {
					case WIFI_POWER:
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.CONTENT,
								com.android.internal.R.string.WIFI_00_L1);
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								m_wifiStatus);
						if ((WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin
								.checkState())
								|| (WifiManager.WIFI_STATE_DISABLED == m_wifiAdmin
										.checkState())) {
							item.setViewActiveDisable(false);
						} else {
							item.setViewActiveDisable(true);
						}
						break;
					case WIFI_REGISTEREDAP:
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.CONTENT,
								com.android.internal.R.string.WIFI_00_L2);
						if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin
								.checkState()) {
							item.setViewOnProhibitionEnabled(true);
						} else {
							item.setViewOnProhibitionEnabled(false);
						}
						break;
					case WIFI_SIMPLESETTING:
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.CONTENT,
								com.android.internal.R.string.WIFI_00_L3);
						if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin
								.checkState()) {
							item.setViewOnProhibitionEnabled(true);
						} else {
							item.setViewOnProhibitionEnabled(false);
						}
						break;
					default:
						break;
					}

				} else {
					return false;
				}
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #fillEmptyView(java.lang.String, int,
		 * android.view.View)
		 */
		@Override
		public void fillEmptyView(String key, int position,
				CTL_14AJ_L_600_Button item) {
			item.setButtonContentEmpty();
			item.setViewNormal();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #getNewT()
		 */
		@Override
		public CTL_14AJ_L_600_Button getNewT() {
			return new CTL_14AJ_L_600_Button(m_Context, m_Attrs);
		}

	}

	/**
	 * ListItemListener
	 */
	private IListItemButtonGroupListener4UI listItemListener = new IListItemButtonGroupListener4UI() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #getButtonMode(int, java.util.List)
		 */
		@Override
		public int getButtonMode(int buttonChildId, List<Integer> time) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #OnItemPush(java.lang.String, int,
		 * int)
		 */
		@Override
		public void OnItemPush(String key, int position, int id) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #OnItemRelease(java.lang.String, int,
		 * int)
		 */
		@Override
		public void OnItemRelease(String key, int position, int id) {
			if (CTL_14AJ_L_600_Button_ID.LISTITME == id) {
				if (m_commonInfoSetting.isForwardViewChanged()) {
					SysLog.out(TAG, "COM_WIFIMenu OnItemPush", "isForwardViewChanged = TRUE");
				} else {
					switch (m_wifiListItem[position]) {
					case WIFI_POWER:
						if (null != m_wifiAdmin) {
							if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin
									.checkState()) {
								VoicePlayer
										.getInstance(m_Context)
										.playbackVoiceById(
												VoicePlayer.VOICE_ID_BEEP_1_LOW,
												VoicePlayer.PRIORITY_CALL_AND_BEEP);
								m_handler.sendEmptyMessage(CLOSEWIFI);
							}
							if ((WifiManager.WIFI_STATE_DISABLED == m_wifiAdmin
									.checkState())) {
								VoicePlayer
										.getInstance(m_Context)
										.playbackVoiceById(
												VoicePlayer.VOICE_ID_BEEP_1_HIGH,
												VoicePlayer.PRIORITY_CALL_AND_BEEP);
								m_handler.sendEmptyMessage(OPENWIFI);
							}
						}

						break;
					case WIFI_REGISTEREDAP:
						VoicePlayer.getInstance(m_Context).playbackVoiceById(
								VoicePlayer.VOICE_ID_BEEP_1_HIGH,
								VoicePlayer.PRIORITY_CALL_AND_BEEP);
						if (null != m_commonInfoSetting) {

							m_commonInfoSetting.setForwardViewChanged(true);
							forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
						}
						break;
					case WIFI_SIMPLESETTING:
						VoicePlayer.getInstance(m_Context).playbackVoiceById(
								VoicePlayer.VOICE_ID_BEEP_1_HIGH,
								VoicePlayer.PRIORITY_CALL_AND_BEEP);
						List<WifiConfiguration> config= SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_configList();
						if(null!=config){
							if ((config.size()) == 20) {

								MessageControlIF.getIntance()
										.openMessageForSysView("WIFI_02_T1", MsgErrorType.ERROR_HAVE,
												messageListener);

							} else {

								if (null != m_commonInfoSetting) {

									m_commonInfoSetting.setForwardViewChanged(true);
									forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SIMPLESETTING);
								}
							}
						}
						
						break;
					default:
						break;
					}
				}
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #OnItemLongClick(java.lang.String,
		 * int, int, int)
		 */
		@Override
		public boolean OnItemLongClick(String key, int position, int id,
				int time) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #OnItemRepeatClick(java.lang.String,
		 * int, int, int, int)
		 */
		@Override
		public boolean OnItemRepeatClick(String key, int position, int id,
				int... param) {
			return false;
		}

	};

	private IButtonActionListenerLongRepeat TitleBarListener = new IButtonActionListenerLongRepeat() {

		@Override
		public void OnRelease(int id) {

		}

		@Override
		public void OnPush(int id) {
			switch (id) {
			case CTL_14AJ_L_004_1_Titlebar_ButtonId.BACK:
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_LOW,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_WIFIMenu OnReturnBtnReleased", "BACK");
				backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_TOPMENU);
				break;
			case CTL_14AJ_L_004_1_Titlebar_ButtonId.ESC:
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_WIFIMenu OnESCBtnReleased", "ESC");
				exit(VType.SYS_VIEW_TYPE_MENU_COM);
				break;
			default:
				break;
			}

		}

		@Override
		public boolean OnRepeat(int id, int... param) {
			return false;
		}

		@Override
		public boolean OnLongClick(int id, int time) {
			return false;
		}
	};

	private IMessageActionListener messageListener = new IMessageActionListener() {

		@Override
		public boolean OnClick(String msgId, int btnIndex) {
			if("WIFI_02_T1".equals(msgId)){
				switch (btnIndex) {
				case MESSAGEOK:
					VoicePlayer.getInstance(m_Context).playbackVoiceById(VoicePlayer.VOICE_ID_BEEP_1_HIGH, VoicePlayer.PRIORITY_CALL_AND_BEEP);
					if (null != m_commonInfoSetting) {
						if(m_commonInfoSetting.isForwardViewChanged()){
						}else{
							int oldNetId=m_wifiAdmin.getOldestNetWorkId();
							if(0 <= oldNetId){
								m_wifiAdmin.deleteConfiguration(oldNetId);
								m_wifiAdmin.saveConfiguration();
								m_commonInfoSetting.setForwardViewChanged(true);
								forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SIMPLESETTING);
							}else{
								SysLog.out(TAG, "COM_WIFIMenu getOldestNetWorkId", "failure");
							}
						}
					}
					break;
				case MESSAGENG:
					VoicePlayer.getInstance(m_Context).playbackVoiceById(VoicePlayer.VOICE_ID_BEEP_1_LOW, VoicePlayer.PRIORITY_CALL_AND_BEEP);
					MessageControlIF.getIntance().closeMessageForSysView("WIFI_02_T1");
					
					break;

				default:
					break;
				}
			}
			return false;
		}

		@Override
		public boolean OnStatusChange(String msgId, int state) {
			// TODO Auto-generated
			// method stub
			return false;
		}
		@Override
		public boolean OnKeyEvent(String msgId, int keyCode, int keyAction,
				int pressedCount) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean OnBackScreenChange(String msgId) {
			// TODO Auto-generated method stub
			return false;
		}


	};

	/**
	 * String to byte[]
	 * 
	 * @param data
	 * @param charsetName
	 * @return byte[]
	 */
	public static byte[] getBytes(String data, String charsetName) {
		byte[] byOutputData = new byte[17];
		if (null == byOutputData) {
			SysLog.out(TAG, "getBytes", "fail to new memory.");
			return null;
		}
		for (int i = 0; i < byOutputData.length; i++) {
			byOutputData[i] = (byte) i;
		}
		Charset charset = Charset.forName(charsetName);
		byOutputData = data.getBytes(charset);
		return byOutputData;
	}
	
	/**
	 * int to byte[]
	 * @param int
	 * @return byte[]
	 */
	private byte[] intToBytes( int value )   
	{   
	    byte[] src = new byte[4];  
	    src[3] =  (byte) ((value>>24) & 0xFF);  
	    src[2] =  (byte) ((value>>16) & 0xFF);  
	    src[1] =  (byte) ((value>>8) & 0xFF);    
	    src[0] =  (byte) (value & 0xFF);                  
	    return src;   
	}  

	/**
	 * byte[] to String
	 * 
	 * @param bytes
	 * @param charsetName
	 * @return String
	 */
	public static String getString(byte[] bytes, String charsetName) {
		return new String(bytes, Charset.forName(charsetName));
	}

	/**
	 * judge the macAddress Legality
	 * 
	 * @param macAddress
	 * @return boolean if true stand for the macAddress is legal,else is illegal
	 */
	private boolean judgeMACLegality(String macAddress) {
		if(null!=macAddress){
			if (macAddress.equals("00:00:00:00:00:00")
					|| macAddress.equalsIgnoreCase("ff:ff:ff:ff:ff:ff")) {
				return false;
			} else {
				String[] strarray = macAddress.split(":");
				if (MACLEGALLENGTH != strarray.length) {
					return false;
				}
			}
		}
		return true;
	}
	
	private void setPriorityFromConf(){
		List<WifiConfiguration> list=SysManagerWIFIAdmin.getInstance(m_Context).getConfiguration();
		SysLog.out(TAG, "setPriorityFromConf", "list="+list);
		if(null!=list){
			if(0==list.size()){
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPriority(0);
			}else{
				int latestPriority=SysManagerWIFIAdmin.getInstance(m_Context).getLatestPriority(list);
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPriority(100-(latestPriority-1));
				SysLog.out(TAG, "setPriorityFromConf", "latestPriority="+latestPriority);
			}
		}else{
			SysLog.out(TAG, "setPriorityFromConf", "WifiConfigurationList is null");
		}
	}
	
	private void resetPriority(){
		List<WifiConfiguration> list=SysManagerWIFIAdmin.getInstance(m_Context).getConfiguration();
		int priority=100;
		int p=SysManagerWIFIAdmin.getInstance(m_Context).getLatestPriority(list);
		if(50>p){
			if(null!=list){
				for(int i=0;i<list.size();i++){
					int id=list.get(i).networkId;
					SysManagerWIFIAdmin.getInstance(m_Context).setAutoConnectPriority(id, priority--);
					SysManagerWIFIAdmin.getInstance(m_Context).saveConfiguration();
				}
			}
		}
	}
}
