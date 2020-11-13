package com.viatom.azur.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.viatom.azur.monitor.utils.LogUtils;
import com.viatom.azur.monitor.utils.MsgUtils;

import java.util.LinkedList;
import java.util.List;

public class RTWaveView extends View {

	private static final int MSG_REDRAW = 1001;

	protected WaveViewParameters parameters;
	protected List<Float> drawList;
	protected int curIndex;// 当前刷新点
	// 手势

	public Handler handler = new Handler() {
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

	public RTWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RTWaveView(Context context, WaveViewParameters parameters) {
		super(context);
		// TODO Auto-generated constructor stub
		this.parameters = parameters;
		// initGestureDetector(context);
	}

	/**
	 * 初始化手势识别
	 *
	 * @param context
	 */
	public void initGestureDetector(Context context) {
		if (context == null) {
			return;
		}
	}

	/**
	 * Add data to draw list
	 *
	 * @param datas
	 *            Datas to added
	 */
	public void addDatasToDrawList(Float[] datas) {
		if (datas == null) {
			return;
		}

		if (drawList == null) {
			drawList = new LinkedList<Float>();
		}
		// 计算当前宽度能画下多少个点
		int drawListLength = (int) (parameters.getWidth() / parameters
				.getxDis());

		synchronized (drawList) {
			for (int i = 0; i < datas.length; i++) {
				// 不够长添加，够长则从前开始替换
				if (drawList.size() >= drawListLength) {
					drawList.set(curIndex, datas[i]);
					if (++curIndex >= drawListLength) {
						curIndex = 0;
					}
				} else {
					drawList.add(datas[i]);
				}
			}
		}
	}

	public void reDrawWave() {
		// LogUtils.d("重画wave*****");

		if (!drawList.contains(null)) {
			MsgUtils.sendMsg(handler, MSG_REDRAW);
		}else if(drawList.contains(null)){
			LogUtils.d("没有数据不需要重绘，等待断开蓝牙广播重新连接。。。。");
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawPath(canvas);
	}

	protected void drawPath(Canvas canvas) {
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
				float curData = drawList.get(i) == null ? (parameters
						.getMaxVal() + parameters.getMinVal()) / 2 : drawList
						.get(i);
				float x = i * parameters.getxDis();
				float y = parameters.getHeight()
						- ((curData - parameters.getMinVal()) / (parameters
						.getMaxVal() - parameters.getMinVal()))
						* parameters.getHeight();

				// Don't connect at curIndex point
				if (Math.abs(i - curIndex) < parameters.getSampleDis()
						|| i == 0) {
					path.moveTo(x, y);
				} else {
					path.lineTo(x, y);
				}

			}
		}
		canvas.drawPath(path, pathPaint);
	}

	public static class WaveViewParameters {
		private int color;
		private float width;
		private float height;
		private float maxVal;
		private float minVal;
		// Distance between two point
		private float xDis;
		// Decimation interval
		private int sampleDis;

		public WaveViewParameters(int color, float width, float height,
								  float maxVal, float minVal, float xDis, int sampleDis) {
			super();
			this.color = color;
			this.width = width;
			this.height = height;
			this.maxVal = maxVal;
			this.minVal = minVal;
			this.xDis = xDis;
			this.sampleDis = sampleDis;
		}

		public int getColor() {
			return color;
		}

		public float getWidth() {
			return width;
		}

		public float getHeight() {
			return height;
		}

		public float getMaxVal() {
			return maxVal;
		}

		public float getMinVal() {
			return minVal;
		}

		public float getxDis() {
			return xDis;
		}

		public int getSampleDis() {
			return sampleDis;
		}

	}

}
