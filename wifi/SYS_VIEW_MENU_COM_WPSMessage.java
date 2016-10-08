package jp.pioneer.ceam.view.menucommunication.wifi;

import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ImageViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_TextViewBase.TypeFaceId;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_071_Button;
import jp.pioneer.ceam.manage.SysManagerIF;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.uicommonlib.TextTheme.TextListConstant.TextStyleID;
import jp.pioneer.ceam.uicommonlib.constant.SysViewConstant.SysViewKind;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SYS_VIEW_MENU_COM_WPSMessage extends SysView {
	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private Context m_Context;
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;
	private SysManagerWIFIAdmin m_wifiAdmin;
	
	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_Control_ViewGroupBase m_view_background = null;
	private CTL_Control_TextViewBase m_textView=null;
	private CTL_Control_ImageViewBase m_imageView=null;
	private CTL_14AJ_L_071_Button m_stopButton=null;
	
	public SYS_VIEW_MENU_COM_WPSMessage(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_WpsMessage onCreate", "onCreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_com_wpsmessage, null);
		if (null == m_view_main) {
			return null;
		}
		m_wifiAdmin=SysManagerWIFIAdmin.getInstance(m_Context);
		obtainCtrlViews();
		
		return m_view_main;
	}

	private void obtainCtrlViews() {
		if (null != m_view_main) {
			m_view_background = (CTL_Control_ViewGroupBase)m_view_main.findViewById(R.id.menu_com_wpsbackground);
			m_textView=(CTL_Control_TextViewBase)m_view_main.findViewById(R.id.menu_com_wpstextview);
			m_imageView=(CTL_Control_ImageViewBase)m_view_main.findViewById(R.id.menu_com_wpsimage);
			m_stopButton=(CTL_14AJ_L_071_Button)m_view_main.findViewById(R.id.menu_com_wpsstopbutton);
		}
		if(null != m_view_background){
			m_view_background.setBackgroundResource(com.android.internal.R.drawable.p1900_messageplate);
		}
		
		if(null!=m_textView){
			m_textView.setVisibility(View.VISIBLE);
			m_textView.setTextFont(TypeFaceId.HG);
			m_textView.setText(com.android.internal.R.string.WIFI_15_T1);
		}
		if(null!=m_imageView){
			m_imageView.setVisibility(View.VISIBLE);
			m_imageView.setBackgroundResource(com.android.internal.R.drawable.p10608_wifiwps);
		}
		
		if(null!=m_stopButton){
			m_stopButton.setVisibility(View.VISIBLE);
			m_stopButton.setTextFont(TypeFaceId.HG);
			m_stopButton.setTextStyle(TextStyleID.TEXT_007);
			m_stopButton.setGravity(Gravity.CENTER);
			m_stopButton.setButtonSize(196, 60);
			m_stopButton.setTextSize(30);
			m_stopButton.setText(com.android.internal.R.string.MSG_BTN_002);
		}
		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);
		
	}

	@Override
	protected void onShow() {
		if (null != m_stopButton && null != btnListener) {
			m_stopButton.setButtonListener(
					IButtonActionListener.BUTTON_EVENT.NORMAL, btnListener,
					null,true);
		}
		
	}

	@Override
	protected void onHide() {
		if (null != m_wifiAdmin) {
			m_wifiAdmin.cancelWps();
		}
		if(null!=m_stopButton){
			m_stopButton.removeListener();
		}
		SysManagerIF.instance().closeViewByKind(SysViewKind.KIND_WPSMESSAGE);
		
	}
	
	
	@Override
	protected void onHideFinished() {
		super.onHideFinished();
		if (null != m_commonInfoSetting) {
			m_commonInfoSetting.setForwardViewChanged(false);
		}
	}
	
	
	private IButtonActionListener btnListener = new IButtonActionListener() {

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
			SysLog.out(TAG, "COM_WpsMessage btnListener", "OnPush");
			VoicePlayer.getInstance(m_Context).playbackVoiceById(VoicePlayer.VOICE_ID_BEEP_1_LOW, VoicePlayer.PRIORITY_CALL_AND_BEEP);
			m_wifiAdmin.cancelWps();
			if(null!=m_commonInfoSetting){
				if(m_commonInfoSetting.isForwardViewChanged()){
				}else{
					m_commonInfoSetting.setForwardViewChanged(true);
					SysManagerIF.instance().closeViewByKind(SysViewKind.KIND_WPSMESSAGE);
				}
			}
			
		}

		@Override
		public void OnRelease(View view) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
}
