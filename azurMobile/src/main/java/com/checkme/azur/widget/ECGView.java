package com.checkme.azur.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.checkme.azur.utils.LogUtils;

public class ECGView extends View {

	// Starting position of drawing
	private final float chartStartX = 0, chartStartY = 0;
	// Sampling step
	private final int sampleStep = 5;
	// line number of wave
	private int lineNum;
	// the distance(px) between two lines
	private float lineDis;
	// the distance(px) between two point
	private float xDis;
	// the length(px) of per line
	private float chartLineLength;
	
	// About ECG Ruler
	private final float rulerStandardWidth = 20;
	private final float rulerZeroWidth = 13;
	private final float rulerTotalWidth = rulerStandardWidth + 2 * rulerZeroWidth;
	private final float standard1mV = (32767 / 4033) * 12 * 8;
	private final float[] standardNmV = {standard1mV*4,standard1mV*2,standard1mV};
	private float rulerStandard;
	// The distance between ruler and chart
	private final float disOfRulerChart = 10; 
	
	// About drawing
	private int screenW,screenH;
	private int[] chartY;
	private Paint linePaint, textPaint, recPaint,axisPaint;
	private float minY, maxY;
	
	// About selection rectangle
	private float recWidth, recHeight;
	private float recX, recY, preRecX, preRecY;
	private boolean canSelect = true;

	// Delegate interface
	private ECGViewDelegate delegate;
	
	public ECGView(Context context, int[] Y, int ScreenW, int ScreenH,
			int lineNum) {
		super(context);
		chartY = Y;
		if(chartY==null||chartY.length==0){
			LogUtils.d("Initialization failed");
			return;
		}
		this.screenW = ScreenW;
		this.screenH = ScreenH;
		this.lineNum = lineNum;
		InitFixParams();
		initPaint();
		getMinAndMax();
	}
	
	public void setDelegate(ECGViewDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if(chartY==null||chartY.length==0){
			LogUtils.d("no data to draw");
			return;
		}
		canvas.drawColor(Color.WHITE);
		drawAxis(canvas, chartY);
		drawPath(canvas, chartY);
		drawRec(canvas);
	}

	/**
	 * Draw axes and ruler
	 * 
	 * @param canvas
	 * @param Y
	 */
	public void drawAxis(Canvas canvas, int[] Y) {

		float zeroLineY = lineDis-25;
		float standardLineY = zeroLineY- (standard1mV/rulerStandard) * lineDis*0.8f;
		
		// Draw first zero line
//		canvas.drawLine(chartStartX, zeroLineY, chartStartX + rulerZeroWidth,
//				zeroLineY, linePaint);
		// Draw first vertical line
//		canvas.drawLine(chartStartX + rulerZeroWidth, zeroLineY, chartStartX
//				+ rulerZeroWidth, standardLineY, linePaint);
		// Draw 1mV line
//		canvas.drawLine(chartStartX + rulerZeroWidth, standardLineY, chartStartX
//				+ rulerZeroWidth + rulerStandardWidth, standardLineY, linePaint);
		// Draw second vertical line
//		canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
//				zeroLineY, chartStartX + rulerZeroWidth + rulerStandardWidth,
//				standardLineY, linePaint);
		// Draw second zero line
//		canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
//				zeroLineY,chartStartX + rulerZeroWidth * 2 + rulerStandardWidth,
//				zeroLineY, linePaint);
//		String rulerStr = "1mV";
//		canvas.drawText(rulerStr, chartStartX+5, zeroLineY+20, linePaint);
	}

	/**
	 * Draw ECG Path
	 * @param canvas
	 * @param Y
	 */
	public void drawPath(Canvas canvas, int[] Y) {
		int line = 0;// The line drawing currently
		float preTempX = 0, preTempY = 0;
		
		for (int i = 0, k=0; i < Y.length; i += sampleStep) {
			float tempX;
			// Staring position are different from first row and other line
			if (line == 0){
				tempX = chartStartX + rulerZeroWidth * 2 + rulerStandardWidth
						+ disOfRulerChart + k * xDis;// Draw wave to right.png of ruler
			}else{
				tempX = chartStartX + k * xDis;
			}
			float tempY = lineDis + line - (Y[i]-minY)/rulerStandard*lineDis*0.8f;
			if (i == 0) {// First point
				preTempX = 0;
				preTempY = 0;
				// Draw seperator line 
				canvas.drawLine(chartStartX, lineDis + line , 
						chartStartX + chartLineLength, lineDis + line
								, axisPaint);
			}
			if (preTempX != 0 && preTempY != 0){
				canvas.drawLine(preTempX, preTempY, tempX, tempY, linePaint);
			}
			preTempX = tempX;
			preTempY = tempY;
			k++;
			
			//If draw a line full, move to next line
			if (preTempX >= chartStartX + chartLineLength) {
				line += lineDis;
				//draw sperator line
				canvas.drawLine(chartStartX, lineDis + line, chartStartX 
						+ chartLineLength, lineDis + line, axisPaint);
				preTempX = 0;
				preTempY = 0;
				k = 0;
			}
		}
	}

	
	/**
	 * Draw a rectangle when select wave 
	 * @param canvas
	 */
	public void drawRec(Canvas canvas) {
//		canvas.drawRect(recX - recWidth / 2, recY - recHeight / 2, recX
//				+ recWidth / 2, recY + recHeight / 2, recPaint);
	}

