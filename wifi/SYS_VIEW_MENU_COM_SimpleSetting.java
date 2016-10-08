package jp.pioneer.ceam.view.menucommunication.wifi;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.Interface.common.IMessageActionListener;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase.FontSize;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase.TypeFaceId;
import jp.pioneer.ceam.ctl.common.CTL_Constant.TitleBar_Layout_Pattern;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_071_Button;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_14AJ_L_004_1_Titlebar.CTL_14AJ_L_004_1_Titlebar_ButtonId;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar.CTL_16AJ_L_004_0_Titlebar_ButtonId;
import jp.pioneer.ceam.manage.SysManagerIF;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver.WpsCancelListener;
import jp.pioneer.ceam.manage.SysMangerWIFIBroadcastReceiver.WpsConnectListener;
import jp.pioneer.ceam.manage.message.MessageControlIF;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.uicommonlib.TextTheme.TextListConstant.TextStyleID;
import jp.pioneer.ceam.uicommonlib.constant.MessageConstant.MsgErrorType;
import jp.pioneer.ceam.uicommonlib.constant.SysViewConstant.SysViewKind;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_WifiAPUtil.ScreenType;
import jp.pioneer.ceam.view.mysetup.MysetupUtils;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WpsInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SYS_VIEW_MENU_COM_SimpleSetting extends SysView {

	protected static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private static final int WPSSTARTSUCCESS = 1;
	private static final int WPSCOMPLETION = 2;
	private static final int WPSFAILURE = 3;
	private static final int WPSPINSTARTSUCCESS = 4;
	private static final int WPSPINCOMPLETION = 5;
	private static final int WPSPINFAILURE = 6;

	private static final int CANCELMESSAGE = 0;

	private Context m_Context;
	private CTL_Control_ViewGroupBase m_view = null;
	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_16AVHN_L_004_Titlebar m_titleBar = null;

	private CTL_14AJ_L_071_Button m_WIFI_WPSButton;
	private CTL_14AJ_L_071_Button m_WIFI_WPS_PIN_Button;

	private CTL_Control_TextViewBase m_WIFI_WPSInfo;
	private CTL_Control_TextViewBase m_WIFI_WPS_PINInfo;

	private SysManagerWIFIAdmin m_wifiAdmin;
	private static String m_pin = "";
	private boolean isWpsFlag = true;

	// SysViewCommonInfoMenuSettings
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;

	private Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WPSSTARTSUCCESS:
				SysManagerIF.instance().openViewByKind(
						SysViewKind.KIND_WPSMESSAGE);
				break;
			case WPSCOMPLETION:
				break;
			case WPSFAILURE:
				break;
			case WPSPINSTARTSUCCESS:
				MessageControlIF
						.getIntance()
						.openMessageForSysView(
								"WIFI_16_T1",
								String.format(
										m_Context
												.getResources()
												.getString(
														com.android.internal.R.string.WIFI_16_T1),
										m_pin), MsgErrorType.ERROR_HAVE, stopWpsMessageListener);
				break;
			case WPSPINCOMPLETION:
				break;
			case WPSPINFAILURE:
				break;

			default:
				break;
			}
		};
	};


	private WpsConnectListener m_wpsConnectListener = new WpsConnectListener() {

		@Override
		public void onCompletion() {
			int num = 0;
			SysLog.out(TAG, "COM_SimpleSetting m_wpsConnectListener", "onCompletion:" + (++num));

		}

		@Override
		public void onFailure(int arg0) {
			int num = 0;
			SysLog.out(TAG, "COM_SimpleSetting m_wpsConnectListener", "onFailure:" + (++num));

		}

		@Override
		public void onStartSuccess(String arg0) {
			SysLog.out(TAG, "onStartSuccess", "pin = " + arg0);
			if (isWpsFlag) {
				int num = 0;
				SysLog.out(TAG, "COM_SimpleSetting m_wpsConnectListener", "WPs onStartSuccess:"
						+ (++num));
				Message msgWpsSuccess = Message.obtain();
				msgWpsSuccess.what = WPSSTARTSUCCESS;
				m_handler.sendMessage(msgWpsSuccess);
			} else {
				int num = 0;
				SysLog.out(TAG, "COM_SimpleSetting m_wpsConnectListener",
						"WPsPIN onStartSuccess:" + (++num));
				m_pin = arg0;
				Message msgWpsPinSuccess = Message.obtain();
				msgWpsPinSuccess.what = WPSPINSTARTSUCCESS;
				m_handler.sendMessage(msgWpsPinSuccess);
			}
		}

	};

	private WpsCancelListener m_wpsCancelListener = new WpsCancelListener() {

		@Override
		public void onSuccess() {
			int num = 0;
			SysLog.out(TAG, "COM_SimpleSetting m_wpsCancelListener", "onSuccess:" + (++num));
		}

		@Override
		public void onFailure(int arg0) {
			int num = 0;
			SysLog.out(TAG, "COM_SimpleSetting m_wpsCancelListener", "onFailure:" + (++num));
		}
	};

	public SYS_VIEW_MENU_COM_SimpleSetting(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_SimpleSetting onCreat", "onCreat");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_com_simplesetting, null);
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);
		if (null == m_view_main) {
			return null;
		}
		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		obtainCtrlViews();

		return m_view_main;
	}

	private void obtainCtrlViews() {
		SysLog.out(TAG, "COM_SimpleSetting obtainCtrlViews", "obtainCtrlViews");
		if (null != m_view_main) {

			m_view = (CTL_Control_ViewGroupBase) m_view_main
					.findViewById(R.id.menu_com_wifisimplesettingview);

			m_titleBar = (CTL_16AVHN_L_004_Titlebar) m_view_main
					.findViewById(R.id.menu_com_wifisimplesetting_title_bar);

			m_WIFI_WPSButton = (CTL_14AJ_L_071_Button) m_view_main
					.findViewById(R.id.menu_com_wifiwps);
			m_WIFI_WPS_PIN_Button = (CTL_14AJ_L_071_Button) m_view_main
					.findViewById(R.id.menu_com_wifiwps_pin);

			m_WIFI_WPSInfo = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.menu_com_wifiwpsinfo);
			m_WIFI_WPS_PINInfo = (CTL_Control_TextViewBase) m_view_main
					.findViewById(R.id.menu_com_wifiwps_pininfo);
		}

		if (null != m_titleBar) {
			m_titleBar.setTitleText(com.android.internal.R.string.WIFI_14_T1);
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT, false);
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE, false);
			m_titleBar.setButtonVisibility(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT, false);
			m_titleBar
					.setLayoutPatternID(TitleBar_Layout_Pattern.RETURN_TEXT_ESC);
		}

		if (null != m_WIFI_WPSButton) {
			m_WIFI_WPSButton.setButtonSize(196, 60);
			m_WIFI_WPSButton.setTextSize(FontSize.FONTSIZE_30);
			m_WIFI_WPSButton.setTextFont(TypeFaceId.HG);
			m_WIFI_WPSButton.setTextStyle(TextStyleID.TEXT_007);
			m_WIFI_WPSButton.setGravity(Gravity.CENTER);
			m_WIFI_WPSButton.setText(com.android.internal.R.string.WIFI_14_K1);
		}
		if (null != m_WIFI_WPS_PIN_Button) {
			m_WIFI_WPS_PIN_Button.setButtonSize(196, 60);
			m_WIFI_WPS_PIN_Button.setTextSize(FontSize.FONTSIZE_30);
			m_WIFI_WPS_PIN_Button.setTextFont(TypeFaceId.HG);
			m_WIFI_WPS_PIN_Button.setTextStyle(TextStyleID.TEXT_007);
			m_WIFI_WPS_PIN_Button.setGravity(Gravity.CENTER);
			m_WIFI_WPS_PIN_Button
					.setText(com.android.internal.R.string.WIFI_14_K2);
		}
		if (null != m_WIFI_WPSInfo) {
			m_WIFI_WPSInfo.setTextSize(FontSize.FONTSIZE_30);
			m_WIFI_WPSInfo.setTextFont(TypeFaceId.HG);
			m_WIFI_WPSInfo.setTextStyle(TextStyleID.TEXT_001);
			m_WIFI_WPSInfo.setText(com.android.internal.R.string.WIFI_14_T2);
			m_WIFI_WPSInfo.setGravity(Gravity.CENTER);
		}
		if (null != m_WIFI_WPS_PINInfo) {
			m_WIFI_WPS_PINInfo.setTextSize(FontSize.FONTSIZE_30);
			m_WIFI_WPS_PINInfo.setTextFont(TypeFaceId.HG);
			m_WIFI_WPS_PINInfo.setTextStyle(TextStyleID.TEXT_001);
			m_WIFI_WPS_PINInfo
					.setText(com.android.internal.R.string.WIFI_14_T3);
			m_WIFI_WPS_PINInfo.setGravity(Gravity.CENTER);
		}

		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);

	}

	@Override
	protected void onShow() {
		SysLog.out(TAG, "COM_SimpleSetting onShow", "onShow");
		
		SysMangerWIFIBroadcastReceiver.getInstance().addWpsConnectListener(
				m_wpsConnectListener);
		SysMangerWIFIBroadcastReceiver.getInstance().addWpsCancelListener(
				m_wpsCancelListener);
		
		List<WifiConfiguration> m_configList = new ArrayList<WifiConfiguration>();
		List<WifiConfiguration> list = new ArrayList<WifiConfiguration>();
		m_configList = m_wifiAdmin.getConfiguration();
		if(null != m_configList){
			for (WifiConfiguration config : m_configList) {
				if (null != config) {
					int netid = config.networkId;
					int timeStamp = m_wifiAdmin.getTimestamp(netid);
					if(0 >= timeStamp){
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
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_configList(list);

		if (null != m_view && null != m_commonInfoSetting) {
			SysLog.out(TAG, "COM_SimpleSetting onShow", "setVisibility VISIBLE");

			m_view.setVisibility(View.VISIBLE);
		}
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

		if (null != m_WIFI_WPSButton) {
			m_WIFI_WPSButton.setOnProhibition();
			m_WIFI_WPSButton.setButtonListener(
					IButtonActionListener.BUTTON_EVENT.NORMAL, WIFIWPSListener,
					null,true);
		}
		if (null != m_WIFI_WPS_PIN_Button) {
			m_WIFI_WPS_PIN_Button.setOnProhibition();
			m_WIFI_WPS_PIN_Button.setButtonListener(
					IButtonActionListener.BUTTON_EVENT.NORMAL,
					WIFIWPSPINListener, null,true);
		}

	}

	@Override
	protected void onHide() {
		SysLog.out(TAG, "COM_SimpleSetting onHide", "setVisibility VISIBLE");
		SysManagerIF.instance().closeViewByKind(SysViewKind.KIND_WPSMESSAGE);
		MessageControlIF.getIntance().closeMessageForSysView("WIFI_16_T1");
		if (null != m_wifiAdmin) {
			m_wifiAdmin.cancelWps();
		}
		if (null != m_titleBar) {
			m_titleBar.removeListener();
		}
		if (null != m_WIFI_WPSButton) {
			m_WIFI_WPSButton.removeOnProhibition();
			m_WIFI_WPSButton.removeListener();
		}
		if (null != m_WIFI_WPS_PIN_Button) {
			m_WIFI_WPS_PIN_Button.removeOnProhibition();
			m_WIFI_WPS_PIN_Button.removeListener();
		}
		if (null != m_commonInfoSetting) {
			m_commonInfoSetting.setForwardViewChanged(false);
		}
		SysMangerWIFIBroadcastReceiver.getInstance().removeWpsConnectListener(
				m_wpsConnectListener);
		SysMangerWIFIBroadcastReceiver.getInstance().removeWpsCancelListener(
				m_wpsCancelListener);
	}

	@Override
	protected void onHideFinished() {
		super.onHideFinished();
		if (null != m_commonInfoSetting) {
			m_commonInfoSetting.setForwardViewChanged(false);
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
				SysLog.out(TAG, "COM_SimpleSetting OnReturnBtnReleased", "BACK");
				if(MysetupUtils.Instance().getIsToMysetup()){
					backDefaultViewChange(ViewID.SYS_VIEW_ID_MYSETUP_WIFI);
				}else {
					backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_TOPMENU);					
				}
			} else {
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				SysLog.out(TAG, "OnESCBtnReleased", "ESC");
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

	private IButtonActionListener WIFIWPSListener = new IButtonActionListener() {

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
			isWpsFlag = true;
			SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setWpsFlag(true);
			if (m_wifiAdmin.isFastDoubleClick()) {
				return;
			}
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			WpsInfo m_wpsInfo = new WpsInfo();
			m_wpsInfo.setup = WpsInfo.PBC;
			m_wifiAdmin.startWps(WpsInfo.PBC, "");
			setScreenType();
		}

		@Override
		public void OnRelease(View view) {
		}

	};

	private IButtonActionListener WIFIWPSPINListener = new IButtonActionListener() {

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
			isWpsFlag = false;
			SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setWpsFlag(false);
			if (isFastDoubleClick()) {
				return;
			}
			VoicePlayer.getInstance(m_Context).playbackVoiceById(
					VoicePlayer.VOICE_ID_BEEP_1_HIGH,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			WpsInfo m_wpsPinInfo = new WpsInfo();
			m_wpsPinInfo.setup = WpsInfo.DISPLAY;
			m_wifiAdmin.startWps(WpsInfo.DISPLAY, "");
			setScreenType();
		}

		@Override
		public void OnRelease(View view) {

		}

	};

	private IMessageActionListener stopWpsMessageListener = new IMessageActionListener() {

		@Override
		public boolean OnStatusChange(String msgId, int state) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean OnKeyEvent(String msgId, int keyCode, int keyAction,
				int pressedCount) {
			if ("WIFI_16_T1".equals(msgId)) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& keyAction == KeyEvent.ACTION_UP) {
					m_wifiAdmin.cancelWps();
					return true;
				}
			}
			
			return false;
		}

		@Override
		public boolean OnBackScreenChange(String msgId) {
			if ("WIFI_16_T1".equals(msgId)) {
				m_wifiAdmin.cancelWps();
			}
			return false;
		}


		@Override
		public boolean OnClick(String msgId, int btnIndex) {
			int num = 0;
			SysLog.out(TAG, "stopWpsMessageListener", "num=" + (++num));
			if("WIFI_16_T1".equals(msgId)){
				if (m_wifiAdmin.isFastDoubleClick()) {
					return false;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_LOW,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (CANCELMESSAGE == btnIndex) {
					m_wifiAdmin.cancelWps();
					return true;
				} 
			}
			
			return false;
		}
	};

	
	private static long lastClickTime;
	private static boolean isFastDoubleClick() {   
        long time = SystemClock.uptimeMillis();      
        if ( time - lastClickTime < 1100) { 
            return true;      
        }      
        lastClickTime = time; 
        return false;      
    } 
	
	private void setScreenType(){
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_screenType(ScreenType.WPSCONNETVIEW);
	}
	
}
