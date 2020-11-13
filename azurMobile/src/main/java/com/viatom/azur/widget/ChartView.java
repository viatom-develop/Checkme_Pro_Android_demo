package com.viatom.azur.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import u.aly.cu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.viatom.newazur.R;
import com.viatom.azur.element.ChartItem;
import com.viatom.azur.element.Constant;
import com.viatom.azur.tools.LocaleUtils;
import com.viatom.azur.utils.LogUtils;

public class ChartView extends View implements OnGestureListener{

	// 数据过滤
	public static final byte FILTER_TYPE_DAY = 0;
	public static final byte FILTER_TYPE_WEEK = 1;
	public static final byte FILTER_TYPE_MONTH = 2;
	public static final byte FILTER_TYPE_YEAR = 3;

	private static final byte DAY_SAMPLE_NUM = 25;
	private static final byte WEEK_SAMPLE_NUM = 7;
	private static final byte MONTH_SAMPLE_NUM = 29;
	private static final byte YEAR_SAMPLE_NUM = 13;
	private static final long MILLISECONDS_A_DAY = 3600 * 24 * 1000;
	private static final long MILLISECONDS_AN_HOUR = 3600 * 1000;

	// 画图固定参数
	private static final float chartStartX = 50, chartStartY = 70;

	private List<ChartItem> rowList;
	private List<SampleItem> drawList;
	private int errVal;
	private byte chartType;
	private byte curFilterType = FILTER_TYPE_WEEK;
	private Date curDate = new Date();
	private Context mContext;
	//手势
	private GestureDetector detector;

	// 画图相关参数
	private boolean isValsInited = false;
	private float maxVal, minVal, xDis, lineDis, chartEndY, chartEndX;
	private int lineNum, screenW, screenH;
	private Paint axisTextPaint, pathLinePaint, axisLinePaint, rectPaint,
			bigPointPaint,smallPointPaint,noDataTextPaint,titlePaint;
	public class SampleItem {
		public float minSample;
		public float maxSample;
		public Date date;
		public boolean isMultiSamples = false;
		private boolean isNullSample = true;

		public void addSample(float sample) {
			if (isNullSample) {// 第一次添加
				minSample = maxSample = sample;
				isNullSample = false;
			} else {// 后面添加
				minSample = Math.min(minSample, sample);
				maxSample = Math.max(maxSample, sample);
				isMultiSamples = true;
			}
		}
	}

	public ChartView(Context context, List<ChartItem> list, byte chartType,
					 int screenW, int screenH) {
		// TODO Auto-generated constructor stub
		super(context);
		this.mContext = context;
		this.screenW = screenW;
		this.screenH = screenH;
		this.chartType = chartType;
		this.rowList = list;

		initGestureDetector(context);
		initPaint();
	}

