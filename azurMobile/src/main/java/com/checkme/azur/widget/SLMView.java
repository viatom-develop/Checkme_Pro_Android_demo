package com.checkme.azur.widget;

import com.checkme.azur.utils.LogUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

public class SLMView extends View {

	//固定参数
	final float ChartStartX = 50, ChartStartY = 30;
	final float minSPO2 = 65, maxSPO2 = 100, minPR = 30, MaxPR = 240 , yStepSPO2 = 5, yStepPR = 30;
	final int sampleNum = 350;// 一屏显示多少个点
	final int lineNum = 8;
	final int axisXStep = sampleNum/5;// x坐标间隔

	//数据
	private int[] spo2Y,prY;
	private String[] chartX;

	//画图
	private int screenW;
	private float lineDis, chartEndY, chartEndX, xDis, ChartLineLength;
	private Paint axisTextPaint, oxygenPathPaint, prPathPaint,
			axisLinePaint, rectPaint,spo2AxisTextPaint,prAxisTextPaint;

	//动态参数
	private float currentX = 0, moveX = 0, preX = 0, preTempX = 0, preTempY = 0;
	private boolean touching = false;
	private SeekBar seekBar;


	public SLMView(Context context, String[] X, int[] OxygenY, int[] PRY,
				   int ScreenW, int ScreenH, SeekBar seekBar) {
		super(context);
		this.spo2Y = OxygenY;
		this.prY = PRY;
		this.chartX = X;
		this.screenW = ScreenW;
		this.seekBar = seekBar;
		initPaint();
		initFixParams(ScreenW, ScreenH);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		drawAxis(canvas, chartX);
		drawSPO2Path(canvas, spo2Y, oxygenPathPaint);
		drawPRPath(canvas, prY, prPathPaint);
		drawYText(canvas);
	}

	/**
	 * 画坐标轴
	 *
	 * @param canvas
	 * @param X
	 * @param Y
	 */
	public void drawAxis(Canvas canvas, String[] X) {
		canvas.translate(currentX, 0);
		// 画横线
		for (int i = 0; i < lineNum; i++) {
			canvas.drawLine(ChartStartX, ChartStartY + i * lineDis, ChartStartX
					+ ChartLineLength, ChartStartY + i * lineDis, axisLinePaint);
		}
		int CurrentStartSample = (int) ((-currentX / ChartLineLength) * X.length);// 超出左左边界的部分不重绘
		for (int i = CurrentStartSample; i < ((CurrentStartSample + (int) sampleNum) < X.length ? (CurrentStartSample + (int) sampleNum)
				: X.length); i += axisXStep) {
			// X轴竖线
			canvas.drawLine(ChartStartX + i * xDis, chartEndY - 3, ChartStartX
					+ i * xDis, chartEndY + 3, axisTextPaint);
			// X轴文字
			canvas.drawText(X[i], ChartStartX + i * xDis, chartEndY + 20,
					axisTextPaint);
		}
	}

	/**
	 * 画曲线
	 *
	 * @param canvas
	 * @param X
	 * @param Y
	 */
	public void drawSPO2Path(Canvas canvas, int[] Y, Paint LinePaint) {
		boolean dumpLine = false;//脱落，虚线
		int CurrentStartSample = (int) ((-currentX / ChartLineLength) * Y.length);// 超出左左边界的部分不重绘
		for (int i = CurrentStartSample, k = 0; i < ((CurrentStartSample + (int) sampleNum)
				< Y.length ? (CurrentStartSample + (int) sampleNum)
				: Y.length); i++, k++) {
			float tempX = ChartStartX + -currentX + k * xDis;
			if (i == CurrentStartSample) {
				preTempX = 0;
				preTempY = 0;
			}
			if (Y[i] != 0xFF) {// 0xFF为错误值，忽略不画
				float tempY = chartEndY - ((Y[i] - minSPO2) / (maxSPO2 - minSPO2))
						* (chartEndY - ChartStartY);
				if (preTempX != 0 && preTempY != 0) {
					if(!dumpLine){
						canvas.drawLine(preTempX, preTempY, tempX, tempY, LinePaint);
					}
				}
				preTempX = tempX;
				preTempY = tempY;
				dumpLine = false;
			}else{
				dumpLine = true;//错误标记
			}
		}
	}

	public void drawPRPath(Canvas canvas, int[] Y, Paint LinePaint) {

		boolean dumpLine = false;//脱落，虚线
		int CurrentStartSample = (int) ((-currentX / ChartLineLength) * Y.length);// 超出左左边界的部分不重绘
		for (int i = CurrentStartSample, k = 0; i < ((CurrentStartSample + (int) sampleNum)
				< Y.length ? (CurrentStartSample + (int) sampleNum)
				: Y.length); i++, k++) {
			float tempX = ChartStartX + -currentX + k * xDis;
			if (i == CurrentStartSample) {
				preTempX = 0;
				preTempY = 0;
			}
			if (prY[i] != 0xFF) {// 0xFF为错误值，忽略不画
				float tempY = chartEndY - ((Y[i] - minPR) / (MaxPR - minPR))
						* (chartEndY - ChartStartY);
				if (preTempX != 0 && preTempY != 0) {
					if(!dumpLine){
						canvas.drawLine(preTempX, preTempY, tempX, tempY, LinePaint);
					}
//					else{
//						canvas.drawLine(preTempX, preTempY, tempX, tempY, LinePaint);
//					}
				}
				preTempX = tempX;
				preTempY = tempY;
				dumpLine = false;
			}else {
				dumpLine = true;//错误标记
			}
		}
	}



