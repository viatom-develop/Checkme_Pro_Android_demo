package com.checkme.azur.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.ECGInnerItem;
import com.checkme.azur.utils.LogUtils;

public class ECGReportWave extends View{

	private static final float grid1mmLength = 5.9f; //1mm长度
	private static final float sampleDis = 5; //抽点距离
	private static final float standard1mV = (32767 / 4033) * 12 * 8;
	private static final float rulerStandardWidth = grid1mmLength*5;
	private static final float rulerZeroWidth = grid1mmLength*5;//5mm
	private static final float rulerTotalWidth = rulerStandardWidth + rulerZeroWidth*2;
	private static final float perLineVal = 2.5f;//每行2.5毫伏最大
	private static final float perLineHeight = perLineVal * grid1mmLength * 10;//每行2.5mV,10mm/mV
	private static final float zeroLineVal = 1.0f;//基线从1mV处画，上面1.5mV下面1mV

	//画图相关
	private float imgWidth, imgHeight;
	private float xDis;//未抽点画图点距离

	//数据
	private ECGInnerItem innerItem;


	public ECGReportWave(Context context, ECGInnerItem innerItem, float imgWidth, float imgHeight) {
		super(context);
		if (context==null || innerItem==null) {
			return;
		}
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.innerItem = innerItem;

		xDis = 1f / Constant.ECG_DATA_SAMPLING_FREQUENCY * 25 *grid1mmLength;//25mm/s,未抽点的
		xDis *= sampleDis;//抽点后的两点距离
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		LogUtils.d("绘制ECG波形");
		drawGrid(canvas);
		drawRuler(canvas);
		drawWave(canvas);
	}

	/**
	 * 画网格
	 * @param canvas
	 */
	protected void drawGrid(Canvas canvas) {
		//初始化画笔
		Paint gridPaint1mm = new Paint();
		gridPaint1mm.setAntiAlias(true);
		gridPaint1mm.setStyle(Paint.Style.FILL);
		gridPaint1mm.setStrokeWidth(2);
		gridPaint1mm.setColor(Color.argb(255, 255, 210, 240));

		Paint gridPaint5mm = new Paint();
		gridPaint5mm.setAntiAlias(true);
		gridPaint5mm.setStyle(Paint.Style.FILL);
		gridPaint5mm.setStrokeWidth(1.5f);
		gridPaint5mm.setColor(Color.argb(255, 255, 170, 220));

		int lineNum = (int)(imgWidth / grid1mmLength); //列数
		int rowNum = (int)(imgHeight / grid1mmLength); //行数
//		LogUtils.Log("列数"+lineNum+"行数"+rowNum);

		//1mm 竖线
		for(float i=0,j=0;j<=lineNum;i+=grid1mmLength,j++) {
			//最后一行，画上n像素，避免画在View外面
			if(j == lineNum)
				i -= gridPaint1mm.getStrokeWidth();
			canvas.drawLine(i, 0, i, imgHeight, gridPaint1mm);
		}

		//1mm 横线
		for(float i=0,j=0;j<=rowNum;i+=grid1mmLength,j++) {
			if(j == rowNum)
				i -= gridPaint1mm.getStrokeWidth();
			canvas.drawLine(0, i, imgWidth, i, gridPaint1mm);
		}

		//5mm竖线
		for(float i=0,j=0;j<=(lineNum/5);i+=grid1mmLength*5,j++) {
			canvas.drawLine(i, 0, i, imgHeight, gridPaint5mm);
			if(j == lineNum/5)
				i -= gridPaint5mm.getStrokeWidth();
		}

		// 5mm横线
		for(float i=0,j=0;j<=(rowNum/5);i+=grid1mmLength*5,j++) {
			if(j == rowNum/5)
				i -= gridPaint5mm.getStrokeWidth();
			canvas.drawLine(0, i, imgWidth, i, gridPaint5mm);
		}

	}

	/**
	 * 画标尺
	 * @param canvas
	 */
	protected void drawRuler(Canvas canvas) {
		float zeroLine = 0;
		float standardLine = 0;

		Paint linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setStrokeWidth((float) 2.5);
		linePaint.setColor(Color.argb(255, 0, 0, 0));

		//基线，从1mV处开始画，行高2.5mV,10mm/mV
		zeroLine = (perLineVal - zeroLineVal) * grid1mmLength * 10;
		standardLine = zeroLine - grid1mmLength*10;//1mV线
		canvas.drawLine(0, zeroLine, rulerZeroWidth,
				zeroLine, linePaint);
		canvas.drawLine( rulerZeroWidth, zeroLine, rulerZeroWidth
				, standardLine, linePaint);
		canvas.drawLine(rulerZeroWidth, standardLine
				, rulerZeroWidth + rulerStandardWidth, standardLine, linePaint);
		canvas.drawLine(rulerZeroWidth + rulerStandardWidth,
				zeroLine, rulerZeroWidth + rulerStandardWidth,
				standardLine, linePaint);
		canvas.drawLine(rulerZeroWidth + rulerStandardWidth,
				zeroLine, rulerZeroWidth * 2 + rulerStandardWidth,
				zeroLine, linePaint);
	}

	/**
	 * 画心电波形
	 * @param canvas
	 */
	protected void drawWave(Canvas canvas) {
		if (innerItem == null) {
			return;
		}
		//画笔
		Paint linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setStrokeWidth((float) 1.5);
		linePaint.setColor(Color.argb(255, 0, 0, 0));

		//数据
		int[] chartY = innerItem.getECGData();
		float preTempX = 0, preTempY = 0;

		int lineNum = 0;//行号
		for (int i = 0,l = 0; i < chartY.length; i+=sampleDis,l++) {
			//第一行从ruler后面开始
			float tempX = (lineNum==0 ? rulerTotalWidth : 0)+ l * xDis;
			//Y值，基线Y坐标-数据毫伏长度+行号*行高
			float tempY = (perLineHeight-zeroLineVal*grid1mmLength*10)
					- (chartY[i]/standard1mV)*grid1mmLength*10 + lineNum*perLineHeight;
			//换行
			if (tempX > imgWidth) {
				tempX = 0;
				tempY += perLineHeight;
				preTempX = tempX;
				preTempY = tempY;
				l = 0;//行内index
				lineNum ++;
				continue;
			}
			//画线，第一个点不画
			if (preTempX != 0 || preTempY != 0)
				canvas.drawLine(preTempX, preTempY, tempX, tempY, linePaint);
			preTempX = tempX;
			preTempY = tempY;
		}
	}
}