	/**
	 * 初始化手势识别
	 * @param context
	 */
	private void initGestureDetector(Context context) {
		if (context == null) {
			return;
		}
		detector = new GestureDetector(context, this);
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				detector.onTouchEvent(arg1);
				return false;
			}
		});
	}

	/**
	 * 初始化画图参数，必须在构造函数调用后调用
	 * @param max
	 * @param min
	 * @param num
	 */
	public void initParams(float max, float min, int num, int err) {
		if (rowList == null || rowList.size() == 0) {
			return;
		}
		if (max < min || num <= 0) {
			LogUtils.d("chart初始化参数错误");
			return;
		}
		maxVal = max;
		minVal = min;
		lineNum = num;
		errVal = err;
		isValsInited = true;

		rowList = filterErrNum(rowList);
		drawList = filterWeekList(rowList);

		// 画图相关
		chartEndY = screenH - 40;
		lineDis = (chartEndY - chartStartY) / (lineNum - 1);
		chartEndX = screenW - 50;
		xDis = (chartEndX - chartStartX) / (getCurSampleNum());// 点间距=行长/每行点数
	}

	/**
	 * 初始化画笔
	 */
	private void initPaint() {
		axisLinePaint = new Paint();
		axisLinePaint.setAntiAlias(true);
		axisLinePaint.setStyle(Paint.Style.STROKE);
		axisLinePaint.setStrokeWidth((float) 2);
		axisLinePaint.setColor(Color.WHITE);
		axisLinePaint.setAlpha(100);

		axisTextPaint = new Paint();
		axisTextPaint.setTextSize(20);
		axisTextPaint.setAlpha(100);
		axisTextPaint.setColor(Color.WHITE);

		titlePaint = new Paint();
		titlePaint.setTextSize(25);
		titlePaint.setColor(Color.WHITE);

		pathLinePaint = new Paint();
		pathLinePaint.setAntiAlias(true);
		pathLinePaint.setStyle(Paint.Style.STROKE);
		pathLinePaint.setStrokeWidth((float) 10);
		pathLinePaint.setColor(Color.WHITE);// 黑线
		pathLinePaint.setAlpha(130);

		bigPointPaint = new Paint();
		bigPointPaint.setColor(Color.WHITE);

		smallPointPaint = new Paint();
		smallPointPaint.setColor(getResources().getColor(R.color.default_bkg));

		rectPaint = new Paint();
		rectPaint.setStrokeWidth(3);
		rectPaint.setColor(Color.rgb(255, 255, 255));

		noDataTextPaint = new Paint();
		noDataTextPaint.setStrokeWidth(10);// 没作用
		noDataTextPaint.setTextSize(50);
		noDataTextPaint.setAlpha(140);
		noDataTextPaint.setColor(Color.WHITE);
		noDataTextPaint.setTextAlign(Align.CENTER);
	}

	/**
	 * 过滤列表中错误数值
	 *
	 * @param inList
	 * @return
	 */
	private List<ChartItem> filterErrNum(List<ChartItem> inList) {
		List<ChartItem> outList = new ArrayList<ChartItem>();

		for (ChartItem item : inList) {
			if (item.value != errVal) {
				outList.add(item);
			}
		}

		return outList;
	}

	/**
	 * 将原始list取最新24小时，生成day列表 列表项目变成SampleItem，有大小值等信息
	 *
	 * @param inList
	 *            必须是去掉错误值的list
	 * @return
	 */
	private List<SampleItem> filterDayList(List<ChartItem> inList) {
		if (inList == null || inList.size() == 0)
			return null;
		List<SampleItem> dayList = new ArrayList<ChartView.SampleItem>();
		Date lastDate = makeLastDate(curDate); //从当前时间开始
		for (int i = DAY_SAMPLE_NUM - 1; i >= 0; i--) {
			// 新建7个sampleItem，分辨是最后日期前7天
			SampleItem sampleItem = new SampleItem();
			sampleItem.date = new Date(lastDate.getTime() - i
					* MILLISECONDS_AN_HOUR);
			for (ChartItem charItem : inList) {// 遍历inList中item，同一天的全加入当前sampleItem
				// 如果chartItem的时间与当前sampleItem的date是同一天
				if (isSameHour(charItem.date, sampleItem.date)) {
					sampleItem.addSample(charItem.value);
				}
			}
			dayList.add(sampleItem);
		}
		return dayList;
	}

	/**
	 * 将原始list取最新7天，生成周列表 列表项目变成SampleItem，有大小值等信息
	 *
	 * @param inList 必须是去掉错误值的list
	 * @return
	 */
	private List<SampleItem> filterWeekList(List<ChartItem> inList) {
		if (inList == null || inList.size() == 0)
			return null;
		List<SampleItem> weekList = new ArrayList<ChartView.SampleItem>();
		Date lastDate = curDate;
		for (int i = WEEK_SAMPLE_NUM - 1; i >= 0; i--) {
			// 新建7个sampleItem，分辨是最后日期前7天
			SampleItem sampleItem = new SampleItem();
			sampleItem.date = new Date(lastDate.getTime() - i
					* MILLISECONDS_A_DAY);
			for (ChartItem charItem : inList) {// 遍历inList中item，同一天的全加入当前sampleItem
				// 如果chartItem的时间与当前sampleItem的date是同一天
				if (isSameDay(charItem.date, sampleItem.date)) {
					sampleItem.addSample(charItem.value);
				}
			}
			weekList.add(sampleItem);
		}
		return weekList;
	}

	/**
	 * 将原始list取最新29天，生成月列表 列表项目变成SampleItem，有大小值等信息
	 *
	 * @param inList
	 *            必须是去掉错误值的list
	 * @return
	 */
	private List<SampleItem> filterMonthList(List<ChartItem> inList) {
		List<SampleItem> monthList = new ArrayList<ChartView.SampleItem>();
//		Date lastDate = inList.get(inList.size() - 1).date;
		Date lastDate = curDate;
		for (int i = MONTH_SAMPLE_NUM - 1; i >= 0; i--) {
			// 新建7个sampleItem，分辨是最后日期前7天
			SampleItem sampleItem = new SampleItem();
			sampleItem.date = new Date(lastDate.getTime() - i
					* MILLISECONDS_A_DAY);
			for (ChartItem charItem : inList) {// 遍历inList中item，同一天的全加入当前sampleItem
				// 如果chartItem的时间与当前sampleItem的date是同一天
				if (isSameDay(charItem.date, sampleItem.date)) {
					sampleItem.addSample(charItem.value);
				}
			}
			monthList.add(sampleItem);
		}
		return monthList;
	}

	/**
	 * 以月为单位 将原始list取最新12个月，生成年列表 列表项目变成SampleItem，有大小值等信息
	 *
	 * @param inList
	 *            必须是去掉错误值的list
	 * @return
	 */
	private List<SampleItem> filterYearList(List<ChartItem> inList) {
		List<SampleItem> yearList = new ArrayList<ChartView.SampleItem>();
//		Date lastDate = inList.get(inList.size() - 1).date;
		Date lastDate = curDate;
		for (int i = YEAR_SAMPLE_NUM - 1; i >= 0; i--) {
			// 新建7个sampleItem，分辨是最后日期前7天
			SampleItem sampleItem = new SampleItem();
			sampleItem.date = new Date(lastDate.getTime() - i
					* MILLISECONDS_A_DAY * 31);// 31天算一个月
			for (ChartItem charItem : inList) {// 遍历inList中item，同一天的全加入当前sampleItem
				// 如果chartItem的时间与当前sampleItem的date是同一天
				if (isSameMonth(charItem.date, sampleItem.date)) {
					sampleItem.addSample(charItem.value);
				}
			}
			yearList.add(sampleItem);
		}
		return yearList;
	}

	/**
	 * 生成一天最后一小时Date，用于产生天列表
	 *
	 * @param date
	 */
	private Date makeLastDate(Date date) {
		Date lastDate = new Date(date.getTime());
		lastDate.setHours(24);
		lastDate.setMinutes(0);
		lastDate.setSeconds(0);
		return lastDate;
	}

	/**
	 * 判断两个date是否是同一小时
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean isSameHour(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		if (date1.getDate() == date2.getDate()
				&& date1.getMonth() == date2.getMonth()
				&& date1.getYear() == date2.getYear()
				&& date1.getHours() == date2.getHours()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断两个date是否是同一天
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		if (date1.getDate() == date2.getDate()
				&& date1.getMonth() == date2.getMonth()
				&& date1.getYear() == date2.getYear()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断两个date是否是同一个月
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean isSameMonth(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		if (date1.getMonth() == date2.getMonth()
				&& date1.getYear() == date2.getYear()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断两个date是否是同一年
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean isSameYear(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		if (date1.getYear() == date2.getYear()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据当前filter类型，返回一屏要画的点数
	 *
	 * @return
	 */
	private int getCurSampleNum() {
		switch (curFilterType) {
			case FILTER_TYPE_DAY:
				return DAY_SAMPLE_NUM;
			case FILTER_TYPE_WEEK:
				return WEEK_SAMPLE_NUM;
			case FILTER_TYPE_MONTH:
				return MONTH_SAMPLE_NUM;
			case FILTER_TYPE_YEAR:
				return YEAR_SAMPLE_NUM;
			default:
				return 0;
		}
	}

	/**
	 * 生成x坐标轴文字
	 *
	 * @param preDate
	 * @param curDate
	 * @return
	 */
	private String makeAxisXStr(Date preDate, Date curDate) {
		String str = "";
		if (curFilterType == FILTER_TYPE_DAY) {
			if (preDate == null) {
				if (LocaleUtils.isWestLanguage()) {
					str += "(" + Constant.getString(Constant.MONTH[curDate.getMonth()]) + " "
							+ curDate.getDate() + ") " + curDate.getHours();
				}else {
					str += "(" + (curDate.getMonth() + 1) + "-"
							+ curDate.getDate() + ") " + curDate.getHours();
				}
			} else {
				str += (curDate.getHours() == 0 ? 24 : curDate.getHours());//0点写24点
			}
		}
		// 周和月的逻辑相同
		else if (curFilterType == FILTER_TYPE_WEEK
				|| curFilterType == FILTER_TYPE_MONTH) {
			if (preDate == null) {// 第一个 写出月份
				if (LocaleUtils.isWestLanguage()) {
					str += Constant.getString(Constant.MONTH[curDate.getMonth()]);
					str += " ";
					str += curDate.getDate();
				}else {
					str += (curDate.getMonth()+1);
					str += "-";
					str +=  curDate.getDate();
				}
			} else {
				if (isSameMonth(preDate, curDate)) {// 同一个月，只写日期
					str += curDate.getDate();
				} else {// 与上一个值不同月，写出月份
					if (LocaleUtils.isWestLanguage()) {
						str += Constant.getString(Constant.MONTH[curDate.getMonth()]);
						str += " ";
						str += curDate.getDate();
					}else {
						str += (curDate.getMonth()+1);
						str += "-";
						str +=  curDate.getDate();
					}
				}
			}
		} else if (curFilterType == FILTER_TYPE_YEAR) {
			if (preDate == null) {//第一个，写出年份
				if (LocaleUtils.isWestLanguage()) {
					str += Constant.getString(Constant.MONTH[curDate.getMonth()]);
					str += " ";
					str += (curDate.getYear() + 1900);
				}else {
					str += (curDate.getYear() + 1900);
					str += "-";
					str += (curDate.getMonth()+1);
				}
			} else {//后面的
				if (isSameYear(preDate, curDate)) {//与前一个相同年份
					if (LocaleUtils.isWestLanguage()) {
						str += Constant.getString(Constant.MONTH[curDate.getMonth()]);
					}else {
						str += (curDate.getMonth() +1);
					}
				} else {//与前一个不同年份
					if (LocaleUtils.isWestLanguage()) {
						str += Constant.getString(Constant.MONTH[curDate.getMonth()]);
						str += " ";
						str += (curDate.getYear() + 1900);
					}else {
						str += (curDate.getYear() + 1900);
						str += "-";
						str += (curDate.getMonth() + 1);
					}
				}
			}
		}
		return str;
	}

	/**
	 * 计算横坐标间隔
	 *
	 * @return
	 */
	private int calAxisXStep() {
		switch (curFilterType) {
			case FILTER_TYPE_DAY:
				return 12;
			case FILTER_TYPE_WEEK:
				return 1;
			case FILTER_TYPE_MONTH:
				return 7;
			case FILTER_TYPE_YEAR:
				return 3;
			default:
				return 1;
		}
	}

	/**************** 绘图部分 *******************/
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 如果没初始化参数，则返回
		if (!isValsInited || drawList == null || drawList.size() == 0) {
			LogUtils.d("Chart未初始化参数或drawList为空");
			drawNoDataView(canvas);
			return;
		}
		drawAxis(canvas, drawList);
		drawPath(canvas, drawList);
		drawYText(canvas);
	}

	private void drawNoDataView(Canvas canvas){
		canvas.drawText(Constant.getString(R.string.no_data)
				, screenW/2, screenH/2, noDataTextPaint);
	}

	/**
	 * 画坐标轴
	 *
	 * @param canvas
	 * @param X
	 * @param Y
	 */
	private void drawAxis(Canvas canvas, List<SampleItem> list) {
		// 画横线
		for (int i = 0; i < lineNum; i++) {
			canvas.drawLine(chartStartX, chartStartY + i * lineDis, chartStartX
					+ chartEndX, chartStartY + i * lineDis, axisLinePaint);
		}

		int axisXStep = calAxisXStep();// x坐标抽点间距
		for (int i = 0; i < getCurSampleNum(); i += axisXStep) {
			// X轴文字
			if (i == 0) {// 第一个值,总是写出月份
				String axisStr = makeAxisXStr(null, list.get(i).date);//坐标内容
				float strX = chartStartX + i * xDis - axisStr.length()/3.0f*axisLinePaint.getTextSize();//x轴位置，居中显示
				canvas.drawText(axisStr, strX, chartStartY + (lineNum - 1)
						* lineDis + 30, axisTextPaint);
			} else {
				String axisStr = makeAxisXStr(list.get(i - axisXStep).date, list.get(i).date);//坐标内容
				float strX = chartStartX + i * xDis - axisStr.length()/2.0f*axisLinePaint.getTextSize();//x轴位置，居中显示
				canvas.drawText(axisStr, strX, chartStartY + (lineNum - 1)
						* lineDis + 30,axisTextPaint);
			}
		}
	}

	/**
	 * 画波形
	 *
	 * @param canvas
	 * @param list
	 */
	private void drawPath(Canvas canvas, List<SampleItem> list) {
		int drawSampleNum = getCurSampleNum();
		if (drawSampleNum > list.size()) {
			LogUtils.d("画path需要的sample数和list长度不等");
			return;
		}
		for (int i = 0; i < getCurSampleNum(); i++) {
			SampleItem item = list.get(i);
			float bigR = 5, smallR = 2;

			if (item.isNullSample) {// 无值点
				continue;
			}
			float x = chartStartX + i * xDis;
			if (item.isMultiSamples) {// 多值型
				float bigY = chartEndY - ((item.maxSample - minVal) / (maxVal - minVal))
						* (chartEndY - chartStartY);
				float smallY = chartEndY - ((item.minSample - minVal) / (maxVal - minVal))
						* (chartEndY - chartStartY);
				if (smallY - bigY >= bigR*2-1) { //距离够远，画连个点连线，重叠一点也画两个
					// 两点连线
					canvas.drawLine(x-(float)0.4,bigY,x-(float)0.4,smallY, pathLinePaint);
					// 画最大值
					canvas.drawCircle(x, bigY, bigR, bigPointPaint);
					canvas.drawCircle(x, bigY, smallR, smallPointPaint);
					// 画最小值
					canvas.drawCircle(x, smallY, bigR, bigPointPaint);
					canvas.drawCircle(x, smallY, smallR, smallPointPaint);
				}else {//距离太小，画平均点
					canvas.drawCircle(x, (bigY + smallY)/2, bigR, bigPointPaint);
					canvas.drawCircle(x, (bigY + smallY)/2, smallR, smallPointPaint);
				}
			} else {// 单值型
				float y = chartEndY - ((item.maxSample - minVal) / (maxVal - minVal))
						* (chartEndY - chartStartY);
				canvas.drawCircle(x, y, bigR, bigPointPaint);
				canvas.drawCircle(x, y, smallR, smallPointPaint);
			}
		}
	}

	/**
	 * 画纵坐标
	 *
	 * @param canvas
	 */
	private void drawYText(Canvas canvas) {
		// 画一个白色矩形遮盖趋势图
		// canvas.drawRect(0, 0, chartStartX - 10, lineDis * lineNum + 40,
		// rectPaint);
		// 画横线和纵轴点
		int yStep = (int) ((maxVal - minVal) / (lineNum - 1));
		for (int i = 0; i < lineNum; i++) {
			int value = (int) (maxVal - i * yStep);
			String strValue = new String();
			switch (chartType) {
				case Constant.DLC_INFO_OXYGEN:
					strValue = value + "%";
					break;
				case Constant.DLC_INFO_BP_RE:
					strValue = (value > 0 ? "+" : "") + value + "%";
					break;
				case Constant.DLC_INFO_BP_ABS:
					strValue = value + "";
					break;
				case Constant.DLC_INFO_HR:
					strValue = value + "";
					break;
				case Constant.DLC_INFO_PI:
					strValue = value + "";
					break;
			}
			canvas.drawText(strValue, 0, chartStartY + i * lineDis + 3,
					axisTextPaint);
		}

		//画标题
		String title = "";
		if(chartType == Constant.DLC_INFO_HR)
			title = Constant.getString(R.string.heart_rate);
		else if(chartType == Constant.DLC_INFO_OXYGEN)
			title = Constant.getString(R.string.oxygen_saturation);
		else if (chartType == Constant.DLC_INFO_PI) {
			title = Constant.getString(R.string.pi);
		}else
			title = Constant.getString(R.string.blood_pressure);
		canvas.drawText(title, 0, chartStartY-30 ,titlePaint);
	}

	/*************** 动态调整 ****************/

	/**
	 * 调整周、月、年视图
	 */
	public void switchScale(byte scale) {
		if (!isValsInited || rowList == null || rowList.size() == 0) {
			return;
		}
		if (scale < FILTER_TYPE_DAY || scale > FILTER_TYPE_YEAR) {
			return;
		}
		if (curFilterType != scale) {
			// 改变scale类型，回归当前时间
			curDate = new Date();
			curFilterType = scale;
		}

		// 根据filter类型，得到新的画图list
		switch (curFilterType) {
			case FILTER_TYPE_DAY:
				drawList = filterDayList(rowList);
				break;
			case FILTER_TYPE_WEEK:
				drawList = filterWeekList(rowList);
				break;
			case FILTER_TYPE_MONTH:
				drawList = filterMonthList(rowList);
				break;
			case FILTER_TYPE_YEAR:
				drawList = filterYearList(rowList);
				break;
			default:
				break;
		}
		// 更新点间距
		xDis = (chartEndX - chartStartX) / (getCurSampleNum());
		//重绘
		invalidate();
	}

	/**
	 * 切换最后时间，用于平行切换时间
	 * @param bFront 是否是向前
	 */
	private void switchCurDate(boolean bFront){
		if (curDate == null) {
			return;
		}
		int b = bFront ? 1 : -1;

		if (curFilterType == FILTER_TYPE_DAY) {
			curDate = new Date(curDate.getTime() + b * MILLISECONDS_A_DAY);
		}else if (curFilterType == FILTER_TYPE_WEEK) {
			curDate = new Date(curDate.getTime() + b * WEEK_SAMPLE_NUM * MILLISECONDS_A_DAY);
		}else if (curFilterType == FILTER_TYPE_MONTH) {
			curDate = new Date(curDate.getTime() + b * MONTH_SAMPLE_NUM * MILLISECONDS_A_DAY);
		}else if (curFilterType == FILTER_TYPE_YEAR) {
			curDate = new Date(curDate.getTime() + b * 365 * MILLISECONDS_A_DAY);
		}

		//防止显示超过今天的日期
		if (curDate.getTime() > new Date().getTime()) {
			curDate = new Date();
		}
	}

	//手势操作
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						   float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > 120) {
			LogUtils.d("左划");
			LeftRightDialog.show(mContext, LeftRightDialog.STYLE_LEFT);
			switchCurDate(true);
			switchScale(curFilterType);
			return true;
		} else if (e1.getX() - e2.getX() < -120) {
			LogUtils.d("右划");
			LeftRightDialog.show(mContext, LeftRightDialog.STYLE_RIGHT);
			switchCurDate(false);
			switchScale(curFilterType);
			return true;
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
							float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//TouchEvent dispatcher.
		if (detector != null) {
			if (detector.onTouchEvent(ev))
				//If the gestureDetector handles the event, a swipe has been executed and no more needs to be done.
				return true;
		}
		return super.dispatchTouchEvent(ev);
	}


}
