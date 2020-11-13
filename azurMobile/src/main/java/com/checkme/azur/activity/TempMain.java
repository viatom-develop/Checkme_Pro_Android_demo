package com.checkme.azur.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.ReadFileListener;
import com.checkme.azur.element.CommonCreator;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.CommonItem;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.measurement.TempItem;
import com.checkme.azur.measurement.User;
import com.checkme.azur.tools.NoInfoViewUtils;
import com.checkme.azur.tools.ShareUtils;
import com.checkme.azur.tools.StringMaker;
import com.checkme.azur.utils.CommonItemFilter;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.JsonUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.utils.TimeComparator;

public class TempMain extends BaseActivity implements OnMenuItemClickListener, ReadFileListener{

	User defaultUser = Constant.defaultUser;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Constant.CMD_TYPE_TEMP:
					ReadLocalTempList();
					break;
			}
		};
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNTEMP_CLICKED);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temp_main);

		initUI();
		getTempList();
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

	/**
	 * UI初始化
	 */
	public void initUI() {
		reFreshActionBarBn(false, false);
		reFreshTitle(getResources().getString(R.string.title_temp));
	}

	/**
	 * 获取temp列表
	 */
	public void getTempList() {
		// 先判断是否缓存有数据
		if (defaultUser.getTempList().size() == 0) {// 没有数据，读取
			if (Constant.btConnectFlag == true) {
				Constant.binder.interfaceReadFile(MeasurementConstant.FILE_NAME_TEMP_LIST
						, Constant.CMD_TYPE_TEMP, 5000, this);
			} else {
				ReadLocalTempList();
			}
		} else {// 有数据，直接显示
			refreshView();
		}
	}

	@Override
	public void onReadPartFinished(String fileName, byte fileType,
								   float percentage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
		// TODO Auto-generated method stub
		//删除旧文件，保存新数据表到本地文件
		FileDriver.delFile(Constant.dir, fileName);
		FileDriver.write(Constant.dir,  fileName, fileBuf);
		MsgUtils.sendMsg(handler, fileType);
	}

	@Override
	public void onReadFailed(String fileName, byte fileType, byte errCode) {
		// TODO Auto-generated method stub
		//读失败同样删除旧文件
//		FileDriver.delFile(Constant.dir, fileName);
		MsgUtils.sendMsg(handler, fileType, errCode);
	}

	/**
	 * 离线模式时读取本地列表
	 */
	public void ReadLocalTempList() {

		//读取上次退出是存储的旧列表
		List<TempItem> oldTempList = FileUtils.readTempList(Constant.dir
				, MeasurementConstant.FILE_NAME_TEMP_LIST_OLD);
		if (oldTempList != null && oldTempList.size() != 0) {
			Constant.defaultUser.addTempList(oldTempList);
		}
		//读取新下载的列表，合并到旧列表
		List<TempItem> newTempList = FileUtils.readTempList(Constant.dir
				, MeasurementConstant.FILE_NAME_TEMP_LIST);
		if (newTempList != null && newTempList.size() != 0) {
			Constant.defaultUser.addTempList(newTempList);
		}
		//去除重复，并倒序排列
		CommonItemFilter.removeSameItems(Constant.defaultUser.getTempList());
		Collections.sort((List<CommonItem>)Constant.defaultUser
						.getList(MeasurementConstant.CMD_TYPE_TEMP)
				, new TimeComparator());

		refreshView();

		refreshView();
	}

	public void refreshView() {
		((ProgressBar) findViewById(R.id.TempMainListPro))
				.setVisibility(View.INVISIBLE);
		processTempList();
		refreshNoInfoView();
	}

	/**
	 * 重新筛选列表
	 */
	public void processTempList() {
		if (defaultUser.getTempList().size() != 0) {
			List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
			ArrayList<TempItem> list = defaultUser.getTempList();

			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("Times",StringMaker.makeTimeString(list.get(i).getDate()));
				listItem.put("Dates",StringMaker.makeDateString(list.get(i).getDate()));
				//判断摄氏度还是华氏度
				if (Constant.thermometerUnit == Constant.THERMOMETER_C) {
					listItem.put("Result", list.get(i).getResult()+"℃");
				}else {
					listItem.put("Result", String.format("%.1f", cToF(list.get(i).getResult()))+"℉");
				}

//				listItem.put("CheckMode",Constant.TEMP_CHECK_MODE_IMG[list.get(i).getCheckMode()]);
				//如果是体温，显示笑脸，否则显示温度计
				if (list.get(i).getMeasuringMode() == 0) {
//					listItem.put("IMGResult", Constant.RESULT_IMG[list.get(i).getImgResult()]);
					listItem.put("IMGResult", R.drawable.none);
				}else {
					listItem.put("IMGResult", Constant.TEMP_CHECK_MODE_IMG[list.get(i).getMeasuringMode()]);
				}
				listItems.add(listItem);
			}

			SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.TempMainList);
			SimpleAdapter simpleAdapter = new SimpleAdapter(TempMain.this,
					listItems, R.layout.list_temp, new String[] { "Times", "Dates",
					"IMGResult", "Result" }, new int[] {
					R.id.ListTempTextTime, R.id.ListTempTextDate,
					R.id.ListTempImgResult,R.id.ListTempTextResult });

			listView.setAdapter(simpleAdapter);
			listView.setMenuCreator(CommonCreator.makeSmallSwipeMenuCreator(
					getApplicationContext()));
			listView.setOnMenuItemClickListener(this);
		} else {
			View view = findViewById(R.id.TempMainLinearList);
			view.setVisibility(View.INVISIBLE);
		}
	}

	private void refreshNoInfoView() {
		if (defaultUser.getTempList()==null || defaultUser.getTempList().size()==0) {
			NoInfoViewUtils.showNoInfoView(TempMain.this, findViewById(R.id.ImgNoInfo));
		}else {
			NoInfoViewUtils.hideNoInfoView(TempMain.this, findViewById(R.id.ImgNoInfo));
		}
	}

	private float cToF(float c) {
		float f = c * 9 / 5 + 32;
		return f;
	}

	@Override
	public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
		// TODO Auto-generated method stub
		if (index == 0) {//如果是第0个按键按下
			List<TempItem> list= Constant.defaultUser.getTempList();
			list.remove(position);
			FileUtils.saveListToFile(Constant.dir,MeasurementConstant.FILE_NAME_TEMP_LIST, (ArrayList<? extends CommonItem>) list);
			refreshView();
		}

		return false;
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
			default:
				break;
		}
	}
	private void onBnShareClicked() {
		// TODO share
		if(Constant.defaultUser.getSpo2list().size()!=0){
			String SpO2=getJsonString();
			FileDriver.write(Constant.dir, "TMP.txt", SpO2.getBytes());
			ShareUtils.shareToNet(getApplicationContext(), "TMP.txt");
		}

	}
	private String getJsonString() {
		StringBuilder  builder=new StringBuilder();
		for (TempItem item : Constant.defaultUser.getTempList()) {
			builder.append(JsonUtils.makeTMPString(item)+"\n");
		}
		return builder.toString();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			if (Constant.defaultUser==null)
				return;
			FileUtils.saveListToFile(Constant.dir
					, MeasurementConstant.FILE_NAME_TEMP_LIST_OLD
					, Constant.defaultUser.getTempList());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return super.onKeyDown(keyCode, event);
		}
		return false;
	}
}
