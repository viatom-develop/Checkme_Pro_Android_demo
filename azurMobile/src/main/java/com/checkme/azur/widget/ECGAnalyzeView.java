package com.checkme.azur.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.ECGInnerItem;

public class ECGAnalyzeView extends View {
	int[] chartY;
	float xDis;
	int FirstSample;
	final float chartStartX = 15;
	public float preTempX = 0, preTempY = 0;
	public float preTouchX = 0;
	public boolean isTouching = false;
	public float CurrentMoveX = 0;
	public float touchX = 0;
	boolean firstTimeDraw = true;
	public float ChartLineLength;
	float DrawSampleNum;// 一屏画的点数
	int screenWidth, screenHeight;
	private Paint linePaint, textPaint;
	private SeekBar seekBar;
	// 标尺相关
	final float rulerStandardWidth = 20;
	final float rulerZeroWidth = 13;
	final float rulerTotalWidth = rulerStandardWidth + 2 * rulerZeroWidth;
	final float standard1mV = (32768 / 4033) * 12 * 8;
	final float DisRulerChart = 15; // 标尺、图表间距

	private float grid1mmLength;
	private Paint gridPaint1mm, gridPaint5mm;

	public ECGAnalyzeView(Context context, ECGInnerItem item, int FirstSample,
			int ScreenW, int ScreenH) {
		super(context);
		chartY = item.getECGData();
		this.FirstSample = FirstSample;
		this.screenWidth = ScreenW;
		this.screenHeight = ScreenH;
		InitFixParams();
		InitPaint();
	}

	/**
	 * 根据屏幕尺寸初始化相关参数
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 */
	public void InitFixParams() {
		grid1mmLength = screenHeight / 40;
		xDis = (float) 25 * grid1mmLength / Constant.ECG_DATA_SAMPLING_FREQUENCY; // 25mm/s,25*grid1mmLength/(500Hz)
		DrawSampleNum = screenWidth / xDis;
		ChartLineLength = xDis * (float) (chartY.length);
	}

	/**
	 * 初始化画笔
	 */
	public void InitPaint() {

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setStrokeWidth((float) 2);
		linePaint.setColor(Color.argb(255, 0, 0, 0));

		textPaint = new Paint();
		textPaint.setTextSize(25);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setStrokeWidth((float) 1.8);
		textPaint.setColor(Color.argb(255, 0, 00, 0));// 黑线
		textPaint.setFakeBoldText(true);

//		gridPaint1mm = new Paint();
//		gridPaint1mm.setAntiAlias(true);
//		gridPaint1mm.setStyle(Paint.Style.FILL);
//		gridPaint1mm.setStrokeWidth(0.1f);
//		gridPaint1mm.setColor(Color.RED);
//		gridPaint1mm.setAlpha(150);
//
//		gridPaint5mm = new Paint();
//		gridPaint5mm.setAntiAlias(true);
//		gridPaint5mm.setStyle(Paint.Style.FILL);
//		gridPaint5mm.setStrokeWidth(0.8f);
//		gridPaint5mm.setColor(Color.RED);
//		gridPaint5mm.setAlpha(150);
		
		gridPaint1mm = new Paint();
		gridPaint1mm.setAntiAlias(true);
		gridPaint1mm.setStyle(Paint.Style.FILL);
		gridPaint1mm.setStrokeWidth(1);
		gridPaint1mm.setColor(Color.argb(255, 255, 240, 210));
		
		gridPaint5mm = new Paint();
		gridPaint5mm.setAntiAlias(true);
		gridPaint5mm.setStyle(Paint.Style.FILL);
		gridPaint5mm.setStrokeWidth(1);
		gridPaint5mm.setColor(Color.argb(255, 255, 220, 190));
	}

	protected void onDraw(Canvas canvas) {
		drawGrid(canvas);
		drawAxis(canvas);
		DrawPath(canvas);
	}

	public void drawGrid(Canvas canvas) {
		// 1mm
		for (int i = 0; i < screenWidth; i += grid1mmLength)
			canvas.drawLine(i, 0, i, screenHeight, gridPaint1mm);
		for (int i = 0; i < screenHeight; i += grid1mmLength)
			canvas.drawLine(0, i, screenWidth, i, gridPaint1mm);
		// 5mm
		for (int i = 0; i < screenWidth; i += grid1mmLength * 5)
			canvas.drawLine(i, 0, i, screenHeight, gridPaint5mm);
		for (int i = 0; i < screenHeight; i += grid1mmLength * 5)
			canvas.drawLine(0, i, screenWidth, i, gridPaint5mm);
	}

