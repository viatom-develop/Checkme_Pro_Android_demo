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
import com.checkme.azur.measurement.DailyCheckItem;
import com.checkme.azur.measurement.ECGInnerItem;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.measurement.User.UserInfo;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.widget.ECGReportWave;

public class DailyCheckReportUtils {

	private static final int A4_WIDTH = 2479/2;
	private static final int A4_HEIGHT = 3508/2;
	private static final String TAG_SCROLL_VIEW = "TAG_SCROLL_VIEW";

	/**
	 * dlc模式下生成报告
	 * @param context
	 * @param callerHandler
	 * @param rootView
	 * @param userInfo
	 * @param dlcItem
	 */
	public static void makeDLCReportPicture(Context context, Handler callerHandler
			, RelativeLayout rootView, UserInfo userInfo, DailyCheckItem dlcItem, int shareType) {

		if (context==null || dlcItem==null || rootView==null || userInfo==null) {
			LogUtils.d("contex或rootVie或dlcItem为空");
			return;
		}

		//从xml载入reportView
		View reportView = inflateReportView(context);
		setUserInfo(reportView, userInfo);
		setDailyCheckItem(reportView, dlcItem,context);
		addECGWave(context, (RelativeLayout)reportView.findViewById(R.id.rl_wave)
				, dlcItem.getInnerItem());
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
		View reportView = inflater.inflate(R.layout.widget_dlc_report, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(A4_WIDTH, A4_HEIGHT);
		reportView.setLayoutParams(params);

		return reportView;
	}

	/**
	 * 填写用户信息
	 * @param reportView
	 * @param userInfo
	 */
	protected static void setUserInfo(View reportView, UserInfo userInfo) {
		if(userInfo == null || reportView == null) {
			return;
		}
		//guest用户直接留白
		if (userInfo.getID() == 1) {
			return;
		}
		//名字
		TextView tvName = (TextView)reportView.findViewById(R.id.tv_name_val);
		tvName.setText(userInfo.getName());
		//性别
		TextView tvGender = (TextView)reportView.findViewById(R.id.tv_gender_val);
		tvGender.setText(userInfo.getGender()==1 ? Constant.getString(R.string.female)
				: Constant.getString(R.string.male));
		//生日
		TextView tvBirth = (TextView)reportView.findViewById(R.id.tv_birth_val);
		tvBirth.setText(StringMaker.makeDateString(userInfo.getBirthDate()));
	}

	/**
	 * 设置dlc项目
	 * @param reportView
	 * @param item
	 */
	protected static void setDailyCheckItem(View reportView, DailyCheckItem item, Context context) {
		if(item == null || reportView == null) {
			return;
		}
		ECGInnerItem innerItem = item.getInnerItem();//内部数据

		//测量模式
		TextView tvMeasureMode = (TextView)reportView.findViewById(R.id.tv_measure_val);
		tvMeasureMode.setText(Constant.getString(R.string.title_dlc));
		//测量时间
		TextView tvMeasureDate = (TextView)reportView.findViewById(R.id.tv_measure_date_val);
		tvMeasureDate.setText(StringMaker.makeDateString(item.getDate())
				+ "  " + StringMaker.makeTimeString(item.getDate()));
		//心率
		TextView tvHR = (TextView)reportView.findViewById(R.id.tv_hr_val);
		tvHR.setText(innerItem.getHR() == 0 ? "--" : String.valueOf(innerItem.getHR()) + "/min");
		//QRS
		TextView tvQRS = (TextView)reportView.findViewById(R.id.tv_qrs_val);
		tvQRS.setText(innerItem.getQRS() == 0 ? "--" : String.valueOf(innerItem.getQRS()) + "ms");
		//QT&&QTC
		TextView tvQT=(TextView) reportView.findViewById(R.id.tv_qt_val);
		TextView tvqt=(TextView) reportView.findViewById(R.id.tv_qt);
		TextView tvQTC=(TextView) reportView.findViewById(R.id.tv_qtc_val);
		TextView tvqtc=(TextView) reportView.findViewById(R.id.tv_qtc);
		String fileName = StringMaker.makeDateFileName(item.getDate(), Constant.CMD_TYPE_ECG_NUM);
		if(FileDriver.isFileExist(Constant.dir, fileName+MeasurementConstant.QT_FILE_NAME)){
			byte[] buf=FileDriver.read(Constant.dir, fileName+MeasurementConstant.QT_FILE_NAME);
			if (buf[0]==1) {
				tvQT.setVisibility(View.INVISIBLE);
				tvQTC.setVisibility(View.INVISIBLE);
				tvqt.setVisibility(View.INVISIBLE);
				tvqtc.setVisibility(View.INVISIBLE);
			} else {

				tvQT.setVisibility(View.VISIBLE);
				tvQTC.setVisibility(View.VISIBLE);
				tvqt.setVisibility(View.VISIBLE);
				tvqtc.setVisibility(View.VISIBLE);
				tvQT.setText(innerItem.getQT() == 0 ? "--" : String.valueOf(innerItem.getQT()) + "ms");
				tvQTC.setText(innerItem.getQTc() == 0 ? "--" : String.valueOf(innerItem.getQTc()) + "ms");
			}

		}else{
			tvQT.setVisibility(View.INVISIBLE);
			tvQTC.setVisibility(View.INVISIBLE);
			tvqt.setVisibility(View.INVISIBLE);
			tvqtc.setVisibility(View.INVISIBLE);
		}
		//ecg诊断结果
		TextView tvECGDiagnostic = (TextView)reportView.findViewById(R.id.tv_ecg_diagnostic_val);
		tvECGDiagnostic.setText(StringMaker.makeECGResult(item.getInnerItem()
				.getStrResultIndex(), 1, false));
		//SPO2
		TextView textSpo2 = (TextView)reportView.findViewById(R.id.tv_spo2_val);
		textSpo2.setText(item.getSPO2() == 0 ? "--" : " "+String.valueOf(item.getSPO2()) + "%");
		//PI
		TextView textPI = (TextView)reportView.findViewById(R.id.tv_pi_val);
		textPI.setText(item.getPI() == 0 ? "--" : String.valueOf(item.getPI()));
		//SPO2诊断结果
		TextView textSpo2Diagnostic = (TextView)reportView.findViewById(R.id.tv_spo2_diagnostic_val);
		textSpo2Diagnostic.setText(StringMaker.makeSPO2StrResult(item.getSPO2()));
		//BP
		TextView textBP = (TextView)reportView.findViewById(R.id.tv_bp_val);
		textBP.setText(StringMaker.makeBPValueStr(item) + "    " + StringMaker.makeBPCalDate(item));

		//带宽
		TextView tvFilter = (TextView)reportView.findViewById(R.id.tv_filter);
		tvFilter.setText(StringMaker.makeFilterStr(innerItem.getCheckMode(), innerItem.getFilterMode()));
	}

	/**
	 * 添加波形
	 * @param innerItem
	 */
	protected static void addECGWave(Context context, RelativeLayout layout, ECGInnerItem innerItem) {
		if (context==null || layout==null || innerItem==null) {
			return;
		}
		float imgWidth = 5.9f * 5 * 35; //25mm/s，每行7秒，每mm对应11.8f
		float imgHeight = 5.9f * 10 * 2.5f * 7; //每mm对应11.8f*10mm/mV*每行2.5mV*7行
		ECGReportWave wave = new ECGReportWave(context, innerItem, imgWidth, imgHeight);
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
				MsgUtils.sendMsg(handler, bitmap,
						Constant.MSG_REPORT_BITMAP_CONVERTED, shareType);
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
