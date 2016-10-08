package jp.pioneer.ceam.view.menucommunication.wifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import jp.pioneer.ceam.base.SysView_DEF.SysLog;
import jp.pioneer.ceam.base.SysView_DEF.VType;
import jp.pioneer.ceam.base.SysView_DEF.ViewID;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListener;
import jp.pioneer.ceam.ctl.Interface.common.IButtonActionListenerLongRepeat;
import jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_Control_ViewGroupBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewAdapterBase;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewScrollBar.ScrollBarType;
import jp.pioneer.ceam.ctl.common.CTL_CommonOnProhibition;
import jp.pioneer.ceam.ctl.common.CTL_CommonStructure.Position;
import jp.pioneer.ceam.ctl.common.CTL_Constant.TitleBar_Layout_Pattern;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_14AJ_L_625_2_PositionInfo.PostionType;
import jp.pioneer.ceam.ctl.designControl.Button.CTL_16AVHN_L_710_wifi_Button;
import jp.pioneer.ceam.ctl.designControl.ListView.CTL_14AJ_L_600_ListView;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar;
import jp.pioneer.ceam.ctl.designControl.TitleBar.CTL_16AVHN_L_004_Titlebar.CTL_16AJ_L_004_0_Titlebar_ButtonId;
import jp.pioneer.ceam.manage.SysManagerWIFIAdmin;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfo;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoCommon;
import jp.pioneer.ceam.manage.viewcommoninfo.SysViewCommonInfoMenuSettings;
import jp.pioneer.ceam.manage.viewcontrol.SysView;
import jp.pioneer.ceam.sysservice.R;
import jp.pioneer.ceam.view.srcsel.DragListView;
import jp.pioneer.ceam.view.srcsel.SYS_VIEW_MENU_SRC_SEL_MAIN_DEF;
import jp.pioneer.ceam.widget.SettingServer.SettingServerController;
import jp.pioneer.ceam.widget.VoicePlayer.VoicePlayer;

public class SYS_VIEW_MENU_COM_PriorityAdjust extends SysView {

	private static final String TAG = SysLog.LOG_ID_SYS_VIEW_TMEP;
	private Context m_Context;
	public static int m_wmParamsType = -1;
	public static CTL_16AVHN_L_710_wifi_Button listitem = null;//

	private CTL_Control_ViewGroupBase m_view_main = null;
	private CTL_Control_ViewGroupBase m_view = null;
	private CTL_16AVHN_L_004_Titlebar m_titleBar = null;

	private PriorityAdjustListView m_dragListView = null;
//	private CTL_14AJ_L_600_ListView m_listView = null;
	private SysManagerWIFIAdmin m_wifiAdmin = null;

	private PriorityDragListAdapter m_mainListAdapter = null;
	private AttributeSet attrs = null;
	private SysViewCommonInfoMenuSettings m_commonInfoSetting = null;
	private List<WifiRegisteredAP> m_registeredAP = new ArrayList<WifiRegisteredAP>();

	public SYS_VIEW_MENU_COM_PriorityAdjust(Context context) {
		super(context);
		this.m_Context = context;
	}

	@Override
	protected View onCreate(LayoutInflater inflater,
			CTL_Control_ViewGroupBase root, LayoutParams childViewParams) {
		SysLog.out(TAG, "COM_PriorityAdjust oncreate", "oncreate");
		m_view_main = (CTL_Control_ViewGroupBase) getScreenView(inflater,
				R.layout.menu_com_priorityadjust, null);
		m_view_main
				.setBackgroundResource(com.android.internal.R.drawable.p4001_bgp1_t1);

		if (null == m_view_main) {
			return null;
		}
		m_wmParamsType = getWMParams().type;
		
		m_wifiAdmin = SysManagerWIFIAdmin.getInstance(m_Context);
		

		obtainCtrlViews();

		return m_view_main;
	}

