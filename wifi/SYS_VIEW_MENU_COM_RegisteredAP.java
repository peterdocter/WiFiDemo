package jp.pioneer.ceam.view.menucommunication.wifi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import jp.pioneer.ceam.util.Timer;
import jp.pioneer.ceam.util.TimerTask;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.Interface.common.IListItemButtonGroupListener4UI;
import jp.pioneer.ceam.ctl.Interface.common.IMessageActionListener;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewAdapterBase;
import jp.pioneer.ceam.ctl.common.CTL_Constant.TitleBar_Layout_Pattern;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_16AVHN_L_710_wifi_Button;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_16AVHN_L_710_wifi_Button.CTL_14AJ_L_710_List_Button_ID;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView.ListWidthType;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar.CTL_16AJ_L_004_0_Titlebar_ButtonId;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver.WIFIConnectListener;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver.WiFIStatusListener;
import jp.pioneer.ceam.manage.common.SysManagerCommon.OnTimeOutListener;
import jp.pioneer.ceam.manage.common.SysManagerCommonIF;
import jp.pioneer.ceam.manage.message.MessageControlIF;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.uicommonlib.constant.MessageConstant.MsgErrorType;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_WifiAPUtil.ScreenType;
import jp.pioneer.ceam.view.mysetup.MysetupUtils;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SYS_VIEW_MENU_COM_RegisteredAP extends SysView {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private Context m_Context;

	private static final int MENU_LIST_PAGE_COUNT = 5;
	private static final int DISCONNECTING = 0;
	private static final int DISCONNECTED = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;

	private static final int MESSAGEOK = 0;
	private static final int MESSAGENG = 1;
	private static final int MESSAGEOPENMESSAGE = 2;
	
	private static final String DEFAULTBSSID = "00:00:00:00:00:00";

	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_16AVHN_L_004_Titlebar m_titleBar = null;
	private CTL_14AJ_L_600_ListView m_listView = null;
	private SysManagerWIFIAdmin m_wifiAdmin = null;
	private SysMangerWIFIBroadcastReceiver m_broadCastReceiver = null;

	private Timer m_timer = null;
	private TimerTask m_task = null;

	// main count
	private ListViewAdapter m_listAdapter = null;
	// AttributeSet
	private AttributeSet m_attrs = null;
	private boolean m_isSSID = true;
	
	private boolean m_isConnecting = false;
	private boolean m_isManually = false;
	
	private volatile int itempos = -1;
	private float m_scroll_selection = 0;
	// SysViewCommonInfoMenuSettings
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;
	
	// registeredAP list
	private List<WifiRegisteredAP> m_registeredAP = new ArrayList<WifiRegisteredAP>();
	private List<ScanResult> m_scannedAP = new ArrayList<ScanResult>();
	private HashMap<String, String> m_scanList = new HashMap<String, String>(); // (BSSID,SSID)
	private HashMap<String, Integer> m_scanLevelList = new HashMap<String, Integer>(); // (BSSID,level)

	private WiFIStatusListener m_wifiStatusListener = new WiFIStatusListener() {

		@Override
		public void ipChange() {

		}

		@Override
		public void macChange() {
			// TODO Auto-generated method stub

		}

		@Override
		public void buttonStatusChange() {
			// TODO Auto-generated method stub

		}

		@Override
		public void isConnected(int satus) {
			switch (satus) {
			case DISCONNECTING:
				SysLog.out(TAG, "COM_RegisteredAP  WiFIStatusListener",
						"disconnecting");
				setTitleButtonEnable(m_registeredAP,
						m_broadCastReceiver.isResult());
				m_listView.getList().notifyDataSetChanged();
				break;
			case DISCONNECTED:
				SysLog.out(TAG, "COM_RegisteredAP WiFIStatusListener",
						"disconnected");
				m_isConnecting = false;
				m_isManually = false;
				setTitleButtonEnable(m_registeredAP,
						m_broadCastReceiver.isResult());
				m_handler.removeMessages(MESSAGEOK);
				removeTimer();
				m_wifiAdmin.startScan();
				startTimer();
				m_listView.getList().notifyDataSetChanged();
				break;
			case CONNECTING:
				int connecting_networkID=m_wifiAdmin.getNetworkId();
				SysLog.out(TAG, "COM_RegisteredAP WiFIStatusListener",
						"connectingNetID:"+connecting_networkID);
				itempos = netIDToPosition(connecting_networkID);
				setTitleButtonEnable(m_registeredAP,
						false);
				m_handler.removeMessages(MESSAGEOK);
				removeTimer();
				if(!m_isManually){
					SysManagerCommonIF.instance().dealWifiConnectTimeOut();
				}
				m_listView.getList().notifyDataSetChanged();
				break;
			case CONNECTED:
				SysLog.out(TAG, "COM_RegisteredAP WiFIStatusListener",
						"connected");
				m_isConnecting = false;
				m_isManually = false;
				SysLog.out(TAG, "COM_RegisteredAP WiFIStatusListener",
						"connected");
				setTitleButtonEnable(m_registeredAP,
						m_broadCastReceiver.isResult());
				m_handler.removeMessages(MESSAGEOK);
				removeTimer();
				m_wifiAdmin.startScan();
				startTimer();
				m_listView.getList().notifyDataSetChanged();
				break;

			default:
				break;
			}

		}

	};

	private WIFIConnectListener m_wifiConnectListener = new WIFIConnectListener() {
		
		@Override
		public void isConnectSuccess(boolean isSuccess) {
			if(!isSuccess){
				m_isConnecting=false;
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						setTitleButtonEnable(m_registeredAP,
								true);
					}
				},600);
				
				m_listView.getList().notifyDataSetChanged();
			}
		}
	};
	
	private Handler m_handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGEOK:
				getUpdateList();
				m_listView.getList().notifyDataSetChanged();
				SysLog.out(
						TAG,
						"COM_RegisteredAP handleMessage",
						"current time:"
								+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
										.format(new Date()));
				break;
			case MESSAGEOPENMESSAGE:
				MessageControlIF.getIntance().openMessageForSysView(
						"WIFI_11_T1", MsgErrorType.ERROR_HAVE, null);
				break;
			default:
				break;
			}
		};
	};

	public SYS_VIEW_MENU_COM_RegisteredAP(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_RegisteredAP onCreate", "onCreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_com_registeredaccesspoint, null);
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);

		if (null == m_view_main) {
			return null;
		}

		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		m_broadCastReceiver = SysMangerWIFIBroadcastReceiver.getInstance();

		obtainCtrlViews();

		return m_view_main;
	}

	private void obtainCtrlViews() {
		SysLog.out(TAG, "COM_RegisteredAP obtainCtrlViews", "obtainCtrlViews");
		if (null != m_view_main) {

			m_listView = (CTL_14AJ_L_600_ListView) m_view_main
					.findViewById(R.id.menu_com_wifiaccesspoint_list);
			m_titleBar = (CTL_16AVHN_L_004_Titlebar) m_view_main
					.findViewById(R.id.menu_com_wifiaccesspointsetting_title_bar);
		}

		if (null != m_titleBar) {
			m_titleBar
					.setLayoutPatternID(TitleBar_Layout_Pattern.RETURN_TEXT_ESC);
			if (null != m_titleBar) {
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT)
						.setOnProhibition();
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT)
						.setViewOnProhibitionEnabled(true);
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
						.setOnProhibition();
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
						.setViewOnProhibitionEnabled(true);
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT)
						.setOnProhibition();
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT)
						.setViewOnProhibitionEnabled(true);
			}
		}

		if (null != m_view_main && null != m_listView) {
			XmlPullParser parser1 = m_view_main.getResources().getXml(
					R.layout.menu_com_registeredaccesspoint);
			m_attrs = Xml.asAttributeSet(parser1);
			m_listAdapter = new ListViewAdapter(m_view_main.getContext(),
					m_attrs);
			m_listView.getList().setListOnProhibition();
			m_listView.getList().setAdapter(m_listAdapter);
			m_listView.setListType(ListWidthType.LARGE);
			m_listView.getList().setPageChildrenCount(MENU_LIST_PAGE_COUNT);

		}

		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);

	}

	@Override
	protected void onShow() {
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_screenType(ScreenType.SAVEDVIEW);
		
		m_broadCastReceiver.addWiFIStatusListener(m_wifiStatusListener);
		m_broadCastReceiver.addWIFIConnectListener(m_wifiConnectListener);
		
		if (null != m_listView) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					m_wifiAdmin.startScan();
					getAllNetWorkList();
					int connectingID = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getNetID();
					itempos = netIDToPosition(connectingID);
					SysLog.out(TAG, "Handler", "itempos="+itempos);
					if(m_broadCastReceiver.isResult()){
						setTitleButtonEnable(m_registeredAP,
								true);
						m_isConnecting = false;
					}else{
						setTitleButtonEnable(m_registeredAP,
								false);
						m_isConnecting = true;
					}
					m_listView.getList().notifyDataSetChanged();
				}
			}, 225);
		}
		
		startTimer();
		
		if (null != m_view_main && null != m_commonInfoSetting) {
			SysLog.out(TAG, "COM_RegisteredAP onShow", "setVisibility VISIBLE");
			m_view_main.setVisibility(View.VISIBLE);
		}

		List<WifiConfiguration> m_configList = new ArrayList<WifiConfiguration>();
		List<WifiConfiguration> list = new ArrayList<WifiConfiguration>();
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
						if (null != list) {
							list.add(config);
						}
					}
				}
			}
		}
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
				.setM_configList(list);

		m_scroll_selection = SYS_VIEW_MENU_COM_EntryUtil.getInstance().getM_scroll_selectionForScan();
		m_listView.getList().scrollToRate(m_scroll_selection);

		if (null != m_titleBar) {
			m_titleBar.setTitleText(com.android.internal.R.string.WIFI_01_T1);
			if (MysetupUtils.Instance().getIsToMysetup()) {
				m_titleBar.setButtonVisibility(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC, false);
			} else {
				m_titleBar.setButtonVisibility(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC, true);
			}

			if (null != m_registeredAP) {
				if (0 == m_registeredAP.size()) {
					SysLog.out(TAG, "COM_RegisteredAP onShow",
							"not registered AP");
				}
			}
			
		}

		addViewCtrlListener();
		
		if(null != m_titleBar){
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT).onProhibition();
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE).onProhibition();
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT).onProhibition();
		}
		
	}

	private void addViewCtrlListener() {
		if (null != m_listView && null != listViewListener) {
			m_listView.getList().setButtonListener(listViewListener);
		}
		if (null != m_titleBar && null != TitleBarListener) {
			m_titleBar.setTitlebarListener(TitleBarListener,
					IButtonActionListener.BUTTON_EVENT.NORMAL, null);
		}

	}
	

	@Override
	protected void onHide() {
		SysLog.out(TAG, "RegisteredAP onHide", "setVisibility VISIBLE");
		m_scroll_selection = m_listView.getList().getScrollBarPosRate();
		SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_scroll_selectionForSave(m_scroll_selection);

		MessageControlIF.getIntance().closeMessageForSysView("WIFI_02_T1");
		MessageControlIF.getIntance().closeMessageForSysView("WIFI_10_T1");
		MessageControlIF.getIntance().closeMessageForSysView("WIFI_12_T1");
		
        boolean isRestore = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isRestore();
        SysLog.out(TAG, "RegisteredAP onHide", "isRestore="+isRestore);
		
		if (null != m_listView) {
			m_listView.getList().setButtonListener(null);
		}
		
		if (null != m_titleBar) {
			m_titleBar.removeListener();
		}
		
		m_isConnecting = false;
		
		removeTimer();
		
		if (m_handler != null) {
			m_handler.removeMessages(MESSAGEOPENMESSAGE);
			m_handler.removeMessages(MESSAGEOK);
		}

		m_broadCastReceiver.removeWiFIStatusListener(m_wifiStatusListener);
		m_broadCastReceiver.removeWIFIConnectListener(m_wifiConnectListener);
	}

	@Override
	protected void onHideFinished() {
		super.onHideFinished();
		if (null != m_commonInfoSetting) {
			m_commonInfoSetting.setForwardViewChanged(false);
		}

	}

	/**
	 * listView listener
	 */
	private IListItemButtonGroupListener4UI listViewListener = new IListItemButtonGroupListener4UI() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #getButtonMode(int, java.util.List)
		 */
		@Override
		public int getButtonMode(int buttonChildId, List<Integer> time) {
			if (buttonChildId == CTL_14AJ_L_710_List_Button_ID.PROFILEBUTTON) {
				time.add(0, 500);
				return IButtonActionListener.BUTTON_EVENT.LONG;
			}
			if (buttonChildId == CTL_14AJ_L_710_List_Button_ID.DELETEBUTTON) {
				time.add(0, 500);
				return IButtonActionListener.BUTTON_EVENT.LONG;
			} else {

				return CTL_14AJ_L_710_List_Button_ID.LISTBUTTON;
			}
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
//			itempos = position;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.Interface.common.
		 * IListItemButtonGroupListener4UI #OnItemRelease(java.lang.String, int,
		 * int)
		 */
		@SuppressWarnings("static-access")
		@Override
		public void OnItemRelease(String key, int position, int id) {
			final int pos = position;
			if (id == CTL_14AJ_L_710_List_Button_ID.PROFILEBUTTON) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				int netid = m_registeredAP.get(position).getmConfig().networkId;
				if (getAutoConnectStatus(position)) {
					VoicePlayer.getInstance(m_Context).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_LOW,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);
					m_wifiAdmin.setAutoConnectStatus(netid, false);
				} else {
					VoicePlayer.getInstance(m_Context).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_HIGH,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);
					m_wifiAdmin.setAutoConnectStatus(netid, true);
				}
				m_wifiAdmin.saveConfiguration();
				m_listView.getList().notifyDataSetChanged();
