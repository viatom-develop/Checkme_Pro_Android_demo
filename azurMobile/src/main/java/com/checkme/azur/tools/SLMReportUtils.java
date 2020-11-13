package com.checkme.azur.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.SLMItem;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.widget.SLMReportWave;

public class SLMReportUtils {

	private static final int A4_WIDTH = 2479/2;
	private static final int A4_HEIGHT = 3508/2;
	private static final String TAG_SCROLL_VIEW = "TAG_SCROLL_VIEW";

	/**
	 * ecg模式下生成报告
	 * @param context
	 * @param callerHandler
	 * @param rootView
	 * @param ecgItem
	 */
	public static void makeSLMReportPicture(Context context, Handler callerHandler
			, RelativeLayout rootView, SLMItem slmItem, int shareType) {

		if (context==null || slmItem==null || rootView==null) {
			LogUtils.d("contex或rootVie或ecgItem为空");
			return;
		}

		//从xml载入reportView
		View reportView = inflateReportView(context);
		setSLMItem(reportView, slmItem);
		addSLMWave(context, (RelativeLayout)reportView.findViewById(R.id.rl_wave), slmItem);
		View scrollView = makeScrollView(context, reportView);
		rootView.addView(scrollView);
		//启动转换线程,完成后发通知
		new convertThread(callerHandler, reportView, shareType).start();
	}

	/**
	 * 从xml中加载reportView
	 * @return
	 */
	protected static View inflateReportView(Context context) {
		if (context == null) {
			return null;
		}

		LayoutInflater inflater = LayoutInflater.from(context);
		View reportView = inflater.inflate(R.layout.widget_slm_report, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(A4_WIDTH, A4_HEIGHT);
		reportView.setLayoutParams(params);

		return reportView;
	}

	/**
	 * 填写ecg项目结果
	 * @param reportView
	 * @param item
	 */
	protected static void setSLMItem(View reportView, SLMItem item) {
		if(item == null || reportView == null) {
			return;
		}
		//测量模式
		TextView tvMeasureMode = (TextView)reportView.findViewById(R.id.tv_measure_val);
		tvMeasureMode.setText(Constant.getString(R.string.title_slm));
		//测量时间
		TextView tvMeasureDate = (TextView)reportView.findViewById(R.id.tv_measure_date_val);
		tvMeasureDate.setText(StringMaker.makeDateString(item.getStartTime())
				+ "  " + StringMaker.makeTimeString(item.getStartTime()));
		//总时间
		TextView tvTotalTime = (TextView)reportView.findViewById(R.id.tv_total_time_val);
		tvTotalTime.setText(StringMaker.makeSecondToMinute(item.getTotalTime()));
		//跌落时间
		TextView tvDrops = (TextView)reportView.findViewById(R.id.tv_drops_val);
		tvDrops.setText(item.getLowOxygenNum() + Constant.getString(R.string.drops) + ", "
				+ StringMaker.makeSecondToMinute(item.getLowOxygenTime()));
		//平均值
		TextView tvAverage = (TextView)reportView.findViewById(R.id.tv_average_val);
		tvAverage.setText(String.valueOf((item.getAverageOxygen())==0?"--":item.getAverageOxygen())+"%");
		//最低值
		TextView tvLowest = (TextView)reportView.findViewById(R.id.tv_lowest_val);
		tvLowest.setText(String.valueOf((item.getLowestOxygen())==0?"--":item.getLowestOxygen())+"%");
		//诊断结果
		TextView tvDiagnostic = (TextView)reportView.findViewById(R.id.tv_diagnostic);
		tvDiagnostic.setText(StringMaker.makeSLMResultStr(item.getImgResult(),
				item.getLowOxygenNum()));
	}

	/**
	 * 添加波形
	 * @param innerItem
	 */
	protected static void addSLMWave(Context context, RelativeLayout layout, SLMItem slmItem) {
		if (context==null || layout==null || slmItem==null) {
			return;
		}
		float imgWidth = 5.9f * 5 * 35; //25mm/s，每行7秒，每mm对应11.8f
		float imgHeight = 5.9f * 10 * 2.5f * 8; //每mm对应11.8f*10mm/mV*每行2.5mV*8行
		SLMReportWave wave = new SLMReportWave(context, slmItem, imgWidth, imgHeight);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
		wave.setLayoutParams(layoutParams);
		layout.addView(wave);
	}

	/**
	 * 生成滚动视图，返回给工具调用者
	 * @param reportView
	 * @return
	 */
	protected static View makeScrollView(Context context, View reportView) {
		if(reportView == null || context == null) {
			return null;
		}
		ScrollView scrollView = new ScrollView(context);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		scrollView.setLayoutParams(layoutParams);
		scrollView.addView(reportView);
		scrollView.setVisibility(View.INVISIBLE);
		scrollView.setTag(TAG_SCROLL_VIEW);//加入标签，方便删除

		return scrollView;
	}

	/**
	 * 转换View到BMP
	 * @param view
	 * @return
	 */
	protected static Bitmap convertViewToBitmap(View view) {
		if (view == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(A4_WIDTH
				, A4_HEIGHT, Bitmap.Config.RGB_565);
		view.draw(new Canvas(bitmap));

		return bitmap;
	}

	/**
	 * @author zouhao
	 * 将view转换成bmp，中间阻塞delay，等待view添加进layout
	 */
	protected static class convertThread extends Thread {
		Handler handler;
		View view;
		int shareType;

		public convertThread(Handler handler, View view, int shareType) {
			super();
			this.handler = handler;
			this.view = view;
			this.shareType = shareType;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				sleep(500);
				Bitmap bitmap = convertViewToBitmap(view);
				MsgUtils.sendMsg(handler, bitmap, Constant.MSG_REPORT_BITMAP_CONVERTED, shareType);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	/**
	 * 生成完图片后删除加入到layout的View
	 * @param rootView
	 */
	public static void removeScrollView(RelativeLayout rootView) {
		if (rootView == null) {
			return;
		}

		View scrollView = rootView.findViewWithTag(TAG_SCROLL_VIEW);
		if (scrollView != null) {
			rootView.removeView(scrollView);
		}
	}
}