	private void obtainCtrlViews() {
		SysLog.out(TAG, "COM_PriorityAdjust obtainCtrlViews", "obtainCtrlViews");
		if (null != m_view_main) {
			m_view = (CTL_Control_ViewGroupBase) m_view_main
					.findViewById(R.id.menu_com_priorityadjustview);
			m_dragListView = (PriorityAdjustListView) m_view_main
					.findViewById(R.id.menu_com_sel_main_listView);
//			m_listView = (CTL_14AJ_L_600_ListView) m_view_main
//					.findViewById(R.id.menu_com_wifiaccesspoint_list);
			m_titleBar = (CTL_16AVHN_L_004_Titlebar) m_view_main
					.findViewById(R.id.menu_com_priorityadjust_title_bar);
		}

		if (null != m_dragListView) {
//			if(0==SysViewCommonInfoCommon.getXOffSet()){
//				jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_RelativeLayout.LayoutParams sp =(jp.pioneer.ceam.ctl.basecontrol.layoutBase.CTL_RelativeLayout.LayoutParams) m_dragListView.getLayoutParams();
//				sp.leftMargin=20;
//				m_dragListView.setLayoutParams(sp);
//			}
		}

//		if(null!=m_listView){
//			m_listView.setVisibility(View.INVISIBLE);
//		}
		

		if (null != m_view_main && null != m_dragListView) {
			XmlPullParser parser2 = m_view_main.getResources().getXml(
					R.layout.menu_com_registeredaccesspoint);
			attrs = Xml.asAttributeSet(parser2);
			m_mainListAdapter = new PriorityDragListAdapter(m_dragListView.getContext(),
					attrs);
			m_dragListView.setScrollBarVerticalEnable(true);
			m_dragListView.getScrollBarVertical().setScrollBarType(
					ScrollBarType.TYPE_14AJ_L_002);
			m_dragListView.setPageChildrenCount(5, true);
			m_dragListView.setAdapter(m_mainListAdapter);
			m_dragListView.notifyDataSetChanged();

		}

		m_commonInfoSetting = (SysViewCommonInfoMenuSettings) SysViewCommonInfo
				.getComomnInfo(VType.SYS_VIEW_TYPE_MENU_SETTINGS);

	}

