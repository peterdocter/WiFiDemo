package jp.pioneer.ceam.view.menucommunication.wifi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import jp.pioneer.ceam.base.SysView_DEF.SysViewMWTitle;
import jp.pioneer.ceam.ctl.basecontrol.viewBase.CTL_Control_ListViewBase;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_PriorityAdjust;
import jp.pioneer.ceam.view.menucommunication.wifi.SYS_VIEW_MENU_COM_PriorityAdjust.PriorityDragListAdapter;

public class PriorityAdjustListView extends CTL_Control_ListViewBase {

	protected static WindowManager m_windowManager;
	private WindowManager.LayoutParams m_windowParams;

	// drag image
	protected static ImageView m_img_dragImageView;
	// src position in list
	protected static int m_i_dragSrcPosition = -1;
	// ready to drag position in list
	private int m_i_dragPosition;
	// current point
	private int m_i_dragPoint;
	// to top
	private int m_i_dragOffset;
	// up bounce when drag
	private int m_i_upScrollBounce;
	// down bounce when drag
	private int m_i_downScrollBounce;
	// list step
	private final static int m_i_step = 1;
	// current step
	private int m_i_current_Step;
	
	public PriorityAdjustListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/***
	 * onInterceptTouchEvent
	 * 
	 * @param ev
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		// down
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			
			
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			m_i_dragSrcPosition = m_i_dragPosition = pointToPosition(x, y);
			// not done
			if (m_i_dragPosition == AdapterView.INVALID_POSITION) {
				return super.onInterceptTouchEvent(ev);
			}
			if (m_i_dragSrcPosition >= getListAdapter().getChildCount()) {
				return super.onInterceptTouchEvent(ev);
			}
			// current view
			ViewGroup itemView = (ViewGroup) getChildAt(m_i_dragPosition
					- getFirstVisiblePosition());

			m_i_dragPoint = y - itemView.getTop();
			m_i_dragOffset = (int) (ev.getRawY() - y);

			
			m_i_upScrollBounce = getHeight() / 5;
			m_i_downScrollBounce = getHeight() * 4 / 5;

			// enable cache.
			itemView.setDrawingCacheEnabled(true);
			Bitmap bitmap = Bitmap.createBitmap(itemView.getDrawingCache());
			itemView.setDrawingCacheEnabled(false);
			
			Matrix matrix = new Matrix();
			matrix.postScale(1.0f, 1.0f);
			Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);

			startDrag(bm, y);
			notifyDataSetChanged();
		}else if(ev.getAction() == MotionEvent.ACTION_UP){
			stopDrag();
		}

		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * onTouchEvent
	 * 
	 * @param ev
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (m_img_dragImageView != null && m_i_dragPosition != INVALID_POSITION) {
			int action = ev.getAction();
			switch (action) {
			case MotionEvent.ACTION_UP:
				int upY = (int) ev.getY();
				stopDrag();
				onDrop(upY);
				break;
			case MotionEvent.ACTION_MOVE:
				int moveY = (int) ev.getY();
				onDrag(moveY);
				break;
			case MotionEvent.ACTION_DOWN:
				break;
			default:
				// 12-672-1-02462 (fix problem:avoid remain when double click)
				int up2Y = (int) ev.getY();
				stopDrag();
				onDrop(up2Y);
				break;
			}
			return true;
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * ready to drag
	 * 
	 * @param bm
	 * @param y
	 */
	private void startDrag(Bitmap bm, int y) {

		// init window
		m_windowParams = new WindowManager.LayoutParams(-1, 4);
		m_windowParams
				.setTitle(SysViewMWTitle.SYS_VIEW_MW_TITLE_SRC_SEL_WINDOW);
		m_windowParams.type = SYS_VIEW_MENU_COM_PriorityAdjust.m_wmParamsType;
		m_windowParams.gravity = Gravity.TOP;
		m_windowParams.x = 0;
		m_windowParams.y = y - m_i_dragPoint + m_i_dragOffset;
		m_windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		m_windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		m_windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

		m_windowParams.windowAnimations = 0;

		ImageView imageView = new ImageView(getContext());
		imageView.setImageBitmap(bm);
		m_windowManager = (WindowManager) getContext().getSystemService(
				"window");
		m_windowManager.addView(imageView, m_windowParams);
		m_img_dragImageView = imageView;

	}

	/**
	 * onDrag when moving
	 * 
	 * @param y
	 */
	public void onDrag(int y) {
		// drag top < 0
		int drag_top = y - m_i_dragPoint;
		if (m_img_dragImageView != null && drag_top >= 0) {
			m_windowParams.alpha = 0.8f;
			m_windowParams.y = y - m_i_dragPoint + m_i_dragOffset;
			m_windowManager.updateViewLayout(m_img_dragImageView,
					m_windowParams);
		}

		int tempPosition = pointToPosition(20, y);
		if (tempPosition != INVALID_POSITION) {
			m_i_dragPosition = tempPosition;

		}

		doScroller(y);
	}

	/***
	 * scroll list
	 * 
	 * @param y
	 */
	public void doScroller(int y) {

		// down
		if (y < m_i_upScrollBounce) {
			m_i_current_Step = m_i_step + (m_i_upScrollBounce - y);
		}// up
		else if (y > m_i_downScrollBounce) {
			m_i_current_Step = -(m_i_step + (y - m_i_downScrollBounce));
		} else {
			m_i_current_Step = 0;
		}

		View view = getChildAt(m_i_dragPosition - getFirstVisiblePosition());
		// true scroll
		setSelectionFromTop(m_i_dragPosition, view.getTop() + m_i_current_Step);

	}

	/**
	 * stopDrag
	 */
	public static void stopDrag() {
		if (m_img_dragImageView != null) {
			m_windowManager.removeView(m_img_dragImageView);
			m_img_dragImageView = null;
		}
	}

	/**
	 * drag when drop
	 * 
	 * @param y
	 */
	public void onDrop(int y) {

		int tempPosition = pointToPosition(0, y);
		if (tempPosition != INVALID_POSITION) {
			m_i_dragPosition = tempPosition;
		}

		// to top
		if (y < getChildAt(0).getTop()) {
			// top+
			m_i_dragPosition = 0;

		} else if (y > getChildAt(getChildCount() - 1).getBottom()) {
			// bottom-
			m_i_dragPosition = getListAdapter().getChildCount() - 1;
		}

		// data change
		if (m_i_dragPosition < getListAdapter().getChildCount()) {
			PriorityDragListAdapter adapter = (PriorityDragListAdapter) getListAdapter();
			adapter.update(m_i_dragSrcPosition, m_i_dragPosition);
			m_i_dragSrcPosition = -1;
			adapter.notifyDataSetChanged();

		}
	}

}