	/**
	 * Initialize paints
	 */
	public void initPaint() {
		axisPaint = new Paint();
		axisPaint.setAntiAlias(true);
		axisPaint.setStyle(Paint.Style.FILL);
		axisPaint.setStrokeWidth((float) 1.5);
		axisPaint.setColor(Color.argb(255, 204, 204, 204));
		
		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setTextSize(15);
		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setStrokeWidth((float) 1.5);
		linePaint.setColor(Color.argb(255, 0, 00, 0));

		textPaint = new Paint();
		textPaint.setTextSize(25);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setStrokeWidth((float) 1.8);
		textPaint.setColor(Color.argb(255, 204, 204, 204));
		textPaint.setFakeBoldText(true);

		recPaint = new Paint();
		recPaint.setAntiAlias(true);
		recPaint.setStyle(Paint.Style.STROKE);
		recPaint.setStrokeWidth(4);
		recPaint.setColor(Color.argb(255, 48, 100, 0));
	}

	/**
	 * Get the maximum and minimum in ECG data
	 * And select a ruler in the meantime
	 */
	public void getMinAndMax() {
		maxY = -100;
		minY = 100;
		for (int i = 0; i < chartY.length; i++) {
			if ((maxY) < chartY[i])
				maxY = chartY[i];
			if ((minY) > chartY[i])
				minY = chartY[i];
		}
		maxY = Math.min(maxY, standardNmV[0]/2);
		minY = Math.max(minY, -standardNmV[0]/2);
		for(int i=0;i<standardNmV.length;i++){
			if((maxY-minY)<=standardNmV[i])
				rulerStandard = standardNmV[i];
		}
		// Expand the scope of the maximum and minimum
		maxY = (maxY+minY)/2+rulerStandard;
		minY = (maxY+minY)/2-rulerStandard;
	}

	/**
	 * Initialize parameters
	 *
	 */
	public void InitFixParams() {
		chartLineLength = screenW; 
		lineDis = screenH  / lineNum;
		//The total length of ECG wave
		float totalLength = chartLineLength * lineNum - rulerTotalWidth
				- disOfRulerChart;
		xDis = totalLength / (chartY.length / sampleStep);
		recHeight = lineDis-2;
		recWidth = chartLineLength / 2;
		recX = chartStartX + recWidth/2+2;
		recY = chartStartY + recHeight/2+2;
	}

	/**
	 * If the position in last range
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isTouchInPreRange(float x, float y) {
		if ((Math.abs(x - preRecX) < recWidth / 2) && (y == preRecY))
			return true;
		else
			return false;
	}

	/**
	 * Notify that ECG wave is selected
	 * @param recX
	 * @param lines
	 */
	public void notifyECGSelected(float recX, int lines) {
		// X position of selected point
		float xRange = recX - recWidth / 2; 
		// Selecting range of wave
		float range = ((xRange + (chartLineLength * lines - (chartStartX
				+ rulerTotalWidth + disOfRulerChart))) / ((chartLineLength * lineNum) - (chartStartX
				+ rulerTotalWidth + disOfRulerChart)));
		if(range<0)
			range = 0;
		// Forbidding select after selected
		setCanSelect(false);
		if (delegate!=null) {
			delegate.onRectSelected(range);
		}
	}

	public void setCanSelect(boolean canSelect) {
		this.canSelect = canSelect;
	}
	
	public boolean getCanSelect(){
		return canSelect;
	}

	/**
	 * redraw the rectangle to green color
	 */
	public void redrawRec() {
		recPaint.setColor(Color.argb(255, 48, 100, 0));
		invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			recX = event.getX();
			recY = event.getY();
			int Lines = 0;
			//Limit the size and position of the rectangle
			if (recY < lineDis - recHeight / 2) 
				recY = lineDis - recHeight / 2;
			for (int i = (lineNum-1); i >= 0; i--) {
				if (recY > lineDis * i) {
					recY = lineDis * (i + 1) - recHeight / 2;
					Lines = i;
					break;
				}
			}
			if (recX > chartLineLength - recWidth / 2) 
				recX = chartLineLength - recWidth / 2 - 2;
			if (recX < chartStartX + recWidth / 2)
				recX = chartStartX + recWidth / 2 + 2;
			if (isTouchInPreRange(recX, recY)) {
				LogUtils.d( "in Range");
				recPaint.setColor(Color.argb(255, 255, 255, 0));
				if (canSelect){
					notifyECGSelected(recX, Lines);
				}
			} else {
				recPaint.setColor(Color.argb(255, 48, 100, 0));
			}
			preRecX = recX;
			preRecY = recY;
			invalidate();
		}

		return true;
	}
	
	public static interface ECGViewDelegate {
		public void onRectSelected(float range);
	}

}
