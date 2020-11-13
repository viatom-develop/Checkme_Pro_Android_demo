package com.viatom.azur.monitor.element;

import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;

import com.viatom.azur.monitor.tools.PreferenceUtils;
import com.viatom.azur.activity.MainActivity;
import com.viatom.azur.monitor.utils.LogUtils;
import com.viatom.azur.monitor.widget.RTWaveView;
import com.viatom.newazur.R;

import java.util.LinkedList;
import java.util.List;

public abstract class MonitorComponent<T extends GeneralData> {

	private CompParameters parameters;
	protected RelativeLayout rootLayout;
	private RTWaveView waveView;
	private List<T> dataList;

	public MonitorComponent(RelativeLayout rootLayout, RTWaveView waveView,
							List<T> dataList, CompParameters parameters) {
		super();
		this.rootLayout = rootLayout;
		this.waveView = waveView;
		this.dataList = dataList;
		this.parameters = parameters;
		addWaveViewToComponent(waveView);
	}

	/**
	 * Add WaveView to Component, and start to draw
	 *
	 * @param waveView
	 */
	private void addWaveViewToComponent(RTWaveView waveView) {
		if (rootLayout == null || waveView == null) {
			return;
		}
		RelativeLayout waveLayout = (RelativeLayout) rootLayout
				.findViewById(R.id.rl_wave);
		waveLayout.addView(waveView);
	}

	/**
	 * Get data from data list
	 *
	 * @return
	 */
	private List<T> getRefreshDatasFromDataList(int sampleDis) {
		if (dataList == null) {
			return null;
		}
		List<T> datas = new LinkedList<T>();

		synchronized (dataList) {
			for (int i = 0; i < sampleDis; i++) {
				if (dataList.size() != 0) {
					datas.add(dataList.get(0));
					dataList.remove(0);
				} else {
					// datas.add(null);
				}
			}
		}
		return datas;
	}

	// 从元素列表取出画图数据，转换成一维纯数据数组
	private Float[] getDrawListFromDatas(List<T> datas) {

		// 如果in数据为空，返回全0数据
		if (datas == null || datas.size() == 0) {
			return new Float[5];// warning 5
		}

		Float[] drawList = new Float[parameters.getSampleDis()
				* datas.get(0).getDatas().length];
		for (int i = 0; i < datas.size(); i++) {
			Float[] tempDatas = datas.get(i).getDatas();
			System.arraycopy(tempDatas, 0, drawList, i * tempDatas.length,
					tempDatas.length);
		}

		return drawList;
	}

	/**
	 * 刷新View，外部定时器调用
	 */
	public void doRefreshViews(Handler mHandler, String str) {
		// 从数据池中取出要刷新的数据
		List<T> refreshDatas = getRefreshDatasFromDataList(parameters
				.getSampleDis());
		// 刷新wave
		if (waveView != null) {
			// 判断有无数据，进行处理。

			if(!PreferenceUtils.readBoolPreferences(MainActivity.instance, "BT_MODE")){
				if (str.equals("play")) {
					Message msg = new Message();
					msg.what = Constant.PLAY_FNISH;
					mHandler.sendMessage(msg);
					LogUtils.d(getDrawListFromDatas(refreshDatas)[0]+";"
							+getDrawListFromDatas(refreshDatas)[1]+";"
							+getDrawListFromDatas(refreshDatas)[2] +";"
							+getDrawListFromDatas(refreshDatas)[3]);
					//	return;
				}
			}else{
				if (com.viatom.azur.element.Constant.btConnectFlag == true && str.equals("play")) {
					Message msg = new Message();
					msg.what = Constant.PLAY_FNISH;
					mHandler.sendMessage(msg);
					LogUtils.d("刷新的数据123"+getDrawListFromDatas(refreshDatas)[0]+";"
							+getDrawListFromDatas(refreshDatas)[1]+";"
							+getDrawListFromDatas(refreshDatas)[2] +";"
							+getDrawListFromDatas(refreshDatas)[3]);
					//	return;
				}
			}



			/*if (getDrawListFromDatas(refreshDatas)[0] == null
					&& getDrawListFromDatas(refreshDatas)[1] == null
					&& getDrawListFromDatas(refreshDatas)[2] == null
					&& getDrawListFromDatas(refreshDatas)[3] == null
					&& str.equals("moni")) {
				Message msg = new Message();
				msg.what = SCREEN_SHOT;
				LogUtils.d("断开蓝牙调用");
				mHandler.sendMessage(msg);
				return;
			}*/

			// LogUtils.d("有调用的刷新");
			// 将要画图的数据添加到waveView中
			waveView.addDatasToDrawList(getDrawListFromDatas(refreshDatas));
			waveView.reDrawWave();
		}

		// 无论sampleDis为多少，数值以第一个为准
		if (refreshDatas != null && refreshDatas.size() != 0) {
			refreshVals(refreshDatas.get(0));
		} else {
			refreshVals(null);

		}
	}

	/**
	 * Refresh values
	 */
	protected abstract void refreshVals(T data);

	public static class CompParameters {
		private int sampleDis;

		public CompParameters(int sampleDis) {
			super();
			this.sampleDis = sampleDis;
		}

		public int getSampleDis() {
			return sampleDis;
		}

	}
}
