package com.viatom.azur.activity;

import java.util.List;
import java.util.Locale;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.fragment.ECGDetailFragment;
import com.viatom.azur.fragment.ECGMainFragment;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ECGMain extends BaseActivity {

	private Bundle preDetailBundle;

	Handler ECGMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constant.MSG_GOTO_ECG_DETAIL:
					preDetailBundle = (Bundle)msg.obj;
					initECGDetailFragment((Bundle)msg.obj);
					reFreshActionBarBn(1,false,true);
					break;
			}
		}
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNECG_CLICKED);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ecg_main);
		EventBus.getDefault().register(this);
		reFreshTitle(Constant.getString(R.string.title_ecg));
		reFreshActionBarBn(0,false,false);
		initECGMainFragment();
	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onFlushIvEvent(FlushIvEvent event) {
		initBtnCloud(event.getUploadState());
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	public void initECGMainFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		ECGMainFragment fragment = new ECGMainFragment();
		fragment.setArguments(ECGMainHandler);
		fragmentTransaction.replace(R.id.ECGReMain,fragment);
		fragmentTransaction.commit();
	}

	public void initECGDetailFragment(Bundle bundle) {
		if (bundle == null) {
			LogUtils.d("bundle错误，无法跳转");
			return;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.popBackStack();//warning test,防止横屏重复添加detail
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		ECGDetailFragment fragment = new ECGDetailFragment();
		fragment.setArguments(bundle);
		fragmentTransaction.replace(R.id.ECGReMain,fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		//切换横竖屏后重载ECG详情frag
		if (getSupportFragmentManager().getBackStackEntryCount()!=0) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					initECGDetailFragment(preDetailBundle);
				}
			}, 20);

		}
	}

	protected void onBnShareClicked() {
		// TODO Auto-generated method stub
		LogUtils.d("share键按下");
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for(Fragment f: fragments) {
			LogUtils.d("f ==  " + f);
		}
		if (fragments.size() >= 2) {
			int index = fragments.size() - 1;
			ECGDetailFragment detailFragment = (ECGDetailFragment)fragments.get(index);
			detailFragment.showShareAlertView();
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
			case R.id.WidgetActionbarBnMenu:
				if(getSupportFragmentManager().getBackStackEntryCount()==0){
					//在主界面
					toggle();
				}else{
					getSupportFragmentManager().popBackStack();
					reFreshActionBarBn(0,false,false);
				}
				break;
			case R.id.WidgetActionbarBnShare:
				onBnShareClicked();
				break;
			case R.id.WidgetActionbarBnCloud:
				showSettingActivity();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if (getSupportFragmentManager().getBackStackEntryCount()==0) {
				return super.onKeyDown(keyCode, event);
			}else {
				getSupportFragmentManager().popBackStack();
				reFreshActionBarBn(0,false,false);
			}
		}
		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			if (Constant.defaultUser==null)
				return;
			FileUtils.saveListToFileWithoutUndownloaded(Constant.dir
					, MeasurementConstant.FILE_NAME_ECG_LIST_OLD
					, Constant.defaultUser.getEcgList());
		}
	}

}
