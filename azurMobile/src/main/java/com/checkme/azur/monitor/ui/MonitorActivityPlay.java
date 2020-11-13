package com.checkme.azur.monitor.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.checkme.azur.monitor.element.Constant;
import com.checkme.azur.monitor.element.ECGComponent;
import com.checkme.azur.monitor.element.ECGData;
import com.checkme.azur.monitor.element.MonitorComponent.CompParameters;
import com.checkme.azur.monitor.element.OxiComponent;
import com.checkme.azur.monitor.element.OxiData;
import com.checkme.azur.monitor.element.RTDataPool;
import com.checkme.azur.monitor.tools.FileCoder;
import com.checkme.azur.monitor.tools.PreferenceUtils;
import com.checkme.azur.monitor.widget.ECGWaveView;
import com.checkme.azur.monitor.widget.MypopuListFinish;
import com.checkme.azur.monitor.widget.OxiWaveView;
import com.checkme.azur.monitor.widget.RTWaveView.WaveViewParameters;
import com.checkme.newazur.R;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorActivityPlay extends Activity {

	private RelativeLayout ecgCompView, oxiCompView;
	private RTDataPool dataPool = new RTDataPool();
	private ECGComponent ecgComponent;
	private OxiComponent oxiComponent;
	public Timer timer;
	public Timer timerAdd, playTimer;
	public ProgressDialog progressDialog = null;
	public TextView tv_play_name, tv_time_long, tv_timer_val;
	public ImageView iv_show;
	public LinearLayout mGallery;
	public LinearLayout rootLayout;
	public MypopuListFinish mypopuListFinish;
	//	public int jishi = 0;
	private int timelong;
	private String time ;
	public boolean pasue = false;

	public Handler mPlayHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
				case Constant.PLAY_FNISH:

					if(timelong<=0){
						if (timer != null) {
							timer.cancel();
							timer = null ;
						}
						playFinishList();
					}

					break;
				case Constant.RE_PLAY:
					break;
				case Constant.BACK_PLAY:
					finish();
					if (timer != null) {
						timer.cancel();
						timer = null ;
					}
					if (timerAdd != null) {
						timerAdd.cancel();
						timerAdd = null ;
					}
					if (playTimer != null) {
						playTimer.cancel();
						playTimer = null ;
					}
					break;
				case Constant.PAUSE_PLAY:
					if(timelong<=0){

						Intent mIntent = new Intent(MonitorActivityPlay.this,MonitorActivityPlay.class);

						mIntent.putExtra("filename", time);

						startActivity(mIntent);
						finish();
						//		rePlay();
					}else{
						pasue = false ;
					}

					break;

				case Constant.PLAY_TIME:
					timelong--;
					//	int alltime = msg.arg1;

					if(timelong<0){
						playTimer.cancel();
						tv_timer_val.setText("00:00:00");
						break ;
					}

					int h = (timelong / 3600);
					int m = ((timelong % 3600) / 60);
					int s = ((timelong % 3600) % 60);
					String TimesH = String.format("%02d", h);
					String TimesM = String.format("%02d", m);
					String TimesS = String.format("%02d", s);
					tv_timer_val.setText(TimesH + ":" + TimesM + ":" + TimesS);

					if (timelong == 0) {
						//	jishi = 0;
						playTimer.cancel();
					}

					break;

				case Constant.PLAY:

					break;
				case Constant.DELETE:

					break;
				case Constant.DELETE_ALL:

					break;

				default:
					break;
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_monitor_play);
		super.onCreate(savedInstanceState);

		rootLayout = (LinearLayout) findViewById(R.id.rl_components);
		tv_play_name = (TextView) findViewById(R.id.tv_name_val);
		tv_time_long = (TextView) findViewById(R.id.tv_time_val);
		addECGComponentView();
		addOxiComponentView();
		rootLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mypopuListFinish == null) {
					mypopuListFinish = new MypopuListFinish(mPlayHandler);
				}
				mypopuListFinish.popuWindow.showAtLocation(
						findViewById(R.id.rl_show_play), Gravity.CENTER, 0, 0);

				pasue = true ;

			}
		});
		// 准备播放数据
		preparePlay();

	}


	private void preparePlay() {
		// TODO Auto-generated method stub
		// i = 0 ; //播放的时候 i = 0

		tv_timer_val = (TextView) findViewById(R.id.tv_paly_time);


		// 获取文件名
		Intent mIntent = getIntent();

		time = mIntent.getStringExtra("filename");

		// 播放数据
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				playSaveDate(time);
			}
		});

	}



	private void playSaveDate(final String time) {

		timelong = PreferenceUtils.readIntPreferences(getApplicationContext(),
				time); // 单位是秒

		int h = (timelong / 3600);
		int m = ((timelong % 3600) / 60);
		int s = ((timelong % 3600) % 60);
		String TimesH = String.format("%02d", h);
		String TimesM = String.format("%02d", m);
		String TimesS = String.format("%02d", s);
		tv_timer_val.setText(TimesH + ":" + TimesM + ":" + TimesS);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				byte[] buf = null;
				try {
					buf = FileCoder.read(Constant.dir, time);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				itemNum = buf.length / 44;
				startAddData(40, buf, itemNum);
			}

		}, 0);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				makeECGComponent(ecgCompView, dataPool.getEcgDatas());
				makeOxiComponent(oxiCompView, dataPool.getOxiDatas());
			}
		}, 100);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 计时
				playRemeberTimer(1000);

			}
		}, 200);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				startToRefreshViews(40);
			}
		}, 200);

	}

	public int i = 0;
	public int itemNum;// 防止重复添加数据。

	private void startAddData(int timeadd, final byte[] buf, final int num) {
		// TODO Auto-generated method stub

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {

				if (!pasue) {
					byte[] b = new byte[44];
					System.arraycopy(buf, i * 44, b, 0, 44);
					i++;
					dataPool.addDatas(b);
				}
				if (i == num) {
					timerAdd.cancel();
					timerAdd = null ;
					i = 0;
				}

			}
		};
		timerAdd = new Timer();
		timerAdd.schedule(timerTask, 0, timeadd);

	}

	private void playRemeberTimer(int t) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				// 暂停

				if (!pasue) {
					Message message = new Message();
					message.what = Constant.PLAY_TIME;
					//	message.arg1 = (int) longtime;
					mPlayHandler.sendMessage(message);
				}

			}
		};
		playTimer = new Timer();
		playTimer.schedule(timerTask, 1000, t);
	}

	private View loadComponentView(int res) {
		LayoutInflater inflater = LayoutInflater.from(MonitorActivityPlay.this);
		View ecgCompView = inflater.inflate(res, null);
		return ecgCompView;
	}

	private void addECGComponentView() {

		ecgCompView = (RelativeLayout) loadComponentView(R.layout.widget_ecg_component);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params.weight = 1;
		ecgCompView.setLayoutParams(params);
		ecgCompView.findViewById(R.id.ll_function).setVisibility(View.GONE);
		rootLayout.addView(ecgCompView);

	}

	private void addOxiComponentView() {
		oxiCompView = (RelativeLayout) loadComponentView(R.layout.widget_oxi_component);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		params.weight = 1;
		oxiCompView.setLayoutParams(params);
		rootLayout.addView(oxiCompView);

	}

	// test
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
		ECGWaveView waveView = new ECGWaveView(MonitorActivityPlay.this,
				waveParameters, getResources().getDisplayMetrics().ydpi);

		// ecg component
		CompParameters parameters = new CompParameters(1);
		ecgComponent = new ECGComponent(compLayout, waveView, ecgDatas,
				parameters,false);

		// ecgComponent.startToRefreshViews();
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
		OxiWaveView waveView = new OxiWaveView(MonitorActivityPlay.this,
				waveParameters);

		// oxi component
		CompParameters parameters = new CompParameters(1);
		// oxiComponent.startToRefreshViews();
		oxiComponent = new OxiComponent(compLayout, waveView, oxiDatas,
				parameters,false);
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

	private void startToRefreshViews(int refreshRate) {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 暂停
				if (!pasue) {
					if (ecgComponent != null && oxiComponent != null) {
						ecgComponent.doRefreshViews(mPlayHandler, "play");
						oxiComponent.doRefreshViews(mPlayHandler, "play");
					}
				}

			}
		};
		timer = new Timer();
		timer.schedule(timerTask, 0, refreshRate);

	}

	/**
	 * Start to monitor battery state
	 */

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				finish();
				if (timer != null) {
					timer.cancel();
					timer = null ;
				}
				if (timerAdd != null) {
					timerAdd.cancel();
					timerAdd = null ;
				}
				if (playTimer != null) {
					playTimer.cancel();
					playTimer = null ;
				}

				dataPool = null ;
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void playFinishList() {
		dataPool.getEcgDatas().clear() ;
		dataPool.getOxiDatas().clear() ;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				// TODO Auto-generated method stub
				if (mypopuListFinish == null) {
					mypopuListFinish = new MypopuListFinish(mPlayHandler);
				}
				mypopuListFinish.popuWindow.showAtLocation(
						findViewById(R.id.rl_show_play), Gravity.CENTER, 0, 0);
			}
		});
	}

}
