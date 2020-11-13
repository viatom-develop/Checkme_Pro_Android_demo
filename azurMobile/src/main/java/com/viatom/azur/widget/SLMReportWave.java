package com.viatom.azur.widget;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.utils.LogUtils;

public class SLMReportWave extends View{

	private static final int lineNum = 8; //8条线7等分
	private static final int sampleStep = 50; //抽点距离
	private static final float chartStartX = 35;
	private static final float chartStartY = 50;
	private static final float minSPO2 = 65, maxSPO2 = 100, minPR = 30,
			MaxPR = 240 , yStepSPO2 = 5, yStepPR = 30;
	private static final int maxTime = 10;//最长10小时

	//画图相关
	private float imgWidth, imgHeight;
	private float xDis;//未抽点画图点距离
	private float chartEndY;
	private float chartHeight;
	private float lineDis;
	//数据
	private SLMItem slmItem;
	private List<Integer> spo2DrawList;
	private List<Integer> prDrawList;

	public SLMReportWave(Context context, SLMItem slmItem, float imgWidth, float imgHeight) {
		super(context);
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.slmItem = slmItem;
		initFixParams();
		spo2DrawList = slmItem.getInnerItem().getSimpleSpo2List(sampleStep);
		prDrawList = slmItem.getInnerItem().getSimplePrList(sampleStep);
	}

	/**
	 *  初始化参数
	 */
	protected void initFixParams() {
		chartHeight = imgHeight/2-chartStartY;
		lineDis = chartHeight/lineNum;
		chartEndY = chartStartY + lineDis * (lineNum - 1);
		int maxSampleNum = maxTime * 3600 / 2;//10小时
		xDis = (imgWidth - chartStartX) / maxSampleNum;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		LogUtils.d("绘制SLM波形");
		drawSPO2Axis(canvas);
		drawPRAxis(canvas);
		drawSPO2Wave(canvas);
		drawPRWave(canvas);
	}

	/**
	 * 画血氧坐标
	 * @param canvas
	 */
	protected void drawSPO2Axis(Canvas canvas) {
		if (canvas==null || slmItem==null) {
			return;
		}
		//坐标轴画笔
		Paint axisLinePaint = new Paint();
		axisLinePaint.setAntiAlias(true);
		axisLinePaint.setStyle(Paint.Style.STROKE);
		axisLinePaint.setStrokeWidth(1);
		axisLinePaint.setColor(Color.DKGRAY);
		//坐标文字画笔
		Paint axisTextPaint = new Paint();
		axisTextPaint.setTextSize(15);
		axisTextPaint.setColor(Color.BLACK);// 浅蓝色
//		axisTextPaint.setFakeBoldText(true);
		axisTextPaint.setStrokeWidth(1.5f);
		axisLinePaint.setTextAlign(Align.CENTER);
		//方块画笔
		Paint rectPaint = new Paint();
		rectPaint.setStrokeWidth(3);
		rectPaint.setColor(Color.WHITE);

		// 画横线
		for (int i = 0; i < lineNum; i++) {
			canvas.drawLine(chartStartX, chartStartY + i * lineDis, imgWidth
					, chartStartY + i * lineDis, axisLinePaint);
		}

		// x轴竖线，横坐标
		float labelXDis = (imgWidth - chartStartX)/maxTime;
		Date startDate = slmItem.getStartTime();
		String labelStr = StringMaker.makeTimeString(startDate);
		for (int i = 0; i < maxTime; i++) {
			// X轴竖线
			canvas.drawLine(chartStartX + i * labelXDis, chartEndY - 3, chartStartX
					+ i * labelXDis, chartEndY + 3, axisTextPaint);
			// X轴文字
			canvas.drawText(labelStr, chartStartX + i * labelXDis, chartEndY + 20,
					axisTextPaint);
			//每次赋予新时间(间隔1小时)
			startDate = new Date(startDate.getTime() + 3600000);
			labelStr = StringMaker.makeTimeString(startDate);
		}

		//画Y坐标
		canvas.drawRect(0, 0, chartStartX - 10, chartEndY + 100, rectPaint);// 画一个白色矩形遮盖趋势图
		// 画纵轴点
		for (int i = 0; i < lineNum; i++) {
			canvas.drawText(String.valueOf((int)(maxSPO2 - i*yStepSPO2)) + "%",
					0, chartStartY +i * lineDis + 3,axisTextPaint);
		}

		//画标题
		canvas.drawText("SpO2(%)", 0, chartStartY - 25, axisTextPaint);
	}

