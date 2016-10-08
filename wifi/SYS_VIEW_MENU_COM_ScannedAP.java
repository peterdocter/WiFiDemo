package jp.pioneer.ceam.view.menucommunication.wifi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import jp.pioneer.ceam.util.TimerTask;
import jp.pioneer.ceam.util.Timer;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.bluetooth.BluetoothAudio;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.Interface.common.IListItemButtonGroupListener4UI;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewAdapterBase;
import jp.pioneer.ceam.ctl.common.CTL_Constant.TitleBar_Layout_Pattern;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_16AVHN_L_710_wifi_Button;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView.ListWidthType;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar.CTL_16AJ_L_004_0_Titlebar_ButtonId;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver.WiFIStatusListener;
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
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SYS_VIEW_MENU_COM_ScannedAP extends SysView {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private Context m_Context;

	private static final int MENU_LIST_PAGE_COUNT = 5;
	public int pos = 0;
	private float m_scroll_selection=0;

	private static final int TYPE_WPAWPA2 = 3;
	private static final int TYPE_WEP = 2;
	private static final int TYPE_NOSECURITY = 1;
	
	private static final int DISCONNECTING = 0;
	private static final int DISCONNECTED = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;

	private static final int TYPE_DEFAULT = 3;

	private static final int MESSAGEAUTOUPDATE = 1;
	
	private static final int PWDNOTDISPLAY=0;

	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_16AVHN_L_004_Titlebar m_titleBar = null;
	private CTL_14AJ_L_600_ListView m_listView = null;
	private SysManagerWIFIAdmin m_wifiAdmin = null;
	private HashMap<String , String> m_registerdList = new HashMap<String , String>();    //(BSSID,SSID)
	// SysViewCommonInfoMenuSettings
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;
	// scannedAP list
	public List<ScanResult> m_scannedAP = new ArrayList<ScanResult>();

	private Timer m_timer = null;
	private TimerTask m_task = null;
	private SysMangerWIFIBroadcastReceiver m_broadCastReceiver = null;

	private static final int ITEM_NORMAL = 0;
	private static final int ITEM_OTHER = 1;

	// adapter
	private ListViewAdapter m_listAdapter = null;
	// AttributeSet
	private AttributeSet m_attrs = null;
	private boolean m_isSSID = true;
	
	private BluetoothAudio m_bluetoothAudio = null;
	
	private WiFIStatusListener m_wifiStatusListener = new WiFIStatusListener() {

		@Override
		public void ipChange() {
			// TODO Auto-generated method stub
			
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
				SysLog.out(TAG, "COM_ScannedAP  WiFIStatusListener",
						"disconnecting");
				break;
			case DISCONNECTED:
				SysLog.out(TAG, "COM_ScannedAP WiFIStatusListener",
						"disconnected");
				m_handler.removeMessages(MESSAGEAUTOUPDATE);
				removeTimer();
				m_wifiAdmin.startScan();
				startTimer();
				break;
			case CONNECTING:
				SysLog.out(TAG, "COM_ScannedAP WiFIStatusListener",
						"connecting");
				m_handler.removeMessages(MESSAGEAUTOUPDATE);
				removeTimer();
				break;
			case CONNECTED:
				SysLog.out(TAG, "COM_ScannedAP WiFIStatusListener",
						"connected");
				m_handler.removeMessages(MESSAGEAUTOUPDATE);
				removeTimer();
				m_wifiAdmin.startScan();
				startTimer();
				break;

			default:
				break;
			}
			
		}
		
	};

	private Handler m_handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGEAUTOUPDATE:
				getAllNetWorkList();
				setInfoButtonStatus(m_scannedAP);
				m_listView.getList().notifyDataSetChanged();
				SysLog.out(
						TAG,
						"COM_ScannedAP handleMessage",
						"current time:"
								+ (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
										.format(new Date()));
				break;

			default:
				break;
			}
		};
	};

	public SYS_VIEW_MENU_COM_ScannedAP(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_ScannedAP onCreate", "onCreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_com_scannedap, null);
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);

		if (null == m_view_main) {
			return null;
		}

		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		m_broadCastReceiver = SysMangerWIFIBroadcastReceiver.getInstance();
		
		m_bluetoothAudio = BluetoothAudio.getDefault();

		obtainCtrlViews();

		return m_view_main;
	}

	private void obtainCtrlViews() {
		if (null != m_view_main) {
			SysLog.out(TAG, "COM_ScannedAP obtainCtrlViews", "obtainCtrlViews");

			m_listView = (CTL_14AJ_L_600_ListView) m_view_main
					.findViewById(R.id.menu_com_wifiscanned_list);
			m_titleBar = (CTL_16AVHN_L_004_Titlebar) m_view_main
					.findViewById(R.id.menu_com_wifiscannedapsetting_title_bar);
		}

		if (null != m_titleBar) {
			m_titleBar
					.setLayoutPatternID(TitleBar_Layout_Pattern.RETURN_TEXT_ESC);
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT, false);
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
					.setOnProhibition();
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT)
					.setOnProhibition();
		}

		if (null != m_view_main && null != m_listView) {
			XmlPullParser parser1 = m_view_main.getResources().getXml(
					R.layout.menu_com_scannedap);
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
		if (null != m_view_main && null != m_commonInfoSetting) {
			SysLog.out(TAG, "COM_ScannedAP onShow", "onShow");
			m_view_main.setVisibility(View.VISIBLE);
		}
		m_broadCastReceiver.addWiFIStatusListener(m_wifiStatusListener);
		
		List<WifiConfiguration> m_configList = new ArrayList<WifiConfiguration>();
		List<WifiConfiguration> list = new ArrayList<WifiConfiguration>();
		m_configList = m_wifiAdmin.getConfiguration();
		
		boolean isPasswordForward = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isPasswordForward();
		if (null != m_configList) {
			for (WifiConfiguration config : m_configList) {
				if (null != config) {
					int netid = config.networkId;
					int timeStamp = m_wifiAdmin.getTimestamp(netid);
					if (0 >= timeStamp || null == config.BSSID) {
						if (!isPasswordForward) {
							m_wifiAdmin.deleteConfiguration(netid);
							m_wifiAdmin.saveConfiguration();
						}
					} else {
						if (null != list) {
							list.add(config);
						}
					}
				}
			}
		}
		
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_configList(list);
		
		if (null != m_listView) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					m_wifiAdmin.startScan();
					getAllNetWorkList(); // get scannedAP List
					setInfoButtonStatus(m_scannedAP);
					m_listView.getList().notifyDataSetChanged();
				}
			}, 225);
		}
		m_scroll_selection = SYS_VIEW_MENU_COM_EntryUtil.getInstance().getM_scroll_selectionForScan();
		m_listView.getList().scrollToRate(m_scroll_selection);
		
		if (null != m_titleBar) {
			m_titleBar.setTitleText(com.android.internal.R.string.WIFI_03_T1);
			if (MysetupUtils.Instance().getIsToMysetup()) {
				m_titleBar.setButtonVisibility(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC, false);
			} else {
				m_titleBar.setButtonVisibility(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC, true);
			}
		
		}

		startTimer();

		addViewCtrlListener();
		
		if(null != m_titleBar){
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE).onProhibition();
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT).onProhibition();
		}
	}
	
	@Override
	protected void onShowFinished() {
		super.onShowFinished();
		boolean isPasswordForward = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().isPasswordForward();
		if(isPasswordForward){
			MessageControlIF.getIntance().openMessageForSysView(
					"WIFI_05_T1", MsgErrorType.ERROR_HAVE, null);
			SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPasswordForward(false);
			SysManagerCommonIF.instance().dealWifiConnectTimeOut();
		}
	}

	private void addViewCtrlListener() {
		SysLog.out(TAG, "COM_ScannedAP  addViewCtrlListener",
				"addViewCtrlListener");
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
		SysLog.out(TAG, "COM_ScannedAP onHide",
				"setVisibility VISIBLE and showAnimation");
		m_scroll_selection=m_listView.getList().getScrollBarPosRate();
		SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_scroll_selectionForScan(m_scroll_selection);
		
		MessageControlIF.getIntance().closeMessageForSysView("WIFI_05_T1");
		
		if (null != m_listView) {
			m_listView.getList().setButtonListener(null);
		}
		if (null != m_titleBar) {
			m_titleBar.removeListener();
		}
		removeTimer();
		if(m_handler != null){
			m_handler.removeMessages(MESSAGEAUTOUPDATE);
		}
		m_broadCastReceiver.removeWiFIStatusListener(m_wifiStatusListener);
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
			onItemClickForIDPosition(id, position);
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

		if (m_listAdapter.getItemViewType(position) == ITEM_OTHER) {
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			SysLog.out(TAG, "COM_ScannedAP listViewListener", "other="
					+ position);
			if (null != m_commonInfoSetting) {
				if (m_commonInfoSetting.isForwardViewChanged()) {
				} else {
					m_commonInfoSetting.setForwardViewChanged(true);
					String nosecurity=String.format(m_Context.getResources().getString(com.android.internal.R.string.WIFI_08_L1));
					SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_SSID("");
					SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_securityText(nosecurity);
					SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_pwdAdd("");
					forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_NETWORKADD);
				}
			}
		} else {
			ScanResult scResult = m_scannedAP.get(position);
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			if (null != scResult) {
				if (TYPE_NOSECURITY == wifiType(scResult.capabilities)) {
					int isconnect = m_wifiAdmin.addNetwork(scResult.SSID, "",
							wifiType(scResult.capabilities), scResult.BSSID);
					m_wifiAdmin.sendManuallyNetworkBroadcast(isconnect, m_Context);//zy
					
					setScreenType();
					
					MessageControlIF.getIntance().openMessageForSysView(
							"WIFI_05_T1", MsgErrorType.ERROR_HAVE, null);
					SysLog.out(TAG, "COM_ScannedAP listViewListener", "netID="
							+ isconnect);
					if (isconnect > -1) {
						int netID = isconnect;
						SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setNetID(netID);
						SysManagerCommonIF.instance().dealWifiConnectTimeOut();
					} else {
						SysLog.out(TAG,
								"COM_ScannedAP onItemClickForIDPosition",
								"connect failure");
						MessageControlIF.getIntance().closeMessageForSysView(
								"WIFI_05_T1");
						String temp = String.format(m_Context.getResources().getString(com.android.internal.R.string.WIFI_18_T1));
						MessageControlIF.getIntance().openMessageForSysView("WIFI_18_T1", temp, MsgErrorType.ERROR_HAVE, null); 
					}
				} else {
					SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
							.setScannedAPPosition(position);
					SYS_VIEW_MENU_COM_EntryUtil.getInstance().setM_entryflag(
							TYPE_DEFAULT);
					SYS_VIEW_MENU_COM_EntryUtil.getInstance()
							.setM_securityTextDefault(
									wifiType(scResult.capabilities));
					SysLog.out(
							TAG,
							"COM_ScannedAP listViewListener",
							"position=" + position + ",SSID="
									+ m_scannedAP.get(position).SSID);
					if (null != m_commonInfoSetting) {
						if (m_commonInfoSetting.isForwardViewChanged()) {
						} else {
							m_commonInfoSetting.setForwardViewChanged(true);
							forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY);
						}
					}
				}
			}
		}
	}

	private IButtonActionListenerLongRepeat TitleBarListener = new IButtonActionListenerLongRepeat() {

		@Override
		public void OnRelease(int id) {

		}

		@Override
		public void OnPush(int id) {

			if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BACK == id) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_LOW,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_ScannedAP OnReturnBtnPush", "BACK");
				backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
				
			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC == id) {
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_ScannedAP OnESCBtnPush", "ESC");
				exit(VType.SYS_VIEW_TYPE_MENU_COM);
			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE == id) {
				SysLog.out(TAG, "COM_ScannedAP OnInfoBtnPush", "Info");
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (0 == m_scannedAP.size()) {
					m_titleBar.getTitleBarButton(
							CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
							.setViewOnProhibitionEnabled(false);
				} else {
					m_titleBar.getTitleBarButton(
							CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
							.setViewOnProhibitionEnabled(true);
					if (m_isSSID) {
						m_isSSID = false;
					} else {
						m_isSSID = true;
					}
				}
				
				m_listView.getList().notifyDataSetChanged();
			} else if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT == id) {
				SysLog.out(TAG, "COM_ScannedAP OnSearchBtnPush", "Search");
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (null != m_commonInfoSetting) {
					if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin
							.checkState()) {
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								m_wifiAdmin.startScan();
								getAllNetWorkList(); // get scannedAP List
								setInfoButtonStatus(m_scannedAP); 
								m_listView.getList().notifyDataSetChanged();
							}
						}, 225);
					} else {
						m_scannedAP.clear();
						m_listView.getList().notifyDataSetChanged();
						SysLog.out(TAG, "COM_ScannedAP OnSearchBtnPush",
								"WIFI Closed");
					}
				}
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

		@Override
		public int getItemViewType(int position) {
			if (m_scannedAP.size() >= 30) {
				if (position == 30) {
					return ITEM_OTHER;
				} else {
					return ITEM_NORMAL;
				}
			} else {
				if (position == m_scannedAP.size()) {
					return ITEM_OTHER;
				} else {
					return ITEM_NORMAL;
				}
			}
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #getCount(java.lang.String)
		 */
		@Override
		protected int getCount(String key) {

			if (m_scannedAP.size() >= 30) {
				return 31;
			}
			return m_scannedAP.size() + 1;

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
			if (null != m_commonInfoSetting) {
				if (null != m_scannedAP && m_scannedAP.size() > 0) {
					if (getItemViewType(position) == ITEM_NORMAL) {
						SysLog.out(TAG, "COM_ScannedAP  fillview", "m_scannedAP.size()="
								+ m_scannedAP.size()+", position="+position+", signal="+m_scannedAP.get(position).level+", ssid="+m_scannedAP.get(position).SSID);
						
						ScanResult scanRsult = m_scannedAP.get(position);
						if (scanRsult != null) {
							if (m_isSSID) {

								item.setButtonVisible(false);
								item.setImageVisibility(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_LOCK,
										true);
								item.setImageVisibility(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
										true);
								item.setImage(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_LOCK,
										setImageBySecurity(wifiType(scanRsult.capabilities)));
								item.setImage(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
										setImageBylevel(scanRsult.level));
								item.setText(
										CTL_16AVHN_L_710_wifi_Button.TEXTID_NAME,
										scanRsult.SSID);
								item.setText(
										CTL_16AVHN_L_710_wifi_Button.TEXTID_WPA,
										dealCapabilities(scanRsult.capabilities));
							} else {

								item.setButtonVisible(false);
								item.setImageVisibility(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_LOCK,
										true);
								item.setImageVisibility(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
										true);
								item.setImage(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_LOCK,
										setImageBySecurity(wifiType(scanRsult.capabilities)));
								item.setImage(
										CTL_16AVHN_L_710_wifi_Button.IMAGEID_SIGNAL,
										setImageBylevel(scanRsult.level));
								item.setText(
										CTL_16AVHN_L_710_wifi_Button.TEXTID_NAME,
										m_wifiAdmin
												.changeFormateBSSID(scanRsult.BSSID));
								item.setText(
										CTL_16AVHN_L_710_wifi_Button.TEXTID_WPA,
										dealCapabilities(scanRsult.capabilities));
							}
						}
					} else {
						item.setButtonOtherLayout();
					}
				}else{
					item.setButtonOtherLayout();
				}
			}
			item.setButtonVisible(false);
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
			if (getItemViewType(position) == ITEM_NORMAL) {
				item.setButtonContentEmpty();
				item.setViewNormal();
			} else {
				item.setButtonOtherLayout();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jp.pioneer.ceam.ctl.basecontrol.viewBase.
		 * CTL_Control_ListViewAdapterBase #getNewT()
		 */
		@Override
		public CTL_16AVHN_L_710_wifi_Button getNewT() {
			return new CTL_16AVHN_L_710_wifi_Button(m_Context, m_Attrs);
		}

	}

	public  synchronized void getAllNetWorkList() {
		if (m_scannedAP == null) {
			m_scannedAP = new ArrayList<ScanResult>();
		}
		m_scannedAP.clear();
		m_registerdList.clear();
		List<ScanResult> result = m_wifiAdmin.getWifiList();
		
		List<WifiConfiguration>  device=SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().getM_configList();
		if (null != device) {
			
			for (WifiConfiguration reAp : device) {

				if (null != reAp) {
					String BSSID = reAp.BSSID;
					String SSID = reAp.SSID;
					int timestamp = m_wifiAdmin.getTimestamp(reAp.networkId);
					if (null != result) {
						for (int j = 0; j < result.size(); j++) {
							ScanResult scAp = result.get(j);
							if (null != scAp) {
								String scBssid = scAp.BSSID;
								String scSsid = scAp.SSID;
								if ((null != scBssid) && (null != scSsid)) {
									if (scBssid.equals(BSSID)
											&& scSsid.equals(dealSSID(SSID))
											&& (0 <= timestamp)) {
										result.remove(j);
										break;
									}
								}

							}

						}

					}

				}

			}
			
		}
		
		for (ScanResult sc : result) {
			if (null != sc) {
				String scSsid = sc.SSID;
				if (((scSsid.length() >= 1) && (scSsid.length() <= 32))
						&& (judgeSSID(scSsid))) {
					m_scannedAP.add(sc);
				}
			}
		}
		
		Collections.sort(m_scannedAP, new MyCompartor());

		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_scannedAP(m_scannedAP);

		setInfoButtonStatus(m_scannedAP);
		
		SysLog.out(TAG, "SYS_VIEW_MENU_COM_ScannedAP  getAllNetWorkList",
				"scannedAPSize=" + m_scannedAP.size());
	}

	private class MyCompartor implements Comparator {

		@Override
		public int compare(Object arg0, Object arg1) {
			ScanResult previousAP = (ScanResult) arg0;
			ScanResult nextAP = (ScanResult) arg1;
			int prelevel = m_wifiAdmin
					.calculateSignalLevel(previousAP.level, 5);
			int nextlevel = m_wifiAdmin.calculateSignalLevel(nextAP.level, 5);
			if (prelevel != nextlevel) {
				return Integer.valueOf(nextlevel).compareTo(
						Integer.valueOf(prelevel));
			} else {
				return previousAP.SSID.compareTo(nextAP.SSID);
			}
		}

	}

	/**
	 * judge ssid whether invisible
	 * 
	 * @param ssid
	 * @return boolean
	 */
	private static boolean judgeSSID(String ssid) {
		char[] contentCharArr = ssid.toCharArray();
		for (int i = 0; i < contentCharArr.length; i++) {
			if (contentCharArr[i] <= 0x20 || contentCharArr[i] == 0x7F) {
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * deal wifi type
	 * 
	 * @param capabilities
	 * @return
	 */
	private int wifiType(String capabilities) {
		if (capabilities.contains("[WPA2-") || capabilities.contains("[WPA-")) {
			return TYPE_WPAWPA2;
		} else if (capabilities.contains("WEP")) {
			return TYPE_WEP;
		} else {
			return TYPE_NOSECURITY;
		}
	}

	/**
	 * deal wifi capabilities
	 * 
	 * @param capabilities
	 * @return String
	 */
	private String dealCapabilities(String capabilities) {
		if (capabilities.contains("[WPA2-") || capabilities.contains("[WPA-")) {
			capabilities = m_Context.getResources().getString(
					com.android.internal.R.string.WIFI_01_Y1_02);
		} else if (capabilities.contains("WEP")) {
			capabilities = m_Context.getResources().getString(
					com.android.internal.R.string.WIFI_01_Y1_01);
		} else {
			capabilities = "";
		}
		return capabilities;
	}

	/**
	 * set image for Security
	 * 
	 * @param wifiType
	 * @return ImageID
	 */
	private int setImageBySecurity(int wifiType) {
		if ((wifiType == TYPE_WPAWPA2) || (wifiType == TYPE_WEP)) {
			return com.android.internal.R.drawable.p10605_wifilocked;
		} else if (wifiType == TYPE_NOSECURITY) {
			return com.android.internal.R.drawable.p10606_wifiunlocked;
		} else {
			return 0;
		}
	}

	/**
	 * set image for signalLevel
	 * 
	 * @param level
	 * @return ImageID
	 */
	private int setImageBylevel(int level) {
		int signal = m_wifiAdmin.calculateSignalLevel(level, 5);
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

	@SuppressWarnings("unused")
	private String dealSSID(String SSID) {
		return SSID.replaceAll("\"", "");
	}
	
	private void setInfoButtonStatus(List<ScanResult> scannedAP){
		if (0 == scannedAP.size()) {
			SysLog.out(TAG, "SYS_VIEW_MENU_COM_ScannedAP  setInfoButtonStatus",
					"no AP");
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
					.setViewOnProhibitionEnabled(false);
		}else{
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
					.setViewOnProhibitionEnabled(true);
		}
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
				m_handler.sendEmptyMessage(MESSAGEAUTOUPDATE);
			}
		};
		if (m_task != null && m_timer != null) {
			m_timer.schedule(m_task, 6000, 6000);
		}
	}
	
	private void setScreenType(){
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_screenType(ScreenType.SCANNEDVIEW);
	}
	
}
