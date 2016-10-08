package jp.pioneer.ceam.view.menucommunication.wifi;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_EditTextBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase.TypeFaceId;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_VK_Titlebar;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_VK_Titlebar.CTL_16AVHN_L_VK_ButtonId;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_VK_Titlebar.KeyBoardWatch;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.message.MessageControlIF;
import jp.pioneer.ceam.manage.screen.ScreenCombineManagerIF;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.uicommonlib.constant.UIScreenConstant.ScreenID;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_WifiAPUtil.ScreenType;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SYS_VIEW_MENU_COM_PassWordEntry extends SysView {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private Context m_Context;
	private MyHandler m_VKhandler = null;

	private static final int TYPE_WPAWPA2 = 3;
	private static final int TYPE_WEP = 2;
	private static final int TYPE_NOSECURITY = 1;


	// SysViewCommonInfoMenuSettings
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;
	private InputMethodManager m_inputManager = null;
	private SysManagerWIFIAdmin m_wifiAdmin = null;
	private SYS_VIEW_MENU_COM_EntryUtil m_entryUtil = null;
	private SYS_VIEW_MENU_COM_WifiAPUtil m_wifiAPUtil = null;

	private List<ScanResult> m_scanedAPList = new ArrayList<ScanResult>();
	private int pos = 0;

	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_16AVHN_L_VK_Titlebar m_vkEdit = null;

	private CTL_Control_EditTextBase m_Et_Input = null;
	private KeyBoardWatch  mKeyBoardWatch = null;

	private int netID = 0;
	private int VKType = 0;


	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		private static final int MSG_OPENVK = 0;
		private static final int MES_CLOSEVIEW = 1;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_OPENVK:
				if (null != m_Et_Input && null != m_inputManager) {

					m_Et_Input.requestFocus();
					m_inputManager.showSoftInput(m_Et_Input,
							InputMethodManager.RESULT_UNCHANGED_SHOWN); //renovate  message display for OnProhibition
					m_Et_Input.setSelection(m_Et_Input.length());
				}
				break;
				
			case MES_CLOSEVIEW:
				if (m_entryUtil.TYPE_DEFAULT == m_entryUtil.getM_entryflag()) {
					if (m_commonInfoSetting.isForwardViewChanged()) {
					} else {
						m_commonInfoSetting.setForwardViewChanged(true);
						backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
					}
				} else {
					if (m_commonInfoSetting.isForwardViewChanged()) {
					} else {
						m_commonInfoSetting.setForwardViewChanged(true);
						backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_NETWORKADD);
					}
				}
				break;

			default:
				break;
			}

		}

	};

	public SYS_VIEW_MENU_COM_PassWordEntry(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_PassWordEntry onCreate", "onCreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_bt_pincodeinput, null);
		if (null == m_view_main) {
			return null;
		}
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);
		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		m_entryUtil = SYS_VIEW_MENU_COM_EntryUtil.getInstance();
		m_wifiAPUtil = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance();

		obtainCtrlViews();
		return m_view_main;
	}

	private void obtainCtrlViews() {
		SysLog.out(TAG, "COM_PassWordEntry obtainCtrlViews", "obtainCtrlViews");
		m_VKhandler = new MyHandler();
		if (null != m_view_main) {
			m_vkEdit = (CTL_16AVHN_L_VK_Titlebar) m_view_main.findViewById(R.id.vk_titlebar);
			m_Et_Input = m_vkEdit.getEditText();
		}
		if(null!=m_vkEdit){
			m_vkEdit.setText(com.android.internal.R.string.VINFO_01_P5);
		}
		
		if (null != m_Et_Input) {
			m_inputManager = (InputMethodManager) m_Et_Input.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		mKeyBoardWatch = m_vkEdit.new KeyBoardWatch(m_view_main);

		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);

	}

	@Override
	protected void onShow() {
		SysLog.out(TAG, "COM_PassWordEntry onShow", "onShow");
		VKType = m_entryUtil.getM_entryflag();
		switch (VKType) {
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_SSID:
			ScreenCombineManagerIF.instance()
					.setScreenIDforSysView(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY, ScreenID.SCR_ID_AP_ADD_SSID_INPUT_WIFI_07);
			break;
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_PWD:
			ScreenCombineManagerIF.instance()
					.setScreenIDforSysView(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY, ScreenID.SCR_ID_AP_ADD_PASSWORD_INPUT_WIFI_09);
			break;
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_DEFAULT:
			ScreenCombineManagerIF.instance()
					.setScreenIDforSysView(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY, ScreenID.SCR_ID_AP_CONNECT_PASSWORD_INPUT_WIFI_04);
			break;
		}
		
		m_scanedAPList = m_wifiAPUtil.getM_scannedAP();
		
		pos = m_wifiAPUtil.getScannedAPPosition();

		initVKType();
		initEditText();
		addViewCtrlListener();

		if (null != m_VKhandler) {
			m_VKhandler.sendEmptyMessageDelayed(MyHandler.MSG_OPENVK, 100);
		}

	}

	public void initVKType() {
		
		InputFilter inputFilterPwd=new InputFilter() {
			
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				return null;
				
			}
		};
		
		InputFilter inputFilterSSID=new InputFilter() {
			
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				return null;
			}
		};

		switch (VKType) {
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_SSID:

			if(null!=m_vkEdit){
				m_vkEdit.setText(com.android.internal.R.string.WIFI_07_P5_01);
			}
			
			if (null != m_Et_Input) {
				m_Et_Input.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_URI);
				m_Et_Input.setImeOptions(EditorInfo.IME_ACTION_DONE);
				m_Et_Input
						.setFilters(new InputFilter[] { inputFilterSSID, new InputFilter.LengthFilter(
								32) });
			}
			
			break;

		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_PWD:
			if(null!=m_vkEdit){
				m_vkEdit.setText(com.android.internal.R.string.WIFI_09_P5_01);
			}
			
			if (null != m_Et_Input) {
				m_Et_Input.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				m_Et_Input.setImeOptions(EditorInfo.IME_ACTION_DONE);
				if ("WEP".equals(m_entryUtil.getM_securityText())) {
					m_Et_Input
							.setFilters(new InputFilter[] { inputFilterPwd, new InputFilter.LengthFilter(
									26) });
				} else {
					m_Et_Input
							.setFilters(new InputFilter[] { inputFilterPwd, new InputFilter.LengthFilter(
									63)});
				}
			}
			break;
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_DEFAULT:
			
			if(null!=m_vkEdit){
				m_vkEdit.setText(com.android.internal.R.string.WIFI_04_P5_01);
			}
			
			if (null != m_Et_Input) {
				m_Et_Input.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				m_Et_Input.setImeOptions(EditorInfo.IME_ACTION_DONE);
				SysLog.out(TAG, "COM_PassWordEntry initVKType",
						"security=" + m_entryUtil.getM_securityTextDefault());
				if (TYPE_WEP == m_entryUtil.getM_securityTextDefault()) {
					m_Et_Input
							.setFilters(new InputFilter[] { inputFilterPwd,new InputFilter.LengthFilter(
									26) });
				} else {
					m_Et_Input
							.setFilters(new InputFilter[] { inputFilterPwd,new InputFilter.LengthFilter(
									63) });
				}
			}
			break;

		default:
			break;
		}
	}

	private void initEditText() {
		if (null == m_Et_Input) {
			return;
		}
		m_Et_Input.setSingleLine(true);
		m_Et_Input.getText().clear();
		switch (VKType) {
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_SSID:
			ScreenCombineManagerIF.instance()
					.setScreenIDforSysView(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY, ScreenID.SCR_ID_AP_ADD_SSID_INPUT_WIFI_07);
			if (m_entryUtil.getM_SSID().equals("")) {
				m_Et_Input.setHint(com.android.internal.R.string.WIFI_07_T1_01);
			} else {
				m_Et_Input.setText(m_entryUtil.getM_SSID());
			}
			break;

		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_PWD:
			ScreenCombineManagerIF.instance()
					.setScreenIDforSysView(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY, ScreenID.SCR_ID_AP_ADD_PASSWORD_INPUT_WIFI_09);
			if (m_entryUtil.getM_pwdAdd().equals("")) {
				m_Et_Input.setHint(com.android.internal.R.string.WIFI_09_T1_01);
			} else {
				m_Et_Input.setText(m_entryUtil.getM_pwdAdd());
			}
			break;
		case SYS_VIEW_MENU_COM_EntryUtil.TYPE_DEFAULT:
			ScreenCombineManagerIF.instance()
					.setScreenIDforSysView(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_PASSWORDENTRY, ScreenID.SCR_ID_AP_CONNECT_PASSWORD_INPUT_WIFI_04);
			if (m_entryUtil.getM_pwd().equals("")) {
				m_Et_Input.setHint(com.android.internal.R.string.WIFI_04_T1_01);
			} else {
			}
			break;
		default:
			break;

		}
	}

	private void addViewCtrlListener() {

		m_Et_Input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (EditorInfo.IME_ACTION_DONE == actionId) {
					m_inputManager.hideSoftInputFromWindow(
							m_Et_Input.getWindowToken(), 0);
					m_Et_Input.clearFocus();
					if (m_entryUtil.TYPE_DEFAULT == m_entryUtil
							.getM_entryflag()) {
						if (TYPE_WPAWPA2 == dealSecurity(m_scanedAPList
								.get(pos).capabilities)) {
							if (m_Et_Input.getText().toString().length() < 8) {
								SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
								m_Et_Input.getText().clear();
								backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
								return false;
							} else {
							}
						} else if (TYPE_WEP == dealSecurity(m_scanedAPList
								.get(pos).capabilities)) {
							if (m_Et_Input.getText().toString().length() < 5) {
								SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
								m_Et_Input.getText().clear();
								backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
								return false;
							} else {
							}
						}
						m_entryUtil.setM_pwd(m_Et_Input.getText().toString());

						if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin
								.checkState()) {
							int sta = m_wifiAdmin.addNetwork(m_scanedAPList
									.get(pos).SSID, m_Et_Input.getText()
									.toString(), wifiType(m_scanedAPList
									.get(pos).capabilities), m_scanedAPList
									.get(pos).BSSID);
							
							m_wifiAdmin.sendManuallyNetworkBroadcast(sta, m_Context);//zy
							
							setScreenType();
							
							if (sta > -1) {
								SysLog.out(
										TAG,
										"COM_PassWordEntry IButtonActionListener",
										"netWorkID=" + sta);
								netID = sta;
								SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setNetID(netID);
								SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPasswordForward(true);
								backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
							} else {
								SysLog.out(
										TAG,
										"COM_PassWordEntry IButtonActionListener",
										"connect failure(input error)");
								backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
							}
						}

					} else if (m_entryUtil.TYPE_SSID == m_entryUtil
							.getM_entryflag()) {
						m_entryUtil.setM_SSID(m_Et_Input.getText().toString());
						if (null != m_commonInfoSetting) {
							if (m_commonInfoSetting.isForwardViewChanged()) {
							} else {
								m_commonInfoSetting.setForwardViewChanged(true);
								forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_NETWORKADD);
							}
						}

					} else {
						if (TYPE_WPAWPA2 == dealSecurity(m_entryUtil
								.getM_securityText())) {
							if (m_Et_Input.getText().toString().length() < 8) {
								SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
								m_Et_Input.getText().clear();
							} else {
								m_entryUtil
								.setM_pwdAdd(m_Et_Input.getText().toString());
							}
						} else if (TYPE_WEP == dealSecurity(m_entryUtil
								.getM_securityText())) {
							if (m_Et_Input.getText().toString().length() < 5) {
								SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
								m_Et_Input.getText().clear();
							} else {
								m_entryUtil
								.setM_pwdAdd(m_Et_Input.getText().toString());
							}
						}
						if (null != m_commonInfoSetting) {
							if (m_commonInfoSetting.isForwardViewChanged()) {
							} else {
								m_commonInfoSetting.setForwardViewChanged(true);
								forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_NETWORKADD);
							}
						}
					}
				}
				return false;
			}

		});
		
		m_vkEdit.setButtonListener(IButtonActionListener.BUTTON_EVENT.NORMAL, m_ButtonListener,
				null);
		mKeyBoardWatch.addLayoutObserver();

	}

	private IButtonActionListenerLongRepeat m_ButtonListener = new IButtonActionListenerLongRepeat() {

		@Override
		public void OnPush(int id) {
			if (CTL_16AVHN_L_VK_ButtonId.BACK == id) {
				SysLog.out(TAG, "COM_PassWordEntry OnPush", "m_BackButtonListener");
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_LOW,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				m_inputManager.hideSoftInputFromWindow(m_Et_Input.getWindowToken(),
						0);
				m_Et_Input.getText().clear();
				if (null != m_inputManager && null != m_Et_Input) {
					m_Et_Input.clearFocus();
					if (null != m_VKhandler) {
						m_VKhandler.sendEmptyMessageDelayed(
								MyHandler.MES_CLOSEVIEW, 100);
					}
				}
			} else if (CTL_16AVHN_L_VK_ButtonId.OK == id) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				String input = m_Et_Input.getText().toString();
				SysLog.out(TAG, "COM_PassWordEntry EntryComplete OnRelease ", input);
				m_inputManager.hideSoftInputFromWindow(m_Et_Input.getWindowToken(),
						0);
				if (m_entryUtil.getM_entryflag() == SYS_VIEW_MENU_COM_EntryUtil.TYPE_DEFAULT) {

					if (TYPE_WPAWPA2 == dealSecurity(m_scanedAPList.get(pos).capabilities)) {
						if (m_Et_Input.getText().toString().length() < 8) {
							SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
							m_Et_Input.getText().clear();
							if (null != m_commonInfoSetting) {
								if (m_commonInfoSetting.isForwardViewChanged()) {
								} else {
									m_commonInfoSetting.setForwardViewChanged(true);
									forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
								}
							}
							return;
						} else {
						}
					} else if (TYPE_WEP == dealSecurity(m_scanedAPList.get(pos).capabilities)) {
						if (m_Et_Input.getText().toString().length() < 5) {
							SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
							m_Et_Input.getText().clear();
							if (null != m_commonInfoSetting) {
								if (m_commonInfoSetting.isForwardViewChanged()) {
								} else {
									m_commonInfoSetting.setForwardViewChanged(true);
									forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
								}
							}
							return;
						} else {
						}
					}
					m_entryUtil.setM_pwd(m_Et_Input.getText().toString());

					if (WifiManager.WIFI_STATE_ENABLED == m_wifiAdmin.checkState()) {
						int sta = m_wifiAdmin.addNetwork(
								m_scanedAPList.get(pos).SSID, input,
								wifiType(m_scanedAPList.get(pos).capabilities),
								m_scanedAPList.get(pos).BSSID);
						m_wifiAdmin.sendManuallyNetworkBroadcast(sta, m_Context);//zy
						
						setScreenType();
						
						if (sta > -1) {
							SysLog.out(TAG,
									"COM_PassWordEntry IButtonActionListener",
									"netWorkID=" + sta);
							netID = sta;
							SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setNetID(netID);
							SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPasswordForward(true);
							backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
						} 
						else {
							SysLog.out(TAG,
									"COM_PassWordEntry IButtonActionListener",
									"connect failure(input error)");
							backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
						}
					}
				} else {
					if (m_entryUtil.getM_entryflag() == SYS_VIEW_MENU_COM_EntryUtil.TYPE_SSID) {
						if (m_Et_Input.getText().toString()
								.equals(m_entryUtil.getM_SSID())) {
						} else {
							m_entryUtil.setM_SSID(m_Et_Input.getText().toString());
						}
					}
					if (m_entryUtil.getM_entryflag() == SYS_VIEW_MENU_COM_EntryUtil.TYPE_PWD) {
						if (m_Et_Input.getText().toString()
								.equals(m_entryUtil.getM_pwdAdd())) {
						} else {
							if (TYPE_WPAWPA2 == dealSecurity(m_entryUtil
									.getM_securityText())) {
								if (m_Et_Input.getText().toString().length() < 8) {
									SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
									m_Et_Input.getText().clear();
								} else {
									m_entryUtil
									.setM_pwdAdd(m_Et_Input.getText().toString());
								}
							} else if (TYPE_WEP == dealSecurity(m_entryUtil
									.getM_securityText())) {
								if (m_Et_Input.getText().toString().length() < 5) {
									SysLog.out(TAG, "COM_PassWordEntry IButtonActionListener", "input illegal");
									m_Et_Input.getText().clear();
								} else {
									m_entryUtil
									.setM_pwdAdd(m_Et_Input.getText().toString());
								}
							}
						}
						SysLog.out(TAG, "COM_PassWordEntry btnListener",
								"ssid = " + m_entryUtil.getM_SSID() + ",pwd="
										+ m_entryUtil.getM_pwd() + ",type="
										+ m_entryUtil.getM_securityText());
					}
					if (null != m_commonInfoSetting) {
						if (m_commonInfoSetting.isForwardViewChanged()) {
						} else {
							m_commonInfoSetting.setForwardViewChanged(true);
							forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_NETWORKADD);
						}
					}

				}
			} else {

			}
			
		}

		@Override
		public void OnRelease(int id) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean OnLongClick(int id, int time) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean OnRepeat(int id, int... param) {
			// TODO Auto-generated method stub
			return false;
		}
		
	};


	@Override
	protected void onHide() {
		SysLog.out(TAG, "COM_PassWordEntry onHide",
				"setVisibility VISIBLE");
		m_inputManager.hideSoftInputFromWindow(m_Et_Input.getWindowToken(), 0);
		MessageControlIF.getIntance().closeMessageForSysView("WIFI_05_T1");
		
		if(null!=m_VKhandler){
			m_vkEdit.removeListener();
		}
		mKeyBoardWatch.removeLayoutObserver();

	}

	@Override
	protected boolean onKeyEvent(KeyEvent keyEv) {
		
		if (keyEv.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEv.getAction() == KeyEvent.ACTION_UP) {
			VoicePlayer.getInstance(m_Context).playbackVoiceById(VoicePlayer.VOICE_ID_BEEP_1_LOW,
					VoicePlayer.PRIORITY_CALL_AND_BEEP);
			// clear input character by IME
			if (mKeyBoardWatch.isVisible()) {

			} else if (mKeyBoardWatch.statusChange()) {// close keyboard by IME
				SysLog.out(TAG, "MMKeyBoardWatch", "mKeyBoardWatch.statusChange()--true");
				mKeyBoardWatch.resetStatus();
			} else {
				if (m_entryUtil.getM_entryflag() == SYS_VIEW_MENU_COM_EntryUtil.TYPE_DEFAULT) {
					backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_SCANNEDAP);
				} else {
					backDefaultViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_NETWORKADD);
				}
			}
			return true;
		}
		
		return false;
	}

	@Override
	protected void onHideFinished() {
		super.onHideFinished();
		if (null != m_commonInfoSetting) {
			m_commonInfoSetting.setForwardViewChanged(false);
		}
	}

	private int dealSecurity(String security) {
		if (security.contains("WPA")) {
			return TYPE_WPAWPA2;
		} else if (security.contains("WEP")) {
			return TYPE_WEP;
		} else {
			return TYPE_NOSECURITY;
		}
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

	private void setScreenType(){
		SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_screenType(ScreenType.PASSWORDENTRYVIEW);
	}

}
