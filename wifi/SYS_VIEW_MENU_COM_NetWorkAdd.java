package jp.pioneer.ceam.view.menucommunication.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.pioneer.ceam.DataManager.DataManager;
import jp.pioneer.ceam.DataManager.DataManagerKeyDef;
import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.Interface.common.IListItemButtonGroupListener4UI;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewAdapterBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase.TypeFaceId;
import jp.pioneer.ceam.ctl.common.CTL_Constant.TitleBar_Layout_Pattern;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_071_Button;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_600_Button;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_600_Button.CTL_14AJ_L_600_Button_ID;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_600_Text_Button.CTL_14AJ_L_600_Text_Button_Textid;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_601_Big_Button;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_16AVHN_L_172_CheckButton;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView.ListWidthType;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_601_PopupList.PopupListStage;
import jp.pioneer.ceam.ctl.designControl.Popup.CTL_Control_PopupList;
import jp.pioneer.ceam.ctl.designControl.Popup.CTL_Control_PopupList.PopupLayoutLIst;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_14AJ_L_004_1_Titlebar.CTL_14AJ_L_004_1_Titlebar_ButtonId;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar.CTL_16AJ_L_004_0_Titlebar_ButtonId;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.common.SysManagerCommonIF;
import jp.pioneer.ceam.manage.message.MessageControlIF;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.uicommonlib.TextTheme.TextListConstant.TextStyleID;
import jp.pioneer.ceam.uicommonlib.constant.MessageConstant.MsgErrorType;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_WifiAPUtil.ScreenType;
import jp.pioneer.ceam.view.mysetup.MysetupUtils;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SYS_VIEW_MENU_COM_NetWorkAdd extends SysView {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;
	private SYS_VIEW_MENU_COM_EntryUtil m_entryUtil = null;

	private static final float FLOAT_SIZE_WARNING_TITLE = 26;
	private static final int MENU_LIST_PAGE_COUNT = 3;
	private static final int NETWORKSSID = 1;
	private static final int NETWORKSECURITY = 2;
	private static final int NETWORKPASSWORD = 3;

	private static final int TYPE_WPA = 3;
	private static final int TYPE_WEP = 2;
	private static final int TYPE_NOSECURITY = 1;

	private static final int PWDDISPLAY = 1;
	private static final int PWDNOTDISPLAY = 0;
	
	private String NOSECURITY = null;

	private Context m_Context;
	private DataManager m_dataManger;
	private CTL_Control_ViewGroupBase m_view_main = null;

	private CTL_16AVHN_L_004_Titlebar m_titleBar = null;
	private CTL_14AJ_L_600_ListView m_listView = null;
	private CTL_16AVHN_L_172_CheckButton m_checkBox = null;
	private CTL_14AJ_L_071_Button m_connect = null;
	private PopupListAdapter m_popupAdapter = null;
	private CTL_Control_PopupList m_popuplist = null;

	private CTL_Control_TextViewBase m_hint = null;
	private SysManagerWIFIAdmin m_wifiAdmin = null;
	private ListViewAdapter m_mAdapter = null;
	private int[] m_netWorkItem = null;
	private AttributeSet m_attrs = null;
	private String m_SSIDText = "";
	private String m_pwdText = "";
	public String m_securityText = "";
	private final static int[] POPUPPOS = { 505, 58 };
	private int[] mPositionList = new int[] {
			com.android.internal.R.string.WIFI_06_S2_01,
			com.android.internal.R.string.WIFI_06_S2_02,
			com.android.internal.R.string.WIFI_06_S2_03 };
	
	@SuppressLint("UseValueOf")
	private static final Integer DATASIZE = new Integer(4);

	public SYS_VIEW_MENU_COM_NetWorkAdd(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "Com_NetWorkAdd onCreate", "onCreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_com_networkadd, null);
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);
		m_entryUtil = SYS_VIEW_MENU_COM_EntryUtil.getInstance();
		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		m_dataManger = DataManager.getInstance();
		obtainCtrlViews();
		return m_view_main;
	}

	private void obtainCtrlViews() {
		SysLog.out(TAG, "Com_NetWorkAdd obtainCtrlViews", "obtainCtrlViews");
		if (null != m_view_main) {
			m_listView = (CTL_14AJ_L_600_ListView) m_view_main
					.findViewById(R.id.menu_netwokadd_toplist);
			m_titleBar = (CTL_16AVHN_L_004_Titlebar) m_view_main
					.findViewById(R.id.menu_com_network_title_bar);
			m_checkBox = (CTL_16AVHN_L_172_CheckButton) m_view_main
					.findViewById(R.id.checkbox);
			m_hint = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.hint);
			m_popuplist = (CTL_Control_PopupList) m_view_main
					.findViewById(R.id.popuplist);
		}

		m_netWorkItem = getNetWorkItem();

		NOSECURITY = String.format(m_Context.getResources().getString(
				com.android.internal.R.string.WIFI_08_L1));

		if (null != m_titleBar) {
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT, false);
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE, false);
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT, false);
			m_titleBar
					.setLayoutPatternID(TitleBar_Layout_Pattern.RETURN_TEXT_ESC);
			m_titleBar.setTitleText(com.android.internal.R.string.WIFI_06_T1);

			m_connect = m_titleBar.getMySetupButton();
			m_connect.setVisibility(View.VISIBLE);
		}
		if (null != m_hint) {
			m_hint.setTextFont(TypeFaceId.HG);
			m_hint.setTextStyle(TextStyleID.TEXT_001);
			m_hint.setTextSize(FLOAT_SIZE_WARNING_TITLE);
			m_hint.setText(com.android.internal.R.string.WIFI_06_T2);
		}
		if (null != m_connect) {
			m_connect.setTextFont(TypeFaceId.HG);
			m_connect.setTextStyle(TextStyleID.TEXT_007);
			m_connect.setGravity(Gravity.CENTER);
			m_connect.setTextSize(26);
			m_connect.setButtonSize(218, 60);
			m_connect.setText(com.android.internal.R.string.WIFI_06_K1);
		}

		if (null != m_view_main && null != m_listView) {
			XmlPullParser parser = m_view_main.getResources().getXml(
					R.layout.menu_com_networkadd);
			m_attrs = Xml.asAttributeSet(parser);
			m_mAdapter = new ListViewAdapter(m_view_main.getContext(), m_attrs);
			m_listView.setListType(ListWidthType.LARGE);
			m_listView.getList().setAdapter(m_mAdapter);
			m_listView.getList().setPageChildrenCount(MENU_LIST_PAGE_COUNT);
		}

		m_popupAdapter = new PopupListAdapter(m_Context, null);

		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);
	}

	private int[] getNetWorkItem() {
		return new int[] { NETWORKSSID, NETWORKSECURITY, NETWORKPASSWORD };
	}

	@Override
	protected void onShow() {
		SysLog.out(TAG, "Com_NetWorkAdd onShow", "onShow");
		byte[] pwdDisplayA = new byte[4];
		int retA=m_dataManger.GetDataInfo(
				DataManagerKeyDef.DATA_MGR_SETTING_WIFI_KEYDISPLAYSTATUS, pwdDisplayA, DATASIZE);
		int tempA = bytesToInt(pwdDisplayA);
		SysLog.out(TAG, "Com_NetWorkAdd onShow", "onShow tempA="+tempA+", retA="+retA);
		if (PWDNOTDISPLAY == tempA) {
			m_checkBox.setChecked(false);
		} else {
			m_checkBox.setChecked(true);
		}
		
		List<WifiConfiguration> m_configList = new ArrayList<WifiConfiguration>();
		List<WifiConfiguration> list = new ArrayList<WifiConfiguration>();
		m_configList = m_wifiAdmin.getConfiguration();
		if(null != m_configList){
			for (WifiConfiguration config : m_configList) {
				if (null != config) {
					int netid = config.networkId;
					int timeStamp = m_wifiAdmin.getTimestamp(netid);
					if (0 >= timeStamp || null == config.BSSID) {
						m_wifiAdmin.deleteConfiguration(netid);
						m_wifiAdmin.saveConfiguration();
					}else{
						if(null != list){
							list.add(config);
						}
					}
				}
			}
		}
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
				.setM_configList(list);
		
		HashMap<String, Integer> m_priorityList = new HashMap<String, Integer>();
		if (null != list) {
			if(null != m_priorityList){
				m_priorityList.clear();
				for (WifiConfiguration conf : list) {
					int samePriorty = m_wifiAdmin
							.getAutoConnectPriority(conf.networkId);
					m_priorityList.put(conf.BSSID, samePriorty);
				}
				SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_priorityList(
						m_priorityList);
			}
		}

		if (null != m_connect) {
			m_connect.setOnProhibition();
		}
		
		if (null != m_checkBox) {
			m_checkBox.setOnProhibition();
			if (m_entryUtil.getM_securityText().equals(NOSECURITY)) {
				m_checkBox.setViewOnProhibitionEnabled(false);
			} else {
				m_checkBox.setViewOnProhibitionEnabled(true);
			}
		}

		if (null != m_SSIDText) {
			String ssid = m_entryUtil.getM_SSID();
			m_SSIDText = ssid;
		}

		if (null != m_securityText) {
			m_securityText = m_entryUtil.getM_securityText();
		}
		 
		byte[] pwdDisplayB = new byte[4];
		int retB =  m_dataManger.GetDataInfo(
				DataManagerKeyDef.DATA_MGR_SETTING_WIFI_KEYDISPLAYSTATUS, pwdDisplayB, DATASIZE);
		int tempB = bytesToInt(pwdDisplayB);
		SysLog.out(TAG, "onshow", "retB ="+retB+", tempB="+tempB);
		if (PWDDISPLAY == tempB) {
			if (m_securityText.equals(NOSECURITY)) {
				m_pwdText = "";
			} else {
				String pwd = m_entryUtil.getM_pwdAdd();
				if (pwd.length() > 18) {
					m_pwdText = pwd.substring(0, 17) + "...";
				} else {
					m_pwdText = pwd;
				}

			}
		} else {
			if (m_securityText.equals(NOSECURITY)) {
				m_pwdText = "";
			} else {
				String protecedPwd = m_entryUtil.getProtectedPwd(m_entryUtil
						.getM_pwdAdd());
				if (protecedPwd.length() > 18) {
					m_pwdText = protecedPwd.substring(0, 18);
				} else {
					m_pwdText = protecedPwd;
				}
			}
		}
		m_listView.getList().notifyDataSetChanged();

		dealConnectBtn();

		addViewCtrlListener();
	}

	private void addViewCtrlListener() {
		if (null != m_titleBar && null != TitleBarListener) {
			if (MysetupUtils.Instance().getIsToMysetup()) {
				m_titleBar.setButtonVisibility(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC, false);
			} else {
				m_titleBar.setButtonVisibility(
						CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC, true);
			}
			m_titleBar.setTitlebarListener(TitleBarListener,
					IButtonActionListener.BUTTON_EVENT.NORMAL, null);
		}
		if (null != m_listView && null != listItemListener) {
			m_listView.getList().setButtonListener(listItemListener);
		}

		if (null != m_connect && null != btnListener) {
			m_connect.setButtonListener(
					IButtonActionListener.BUTTON_EVENT.NORMAL, btnListener,
					null, true);
		}

		if (null != m_checkBox) {
			m_checkBox.setButtonListener(
					IButtonActionListener.BUTTON_EVENT.NORMAL,
					m_checkBoxListener, null, true);
		}

	}

	@Override
	protected boolean onKeyEvent(KeyEvent keyEv) {
		SysLog.out(TAG, "Com_NetWorkAdd onKeyEvent",
				"KeyCode:" + keyEv.getKeyCode());
		if (m_popuplist.isPopupListOpen()) {
			if (keyEv.getKeyCode() == KeyEvent.KEYCODE_BACK
					&& keyEv.getAction() == KeyEvent.ACTION_UP) {
				if (KeyEvent.KEYEVENT_PRESSCOUNT_2S >= keyEv.getPressedCount()) {
					VoicePlayer.getInstance(getContext()).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_LOW,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);
					m_popuplist.onEliminatePopup();
					return true;
				}
			}
			return false;
		} else {
			return super.onKeyEvent(keyEv);
		}

	}

	@Override
	protected void onHide() {
		SysLog.out(TAG, "Com_NetWorkAdd onHide", "onHide");
		if (null != m_titleBar) {
			m_titleBar.removeListener();
		}
		
		if(null != m_checkBox){
			m_checkBox.removeOnProhibition();
		}
		
		if(null != m_connect){
			m_connect.removeOnProhibition();
		}
		
		m_popuplist.onEliminatePopup();

		MessageControlIF.getIntance().closeMessageForSysView("WIFI_05_T1");
	}

	@Override
	protected void onHideFinished() {
		super.onHideFinished();
		if (null != m_commonInfoSetting) {
			m_commonInfoSetting.setForwardViewChanged(false);
		}
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
			if (null != m_netWorkItem) {
				return m_netWorkItem.length;
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
			SysLog.out(TAG, "Com_NetWorkAdd fillview", "fillview");
			item.setImageV(false);
			item.setOnProhibition();
			item.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.CONTENT, 24, 50,
					382, 29);
			item.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS, 24, 464,
					260, 38);
			CTL_Control_TextViewBase textviews = item
					.getButtonTextView(CTL_14AJ_L_600_Text_Button_Textid.STATUS);
			TextPaint textPaint = textviews.getPaint();
			float avail = textviews.getMeasuredWidth();
			if (null != m_commonInfoSetting && null != m_netWorkItem) {
				if (position < m_netWorkItem.length) {
					switch (m_netWorkItem[position]) {
					case NETWORKSSID:
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.CONTENT,
								com.android.internal.R.string.WIFI_06_K2);
						item.setTextWithoutScale(
								CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								m_SSIDText);
						if (null != m_SSIDText && m_SSIDText.length() > 0) {
							CharSequence ch = TextUtils.ellipsize(m_SSIDText,
									textPaint, avail,
									TextUtils.TruncateAt.valueOf("END"));
							textviews.setText(ch);
						}
						break;
					case NETWORKSECURITY:
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.CONTENT,
								com.android.internal.R.string.WIFI_06_K3);
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								m_securityText);
						break;
					case NETWORKPASSWORD:
						item.setText(CTL_14AJ_L_600_Text_Button_Textid.CONTENT,
								com.android.internal.R.string.WIFI_06_K4);
						if (m_entryUtil.getM_securityText().equals(NOSECURITY)) {
							item.setViewOnProhibitionEnabled(false);
						} else {
							item.setViewOnProhibitionEnabled(true);
						}
						item.setTextWithoutScale(
								CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								m_pwdText);
						if (null != m_pwdText && m_pwdText.length() > 0) {
							CharSequence ch = TextUtils.ellipsize(m_pwdText,
									textPaint, avail,
									TextUtils.TruncateAt.valueOf("END"));
							textviews.setText(ch);
						}
						break;
					default:
						break;
					}

				} else {
					return false;
				}
			}
			item.setTextGravity(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
					Gravity.LEFT);
			dealConnectBtn();
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

	private IButtonActionListenerLongRepeat TitleBarListener = new IButtonActionListenerLongRepeat() {

		@Override
		public void OnRelease(int id) {

		}

		@Override
		public void OnPush(int id) {

			if (CTL_14AJ_L_004_1_Titlebar_ButtonId.BACK == id) {
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_LOW,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_NetWorkAdd OnReturnBtnReleased", "BACK");
				if (null != m_commonInfoSetting) {
					if (m_commonInfoSetting.isForwardViewChanged()) {
					} else {
						m_commonInfoSetting.setForwardViewChanged(true);
						forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
					}
				}

			} else {
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "COM_NetWorkAdd OnESCBtnReleased", "ESC");
				exit(VType.SYS_VIEW_TYPE_MENU_COM);
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
					SysLog.out(TAG, "Com_NetWorkAdd OnItemPush",
							"isForwardViewChanged = TRUE");
				} else if (m_listView.isInAnimation()) {
					SysLog.out(TAG, "OnItemRelease", " isInAnimation ");
				} else {
					VoicePlayer.getInstance(m_Context).playbackVoiceById(
							VoicePlayer.VOICE_ID_BEEP_1_HIGH,
							VoicePlayer.PRIORITY_CALL_AND_BEEP);
					m_commonInfoSetting.setForwardViewChanged(true);
					switch (m_netWorkItem[position]) {
					case NETWORKSSID:
						m_entryUtil.setM_entryflag(m_entryUtil.TYPE_SSID);
						forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY);
						break;
					case NETWORKSECURITY:
						m_popuplist.openPopupList(POPUPPOS,
								PopupListStage.THREE_STAGE,
								PopupLayoutLIst.Layout_14AJ_L_601,
								m_popupAdapter, m_popuplistListener, 0);

						m_commonInfoSetting.setForwardViewChanged(false);
						break;
					case NETWORKPASSWORD:
						m_entryUtil.setM_entryflag(m_entryUtil.TYPE_PWD);
						forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY);
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

	private IButtonActionListener m_checkBoxListener = new IButtonActionListener() {

		@Override
		public boolean OnLongClick(View view, int time) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean OnRepeat(View view, int... param) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void OnPush(View view) {
			if (SysManagerWIFIAdmin.getInstance(m_Context).isFastDoubleClick()) {
				return;
			}
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			if (m_checkBox.isChecked()) {
				String pwd = m_entryUtil.getProtectedPwd(m_entryUtil
						.getM_pwdAdd());
				m_pwdText = pwd;

				if (pwd.length() > 18) {
					m_pwdText = pwd.substring(0, 18);
				} else {
					m_pwdText = pwd;
				}

				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setText(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								m_pwdText);
				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								24, 364, 260, 38);
				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setTextGravity(
								CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								Gravity.LEFT);

				m_listView.getList().notifyDataSetChanged();

				byte[] byOutputDataA = new byte[4];
				byOutputDataA = intToBytes(PWDNOTDISPLAY);
				int retNotDisplay = m_dataManger
						.SetDataInfo(
								DataManagerKeyDef.DATA_MGR_SETTING_WIFI_KEYDISPLAYSTATUS,
								byOutputDataA, 4);
				m_checkBox.setChecked(false);
				SysLog.out(TAG, "m_checkBoxListener", "retNotDisplay="
						+ retNotDisplay);
			} else {
				String pwd = m_entryUtil.getM_pwdAdd();
				m_pwdText = pwd;
				if (pwd.length() > 18) {
					m_pwdText = pwd.substring(0, 17) + "...";
				} else {
					m_pwdText = pwd;
				}

				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setText(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								m_pwdText);
				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								24, 364, 260, 38);
				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setTextGravity(
								CTL_14AJ_L_600_Text_Button_Textid.STATUS,
								Gravity.LEFT);

				m_listView.getList().notifyDataSetChanged();
				
				byte[] byOutputDataB = new byte[4];
				byOutputDataB = intToBytes(PWDDISPLAY);
				int retDisplay = m_dataManger
						.SetDataInfo(
								DataManagerKeyDef.DATA_MGR_SETTING_WIFI_KEYDISPLAYSTATUS,
								byOutputDataB, 4);
				m_checkBox.setChecked(true);
				SysLog.out(TAG, "m_checkBoxListener", "retDisplay="
						+ retDisplay);
			}
		}

		@Override
		public void OnRelease(View view) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * @brief adapter of positionpopuplist
	 */
	private class PopupListAdapter extends
			CTL_Control_ListViewAdapterBase<CTL_14AJ_L_601_Big_Button> {

		public PopupListAdapter(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected int getCount(String key) {

			return mPositionList.length;
		}

		@Override
		public boolean fillView(String key, int position,
				CTL_14AJ_L_601_Big_Button item) {
			item.setOnProhibition();
			if (m_securityText.equals(m_Context.getResources().getString(
					mPositionList[position]))) {
				item.setViewFocused(true);
			} else {
				item.setViewNormal();
			}
			item.setText(mPositionList[position]);

			return true;
		}

		@Override
		public void fillEmptyView(String key, int position,
				CTL_14AJ_L_601_Big_Button item) {
			// TODO Auto-generated method stub

		}

		@Override
		public CTL_14AJ_L_601_Big_Button getNewT() {
			// TODO Auto-generated method stub
			return new CTL_14AJ_L_601_Big_Button(m_Context, m_Attrs);
		}

	}

	/**
	 * @brief list item click listener
	 */
	private IListItemButtonGroupListener4UI m_popuplistListener = new IListItemButtonGroupListener4UI() {

		@Override
		public int getButtonMode(int buttonChildId, List<Integer> time) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void OnItemPush(String key, int position, int id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnItemRelease(String key, int position, int id) {
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			String securityText = m_entryUtil.getM_securityText();
			String security = m_Context.getResources().getString(
					mPositionList[position]);
			m_entryUtil.setM_securityText(security);
			m_securityText = m_entryUtil.getM_securityText();
			if (null != m_checkBox) {
				if (security.equals(securityText)) {
				} else {
					m_pwdText = "";
					m_entryUtil.setM_pwdAdd("");
				}
				if (m_securityText.equals(NOSECURITY)) {
					m_pwdText = "";
					m_entryUtil.setM_pwdAdd("");
					m_checkBox.setViewOnProhibitionEnabled(false);
				} else {
					m_checkBox.setViewOnProhibitionEnabled(true);
				}
			}

			dealConnectBtn();

			if (m_entryUtil.getM_securityText().equals(NOSECURITY)) {
				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setViewOnProhibitionEnabled(false);
			} else {
				((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
						.setViewOnProhibitionEnabled(true);
			}
			((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
					.setText(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
							m_pwdText);
			((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
					.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS, 24,
							364, 260, 38);
			((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(2))
					.setTextGravity(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
							Gravity.LEFT);

			((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(1))
					.setText(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
							m_securityText);
			((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(1))
					.setTextPos(CTL_14AJ_L_600_Text_Button_Textid.STATUS, 24,
							364, 260, 38);
			((CTL_14AJ_L_600_Button) m_listView.getList().getItemAtPos(1))
					.setTextGravity(CTL_14AJ_L_600_Text_Button_Textid.STATUS,
							Gravity.LEFT);
			m_popuplist.onEliminatePopup();
		}

		@Override
		public boolean OnItemLongClick(String key, int position, int id,
				int time) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean OnItemRepeatClick(String key, int position, int id,
				int... param) {
			// TODO Auto-generated method stub
			return false;
		}

	};

	/**
	 * 
	 */
	private IButtonActionListener btnListener = new IButtonActionListener() {

		@Override
		public boolean OnRepeat(View view, int... param) {
			return false;
		}

		@Override
		public void OnRelease(View view) {

		}

		@Override
		public void OnPush(View view) {
			if (SysManagerWIFIAdmin.isFastDoubleClick()) {
				return;
			}
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			if (WifiManager.WIFI_STATE_ENABLED == SysManagerWIFIAdmin
					.getInstance(m_Context).checkState()) {
				int status = SysManagerWIFIAdmin.getInstance(m_Context)
						.addNetwork(m_entryUtil.getM_SSID(),
								m_entryUtil.getM_pwdAdd(),
								dealSecurity(m_entryUtil.getM_securityText()),
								null);
				
				setScreenType();
				
				MessageControlIF.getIntance().openMessageForSysView(
						"WIFI_05_T1", MsgErrorType.ERROR_HAVE, null);
				if (status > -1) {
					int netID = status;
					SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setNetID(netID);
					SysLog.out(TAG, "COM_NetWorkAdd btnListener",
							"ssid = "
									+ SYS_VIEW_MENU_COM_EntryUtil.getInstance()
											.getM_SSID() + ",pwd="
									+ m_entryUtil.getM_pwdAdd() + ",type="
									+ m_entryUtil.getM_securityText()
									+ ",netID=" + netID);
					SysManagerCommonIF.instance().dealWifiConnectTimeOut();
				} else {
					SysLog.out(TAG, "COM_NetWorkAdd btnListener",
							"connect failure(input error)");
					MessageControlIF.getIntance().closeMessageForSysView(
							"WIFI_05_T1");
					String temp = String
							.format(m_Context.getResources().getString(
									com.android.internal.R.string.WIFI_18_T1));
					MessageControlIF.getIntance().openMessageForSysView(
							"WIFI_18_T1", temp, MsgErrorType.ERROR_HAVE, null);
				}
			}
		}

		@Override
		public boolean OnLongClick(View view, int time) {
			return false;
		}
	};

	private int dealSecurity(String security) {
		if (security.contains("WPA")) {
			return TYPE_WPA;
		} else if (security.contains("WEP")) {
			return TYPE_WEP;
		} else {
			return TYPE_NOSECURITY;
		}
	}

	private void dealConnectBtn() {
		if (m_entryUtil.getM_securityText().equals(NOSECURITY)) {
			if (m_SSIDText.length() > 0) {
				m_connect.setViewOnProhibitionEnabled(true);
			} else {
				m_connect.setViewOnProhibitionEnabled(false);
			}
		} else {
			if (m_pwdText.length() > 0 && m_SSIDText.length() > 0) {
				m_connect.setViewOnProhibitionEnabled(true);
			} else {
				m_connect.setViewOnProhibitionEnabled(false);
			}
		}
	}

	private void setScreenType(){
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_screenType(ScreenType.NETWORADDVIEW);
	}
	
	/**
	 * int to byte[]
	 * @param int
	 * @return byte[]
	 */
	private   byte[] intToBytes( int value )   
	{   
	    byte[] src = new byte[4];  
	    src[3] =  (byte) ((value>>24) & 0xFF);  
	    src[2] =  (byte) ((value>>16) & 0xFF);  
	    src[1] =  (byte) ((value>>8) & 0xFF);    
	    src[0] =  (byte) (value & 0xFF);                  
	    return src;   
	}  
	
	/**
	 * byte[] to int
	 * @param byte[]
	 * @return  int
	 */
	private  int bytesToInt(byte[] src) {  
	    int value;    
	    value = (int) ((src[0] & 0xFF)   
	            | ((src[1] & 0xFF)<<8)   
	            | ((src[2] & 0xFF)<<16)   
	            | ((src[3] & 0xFF)<<24));  
	    return value;  
	}  

}