//				SysLog.out(TAG, "RegisteredAP AutoConnectBtn",
//						"AutoConnectStatus: "
//								+ WifiClient.getDefaultClient()
//										.getAutoConnectStatus(netid));
			} else if (id == CTL_14AJ_L_710_List_Button_ID.DELETEBUTTON) {
				if (SysManagerWIFIAdmin.getInstance(m_Context)
						.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (position == m_registeredAP.size()) {
					return;
				}
				SysLog.out(TAG, "RegisteredAP Message", "delete push ");
				MessageControlIF.getIntance().openMessageForSysView(
						"WIFI_10_T1", MsgErrorType.ERROR_HAVE,
						new IMessageActionListener() {

							@Override
							public boolean OnStatusChange(String msgId,
									int state) {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean OnKeyEvent(String msgId,
									int keyCode, int keyAction, int pressedCount) {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean OnBackScreenChange(String msgId) {
								// TODO Auto-generated method stub
								return false;
							}

							@Override
							public boolean OnClick(String msgId, int btnIndex) {
								if ("WIFI_10_T1".equals(msgId)) {
									if (MESSAGEOK == btnIndex) {
										VoicePlayer
												.getInstance(m_Context)
												.playbackVoiceById(
														VoicePlayer.VOICE_ID_BEEP_1_HIGH,
														VoicePlayer.PRIORITY_CALL_AND_BEEP);
										
										m_wifiAdmin.disconnectWifi(m_registeredAP.get(pos).getmConfig().networkId);
										
										boolean isDeleteSuccess = m_wifiAdmin
												.deleteConfiguration(m_registeredAP
														.get(pos).getmConfig().networkId);

										if (isDeleteSuccess) {
											SysLog.out(TAG, "isDeleteSuccess ",
													"true");
											m_wifiAdmin.saveConfiguration();
											m_registeredAP.remove(pos);
										} else {
											SysLog.out(TAG, "isDeleteSuccess ",
													"flase");
											m_handler
													.sendEmptyMessage(MESSAGEOPENMESSAGE);
										}
										setTitleButtonEnable(m_registeredAP,
												SysMangerWIFIBroadcastReceiver
														.getInstance()
														.isResult());
										m_listView.getList()
												.notifyDataSetChanged();
										SYS_VIEW_MENU_COM_WifiAPUtil
												.getInstance()
												.setM_registeredAP(
														m_registeredAP);
									}

									if (MESSAGENG == btnIndex) {
										VoicePlayer
												.getInstance(m_Context)
												.playbackVoiceById(
														VoicePlayer.VOICE_ID_BEEP_1_LOW,
														VoicePlayer.PRIORITY_CALL_AND_BEEP);
										MessageControlIF.getIntance()
												.closeMessageForSysView(
														"WIFI_10_T1");
									}
								}

								return false;
							}
						});
			} else {
				onItemClickForIDPosition(id, position);
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
		 * int, int, int[])
		 */
		@Override
		public boolean OnItemRepeatClick(String key, int position, int id,
				int... param) {
			return false;
		}

	};

	/**
	 * @param id
	 * @param position
	 */
	private void onItemClickForIDPosition(int id, int position) {
		if (isFastDoubleClick()) {
			return;
		}
		WifiRegisteredAP wrAP = m_registeredAP.get(position);
		VoicePlayer.getInstance(m_Context).playbackVoiceById(
				VoicePlayer.VOICE_ID_BEEP_1_HIGH,
				VoicePlayer.PRIORITY_CALL_AND_BEEP);
//		if ((m_wifiAdmin.getBSSID()).equals(wrAP.getmConfig().BSSID)) {
		if(!(m_wifiAdmin.getBSSID()).equals(DEFAULTBSSID)){
			m_wifiAdmin.disconnectWifi();
			SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition", "itempos="+itempos);
			if(0 <= itempos){
				CTL_16AVHN_L_710_wifi_Button item=(CTL_16AVHN_L_710_wifi_Button)m_listView.getList().getItemAtPos(itempos);
				if(null != item){
					item.setImage(CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL, 0);
					item.setDisConnect();
				}
			}
			SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
					"position="+position+", disconnect: "  + wrAP.getmConfig().SSID);
			
		} else {
			if (-100 >= wrAP.getSignalLevel()) {
				SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
						"AP is InActive ,signal=" + wrAP.getSignalLevel());
				m_wifiAdmin.disconnectWifi();
				String temp = String.format(m_Context.getResources().getString(
						com.android.internal.R.string.WIFI_18_T1));
				MessageControlIF.getIntance().openMessageForSysView(
						"WIFI_18_T1", temp, MsgErrorType.ERROR_HAVE, null);
			} else {
				boolean con = m_wifiAdmin.connectConfiguration(wrAP
						.getmConfig().networkId);		
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setNetID(
						wrAP.getmConfig().networkId);
				m_wifiAdmin.sendManuallyNetworkBroadcast(
						wrAP.getmConfig().networkId, m_Context);// zy
				SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
						"position="+position+", connect: " + wrAP.getmConfig().SSID);
				
				SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
						"startConnect"+">con"+con);
				
				if (con) {
					
					final CTL_16AVHN_L_710_wifi_Button item=(CTL_16AVHN_L_710_wifi_Button)m_listView.getList().getItemAtPos(position);
					item.setImage(CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL, 0);
					
					SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
							"item="+item.isConnect());
					
					setTitleButtonEnable(m_registeredAP,
							false);
					item.startConnectAnimation();
					m_isConnecting=true;
					
					SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
							"item="+item.isConnect());
					
					SysManagerCommonIF.instance().dealWifiConnectTimeOut();
					
					m_isManually = true;
					
					SysManagerCommonIF.instance().m_managerCommon.setOnTimeOutListener(new OnTimeOutListener() {
						@Override
						public void func() {
							SysLog.out(TAG, "onItemClickForIDPosition", "isTimeOut");
							m_isConnecting = false;
							m_isManually = false;
							setTitleButtonEnable(m_registeredAP, true);
							m_listView.getList().notifyDataSetChanged();
							SysLog.out(TAG, "onItemClickForIDPosition", "m_isConnecting="+m_isConnecting);
						}
					});
					
					
				} else {
					
					SysLog.out(TAG, "COM_RegisteredAP onItemClickForIDPosition",
							"startMessage");
					
					
					SysLog.out(TAG,
							"COM_RegisteredAP onItemClickForIDPosition",
							"connect failure");
					String temp = String
							.format(m_Context.getResources().getString(
									com.android.internal.R.string.WIFI_18_T1));
					MessageControlIF.getIntance().openMessageForSysView(
							"WIFI_18_T1", temp, MsgErrorType.ERROR_HAVE, null);
				}
			}

		}
		m_listView.getList().notifyDataSetChanged();
	}

	private IButtonActionListenerLongRepeat TitleBarListener = new IButtonActionListenerLongRepeat() {

		@Override
		public void OnRelease(int id) {

		}

		@Override
		public void OnPush(int id) {
			SysLog.out(TAG, "COM_RegisteredAP TitleBarListener",
					"m_registeredAP.size()=" + m_registeredAP.size());
			if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BACK == id) {
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_LOW,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_RegisteredAP OnReturnBtnReleased", "BACK");
				if (null == m_commonInfoSetting) {
					return;
				}
				if (m_commonInfoSetting.isForwardViewChanged()) {
				} else {
					m_commonInfoSetting.setForwardViewChanged(true);
					if (MysetupUtils.Instance().getIsToMysetup()) {
						backDefaultViewChange(ViewID.SYS_VIEW_ID_MYSETUP_WIFI);
						return;
					}
					backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_TOPMENU);
				}
			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC == id) {
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_RegisteredAP OnESCBtnReleased", "ESC");
				exit(VType.SYS_VIEW_TYPE_MENU_COM);
			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT == id) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (m_registeredAP.size() >= 2) {
					MessageControlIF.getIntance().openMessageForSysView(
							"WIFI_12_T1", MsgErrorType.ERROR_HAVE,
							priorityMessageListener);
				}

			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE == id) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (m_isSSID) {
					m_isSSID = false;
				} else {
					m_isSSID = true;
				}
				m_listAdapter.notifyDataSetChanged();

			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT == id) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if ((m_registeredAP.size()) == 20) {

					MessageControlIF.getIntance().openMessageForSysView(
							"WIFI_02_T1", MsgErrorType.ERROR_HAVE,
							searchMessageListener);

				} else {

					if (null != m_commonInfoSetting) {
						if (m_commonInfoSetting.isForwardViewChanged()) {
						} else {
							m_commonInfoSetting.setForwardViewChanged(true);
							forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
						}
					}
				}

			} else {

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

	private class ListViewAdapter extends
			CTL_Control_ListViewAdapterBase<CTL_16AVHN_L_710_wifi_Button> {

		/**
		 * @param context
		 * @param attrs
		 */
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
			if (m_registeredAP == null) {
				return 0;
			}
			if (m_registeredAP.size() > 20) {
				return 20;
			} else {
				return m_registeredAP.size();
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
				CTL_16AVHN_L_710_wifi_Button item) {
			item.setOnProhibition();
			if (m_broadCastReceiver.isResult()) {
				item.setViewOnProhibitionEnabled(true);
				SysLog.out(
						TAG,"fillview++++","m_isConnecting="+m_isConnecting+",itempos="+itempos);
				if(!m_isConnecting){
					item.stopConnectAnimation();	
				}else{
					item.setViewOnProhibitionEnabled(false);
					item.getButton(CTL_14AJ_L_710_List_Button_ID.PROFILEBUTTON)
					.setViewOnProhibitionEnabled(true);
					item.getButton(CTL_14AJ_L_710_List_Button_ID.DELETEBUTTON)
					.setViewOnProhibitionEnabled(true);
				}
				
			} else {
				SysLog.out(
						TAG,"fillview++++","m_isConnecting="+m_isConnecting+",itempos="+itempos);
				item.setViewOnProhibitionEnabled(false);
				item.getButton(CTL_14AJ_L_710_List_Button_ID.PROFILEBUTTON)
						.setViewOnProhibitionEnabled(true);
				item.getButton(CTL_14AJ_L_710_List_Button_ID.DELETEBUTTON)
						.setViewOnProhibitionEnabled(true);
				if (itempos == position) {
					item.startConnectAnimation();
					m_isConnecting = true;
				} else {
					item.stopConnectAnimation();
				}
			}
			
			if (null != m_commonInfoSetting) {
				if (null != m_registeredAP && m_registeredAP.size() > 0) {
					WifiRegisteredAP m_config = m_registeredAP.get(position);
					int signal = m_config.getSignalLevel();
					String bssid = m_config.getmConfig().BSSID;
					String ssid = m_config.getmConfig().SSID;
					
					SysLog.out(
							TAG,
							"COM_RegisteredAP  fillview",
							"m_registeredAP.size()= "
									+ m_registeredAP.size()
									+ ", position = "
									+ position
									+ ", netID="
									+ m_registeredAP.get(position).getmConfig().networkId
//									+ ", priority="
//									+ m_wifiAdmin
//											.getAutoConnectPriority(m_registeredAP
//													.get(position).getmConfig().networkId)
//									+ ", AutoStatus="
//									+ WifiClient
//											.getDefaultClient()
//											.getAutoConnectStatus(
//													m_registeredAP
//															.get(position)
//															.getmConfig().networkId)
									+ ", signal=" + signal);

					if (m_config != null
							&& (m_config.getmConfig().wepKeys) != null
							&& (m_config.getmConfig().wepKeys.length > 0)) {
						item.setViewVisible(View.VISIBLE);
						item.setButtonVisible(true);
						item.setImageVisibility(
								CTL_16AVHN_L_710_wifi_Button.IMAGEID_LOCK, true);
						item.setImageVisibility(
								CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
								true);
						item.setONOFF(getAutoConnectStatus(position));
						item.setImage(
								CTL_16AVHN_L_710_wifi_Button.IMAGEID_LOCK,
								setImageBySecurity(
										m_config.getmConfig().allowedKeyManagement,
										m_config.getmConfig().wepKeys[0],
										m_config));

						SysLog.out(
								TAG,"fillview----","isConnect="+item.isConnect()+">isDisConnect="+item.isDisConnect());
						if(item.isConnect()||item.isDisConnect()){
							item.setImage(
									CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
									0);
						}else{
							item.setImage(
									CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
									setImageBylevel(signal, bssid, ssid));
						}
						
						if (m_isSSID) {
							item.setText(
									CTL_16AVHN_L_710_wifi_Button.TEXTID_NAME,
									dealSSID(m_config.getmConfig().SSID));
						} else {
							item.setText(
									CTL_16AVHN_L_710_wifi_Button.TEXTID_NAME,
									m_wifiAdmin.changeFormateBSSID(bssid));
						}

					}
				}
			}
			item.setViewActiveDisable(false);
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
				CTL_16AVHN_L_710_wifi_Button item) {
			item.setButtonContentEmpty();
			item.setViewNormal();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #getNewT()public void
		 * setProcessOrResult(boolean isResult) { //
		 * this.isResult = isResult; // }
		 */
		@Override
		public CTL_16AVHN_L_710_wifi_Button getNewT() {
			return new CTL_16AVHN_L_710_wifi_Button(m_Context, m_Attrs);
		}

	}

	private IMessageActionListener priorityMessageListener = new IMessageActionListener() {

		@Override
		public boolean OnClick(String msgId, int btnIndex) {
			if ("WIFI_12_T1".equals(msgId)) {
				switch (btnIndex) {
				case MESSAGEOK:
					VoicePlayer.getInstance(m_Context).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_HIGH,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);

					if (null != m_commonInfoSetting) {
						if (m_commonInfoSetting.isForwardViewChanged()) {
						} else {
							m_commonInfoSetting.setForwardViewChanged(true);
							List<WifiRegisteredAP> list=new ArrayList<WifiRegisteredAP>();
							for(WifiRegisteredAP ap:m_registeredAP){
								list.add(ap);
							}
							SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
									.setM_registeredAP(list);
							
							forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_PRIORITYADJUST);
						}
					}

					break;
				default:
					break;
				}
			}

			return false;
		}

		@Override
		public boolean OnStatusChange(String msgId, int state) {
			// TODO Auto-generated method stub
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

	private IMessageActionListener searchMessageListener = new IMessageActionListener() {

		@Override
		public boolean OnClick(String msgId, int btnIndex) {
			if ("WIFI_02_T1".equals(msgId)) {
				switch (btnIndex) {
				case MESSAGEOK:
					VoicePlayer.getInstance(m_Context).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_HIGH,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);
					if (null != m_commonInfoSetting) {
						if (m_commonInfoSetting.isForwardViewChanged()) {
						} else {
							int oldNetId = m_wifiAdmin.getOldestNetWorkId();
							if(0 <= oldNetId){
								m_wifiAdmin.deleteConfiguration(oldNetId);
								m_wifiAdmin.saveConfiguration();
								m_commonInfoSetting.setForwardViewChanged(true);
								forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
							}else{
								SysLog.out(TAG, "COM_RegisteredAP getOldestNetWorkId", "failure");
							}
						}
					}

					break;
				case MESSAGENG:
					VoicePlayer.getInstance(m_Context).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_LOW,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);
					MessageControlIF.getIntance().closeMessageForSysView(
							"WIFI_02_T1");

					break;
				default:
					break;
				}
			}
			return false;
		}

		@Override
		public boolean OnStatusChange(String msgId, int state) {
			// TODO Auto-generated method stub
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

	private int setImageBySecurity(BitSet allowedKeyManagement, String wepKeys,
			WifiRegisteredAP config) {
		if ((config.getmConfig().allowedKeyManagement.get(KeyMgmt.NONE))
				&& (wepKeys == null)) {
			return com.android.internal.R.drawable.p10606_wifiunlocked;
		}
		return com.android.internal.R.drawable.p10605_wifilocked;
	}
	
	private synchronized void getUpdateList(){
		
		m_scanList.clear();
		m_scanLevelList.clear();
		
		m_registeredAP = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
				.getM_registeredAP();
		m_scannedAP = m_wifiAdmin.getWifiList();
		if(null != m_scannedAP){
			for (ScanResult result : m_scannedAP) {
				String Bssid = result.BSSID;
				String Ssid = result.SSID;
				int level = result.level;
				m_scanList.put(Bssid, Ssid);
				m_scanLevelList.put(Bssid, level);
			}
		}
		
		if(null != m_registeredAP){
			for (int i = 0; i < m_registeredAP.size(); i++) {
				String apbssid = m_registeredAP.get(i).getmConfig().BSSID;
				String apssid = m_registeredAP.get(i).getmConfig().SSID;
				if ((null != apbssid) && (null != apssid)) {
					String ssid = m_scanList.get(apbssid);
//					SysLog.out(TAG, "COM_RegisteredAP m_registeredAP",
//							"apbssid=" + apbssid + ", apssid=" + apssid
//									+ ", ssid=" + ssid);
					if (dealSSID(apssid).equals(ssid)) {
						int level = m_scanLevelList.get(apbssid);
						m_registeredAP.get(i).setSignalLevel(level);
					} else {
						m_registeredAP.get(i).setSignalLevel(-100);
					}
				}
			}
			
		}

		if(m_isConnecting||(!m_broadCastReceiver.isResult())){
			setTitleButtonEnable(m_registeredAP,
					false);
		}else{
			setTitleButtonEnable(m_registeredAP,
					true);
		}
		
		SysLog.out(TAG, "COM_RegisteredAP  UpdateList",
				m_registeredAP.size() + "");
	}

	private synchronized void getAllNetWorkList() {
		if (m_scannedAP == null) {
			m_scannedAP = new ArrayList<ScanResult>();
		}
		m_scannedAP.clear();
		m_registeredAP.clear();

		m_scanList.clear();
		m_scanLevelList.clear();

		m_scannedAP = m_wifiAdmin.getWifiList();
		WifiRegisteredAP m_wifiRegisteredAP = null;
		List<WifiConfiguration> configuration = m_wifiAdmin.getConfiguration();
		if (null != configuration) {

			for (WifiConfiguration config : configuration) {
				if (null != config) {
					SysLog.out(TAG, "COM_RegisteredAP getAllNetWorkList",
							"SSID=" + config.SSID + ", BSSID=" + config.BSSID);

					int timeStamp = m_wifiAdmin.getTimestamp(config.networkId);
					String bssid = config.BSSID;
					if ((null != bssid) && (0 < timeStamp)) {
						m_wifiRegisteredAP = new WifiRegisteredAP();
						m_wifiRegisteredAP.setmConfig(config);
						m_wifiRegisteredAP.setSignalLevel(-100);
						m_registeredAP.add(m_wifiRegisteredAP);
					}
				}
			}

			for (ScanResult result : m_scannedAP) {
				String Bssid = result.BSSID;
				String Ssid = result.SSID;
				int level = result.level;
				m_scanList.put(Bssid, Ssid);
				m_scanLevelList.put(Bssid, level);
			}

			for (int i = 0; i < m_registeredAP.size(); i++) {
				String apbssid = m_registeredAP.get(i).getmConfig().BSSID;
				String apssid = m_registeredAP.get(i).getmConfig().SSID;
				if ((null != apbssid) && (null != apssid)) {
					String ssid = m_scanList.get(apbssid);
//					SysLog.out(TAG, "COM_RegisteredAP m_registeredAP",
//							"apbssid=" + apbssid + ", apssid=" + apssid
//									+ ", ssid=" + ssid);
					if (dealSSID(apssid).equals(ssid)) {
						int level = m_scanLevelList.get(apbssid);
						m_registeredAP.get(i).setSignalLevel(level);
					} else {
						m_registeredAP.get(i).setSignalLevel(-100);
					}
				}
			}

		}

		if (SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isPriorityAdjusted()) {
			m_registeredAP = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
					.getM_registeredAP();
			SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPriorityAdjusted(
					false);
		}

		Collections.sort(m_registeredAP, new MyCompartor());

		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_registeredAP(
				m_registeredAP);

		if(m_isConnecting||(!m_broadCastReceiver.isResult())){
			setTitleButtonEnable(m_registeredAP,
					false);
		}else{
			setTitleButtonEnable(m_registeredAP,
					true);
		}

		SysLog.out(TAG, "COM_RegisteredAP  getAllNetWorkList",
				m_registeredAP.size() + "");
	}

	private class MyCompartor implements Comparator {

		@Override
		public int compare(Object arg0, Object arg1) {
			WifiRegisteredAP previousAP = (WifiRegisteredAP) arg0;
			WifiRegisteredAP nextAP = (WifiRegisteredAP) arg1;
			int prepriority = m_wifiAdmin.getAutoConnectPriority(previousAP
					.getmConfig().networkId);
			int nextpriority = m_wifiAdmin.getAutoConnectPriority(nextAP
					.getmConfig().networkId);
			return Integer.valueOf(nextpriority).compareTo(
					Integer.valueOf(prepriority));
		}

	}

	private String dealSSID(String SSID) {
		return SSID.replaceAll("\"", "");
	}

	/**
	 * set image for signalLevel
	 * 
	 * @param level
	 * @return ImageID
	 */
	private int setImageBylevel(int level, String bssid, String ssid) {
		int signal = m_wifiAdmin.calculateSignalLevel(level, 5);
		if ((null != bssid) && (null != ssid)) {
			String BSSID = m_wifiAdmin.getBSSID();
			String SSID = m_wifiAdmin.getSSID();
			if (1 == m_wifiAdmin.isWifiContected(m_Context)) {
				if ((bssid.equals(BSSID)) && (ssid.equals(SSID))) {
					if (0 == signal) {
						return com.android.internal.R.drawable.p10602_wificonnect0;
					} else if (1 == signal) {
						return com.android.internal.R.drawable.p10602_wificonnect1;
					} else if (2 == signal) {
						return com.android.internal.R.drawable.p10602_wificonnect2;
					} else if (3 == signal) {
						return com.android.internal.R.drawable.p10602_wificonnect3;
					} else {
						return com.android.internal.R.drawable.p10602_wificonnect4;
					}
				} else {
					if ((bssid.equals(BSSID)) && (!ssid.equals(SSID))) {
						return com.android.internal.R.drawable.p10601_wifiantenna0;
					}
					if (0 == signal) {
						return com.android.internal.R.drawable.p10601_wifiantenna0;
					} else if (1 == signal) {
						return com.android.internal.R.drawable.p10601_wifiantenna1;
					} else if (2 == signal) {
						return com.android.internal.R.drawable.p10601_wifiantenna2;
					} else if (3 == signal) {
						return com.android.internal.R.drawable.p10601_wifiantenna3;
					} else {
						return com.android.internal.R.drawable.p10601_wifiantenna4;
					}
				}
			} else {
				if (0 == signal) {
					return com.android.internal.R.drawable.p10601_wifiantenna0;
				} else if (1 == signal) {
					return com.android.internal.R.drawable.p10601_wifiantenna1;
				} else if (2 == signal) {
					return com.android.internal.R.drawable.p10601_wifiantenna2;
				} else if (3 == signal) {
					return com.android.internal.R.drawable.p10601_wifiantenna3;
				} else {
					return com.android.internal.R.drawable.p10601_wifiantenna4;
				}
			}

		} else {
			return com.android.internal.R.drawable.p10601_wifiantenna0;
		}
	}

	private boolean getAutoConnectStatus(int postion) {
		int netid = m_registeredAP.get(postion).getmConfig().networkId;
		if (m_wifiAdmin.getAutoConnectStatus(netid)) {
			return true;
		} else {
			return false;
		}
	}

	private void setTitleButtonEnable(List<WifiRegisteredAP> APList,
			boolean isConnect) {
		if (null != APList) {
			if (isConnect) {
				if (APList.size() < 2) {
					m_titleBar.getTitleBarButton(
							CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT)
							.setViewOnProhibitionEnabled(false);
					if (APList.size() == 0) {
						m_titleBar
								.getTitleBarButton(
										CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
								.setViewOnProhibitionEnabled(false);
					} else {
						m_titleBar
								.getTitleBarButton(
										CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
								.setViewOnProhibitionEnabled(true);
					}
				} else {
					m_titleBar.getTitleBarButton(
							CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT)
							.setViewOnProhibitionEnabled(true);
					m_titleBar.getTitleBarButton(
							CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
							.setViewOnProhibitionEnabled(true);
				}
			} else {
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT)
						.setViewOnProhibitionEnabled(false);
				m_titleBar.getTitleBarButton(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
						.setViewOnProhibitionEnabled(true);
			}
			
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT)
					.setViewOnProhibitionEnabled(true);
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BACK)
					.setViewOnProhibitionEnabled(true);
			m_titleBar
					.getTitleBarButton(CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC)
					.setViewOnProhibitionEnabled(true);
		}
	}

	private static long lastClickTime;

	private static boolean isFastDoubleClick() {
		long time = SystemClock.uptimeMillis();
		if (time - lastClickTime < 800) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	private int netIDToPosition(int netID){
		int netid = 0;
		for(int i=0;i<m_registeredAP.size();i++){
			netid = m_registeredAP.get(i).getmConfig().networkId;
			if(netID == netid){
				return i;
			}else{
				continue;
			}
		}
		return -1;
	}
	
	private void removeTimer(){
		if (m_task != null) {
			m_task.cancel();
			m_task = null;
		}
		if (m_timer != null) {
			m_timer.cancel();
			m_timer = null;
		}
	}
	
	private void startTimer(){
		m_timer = new Timer();
		m_task = new TimerTask() {
			@Override
			public void run() {
				m_wifiAdmin.startScan();
				m_handler.sendEmptyMessage(MESSAGEOK);
			}
		};
		if (m_task != null && m_timer != null) {
			m_timer.schedule(m_task, 6000, 6000);
		}
	}
	
}
