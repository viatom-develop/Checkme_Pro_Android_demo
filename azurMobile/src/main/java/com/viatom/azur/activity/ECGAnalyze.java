package com.viatom.azur.activity;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.viatom.newazur.R;
import com.viatom.azur.measurement.ECGInnerItem;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.widget.ECGAnalyzeView;

public class ECGAnalyze extends Activity implements OnClickListener{

	private ECGAnalyzeView ecgAnalyzeView;
	private ECGInnerItem curItem;
	private Date date;
	private String userName;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);// warning 这句报错
		//如果不是横屏 设置默认横屏
		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		setContentView(R.layout.activity_ecg_analyze);
		setButtonsFunc();
	}

	public void initUI() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		date = (Date) bundle.getSerializable("Date");
		userName = bundle.getString("UserName");
		int FirstSample = bundle.getInt("FirstSample");
		curItem = (ECGInnerItem) bundle.getSerializable("CurItem");
		((TextView) findViewById(R.id.ECGAnalyzeTextTitle)).setText("ECG "
				+ StringMaker.makeDateString(date));
		LinearLayout Linear = (LinearLayout) findViewById(R.id.ECGAnalyzeLinearChart);
		Linear.removeAllViews();
		ecgAnalyzeView = new ECGAnalyzeView(ECGAnalyze.this, curItem,
				FirstSample, Linear.getWidth(), Linear.getHeight());
		Linear.addView(ecgAnalyzeView);
		initSeekBar();

	}

	/**
	 * 设置各按键功能
	 */
	public void setButtonsFunc() {
		Button bnBack = (Button) findViewById(R.id.ECGAnalyzeBnBack);
		bnBack.setOnClickListener(this);
	}

	public void initSeekBar() {
		SeekBar seekBar = (SeekBar) findViewById(R.id.ECGAnalyzeSeekBar);
		seekBar.setMax(ecgAnalyzeView.getLength()
				- this.getWindowManager().getDefaultDisplay().getWidth() + 100);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int progress,
										  boolean fromUser) {
				ecgAnalyzeView.setCurrentX(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar bar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar bar) {
			}
		});
		ecgAnalyzeView.setSeekBar(seekBar);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		//横竖切换后重绘
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initUI();
			}
		}, 20);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus && ecgAnalyzeView==null){
			initUI();
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (arg0.getId() == R.id.ECGAnalyzeBnBack) {
			finish();
		}
	}

}
