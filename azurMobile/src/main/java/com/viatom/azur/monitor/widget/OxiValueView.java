package com.viatom.azur.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.viatom.azur.monitor.utils.MsgUtils;
import com.viatom.newazur.R;

public class OxiValueView extends View{

	private static final int MSG_REDRAW = 1001;
	
	private Context context;
	private int curVal = 0;
	
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_REDRAW:
				invalidate();
				break;

			default:
				break;
			}
			
		};
	};
	
	public OxiValueView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		drawRect(canvas);
	}
	
	
	private void drawRect(Canvas canvas) {
		Paint rectPaint = new Paint();
		rectPaint.setColor(context.getResources().getColor(R.color.MonitorSkyBlue));
		rectPaint.setStyle(Paint.Style.STROKE);
		rectPaint.setStrokeWidth(getWidth());
		
		int rectH = (int)((float)(200 - curVal) / 0xFF * getHeight());
//		Rect rect = new Rect(0, rectH, getWidth(), 0);
//		canvas.drawRect(rect, rectPaint);
		
		canvas.drawLine(0, getHeight(), 0, getHeight() - rectH, rectPaint);
//		canvas.drawRect(0, 100, getWidth(), 0, rectPaint);
		
	}
	
	public void reDraw(int val) {
		if (val >= 0) {
			this.curVal = val;
		}
		MsgUtils.sendMsg(handler, MSG_REDRAW);
	}
}