	/**
	 * 画Y轴刻度
	 *
	 * @param canvas
	 * @param Y
	 */
	public void drawYText(Canvas canvas) {
		canvas.translate(-currentX, 0);
		canvas.drawRect(0, 0, ChartStartX - 10, chartEndY + 100, rectPaint);// 画一个白色矩形遮盖趋势图
		canvas.drawRect(chartEndX, 0, screenW, chartEndY + 100, rectPaint);// 画一个白色矩形遮盖趋势图
		// 画横线和纵轴点
		for (int i = 0; i < lineNum; i++) {
			canvas.drawText(String.valueOf((int)(maxSPO2 - i*yStepSPO2)) + "%",
					0, ChartStartY + i * lineDis + 3,
					spo2AxisTextPaint);
			canvas.drawText(String.valueOf((int)(MaxPR - i*yStepPR)),
					chartEndX+5, ChartStartY + i * lineDis + 3,
					prAxisTextPaint);
		}

		// 画图例
		// 绿线
		float tempDis = 45;
		canvas.drawLine(screenW / 2 - (float) 4.5 * tempDis, chartEndY + 50,
				screenW / 2 - (float) 3.5 * tempDis, chartEndY + 50,
				oxygenPathPaint);
		// 绿文字
		canvas.drawText("SpO2(%)", screenW / 2 - (float) 3.5 * tempDis + 20,
				chartEndY + 50 + 15, axisTextPaint);

		// 蓝线
		canvas.drawLine(screenW / 2 + 2 * tempDis, chartEndY + 50, screenW / 2 + 3
				* tempDis, chartEndY + 50, prPathPaint);
		// 蓝文字
		canvas.drawText("PR(bpm)", screenW / 2 + 3 * tempDis + 20,
				chartEndY + 50 + 5, axisTextPaint);

	}

	/**
	 * 初始化画笔
	 */
	public void initPaint() {
		axisLinePaint = new Paint();
		axisLinePaint.setAntiAlias(true);
		axisLinePaint.setStyle(Paint.Style.STROKE);
		axisLinePaint.setStrokeWidth((float) 2);
		axisLinePaint.setColor(Color.argb(140, 204, 204, 204));

		axisTextPaint = new Paint();
		axisTextPaint.setStrokeWidth(10);// 没作用
		axisTextPaint.setTextSize(20);
		axisTextPaint.setColor(Color.argb(255, 153, 153, 153));// 浅蓝色
		axisTextPaint.setFakeBoldText(true);
		axisTextPaint.setStrokeWidth(3);

		prPathPaint = new Paint();
		prPathPaint.setAntiAlias(true);
		prPathPaint.setStyle(Paint.Style.STROKE);
		prPathPaint.setStrokeWidth((float) 3.7);
		prPathPaint.setColor(Color.argb(255, 14, 182, 253));// 蓝线

		oxygenPathPaint = new Paint();
		oxygenPathPaint.setAntiAlias(true);
		oxygenPathPaint.setStyle(Paint.Style.STROKE);
		oxygenPathPaint.setStrokeWidth((float) 3.7);
		oxygenPathPaint.setColor(Color.argb(255, 0, 0xCC, 0x66));// 绿线s

		rectPaint = new Paint();
		rectPaint.setStrokeWidth(3);
		rectPaint.setColor(Color.rgb(255, 255, 255));

		spo2AxisTextPaint = new Paint();
		spo2AxisTextPaint.setTextSize(20);
		spo2AxisTextPaint.setColor(Color.argb(255, 0, 0xCC, 0x66));// 同spo2
		spo2AxisTextPaint.setFakeBoldText(true);
		spo2AxisTextPaint.setStrokeWidth(3);

		prAxisTextPaint = new Paint();
		prAxisTextPaint.setTextSize(20);
		prAxisTextPaint.setColor(Color.argb(255, 14, 182, 253));// 同pr
		prAxisTextPaint.setFakeBoldText(true);
		prAxisTextPaint.setStrokeWidth(3);
	}

	/**
	 * 初始化相关参数
	 *
	 * @param screenW
	 * @param screenH
	 */
	public void initFixParams(int screenW, int screenH) {
		chartEndX = screenW - 40;//波形距右边界距离
		lineDis = screenH / lineNum;
		chartEndY = ChartStartY + lineDis * (lineNum - 1);
		xDis = (chartEndX - ChartStartX) / (sampleNum);// 点间距=行长/每行点数
		ChartLineLength = xDis * (spo2Y.length);
		// 如果横线太短，则最少画满一屏
		if (ChartLineLength < chartEndX - ChartStartX)
			ChartLineLength = chartEndX - ChartStartX;
	}


	public int getLength() {
		return (int) ((ChartLineLength - (screenW - ChartStartX)) + 25);
	}

	public void setCurrentX(int CurrentX) {
		this.currentX = -CurrentX;
		if (-this.currentX > (ChartLineLength - (chartEndX - ChartStartX))){
			this.currentX = -(ChartLineLength - (chartEndX - ChartStartX));
			LogUtils.d("setCurrentX,currentX超出范围，改为："+currentX);
		}
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			preX = event.getX();
			touching = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			touching = false;
		}

		if (touching == true) {
			float TempX = event.getX();
			moveX = TempX - preX;
			preX = TempX;
			currentX += moveX;
			if (currentX > 0)
				currentX = 0;
			if (-currentX > (ChartLineLength - (chartEndX - ChartStartX)))// 右边界25Margin
				currentX = -(ChartLineLength - (chartEndX - ChartStartX));
			invalidate();
			this.seekBar.setProgress(-(int) currentX);
		}
		return true;
	}
}
