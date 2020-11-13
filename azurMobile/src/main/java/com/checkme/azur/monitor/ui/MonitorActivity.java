package com.checkme.azur.monitor.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.checkme.azur.activity.MainActivity;
import com.checkme.azur.monitor.bt.ReadThreadListener;
import com.checkme.azur.monitor.element.Constant;
import com.checkme.azur.monitor.element.ECGComponent;
import com.checkme.azur.monitor.element.ECGData;
import com.checkme.azur.monitor.element.MonitorComponent.CompParameters;
import com.checkme.azur.monitor.element.OtherData;
import com.checkme.azur.monitor.element.OxiComponent;
import com.checkme.azur.monitor.element.OxiData;
import com.checkme.azur.monitor.element.RTDataPool;
import com.checkme.azur.monitor.element.StatusBarComponent;
import com.checkme.azur.monitor.tools.BitmapUtils;
import com.checkme.azur.monitor.tools.FileCoder;
import com.checkme.azur.monitor.tools.PreferenceUtils;
import com.checkme.azur.monitor.utils.LogUtils;
import com.checkme.azur.monitor.widget.ECGWaveView;
import com.checkme.azur.monitor.widget.OxiWaveView;
import com.checkme.azur.monitor.widget.RTWaveView.WaveViewParameters;
import com.checkme.newazur.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorActivity extends Activity {

	private TextView deviceSn;
	private RelativeLayout ecgCompView, oxiCompView;
	private RTDataPool dataPool = new RTDataPool();
	private BatteryReceiver batteryReceiver;
	private ECGComponent ecgComponent;
	private OxiComponent oxiComponent;
	private StatusBarComponent statusBarComponent;
	private Timer timer, timejishi;
	private static final int SCREEN_SHOT = 1079;
	public final static int MSG_BT_CONNECT_TIMEOUT = 1078;
	public static MonitorActivity instance = null;
	public ProgressDialog progressDialog = null;
	public boolean b = false; // 截屏的标识
	public int width;
	public int height;
	public boolean flag = false; // 判断是否录制的标识
//	public long start = 0, end = 0;

	private LinearLayout ll_luzhi;
	private TextView tv_luzhi_time;
	private Dialog dialog;

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
				case SCREEN_SHOT:
					timer.cancel();

					if (progressDialog == null) {
						progressDialog = new ProgressDialog(MonitorActivity.this);

						progressDialog.setTitle(Constant
								.getString(R.string.loading));
						progressDialog.setMessage(Constant
								.getString(R.string.searching));
						// progressDialog.setCancelable(false);
						progressDialog.show();
					}
					break;
				case MSG_BT_CONNECT_TIMEOUT:
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					LogUtils.d("是monitor这个的提示");
					new AlertDialog.Builder(MonitorActivity.this)
							.setTitle(Constant.getString(R.string.warning))
							.setMessage(Constant.getString(R.string.connect_failed))
							.setCancelable(false)
							.setPositiveButton(Constant.getString(R.string.exit),
									ConnectFailedDialogListener)
							.setCancelable(false).show();
					break;
				case Constant.START_JISHI:
					int h = (int) (jishi / 3600);
					int m = (int) ((jishi % 3600) / 60);
					int s = (int) ((jishi % 3600) % 60);
					String TimesH = String.format("%02d", h);
					String TimesM = String.format("%02d", m);
					String TimesS = String.format("%02d", s);
					tv_luzhi_time.setText(TimesH + ":" + TimesM + ":" + TimesS);
					break;

				case Constant.SCREEN_CUT:
					Toast.makeText(MonitorActivity.this,
							R.string.save_successfully, Toast.LENGTH_SHORT).show();

					BitmapUtils.saveBitmap(Constant.pic_dir,
							getScreen(), new SimpleDateFormat(
									"yyyy-MM-dd_HH-mm-ss")
									.format(new Date()));

					break ;
				default:
					break;
			}

		}

	};

	public DialogInterface.OnClickListener ConnectFailedDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if (which == AlertDialog.BUTTON_POSITIVE) {
				LogUtils.d("选择退出");
				if (MainActivity.instance.timer != null) {
					MainActivity.instance.timer.cancel(); // 退出程序时，也要取消计时器，不然一直在走。
				}
				if (timer != null) {
					timer.cancel(); // 自己本身的也要取消
				}

				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_monitor);
		super.onCreate(savedInstanceState);

		if (Constant.binder != null) {
			Constant.binder.interfaceStartRead(listener); // 实现数据读取成功后的回调。
		}
		Constant.initDir();
		addECGComponentView();
		addOxiComponentView();

		deviceSn = (TextView) findViewById(R.id.tv_checkme_sn);
		deviceSn.setText(PreferenceUtils.readStrPreferences(getApplicationContext(),
				"PreDeviceName"));

		ll_luzhi = (LinearLayout) findViewById(R.id.ll_luzhi);
		tv_luzhi_time = (TextView) findViewById(R.id.tv_luzhi_time);

		instance = this;
		// 获取屏幕长和高
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				startToRefreshViews(40);
			}
		}, 150);
		// 电池

		startToMonitorBattery();
		makeStatusBarComponent(MonitorActivity.this,
				(RelativeLayout) findViewById(R.id.rl_status_bar),
				dataPool.getOtherDatas());


	}
	public void aboutinfo(View v){
		create();
		show();
	}
	private void show(){
		dialog.show();
	}
	private void create(){
		dialog=new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.setContentView(R.layout.about);
//	    	initImageView();
		initVersion();
	}
	private void initVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			TextView textView=(TextView) dialog.findViewById(R.id.version_info);
			textView.setText(Constant.getString(R.string.version) + " " + info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
//		private void initImageView(){
//	    	ImageView image=(ImageView)dialog.findViewById(R.id.image);
//	    	image.setImageResource(R.drawable.aboutlogo);
//	    	image.setOnClickListener(new ImageView.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					if(dialog!=null){
//						dialog.dismiss();
//					}
//				}
//			});
//	    }

	private View loadComponentView(int res) {
		LayoutInflater inflater = LayoutInflater.from(MonitorActivity.this);
		View ecgCompView = inflater.inflate(res, null);
		return ecgCompView;
	}

	private void addECGComponentView() {
		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.rl_components);
		ecgCompView = (RelativeLayout) loadComponentView(R.layout.widget_ecg_component);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params.weight = 1;
		ecgCompView.setLayoutParams(params);
		rootLayout.addView(ecgCompView);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				makeECGComponent(ecgCompView, dataPool.getEcgDatas());
			}
		}, 100);
	}

	private void addOxiComponentView() {
		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.rl_components);
		oxiCompView = (RelativeLayout) loadComponentView(R.layout.widget_oxi_component);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params.weight = 1;
		oxiCompView.setLayoutParams(params);
		rootLayout.addView(oxiCompView);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				makeOxiComponent(oxiCompView, dataPool.getOxiDatas());

			}
		}, 100);
	}

	/**
	 * @param compLayout
	 */
	private void makeECGComponent(final RelativeLayout compLayout,
								  List<ECGData> ecgDatas) {
		// ecg wave
		RelativeLayout waveLayout = (RelativeLayout) compLayout
				.findViewById(R.id.rl_wave);
		waveLayout.measure(0, 0);
		WaveViewParameters waveParameters = new WaveViewParameters(
				getResources().getColor(R.color.MonitorGreen),
				waveLayout.getWidth(), waveLayout.getHeight(), 100, -100,
				calXdis(25, 8), 1);
		ECGWaveView waveView = new ECGWaveView(MonitorActivity.this,
				waveParameters, getResources().getDisplayMetrics().ydpi);

		// ecg component
		CompParameters parameters = new CompParameters(1);
		ecgComponent = new ECGComponent(compLayout, waveView, ecgDatas,
				parameters,true);


		ImageView iv_screen = (ImageView) compLayout
				.findViewById(R.id.iv_screen);
		// TODO Auto-generated method stub
		iv_screen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// long l =
				// FileSizeUtil.getAvailableInternalMemorySize();
					/*	long l = FileSizeUtil
								.getmem_UNUSED(MonitorActivity.this);

						LogUtils.d(l + "");*/

				Message msg = new Message();

				msg.what = Constant.SCREEN_CUT ;

				mHandler.sendMessage(msg);





			}
		});


		// TODO Auto-generated method stub
		final ImageView iv_transcribe = (ImageView) compLayout
				.findViewById(R.id.iv_transcribe);
		iv_transcribe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!flag) {
					flag = true;
					ll_luzhi.setVisibility(View.VISIBLE);
					openTimer();
					Animation animation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.luzhi_change_alpha);
					//	animation.setFillAfter(true);
					iv_transcribe.startAnimation(animation);

					iv_transcribe.setImageResource(R.drawable.tingzhi);
				} else {
					flag = false;
					ll_luzhi.setVisibility(View.GONE);
					timejishi.cancel();
					stopTranscribe();
					jishi = 0;
					time = null ;
					Animation animation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.luzhi_change_alpha);
					//	animation.setFillAfter(true); 设为true相当于保存当前状态不在改变
					iv_transcribe.startAnimation(animation);
					iv_transcribe.setImageResource(R.drawable.luzhi);
					Toast.makeText(MonitorActivity.this, R.string.save_successfully,Toast.LENGTH_SHORT).show();
					// iv_transcribe.setBackgroundResource(R.drawable.luzhi);
				}

			}
		});

		ImageView iv_detail = (ImageView) compLayout
				.findViewById(R.id.iv_detail);
		iv_detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(MonitorActivity.this,
						MonitorDetaiListActivity.class);
				startActivity(mIntent);
			}
		});

	}

	private int jishi = 0;

	private void openTimer() {
		timejishi = new Timer();

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = Constant.START_JISHI;
				mHandler.sendMessage(msg);
				jishi++;
			}
		};

		timejishi.schedule(task, 0, 1000);

	}

	// 截屏
	public Bitmap getScreen() {
		View view = getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		// 获取状态栏高度
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return b;
	}

	/**
	 * @param compLayout
	 */
	private void makeOxiComponent(RelativeLayout compLayout,
								  List<OxiData> oxiDatas) {
		// oxi wave
		RelativeLayout waveLayout = (RelativeLayout) compLayout
				.findViewById(R.id.rl_wave);
		waveLayout.measure(0, 0);
		WaveViewParameters waveParameters = new WaveViewParameters(
				getResources().getColor(R.color.MonitorSkyBlue),
				waveLayout.getWidth(), waveLayout.getHeight(), 0, 255, calXdis(
				25, 8), 1);
		OxiWaveView waveView = new OxiWaveView(MonitorActivity.this,
				waveParameters);

		// oxi component
		CompParameters parameters = new CompParameters(1);
		// oxiComponent.startToRefreshViews();
		oxiComponent = new OxiComponent(compLayout, waveView, oxiDatas,
				parameters,true);
	}

	private void makeStatusBarComponent(Context context,
										RelativeLayout relativeLayout, List<OtherData> otherDatas) {
		statusBarComponent = new StatusBarComponent(context, relativeLayout,
				otherDatas);
	}

	/**
	 * @param speed
	 *            Wave Speed(mm/s) (default:25mm/s)
	 * @param xTimeDis
	 *            Time distance between two points(ms) (default:8ms)
	 * @return
	 */
	private float calXdis(int speed, int xTimeDis) {
		if (speed <= 0 || xTimeDis <= 0) {
			return 1;
		}
		DisplayMetrics dm = getResources().getDisplayMetrics();

		// Physical distance between two points in millimeter
		float xPhysicalDisMM = xTimeDis / 1000f * speed;
		// Physical distance between two points in inch
		float xPhysicalDisInch = xPhysicalDisMM / 25.4f;
		// Pixel distance
		float xDis = xPhysicalDisInch * dm.xdpi;

		return xDis;
	}

	public String time = null;

	private ReadThreadListener listener = new ReadThreadListener() {

		@Override
		public void onReadThreadFinished(final byte[] buf) {
			// TODO Auto-generated method stub

			if (dataPool != null) {
				synchronized (dataPool) {

					dataPool.addDatas(buf);

					// 判断开始录制视频。
					if (flag) {

						if (time == null) {
							time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
									.format(new Date());
						}

						FileCoder.write(Constant.dir, time, buf);

					}

				}
			}

		}

		@Override
		public void onReadThreadFailed(byte errCode) {
			// TODO Auto-generated method stub
			LogUtils.e("Read RTData failed");
		}
	};

	public void stopTranscribe() {

		// 存储完数据后，在存储时间和存储名字。
		//	long atime = (end - start) / 1000; // 单位是秒
		PreferenceUtils.savePreferences(getApplicationContext(), time, jishi); // 保存录制了多少时间。

	}

	private void startToRefreshViews(int refreshRate) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (ecgComponent != null && oxiComponent != null) {
					ecgComponent.doRefreshViews(mHandler, "moni");
					oxiComponent.doRefreshViews(mHandler, "moni");
				}
				if (statusBarComponent != null) {
					statusBarComponent.refreshViews();
				}
			}
		};
		timer = new Timer();
		timer.schedule(timerTask, 0, refreshRate);

	}

	/**
	 * Start to monitor battery state
	 */
	private void startToMonitorBattery() {

		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		batteryReceiver = new BatteryReceiver();
		registerReceiver(batteryReceiver, intentFilter);
	}

	class BatteryReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 判断它是否是为电量变化的Broadcast Action
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 获取当前电量
				int level = intent.getIntExtra("level", 0);
				// 电量的总刻度
				int scale = intent.getIntExtra("scale", 100);
				// 把它转成百分比
				float powerPercent = (level * 100f) / scale;
				ImageView ivBattery = (ImageView) findViewById(R.id.iv_battery);
				if (powerPercent >= 75) {
					ivBattery.setImageDrawable(getResources().getDrawable(
							R.drawable.battery1));
				} else if (powerPercent >= 50) {
					ivBattery.setImageDrawable(getResources().getDrawable(
							R.drawable.battery2));
				} else if (powerPercent >= 25) {
					ivBattery.setImageDrawable(getResources().getDrawable(
							R.drawable.battery3));
				} else {
					ivBattery.setImageDrawable(getResources().getDrawable(
							R.drawable.battery4));
				}
			}
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				LogUtils.d("");

				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}

				if (timer != null) {
					timer.cancel(); // 自己本身的也要取消
				}
				finish();
				if (MainActivity.instance.timer != null) {
					MainActivity.instance.timer.cancel(); // 退出程序时，也要取消计时器，不然一直在走。
					if (MainActivity.instance.progressDialog != null) {
						MainActivity.instance.progressDialog.dismiss();
					}

					MainActivity.instance.finish();
				}

				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
