package com.checkme.azur.activity;

import java.util.List;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.ECGDetailFragment;
import com.checkme.azur.fragment.ECGMainFragment;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.LogUtils;

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
		reFreshTitle(Constant.getString(R.string.title_ecg));
		reFreshActionBarBn(0,false,false);
		initECGMainFragment();
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
