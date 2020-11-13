package com.viatom.azur.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

public class SLMChart extends LineChart{

	private boolean isSpo2Val=false;

	public SLMChart(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SLMChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SLMChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawLinear(LineDataSet dataSet, ArrayList<Entry> entries) {
		// TODO Auto-generated method stub
//		super.drawLinear(dataSet, entries);
		mRenderPaint.setStyle(Paint.Style.STROKE);

		// more than 1 color
		if (dataSet.getColors() == null || dataSet.getColors().size() > 1) {

			float[] valuePoints = mTrans.generateTransformedValuesLineScatter(entries, mPhaseY);

			for (int j = 0; j < (valuePoints.length - 2) * mPhaseX; j += 2) {

				if (isOffContentRight(valuePoints[j]))
					break;

				// make sure the lines don't do shitty things outside
				// bounds
				if (j != 0 && isOffContentLeft(valuePoints[j - 1])
						&& isOffContentTop(valuePoints[j + 1])
						&& isOffContentBottom(valuePoints[j + 1]))
					continue;

				// get the color that is set for this line-segment
				mRenderPaint.setColor(dataSet.getColor(j / 2));

				mDrawCanvas.drawLine(valuePoints[j], valuePoints[j + 1],
						valuePoints[j + 2], valuePoints[j + 3], mRenderPaint);
			}

		} else { // only one color per dataset

			mRenderPaint.setColor(dataSet.getColor());

			Path line = generateLinePath(entries);
			mTrans.pathValueToPixel(line);

			mDrawCanvas.drawPath(line, mRenderPaint);
		}

		mRenderPaint.setPathEffect(null);

		// if drawing filled is enabled
		if (dataSet.isDrawFilledEnabled() && entries.size() > 0) {
			drawLinearFill(dataSet, entries);
		}
	}

	private Path generateLinePath(ArrayList<Entry> entries) {

		Path line = new Path();
		line.moveTo(entries.get(0).getXIndex(), entries.get(0).getVal() * mPhaseY);

		// create a new path
		for (int x = 1; x < entries.size() * mPhaseX; x++) {
			if (isSpo2Val) {
				Entry entry = entries.get(x);
				if (isDumpO2Val(entry.getVal())) {//当前为非法值，不画只移
					line.moveTo(entry.getXIndex(), entry.getVal() * mPhaseY);
				}else {
					if (x > 0) {
						Entry preEntry = entries.get(x-1);
						if (!isDumpO2Val(preEntry.getVal())) {//上一个值不是非法值才画
							line.lineTo(entry.getXIndex(), entry.getVal() * mPhaseY);
						}else {
							line.moveTo(entry.getXIndex(), entry.getVal() * mPhaseY);
						}
					}
				}
			}else {
				Entry entry = entries.get(x);
				if (isDumpPrVal(entry.getVal())) {//当前为非法值，不画只移
					line.moveTo(entry.getXIndex(), entry.getVal() * mPhaseY);
				}else {
					if (x > 0) {
						Entry preEntry = entries.get(x-1);
						if (!isDumpPrVal(preEntry.getVal())) {//上一个值不是非法值才画
							line.lineTo(entry.getXIndex(), entry.getVal() * mPhaseY);
						}else {
							line.moveTo(entry.getXIndex(), entry.getVal() * mPhaseY);
						}
					}
				}
			}
		}

		return line;
	}

	private boolean isDumpVal(float val) {
		if (val==0xFF) {
			return true;
		}else {
			return false;
		}
	}
	private boolean isDumpO2Val(float val) {
		if (val==0xFF||val<67) {
			return true;
		}else {
			return false;
		}
	}
	private boolean isDumpPrVal(float val) {
		if (val==0xFF||val<30) {
			return true;
		}else {
			return false;
		}
	}

	public boolean isSpo2Val(boolean i){
		isSpo2Val=i;
		return isSpo2Val;
	}
}
