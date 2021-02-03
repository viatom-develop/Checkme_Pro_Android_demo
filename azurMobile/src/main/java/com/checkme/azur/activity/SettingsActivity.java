package com.checkme.azur.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.checkme.azur.EventBusEvent.AgreeClickedEvent;
import com.checkme.azur.fragment.PolicyDialogFragment;
import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.ChooseDeviceFragment;
import com.checkme.azur.fragment.SettingsFragment;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

public class SettingsActivity extends BaseActivity implements PolicyDialogFragment.OnAgreeClickedListener {

	private Handler handler = new Handler(){

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case Constant.MSG_GOTO_CHOOSE_DEVICE:
					initChooseDeviceFragment();
					reFreshActionBarBn(1,false,false);
					break;
				case Constant.MSG_GOTO_DIALOG:
					dialog();
					break;
				/*case Constant.MSG_SHOW_POLICY_DIALOG:
					initPolicyDialogFragment();
					break;*/
//				case Constant.MSG_UPLOAD_DAT:
//					//TODO
//					uploadingData();
//					break;
				default:
					break;
			}

		};
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNSETTINGS_CLICKED);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		reFreshTitle(Constant.getString(R.string.title_settings));
		reFreshActionBarBn(0,false,false);
		initListFragment();
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

	public void dialog(){
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);

		builder.setTitle(Constant.getString(R.string.warning));
		builder.setMessage(Constant.getString(R.string.clear));

		builder.setPositiveButton(Constant.getString(R.string.yes), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				FileDriver.deleteAllInfo(Constant.dir);

				Toast.makeText(SettingsActivity.this,Constant.getString(R.string.clearS) , Toast.LENGTH_SHORT).show();
				Constant.defaultUser.getSpo2list().clear();
				Constant.defaultUser.getTempList().clear();
				Constant.defaultUser.getSlmList().clear();
				Constant.defaultUser.getEcgList().clear();
				String name = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceName");
				String mode = PreferenceUtils.readStrPreferences(getApplicationContext(), name+"CKM_MODE");
				if (mode == null || mode.equals("") || mode.equals("MODE_HOME")) {
					LogUtils.d("普通模式");
					Constant.curUser.getPedList().clear();
				}else {
					LogUtils.d("医院模式");
				}
//				Constant.curUser.getPedList().clear();
				Constant.userList = null;
				Constant.spotUserList = null;

			}
		});

		builder.setNegativeButton(Constant.getString(R.string.cancel), null);

		builder.create().show();


	}

	@Override
	public void onAgreeClicked(boolean b) {
		LogUtils.d("On Agree Clicked receive at setting activity");
		EventBus.getDefault().post(new AgreeClickedEvent(b));
	}

	private void initListFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SettingsFragment fragment = new SettingsFragment();
		fragment.setArguments(handler);
		fragmentTransaction.add(R.id.SettingsReMain,fragment);
		fragmentTransaction.commit();
	}

	private void initChooseDeviceFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		ChooseDeviceFragment fragment = new ChooseDeviceFragment();
		fragment.setCallHandler(handler);
		fragmentTransaction.replace(R.id.SettingsReMain,fragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
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
					setBehindView(null);//刷新左菜单
				}
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
				setBehindView(null);//刷新左菜单
			}
		}
		return false;
	}


}
