package com.checkme.azur.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.checkme.newazur.R;
import com.checkme.azur.element.RealTimeDataPool.CommonDataPoolInterface;

public class ECGRealTimeWave extends CommonRealTimeWave {

	public ECGRealTimeWave(Context context, float width, float height,
			CommonDataPoolInterface interface1, float maxVal, float minVal) {
		super(context, width, height, interface1, maxVal, minVal);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawPath(Canvas canvas) {
		// TODO Auto-generated method stub
		if (waveBuf == null) {
			return;
		}
		
		Paint pathPaint = new Paint();
		pathPaint.setColor(getResources().getColor(R.color.MonitorGreen));
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeWidth(3);
		
		Path path = new Path();
		path.moveTo(0, height/2);
		
		for (int i = 0; i < waveBuf.size(); i++) {
			float curData = waveBuf.get(i).floatValue();
			path.lineTo(i*xDis, (height-((curData-minVal)/(maxVal-minVal))*height));
		}
		canvas.drawPath(path, pathPaint);
	}
}