	@Override
	protected void onShow() {
		SysLog.out(TAG, "COM_PriorityAdjust onShow", "onShow");
		
		if (null != m_titleBar) {
			m_titleBar
					.setLayoutPatternID(TitleBar_Layout_Pattern.RETURN_TEXT_ESC);
			m_titleBar.setTitleText(com.android.internal.R.string.WIFI_01_T1);
			
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BACK)
					.setViewOnProhibitionEnabled(false);
			m_titleBar
					.getTitleBarButton(CTL_16AJ_L_004_0_Titlebar_ButtonId.ESC)
					.setViewOnProhibitionEnabled(false);
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT)
					.setViewOnProhibitionEnabled(true);
			m_titleBar.initButtonWifi();
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_MIDDLE)
					.setViewOnProhibitionEnabled(false);
			m_titleBar.getTitleBarButton(
					CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_RIGHT)
					.setViewOnProhibitionEnabled(false);
		}
		
		m_registeredAP = SYS_VIEW_MENU_COM_WifiAPUtil.getInstance()
				.getM_registeredAP();
		if(null!=m_dragListView){
			m_dragListView.notifyDataSetChanged();
		}
		addViewCtrlListener();

	}

	private void addViewCtrlListener() {
		if (null != m_titleBar && null != TitleBarListener) {
			m_titleBar.setTitlebarListener(TitleBarListener,
					IButtonActionListener.BUTTON_EVENT.NORMAL, null);
		}
		if (null != m_dragListView) {

			m_dragListView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					int action = event.getAction();
					int y=(int)event.getY();
					if((y>(m_registeredAP.size())*70)
						|| ((MotionEvent.ACTION_DOWN != action)
								&& (MotionEvent.ACTION_MOVE != action) && (MotionEvent.ACTION_UP != action))) {
					event.setAction(MotionEvent.ACTION_CANCEL);
					}

					return false;
				}
			});
			
		}
	}
	
	@Override
	protected void onProhibition() {
		super.onProhibition();
		if(!CTL_CommonOnProhibition.getInstance().isProhibition()){
			forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
		}
	}

	@Override
	protected void onHide() {
		SysLog.out(TAG, "COM_PriorityAdjust onHide", "onHide");
		if (null != m_dragListView) {
			m_dragListView.setButtonListener(null);
		}
		if (null != m_titleBar) {
			m_titleBar.removeListener();
		}
		
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
		public void OnPush(int id) {
			if (CTL_16AJ_L_004_0_Titlebar_ButtonId.BUTTON_LEFT == id) {
				if (m_wifiAdmin.isFastDoubleClick()) {
					return;
				}
				VoicePlayer.getInstance(m_Context).playbackVoiceById(
						VoicePlayer.VOICE_ID_BEEP_1_HIGH,
						VoicePlayer.PRIORITY_CALL_AND_BEEP);
				if (null != m_commonInfoSetting) {
					if (m_commonInfoSetting.isForwardViewChanged()) {
					} else {
						m_commonInfoSetting.setForwardViewChanged(true);
						forwardViewChange(ViewID.SYS_VIEW_ID_MENU_COM_WIFI_REGISTEREDAP);
					}
				}
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

	public class PriorityDragListAdapter extends
			CTL_Control_ListViewAdapterBase<CTL_16AVHN_L_710_wifi_Button> {

		private Context context;

		public PriorityDragListAdapter(Context context, AttributeSet attrs) {
			super(context, attrs);
			this.context = context;
		}

		/***
		 * update list
		 * 
		 * @param start
		 * @param down
		 */
		public void update(int start, int down) {
			// get del item
			WifiRegisteredAP ap= m_registeredAP.get(start);
			WifiRegisteredAP end= m_registeredAP.get(down);
			
			int endNetId=m_registeredAP.get(down).getmConfig().networkId;
			int startNetId=m_registeredAP.get(start).getmConfig().networkId;
			int startPriority=m_wifiAdmin.getAutoConnectPriority(startNetId);
			int endPriority=m_wifiAdmin.getAutoConnectPriority(endNetId);
			
			List<WifiRegisteredAP> m_registered = new ArrayList<WifiRegisteredAP>();
			
			for(int i = 0; i< m_registeredAP.size();i++){
				if(i == start){
					m_registered.add(end);
					m_wifiAdmin.setAutoConnectPriority(endNetId, startPriority);
					m_wifiAdmin.saveConfiguration();
				}else if(i == down){
					m_registered.add(ap);
					m_wifiAdmin.setAutoConnectPriority(startNetId, endPriority);
					m_wifiAdmin.saveConfiguration();
				}else{
					m_registered.add(m_registeredAP.get(i));
				}	
			}
			
			m_registeredAP.clear();
			m_registeredAP.addAll(m_registered);
			
			SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setPriorityAdjusted(true);
			SYS_VIEW_MENU_COM_WifiAPUtil.getInstance().setM_registeredAP(m_registeredAP);
			
		}
		
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

		@Override
		public boolean fillView(String key, int position,
				CTL_16AVHN_L_710_wifi_Button item) {
			if (null != m_commonInfoSetting) {
				SysLog.out(
						TAG,
						"COM_PriorityAdjust  darglist fillview",
						"m_registeredAP.size()= "
								+ m_registeredAP.size()
								+ ", position = "
								+ position
								+ ",priority="
								+ m_wifiAdmin
										.getAutoConnectPriority(m_registeredAP
												.get(position).getmConfig().networkId));
				if (null != m_registeredAP && m_registeredAP.size() > 0) {
					WifiRegisteredAP m_config = m_registeredAP.get(position);

					if (m_config != null
							&& (m_config.getmConfig().wepKeys) != null
							&& (m_config.getmConfig().wepKeys.length > 0)) {
						item.setPriorityItem();
						item.setText(CTL_16AVHN_L_710_wifi_Button.TEXTID_NAME,
								dealSSID(m_config.getmConfig().SSID));
					}
				} 
			}
			item.setViewActiveDisable(false);
			return true;

		}

		@Override
		public void fillEmptyView(String key, int position,
				CTL_16AVHN_L_710_wifi_Button item) {
			item.setButtonContentEmpty();
			item.setViewNormal();
		}

		@Override
		public CTL_16AVHN_L_710_wifi_Button getNewT() {
			listitem=new CTL_16AVHN_L_710_wifi_Button(m_Context, m_Attrs);
			return listitem;
		}
		@Override
		protected void updateEmptyView(String key, int position,
				CTL_16AVHN_L_710_wifi_Button item) {
			item.setButtonContentEmpty();
		}
		

	}
			
	private String dealSSID(String SSID) {
		return SSID.replaceAll("\"", "");
	}

}
