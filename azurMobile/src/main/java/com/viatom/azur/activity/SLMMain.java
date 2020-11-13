package com.viatom.azur.activity;

import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.bluetooth.Logger;
import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.fragment.SLMDetailFragment;
import com.viatom.azur.fragment.SLMMainFragment;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SLMMain extends BaseActivity {

	private Bundle preDetailBundle;//上次进入detail的bundle

	Handler SLMMainHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constant.MSG_GOTO_SLM_DETAIL:
					preDetailBundle = (Bundle)msg.obj;
					initSLMDetailFragment((Bundle)msg.obj);
					reFreshActionBarBn(1,false,true);
					break;
			}
		};
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNSLM_CLICKED);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LogUtils.d("调用slmMain的oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slm_main);
		EventBus.getDefault().register(this);
		InitUI();
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

	/**
	 * UI初始化
	 */
	public void InitUI() {
		reFreshTitle(Constant.getString(R.string.title_slm));
		reFreshActionBarBn(0,false,false);
		initSLMMainFragment();
	}

	public void initSLMMainFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SLMMainFragment fragment = new SLMMainFragment();
		fragment.setArguments(SLMMainHandler);
		fragmentTransaction.add(R.id.SLMReMain,fragment);
		fragmentTransaction.commit();
	}

	public void initSLMDetailFragment(Bundle bundle) {
		if (bundle == null) {
			return;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.popBackStack();//warning test 防止多次创建detail
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SLMDetailFragment fragment = new SLMDetailFragment();
		fragment.setArguments(bundle);
		fragmentTransaction.replace(R.id.SLMReMain,fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		//如果在详情页面，重新加载detailFrag
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (getSupportFragmentManager().getBackStackEntryCount()!=0) {
					initSLMDetailFragment(preDetailBundle);
				}
			}
		}, 20);
	}

	protected void onBnShareClicked() {
		// TODO Auto-generated method stub
		LogUtils.d("share键按下");
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		Logger.d(SLMMain.class, "fragments.size() == " + fragments.size());
		if (fragments.size() >= 2) {
			int index = fragments.size() - 1;
			SLMDetailFragment detailFragment = (SLMDetailFragment)fragments.get(index);
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
					, MeasurementConstant.FILE_NAME_SLM_LIST_OLD
					, Constant.defaultUser.getSlmList());
		}
	}

}