	/**
	 * 画pr坐标
	 * @param canvas
	 */
	protected void drawPRAxis(Canvas canvas) {
		if (canvas==null || slmItem==null) {
			return;
		}
		//坐标轴画笔
		Paint axisLinePaint = new Paint();
		axisLinePaint.setAntiAlias(true);
		axisLinePaint.setStyle(Paint.Style.STROKE);
		axisLinePaint.setStrokeWidth(1);
		axisLinePaint.setColor(Color.DKGRAY);
		//坐标文字画笔
		Paint axisTextPaint = new Paint();
		axisTextPaint.setTextSize(15);
		axisTextPaint.setColor(Color.BLACK);// 浅蓝色
//		axisTextPaint.setFakeBoldText(true);
		axisTextPaint.setStrokeWidth(1.5f);
		axisLinePaint.setTextAlign(Align.CENTER);
		//方块画笔
		Paint rectPaint = new Paint();
		rectPaint.setStrokeWidth(3);
		rectPaint.setColor(Color.rgb(255, 255, 255));
		//y起始位置
		float yOffset = imgHeight/2;

		// 画横线
		for (int i = 0; i < lineNum; i++) {
			canvas.drawLine(chartStartX, chartStartY + i * lineDis + yOffset, imgWidth
					, chartStartY + i * lineDis + yOffset, axisLinePaint);
		}

		// x轴竖线，横坐标
		int labelNum = 10;//10小时
		float labelXDis = (imgWidth - chartStartX)/labelNum;
		Date startDate = slmItem.getStartTime();
		String labelStr = StringMaker.makeTimeString(startDate);
		for (int i = 0; i < labelNum; i++) {
			// X轴竖线
			canvas.drawLine(chartStartX + i * labelXDis, chartEndY - 3 + yOffset, chartStartX
					+ i * labelXDis, chartEndY + 3 + yOffset, axisTextPaint);
			// X轴文字
			canvas.drawText(labelStr, chartStartX + i * labelXDis, chartEndY + 20 + yOffset,
					axisTextPaint);
			//每次赋予新时间(间隔1小时)
			startDate = new Date(startDate.getTime() + 3600000);
			labelStr = StringMaker.makeTimeString(startDate);
		}

		//画Y坐标
		canvas.drawRect(0, 0 + yOffset, chartStartX - 10, chartEndY + 100 + yOffset, rectPaint);// 画一个白色矩形遮盖趋势图
		// 画纵轴点
		for (int i = 0; i < lineNum; i++) {
			canvas.drawText(String.valueOf((int)(MaxPR - i*yStepPR)),
					0, chartStartY +i * lineDis + 3 + yOffset,axisTextPaint);
		}

		//画标题
		canvas.drawText("PR(/min)", 0, yOffset + chartStartY - 25, axisTextPaint);
	}

	/**
	 * 画血氧波形
	 * @param canvas
	 */
	protected void drawSPO2Wave(Canvas canvas) {
		List<Integer> datas = spo2DrawList;
		float preTempX = 0, preTempY = 0;
		boolean dumpLine = false;//脱落，虚线

		//血氧画笔
		Paint oxygenPathPaint = new Paint();
		oxygenPathPaint.setAntiAlias(true);
		oxygenPathPaint.setStyle(Paint.Style.FILL);
		oxygenPathPaint.setStrokeWidth((float) 2);
		oxygenPathPaint.setColor(Color.BLACK);// 绿线

		for (int i = 0; i < datas.size(); i++) {
			float tempX = chartStartX + i * sampleStep * xDis;
			if (datas.get(i) != 0xFF) {// 0xFF为错误值，忽略不画
				float tempY = chartEndY - ((datas.get(i) - minSPO2) / (maxSPO2 - minSPO2))
						* (chartEndY - chartStartY);
				if (preTempX != 0 && preTempY != 0) {
					if(!dumpLine){
						canvas.drawLine(preTempX, preTempY, tempX, tempY, oxygenPathPaint);
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

	/**
	 * 画pr波形
	 * @param canvas
	 */
	protected void drawPRWave(Canvas canvas) {
		List<Integer> datas = prDrawList;
		float preTempX = 0, preTempY = 0;
		boolean dumpLine = false;//脱落，虚线

		//血氧画笔
		Paint prPathPaint = new Paint();
		prPathPaint.setAntiAlias(true);
		prPathPaint.setStyle(Paint.Style.FILL);
		prPathPaint.setStrokeWidth((float) 2);
		prPathPaint.setColor(Color.BLACK);// 蓝线
		//y起始位置
		float yOffset = imgHeight/2;

		for (int i = 0; i < datas.size(); i++) {
			float tempX = chartStartX + i * sampleStep * xDis;
			if (datas.get(i) != 0xFF) {// 0xFF为错误值，忽略不画
				float tempY = chartEndY - ((datas.get(i) - minPR) / (MaxPR - minPR))
						* (chartEndY - chartStartY) + yOffset;
				if (preTempX != 0 && preTempY != 0) {
					if(!dumpLine){
						canvas.drawLine(preTempX, preTempY, tempX, tempY, prPathPaint);
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
}