	/**
	 * 主要画标尺
	 * 
	 * @param canvas
	 * @param Y
	 */
	public void drawAxis(Canvas canvas) {
		float ZeroLine = 0;
		float StandardLine = 0;

		// 锟斤拷锟斤拷锟�
		ZeroLine = grid1mmLength*10*2;//自上而下2cm处
		StandardLine = ZeroLine - grid1mmLength * 10;
		canvas.drawLine(chartStartX, ZeroLine, chartStartX + rulerZeroWidth,
				ZeroLine, linePaint);// 锟斤拷一锟斤拷0锟斤拷
		canvas.drawLine(chartStartX + rulerZeroWidth, ZeroLine, chartStartX
				+ rulerZeroWidth, StandardLine, linePaint);// 锟斤拷一锟斤拷锟斤拷锟斤拷
		canvas.drawLine(chartStartX + rulerZeroWidth, StandardLine, chartStartX
				+ rulerZeroWidth + rulerStandardWidth, StandardLine, linePaint);// 锟斤拷准1mV锟斤拷
		canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
				ZeroLine, chartStartX + rulerZeroWidth + rulerStandardWidth,
				StandardLine, linePaint);// 锟节讹拷锟斤拷锟斤拷锟斤拷
		canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
				ZeroLine,
				chartStartX + rulerZeroWidth * 2 + rulerStandardWidth,
				ZeroLine, linePaint);// 锟节讹拷锟斤拷0锟斤拷

		float textDis = textPaint.getTextSize() + 5;
		canvas.drawText("1mV", chartStartX, ZeroLine + textDis * 1, textPaint);
	}

	public void DrawPath(Canvas canvas) {
		if (firstTimeDraw == true) {
			// 进来时的位置
			CurrentMoveX = -(((float) FirstSample) / (float) (chartY.length))
					* (ChartLineLength);
			if (-CurrentMoveX > ChartLineLength - screenWidth)// 超出右边界则等于右边界
				CurrentMoveX = -(ChartLineLength - screenWidth);
			canvas.translate(CurrentMoveX, 0);
			firstTimeDraw = false;// 此后永远是false
		} else
			canvas.translate(CurrentMoveX, 0);

		int CurrentStartSample = (int) ((-CurrentMoveX / ChartLineLength) * chartY.length);// 超出左左边界的部分不重绘

		// 开始X+要画的X如果小于Y.length，就话要画的X
		// 如果超过了Y.length，就画到Y.length
		for (int i = CurrentStartSample, K = 0; i < ((CurrentStartSample + (int) DrawSampleNum) < chartY.length ? (CurrentStartSample + (int) DrawSampleNum)
				: chartY.length); i++, K++) {
			float tempX = chartStartX + rulerTotalWidth + DisRulerChart
					- CurrentMoveX + K * xDis;
			float tempY = grid1mmLength*10*2 - (chartY[i] / standard1mV)
					* grid1mmLength * 10;//2CM处为基线
			if (K == 0) {
				preTempX = 0;
				preTempY = 0;
			}
			if (preTempX != 0 && preTempY != 0)
				canvas.drawLine(preTempX, preTempY, tempX, tempY, linePaint);
			preTempX = tempX;
			preTempY = tempY;
		}
	}

	public int getLength() {
		return (int) ChartLineLength;
	}

	public void setCurrentX(int CurrentX) {
		this.CurrentMoveX = -CurrentX;
		if (-this.CurrentMoveX > ChartLineLength - screenWidth + 100)
			this.CurrentMoveX = -(ChartLineLength - screenWidth + 100);
		invalidate();
	}

	public void setSeekBar(SeekBar seekBar) {
		this.seekBar = seekBar;
		CurrentMoveX = -(((float) FirstSample) / (float) (chartY.length))
				* (ChartLineLength);
		if (-CurrentMoveX > ChartLineLength - screenWidth)// 超出右边界则等于右边界
			CurrentMoveX = -(ChartLineLength - screenWidth);
		this.seekBar.setProgress(-(int) CurrentMoveX);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			preTouchX = event.getX();
			isTouching = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			isTouching = false;
		}
		if (isTouching == true) {
			float TempX = event.getX();
			touchX = TempX - preTouchX;
			preTouchX = TempX;
			CurrentMoveX += touchX;
			if (CurrentMoveX > 0)
				CurrentMoveX = 0;
			if (-CurrentMoveX > ChartLineLength - screenWidth + 100)
				CurrentMoveX = -(ChartLineLength - screenWidth + 100);
			seekBar.setProgress((int) -CurrentMoveX);
			invalidate();
		}
		return true;
	}
}
