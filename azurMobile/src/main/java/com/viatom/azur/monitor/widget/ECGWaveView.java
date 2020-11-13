package com.viatom.azur.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ECGWaveView extends RTWaveView{

	public static final float RULER_SCALE_5 = 5;
	public static final float RULER_SCALE_10 = 10;
	public static final float RULER_SCALE_20 = 20;

	private float ppi = 1;
	private float rulerScale = 10; // default:10mm/mv

	public ECGWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ECGWaveView(Context context, WaveViewParameters parameters) {
		super(context, parameters);
		// TODO Auto-generated constructor stub
	}

	public ECGWaveView(Context context, WaveViewParameters parameters, float ppi) {
		super(context, parameters);
		// TODO Auto-generated constructor stub
		this.ppi = ppi <=0 ? 1 : ppi;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawRuler(canvas);
	}

	@Override
	protected void drawPath(Canvas canvas) {
		// TODO Auto-generated method stub
		if (canvas == null || drawList == null) {
			return;
		}
		Paint pathPaint = new Paint();
		pathPaint.setAntiAlias(true);
		pathPaint.setColor(parameters.getColor());
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeWidth(3.5f);

		Path path = new Path();

		synchronized (drawList) {

			for (int i = 0; i < drawList.size(); i += parameters.getSampleDis()) {
				float curData = drawList.get(i) == null ? 0 : drawList.get(i);
				float curDataMV = ((curData * 4033) / (32767 * 12)) * 1.05f;
				float curDataPx = curDataMV * rulerScale / 25.4f * ppi; //实际坐标
				float x = i * parameters.getxDis();
				float y = parameters.getHeight()/2 - curDataPx;
				//超出边界不画
				if (y < 0) {
					y = 0;
				}else if (y > getHeight()) {
					y = getHeight();
				}

				// Don't connect at curIndex point
				if (Math.abs(i-curIndex) < parameters.getSampleDis() || i == 0) {
					path.moveTo(x, y);
				}else {
					path.lineTo(x, y);
				}

			}
		}
		canvas.drawPath(path, pathPaint);
	}

	/**
	 * 画标尺
	 * @param canvas
	 */
	private void drawRuler(Canvas canvas) {
		if (canvas == null) {
			return;
		}
		Paint rulerPaint = new Paint();
		rulerPaint.setAntiAlias(true);
		rulerPaint.setColor(Color.GRAY);
		rulerPaint.setStyle(Paint.Style.STROKE);
		rulerPaint.setStrokeWidth(4f);

		Paint textPaint = new Paint();
		textPaint.setStrokeWidth(2f);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(18);

		//计算标尺长度(PX)
		float totalHeight = getHeight();
		float rulerScaleInch = rulerScale / 25.4f;
		float rulerHeight = rulerScaleInch * ppi;
		float rulerWidth = 10; //自定义
		float startX = 5; //从某X处开始画

		//上横杠
		canvas.drawLine(startX, totalHeight/2 - rulerHeight/2, startX + rulerWidth
				, totalHeight/2 - rulerHeight/2, rulerPaint);
		//竖线
		canvas.drawLine(startX + rulerWidth/2, totalHeight/2 - rulerHeight/2
				, startX + rulerWidth/2, totalHeight/2 + rulerHeight/2, rulerPaint);
		//下横杠
		canvas.drawLine(startX, totalHeight/2 + rulerHeight/2, startX + rulerWidth
				, totalHeight/2 + rulerHeight/2, rulerPaint);
		//文字
//		canvas.drawText("1mV", startX + rulerWidth/2, totalHeight/2 - rulerHeight
//				- rulerPaint.getTextSize(), textPaint);
	}


	/**
	 * Change ruler scale
	 * @param rulerScale
	 */
	public void changeRulerScale(float rulerScale) {
		if (rulerScale < 5 || rulerScale > RULER_SCALE_20) {
			return;
		}
		this.rulerScale = rulerScale;

//		if (drawList != null) {
//			synchronized (drawList) {
//			drawList.clear();
//			}
//		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (rulerScale == RULER_SCALE_5) {
				rulerScale = RULER_SCALE_10;
			}else if (rulerScale == RULER_SCALE_10) {
				rulerScale = RULER_SCALE_20;
			}else if (rulerScale == RULER_SCALE_20) {
				rulerScale = RULER_SCALE_5;
			}else {
				rulerScale = RULER_SCALE_10;
			}
			changeRulerScale(rulerScale);
		}

		return true;
	}

}
