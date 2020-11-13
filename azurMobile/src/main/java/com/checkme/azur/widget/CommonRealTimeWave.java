package com.checkme.azur.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.View;

import com.checkme.azur.element.RealTimeDataPool.CommonDataPoolInterface;
import com.checkme.azur.utils.MsgUtils;

/**
 * @author zouhao
 * 通用实时波形类
 */
public abstract class CommonRealTimeWave extends View{

	private static final int MSG_DRAW_POINT = 1001;
	protected static final float xDis = 2.5f;//两点X距离

	private CommonDataPoolInterface interface1;
	protected List<Integer> waveBuf;
	protected float width, height;
	protected float maxVal, minVal;
	private float curIndex;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
				case MSG_DRAW_POINT:
					invalidate();
					break;

				default:
					break;
			}
		};
	};

	public CommonRealTimeWave(Context context, float width, float height,
							  CommonDataPoolInterface interface1, float maxVal, float minVal) {
		super(context);
		this.width = width;
		this.height = height;
		this.interface1 = interface1;
		this.maxVal = maxVal;
		this.minVal = minVal;
	}

	/**
	 * 开始画波形
	 * @param refreshRate 刷新时间ms
	 */
	public void startDrawWave(int refreshRate) {

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MsgUtils.sendMsg(handler, MSG_DRAW_POINT);
//				LogUtils.Log("执行重画");
			}
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, 0, refreshRate);
	}

	/**
	 * 从datapool中获取一段数据，并加入波形缓冲队列
	 * @return
	 */
	public void getDatasFromDataPool(int getNum) {
		List<Integer> dataPoolList = interface1.getDataList();

		if (dataPoolList == null) {
			return;
		}
		for (int i = 0; i < getNum; i++) {
			if (dataPoolList!=null && dataPoolList.size()!=0) {
				Integer dataInteger = dataPoolList.get(0);
				addDataToWaveBuf(dataInteger);
				if(dataPoolList.size()!=0)
					dataPoolList.remove(0);
			}else {
				addDataToWaveBuf(0);
//				LogUtils.Log("dataPool被取空");
			}
		}
	}

	/**
	 * 将单个数据写入波形缓冲队列
	 * @param dataInteger
	 */
	protected void addDataToWaveBuf(Integer dataInteger) {
		if (dataInteger == null) {
			return;
		}

		if (waveBuf == null) {
			waveBuf = new ArrayList<Integer>();
		}

		//计算当前宽度能画下多少个点
		int sampleNum = (int)(width/xDis);
		//不够长添加，够长则从前开始替换
		if (waveBuf.size()>=sampleNum) {
			waveBuf.set((int)curIndex, dataInteger);
			if (++curIndex >= sampleNum) {
				curIndex = 0;
			}
		}else {
			waveBuf.add(dataInteger);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawPath(canvas);
	}

	protected abstract void drawPath(Canvas canvas);
}
