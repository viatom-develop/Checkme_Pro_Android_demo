package com.viatom.azur.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viatom.newazur.R;
import com.viatom.azur.element.ChartItem;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.widget.ECGView;
import com.viatom.azur.widget.ChartView;

public class DailyCheckChartPagerAdapter extends PagerAdapter {
	public List<View> mListViews;
	public ChartView hrView, spo2View, bpView, piView;

	public DailyCheckChartPagerAdapter(List<View> mListViews) {
		this.mListViews = mListViews;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(mListViews.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		return mListViews.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(mListViews.get(arg1), 0);
		return mListViews.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

	public void addHRView(Context context, byte chartType, int screenW,
						  int screenH) {
		List<ChartItem> chartList = getChartList(chartType);
		hrView = new ChartView(context, chartList, chartType,
				screenW, screenH);

		//设置坐标范围，默认最高180，最大240
		hrView.initParams(180, 30, 6, 0);
		for(ChartItem item : chartList){
			if (item.value>180) {
				hrView.initParams(240, 30, 8, 0);
				break;
			}
		}
		((RelativeLayout) mListViews.get(0).findViewById(R.id.TabChartReChart))
				.addView(hrView);
	}

	public void AddOxygenView(Context context, byte chartType, int screenW,
							  int screenH) {
		// 创建并添加chartView
		spo2View = new ChartView(context, getChartList(chartType),
				chartType, screenW, screenH);
		spo2View.initParams(100, 60, 5, 0);
		((RelativeLayout) mListViews.get(1).findViewById(R.id.TabChartReChart))
				.addView(spo2View);
	}

	public void addPIView(Context context, byte chartType, int screenW,
						  int screenH) {
		List<ChartItem> chartList = getChartList(chartType);
		piView = new ChartView(context, chartList, chartType,
				screenW, screenH);

		//设置坐标范围，默认最高20，最低0
		piView.initParams(10, 0, 6, 0);
		for(ChartItem item : chartList){
			if (item.value>10) {
				piView.initParams(20, 0, 6, 0);
				break;
			}
		}
		((RelativeLayout) mListViews.get(2).findViewById(R.id.TabChartReChart))
				.addView(piView);
	}

	public void AddBPView(Context context, int ScreenW, int ScreenH) {
		bpView = (ChartView) makeBPView(context, ScreenW, ScreenH);
		if (bpView != null)
			((RelativeLayout) mListViews.get(3).findViewById(
					R.id.TabChartReChart)).addView(bpView);
	}

	public List<ChartItem> getChartList(byte chartType) {
		List<ChartItem> chartList = new ArrayList<ChartItem>();
		List<DailyCheckItem> dlcList = Constant.curUser.getDlcList();
		for (DailyCheckItem dlcItem : dlcList) {
			switch (chartType) {
				case Constant.DLC_INFO_HR:
					chartList.add(new ChartItem(dlcItem.getHR(), dlcItem.getDate()));
					break;
				case Constant.DLC_INFO_OXYGEN:
					chartList.add(new ChartItem(dlcItem.getSPO2(), dlcItem.getDate()));
					break;
				case Constant.DLC_INFO_PI:
					chartList.add(new ChartItem(dlcItem.getPI(), dlcItem.getDate()));
					break;
				case Constant.DLC_INFO_BP_RE:
					if (dlcItem.getBPFlag() == 0xFF)// 如果是无效值,则插入0xFF
						chartList.add(new ChartItem(0xFF, dlcItem.getDate()));
					else
						chartList.add(new ChartItem(dlcItem.getBP(), dlcItem.getDate()));
					break;
				case Constant.DLC_INFO_BP_ABS:
					if (dlcItem.getBPFlag() == 0xFF)// 如果是无效值,则插入0x00
						chartList.add(new ChartItem(0, dlcItem.getDate()));
					else
						chartList.add(new ChartItem(dlcItem.getBP(), dlcItem.getDate()));
					break;
				default:
					break;
			}
		}
		return chartList;
	}

	/**
	 * 生成BP趋势图
	 *
	 * @param context
	 * @param Position
	 * @param screenW
	 * @param screenH
	 * @return
	 */
	public View makeBPView(Context context, int screenW, int screenH) {
		byte calType = Constant.curUser.getBpCalItem().getCalType();

		if (calType == Constant.BP_TYPE_NONE) {
			//没有校准时也画chartView，画no data
			ChartView chartView = new ChartView(context,null,
					Constant.DLC_INFO_BP_NONE, screenW, screenH);
			return chartView;
		} else if (calType == Constant.BP_TYPE_RE) {
			ChartView chartView = new ChartView(context,
					getChartList(Constant.DLC_INFO_BP_RE),
					Constant.DLC_INFO_BP_RE, screenW, screenH);
			chartView.initParams(60, -60, 5, 0xFF);// 0xFF为错误值
			return chartView;
		} else if (calType == Constant.BP_TYPE_ABS) {
			ChartView chartView = new ChartView(context,
					getChartList(Constant.DLC_INFO_BP_ABS),
					Constant.DLC_INFO_BP_ABS, screenW, screenH);
			chartView.initParams(220, 80, 8, 0);
			return chartView;
		} else {
			return null;
		}
	}

	/**
	 * 日月年切换
	 *
	 * @param chartIndex
	 *            趋势图种类
	 * @param scale
	 *            日期中类
	 */
	public void switchChart(int chartIndex, byte scale) {
		if (hrView != null) {
			hrView.switchScale(scale);
		}
		if (spo2View != null) {
			spo2View.switchScale(scale);
		}
		if (bpView != null){
			bpView.switchScale(scale);
		}
		if (piView != null) {
			piView.switchScale(scale);
		}
	}

}
