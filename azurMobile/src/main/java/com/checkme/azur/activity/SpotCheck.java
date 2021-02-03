package com.checkme.azur.activity;

import java.util.List;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.ReadFileListener;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.RightMenuFragment2;
import com.checkme.azur.fragment.SpotCheckDetailFragment;
import com.checkme.azur.fragment.SpotCheckMainFragment;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.measurement.SpotUser;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;

public class SpotCheck extends BaseActivity implements ReadFileListener{

	Bundle preBundle;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constant.MSG_USER_CHOSED:
					getSlidingMenu().showContent();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							initSpotCheckMainFragment();
							reFreshActionBarBn(0,true,false);
							String patientId = "";
							if(Constant.curSpotUser != null && Constant.curSpotUser.getUserInfo() != null) {
								patientId = Constant.curSpotUser.getUserInfo().getPatientID();
								if(!TextUtils.isEmpty(patientId)) {
									patientId = patientId.trim();
								}
							}
							if(!TextUtils.isEmpty(patientId)) {
								reFreshTitle(Constant.getString(R.string.title_spot_check) + " - " + patientId);
							} else {
								reFreshTitle(Constant.getString(R.string.title_spot_check));
							}
						}
					}, 300);
					break;
				case Constant.MSG_GOTO_SPOT_DETAIL:
					preBundle = (Bundle)msg.obj;
					initSpotCheckDetailFragment((Bundle)msg.obj);
					reFreshActionBarBn(1,false,true);
					break;
				case MeasurementConstant.CMD_TYPE_SPOT_USER_LIST:
					readLocalUserList();
					break;
			}
		}
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNSPOT_CLICKED);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spot_check);

		//没有用户列表的时候获取用户列表
		if (Constant.spotUserList == null) {
			getSpotCheckUserList();
		}else {
			setSlidingMenu();
			initSpotCheckMainFragment();
			reFreshActionBarBn(0,true,false);
			reFreshTitle(Constant.getString(R.string.title_spot_check) + " - "
					+ new String(Constant.curSpotUser.getUserInfo().getPatientID()));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void getSpotCheckUserList() {
		if(Constant.btConnectFlag){
			LogUtils.d("spot下载用户列表");
			Constant.binder.interfaceReadFile(MeasurementConstant.FILE_NAME_SPOT_USER_LIST
					, MeasurementConstant.CMD_TYPE_SPOT_USER_LIST, 5000, this);
		}else{
			LogUtils.d("spot读本地用户列表");
			readLocalUserList();
		}
	}

	@Override
	public void onReadPartFinished(String fileName, byte fileType, float percentage) {
		LogUtils.d(fileName + "部分完成  " + percentage);
	}

	@Override
	public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
		LogUtils.d(fileName + "读取成功");
		//删除旧文件，保存新数据表到本地文件
		FileDriver.delFile(Constant.dir, fileName);
		FileDriver.write(Constant.dir,  fileName, fileBuf);
		MsgUtils.sendMsg(handler, fileType);
	}

	@Override
	public void onReadFailed(String fileName, byte fileType, byte errCode) {
		LogUtils.d(fileName + "读取失败  " + errCode);
		// 读失败同样删除旧文件
		FileDriver.delFile(Constant.dir, fileName);
		MsgUtils.sendMsg(handler, fileType);
	}

	private void readLocalUserList() {
		Constant.spotUserList = FileUtils.readSpotUserList(Constant.dir
				, MeasurementConstant.FILE_NAME_SPOT_USER_LIST);
		if (Constant.spotUserList != null) {// 存在用户列表文件
			Constant.defaultSpotUser = Constant.spotUserList[0];
			Constant.curSpotUser = Constant.defaultSpotUser;
			reloadPreUserIndex();
			setSlidingMenu();
			initSpotCheckMainFragment();
			reFreshActionBarBn(0,true,false);
			reFreshTitle(Constant.getString(R.string.title_spot_check) + " - "
					+ new String(Constant.curSpotUser.getUserInfo().getPatientID()));
		}else {// 没有用户文件
			processNoUserFile();
		}
	}

	private void reloadPreUserIndex() {
		int preUserIndex = (int)PreferenceUtils.readIntPreferences(
				getApplicationContext(), "PreSpotUserIndex");
		if (preUserIndex>Constant.spotUserList.length-1) {//防止主机删除用户后越界
			preUserIndex = 0;
		}
		Constant.curSpotUser = Constant.spotUserList[preUserIndex];
	}

	public void processNoUserFile() {
		SpotUser u = new SpotUser();
		u.getUserInfo().name = "Guest";
		u.getUserInfo().id=1;
		Constant.curSpotUser = u;
//		findViewById(R.id.ImgNoInfo).setVisibility(View.VISIBLE);
//		new AlertDialog.Builder(SpotCheck.this)
//		 .setTitle(Constant.getString(R.string.warning))
//		 .setMessage(Constant.getString(R.string.no_user_info)).setCancelable(false)
//		 .setPositiveButton(Constant.getString(R.string.exit),
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog,
//								int which) {
//							finish();
//						}
//					})
//		 .show();

//		new AlertDialog.Builder(SpotCheck.this)
//		 .setTitle(Constant.getString(R.string.warning))
//		 .setMessage(Constant.getString(R.string.no_user_info)).setCancelable(false)
//		 .setPositiveButton(Constant.getString(R.string.exit),
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog,
//								int which) {
//							finish();
//						}
//					})
//		.setNegativeButton(Constant.getString(R.string.cancel),
//				new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.cancel();
//						}
//					})
//		 .show();
	}

	/**
	 * 设置右边用户列表
	 *
	 */
	public void setSlidingMenu(){
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setSecondaryMenu(R.layout.menu_frame_two);
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);

		RightMenuFragment2 rmf = new RightMenuFragment2();
		rmf.setmHandler(handler);
		rmf.setUserList();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame_two,rmf)
				.commit();
	}

	public void initSpotCheckMainFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SpotCheckMainFragment fragment = new SpotCheckMainFragment();
		fragment.setArguments(handler);
		fragmentTransaction.replace(R.id.SpotCheckReMain,fragment);
		fragmentTransaction.commit();
	}

	public void initSpotCheckDetailFragment(Bundle bundle) {
		if (bundle == null) {
			LogUtils.d("bundle错误，无法跳转");
			return;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.popBackStack();//warning test,防止横屏重复添加detail
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SpotCheckDetailFragment fragment = new SpotCheckDetailFragment();
		fragment.setArguments(bundle);
		fragmentTransaction.replace(R.id.SpotCheckReMain,fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	protected void onBnShareClicked() {
		LogUtils.d("share键按下");
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments.size() >= 3) {
			int index = fragments.size() - 1;
			SpotCheckDetailFragment detailFragment = (SpotCheckDetailFragment)fragments.get(index);
			detailFragment.showShareAlertView();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (getSupportFragmentManager().getBackStackEntryCount()==0) {
					//主界面
					initSpotCheckMainFragment();
				}else {
					//详情界面
					initSpotCheckDetailFragment(preBundle);
				}
			}
		}, 20);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.WidgetActionbarBnUser:
				showSecondaryMenu();
				//切换用户前保存当前spot列表
				saveCurUserSpotCheckList();
				break;
			case R.id.WidgetActionbarBnMenu:
				if(getSupportFragmentManager().getBackStackEntryCount()==0){
					//在主界面
					toggle();
				}else{
					getSupportFragmentManager().popBackStack();
					String patientId = "";
					if(Constant.curSpotUser != null && Constant.curSpotUser.getUserInfo() != null) {
						patientId = Constant.curSpotUser.getUserInfo().getPatientID();
						if(!TextUtils.isEmpty(patientId)) {
							patientId = patientId.trim();
						}
					}
					if(!TextUtils.isEmpty(patientId)) {
						reFreshTitle(Constant.getString(R.string.title_spot_check) + " - " + patientId);
					} else {
						reFreshTitle(Constant.getString(R.string.title_spot_check));
					}
					reFreshActionBarBn(0,true,false);
				}
				break;
			case R.id.WidgetActionbarBnShare:
				onBnShareClicked();
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if (getSupportFragmentManager().getBackStackEntryCount()==0) {
				return super.onKeyDown(keyCode, event);
			}else {
				getSupportFragmentManager().popBackStack();
				String patientId = "";
				if(Constant.curSpotUser != null && Constant.curSpotUser.getUserInfo() != null) {
					patientId = Constant.curSpotUser.getUserInfo().getPatientID();
					if(!TextUtils.isEmpty(patientId)) {
						patientId = patientId.trim();
					}
				}
				if(!TextUtils.isEmpty(patientId)) {
					reFreshTitle(Constant.getString(R.string.title_spot_check) + " - " + patientId);
				} else {
					reFreshTitle(Constant.getString(R.string.title_spot_check));
				}
				reFreshActionBarBn(0,true,false);
//				reFreshActionBarBn(0,false,false);
			}
		}
		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			// 离开act时保存当前用户列表
			saveCurUserSpotCheckList();
		}
	}

	/**
	 * 保存当前用户spot列表
	 */
	private void saveCurUserSpotCheckList() {
		if (Constant.curSpotUser==null) {
			return;
		}
		LogUtils.d("保存spot列表，用户ID：" + Constant.curSpotUser.getUserInfo().getId());
		FileUtils.saveListToFileWithoutUndownloaded(Constant.dir
				, Constant.curSpotUser.getUserInfo().getId()
						+ MeasurementConstant.FILE_NAME_SPOT_LIST_OLD
				, Constant.curSpotUser.getSpotCheckList());
	}


}
